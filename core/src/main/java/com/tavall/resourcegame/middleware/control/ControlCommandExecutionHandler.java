package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.domain.AccountProgression;
import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.middleware.asset.GlobalAsset;
import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.asset.GlobalAssetType;
import com.tavall.resourcegame.middleware.asset.PlatformAssetVersion;
import com.tavall.resourcegame.middleware.castle.Castle;
import com.tavall.resourcegame.middleware.castle.CastleId;
import com.tavall.resourcegame.middleware.castle.ICastleDomain;
import com.tavall.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.guild.GuildId;
import com.tavall.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.resourcegame.middleware.guild.IGuildDomain;
import com.tavall.resourcegame.middleware.healing.HealingFacilityLevelDefinition;
import com.tavall.resourcegame.middleware.healing.HealingInventory;
import com.tavall.resourcegame.middleware.healing.HealingMode;
import com.tavall.resourcegame.middleware.healing.HealingValidationResult;
import com.tavall.resourcegame.middleware.healing.TroopHealingPlan;
import com.tavall.resourcegame.middleware.healing.TroopHealingRecipe;
import com.tavall.resourcegame.middleware.healing.TroopWound;
import com.tavall.resourcegame.middleware.healing.WoundSeverity;
import com.tavall.resourcegame.middleware.healing.WoundType;
import com.tavall.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerAccount;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.resourcegame.middleware.node.IResourceNodeDomain;
import com.tavall.resourcegame.middleware.node.MiddlewareResourceType;
import com.tavall.resourcegame.middleware.node.ResourceNode;
import com.tavall.resourcegame.middleware.troop.Troop;
import com.tavall.resourcegame.middleware.troop.TroopId;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ControlCommandExecutionHandler implements IControlCommandDomain, IGuildDomain, ICastleDomain, IResourceNodeDomain, IDependencyInjectableConcrete {
    public ControlCommandResult executeCommand(ControlCommand command, Instant startedAt) {
        try {
            return switch (command.commandType()) {
                case DEBUG_PLAYER_STATE -> debugPlayerState(command, startedAt);
                case REGISTER_GLOBAL_ASSET -> registerGlobalAsset(command, startedAt);
                case REGISTER_PLATFORM_ASSET_VERSION -> registerPlatformAssetVersion(command, startedAt);
                case CREATE_GUILD -> createGuild(command, startedAt);
                case CREATE_CASTLE -> createCastle(command, startedAt);
                case CREATE_RESOURCE_NODE -> createResourceNode(command, startedAt);
                case DEBUG_GUILD_STATE -> debugGuildState(command, startedAt);
                case DEBUG_CASTLE_STATE -> debugCastleState(command, startedAt);
                case RUN_RESOURCE_TICK -> runResourceTick(command, startedAt);
                case RUN_GLOBAL_TICK -> runGlobalTick(command, startedAt);
                case REFRESH_FRONTEND_PROJECTIONS -> informational(command, startedAt, "Projection refresh requested.", List.of());
                case ASSIGN_TROOP_WOUND -> assignTroopWound(command, startedAt);
                case START_TROOP_HEALING -> startTroopHealing(command, startedAt);
                case RUN_HEALING_TICK -> runHealingTick(command, startedAt);
                case GIVE_RESOURCE -> giveResource(command, startedAt);
                case BROADCAST_PLATFORM_MESSAGE -> informational(command, startedAt, "Broadcast queued for platform fanout.", List.of());
                case SYNC_PLATFORM_STATE -> informational(command, startedAt, "Platform sync requested.", List.of());
                case DEBUG_TROOP_HEALING_STATE -> debugTroopHealingState(command, startedAt);
                case GET_ACCOUNT_STATUS -> accountStatus(command, startedAt);
                case ADD_ACCOUNT_EXPERIENCE -> addAccountExperience(command, startedAt);
                case SET_ACCOUNT_LEVEL -> setAccountLevel(command, startedAt);
                case SET_ACCOUNT_DEBUG_MODE -> setAccountDebugMode(command, startedAt);
                case VERIFY_FRONTEND_ACTION -> verifyFrontendAction(command, startedAt);
                case ROUTE_FRONTEND_KD_COMMAND -> routeFrontendKdCommand(command, startedAt);
                case START_CONTROL_SURFACE -> startControlSurface(command, startedAt);
                case CREATE_KINGDOM, ARCHIVE_KINGDOM, DEBUG_KINGDOM_STATE, EVALUATE_KINGDOM_SCALING,
                     RUN_KINGDOM_SIMULATION_TICK, CREATE_KINGDOM_BORDER, UPDATE_KINGDOM_BORDER,
                     DEBUG_KINGDOM_BORDER, RESOLVE_COORDINATE_KINGDOM, SIMULATE_BORDER_CROSSING,
                     CONVERT_PLATFORM_COORDINATE, UPDATE_COORDINATE_CONVERSION_PARAMETERS,
                     DEBUG_COORDINATE_CONVERSION, UPDATE_PLAYER_LOCATION, DEBUG_PLAYER_KINGDOM_LOCATION,
                     FORCE_PLAYER_KINGDOM_TRANSITION, REGISTER_PLATFORM_INSTANCE,
                     UPDATE_PLATFORM_INSTANCE_HEALTH, UPDATE_KINGDOM_INSTANCE_ROUTING,
                     REQUEST_INSTANCE_SWITCH, CONFIRM_INSTANCE_SWITCH, FAIL_INSTANCE_SWITCH,
                     DEBUG_INSTANCE_ROUTING, LIST_EDITABLE_PARAMETERS, GET_EDITABLE_PARAMETER,
                     UPDATE_EDITABLE_PARAMETER, DRY_RUN_EDITABLE_PARAMETER_UPDATE,
                     EVALUATE_NEW_PLAYER_KINGDOM, ASSIGN_NEW_PLAYER_KINGDOM ->
                        getUniversalKingdomSimulationSystem().handleControlCommand(command, startedAt);
                case GET_KINGDOM_CLOCK_STATE, TICK_KINGDOM_CLOCK, TICK_ALL_KINGDOM_CLOCKS,
                     SET_KINGDOM_CLOCK_MODE, SET_KINGDOM_TIME_OVERRIDE, CLEAR_KINGDOM_TIME_OVERRIDE,
                     PAUSE_KINGDOM_CLOCK, RESUME_KINGDOM_CLOCK, UPDATE_KINGDOM_CLOCK_CONFIG,
                     DEBUG_KINGDOM_CLOCK, CREATE_KINGDOM_SCHEDULE_RULE, UPDATE_KINGDOM_SCHEDULE_RULE,
                     ENABLE_KINGDOM_SCHEDULE_RULE, DISABLE_KINGDOM_SCHEDULE_RULE,
                     LIST_ACTIVE_KINGDOM_SCHEDULE_RULES, APPLY_KINGDOM_SCHEDULED_STATE_CHANGES,
                     DEBUG_KINGDOM_SCHEDULE, UPDATE_AGING_TICK_POLICY, RUN_AGING_TICK,
                     DEBUG_AGING_TICK, REFRESH_KINGDOM_CLOCK_PROJECTION,
                     REFRESH_KINGDOM_SCHEDULE_PROJECTION ->
                        getKingdomClockControlSystem().handleControlCommand(command, startedAt);
                case CREATE_CITIZEN, CREATE_CITIZENS, MIGRATE_CITIZEN_IN, LIST_CITIZENS,
                     GET_CITIZEN, DEBUG_CITIZEN, DEBUG_CITIZEN_SUMMARY,
                     UPDATE_CITIZEN_AGE_STAGE, DEBUG_SET_CITIZEN_AGE,
                     DEBUG_AGE_ALL_CITIZENS, ASSIGN_CITIZEN_JOB, CLEAR_CITIZEN_JOB,
                     START_CITIZEN_TRAINING, PROMOTE_CITIZEN_TO_TROOP,
                     DEMOTE_TROOP_TO_CITIZEN, UPDATE_CITIZEN_HEALTH,
                     UPDATE_CITIZEN_MORALE, UPDATE_CITIZEN_NUTRITION,
                     UPDATE_CITIZEN_HOUSING, RUN_CITIZEN_MAINTENANCE,
                     APPLY_CITIZEN_FOOD_EFFECTS, APPLY_CITIZEN_MORALE_EFFECTS,
                     APPLY_CITIZEN_NIGHT_REST_EFFECTS, REFRESH_CITIZEN_SUMMARY_CACHE,
                     REFRESH_CITIZEN_DISPLAY_PROJECTIONS ->
                        getCitizenControlSystem().handleControlCommand(command, startedAt);
                case CREATE_COMPANION, LIST_COMPANIONS, GET_COMPANION, DEBUG_COMPANION,
                     SET_COMPANION_LEVEL, ADD_COMPANION_XP, UPDATE_COMPANION_MORALE,
                     SET_COMPANION_BEHAVIOR, START_COMPANION_TRAINING,
                     CLAIM_COMPANION_TRAINING, CANCEL_COMPANION_TRAINING,
                     UNLOCK_COMPANION_SKILL, UPGRADE_COMPANION_SKILL,
                     SUMMON_COMPANION, RECALL_COMPANION, ASSIGN_COMPANION_TO_WALL,
                     REMOVE_COMPANION_FROM_WALL, DEBUG_COMPANION_WALL,
                     REFRESH_COMPANION_PROJECTION ->
                        getCompanionService().handleControlCommand(command, startedAt);
                default -> rejected(command, startedAt, "Command type is registered but execution is not implemented yet: " + command.commandType() + ".");
            };
        } catch (RuntimeException exception) {
            return new ControlCommandResult(command.commandId(), CommandExecutionState.FAILED, false, exception.getMessage(), List.of(), List.of(), List.of(exception.getMessage()), startedAt, Instant.now(), Map.of());
        }
    }

    private ControlCommandResult debugPlayerState(ControlCommand command, Instant startedAt) {
        UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(command.argument("universalPlayerId")));
        boolean accountExists = getUniversalPlayerAccountRepository().findAccount(playerId).isPresent();
        List<PlatformAccountBinding> bindings = getPlatformAccountBindingRepository().findPlatformBindings(playerId);
        String message = "player=" + playerId + " accountExists=" + accountExists + " bindings=" + bindings.size();
        return informational(command, startedAt, message, List.of("player:" + playerId));
    }

    private ControlCommandResult accountStatus(ControlCommand command, Instant startedAt) {
        UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(command.argument("universalPlayerId")));
        UniversalPlayerAccount account = loadOrCreateAccount(playerId, command, startedAt);
        AccountProgression progression = accountProgression(account);
        boolean debugEnabled = accountDebugEnabled(account);
        String message = "Account level " + progression.level()
                + " xp " + progression.experience() + "/" + progression.requiredExperienceForNextLevel()
                + " totalXp " + progression.totalExperience()
                + " debug=" + (debugEnabled ? "on" : "off");
        return informational(command, startedAt, message, List.of("player:" + playerId));
    }

    private ControlCommandResult addAccountExperience(ControlCommand command, Instant startedAt) {
        UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(command.argument("universalPlayerId")));
        int amount;
        try {
            amount = Integer.parseInt(command.argument("amount"));
        } catch (NumberFormatException exception) {
            return rejected(command, startedAt, "Account XP amount must be a whole number.");
        }
        UniversalPlayerAccount account = loadOrCreateAccount(playerId, command, startedAt);
        AccountProgression nextProgression = accountProgression(account).withAddedExperience(amount);
        UniversalPlayerAccount updated = saveAccountProgression(account, nextProgression, startedAt);
        String message = "Account XP added=" + amount
                + " level=" + nextProgression.level()
                + " xp=" + nextProgression.experience() + "/" + nextProgression.requiredExperienceForNextLevel();
        return completed(command, startedAt, message, List.of("player:" + updated.universalPlayerId()));
    }

    private ControlCommandResult setAccountLevel(ControlCommand command, Instant startedAt) {
        UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(command.argument("universalPlayerId")));
        int level;
        try {
            level = Integer.parseInt(command.argument("level"));
        } catch (NumberFormatException exception) {
            return rejected(command, startedAt, "Account level must be a whole number.");
        }
        UniversalPlayerAccount account = loadOrCreateAccount(playerId, command, startedAt);
        AccountProgression nextProgression = accountProgression(account).withLevel(level);
        UniversalPlayerAccount updated = saveAccountProgression(account, nextProgression, startedAt);
        String message = "Account level set=" + nextProgression.level() + " xp=" + nextProgression.experience() + "/" + nextProgression.requiredExperienceForNextLevel();
        return completed(command, startedAt, message, List.of("player:" + updated.universalPlayerId()));
    }

    private ControlCommandResult setAccountDebugMode(ControlCommand command, Instant startedAt) {
        UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(command.argument("universalPlayerId")));
        String mode = command.argument("mode").toLowerCase();
        UniversalPlayerAccount account = loadOrCreateAccount(playerId, command, startedAt);
        if (mode.equals("status")) {
            boolean debugEnabled = accountDebugEnabled(account);
            return informational(command, startedAt, "Account debug=" + (debugEnabled ? "on" : "off") + ".", List.of("player:" + playerId));
        }
        if (!mode.equals("on") && !mode.equals("off")) {
            return rejected(command, startedAt, "Account debug mode must be on, off, or status.");
        }
        UniversalPlayerAccount updated = saveAccountDebugEnabled(account, mode.equals("on"), startedAt);
        return completed(command, startedAt, "Account debug=" + mode + ".", List.of("player:" + updated.universalPlayerId()));
    }

    private ControlCommandResult registerGlobalAsset(ControlCommand command, Instant startedAt) {
        GlobalAssetId assetId = new GlobalAssetId(command.argument("globalAssetId"));
        GlobalAssetType assetType = GlobalAssetType.valueOf(command.argument("assetType").toUpperCase());
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Global asset can be registered: " + assetId.value(), List.of(assetId.value()));
        }
        GlobalAsset asset = new GlobalAsset(assetId, assetType, command.argument("displayName"), Optional.empty(), startedAt, Map.of("registeredByCommandId", command.commandId().toString()));
        getGlobalAssetRepository().saveGlobalAsset(asset);
        return completed(command, startedAt, "Global asset registered: " + assetId.value(), List.of(assetId.value()));
    }

    private ControlCommandResult registerPlatformAssetVersion(ControlCommand command, Instant startedAt) {
        GlobalAssetId assetId = new GlobalAssetId(command.argument("globalAssetId"));
        GamePlatform platform = GamePlatform.valueOf(command.argument("platform").toUpperCase());
        String assetReference = command.argument("assetReference");
        int version = Integer.parseInt(command.arguments().getOrDefault("version", "1"));
        Optional<String> contentHash = Optional.ofNullable(command.arguments().get("contentHash"))
                .filter(value -> !value.isBlank());
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Platform asset version can be registered: " + assetId.value() + " for " + platform + ".", List.of(assetId.value()));
        }
        PlatformAssetVersion platformAssetVersion = getPlatformAssetVersionRegistrationHandler()
                .registerPlatformAssetVersion(assetId, platform, assetReference, version, contentHash, startedAt);
        return completed(command, startedAt, "Platform asset version registered: " + platformAssetVersion.assetReference(), List.of(assetId.value(), platform.name()));
    }

    private ControlCommandResult createGuild(ControlCommand command, Instant startedAt) {
        UniversalPlayerId ownerPlayerId = UniversalPlayerId.of(UUID.fromString(command.argument("ownerPlayerId")));
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Guild can be created: " + command.argument("name") + ".", List.of("player:" + ownerPlayerId));
        }
        GuildKingdom guild = getGuildCreationHandler().createGuildKingdom(command.argument("name"), command.argument("tag"), ownerPlayerId, startedAt);
        return completed(command, startedAt, "Guild created: " + guild.name(), List.of("guild:" + guild.guildId().value()));
    }

    private ControlCommandResult createCastle(ControlCommand command, Instant startedAt) {
        UniversalPlayerId ownerPlayerId = UniversalPlayerId.of(UUID.fromString(command.argument("ownerPlayerId")));
        Optional<GuildId> guildId = optionalUuid(command, "guildId").map(GuildId::new);
        CanonicalLocation location = canonicalLocation(command);
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Castle can be created for " + ownerPlayerId + ".", List.of("player:" + ownerPlayerId));
        }
        Castle castle = getCastleCreationHandler().createCastleForUniversalPlayer(ownerPlayerId, guildId, location);
        return completed(command, startedAt, "Castle created: " + castle.castleId().value(), List.of("castle:" + castle.castleId().value()));
    }

    private ControlCommandResult createResourceNode(ControlCommand command, Instant startedAt) {
        MiddlewareResourceType nodeType = MiddlewareResourceType.valueOf(command.argument("nodeType").toUpperCase());
        Optional<GuildId> ownerGuildId = optionalUuid(command, "ownerGuildId").map(GuildId::new);
        Optional<UniversalPlayerId> ownerPlayerId = optionalUuid(command, "ownerPlayerId").map(UniversalPlayerId::of);
        int productionRate = Integer.parseInt(command.arguments().getOrDefault("productionRate", "1"));
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Resource node can be created: " + nodeType + ".", List.of("resource-node:" + nodeType));
        }
        ResourceNode node = getResourceNodeCreationHandler().createResourceNode(nodeType, canonicalLocation(command), ownerGuildId, ownerPlayerId, productionRate);
        return completed(command, startedAt, "Resource node created: " + node.nodeId().value(), List.of("resource-node:" + node.nodeId().value()));
    }

    private ControlCommandResult debugGuildState(ControlCommand command, Instant startedAt) {
        GuildId guildId = new GuildId(UUID.fromString(command.argument("guildId")));
        String message = getGuildRepository().findGuild(guildId)
                .map(guild -> "guild=" + guild.guildId().value() + " name=" + guild.name() + " state=" + guild.state())
                .orElse("guild=" + guildId.value() + " missing");
        return informational(command, startedAt, message, List.of("guild:" + guildId.value()));
    }

    private ControlCommandResult debugCastleState(ControlCommand command, Instant startedAt) {
        CastleId castleId = new CastleId(UUID.fromString(command.argument("castleId")));
        String message = getCastleRepository().findCastle(castleId)
                .map(castle -> "castle=" + castle.castleId().value() + " owner=" + castle.ownerPlayerId().value() + " state=" + castle.state())
                .orElse("castle=" + castleId.value() + " missing");
        return informational(command, startedAt, message, List.of("castle:" + castleId.value()));
    }

    private ControlCommandResult runResourceTick(ControlCommand command, Instant startedAt) {
        String resourceNodeId = command.arguments().get("resourceNodeId");
        if (resourceNodeId == null || resourceNodeId.isBlank()) {
            return informational(command, startedAt, "Resource tick accepted. A resourceNodeId is required for actor-scoped production mutation.", List.of());
        }
        return informational(command, startedAt, "Resource tick accepted for node " + resourceNodeId + "; actor-scoped production is handled by gameplay interaction events.", List.of("resource-node:" + resourceNodeId));
    }

    private ControlCommandResult runGlobalTick(ControlCommand command, Instant startedAt) {
        ControlCommandResult healingResult = runHealingTick(command, startedAt);
        ControlCommandResult kingdomResult = getUniversalKingdomSimulationSystem().handleControlCommand(
                new ControlCommand(ControlCommandId.random(), ControlCommandType.RUN_KINGDOM_SIMULATION_TICK, command.issuedBy(), command.issuedFrom(), CommandTargetScope.GLOBAL, command.targetPlatforms(), Map.of(), command.dryRun(), command.createdAt(), command.metadata()),
                startedAt
        );
        ArrayList<String> changedObjectIds = new ArrayList<>();
        changedObjectIds.addAll(healingResult.changedObjectIds());
        changedObjectIds.addAll(kingdomResult.changedObjectIds());
        return command.dryRun()
                ? dryRun(command, startedAt, "Global tick can run healing and kingdom simulation systems.", changedObjectIds)
                : completed(command, startedAt, "Global tick completed healing and kingdom simulation systems.", changedObjectIds);
    }

    private ControlCommandResult assignTroopWound(ControlCommand command, Instant startedAt) {
        TroopId troopId = new TroopId(UUID.fromString(command.argument("troopId")));
        WoundType woundType = WoundType.valueOf(command.argument("woundType").toUpperCase());
        WoundSeverity severity = WoundSeverity.valueOf(command.argument("severity").toUpperCase());
        Troop troop = getTroopRepository().findTroop(troopId)
                .orElseThrow(() -> new ControlCommandValidationException("Troop was not found."));
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Wound can be assigned to troop " + troop.troopId().value() + ".", List.of("troop:" + troop.troopId().value()));
        }
        TroopWound wound = getTroopWoundAssignmentHandler().assignWound(troopId, woundType, severity, startedAt);
        return completed(command, startedAt, "Wound assigned: " + wound.woundType() + " " + wound.severity(), List.of("troop:" + troopId.value(), "wound:" + wound.woundId()));
    }

    private ControlCommandResult startTroopHealing(ControlCommand command, Instant startedAt) {
        UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(command.argument("universalPlayerId")));
        TroopId troopId = new TroopId(UUID.fromString(command.argument("troopId")));
        HealingMode healingMode = HealingMode.valueOf(command.argument("healingMode").toUpperCase());
        Troop troop = getTroopRepository().findTroop(troopId)
                .orElseThrow(() -> new ControlCommandValidationException("Troop was not found."));
        TroopWound wound = getTroopHealingRepository().findActiveWoundsForTroop(troopId).stream()
                .findFirst()
                .orElseThrow(() -> new ControlCommandValidationException("Troop has no active wound."));
        TroopHealingRecipe recipe = command.arguments().containsKey("recipeId")
                ? getTroopHealingRecipeSelectionHandler().findRecipe(command.argument("recipeId")).orElseThrow(() -> new ControlCommandValidationException("Healing recipe was not found."))
                : recipeForMode(healingMode, wound.woundType());
        Optional<HealingFacilityLevelDefinition> facility = optionalFacility(command);
        HealingInventory inventory = getHealingInventoryRepository().findInventory(playerId).orElse(new HealingInventory(Map.of()));
        HealingValidationResult validationResult = getTroopHealingRecipeValidationHandler().validateHealingRecipe(troop, wound, recipe, inventory, facility);
        if (command.dryRun()) {
            return validationResult.valid()
                    ? dryRun(command, startedAt, "Healing can start with recipe " + recipe.recipeId() + ".", List.of("troop:" + troopId.value(), "wound:" + wound.woundId()))
                    : rejected(command, startedAt, validationResult.disabledReason().orElse("Healing cannot start."));
        }
        TroopHealingPlan plan = getTroopHealingStartHandler().startHealing(playerId, wound, recipe, facility, startedAt);
        return completed(command, startedAt, "Healing started: " + plan.healingPlanId(), List.of("troop:" + troopId.value(), "healingPlan:" + plan.healingPlanId()));
    }

    private ControlCommandResult runHealingTick(ControlCommand command, Instant startedAt) {
        int tickCount = Integer.parseInt(command.arguments().getOrDefault("tickCount", "1"));
        Instant tickAt = startedAt.plus(Duration.ofDays(Math.max(1, tickCount)));
        List<TroopHealingPlan> updatedPlans = getTroopHealingProgressTickHandler().runHealingProgressTick(tickAt);
        List<String> changedObjectIds = updatedPlans.stream()
                .map(plan -> "healingPlan:" + plan.healingPlanId())
                .toList();
        return completed(command, startedAt, "Healing tick updated " + updatedPlans.size() + " plans.", changedObjectIds);
    }

    private ControlCommandResult giveResource(ControlCommand command, Instant startedAt) {
        UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(command.argument("universalPlayerId")));
        GlobalAssetId assetId = new GlobalAssetId(command.argument("globalAssetId"));
        int amount = Integer.parseInt(command.argument("amount"));
        if (amount <= 0) {
            throw new ControlCommandValidationException("Resource amount must be positive.");
        }
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Resource grant can be applied: " + amount + " " + assetId.value(), List.of("inventory:" + playerId));
        }
        HealingInventory inventory = getHealingInventoryRepository().findInventory(playerId).orElse(new HealingInventory(Map.of()));
        getHealingInventoryRepository().saveInventory(playerId, inventory.withAdded(assetId, amount));
        return completed(command, startedAt, "Resource granted: " + amount + " " + assetId.value(), List.of("inventory:" + playerId + ":" + assetId.value()));
    }

    private ControlCommandResult debugTroopHealingState(ControlCommand command, Instant startedAt) {
        TroopId troopId = new TroopId(UUID.fromString(command.argument("troopId")));
        List<TroopWound> wounds = getTroopHealingRepository().findActiveWoundsForTroop(troopId);
        Optional<TroopHealingPlan> activePlan = getTroopHealingRepository().findActiveHealingPlanForTroop(troopId);
        String message = "troop=" + troopId.value() + " activeWounds=" + wounds.size() + " activeHealingPlan=" + activePlan.map(plan -> plan.healingPlanId().toString()).orElse("none");
        ArrayList<String> changedObjectIds = new ArrayList<>();
        changedObjectIds.add("troop:" + troopId.value());
        wounds.forEach(wound -> changedObjectIds.add("wound:" + wound.woundId()));
        activePlan.ifPresent(plan -> changedObjectIds.add("healingPlan:" + plan.healingPlanId()));
        return informational(command, startedAt, message, changedObjectIds);
    }

    private ControlCommandResult verifyFrontendAction(ControlCommand command, Instant startedAt) {
        String message = "Frontend action accepted platform="
                + command.argument("platform")
                + " surface="
                + command.argument("surface")
                + " category="
                + command.argument("category");
        return informational(command, startedAt, message, List.of("frontend:" + command.argument("platform") + ":" + command.argument("category")));
    }

    private ControlCommandResult routeFrontendKdCommand(ControlCommand command, Instant startedAt) {
        String platform = command.metadata().getOrDefault("platform", command.issuedFrom().name());
        String sourceAccount = command.metadata().getOrDefault("platformAccountId", "");
        String message = "Kingdom command accepted platform="
                + platform
                + " category="
                + command.argument("category")
                + (sourceAccount.isBlank() ? "" : " account=" + sourceAccount);
        return informational(command, startedAt, message, List.of("frontend-kd:" + command.argument("category")));
    }

    private UniversalPlayerAccount loadOrCreateAccount(UniversalPlayerId playerId, ControlCommand command, Instant now) {
        return getUniversalPlayerAccountRepository().findAccount(playerId)
                .orElseGet(() -> {
                    String displayName = command.metadata().getOrDefault("platformDisplayName", "Player");
                    UniversalPlayerAccount created = new UniversalPlayerAccount(
                            playerId,
                            displayName,
                            Optional.empty(),
                            now,
                            now,
                            false,
                            Map.of()
                    );
                    return getUniversalPlayerAccountRepository().saveAccount(created);
                });
    }

    private AccountProgression accountProgression(UniversalPlayerAccount account) {
        Map<String, String> metadata = account.metadata();
        int level = parseInt(metadata.get("accountLevel"), AccountProgression.defaults().level());
        int experience = parseInt(metadata.get("accountExperience"), AccountProgression.defaults().experience());
        long totalExperience = parseLong(metadata.get("accountTotalExperience"), AccountProgression.defaults().totalExperience());
        return new AccountProgression(level, experience, totalExperience);
    }

    private boolean accountDebugEnabled(UniversalPlayerAccount account) {
        return Boolean.parseBoolean(account.metadata().getOrDefault("accountDebugEnabled", "false"));
    }

    private UniversalPlayerAccount saveAccountProgression(UniversalPlayerAccount account, AccountProgression progression, Instant now) {
        Map<String, String> nextMetadata = new LinkedHashMap<>(account.metadata());
        nextMetadata.put("accountLevel", String.valueOf(progression.level()));
        nextMetadata.put("accountExperience", String.valueOf(progression.experience()));
        nextMetadata.put("accountTotalExperience", String.valueOf(progression.totalExperience()));
        UniversalPlayerAccount updated = new UniversalPlayerAccount(
                account.universalPlayerId(),
                account.displayName(),
                account.primaryEmail(),
                account.createdAt(),
                now,
                account.disabled(),
                nextMetadata
        );
        return getUniversalPlayerAccountRepository().saveAccount(updated);
    }

    private UniversalPlayerAccount saveAccountDebugEnabled(UniversalPlayerAccount account, boolean enabled, Instant now) {
        Map<String, String> nextMetadata = new LinkedHashMap<>(account.metadata());
        nextMetadata.put("accountDebugEnabled", String.valueOf(enabled));
        UniversalPlayerAccount updated = new UniversalPlayerAccount(
                account.universalPlayerId(),
                account.displayName(),
                account.primaryEmail(),
                account.createdAt(),
                now,
                account.disabled(),
                nextMetadata
        );
        return getUniversalPlayerAccountRepository().saveAccount(updated);
    }

    private int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private ControlCommandResult startControlSurface(ControlCommand command, Instant startedAt) {
        String surface = command.argument("surface");
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Control surface can be launched: " + surface + ".", List.of("control-surface:" + surface));
        }
        ControlSurfaceLaunchResult launchResult = getControlSurfaceLaunchHandler().startSurface(surface, command.arguments());
        if (!launchResult.success()) {
            return rejected(command, startedAt, launchResult.message());
        }
        return new ControlCommandResult(
                command.commandId(),
                CommandExecutionState.COMPLETED,
                true,
                launchResult.message(),
                List.of(),
                List.of("control-surface:" + surface),
                List.of(),
                startedAt,
                Instant.now(),
                launchResult.metadata()
        );
    }

    private TroopHealingRecipe recipeForMode(HealingMode healingMode, WoundType woundType) {
        return healingMode == HealingMode.FOOD_ONLY
                ? getTroopHealingRecipeSelectionHandler().foodOnlyRecipeFor(woundType)
                : getTroopHealingRecipeSelectionHandler().properTreatmentRecipeFor(woundType);
    }

    private Optional<HealingFacilityLevelDefinition> optionalFacility(ControlCommand command) {
        String facilityLevel = command.arguments().get("facilityLevel");
        if (facilityLevel == null || facilityLevel.isBlank()) {
            return Optional.empty();
        }
        return getHealingFacilityDefinitionRegistry().definitionForLevel(Integer.parseInt(facilityLevel));
    }

    private Optional<UUID> optionalUuid(ControlCommand command, String argumentName) {
        return Optional.ofNullable(command.arguments().get(argumentName))
                .filter(value -> !value.isBlank())
                .map(UUID::fromString);
    }

    private CanonicalLocation canonicalLocation(ControlCommand command) {
        return new CanonicalLocation(
                command.arguments().getOrDefault("worldName", "default"),
                Double.parseDouble(command.arguments().getOrDefault("x", "0")),
                Double.parseDouble(command.arguments().getOrDefault("y", "64")),
                Double.parseDouble(command.arguments().getOrDefault("z", "0"))
        );
    }

    private ControlCommandResult dryRun(ControlCommand command, Instant startedAt, String message, List<String> changedObjectIds) {
        return new ControlCommandResult(command.commandId(), CommandExecutionState.DRY_RUN_COMPLETED, true, message, List.of(), changedObjectIds, List.of(), startedAt, Instant.now(), Map.of("dryRun", "true"));
    }

    private ControlCommandResult informational(ControlCommand command, Instant startedAt, String message, List<String> changedObjectIds) {
        if (command.dryRun()) {
            return dryRun(command, startedAt, message, changedObjectIds);
        }
        return new ControlCommandResult(command.commandId(), CommandExecutionState.COMPLETED, true, message, List.of(), changedObjectIds, List.of(), startedAt, Instant.now(), Map.of("mutatedCanonicalState", "false"));
    }

    private ControlCommandResult completed(ControlCommand command, Instant startedAt, String message, List<String> changedObjectIds) {
        return new ControlCommandResult(command.commandId(), CommandExecutionState.COMPLETED, true, message, List.of(), changedObjectIds, List.of(), startedAt, Instant.now(), Map.of("mutatedCanonicalState", "true"));
    }

    private ControlCommandResult rejected(ControlCommand command, Instant startedAt, String message) {
        return new ControlCommandResult(command.commandId(), CommandExecutionState.REJECTED, false, message, List.of(), List.of(), List.of(message), startedAt, Instant.now(), Map.of());
    }
}
