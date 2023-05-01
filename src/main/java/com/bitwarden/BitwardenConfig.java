package com.bitwarden;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Bitwarden")
public interface BitwardenConfig extends Config
{
	@ConfigItem(
		keyName = "password",
		name = "Bitwarden",
		description = "Bitwarden Runescape passwords directly from Runelite"
	)
	default String greeting()
	{
		return "Hello";
	}
}
