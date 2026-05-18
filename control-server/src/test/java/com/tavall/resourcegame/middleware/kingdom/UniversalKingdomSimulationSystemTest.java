package org.tavall.control.kingdom;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.runtime.CommandIssuedFrom;
import org.tavall.control.runtime.ControlCommand;
import org.tavall.control.runtime.ControlCommandResult;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.control.runtime.ControlCommandRuntimeFactory;
import org.tavall.control.runtime.ControlOperator;
import org.tavall.control.runtime.ControlCommandValidationException;
import org.tavall.control.event.RecordingDomainEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UniversalKingdomSimulationSystemTest {
    private final Instant now = Instant.parse("2026-05-04T12:00:00Z");

    @Test
    void createKingdomAssignsStableFolderBorderAndRoutingProfiles() {
        UniversalKingdomSimulationSystem system = system();

        UniversalKingdomSimulationSystem.UniversalKingdom first = system.createKingdom("First", "default", 1000, now);
        UniversalKingdomSimulationSystem.UniversalKingdom second = system.createKingdom("Second", "default", 1000, now);

        assertEquals("kingdom-1", first.kingdomId().value());
        assertEquals("kingdom-1", first.folderName());
        assertEquals("kingdom-2", second.folderName());
        assertTrue(system.repository().findNamespace(first.kingdomId()).isPresent());
        assertTrue(system.repository().findBorderForKingdom(first.kingdomId()).isPresent());
        assertTrue(system.repository().findRoutingProfile(first.kingdomId(), GamePlatform.MINECRAFT).isPresent());
    }

    @Test
    void canonicalBorderResolutionAndCrossingCreatesSwitchRequest() {
        UniversalKingdomSimulationSystem system = system();
        system.createKingdom("One", "default", 1000, now);
        system.createKingdom("Two", "default", 1000, now);
        String playerId = "player-1";

        system.updatePlayerLocation(playerId, platformCoordinate(100, 65, 100), Optional.of("kingdom-1-minecraft-primary"), now);
        UniversalKingdomSimulationSystem.PlayerLocationUpdateResult crossed = system.updatePlayerLocation(playerId, platformCoordinate(1200, 65, 100), Optional.of("kingdom-1-minecraft-primary"), now.plusSeconds(1));

        assertEquals("kingdom-2", crossed.location().currentKingdomId().value());
        assertTrue(crossed.transition().isPresent());
        assertTrue(crossed.switchRequest().isPresent());
        assertEquals("kingdom-1", crossed.transition().get().fromKingdomId().value());
        assertEquals("kingdom-2", crossed.transition().get().toKingdomId().value());
    }

    @Test
    void robloxConversionUsesExplicitAxisMappingAndRoundTrips() {
        UniversalKingdomSimulationSystem system = system();
        system.repository().saveCoordinateParameters(new UniversalKingdomSimulationSystem.CoordinateConversionParameters(
                GamePlatform.ROBLOX,
                2,
                1,
                2,
                "x",
                "z",
                "y",
                10,
                0,
                -4,
                false,
                false,
                true,
                90,
                true,
                Map.of()
        ));

        UniversalKingdomSimulationSystem.CoordinateConversionResult converted = system.convertToCanonical(new UniversalKingdomSimulationSystem.PlatformWorldCoordinate(
                GamePlatform.ROBLOX,
                "default",
                14,
                6,
                -8,
                Optional.of(0.0),
                Optional.empty(),
                Map.of()
        ));
        UniversalKingdomSimulationSystem.PlatformWorldCoordinate roundtrip = system.convertToPlatform(converted.canonicalCoordinate(), GamePlatform.ROBLOX).platformCoordinate().orElseThrow();

        assertEquals(2.0, converted.canonicalCoordinate().x());
        assertEquals(2.0, converted.canonicalCoordinate().y());
        assertEquals(6.0, converted.canonicalCoordinate().z());
        assertEquals(14.0, roundtrip.x(), 0.0001);
        assertEquals(-8.0, roundtrip.z(), 0.0001);
        assertFalse(converted.conversionWarnings().isEmpty());
    }

    @Test
    void editableParameterDryRunDoesNotMutateValue() {
        UniversalKingdomSimulationSystem system = system();

        system.updateEditableParameter("GLOBAL", Optional.empty(), "maxActivePlayers", "300", "test", true, now);

        assertTrue(system.repository().findEditableParameterValue("maxActivePlayers", Optional.empty()).isEmpty());
        assertThrows(ControlCommandValidationException.class, () -> system.updateEditableParameter("GLOBAL", Optional.empty(), "maxActivePlayers", "0", "test", false, now));
    }

    @Test
    void editableParameterRegistryCoversPopulationPowerBorderRoutingCoordinateAndNewPlayerGroups() {
        UniversalKingdomSimulationSystem system = system();

        assertTrue(system.repository().findEditableParameterDefinition("newPlayerAllowed").isPresent());
        assertTrue(system.repository().findEditableParameterDefinition("maxGuildDominanceRatio").isPresent());
        assertTrue(system.repository().findEditableParameterDefinition("borderShape").orElseThrow().allowedValues().contains("RECTANGLE"));
        assertTrue(system.repository().findEditableParameterDefinition("primaryInstanceId").isPresent());
        assertTrue(system.repository().findEditableParameterDefinition("platformXAxisCanonicalAxis").orElseThrow().allowedValues().contains("z"));
        assertTrue(system.repository().findEditableParameterDefinition("safestKingdomRoutingEnabled").isPresent());
    }

    @Test
    void scopedParameterUpdatesMutateKingdomAndCoordinateConversionState() {
        UniversalKingdomSimulationSystem system = system();
        UniversalKingdomSimulationSystem.UniversalKingdom kingdom = system.createKingdom("First", "default", 1000, now);

        system.updateEditableParameter("KINGDOM", Optional.of(kingdom.kingdomId().value()), "maxGuildDominanceRatio", "0.4", "test", false, now);
        system.updateEditableParameter("KINGDOM", Optional.of(kingdom.kingdomId().value()), "newPlayerAllowed", "false", "test", false, now);
        system.updateEditableParameter("COORDINATE_CONVERSION", Optional.of("ROBLOX"), "platformScaleX", "2", "test", false, now);

        assertEquals(0.4, system.repository().findKingdom(kingdom.kingdomId()).orElseThrow().editableParameters().maxGuildDominanceRatio());
        assertFalse(system.repository().findKingdom(kingdom.kingdomId()).orElseThrow().populationStats().newPlayerAllowed());
        assertEquals(2.0, system.repository().findCoordinateParameters(GamePlatform.ROBLOX).orElseThrow().platformScaleX());
        assertThrows(ControlCommandValidationException.class, () -> system.updateEditableParameter("COORDINATE_CONVERSION", Optional.of("ROBLOX"), "platformScaleX", "0", "test", false, now));
    }

    @Test
    void controlPipelineCreatesKingdomAndParsesCliCommands() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ControlOperator operator = ControlOperator.localOwner(now);

        ControlCommand command = runtime.parsingHandler().parseConsoleCommand("kingdom create --displayName First --worldId default --borderSize 1000", operator, CommandIssuedFrom.CLI, now);
        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command);

        assertTrue(result.success(), result.message());
        assertEquals(1, runtime.kingdomSimulationSystem().repository().kingdoms().size());
        assertNotNull(runtime.kingdomSimulationSystem().repository().findKingdom(UniversalKingdomSimulationSystem.KingdomId.of("kingdom-1")).orElseThrow());
    }

    private UniversalKingdomSimulationSystem system() {
        return UniversalKingdomSimulationSystem.inMemory(new RecordingDomainEventPublisher());
    }

    private UniversalKingdomSimulationSystem.PlatformWorldCoordinate platformCoordinate(double x, double y, double z) {
        return new UniversalKingdomSimulationSystem.PlatformWorldCoordinate(GamePlatform.MINECRAFT, "default", x, y, z, Optional.empty(), Optional.empty(), Map.of());
    }
}
