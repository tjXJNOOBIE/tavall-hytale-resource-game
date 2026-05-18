package org.tavall.minecraft.runtime;

public record WorldPosition(String worldId, double x, double y, double z) {

  public WorldPosition withWorld(String nextWorldId) {
    return new WorldPosition(nextWorldId, x, y, z);
  }
}
