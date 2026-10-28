/*
 * Prediction logic ported from RuneLite's Time Tracking plugin
 * (net.runelite.client.plugins.timetracking.farming.FarmingTracker),
 * Copyright (c) 2018 Abex and RuneLite contributors, BSD 2-Clause License.
 * See NOTICE in this repository.
 */
package com.drfino.treepings.farming;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reads the patch states that RuneLite's Time Tracking plugin already saved and predicts
 * when each belled patch finishes growing. Pure logic: no client, no network.
 */
public final class FarmingPredictor
{
	/** Patch types this plugin schedules dings for. */
	private static final Set<PatchImplementation> SUPPORTED = EnumSet.of(
		PatchImplementation.TREE,
		PatchImplementation.FRUIT_TREE,
		PatchImplementation.HARDWOOD_TREE,
		PatchImplementation.CALQUAT,
		PatchImplementation.CELASTRUS,
		PatchImplementation.REDWOOD,
		PatchImplementation.SPIRIT_TREE,
		PatchImplementation.CRYSTAL_TREE
	);

	private final FarmingWorld world = new FarmingWorld();

	/** Growing trees skipped by the last call because RuneLite hasn't learned the farming clock yet. */
	private int untimedCount;

	/**
	 * Returns every tree-type patch that RuneLite last saw growing, with its predicted
	 * finish time (which may already be in the past: the caller still needs it to
	 * schedule follow-on dings for replants done on mobile).
	 *
	 * @param config the stored Time Tracking values for one profile
	 * @param requireBell only include patches whose Time Tracking bell is on
	 */
	public List<ReadyPatch> predictGrowing(ConfigSource config, boolean requireBell)
	{
		List<ReadyPatch> out = new ArrayList<>();
		untimedCount = 0;
		for (Map.Entry<Tab, Set<FarmingPatch>> tab : world.getTabs().entrySet())
		{
			for (FarmingPatch patch : tab.getValue())
			{
				if (!SUPPORTED.contains(patch.getImplementation()))
				{
					continue;
				}
				if (requireBell && !"true".equalsIgnoreCase(config.get(patch.notifyConfigKey())))
				{
					continue;
				}

				ReadyPatch ready = predict(patch, config);
				if (ready != null)
				{
					out.add(ready);
				}
			}
		}
		return out;
	}

	private ReadyPatch predict(FarmingPatch patch, ConfigSource config)
	{
		String stored = config.get(patch.configKey());
		if (stored == null)
		{
			return null;
		}

		int value;
		long unixTime;
		String[] parts = stored.split(":");
		if (parts.length != 2)
		{
			return null;
		}
		try
		{
			value = Integer.parseInt(parts[0]);
			unixTime = Long.parseLong(parts[1]);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
		if (unixTime <= 0)
		{
			return null;
		}

		PatchState state = patch.getImplementation().forVarbitValue(value);
		if (state == null || state.getCropState() != CropState.GROWING)
		{
			return null;
		}
		// An empty patch regrowing weeds also counts as "growing". That's not a crop.
		if (state.getProduce() == Produce.WEEDS || state.getProduce() == Produce.SCARECROW)
		{
			return null;
		}

		int tickrate = state.getTickRate();
		if (tickrate <= 0)
		{
			return null;
		}
		if (!offsetIsConfident(config, tickrate))
		{
			untimedCount++;
			return null;
		}

		int stage = state.getStage();
		int stages = state.getStages();
		long tickTime = getTickTime(config, tickrate, 0, unixTime);
		long doneEstimate = getTickTime(config, tickrate, stages - 1 - stage, tickTime);

		Produce produce = state.getProduce();
		long growSeconds = (long) produce.getTickrate() * 60L * Math.max(0, produce.getStages() - 1);
		return new ReadyPatch(
			patch.getImplementation().name().toLowerCase(Locale.ROOT),
			produce.getName(),
			patch.getRegion().getName(),
			doneEstimate,
			growSeconds);
	}

	/**
	 * How many growing trees the last {@link #predictGrowing} call had to skip because RuneLite
	 * hasn't yet watched a long enough growth tick to know the farming clock.
	 */
	public int getUntimedCount()
	{
		return untimedCount;
	}

	/** Same confidence rule RuneLite uses before it sends a farming notification. */
	private static boolean offsetIsConfident(ConfigSource config, int tickRate)
	{
		Integer precision = config.getInt(TimeTrackingKeys.FARM_TICK_OFFSET_PRECISION);
		Integer offset = config.getInt(TimeTrackingKeys.FARM_TICK_OFFSET);
		return precision != null && offset != null && (precision >= tickRate || precision >= 40);
	}

	/** Ported from FarmingTracker#getTickTime. */
	static long getTickTime(ConfigSource config, int tickRate, int ticks, long requestedTime)
	{
		Integer precision = config.getInt(TimeTrackingKeys.FARM_TICK_OFFSET_PRECISION);
		Integer offset = config.getInt(TimeTrackingKeys.FARM_TICK_OFFSET);

		// All offsets are negative but are stored as positive
		long calculatedOffsetTime = 0L;
		if (precision != null && offset != null && (precision >= tickRate || precision >= 40))
		{
			calculatedOffsetTime = (offset % tickRate) * 60L;
		}

		long unixNow = requestedTime + calculatedOffsetTime;
		long timeOfCurrentTick = unixNow - (unixNow % (tickRate * 60L));
		long timeOfGoalTick = timeOfCurrentTick + ((long) ticks * tickRate * 60L);
		return timeOfGoalTick - calculatedOffsetTime;
	}
}
