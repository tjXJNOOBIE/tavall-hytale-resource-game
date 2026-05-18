package org.tavall.control.companion;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CompanionHandlerTest {
    @BeforeEach
    void resetDependenciesBeforeTest() {
        DependencyLoaderAccess.clear();
    }

    @AfterEach
    void resetDependenciesAfterTest() {
        DependencyLoaderAccess.clear();
    }

    @Test
    void createsTypedCompanionWithArcaneNamesAndCalculatedStats() {
        CompanionHandler service = CompanionHandler.inMemory();
        UUID playerId = UUID.randomUUID();

        CompanionData companion = service.createCompanion(playerId, CompanionType.ARCANE, 1000L);

        assertEquals(CompanionType.ARCANE, companion.type());
        assertTrue(companion.calculatedStats().arcaneAttack() > companion.calculatedStats().earthAttack());
        assertEquals(4, companion.skillSlots().size());
        assertTrue(companion.skillSlots().getFirst().unlocked());
    }

    @Test
    void companionCampCancelKeepsPartialXp() {
        CompanionRepository repository = new InMemoryCompanionRepository();
        CompanionStatsHandler statsHandler = new CompanionStatsHandler();
        CompanionLevelingHandler levelingHandler = new CompanionLevelingHandler();
        CompanionFactory factory = new CompanionFactory(statsHandler);
        CompanionTrainingHandler trainingHandler = new CompanionTrainingHandler(repository, statsHandler, levelingHandler);
        UUID playerId = UUID.randomUUID();
        CompanionData companion = repository.saveCompanion(factory.createCompanion(playerId, CompanionType.BRAWLER, 1000L));

        CompanionTrainingSession session = trainingHandler.startCompanionTraining(playerId, companion.companionId(), 1000L);
        long halfway = session.startedAtEpochMillis() + Duration.between(
                java.time.Instant.ofEpochMilli(session.startedAtEpochMillis()),
                java.time.Instant.ofEpochMilli(session.expectedCompletedAtEpochMillis())
        ).toMillis() / 2L;
        CompanionData cancelled = trainingHandler.cancelCompanionTraining(playerId, companion.companionId(), halfway);

        assertTrue(cancelled.xp() > 0);
        assertTrue(cancelled.xp() < session.expectedXp());
        assertEquals(CompanionStatus.IDLE, cancelled.status());
        assertFalse(repository.findActiveTrainingSession(companion.companionId()).isPresent());
    }

    @Test
    void wisdomWellUpgradesEligibleAbilityAndImprovesModifiers() {
        CompanionRepository repository = new InMemoryCompanionRepository();
        CompanionStatsHandler statsHandler = new CompanionStatsHandler();
        CompanionSkillHandler skillHandler = new CompanionSkillHandler();
        WisdomWellHandler wisdomWellHandler = new WisdomWellHandler(repository, skillHandler);
        CompanionFactory factory = new CompanionFactory(statsHandler);
        UUID playerId = UUID.randomUUID();
        CompanionData companion = factory.createCompanion(playerId, CompanionType.HEALER, 1000L);
        companion = repository.saveCompanion(companion.withProgress(5, 1600, statsHandler.calculateCompanionStats(companion), 1000L));
        CompanionSkill skill = skillHandler.findSkillByName("Arcane Mend").orElseThrow();

        CompanionWisdomUpgrade upgrade = wisdomWellHandler.upgradeCompanionAbility(playerId, companion.companionId(), skill.skillId(), 2000L);

        assertEquals(2, upgrade.skillLevel());
        assertTrue(upgrade.cooldownModifier() < 1.0d);
        assertTrue(upgrade.powerModifier() > 1.0d);
    }

    @Test
    void wallAssignmentUsesCompanionAsDefensiveModifier() {
        CompanionRepository repository = new InMemoryCompanionRepository();
        CompanionStatsHandler statsHandler = new CompanionStatsHandler();
        CompanionFactory factory = new CompanionFactory(statsHandler);
        CompanionWallDefenseHandler wallDefenseHandler = new CompanionWallDefenseHandler(repository);
        UUID playerId = UUID.randomUUID();
        CompanionData companion = repository.saveCompanion(factory.createCompanion(playerId, CompanionType.BRUTE, 1000L));

        CompanionWallAssignment assignment = wallDefenseHandler.assignCompanionToWall(playerId, companion.companionId(), "north", 2000L);

        assertEquals("north", assignment.wallSectionId());
        assertTrue(assignment.defenseBonus() > 0);
        assertTrue(wallDefenseHandler.calculateWallCompanionBonus(playerId) > 0);
        assertFalse(wallDefenseHandler.getAvailableWallSlots(playerId).contains("north"));
    }

    @Test
    void domainAccessorsResolveRegisteredCompanionGraph() {
        CompanionRepository repository = new InMemoryCompanionRepository();
        CompanionHandler service = CompanionHandler.withRepository(repository);
        ICompanionDomain domain = new ICompanionDomain() {
        };

        assertEquals(service, domain.getCompanionHandler());
        assertEquals(repository, domain.getCompanionRepository());
        assertTrue(domain.getCompanionFactory() instanceof CompanionFactory);
        assertTrue(domain.getCompanionTrainingHandler() instanceof CompanionTrainingHandler);
        assertTrue(domain.getWisdomWellHandler() instanceof WisdomWellHandler);
        assertTrue(domain.getCompanionBehaviorEngine() instanceof CompanionBehaviorEngine);
        assertTrue(domain.getCompanionMoraleHandler() instanceof CompanionMoraleHandler);
        assertTrue(domain.getCompanionWallDefenseHandler() instanceof CompanionWallDefenseHandler);
    }
}
