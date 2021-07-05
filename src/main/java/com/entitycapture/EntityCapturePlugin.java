package com.entitycapture;

import com.google.inject.Provides;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectID;
import net.runelite.api.Tile;
import net.runelite.api.events.GameObjectDespawned;
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
	private static final int MAX_DISTANCE = 2000;

	@Inject
	private Client client;

	@Inject
	private EntityCaptureConfig config;

	@Inject
	private ImageCapture imageCapture;

	@Inject
	private DrawManager drawManager;

	private final Map<GameObject, CaptureObject> captureObjects = new HashMap<>();


	long time;

	@Override
	protected void startUp() throws Exception
	{
//		takeScreenshot(String.valueOf(config.objectId()));
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
		Tile tile = e.getTile();

		if (object.getId() == config.objectId()) {
			log.info("Adding object " + object.getId());
			captureObjects.put(object, new CaptureObject(tile, object));
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		log.info("Despawned object " + e.getGameObject().getId());

		GameObject object = e.getGameObject();

		if (object.getId() == config.objectId()) {
			log.info("Removing object " + object.getId());
			captureObjects.remove(object);
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		int timeElapsed = Math.toIntExact((System.nanoTime() - time) / 1000000000);

		if (timeElapsed > config.pauseDuration()) {
			for (CaptureObject captureObject : captureObjects.values()) {
				GameObject gameObject = captureObject.getGameObject();
				Tile tile = captureObject.getTile();;
				log.info("Plane: " + (gameObject.getPlane() == client.getPlane()));
				log.info("Distance: " + tile.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation()));

				if (gameObject.getPlane() == client.getPlane()
					&& tile.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation()) < MAX_DISTANCE) {
						takeScreenshot(gameObject);
				}
			}
			time = System.nanoTime();
		}

		log.info(String.valueOf(captureObjects.size()));
	}

	private void takeScreenshot(GameObject object)
	{
			drawManager.requestNextFrameListener(image ->
			{
					BufferedImage bufferedImage = (BufferedImage) image;
					Rectangle rect = object.getClickbox().getBounds();

					// cropping image to the object hitbox
					BufferedImage cropped = bufferedImage.getSubimage(rect.x, rect.y, rect.width, rect.height);

					imageCapture.takeScreenshot(cropped, String.valueOf(object.getId()), String.valueOf(object.getId()), false, ImageUploadStyle.NEITHER);
					log.info("image taken: " + object.getId());
			});
	}

}
