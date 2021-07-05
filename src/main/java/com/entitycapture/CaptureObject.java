package com.entitycapture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;

@Setter
@Getter
@AllArgsConstructor
public class CaptureObject {
  private final Tile tile;
  private final GameObject gameObject;
}
