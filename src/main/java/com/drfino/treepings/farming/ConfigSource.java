package com.drfino.treepings.farming;

/**
 * Read-only view of the Time Tracking plugin's stored values for one RuneScape profile.
 * In the plugin this is backed by ConfigManager; in tests by a map.
 */
@FunctionalInterface
public interface ConfigSource
{
	/** Returns the raw stored string for {@code timetracking.<profile>.<key>}, or null. */
	String get(String key);

	default Integer getInt(String key)
	{
		String v = get(key);
		if (v == null)
		{
			return null;
		}
		try
		{
			return Integer.parseInt(v.trim());
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}
}
