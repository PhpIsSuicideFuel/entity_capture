package com.entitycapture;


import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.input.KeyListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.event.KeyEvent;

@Singleton
public class EntityCaptureKeyboardListener implements KeyListener {
  @Inject
  private EntityCaptureConfig entityCaptureConfig;

  @Inject
  private EntityCapturePlugin plugin;

  @Override
  public void keyTyped(KeyEvent e)
  {

  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    System.out.println(e.getKeyCode());
    if (entityCaptureConfig.takeScreenshotHotkey().matches(e))
    {

      plugin.takeAllObjectsScreenshot();
    }
  }

  @Override
  public void keyReleased(KeyEvent e)
  {

  }
}
