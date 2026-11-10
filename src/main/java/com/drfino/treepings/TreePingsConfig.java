package com.drfino.treepings;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(TreePingsConfig.GROUP)
public interface TreePingsConfig extends Config
{
	String GROUP = "treepings";

	@ConfigSection(
		name = "Advanced",
		description = "Fine-tuning. The defaults work for most players.",
		position = 100,
		closedByDefault = true
	)
	String advanced = "advanced";

	@ConfigItem(
		keyName = "setupGuide",
		name = "Open setup guide",
		description = "Tick this to open the 3-minute setup guide in your browser. It unticks itself.",
		position = 0
	)
	default boolean setupGuide()
	{
		return false;
	}

	@ConfigItem(
		keyName = "scriptUrl",
		name = "Script URL",
		description = "The Web app URL you copied when you deployed your Tree Pings script (ends in /exec). Paste it and your phone gets a test ding.",
		position = 1
	)
	default String scriptUrl()
	{
		return "";
	}

	// Made up by the plugin the first time it runs; the script remembers the first one it sees.
	@ConfigItem(
		keyName = "token",
		name = "",
		description = "",
		secret = true,
		hidden = true
	)
	default String token()
	{
		return "";
	}

	@ConfigItem(
		keyName = "sendTest",
		name = "Send test ding",
		description = "Tick this to create a test event 2 minutes from now. It unticks itself.",
		position = 3
	)
	default boolean sendTest()
	{
		return false;
	}

	@Range(max = 23)
	@ConfigItem(
		keyName = "awakeFrom",
		name = "Awake from (hour)",
		description = "Hour you wake up, 0-23. Anything that finishes while you sleep waits for one ding at this hour.",
		position = 4
	)
	default int awakeFrom()
	{
		return 8;
	}

	@Range(max = 23)
	@ConfigItem(
		keyName = "awakeUntil",
		name = "Awake until (hour)",
		description = "Hour you go to sleep, 0-23 (0 = midnight). Set it equal to 'Awake from' to get dings around the clock.",
		position = 5
	)
	default int awakeUntil()
	{
		return 0;
	}

	@Range(max = 4)
	@ConfigItem(
		keyName = "followOnCycles",
		name = "Follow-on runs",
		description = "Runs a day after your morning run. Tree Pings assumes you replant at every ding and keeps this rhythm going for 5 days, so mobile runs still get dings. 1 = morning + afternoon. 0 = only ding for what RuneLite has seen.",
		position = 6
	)
	default int followOnCycles()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "chatMessages",
		name = "Chat messages",
		description = "When you log in or your calendar updates, say when the next ding is. Problems are always shown, even with this off.",
		position = 7
	)
	default boolean chatMessages()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tipShown",
		name = "",
		description = "",
		hidden = true
	)
	default boolean tipShown()
	{
		return false;
	}

	@Range(min = 1, max = 30)
	@Units(Units.MINUTES)
	@ConfigItem(
		keyName = "syncDelay",
		name = "Sync delay",
		description = "Wait this long after your last patch change before updating the calendar, so a run in progress isn't synced halfway. Logging out or closing RuneLite syncs right away.",
		position = 1,
		section = advanced
	)
	default int syncDelay()
	{
		return 5;
	}

	@Range(max = 120)
	@Units(Units.MINUTES)
	@ConfigItem(
		keyName = "groupWindow",
		name = "Group window",
		description = "Patches finishing within this many minutes of each other share one ding",
		position = 2,
		section = advanced
	)
	default int groupWindow()
	{
		return 45;
	}

	@ConfigItem(
		keyName = "requireBell",
		name = "Only belled patches",
		description = "Only ding for patches whose bell is turned on in the Time Tracking panel",
		position = 3,
		section = advanced
	)
	default boolean requireBell()
	{
		return false;
	}
}
