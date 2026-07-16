package org.tavall.minecraft.clock;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Server-wide kingdom time service backed by the real-world 24-hour cycle.
 */
public class KingdomClockHandler {

  private final ZoneId serverZoneId;

  public KingdomClockHandler(ZoneId serverZoneId) {
    this.serverZoneId = Objects.requireNonNull(serverZoneId, "serverZoneId");
  }

  public KingdomClockState snapshot() {
    return KingdomClockState.from(Instant.now(), serverZoneId);
  }

  public KingdomClockState snapshotAt(Instant instant) {
    return KingdomClockState.from(instant, serverZoneId);
  }
}
