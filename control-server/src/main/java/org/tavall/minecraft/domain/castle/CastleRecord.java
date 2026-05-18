package org.tavall.minecraft.domain.castle;

import java.util.UUID;
import org.tavall.minecraft.framework.game.AssetId;

/**
 * Persistent castle record for each player owner.
 */
public class CastleRecord {

  private final UUID castleId;
  private final long ownerProfileId;
  private CastleLocation location;
  private AssetId visualAsset;

  public CastleRecord(UUID castleId, long ownerProfileId, CastleLocation location, AssetId visualAsset) {
    this.castleId = castleId;
    this.ownerProfileId = ownerProfileId;
    this.location = location;
    this.visualAsset = visualAsset;
  }

  public UUID castleId() {
    return castleId;
  }

  public long ownerProfileId() {
    return ownerProfileId;
  }

  public CastleLocation location() {
    return location;
  }

  public AssetId visualAsset() {
    return visualAsset;
  }

  public void relocate(CastleLocation nextLocation) {
    this.location = nextLocation;
  }

  public void replaceVisual(AssetId nextVisualAsset) {
    this.visualAsset = nextVisualAsset;
  }
}
