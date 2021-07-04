package com.entitycapture;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class EntityCapturePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(EntityCapturePlugin.class);
		RuneLite.main(args);
	}
}