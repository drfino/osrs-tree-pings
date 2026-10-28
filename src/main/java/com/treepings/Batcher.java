package com.treepings;

import com.treepings.farming.ReadyPatch;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Turns predicted patches into a small number of dings, built around regular-tree runs.
 *
 * <ul>
 * <li>Regular trees (yews, magics, etc.) set the schedule. Patches finishing within
 * {@code windowSeconds} of each other share one ding, timed for when the last is ready.
 * They also get follow-on dings, one grow cycle after each ding, for replants done on mobile
 * where RuneLite can't see them.</li>
 * <li>Slower crops (fruit trees, calquat, hardwoods, etc.) never ding on their own while a
 * regular-tree ding is coming: they're listed on the next one ("also Calquat x2"). Only when
 * no regular-tree ding follows do they get a ding of their own.</li>
 * <li>Anything finishing outside the player's awake hours waits until they wake up; a ding
 * that includes such an entry is labelled "Morning run".</li>
 * </ul>
 */
final class Batcher
{
	/** The player's waking hours, in their local time zone. */
	static final class Awake
	{
		private final ZoneId zone;
		private final int fromHour;
		private final int untilHour;

		/** {@code untilHour} 0 means midnight. Equal hours mean always awake. */
		Awake(ZoneId zone, int fromHour, int untilHour)
		{
			this.zone = zone;
			this.fromHour = fromHour;
			this.untilHour = untilHour;
		}

		static Awake always(ZoneId zone)
		{
			return new Awake(zone, 0, 0);
		}

		/** When to ding for something ready at {@code t}: {@code t} itself, or the next wake-up. */
		long dingTime(long t)
		{
			if (fromHour == untilHour)
			{
				return t;
			}
			ZonedDateTime z = Instant.ofEpochSecond(t).atZone(zone);
			int m = z.getHour() * 60 + z.getMinute();
			boolean awake = fromHour < untilHour
				? m >= fromHour * 60 && m < untilHour * 60
				: m >= fromHour * 60 || m < untilHour * 60;
			if (awake)
			{
				return t;
			}
			ZonedDateTime wake = z.toLocalDate().atTime(fromHour, 0).atZone(zone);
			if (!wake.isAfter(z))
			{
				wake = wake.plusDays(1);
			}
			return wake.toEpochSecond();
		}
	}

	private static final class Entry
	{
		final String produce;
		final String location;
		final long dingAt;
		final boolean followOn;
		final boolean heldOvernight;
		final boolean anchor;

		Entry(String produce, String location, long dingAt, boolean followOn, boolean heldOvernight, boolean anchor)
		{
			this.anchor = anchor;
			this.produce = produce;
			this.location = location;
			this.dingAt = dingAt;
			this.followOn = followOn;
			this.heldOvernight = heldOvernight;
		}
	}

	private Batcher()
	{
	}

	/**
	 * @param patches predicted patches; finish times may be in the past
	 * @param unixNow current time, unix seconds
	 * @param windowSeconds patches finishing within this span share a ding
	 * @param followOnCycles how many follow-on dings regular trees get
	 * @param lookaheadSeconds ignore anything further out than this
	 * @param awake the player's waking hours
	 */
	static List<Batch> build(List<ReadyPatch> patches, long unixNow, long windowSeconds,
		int followOnCycles, long lookaheadSeconds, Awake awake)
	{
		long horizon = unixNow + lookaheadSeconds;
		List<Entry> entries = new ArrayList<>();

		for (ReadyPatch p : patches)
		{
			boolean regularTree = "tree".equals(p.getTypeKey());
			long ding = awake.dingTime(p.getReadyAt());
			entries.add(new Entry(p.getProduceName(), p.getLocation(), ding, false, ding != p.getReadyAt(), regularTree));

			if (regularTree && p.getGrowSeconds() > 0)
			{
				// Assume the player replants when the ding goes off, not when the tree finished.
				long base = ding;
				for (int k = 1; k <= followOnCycles; k++)
				{
					long ready = base + p.getGrowSeconds();
					long next = awake.dingTime(ready);
					entries.add(new Entry(p.getProduceName(), p.getLocation(), next, true, next != ready, true));
					base = next;
				}
			}
		}

		entries.removeIf(e -> e.dingAt <= unixNow || e.dingAt > horizon);
		entries.sort(Comparator.comparingLong(e -> e.dingAt));

		List<Batch> out = new ArrayList<>();
		Set<Entry> used = Collections.newSetFromMap(new IdentityHashMap<>());

		// Regular-tree dings, with any waiting slower crops riding along.
		List<Entry> anchors = new ArrayList<>();
		for (Entry e : entries)
		{
			if (e.anchor)
			{
				anchors.add(e);
			}
		}
		int i = 0;
		while (i < anchors.size())
		{
			long start = anchors.get(i).dingAt;
			List<Entry> run = new ArrayList<>();
			while (i < anchors.size() && anchors.get(i).dingAt - start <= windowSeconds)
			{
				run.add(anchors.get(i++));
			}
			for (Entry e : entries)
			{
				if (!e.anchor && !used.contains(e) && e.dingAt <= start + windowSeconds)
				{
					run.add(e);
				}
			}
			used.addAll(run);
			out.add(runBatch(run));
		}

		// Slower crops with no regular-tree ding after them: one ding per crop per group window.
		Map<String, List<Entry>> byProduce = new TreeMap<>();
		for (Entry e : entries)
		{
			if (!used.contains(e))
			{
				byProduce.computeIfAbsent(e.produce, k -> new ArrayList<>()).add(e);
			}
		}
		for (List<Entry> list : byProduce.values())
		{
			List<Entry> cluster = new ArrayList<>();
			for (Entry e : list)
			{
				if (!cluster.isEmpty() && e.dingAt - cluster.get(0).dingAt > windowSeconds)
				{
					out.add(runBatch(cluster));
					cluster = new ArrayList<>();
				}
				cluster.add(e);
			}
			if (!cluster.isEmpty())
			{
				out.add(runBatch(cluster));
			}
		}

		out.sort(Comparator.comparingLong((Batch b) -> b.readyAt).thenComparing(b -> b.key));
		return out;
	}

