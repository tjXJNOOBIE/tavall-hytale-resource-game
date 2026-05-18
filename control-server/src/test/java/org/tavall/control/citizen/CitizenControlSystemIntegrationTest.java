package org.tavall.control.citizen;

import org.tavall.control.domain.CitizenJobType;
import org.tavall.control.runtime.CommandIssuedFrom;
import org.tavall.control.runtime.ControlCommandResult;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.control.runtime.ControlCommandRuntimeFactory;
import org.tavall.control.runtime.ControlOperator;
import org.tavall.control.asset.GlobalAssetResolutionHandler;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.projection.DiscordProjectionHandler;
import org.tavall.control.projection.FrontendProjection;
import org.tavall.control.projection.FrontendProjectionHandler;
import org.tavall.control.projection.GlobalAssetProjectionHandler;
import org.tavall.control.projection.MinecraftProjectionHandler;
import org.tavall.control.projection.ProjectionObjectType;
import org.tavall.control.projection.RobloxProjectionHandler;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandSurface;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationState;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CitizenControlSystemIntegrationTest {
    @Test
    void citizenCommandsCreateAssignTrainPromoteDemoteAndRefreshSummaries() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        UniversalPlayerId playerId = UniversalPlayerId.random();
        ControlOperator operator = ControlOperator.localOwner(Instant.parse("2026-05-05T12:00:00Z"));
        Instant now = Instant.parse("2026-05-05T12:00:00Z");

        ControlCommandResult create = dispatch(runtime, "citizens spawn " + playerId + " 2 kingdom-1", operator, now);
        assertTrue(create.success());

        List<CitizenData> citizens = runtime.citizenControlSystem().citizensForPlayer(playerId, now);
        assertEquals(2, citizens.size());
        CitizenData first = citizens.getFirst();
        assertEquals(CitizenAgeStage.YOUNG_ADULT, first.ageStage());

        assertTrue(dispatch(runtime, "citizens setjob " + first.citizenId() + " GATHERER", operator, now).success());
        assertEquals(CitizenJobType.GATHERER, runtime.citizenControlSystem().citizensForPlayer(playerId, now).getFirst().jobType());

        assertTrue(dispatch(runtime, "citizens train " + first.citizenId(), operator, now).success());
        assertTrue(dispatch(runtime, "citizens promote " + first.citizenId(), operator, now).success());
        CitizenData promoted = runtime.citizenControlSystem().citizensForPlayer(playerId, now).getFirst();
        assertEquals(first.citizenId(), promoted.citizenId());
        assertEquals(CitizenStatus.ACTIVE_TROOP, promoted.status());
        assertEquals(CitizenTroopLinkState.ACTIVE_TROOP, promoted.troopLinkState());

        assertTrue(dispatch(runtime, "citizens demote " + first.citizenId(), operator, now).success());
        CitizenSummaryBundle summary = runtime.citizenControlSystem().readSummary(CitizenSummaryScope.player(playerId), now);
        assertEquals(2, summary.populationSummary().activeCitizens());
        assertEquals(0, summary.populationSummary().activeTroops());
        assertEquals(2, summary.populationSummary().jobCounts().get(CitizenJobType.IDLE));
    }

    @Test
    void jobEligibilityUsesExistingCitizenJobListAndRejectsUnknownOrIneligibleJobs() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        UniversalPlayerId playerId = UniversalPlayerId.random();
        ControlOperator operator = ControlOperator.localOwner(Instant.parse("2026-05-05T12:00:00Z"));
        Instant now = Instant.parse("2026-05-05T12:00:00Z");
        dispatch(runtime, "citizens spawn " + playerId + " 1 kingdom-1", operator, now);
        CitizenData citizen = runtime.citizenControlSystem().citizensForPlayer(playerId, now).getFirst();

        assertFalse(dispatch(runtime, "citizens setjob " + citizen.citizenId() + " FARMER", operator, now).success());
        assertTrue(dispatch(runtime, "citizens setstage " + citizen.citizenId() + " INFANT", operator, now).success());
        assertFalse(dispatch(runtime, "citizens setjob " + citizen.citizenId() + " MINER", operator, now).success());
    }

    @Test
    void summaryCacheFallsBackFromRedisLikeLayerToRepositoryAndRewarmsMemory() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        UniversalPlayerId playerId = UniversalPlayerId.random();
        ControlOperator operator = ControlOperator.localOwner(Instant.parse("2026-05-05T12:00:00Z"));
        Instant now = Instant.parse("2026-05-05T12:00:00Z");
        dispatch(runtime, "citizens spawn " + playerId + " 3 kingdom-1", operator, now);

        CitizenSummaryScope scope = CitizenSummaryScope.player(playerId);
        CitizenSummaryBundle firstRead = runtime.citizenControlSystem().readSummary(scope, now);
        CitizenSummaryBundle secondRead = runtime.citizenControlSystem().readSummary(scope, now);

        assertEquals(3, firstRead.populationSummary().activeCitizens());
        assertSame(firstRead, secondRead);
    }

    @Test
    void citizenPopulationAndAnchorsProjectForMinecraftRobloxAndDiscord() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        UniversalPlayerId playerId = UniversalPlayerId.random();
        ControlOperator operator = ControlOperator.localOwner(Instant.parse("2026-05-05T12:00:00Z"));
        Instant now = Instant.parse("2026-05-05T12:00:00Z");
        dispatch(runtime, "citizens spawn " + playerId + " 2 kingdom-1", operator, now);

        CitizenSummaryScope scope = CitizenSummaryScope.player(playerId);
        CitizenPopulationProjection populationProjection = runtime.citizenControlSystem().projectPopulation(scope, now);
        CitizenDisplayAnchorProjection citizenAnchor = runtime.citizenControlSystem().projectAnchor(scope, CitizenAnchorType.CITIZEN_COUNT, now);
        FrontendProjectionHandler projectionHandler = new FrontendProjectionHandler(new GlobalAssetProjectionHandler(new GlobalAssetResolutionHandler(runtime.globalAssetRepository())));

        MinecraftProjectionHandler minecraftProjectionHandler = new MinecraftProjectionHandler(projectionHandler);
        FrontendProjection minecraft = minecraftProjectionHandler.projectCitizenPopulationForMinecraftClient(populationProjection);
        FrontendProjection minecraftAnchor = minecraftProjectionHandler.projectCitizenDisplayAnchorForMinecraftClient(citizenAnchor);
        FrontendProjection roblox = new RobloxProjectionHandler(projectionHandler).projectCitizenPopulationForRobloxClient(populationProjection);
        FrontendProjection discord = new DiscordProjectionHandler(projectionHandler).projectCitizenDisplayAnchorForDiscord(citizenAnchor);

        assertEquals(ProjectionObjectType.CITIZEN_POPULATION, minecraft.objectType());
        assertEquals("Citizens: 2", minecraftAnchor.metadata().get("displayText"));
        assertEquals("2", roblox.metadata().get("totalCitizens"));
        assertEquals(ProjectionObjectType.CITIZEN_DISPLAY_ANCHOR, discord.objectType());
    }

    @Test
    void frontendKdCitizensCommandDispatchesThroughControlIngress() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        UniversalPlayerId playerId = UniversalPlayerId.random();
        FrontendCommandEnvelope envelope = new FrontendCommandEnvelope(
                ResourceGameFrontendPlatform.MINECRAFT,
                FrontendCommandSurface.COMMAND,
                "minecraft-player",
                "Minecraft Player",
                "/kd citizens spawn " + playerId + " 1 kingdom-1",
                "raw-command",
                Map.of(),
                "citizen-test",
                Map.of()
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.parse("2026-05-05T12:00:00Z"));

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertTrue(result.success());
        assertEquals("citizens spawn " + playerId + " 1 kingdom-1", result.metadata().get("controlConsoleInput"));
    }

    private ControlCommandResult dispatch(ControlCommandRuntime runtime, String input, ControlOperator operator, Instant now) {
        return runtime.dispatchHandler().dispatchCommand(runtime.parsingHandler().parseConsoleCommand(input, operator, CommandIssuedFrom.CLI, now));
    }
}
