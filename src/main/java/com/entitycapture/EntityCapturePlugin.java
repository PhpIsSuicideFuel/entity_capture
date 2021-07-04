package com.entitycapture;

import com.google.inject.Provides;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectID;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.ImageUploadStyle;

@Slf4j
@PluginDescriptor(
	name = "Entity Capture"
)
public class EntityCapturePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EntityCaptureConfig config;

	@Inject
	private ImageCapture imageCapture;

	@Inject
	private DrawManager drawManager;

	private ArrayList<GameObject> objects = new ArrayList<>();

	long time;

	@Override
	protected void startUp() throws Exception
	{
		takeScreenshot(String.valueOf(config.objectId()));
		time = System.nanoTime();
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Provides
	EntityCaptureConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EntityCaptureConfig.class);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		GameObject object = e.getGameObject();

		if (object.getId() == config.objectId()) {
			objects.add(object);
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		int timeElapsed = Math.toIntExact((System.nanoTime() - time) / 1000000000);
		log.info(String.valueOf(timeElapsed));
		if (timeElapsed > config.pauseDuration()) {
			log.info("Ready to take screenshot");
			time = System.nanoTime();
		}
	}

	private void takeScreenshot(String id)
	{
			drawManager.requestNextFrameListener(image ->
			{
					BufferedImage bufferedImage = (BufferedImage) image;
					imageCapture.takeScreenshot(bufferedImage, id, id, false, ImageUploadStyle.NEITHER);
					log.info("image taken: " + id);
			});
	}

}