	/** One ding. Title lists the regular trees first, then anything riding along. */
	private static Batch runBatch(List<Entry> run)
	{
		long readyAt = run.stream().mapToLong(e -> e.dingAt).max().getAsLong();

		Map<String, List<Entry>> lead = new LinkedHashMap<>();
		Map<String, List<Entry>> riders = new LinkedHashMap<>();
		boolean hasAnchor = run.stream().anyMatch(e -> e.anchor);
		for (Entry e : run)
		{
			(e.anchor || !hasAnchor ? lead : riders).computeIfAbsent(e.produce, k -> new ArrayList<>()).add(e);
		}

		boolean leadAllFollowOn = lead.values().stream().flatMap(List::stream).allMatch(e -> e.followOn);
		boolean morning = lead.values().stream().flatMap(List::stream).anyMatch(e -> e.heldOvernight);
		String title;
		if (morning)
		{
			List<String> all = counts(lead);
			all.addAll(counts(riders));
			title = "Morning run: " + String.join(", ", all) + (leadAllFollowOn ? " (if replanted)" : "");
		}
		else
		{
			title = String.join(", ", counts(lead)) + " ready" + (leadAllFollowOn ? " (if replanted)" : "")
				+ (riders.isEmpty() ? "" : ", also " + String.join(", ", counts(riders)));
		}

		List<String> details = new ArrayList<>();
		for (Map<String, List<Entry>> group : Arrays.asList(lead, riders))
		{
			for (Map.Entry<String, List<Entry>> crop : group.entrySet())
			{
				boolean allFollowOn = crop.getValue().stream().allMatch(e -> e.followOn);
				details.add(crop.getKey() + (allFollowOn ? " (if replanted)" : "") + ": " + places(crop.getValue()));
			}
		}
		String key = slug(lead.keySet().iterator().next()) + "-" + readyAt;
		return new Batch(key, title, readyAt, String.join("\n", details));
	}

	private static List<String> counts(Map<String, List<Entry>> byProduce)
	{
		List<String> out = new ArrayList<>();
		for (Map.Entry<String, List<Entry>> crop : byProduce.entrySet())
		{
			int n = crop.getValue().size();
			out.add(crop.getKey() + (n > 1 ? " x" + n : ""));
		}
		return out;
	}

	/**
	 * Crops whose finish time has passed, as RuneLite last saw them (e.g. "Yew x7, Calquat x2").
	 * Patches finished more than two weeks ago are ignored as abandoned.
	 */
	static String readyNow(List<ReadyPatch> patches, long unixNow)
	{
		List<ReadyPatch> ready = new ArrayList<>();
		for (ReadyPatch p : patches)
		{
			if (p.getReadyAt() <= unixNow && p.getReadyAt() > unixNow - 14L * 86400)
			{
				ready.add(p);
			}
		}
		ready.sort(Comparator.comparingLong(ReadyPatch::getReadyAt));
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (ReadyPatch p : ready)
		{
			counts.merge(p.getProduceName(), 1, Integer::sum);
		}
		List<String> out = new ArrayList<>();
		for (Map.Entry<String, Integer> c : counts.entrySet())
		{
			out.add(c.getKey() + (c.getValue() > 1 ? " x" + c.getValue() : ""));
		}
		return String.join(", ", out);
	}

	private static String places(List<Entry> entries)
	{
		Set<String> places = new LinkedHashSet<>();
		for (Entry e : entries)
		{
			places.add(e.location);
		}
		return String.join(", ", places);
	}

	private static String slug(String s)
	{
		return s.toLowerCase(Locale.ROOT).replace(' ', '-');
	}
}
