package com.drfino.treepings;

import com.drfino.treepings.farming.ConfigSource;
import com.drfino.treepings.farming.FarmingPredictor;
import com.drfino.treepings.farming.ReadyPatch;
import com.drfino.treepings.farming.TimeTrackingKeys;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Provides;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
@PluginDescriptor(
	name = "Tree Pings",
	description = "Puts your tree finish times on a private Google Calendar so your phone dings when they're ready",
	tags = {"farming", "tree", "timer", "notification", "phone", "calendar", "yew"}
)
public class TreePingsPlugin extends Plugin
{
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final long LOOKAHEAD_SECONDS = 13L * 24 * 3600; // script keeps 14 days
	private static final long MAX_RETRY_SECONDS = 15 * 60;

	@Inject
	private TreePingsConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private ChatMessageManager chatMessageManager;

	private final FarmingPredictor predictor = new FarmingPredictor();

	private ScheduledFuture<?> pending;
	private volatile String lastSentBatches;
	private volatile long retrySeconds;
	private volatile boolean warnedBadSetup;
	/** Time Tracking has changed since the last sync. */
	private volatile boolean dirty;

	@Provides
	TreePingsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TreePingsConfig.class);
	}

	@Override
	protected void startUp()
	{
		lastSentBatches = null;
		retrySeconds = 0;
		warnedBadSetup = false;
		schedule(5);
	}

	@Override
	protected void shutDown()
	{
		synchronized (this)
		{
			if (pending != null)
			{
				pending.cancel(false);
				pending = null;
			}
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged e)
	{
		// Logged in: sync whatever Time Tracking already knows, even if nothing changes this session.
		schedule(5);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (TimeTrackingKeys.CONFIG_GROUP.equals(e.getGroup()))
		{
			// Time Tracking saved new patch data. Wait until the run is over before syncing:
			// every new patch change pushes the sync back again.
			dirty = true;
			schedule(config.syncDelay() * 60L);
			return;
		}

		if (!TreePingsConfig.GROUP.equals(e.getGroup()))
		{
			return;
		}

		if ("sendTest".equals(e.getKey()))
		{
			if (config.sendTest())
			{
				configManager.setConfiguration(TreePingsConfig.GROUP, "sendTest", false);
				executor.execute(this::sendTest);
			}
			return;
		}

		// URL, token or batching settings changed: resend everything.
		lastSentBatches = null;
		warnedBadSetup = false;
		schedule(2);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		// Logging out ends the run, so don't wait out the sync delay.
		if (e.getGameState() == GameState.LOGIN_SCREEN && dirty)
		{
			schedule(2);
		}
	}

	@Subscribe
	public void onClientShutdown(ClientShutdown e)
	{
		// Closing RuneLite right after a run: send it now, or the dings would be lost.
		if (!dirty)
		{
			return;
		}
		synchronized (this)
		{
			if (pending != null)
			{
				pending.cancel(false);
				pending = null;
			}
		}
		dirty = false;
		try
		{
			e.waitFor(syncNow());
		}
		catch (Exception ex)
		{
			log.warn("Tree Pings sync on shutdown failed", ex);
		}
	}

	private synchronized void schedule(long delaySeconds)
	{
		if (pending != null)
		{
			pending.cancel(false);
		}
		pending = executor.schedule(this::sync, delaySeconds, TimeUnit.SECONDS);
	}

	private void sync()
	{
		dirty = false;
		try
		{
			syncNow();
		}
		catch (Exception ex)
		{
			log.warn("Tree Pings sync failed", ex);
		}
	}

	private CompletableFuture<Boolean> syncNow()
	{
		HttpUrl url = scriptUrl();
		if (url == null || config.token().isEmpty())
		{
			return CompletableFuture.completedFuture(false);
		}

		String profile = configManager.getRSProfileKey();
		if (profile == null)
		{
			// Not logged in since the client started; nothing new to report.
			return CompletableFuture.completedFuture(true);
		}

		ConfigSource source = key -> configManager.getConfiguration(TimeTrackingKeys.CONFIG_GROUP, profile, key);
		List<ReadyPatch> patches = predictor.predictGrowing(source, config.requireBell());

		Set<String> followOn = Arrays.stream(config.followOnProduce().split(","))
			.map(s -> s.trim().toLowerCase(Locale.ROOT))
			.filter(s -> !s.isEmpty())
			.collect(Collectors.toSet());

		List<Batch> batches = Batcher.build(patches, Instant.now().getEpochSecond(),
			config.groupWindow() * 60L, followOn, config.followOnCycles(), LOOKAHEAD_SECONDS);

		String batchesJson = gson.toJson(batches);
		if (batchesJson.equals(lastSentBatches))
		{
			return CompletableFuture.completedFuture(true);
		}

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("token", config.token());
		body.put("test", false);
		body.put("batches", batches);

		log.debug("Tree Pings sending {} dings: {}", batches.size(), batches);
		return post(url, gson.toJson(body), ok ->
		{
			if (ok)
			{
				lastSentBatches = batchesJson;
				retrySeconds = 0;
			}
			else
			{
				retrySeconds = retrySeconds == 0 ? 60 : Math.min(retrySeconds * 2, MAX_RETRY_SECONDS);
				schedule(retrySeconds);
			}
		});
	}

	private void sendTest()
	{
		HttpUrl url = scriptUrl();
		if (url == null || config.token().isEmpty())
		{
			chat("Tree Pings: set the Apps Script URL and token first.");
			return;
		}

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("token", config.token());
		body.put("test", true);
		post(url, gson.toJson(body), ok -> chat(ok
			? "Tree Pings: test event created. Your phone should ding in about 2 minutes."
			: "Tree Pings: test failed. Check the URL and token."));
	}

	private interface Done
	{
		void accept(boolean ok);
	}

	private CompletableFuture<Boolean> post(HttpUrl url, String json, Done done)
	{
		CompletableFuture<Boolean> result = new CompletableFuture<>();
		Request request = new Request.Builder()
			.url(url)
			.post(RequestBody.create(JSON, json))
			.build();

		// Apps Script answers a POST with a 302; OkHttp follows it with a GET that returns our JSON reply.
		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.debug("Tree Pings POST failed", e);
				done.accept(false);
				result.complete(false);
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				try (ResponseBody rb = response.body())
				{
					String text = rb == null ? "" : rb.string();
					boolean ok = false;
					String error = null;
					if (response.isSuccessful())
					{
						try
						{
							JsonObject reply = gson.fromJson(text, JsonObject.class);
							ok = reply != null && reply.has("ok") && reply.get("ok").getAsBoolean();
							if (!ok && reply != null && reply.has("error"))
							{
								error = reply.get("error").getAsString();
							}
						}
						catch (RuntimeException parse)
						{
							error = "unexpected reply (is the web app deployed with access: Anyone?)";
						}
					}
					else
					{
						error = "HTTP " + response.code();
					}

					if (!ok)
					{
						log.debug("Tree Pings sync rejected: {} {}", error, text);
						if ("bad token".equals(error) && !warnedBadSetup)
						{
							warnedBadSetup = true;
							chat("Tree Pings: the script rejected the token. Check it matches the TOKEN script property.");
						}
					}
					done.accept(ok);
					result.complete(ok);
				}
				catch (IOException e)
				{
					done.accept(false);
					result.complete(false);
				}
			}
		});
		return result;
	}

	private HttpUrl scriptUrl()
	{
		String raw = config.scriptUrl().trim();
		if (raw.isEmpty())
		{
			return null;
		}
		HttpUrl url = HttpUrl.parse(raw);
		if (url == null || !"https".equals(url.scheme()) || !url.host().endsWith("google.com"))
		{
			log.debug("Tree Pings: ignoring script URL that isn't an https Google URL: {}", raw);
			return null;
		}
		return url;
	}

	private void chat(String message)
	{
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message)
			.build());
	}
}
