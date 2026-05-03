package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAsset;
import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetRepository;
import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetType;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.healing.HealingFacilityDefinitionRegistry;
import com.tavall.hytale.resourcegame.middleware.healing.HealingFacilityLevelDefinition;
import com.tavall.hytale.resourcegame.middleware.healing.HealingInventory;
import com.tavall.hytale.resourcegame.middleware.healing.HealingInventoryRepository;
import com.tavall.hytale.resourcegame.middleware.healing.HealingMode;
import com.tavall.hytale.resourcegame.middleware.healing.HealingValidationResult;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingPlan;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingProgressTickHandler;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingRecipe;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingRecipeSelectionHandler;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingRecipeValidationHandler;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingRepository;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingStartHandler;
import com.tavall.hytale.resourcegame.middleware.healing.TroopWound;
import com.tavall.hytale.resourcegame.middleware.healing.TroopWoundAssignmentHandler;
import com.tavall.hytale.resourcegame.middleware.healing.WoundSeverity;
import com.tavall.hytale.resourcegame.middleware.healing.WoundType;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountBindingRepository;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerAccountRepository;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopId;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ControlCommandExecutionHandler {
    private final UniversalPlayerAccountRepository accountRepository;
    private final PlatformAccountBindingRepository platformAccountBindingRepository;
    private final GlobalAssetRepository globalAssetRepository;
    private final TroopRepository troopRepository;
    private final TroopHealingRepository troopHealingRepository;
    private final HealingInventoryRepository healingInventoryRepository;
    private final HealingFacilityDefinitionRegistry healingFacilityDefinitionRegistry;
    private final TroopWoundAssignmentHandler woundAssignmentHandler;
    private final TroopHealingRecipeSelectionHandler recipeSelectionHandler;
    private final TroopHealingRecipeValidationHandler recipeValidationHandler;
    private final TroopHealingStartHandler healingStartHandler;
    private final TroopHealingProgressTickHandler healingProgressTickHandler;
    private final ControlSurfaceLaunchHandler surfaceLaunchHandler;

    public ControlCommandExecutionHandler(
            UniversalPlayerAccountRepository accountRepository,
            PlatformAccountBindingRepository platformAccountBindingRepository,
            GlobalAssetRepository globalAssetRepository,
            TroopRepository troopRepository,
            TroopHealingRepository troopHealingRepository,
            HealingInventoryRepository healingInventoryRepository,
            HealingFacilityDefinitionRegistry healingFacilityDefinitionRegistry,
            TroopWoundAssignmentHandler woundAssignmentHandler,
            TroopHealingRecipeSelectionHandler recipeSelectionHandler,
            TroopHealingRecipeValidationHandler recipeValidationHandler,
            TroopHealingStartHandler healingStartHandler,
            TroopHealingProgressTickHandler healingProgressTickHandler,
            ControlSurfaceLaunchHandler surfaceLaunchHandler
    ) {
        this.accountRepository = accountRepository;
        this.platformAccountBindingRepository = platformAccountBindingRepository;
        this.globalAssetRepository = globalAssetRepository;
        this.troopRepository = troopRepository;
        this.troopHealingRepository = troopHealingRepository;
        this.healingInventoryRepository = healingInventoryRepository;
        this.healingFacilityDefinitionRegistry = healingFacilityDefinitionRegistry;
        this.woundAssignmentHandler = woundAssignmentHandler;
        this.recipeSelectionHandler = recipeSelectionHandler;
        this.recipeValidationHandler = recipeValidationHandler;
        this.healingStartHandler = healingStartHandler;
        this.healingProgressTickHandler = healingProgressTickHandler;
        this.surfaceLaunchHandler = surfaceLaunchHandler;
    }

    public ControlCommandResult executeCommand(ControlCommand command, Instant startedAt) {
        try {
            return switch (command.commandType()) {
                case DEBUG_PLAYER_STATE -> debugPlayerState(command, startedAt);
                case REGISTER_GLOBAL_ASSET -> registerGlobalAsset(command, startedAt);
                case REFRESH_FRONTEND_PROJECTIONS -> informational(command, startedAt, "Projection refresh requested.", List.of());
                case ASSIGN_TROOP_WOUND -> assignTroopWound(command, startedAt);
                case START_TROOP_HEALING -> startTroopHealing(command, startedAt);
                case RUN_HEALING_TICK -> runHealingTick(command, startedAt);
                case GIVE_RESOURCE -> giveResource(command, startedAt);
                case BROADCAST_PLATFORM_MESSAGE -> informational(command, startedAt, "Broadcast queued for platform fanout.", List.of());
                case SYNC_PLATFORM_STATE -> informational(command, startedAt, "Platform sync requested.", List.of());
                case DEBUG_TROOP_HEALING_STATE -> debugTroopHealingState(command, startedAt);
                case VERIFY_FRONTEND_ACTION -> verifyFrontendAction(command, startedAt);
                case START_CONTROL_SURFACE -> startControlSurface(command, startedAt);
                default -> rejected(command, startedAt, "Command type is registered but execution is not implemented yet: " + command.commandType() + ".");
            };
        } catch (RuntimeException exception) {
            return new ControlCommandResult(command.commandId(), CommandExecutionState.FAILED, false, exception.getMessage(), List.of(), List.of(), List.of(exception.getMessage()), startedAt, Instant.now(), Map.of());
        }
    }

    private ControlCommandResult debugPlayerState(ControlCommand command, Instant startedAt) {
        UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(command.argument("universalPlayerId")));
        boolean accountExists = accountRepository.findAccount(playerId).isPresent();
        List<PlatformAccountBinding> bindings = platformAccountBindingRepository.findPlatformBindings(playerId);
        String message = "player=" + playerId + " accountExists=" + accountExists + " bindings=" + bindings.size();
        return informational(command, startedAt, message, List.of("player:" + playerId));
    }

    private ControlCommandResult registerGlobalAsset(ControlCommand command, Instant startedAt) {
        GlobalAssetId assetId = new GlobalAssetId(command.argument("globalAssetId"));
        GlobalAssetType assetType = GlobalAssetType.valueOf(command.argument("assetType").toUpperCase());
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Global asset can be registered: " + assetId.value(), List.of(assetId.value()));
        }
        GlobalAsset asset = new GlobalAsset(assetId, assetType, command.argument("displayName"), Optional.empty(), startedAt, Map.of("registeredByCommandId", command.commandId().toString()));
        globalAssetRepository.saveGlobalAsset(asset);
        return completed(command, startedAt, "Global asset registered: " + assetId.value(), List.of(assetId.value()));
    }

    private ControlCommandResult assignTroopWound(ControlCommand command, Instant startedAt) {
        TroopId troopId = new TroopId(UUID.fromString(command.argument("troopId")));
        WoundType woundType = WoundType.valueOf(command.argument("woundType").toUpperCase());
        WoundSeverity severity = WoundSeverity.valueOf(command.argument("severity").toUpperCase());
        Troop troop = troopRepository.findTroop(troopId)
                .orElseThrow(() -> new ControlCommandValidationException("Troop was not found."));
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Wound can be assigned to troop " + troop.troopId().value() + ".", List.of("troop:" + troop.troopId().value()));
        }
        TroopWound wound = woundAssignmentHandler.assignWound(troopId, woundType, severity, startedAt);
        return completed(command, startedAt, "Wound assigned: " + wound.woundType() + " " + wound.severity(), List.of("troop:" + troopId.value(), "wound:" + wound.woundId()));
    }

    private ControlCommandResult startTroopHealing(ControlCommand command, Instant startedAt) {
        UniversalPlayerId playerId = UniversalPlayerId.of(UUID.fromString(command.argument("universalPlayerId")));
        TroopId troopId = new TroopId(UUID.fromString(command.argument("troopId")));
        HealingMode healingMode = HealingMode.valueOf(command.argument("healingMode").toUpperCase());
        Troop troop = troopRepository.findTroop(troopId)
                .orElseThrow(() -> new ControlCommandValidationException("Troop was not found."));
        TroopWound wound = troopHealingRepository.findActiveWoundsForTroop(troopId).stream()
                .findFirst()
                .orElseThrow(() -> new ControlCommandValidationException("Troop has no active wound."));
        TroopHealingRecipe recipe = command.arguments().containsKey("recipeId")
                ? recipeSelectionHandler.findRecipe(command.argument("recipeId")).orElseThrow(() -> new ControlCommandValidationException("Healing recipe was not found."))
                : recipeForMode(healingMode, wound.woundType());
        Optional<HealingFacilityLevelDefinition> facility = optionalFacility(command);
        HealingInventory inventory = healingInventoryRepository.findInventory(playerId).orElse(new HealingInventory(Map.of()));
        HealingValidationResult validationResult = recipeValidationHandler.validateHealingRecipe(troop, wound, recipe, inventory, facility);
        if (command.dryRun()) {
            return validationResult.valid()
                    ? dryRun(command, startedAt, "Healing can start with recipe " + recipe.recipeId() + ".", List.of("troop:" + troopId.value(), "wound:" + wound.woundId()))
                    : rejected(command, startedAt, validationResult.disabledReason().orElse("Healing cannot start."));
        }
        TroopHealingPlan plan = healingStartHandler.startHealing(playerId, wound, recipe, facility, startedAt);
        return completed(command, startedAt, "Healing started: " + plan.healingPlanId(), List.of("troop:" + troopId.value(), "healingPlan:" + plan.healingPlanId()));
    }

    private ControlCommandResult runHealingTick(ControlCommand command, Instant startedAt) {
        int tickCount = Integer.parseInt(command.arguments().getOrDefault("tickCount", "1"));
        Instant tickAt = startedAt.plus(Duration.ofDays(Math.max(1, tickCount)));
        List<TroopHealingPlan> updatedPlans = healingProgressTickHandler.runHealingProgressTick(tickAt);
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
        HealingInventory inventory = healingInventoryRepository.findInventory(playerId).orElse(new HealingInventory(Map.of()));
        healingInventoryRepository.saveInventory(playerId, inventory.withAdded(assetId, amount));
        return completed(command, startedAt, "Resource granted: " + amount + " " + assetId.value(), List.of("inventory:" + playerId + ":" + assetId.value()));
    }

    private ControlCommandResult debugTroopHealingState(ControlCommand command, Instant startedAt) {
        TroopId troopId = new TroopId(UUID.fromString(command.argument("troopId")));
        List<TroopWound> wounds = troopHealingRepository.findActiveWoundsForTroop(troopId);
        Optional<TroopHealingPlan> activePlan = troopHealingRepository.findActiveHealingPlanForTroop(troopId);
        String message = "troop=" + troopId.value() + " activeWounds=" + wounds.size() + " activeHealingPlan=" + activePlan.map(plan -> plan.healingPlanId().toString()).orElse("none");
        ArrayList<String> changedObjectIds = new ArrayList<>();
        changedObjectIds.add("troop:" + troopId.value());
        wounds.forEach(wound -> changedObjectIds.add("wound:" + wound.woundId()));
        activePlan.ifPresent(plan -> changedObjectIds.add("healingPlan:" + plan.healingPlanId()));
        return informational(command, startedAt, message, changedObjectIds);
    }

    private ControlCommandResult verifyFrontendAction(ControlCommand command, Instant startedAt) {
        String message = "Frontend action verified platform="
                + command.argument("platform")
                + " surface="
                + command.argument("surface")
                + " category="
                + command.argument("category");
        return informational(command, startedAt, message, List.of("frontend:" + command.argument("platform") + ":" + command.argument("category")));
    }

    private ControlCommandResult startControlSurface(ControlCommand command, Instant startedAt) {
        String surface = command.argument("surface");
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Control surface can be launched: " + surface + ".", List.of("control-surface:" + surface));
        }
        ControlSurfaceLaunchResult launchResult = surfaceLaunchHandler.startSurface(surface, command.arguments());
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
                ? recipeSelectionHandler.foodOnlyRecipeFor(woundType)
                : recipeSelectionHandler.properTreatmentRecipeFor(woundType);
    }

    private Optional<HealingFacilityLevelDefinition> optionalFacility(ControlCommand command) {
        String facilityLevel = command.arguments().get("facilityLevel");
        if (facilityLevel == null || facilityLevel.isBlank()) {
            return Optional.empty();
        }
        return healingFacilityDefinitionRegistry.definitionForLevel(Integer.parseInt(facilityLevel));
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
