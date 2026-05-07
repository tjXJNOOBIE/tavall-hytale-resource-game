package com.tavall.hytale.resourcegame.middleware.companion;

import com.tavall.hytale.resourcegame.middleware.control.CommandExecutionState;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommand;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandValidationException;
import com.tavall.hytale.resourcegame.persistence.PostgresCompanionRepository;
import com.tavall.hytale.resourcegame.persistence.PostgresConnectionProvider;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CompanionService {
    private final CompanionRepository repository;
    private final CompanionFactory companionFactory;
    private final CompanionStatsService statsService;
    private final CompanionSkillService skillService;
    private final CompanionTrainingService trainingService;
    private final WisdomWellService wisdomWellService;
    private final CompanionBehaviorEngine behaviorEngine;
    private final CompanionMoraleService moraleService;
    private final CompanionWallDefenseService wallDefenseService;
    private final CompanionProjectionHandler projectionHandler;

    public CompanionService(
            CompanionRepository repository,
            CompanionFactory companionFactory,
            CompanionStatsService statsService,
            CompanionSkillService skillService,
            CompanionTrainingService trainingService,
            WisdomWellService wisdomWellService,
            CompanionBehaviorEngine behaviorEngine,
            CompanionMoraleService moraleService,
            CompanionWallDefenseService wallDefenseService,
            CompanionProjectionHandler projectionHandler
    ) {
        this.repository = repository;
        this.companionFactory = companionFactory;
        this.statsService = statsService;
        this.skillService = skillService;
        this.trainingService = trainingService;
        this.wisdomWellService = wisdomWellService;
        this.behaviorEngine = behaviorEngine;
        this.moraleService = moraleService;
        this.wallDefenseService = wallDefenseService;
        this.projectionHandler = projectionHandler;
    }

    public static CompanionService inMemory() {
        CompanionRepository repository = new InMemoryCompanionRepository();
        return withRepository(repository);
    }

    public static CompanionService postgres(PostgresConnectionProvider connectionProvider) {
        return withRepository(new PostgresCompanionRepository(connectionProvider));
    }

    public static CompanionService withRepository(CompanionRepository repository) {
        CompanionStatsService statsService = new CompanionStatsService();
        CompanionSkillService skillService = new CompanionSkillService();
        CompanionLevelingService levelingService = new CompanionLevelingService();
        return new CompanionService(
                repository,
                new CompanionFactory(statsService),
                statsService,
                skillService,
                new CompanionTrainingService(repository, statsService, levelingService),
                new WisdomWellService(repository, skillService),
                new CompanionBehaviorEngine(repository),
                new CompanionMoraleService(repository, statsService),
                new CompanionWallDefenseService(repository),
                new CompanionProjectionHandler()
        );
    }

    public CompanionData createCompanion(UUID playerId, CompanionType type, long nowEpochMillis) {
        CompanionData companion = companionFactory.createCompanion(playerId, type, nowEpochMillis);
        return repository.saveCompanion(companion);
    }

    public Optional<CompanionData> getActiveCompanion(UUID playerId) {
        return repository.findActiveCompanion(playerId);
    }

    public List<CompanionData> getCompanions(UUID playerId) {
        return repository.findCompanionsForPlayer(playerId);
    }

    public void saveCompanionAsync(CompanionData companionData) {
        repository.saveCompanion(companionData);
    }

    public CompanionStats calculateCompanionStats(CompanionData companionData) {
        return statsService.calculateCompanionStats(companionData);
    }

    public boolean canUseSkill(CompanionData companionData, CompanionSkill skill) {
        return skillService.canUseSkill(companionData, skill);
    }

    public CompanionWisdomUpgrade castCompanionSkill(UUID playerId, UUID companionId, UUID skillId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(playerId, companionId);
        CompanionSkill skill = skillService.findSkill(skillId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion skill was not found."));
        if (!skillService.canUseSkill(companion, skill)) {
            throw new ControlCommandValidationException("Companion cannot use that skill.");
        }
        return repository.findWisdomUpgrade(companionId, skillId)
                .orElse(new CompanionWisdomUpgrade(companionId, skillId, 1, 1.0d, 1.0d, nowEpochMillis, Map.of("cast", "true")));
    }

    public ControlCommandResult handleControlCommand(ControlCommand command, Instant startedAt) {
        if (command.dryRun() && isMutating(command)) {
            return result(command, startedAt, true, "Companion command can be applied: " + command.commandType() + ".", List.of(), true);
        }
        return switch (command.commandType()) {
            case CREATE_COMPANION -> createCommand(command, startedAt);
            case LIST_COMPANIONS -> listCommand(command, startedAt);
            case GET_COMPANION, DEBUG_COMPANION -> getCommand(command, startedAt);
            case SET_COMPANION_LEVEL -> setLevelCommand(command, startedAt);
            case ADD_COMPANION_XP -> addXpCommand(command, startedAt);
            case UPDATE_COMPANION_MORALE -> moraleCommand(command, startedAt);
            case SET_COMPANION_BEHAVIOR -> behaviorCommand(command, startedAt);
            case START_COMPANION_TRAINING -> trainingStartCommand(command, startedAt);
            case CLAIM_COMPANION_TRAINING -> trainingClaimCommand(command, startedAt);
            case CANCEL_COMPANION_TRAINING -> trainingCancelCommand(command, startedAt);
            case UNLOCK_COMPANION_SKILL -> unlockSkillCommand(command, startedAt);
            case UPGRADE_COMPANION_SKILL -> upgradeSkillCommand(command, startedAt);
            case SUMMON_COMPANION -> summonCommand(command, startedAt);
            case RECALL_COMPANION -> recallCommand(command, startedAt);
            case ASSIGN_COMPANION_TO_WALL -> wallAssignCommand(command, startedAt);
            case REMOVE_COMPANION_FROM_WALL -> wallRemoveCommand(command, startedAt);
            case DEBUG_COMPANION_WALL -> wallDebugCommand(command, startedAt);
            case REFRESH_COMPANION_PROJECTION -> projectionCommand(command, startedAt);
            default -> result(command, startedAt, false, "Unsupported companion command: " + command.commandType() + ".", List.of(), false);
        };
    }

    private boolean isMutating(ControlCommand command) {
        return switch (command.commandType()) {
            case LIST_COMPANIONS, GET_COMPANION, DEBUG_COMPANION, DEBUG_COMPANION_WALL, REFRESH_COMPANION_PROJECTION -> false;
            default -> true;
        };
    }

    private ControlCommandResult createCommand(ControlCommand command, Instant startedAt) {
        UUID playerId = UUID.fromString(command.argument("ownerPlayerId"));
        CompanionType type = CompanionType.valueOf(command.arguments().getOrDefault("type", "BRUTE").toUpperCase());
        if (command.dryRun()) {
            return result(command, startedAt, true, "Companion can be created: " + type + ".", List.of(), true);
        }
        CompanionData companion = createCompanion(playerId, type, startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion created: " + companion.companionId() + " type=" + companion.type() + ".", List.of("companion:" + companion.companionId()), false);
    }

    private ControlCommandResult listCommand(ControlCommand command, Instant startedAt) {
        UUID playerId = UUID.fromString(command.argument("ownerPlayerId"));
        List<CompanionData> companions = getCompanions(playerId);
        return result(command, startedAt, true, "Companions: " + companions.size() + ".", companions.stream().map(companion -> "companion:" + companion.companionId()).toList(), false);
    }

    private ControlCommandResult getCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = companion(command);
        String message = "Companion " + companion.companionId() + " type=" + companion.type() + " level=" + companion.level() + " xp=" + companion.xp() + " morale=" + companion.moraleState() + " status=" + companion.status() + " behavior=" + companion.behaviorState();
        return result(command, startedAt, true, message, List.of("companion:" + companion.companionId()), false);
    }

    private ControlCommandResult setLevelCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = companion(command);
        int level = Integer.parseInt(command.argument("level"));
        if (level < 1 || level > CompanionData.MAX_LEVEL) {
            throw new ControlCommandValidationException("Companion level must be between 1 and 70.");
        }
        long xp = new CompanionLevelingService().xpRequiredForLevel(level);
        CompanionData updated = trainingService.applyXp(companion.withProgress(1, 0, companion.calculatedStats(), startedAt.toEpochMilli()), xp, startedAt.toEpochMilli());
        repository.saveCompanion(updated);
        return result(command, startedAt, true, "Companion level set: " + updated.level() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult addXpCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = companion(command);
        long xp = Long.parseLong(command.argument("xp"));
        CompanionData updated = trainingService.applyXp(companion, xp, startedAt.toEpochMilli());
        repository.saveCompanion(updated);
        return result(command, startedAt, true, "Companion XP added. level=" + updated.level() + " xp=" + updated.xp() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult moraleCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = moraleService.updateMorale(UUID.fromString(command.argument("companionId")), CompanionMoraleState.valueOf(command.argument("moraleState").toUpperCase()), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion morale updated: " + updated.moraleState() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult behaviorCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = behaviorEngine.setBehaviorState(UUID.fromString(command.argument("companionId")), CompanionBehaviorState.valueOf(command.argument("behaviorState").toUpperCase()), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion behavior updated: " + updated.behaviorState() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult trainingStartCommand(ControlCommand command, Instant startedAt) {
        CompanionTrainingSession session = trainingService.startCompanionTraining(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion training started: " + session.trainingSessionId() + " xp=" + session.expectedXp() + ".", List.of("companion:" + session.companionId(), "training:" + session.trainingSessionId()), false);
    }

    private ControlCommandResult trainingClaimCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = trainingService.claimCompanionTraining(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion training claimed. level=" + updated.level() + " xp=" + updated.xp() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult trainingCancelCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = trainingService.cancelCompanionTraining(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion training cancelled with partial XP. level=" + updated.level() + " xp=" + updated.xp() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult unlockSkillCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = ownedCompanion(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")));
        CompanionSkill skill = skill(command.argument("skillId"));
        if (!skillService.canUseSkill(companion, skill)) {
            throw new ControlCommandValidationException("Companion is not eligible for this skill.");
        }
        List<CompanionSkillSlot> slots = companion.skillSlots().stream()
                .map(slot -> slot.unlocked() && slot.skillId().isEmpty() ? slot.withSkill(skill.skillId()) : slot)
                .toList();
        CompanionData updated = repository.saveCompanion(companion.withSkillSlots(slots, startedAt.toEpochMilli()));
        return result(command, startedAt, true, "Companion skill unlocked: " + skill.name() + ".", List.of("companion:" + updated.companionId(), "skill:" + skill.skillId()), false);
    }

    private ControlCommandResult upgradeSkillCommand(ControlCommand command, Instant startedAt) {
        CompanionSkill skill = skill(command.argument("skillId"));
        CompanionWisdomUpgrade upgrade = wisdomWellService.upgradeCompanionAbility(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), skill.skillId(), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion skill upgraded: " + skill.name() + " level=" + upgrade.skillLevel() + ".", List.of("companion:" + upgrade.companionId(), "skill:" + upgrade.skillId()), false);
    }

    private ControlCommandResult summonCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = behaviorEngine.summonCompanion(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion summoned.", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult recallCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = behaviorEngine.recallCompanion(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion recalled.", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult wallAssignCommand(ControlCommand command, Instant startedAt) {
        CompanionWallAssignment assignment = wallDefenseService.assignCompanionToWall(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), command.argument("wallSectionId"), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion assigned to wall: " + assignment.wallSectionId() + " bonus=" + assignment.defenseBonus() + ".", List.of("companion:" + assignment.companionId()), false);
    }

    private ControlCommandResult wallRemoveCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = wallDefenseService.removeCompanionFromWall(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion removed from wall.", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult wallDebugCommand(ControlCommand command, Instant startedAt) {
        UUID playerId = UUID.fromString(command.argument("ownerPlayerId"));
        return result(command, startedAt, true, "Companion wall bonus=" + wallDefenseService.calculateWallCompanionBonus(playerId) + " availableSlots=" + wallDefenseService.getAvailableWallSlots(playerId), List.of("companion-wall:" + playerId), false);
    }

    private ControlCommandResult projectionCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = companion(command);
        CompanionOverviewProjection projection = projectionHandler.projectCompanion(companion);
        return result(command, startedAt, true, "Companion projection refreshed: " + projection.displayName() + ".", List.of("companion:" + projection.companionId()), false);
    }

    private CompanionData ownedCompanion(UUID ownerPlayerId, UUID companionId) {
        CompanionData companion = repository.findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        if (!companion.ownerPlayerId().equals(ownerPlayerId)) {
            throw new ControlCommandValidationException("Companion does not belong to this player.");
        }
        return companion;
    }

    private CompanionData companion(ControlCommand command) {
        return repository.findCompanion(UUID.fromString(command.argument("companionId")))
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
    }

    private CompanionSkill skill(String skillIdOrName) {
        return skillService.findSkillByName(skillIdOrName)
                .orElseThrow(() -> new ControlCommandValidationException("Companion skill was not found."));
    }

    private ControlCommandResult result(ControlCommand command, Instant startedAt, boolean success, String message, List<String> objectIds, boolean dryRun) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("domain", "companion");
        if (dryRun) {
            metadata.put("dryRun", "true");
        }
        CommandExecutionState state = dryRun && success ? CommandExecutionState.DRY_RUN_COMPLETED : success ? CommandExecutionState.COMPLETED : CommandExecutionState.FAILED;
        return new ControlCommandResult(command.commandId(), state, success, message, List.of(), objectIds, success ? List.of() : List.of(message), startedAt, Instant.now(), metadata);
    }
}
