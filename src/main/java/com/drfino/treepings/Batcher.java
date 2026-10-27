package com.drfino.treepings;

import com.drfino.treepings.farming.ReadyPatch;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Turns predicted patches into a small number of dings.
 *
 * Patches of the same crop that finish within {@code windowSeconds} of each other become
 * one ding, timed for when the LAST of them is ready. For crops listed as follow-on crops,
 * the plugin also schedules a ding one grow cycle later (or several), for replants done on
 * mobile where RuneLite can't see them. Because farming growth is tick-aligned, a replant
 * done within one growth tick of the ding finishes exactly one grow cycle after it.
 */
final class Batcher
{
	private static final class Entry
	{
		final String produce;
		final String location;
		final long readyAt;
		final boolean followOn;

		Entry(String produce, String location, long readyAt, boolean followOn)
		{
			this.produce = produce;
			this.location = location;
			this.readyAt = readyAt;
			this.followOn = followOn;
		}
	}

	private Batcher()
	{
	}

	/**
	 * @param patches predicted patches, finish times may be in the past
	 * @param unixNow current time, unix seconds
	 * @param windowSeconds patches of one crop finishing within this span share a ding
	 * @param followOnProduce crop names (case-insensitive) that get follow-on dings, e.g. "yew"
	 * @param followOnCycles how many grow cycles past the real finish time to ding
	 * @param lookaheadSeconds ignore anything further out than this
	 */
	static List<Batch> build(List<ReadyPatch> patches, long unixNow, long windowSeconds,
		Set<String> followOnProduce, int followOnCycles, long lookaheadSeconds)
	{
		long horizon = unixNow + lookaheadSeconds;
		Map<String, List<Entry>> byProduce = new TreeMap<>();

		for (ReadyPatch p : patches)
		{
			List<Entry> list = byProduce.computeIfAbsent(p.getProduceName(), k -> new ArrayList<>());
			if (p.getReadyAt() > unixNow && p.getReadyAt() <= horizon)
			{
				list.add(new Entry(p.getProduceName(), p.getLocation(), p.getReadyAt(), false));
			}

			boolean wantsFollowOn = followOnProduce.contains(p.getProduceName().toLowerCase(Locale.ROOT));
			if (wantsFollowOn && p.getGrowSeconds() > 0)
			{
				for (int k = 1; k <= followOnCycles; k++)
				{
					long t = p.getReadyAt() + k * p.getGrowSeconds();
					if (t > unixNow && t <= horizon)
					{
						list.add(new Entry(p.getProduceName(), p.getLocation(), t, true));
					}
				}
			}
		}

		List<Batch> out = new ArrayList<>();
		for (List<Entry> entries : byProduce.values())
		{
			entries.sort(Comparator.comparingLong(e -> e.readyAt));
			List<Entry> cluster = new ArrayList<>();
			for (Entry e : entries)
			{
				if (!cluster.isEmpty() && e.readyAt - cluster.get(0).readyAt > windowSeconds)
				{
					out.add(toBatch(cluster));
					cluster = new ArrayList<>();
				}
				cluster.add(e);
			}
			if (!cluster.isEmpty())
			{
				out.add(toBatch(cluster));
			}
		}

		out.sort(Comparator.comparingLong((Batch b) -> b.readyAt).thenComparing(b -> b.key));
		return out;
	}

	private static Batch toBatch(List<Entry> cluster)
	{
		String produce = cluster.get(0).produce;
		long readyAt = cluster.get(cluster.size() - 1).readyAt;
		boolean allFollowOn = cluster.stream().allMatch(e -> e.followOn);

		Set<String> places = new LinkedHashSet<>();
		for (Entry e : cluster)
		{
			places.add(e.location);
		}
		int n = cluster.size();

		String title = produce + (n > 1 ? " x" + n : "") + " ready"
			+ (allFollowOn ? " (if replanted)" : "");
		String detail = String.join(", ", places)
			+ (allFollowOn ? ". Assumes you replanted right after the last ding." : "");
		String key = produce.toLowerCase(Locale.ROOT).replace(' ', '-') + "-" + readyAt;
		return new Batch(key, title, readyAt, detail);
	}
}
