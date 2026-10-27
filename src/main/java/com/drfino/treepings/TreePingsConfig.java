package com.drfino.treepings;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(TreePingsConfig.GROUP)
public interface TreePingsConfig extends Config
{
	String GROUP = "treepings";

	@ConfigItem(
		keyName = "scriptUrl",
		name = "Apps Script URL",
		description = "The Web app URL of your Tree Pings Google Apps Script (ends in /exec)",
		position = 1
	)
	default String scriptUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "token",
		name = "Token",
		description = "The TOKEN script property you set in the Apps Script",
		secret = true,
		position = 2
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

	@ConfigItem(
		keyName = "requireBell",
		name = "Only belled patches",
		description = "Only ding for patches whose bell is turned on in the Time Tracking panel",
		position = 4
	)
	default boolean requireBell()
	{
		return false;
	}

	@ConfigItem(
		keyName = "chatMessages",
		name = "Chat messages",
		description = "Show a chat message when a run is detected and when your calendar is updated",
		position = 9
	)
	default boolean chatMessages()
	{
		return true;
	}

	@Range(min = 1, max = 30)
	@Units(Units.MINUTES)
	@ConfigItem(
		keyName = "syncDelay",
		name = "Sync delay",
		description = "Wait this long after your last patch change before updating the calendar, so a run in progress isn't synced halfway. Logging out or closing RuneLite syncs right away.",
		position = 8
	)
	default int syncDelay()
	{
		return 5;
	}

	@Range(min = 0, max = 120)
	@Units(Units.MINUTES)
	@ConfigItem(
		keyName = "groupWindow",
		name = "Group window",
		description = "Patches of the same tree that finish within this many minutes share one ding",
		position = 5
	)
	default int groupWindow()
	{
		return 45;
	}

	@ConfigItem(
		keyName = "followOnProduce",
		name = "Follow-on trees",
		description = "Comma-separated trees that also get a ding one grow cycle later, for replants done on mobile",
		position = 6
	)
	default String followOnProduce()
	{
		return "Yew";
	}

	@Range(min = 0, max = 4)
	@ConfigItem(
		keyName = "followOnCycles",
		name = "Follow-on cycles",
		description = "How many extra grow cycles to ding for follow-on trees (2 = two mobile runs)",
		position = 7
	)
	default int followOnCycles()
	{
		return 2;
	}
}
