package org.tavall.control.clock;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.event.RecordingDomainEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class KingdomClockControlSystemTest {
    @Test
    void realTimeSyncedClockCalculatesCanonicalPhaseAndDayNight() {
        KingdomClockControlSystem system = KingdomClockControlSystem.inMemory(new RecordingDomainEventPublisher(), fixedClock("2026-05-05T12:30:00Z"));

        KingdomClockState state = system.getCurrentClockState("kingdom-1");

        assertEquals(12, state.currentHour());
        assertEquals(30, state.currentMinute());
        assertEquals(KingdomTimePhase.DAY, state.currentPhase());
        assertTrue(system.isDay("kingdom-1"));
        assertFalse(system.isNight("kingdom-1"));
    }

    @Test
    void overridePauseAndClearRemainControlPlaneOwned() {
        MutableKingdomClockTestClock clock = new MutableKingdomClockTestClock(Instant.parse("2026-05-05T09:00:00Z"));
        KingdomClockControlSystem system = KingdomClockControlSystem.inMemory(new RecordingDomainEventPublisher(), clock);

        KingdomClockState night = system.setTimeOverride("kingdom-1", LocalTime.of(22, 15));
        KingdomClockState paused = system.pauseClock("kingdom-1");
        clock.setInstant(Instant.parse("2026-05-05T13:00:00Z"));
        KingdomClockTickResult tick = system.tickClock("kingdom-1");

        assertEquals(KingdomTimePhase.NIGHT, night.currentPhase());
        assertEquals(KingdomClockMode.PAUSED, paused.clockMode());
        assertFalse(tick.advanced());
        assertEquals(KingdomClockMode.PAUSED, tick.currentState().clockMode());

        KingdomClockState cleared = system.clearTimeOverride("kingdom-1");
        assertEquals(KingdomClockMode.PAUSED, cleared.clockMode());
        assertTrue(cleared.timeOverride().isEmpty());
    }

    @Test
    void acceleratedModeAdvancesByConfiguredMultiplier() {
        MutableKingdomClockTestClock clock = new MutableKingdomClockTestClock(Instant.parse("2026-05-05T08:00:00Z"));
        KingdomClockControlSystem system = KingdomClockControlSystem.inMemory(new RecordingDomainEventPublisher(), clock);
        system.updateClockConfig("kingdom-1", Map.of("mode", "ACCELERATED", "acceleratedTimeMultiplier", "10"));
        KingdomClockState before = system.getCurrentClockState("kingdom-1");

        clock.setInstant(Instant.parse("2026-05-05T08:03:00Z"));
        KingdomClockTickResult tick = system.tickClock("kingdom-1");

        assertEquals(before.currentEpochMinute() + 30, tick.currentState().currentEpochMinute());
        assertTrue(tick.advanced());
    }

    @Test
    void scheduleWindowsSupportNormalAndMidnightWrappingContainment() {
        KingdomScheduleWindow work = KingdomScheduleWindow.of("work", "Work", 8, 17);
        KingdomScheduleWindow sleep = new KingdomScheduleWindow("sleep", "Sleep", 22, 0, 6, 0, java.util.Set.of(KingdomTimePhase.NIGHT), true, Map.of());

        assertTrue(work.contains(12, 0, KingdomTimePhase.DAY));
        assertFalse(work.contains(20, 0, KingdomTimePhase.DUSK));
        assertTrue(sleep.contains(23, 30, KingdomTimePhase.NIGHT));
        assertTrue(sleep.contains(2, 0, KingdomTimePhase.NIGHT));
        assertFalse(sleep.contains(12, 0, KingdomTimePhase.DAY));
    }

    @Test
    void activeRulesHooksAndProjectionsReadClockState() {
        KingdomClockControlSystem system = KingdomClockControlSystem.inMemory(new RecordingDomainEventPublisher(), fixedClock("2026-05-05T10:00:00Z"));

        assertTrue(system.getActiveRules("kingdom-1").stream().anyMatch(rule -> rule.ruleType() == KingdomScheduleRuleType.CITIZEN_JOB_SHIFT));
        assertEquals(CitizenScheduledState.WORKING, system.evaluateCitizenScheduledState("kingdom-1", KingdomScheduleWindow.of("work", "Work", 8, 17), new KingdomScheduleWindow("sleep", "Sleep", 22, 0, 6, 0, java.util.Set.of(KingdomTimePhase.NIGHT), true, Map.of())));
        assertTrue(system.evaluateTownScheduleState("kingdom-1", "town-1").shopsOpen());
        assertFalse(system.evaluateInteriorScheduleState("kingdom-1", "interior-1").lightsEnabled());
        assertTrue(system.evaluateTroopTrainingModifier("kingdom-1").active());
        assertTrue(system.evaluateBuildingProductivityModifier("kingdom-1", Optional.empty()).active());
        assertFalse(system.evaluateMoraleModifier("kingdom-1").active());

        KingdomClockProjection robloxClock = system.projectClockState("kingdom-1", GamePlatform.ROBLOX);
        KingdomScheduleProjection discordSchedule = system.projectScheduleState("kingdom-1", GamePlatform.DISCORD);

        assertEquals(KingdomTimePhase.DAY, robloxClock.currentPhase());
        assertEquals(KingdomVisualMood.BRIGHT_DAY, robloxClock.visualMood());
        assertTrue(discordSchedule.shopOpenCloseHints().contains("OPEN"));
        assertEquals("plain-java-control-server", robloxClock.metadata().get("canonicalOwner"));
    }

    @Test
    void agingTickPolicySkipsDisabledAndDuplicateIntervals() {
        KingdomClockControlSystem system = KingdomClockControlSystem.inMemory(new RecordingDomainEventPublisher(), fixedClock("2026-05-05T10:00:00Z"));

        AgingTickResult first = system.runAgingTick("kingdom-1");
        AgingTickResult second = system.runAgingTick("kingdom-1");
        system.updateAgingTickPolicy("kingdom-1", Map.of("enabled", "false"));
        AgingTickResult disabled = system.runAgingTick("kingdom-1");

        assertEquals(1, first.affectedCitizenCount());
        assertEquals("interval-not-reached", second.metadata().get("skippedReason"));
        assertEquals("disabled", disabled.metadata().get("skippedReason"));
    }

    private Clock fixedClock(String instant) {
        return Clock.fixed(Instant.parse(instant), ZoneId.of("UTC"));
    }
}

final class MutableKingdomClockTestClock extends Clock {
    private Instant instant;

    MutableKingdomClockTestClock(Instant instant) {
        this.instant = instant;
    }

    void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
