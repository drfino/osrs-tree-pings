package com.drfino.treepings.farming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class FarmingPredictorTest
{
	private static final long SEEN = 1_800_000_000L;

	/** Time Tracking's saved data with just the Varrock tree patch set to {@code varbitValue}. */
	private static List<ReadyPatch> varrock(int varbitValue)
	{
		ConfigSource config = key ->
		{
			if (key.equals(TimeTrackingKeys.FARM_TICK_OFFSET))
			{
				return "0";
			}
			if (key.equals(TimeTrackingKeys.FARM_TICK_OFFSET_PRECISION))
			{
				return "60";
			}
			return key.startsWith("12854.") ? varbitValue + ":" + SEEN : null;
		};
		return new FarmingPredictor().predictGrowing(config, false);
	}

	@Test
	public void growingYewIsPredicted()
	{
		List<ReadyPatch> out = varrock(38); // yew sapling, a few stages in
		assertTrue(!out.isEmpty());
		for (ReadyPatch p : out)
		{
			assertEquals("Yew", p.getProduceName());
			assertTrue(p.getReadyAt() > SEEN);
		}
	}

	@Test
	public void yewWaitingForHealthCheckIsDoneNotGrowing()
	{
		// Walking past a grown yew must not count as it finishing right then.
		assertTrue(varrock(45).isEmpty()); // yew tree, Check-health
	}
}
