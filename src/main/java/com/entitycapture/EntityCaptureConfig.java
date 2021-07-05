package com.entitycapture;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("entitycapture")
public interface EntityCaptureConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "takeScreenshotHotkey",
		name = "Take screenshot hotkey",
		description = "Hotkey to take a screenshot of all objects detected."
	)
	default Keybind takeScreenshotHotkey()
	{
		return new Keybind(KeyEvent.VK_P, 0);
	}

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
