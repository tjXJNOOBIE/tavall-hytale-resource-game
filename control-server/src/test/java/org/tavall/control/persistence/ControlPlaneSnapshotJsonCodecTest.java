package org.tavall.control.persistence;

import org.tavall.control.clock.KingdomClockMode;
import org.tavall.control.clock.KingdomClockRealTimeSource;
import org.tavall.control.clock.KingdomClockState;
import org.tavall.control.clock.KingdomTimePhase;
import org.tavall.control.companion.CompanionBaseAttributes;
import org.tavall.control.companion.CompanionBehaviorState;
import org.tavall.control.companion.CompanionData;
import org.tavall.control.companion.CompanionMoraleState;
import org.tavall.control.companion.CompanionSkillSlot;
import org.tavall.control.companion.CompanionStats;
import org.tavall.control.companion.CompanionStatus;
import org.tavall.control.companion.CompanionType;
import org.tavall.control.event.RecordingDomainEventPublisher;
import org.tavall.control.kingdom.UniversalKingdomSimulationSystem;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    @Test
    void companionSnapshotsRoundTripSkillSlotsStatsAndOptionalAssignments() {
        ControlPlaneSnapshotJsonCodec codec = new ControlPlaneSnapshotJsonCodec();
        UUID skillId = UUID.randomUUID();
        CompanionData companion = new CompanionData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                CompanionType.BRUTE,
                CompanionStatus.ASSIGNED_TO_WALL,
                CompanionBehaviorState.IDLE,
                CompanionMoraleState.HIGH,
                30,
                90_000,
                1000,
                2000,
                new CompanionBaseAttributes(4, 14, 5),
                new CompanionStats(200, 40, 35, 0.12, 12, 10, 0.02, 65),
                java.util.List.of(new CompanionSkillSlot(1, Optional.of(skillId), true, 1)),
                Optional.empty(),
                Optional.of("north"),
                Map.of("source", "test")
        );

        CompanionData restored = codec.read(codec.write(companion), CompanionData.class);

        assertEquals(companion.companionId(), restored.companionId());
        assertEquals(Optional.of("north"), restored.activeWallSectionId());
        assertEquals(skillId, restored.skillSlots().getFirst().skillId().orElseThrow());
        assertEquals(200, restored.calculatedStats().hp());
    }
}
