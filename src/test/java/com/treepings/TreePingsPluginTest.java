package com.treepings;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Run with ./gradlew run to start RuneLite in developer mode with Tree Pings loaded. */
public class TreePingsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TreePingsPlugin.class);
		RuneLite.main(args);
	}
}
