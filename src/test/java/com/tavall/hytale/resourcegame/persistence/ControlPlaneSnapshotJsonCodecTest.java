package com.tavall.hytale.resourcegame.persistence;

import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockMode;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockRealTimeSource;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockState;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomTimePhase;
import com.tavall.hytale.resourcegame.middleware.event.RecordingDomainEventPublisher;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class ControlPlaneSnapshotJsonCodecTest {
    @Test
    void clockSnapshotsRoundTripOptionalAndJavaTimeFields() {
        ControlPlaneSnapshotJsonCodec codec = new ControlPlaneSnapshotJsonCodec();
        KingdomClockState state = new KingdomClockState(
                "kingdom-1",
                KingdomClockMode.FIXED_OVERRIDE,
                3,
                22,
                15,
                KingdomTimePhase.NIGHT,
                4215,
                Optional.of("UTC"),
                KingdomClockRealTimeSource.FIXED_TEST_TIME,
                Optional.of(LocalTime.of(22, 15)),
                Instant.parse("2026-05-06T12:00:00Z"),
                Instant.parse("2026-05-06T12:01:00Z"),
                Map.of("persistence", "postgres")
        );

        KingdomClockState restored = codec.read(codec.write(state), KingdomClockState.class);

        assertEquals(state.kingdomId(), restored.kingdomId());
        assertEquals(state.timeOverride(), restored.timeOverride());
        assertEquals(state.lastTickAt(), restored.lastTickAt());
    }

    @Test
    void universalKingdomSnapshotsRoundTripNestedControlPlaneRecords() {
        ControlPlaneSnapshotJsonCodec codec = new ControlPlaneSnapshotJsonCodec();
        UniversalKingdomSimulationSystem system = UniversalKingdomSimulationSystem.inMemory(new RecordingDomainEventPublisher());
        UniversalKingdomSimulationSystem.UniversalKingdom kingdom = system.createKingdom("First", "default", 1000, Instant.parse("2026-05-06T12:00:00Z"));

        UniversalKingdomSimulationSystem.UniversalKingdom restored = codec.read(
                codec.write(kingdom),
                UniversalKingdomSimulationSystem.UniversalKingdom.class
        );

        assertEquals("kingdom-1", restored.kingdomId().value());
        assertEquals("kingdom-1", restored.folderName());
        assertNotNull(restored.editableParameters());
    }
}
