package com.drfino.treepings;

import com.drfino.treepings.farming.ReadyPatch;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import java.util.TreeSet;

/**
 * Turns predicted patches into a small number of dings.
 *
 * <ul>
 * <li>Patches of the same crop that finish within {@code windowSeconds} of each other share
 * one ding, timed for when the LAST of them is ready.</li>
 * <li>Anything that finishes outside the player's awake hours is held until they wake up and
 * goes into one combined "Morning run" ding, together with anything else finishing within
 * the group window after waking.</li>
 * <li>Regular trees (yews, magics, etc.) also get follow-on dings, one grow cycle after each
 * ding, for replants done on mobile where RuneLite can't see them. Because farming growth is
 * tick-aligned, a replant done right after a ding finishes about one grow cycle later.</li>
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

		Entry(String produce, String location, long dingAt, boolean followOn, boolean heldOvernight)
		{
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
			long ding = awake.dingTime(p.getReadyAt());
			entries.add(new Entry(p.getProduceName(), p.getLocation(), ding, false, ding != p.getReadyAt()));

			if ("tree".equals(p.getTypeKey()) && p.getGrowSeconds() > 0)
			{
				// Assume the player replants when the ding goes off, not when the tree finished.
				long base = ding;
				for (int k = 1; k <= followOnCycles; k++)
				{
					long ready = base + p.getGrowSeconds();
					long next = awake.dingTime(ready);
					entries.add(new Entry(p.getProduceName(), p.getLocation(), next, true, next != ready));
					base = next;
				}
			}
		}

		entries.removeIf(e -> e.dingAt <= unixNow || e.dingAt > horizon);
		entries.sort(Comparator.comparingLong(e -> e.dingAt));

		List<Batch> out = new ArrayList<>();
		Set<Entry> used = Collections.newSetFromMap(new IdentityHashMap<>());

		// Morning runs: everything held overnight, plus anything finishing soon after waking.
		Set<Long> wakeTimes = new TreeSet<>();
		for (Entry e : entries)
		{
			if (e.heldOvernight)
			{
				wakeTimes.add(e.dingAt);
			}
		}
		for (long wake : wakeTimes)
		{
			List<Entry> run = new ArrayList<>();
			for (Entry e : entries)
			{
				if (used.contains(e))
				{
					continue;
				}
				if ((e.heldOvernight && e.dingAt == wake)
					|| (!e.heldOvernight && e.dingAt >= wake && e.dingAt - wake <= windowSeconds))
				{
					run.add(e);
				}
			}
			used.addAll(run);
			out.add(morningBatch(run));
		}

		// Everything else: one ding per crop per group window.
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
					out.add(cropBatch(cluster));
					cluster = new ArrayList<>();
				}
				cluster.add(e);
			}
			if (!cluster.isEmpty())
			{
				out.add(cropBatch(cluster));
			}
		}

		out.sort(Comparator.comparingLong((Batch b) -> b.readyAt).thenComparing(b -> b.key));
		return out;
	}

	private static Batch cropBatch(List<Entry> cluster)
	{
		String produce = cluster.get(0).produce;
		long readyAt = cluster.get(cluster.size() - 1).dingAt;
		boolean allFollowOn = cluster.stream().allMatch(e -> e.followOn);
		int n = cluster.size();

		String title = produce + (n > 1 ? " x" + n : "") + " ready" + (allFollowOn ? " (if replanted)" : "");
		String detail = places(cluster)
			+ (allFollowOn ? ". Assumes you replanted at the last ding." : "");
		return new Batch(slug(produce) + "-" + readyAt, title, readyAt, detail);
	}

	private static Batch morningBatch(List<Entry> run)
	{
		long readyAt = run.get(run.size() - 1).dingAt;
		Map<String, List<Entry>> byProduce = new LinkedHashMap<>();
		for (Entry e : run)
		{
			byProduce.computeIfAbsent(e.produce, k -> new ArrayList<>()).add(e);
		}

		List<String> counts = new ArrayList<>();
		List<String> details = new ArrayList<>();
		for (Map.Entry<String, List<Entry>> crop : byProduce.entrySet())
		{
			int n = crop.getValue().size();
			counts.add(crop.getKey() + (n > 1 ? " x" + n : ""));
			boolean allFollowOn = crop.getValue().stream().allMatch(e -> e.followOn);
			details.add(crop.getKey() + (allFollowOn ? " (if replanted)" : "") + ": " + places(crop.getValue()));
		}
		return new Batch("morning-" + readyAt, "Morning run: " + String.join(", ", counts), readyAt,
			String.join("\n", details));
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
