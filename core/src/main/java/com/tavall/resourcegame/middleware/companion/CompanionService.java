package com.tavall.resourcegame.middleware.companion;

import com.tavall.resourcegame.middleware.control.CommandExecutionState;
import com.tavall.resourcegame.middleware.control.ControlCommand;
import com.tavall.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.resourcegame.middleware.control.ControlCommandValidationException;
import com.tavall.resourcegame.persistence.PostgresCompanionRepository;
import com.tavall.resourcegame.persistence.PostgresConnectionProvider;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CompanionService implements ICompanionDomain {
    public CompanionService() {
    }

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
        registerCompanionRepository(repository);
        registerCompanionFactory(companionFactory);
        registerCompanionStatsService(statsService);
        registerCompanionSkillService(skillService);
        registerCompanionTrainingService(trainingService);
        registerWisdomWellService(wisdomWellService);
        registerCompanionBehaviorEngine(behaviorEngine);
        registerCompanionMoraleService(moraleService);
        registerCompanionWallDefenseService(wallDefenseService);
        registerCompanionProjectionHandler(projectionHandler);
        registerCompanionService(this);
    }

    public static CompanionService inMemory() {
        CompanionRepository repository = new InMemoryCompanionRepository();
        return withRepository(repository);
    }

    public static CompanionService postgres(PostgresConnectionProvider connectionProvider) {
        return withRepository(new PostgresCompanionRepository(connectionProvider));
    }

    public static CompanionService withRepository(CompanionRepository repository) {
        CompanionService service = new CompanionService();
        service.registerCompanionRepository(repository);
        service.registerCompanionStatsService(new CompanionStatsService());
        service.registerCompanionSkillService(new CompanionSkillService());
        service.registerCompanionLevelingService(new CompanionLevelingService());
        service.registerCompanionFactory(new CompanionFactory());
        service.registerCompanionTrainingService(new CompanionTrainingService());
        service.registerWisdomWellService(new WisdomWellService());
        service.registerCompanionBehaviorEngine(new CompanionBehaviorEngine());
        service.registerCompanionMoraleService(new CompanionMoraleService());
        service.registerCompanionWallDefenseService(new CompanionWallDefenseService());
        service.registerCompanionProjectionHandler(new CompanionProjectionHandler());
        return service.registerCompanionService(service);
    }

    public CompanionData createCompanion(UUID playerId, CompanionType type, long nowEpochMillis) {
        CompanionData companion = getCompanionFactory().createCompanion(playerId, type, nowEpochMillis);
        return getCompanionRepository().saveCompanion(companion);
    }

    public Optional<CompanionData> getActiveCompanion(UUID playerId) {
        return getCompanionRepository().findActiveCompanion(playerId);
    }

    public List<CompanionData> getCompanions(UUID playerId) {
        return getCompanionRepository().findCompanionsForPlayer(playerId);
    }

    public void saveCompanionAsync(CompanionData companionData) {
        getCompanionRepository().saveCompanion(companionData);
    }

    public CompanionStats calculateCompanionStats(CompanionData companionData) {
        return getCompanionStatsService().calculateCompanionStats(companionData);
    }

    public boolean canUseSkill(CompanionData companionData, CompanionSkill skill) {
        return getCompanionSkillService().canUseSkill(companionData, skill);
    }

    public CompanionWisdomUpgrade castCompanionSkill(UUID playerId, UUID companionId, UUID skillId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(playerId, companionId);
        CompanionSkillService skillService = getCompanionSkillService();
        CompanionSkill skill = skillService.findSkill(skillId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion skill was not found."));
        if (!skillService.canUseSkill(companion, skill)) {
            throw new ControlCommandValidationException("Companion cannot use that skill.");
        }
        return getCompanionRepository().findWisdomUpgrade(companionId, skillId)
                .orElse(new CompanionWisdomUpgrade(companionId, skillId, 1, 1.0d, 1.0d, nowEpochMillis, Map.of("cast", "true")));
    }

    public ControlCommandResult handleControlCommand(ControlCommand command, Instant startedAt) {
        if (command.dryRun() && isMutating(command)) {
            return result(command, startedAt, true, "Companion action can be applied: " + command.commandType() + ".", List.of(), true);
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
            return result(command, startedAt, true, "A " + type.name().toLowerCase() + " companion can be recruited.", List.of(), true);
        }
        CompanionData companion = createCompanion(playerId, type, startedAt.toEpochMilli());
        return result(command, startedAt, true, "A " + companion.type().name().toLowerCase() + " companion joins your retinue. ref=" + companion.companionId(), List.of("companion:" + companion.companionId()), false);
    }

    private ControlCommandResult listCommand(ControlCommand command, Instant startedAt) {
        UUID playerId = UUID.fromString(command.argument("ownerPlayerId"));
        List<CompanionData> companions = getCompanions(playerId);
        return result(command, startedAt, true, "Your retinue has " + companions.size() + " companion" + (companions.size() == 1 ? "" : "s") + ".", companions.stream().map(companion -> "companion:" + companion.companionId()).toList(), false);
    }

    private ControlCommandResult getCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = companion(command);
        String message = companion.type().name().toLowerCase() + " companion level " + companion.level() + ", " + companion.moraleState().name().toLowerCase() + " morale, " + companion.behaviorState().name().toLowerCase() + " stance.";
        return result(command, startedAt, true, message, List.of("companion:" + companion.companionId()), false);
    }

    private ControlCommandResult setLevelCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = companion(command);
        int level = Integer.parseInt(command.argument("level"));
        if (level < 1 || level > CompanionData.MAX_LEVEL) {
            throw new ControlCommandValidationException("Companion level must be between 1 and 70.");
        }
        long xp = getCompanionLevelingService().xpRequiredForLevel(level);
        CompanionData updated = getCompanionTrainingService().applyXp(companion.withProgress(1, 0, companion.calculatedStats(), startedAt.toEpochMilli()), xp, startedAt.toEpochMilli());
        getCompanionRepository().saveCompanion(updated);
        return result(command, startedAt, true, "Your companion reaches level=" + updated.level() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult addXpCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = companion(command);
        long xp = Long.parseLong(command.argument("xp"));
        CompanionData updated = getCompanionTrainingService().applyXp(companion, xp, startedAt.toEpochMilli());
        getCompanionRepository().saveCompanion(updated);
        return result(command, startedAt, true, "Your companion gains strength. level=" + updated.level() + " xp=" + updated.xp() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult moraleCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = getCompanionMoraleService().updateMorale(UUID.fromString(command.argument("companionId")), CompanionMoraleState.valueOf(command.argument("moraleState").toUpperCase()), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion morale shifts to " + updated.moraleState().name().toLowerCase() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult behaviorCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = getCompanionBehaviorEngine().setBehaviorState(UUID.fromString(command.argument("companionId")), CompanionBehaviorState.valueOf(command.argument("behaviorState").toUpperCase()), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion stance set to " + updated.behaviorState().name().toLowerCase() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult trainingStartCommand(ControlCommand command, Instant startedAt) {
        CompanionTrainingSession session = getCompanionTrainingService().startCompanionTraining(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion begins training. xp " + session.expectedXp() + ".", List.of("companion:" + session.companionId(), "training:" + session.trainingSessionId()), false);
    }

    private ControlCommandResult trainingClaimCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = getCompanionTrainingService().claimCompanionTraining(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Training complete. Your companion now stands at level " + updated.level() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult trainingCancelCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = getCompanionTrainingService().cancelCompanionTraining(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Training interrupted. Progress is banked at level " + updated.level() + ".", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult unlockSkillCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = ownedCompanion(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")));
        CompanionSkill skill = skill(command.argument("skillId"));
        if (!getCompanionSkillService().canUseSkill(companion, skill)) {
            throw new ControlCommandValidationException("Companion is not eligible for this skill.");
        }
        List<CompanionSkillSlot> slots = companion.skillSlots().stream()
                .map(slot -> slot.unlocked() && slot.skillId().isEmpty() ? slot.withSkill(skill.skillId()) : slot)
                .toList();
        CompanionData updated = getCompanionRepository().saveCompanion(companion.withSkillSlots(slots, startedAt.toEpochMilli()));
        return result(command, startedAt, true, skill.name() + " is now available.", List.of("companion:" + updated.companionId(), "skill:" + skill.skillId()), false);
    }

    private ControlCommandResult upgradeSkillCommand(ControlCommand command, Instant startedAt) {
        CompanionSkill skill = skill(command.argument("skillId"));
        CompanionWisdomUpgrade upgrade = getWisdomWellService().upgradeCompanionAbility(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), skill.skillId(), startedAt.toEpochMilli());
        return result(command, startedAt, true, skill.name() + " rises to rank " + upgrade.skillLevel() + ".", List.of("companion:" + upgrade.companionId(), "skill:" + upgrade.skillId()), false);
    }

    private ControlCommandResult summonCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = getCompanionBehaviorEngine().summonCompanion(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Your companion answers the call.", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult recallCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = getCompanionBehaviorEngine().recallCompanion(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Your companion returns to idle.", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult wallAssignCommand(ControlCommand command, Instant startedAt) {
        CompanionWallAssignment assignment = getCompanionWallDefenseService().assignCompanionToWall(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), command.argument("wallSectionId"), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Wall assignment secured on " + assignment.wallSectionId() + ".", List.of("companion:" + assignment.companionId()), false);
    }

    private ControlCommandResult wallRemoveCommand(ControlCommand command, Instant startedAt) {
        CompanionData updated = getCompanionWallDefenseService().removeCompanionFromWall(UUID.fromString(command.argument("ownerPlayerId")), UUID.fromString(command.argument("companionId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Companion leaves the wall.", List.of("companion:" + updated.companionId()), false);
    }

    private ControlCommandResult wallDebugCommand(ControlCommand command, Instant startedAt) {
        UUID playerId = UUID.fromString(command.argument("ownerPlayerId"));
        CompanionWallDefenseService wallDefenseService = getCompanionWallDefenseService();
        return result(command, startedAt, true, "Wall support bonus " + wallDefenseService.calculateWallCompanionBonus(playerId) + " with open posts " + wallDefenseService.getAvailableWallSlots(playerId), List.of("companion-wall:" + playerId), false);
    }

    private ControlCommandResult projectionCommand(ControlCommand command, Instant startedAt) {
        CompanionData companion = companion(command);
        CompanionOverviewProjection projection = getCompanionProjectionHandler().projectCompanion(companion);
        return result(command, startedAt, true, "Companion projection refreshed: " + projection.displayName() + ".", List.of("companion:" + projection.companionId()), false);
    }

    private CompanionData ownedCompanion(UUID ownerPlayerId, UUID companionId) {
        CompanionData companion = getCompanionRepository().findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        if (!companion.ownerPlayerId().equals(ownerPlayerId)) {
            throw new ControlCommandValidationException("Companion does not belong to this player.");
        }
        return companion;
    }

    private CompanionData companion(ControlCommand command) {
        return getCompanionRepository().findCompanion(UUID.fromString(command.argument("companionId")))
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
    }

    private CompanionSkill skill(String skillIdOrName) {
        return getCompanionSkillService().findSkillByName(skillIdOrName)
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
