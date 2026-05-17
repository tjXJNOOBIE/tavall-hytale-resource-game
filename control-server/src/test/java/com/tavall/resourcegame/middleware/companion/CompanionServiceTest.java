package com.tavall.resourcegame.middleware.companion;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CompanionServiceTest {
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
        CompanionService service = CompanionService.inMemory();
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
        CompanionStatsService statsService = new CompanionStatsService();
        CompanionLevelingService levelingService = new CompanionLevelingService();
        CompanionFactory factory = new CompanionFactory(statsService);
        CompanionTrainingService trainingService = new CompanionTrainingService(repository, statsService, levelingService);
        UUID playerId = UUID.randomUUID();
        CompanionData companion = repository.saveCompanion(factory.createCompanion(playerId, CompanionType.BRAWLER, 1000L));

        CompanionTrainingSession session = trainingService.startCompanionTraining(playerId, companion.companionId(), 1000L);
        long halfway = session.startedAtEpochMillis() + Duration.between(
                java.time.Instant.ofEpochMilli(session.startedAtEpochMillis()),
                java.time.Instant.ofEpochMilli(session.expectedCompletedAtEpochMillis())
        ).toMillis() / 2L;
        CompanionData cancelled = trainingService.cancelCompanionTraining(playerId, companion.companionId(), halfway);

        assertTrue(cancelled.xp() > 0);
        assertTrue(cancelled.xp() < session.expectedXp());
        assertEquals(CompanionStatus.IDLE, cancelled.status());
        assertFalse(repository.findActiveTrainingSession(companion.companionId()).isPresent());
    }

    @Test
    void wisdomWellUpgradesEligibleAbilityAndImprovesModifiers() {
        CompanionRepository repository = new InMemoryCompanionRepository();
        CompanionStatsService statsService = new CompanionStatsService();
        CompanionSkillService skillService = new CompanionSkillService();
        WisdomWellService wisdomWellService = new WisdomWellService(repository, skillService);
        CompanionFactory factory = new CompanionFactory(statsService);
        UUID playerId = UUID.randomUUID();
        CompanionData companion = factory.createCompanion(playerId, CompanionType.HEALER, 1000L);
        companion = repository.saveCompanion(companion.withProgress(5, 1600, statsService.calculateCompanionStats(companion), 1000L));
        CompanionSkill skill = skillService.findSkillByName("Arcane Mend").orElseThrow();

        CompanionWisdomUpgrade upgrade = wisdomWellService.upgradeCompanionAbility(playerId, companion.companionId(), skill.skillId(), 2000L);

        assertEquals(2, upgrade.skillLevel());
        assertTrue(upgrade.cooldownModifier() < 1.0d);
        assertTrue(upgrade.powerModifier() > 1.0d);
    }

    @Test
    void wallAssignmentUsesCompanionAsDefensiveModifier() {
        CompanionRepository repository = new InMemoryCompanionRepository();
        CompanionStatsService statsService = new CompanionStatsService();
        CompanionFactory factory = new CompanionFactory(statsService);
        CompanionWallDefenseService wallDefenseService = new CompanionWallDefenseService(repository);
        UUID playerId = UUID.randomUUID();
        CompanionData companion = repository.saveCompanion(factory.createCompanion(playerId, CompanionType.BRUTE, 1000L));

        CompanionWallAssignment assignment = wallDefenseService.assignCompanionToWall(playerId, companion.companionId(), "north", 2000L);

        assertEquals("north", assignment.wallSectionId());
        assertTrue(assignment.defenseBonus() > 0);
        assertTrue(wallDefenseService.calculateWallCompanionBonus(playerId) > 0);
        assertFalse(wallDefenseService.getAvailableWallSlots(playerId).contains("north"));
    }

    @Test
    void domainAccessorsResolveRegisteredCompanionGraph() {
        CompanionRepository repository = new InMemoryCompanionRepository();
        CompanionService service = CompanionService.withRepository(repository);
        ICompanionDomain domain = new ICompanionDomain() {
        };

        assertEquals(service, domain.getCompanionService());
        assertEquals(repository, domain.getCompanionRepository());
        assertTrue(domain.getCompanionFactory() instanceof CompanionFactory);
        assertTrue(domain.getCompanionTrainingService() instanceof CompanionTrainingService);
        assertTrue(domain.getWisdomWellService() instanceof WisdomWellService);
        assertTrue(domain.getCompanionBehaviorEngine() instanceof CompanionBehaviorEngine);
        assertTrue(domain.getCompanionMoraleService() instanceof CompanionMoraleService);
        assertTrue(domain.getCompanionWallDefenseService() instanceof CompanionWallDefenseService);
    }
}
