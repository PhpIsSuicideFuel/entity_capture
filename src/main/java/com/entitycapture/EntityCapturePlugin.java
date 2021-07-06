package com.entitycapture;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import static net.runelite.client.RuneLite.SCREENSHOT_DIR;


@Slf4j
@PluginDescriptor(
	name = "Entity Capture"
)
public class EntityCapturePlugin extends Plugin
{
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static final int MAX_DISTANCE = 2000;

	@Inject
	private Client client;

	@Inject
	private EntityCaptureConfig config;

	@Inject
	private DrawManager drawManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private EntityCaptureKeyboardListener entityCaptureKeyboardListener;

	private final Map<GameObject, CaptureObject> captureObjects = new HashMap<>();

	long time;

	@Override
	protected void startUp() throws Exception
	{
		time = System.nanoTime();
		keyManager.registerKeyListener(entityCaptureKeyboardListener);
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(entityCaptureKeyboardListener);
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
		GameObject object = e.getGameObject();

		if (object.getId() == config.objectId()) {
			log.info("Removing object " + object.getId());
			captureObjects.remove(object);
		}
	}

	//Maybe make use of this later, but right now I feel like manual image taking gets you better results
//	@Subscribe
//	public void onGameTick(GameTick gameTick)
//	{
//		int timeElapsed = Math.toIntExact((System.nanoTime() - time) / 1000000000);
//
//		if (!config.isActive()) {
//			return;
//		}
//
//		if (timeElapsed > config.pauseDuration()) {
//			takeAllObjectsScreenshot();
//			time = System.nanoTime();
//		}
//
//		log.info(String.valueOf(captureObjects.size()));
//	}

	public void takeAllObjectsScreenshot()
	{
		int objectCount = 0;
		String imageFileName = config.objectId() + "-" + new Timestamp(System.nanoTime()).getTime();

		String resultLine = "";

		if (config.isPositive()) {
			for (CaptureObject captureObject : captureObjects.values()) {
				GameObject gameObject = captureObject.getGameObject();
				Tile tile = captureObject.getTile();

				if (gameObject.getPlane() == client.getPlane()
					&& tile.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation()) < MAX_DISTANCE) {
					if (!isObjectOnScreen(gameObject)) { // if the object is off screen we will skip it
						continue;
					}
					objectCount++;
					Rectangle objectRect = gameObject.getClickbox().getBounds();
					resultLine += " " + objectRect.x + " " + objectRect.y + " " + objectRect.width + " " + objectRect.height;
				}
			}

			if (objectCount < 1) { // Do not output anything if there are no entities to capture
				log.warn("No objects (" + config.objectId() + ") in client viewport, no image is taken.");
				return;
			}
		} else {
			for (CaptureObject captureObject : captureObjects.values()) {
				GameObject gameObject = captureObject.getGameObject();
				// Opposite of capturing positive samples we don't take the picture if object is on screen
				if (gameObject.getClickbox() != null) {
					log.warn("Cannot take negative picture if a specified object (" + config.objectId() + ") is on screen.");
					return;
				}
			}
		}

		// Set the subdir accordingly for the type of images user is capturing
		String subdir = config.isPositive() ? "positive" : "negative";

		imageFileName = objectCount + "-" + imageFileName + ".png";
		// Constructing the final result line [Image file] [object count] [object x, y, w, h]
		String pathToImage = subdir + File.separator + imageFileName;
		resultLine = pathToImage + (config.isPositive() ? " " + objectCount : "") + resultLine + "\n";

		String playerDir = client.getLocalPlayer().getName() + File.separator + config.objectId() + File.separator;
		File playerScreenshotFolder = new File(SCREENSHOT_DIR, playerDir);


		takeScreenshot(imageFileName, new File(playerScreenshotFolder, subdir));

		writeResults(playerScreenshotFolder, subdir, resultLine);

	}

	private void takeScreenshot(String fileName, File directory)
	{
		directory.mkdirs();

		drawManager.requestNextFrameListener(image ->
		{
			BufferedImage screenshot = (BufferedImage) image;

			try {
				File screenshotFile = new File(directory, fileName);
				ImageIO.write(screenshot, "PNG", screenshotFile);
			} catch (IOException ex) {
				log.warn("error writing screenshot", ex);
			}

			log.info("image taken: " + fileName);
		});
	}

	private void writeResults(File directory, String subdir, String resultLine)
	{
		byte[] resultBytes = resultLine.getBytes();

		try {
			File resultsFile = new File(directory, subdir + ".txt");
			resultsFile.createNewFile(); // If file already exists will do nothing

			// Write results to output file
			FileOutputStream oFile = new FileOutputStream(resultsFile, true);
			oFile.write(resultBytes);
			oFile.close();

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	private boolean isObjectOnScreen(GameObject object) {
		Shape clickbox = object.getClickbox();

		if (clickbox == null) {
			return false;
		}
		Rectangle rectangle = clickbox.getBounds();

		int clientWidth = client.getViewportWidth();
		int clientHeight = client.getViewportHeight();

		// Check if the object bounds reach off screen
		if (rectangle.x + rectangle.width > clientWidth) {
			return false;
		}

		if (rectangle.y + rectangle.height > clientHeight) {
			return false;
		}

		return true;
	}
}
