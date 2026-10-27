package com.drfino.treepings.farming;

/**
 * Config keys written by RuneLite's built-in Time Tracking plugin.
 * Mirrors net.runelite.client.plugins.timetracking.TimeTrackingConfig so this plugin
 * only reads that data and never writes to it.
 */
public final class TimeTrackingKeys
{
	public static final String CONFIG_GROUP = "timetracking";
	public static final String FARM_TICK_OFFSET = "farmTickOffset";
	public static final String FARM_TICK_OFFSET_PRECISION = "farmTickOffsetPrecision";
	public static final String AUTOWEED = "autoweed";
	public static final String NOTIFY = "notify";

	private TimeTrackingKeys()
	{
	}
}
