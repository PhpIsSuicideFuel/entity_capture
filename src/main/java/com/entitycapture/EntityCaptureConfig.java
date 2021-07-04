package com.entitycapture;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("entitycapture")
public interface EntityCaptureConfig extends Config
{
	@ConfigItem(
		keyName = "objectId",
		name = "Object Id",
		description = "Specify the object id."
	)
	default int objectId()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "pauseDuration",
		name = "Pause Duration",
		description = "Seconds to wait between screenshots."
	)
	default int pauseDuration()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "isActive",
		name = "Is Active",
		description = "Is the plugin active."
	)
	default boolean isActive()
	{
		return false;
	}
}
