package org.tavall.control.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.domain.CitizenJobType;
import org.tavall.control.clock.KingdomClockControlSystem;
import org.tavall.control.runtime.CommandExecutionState;
import org.tavall.control.runtime.ControlCommand;
import org.tavall.control.runtime.ControlCommandResult;
import org.tavall.control.runtime.ControlCommandType;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.persistence.PostgresCitizenRepository;
import org.tavall.control.persistence.PostgresConnectionProvider;
import org.tavall.control.config.CacheConfig;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CitizenControlSystem implements CitizenDomain, IDependencyInjectableConcrete {
    private static final String DEFAULT_KINGDOM_ID = "kingdom-1";

    public CitizenControlSystem() {
    }

    public CitizenControlSystem(
            CitizenRepository citizenRepository,
            CitizenSummaryCacheRepository summaryCacheRepository,
            CitizenAgingConfigRepository agingConfigRepository,
            CitizenCreationHandler creationHandler,
            CitizenReadHandler readHandler,
            CitizenJobAssignmentHandler jobAssignmentHandler,
            CitizenTrainingStartHandler trainingStartHandler,
            CitizenTroopPromotionHandler troopPromotionHandler,
            CitizenTroopDemotionHandler troopDemotionHandler,
            CitizenConditionUpdateHandler conditionUpdateHandler,
            CitizenAgingMaintenanceHandler agingMaintenanceHandler,
            CitizenSummaryCacheRefreshHandler summaryCacheRefreshHandler,
            CitizenProjectionHandler projectionHandler
    ) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenSummaryCacheRepository.class, summaryCacheRepository);
        registerCitizenDependency(CitizenAgingConfigRepository.class, agingConfigRepository);
        registerCitizenDependency(CitizenCreationHandler.class, creationHandler);
        registerCitizenDependency(CitizenReadHandler.class, readHandler);
        registerCitizenDependency(CitizenJobAssignmentHandler.class, jobAssignmentHandler);
        registerCitizenDependency(CitizenTrainingStartHandler.class, trainingStartHandler);
        registerCitizenDependency(CitizenTroopPromotionHandler.class, troopPromotionHandler);
        registerCitizenDependency(CitizenTroopDemotionHandler.class, troopDemotionHandler);
        registerCitizenDependency(CitizenConditionUpdateHandler.class, conditionUpdateHandler);
        registerCitizenDependency(CitizenAgingMaintenanceHandler.class, agingMaintenanceHandler);
        registerCitizenDependency(CitizenSummaryCacheRefreshHandler.class, summaryCacheRefreshHandler);
        registerCitizenDependency(CitizenProjectionHandler.class, projectionHandler);
        registerCitizenDependency(CitizenControlSystem.class, this);
    }

    public static CitizenControlSystem inMemory(KingdomClockControlSystem clockControlSystem) {
        CitizenRepository citizenRepository = new InMemoryCitizenRepository();
        CitizenSummaryCacheRepository cacheRepository = new InMemoryCitizenSummaryCacheRepository();
        CitizenAgingConfigRepository agingConfigRepository = new InMemoryCitizenAgingConfigRepository();
        return withRepositories(citizenRepository, cacheRepository, agingConfigRepository, clockControlSystem);
    }

    public static CitizenControlSystem postgres(PostgresConnectionProvider connectionProvider, KingdomClockControlSystem clockControlSystem) {
        CitizenRepository citizenRepository = new PostgresCitizenRepository(connectionProvider, new ObjectMapper());
        CitizenSummaryCacheRepository cacheRepository = new InMemoryCitizenSummaryCacheRepository();
        CitizenAgingConfigRepository agingConfigRepository = PostgresCitizenAgingConfigRepository.open(
                connectionProvider,
                CacheConfig.fromEnv(),
                new ObjectMapper()
        );
        return withRepositories(citizenRepository, cacheRepository, agingConfigRepository, clockControlSystem);
    }

    public static CitizenControlSystem withRepositories(
            CitizenRepository citizenRepository,
            CitizenSummaryCacheRepository cacheRepository,
            CitizenAgingConfigRepository agingConfigRepository,
            KingdomClockControlSystem clockControlSystem
    ) {
        CitizenAgeStageMappingHandler ageStageMappingHandler = new CitizenAgeStageMappingHandler();
        CitizenAgingCalculationHandler agingCalculationHandler = new CitizenAgingCalculationHandler(ageStageMappingHandler);
        CitizenCacheInvalidationHandler cacheInvalidationHandler = new CitizenCacheInvalidationHandler(cacheRepository);
        CitizenLifeStageEligibilityHandler lifeStageEligibilityHandler = new CitizenLifeStageEligibilityHandler();
        CitizenJobEligibilityHandler jobEligibilityHandler = new CitizenJobEligibilityHandler(lifeStageEligibilityHandler);
        CitizenTrainingEligibilityHandler trainingEligibilityHandler = new CitizenTrainingEligibilityHandler(lifeStageEligibilityHandler);
        CitizenClockIntegrationHandler clockIntegrationHandler = new CitizenClockIntegrationHandler(clockControlSystem);
        CitizenAggregationCalculationHandler aggregationCalculationHandler = new CitizenAggregationCalculationHandler(
                new CitizenFoodEffectHandler(),
                new CitizenMoraleEffectHandler(),
                new CitizenHousingEffectHandler(),
                clockIntegrationHandler
        );
        return new CitizenControlSystem(
                citizenRepository,
                cacheRepository,
                agingConfigRepository,
                new CitizenCreationHandler(citizenRepository, agingCalculationHandler, cacheInvalidationHandler),
                new CitizenReadHandler(citizenRepository, agingCalculationHandler),
                new CitizenJobAssignmentHandler(citizenRepository, jobEligibilityHandler, cacheInvalidationHandler),
                new CitizenTrainingStartHandler(citizenRepository, trainingEligibilityHandler, cacheInvalidationHandler),
                new CitizenTroopPromotionHandler(citizenRepository, trainingEligibilityHandler, cacheInvalidationHandler),
                new CitizenTroopDemotionHandler(citizenRepository, cacheInvalidationHandler),
                new CitizenConditionUpdateHandler(citizenRepository, cacheInvalidationHandler),
                new CitizenAgingMaintenanceHandler(citizenRepository, agingCalculationHandler, cacheInvalidationHandler),
                new CitizenSummaryCacheRefreshHandler(citizenRepository, cacheRepository, aggregationCalculationHandler),
                new CitizenProjectionHandler()
        );
    }

    public ControlCommandResult handleControlCommand(ControlCommand command, Instant startedAt) {
        return switch (command.commandType()) {
            case CREATE_CITIZEN, CREATE_CITIZENS, MIGRATE_CITIZEN_IN -> createCitizensCommand(command, startedAt);
            case LIST_CITIZENS -> listCitizensCommand(command, startedAt);
            case GET_CITIZEN, DEBUG_CITIZEN -> getCitizenCommand(command, startedAt);
            case DEBUG_CITIZEN_SUMMARY -> summaryCommand(command, startedAt);
            case ASSIGN_CITIZEN_JOB -> assignJobCommand(command, startedAt);
            case CLEAR_CITIZEN_JOB -> clearJobCommand(command, startedAt);
            case START_CITIZEN_TRAINING -> startTrainingCommand(command, startedAt);
            case PROMOTE_CITIZEN_TO_TROOP -> promoteCommand(command, startedAt);
            case DEMOTE_TROOP_TO_CITIZEN -> demoteCommand(command, startedAt);
            case UPDATE_CITIZEN_AGE_STAGE -> updateAgeStageCommand(command, startedAt);
            case DEBUG_SET_CITIZEN_AGE -> debugSetAgeCommand(command, startedAt);
            case DEBUG_AGE_ALL_CITIZENS, RUN_CITIZEN_MAINTENANCE -> maintenanceCommand(command, startedAt);
            case UPDATE_CITIZEN_HEALTH, UPDATE_CITIZEN_MORALE, UPDATE_CITIZEN_NUTRITION, UPDATE_CITIZEN_HOUSING -> conditionCommand(command, startedAt);
            case APPLY_CITIZEN_FOOD_EFFECTS, APPLY_CITIZEN_MORALE_EFFECTS, APPLY_CITIZEN_NIGHT_REST_EFFECTS -> effectsCommand(command, startedAt);
            case REFRESH_CITIZEN_SUMMARY_CACHE -> refreshSummaryCommand(command, startedAt);
            case REFRESH_CITIZEN_DISPLAY_PROJECTIONS -> refreshDisplaysCommand(command, startedAt);
            default -> result(command, startedAt, false, "Unsupported citizen command: " + command.commandType() + ".", List.of(), Map.of(), false);
        };
    }

    public List<CitizenData> citizensForPlayer(UniversalPlayerId playerId, Instant now) {
        return getCitizenReadHandler().findCitizensForPlayer(playerId, now.toEpochMilli(), getCitizenAgingConfig());
    }

    public CitizenSummaryBundle readSummary(CitizenSummaryScope scope, Instant now) {
        return getCitizenSummaryCacheRefreshHandler().readSummary(scope, now);
    }

    public CitizenPopulationProjection projectPopulation(CitizenSummaryScope scope, Instant now) {
        return getCitizenProjectionHandler().projectPopulation(readSummary(scope, now));
    }

    public CitizenDisplayAnchorProjection projectAnchor(CitizenSummaryScope scope, CitizenAnchorType anchorType, Instant now) {
        return getCitizenProjectionHandler().projectAnchor(readSummary(scope, now), anchorType);
    }

    private ControlCommandResult createCitizensCommand(ControlCommand command, Instant startedAt) {
        int amount = command.commandType() == ControlCommandType.CREATE_CITIZEN ? 1 : Integer.parseInt(argumentOrDefault(command, "amount", "1"));
        UniversalPlayerId ownerPlayerId = playerId(command);
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        if (command.dryRun()) {
            return result(command, startedAt, false, "Citizens can be created: " + amount + ".", List.of(), Map.of("amount", Integer.toString(amount)), true);
        }
        List<CitizenData> created = getCitizenCreationHandler().createCitizens(ownerPlayerId, kingdomId, amount, startedAt.toEpochMilli(), getCitizenAgingConfig());
        return result(command, startedAt, true, "Citizens created: " + created.size() + ".", created.stream().map(citizen -> "citizen:" + citizen.citizenId()).toList(), Map.of("amount", Integer.toString(created.size())), true);
    }

    private ControlCommandResult listCitizensCommand(ControlCommand command, Instant startedAt) {
        List<CitizenData> citizens = scopedCitizens(command, startedAt);
        return result(command, startedAt, false, "Citizens listed: " + citizens.size() + ".", citizens.stream().map(citizen -> "citizen:" + citizen.citizenId()).toList(), Map.of("count", Integer.toString(citizens.size())), true);
    }

    private ControlCommandResult getCitizenCommand(ControlCommand command, Instant startedAt) {
        CitizenData citizen = getCitizenReadHandler().findCitizen(CitizenId.of(command.argument("citizenId")), startedAt.toEpochMilli(), getCitizenAgingConfig())
                .orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        return result(command, startedAt, false, "Citizen " + citizen.citizenId() + " " + citizen.ageStage() + " " + citizen.status() + " job=" + citizen.jobType() + ".", List.of("citizen:" + citizen.citizenId()), metadataFor(citizen), true);
    }

    private ControlCommandResult summaryCommand(ControlCommand command, Instant startedAt) {
        CitizenSummaryScope scope = summaryScope(command);
        CitizenSummaryBundle summary = getCitizenSummaryCacheRefreshHandler().readSummary(scope, startedAt);
        Map<String, String> metadata = Map.of(
                "activeCitizens", Integer.toString(summary.populationSummary().activeCitizens()),
                "activeTroops", Integer.toString(summary.populationSummary().activeTroops()),
                "inTraining", Integer.toString(summary.populationSummary().inTraining()),
                "wounded", Integer.toString(summary.populationSummary().wounded())
        );
        return result(command, startedAt, false, "Citizen summary: citizens=" + summary.populationSummary().activeCitizens() + " troops=" + summary.populationSummary().activeTroops() + ".", List.of("citizen-summary:" + scope.cacheKey()), metadata, true);
    }

    private ControlCommandResult assignJobCommand(ControlCommand command, Instant startedAt) {
        CitizenJobType jobType = CitizenJobType.valueOf(command.argument("jobType").toUpperCase());
        if (command.dryRun()) {
            CitizenData citizen = getCitizenRepository().findCitizen(CitizenId.of(command.argument("citizenId"))).orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
            getCitizenJobEligibilityHandler().requireEligible(citizen, jobType);
            return result(command, startedAt, false, "Citizen job can be assigned: " + jobType + ".", List.of("citizen:" + citizen.citizenId()), Map.of("jobType", jobType.name()), true);
        }
        CitizenData updated = getCitizenJobAssignmentHandler().assignJob(CitizenId.of(command.argument("citizenId")), jobType, startedAt.toEpochMilli());
        return result(command, startedAt, true, "Citizen job assigned: " + updated.jobType() + ".", List.of("citizen:" + updated.citizenId()), metadataFor(updated), true);
    }

    private ControlCommandResult clearJobCommand(ControlCommand command, Instant startedAt) {
        if (command.dryRun()) {
            return result(command, startedAt, false, "Citizen job can be cleared.", List.of("citizen:" + command.argument("citizenId")), Map.of(), true);
        }
        CitizenData updated = getCitizenJobAssignmentHandler().clearJob(CitizenId.of(command.argument("citizenId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Citizen job cleared.", List.of("citizen:" + updated.citizenId()), metadataFor(updated), true);
    }

    private ControlCommandResult startTrainingCommand(ControlCommand command, Instant startedAt) {
        if (command.dryRun()) {
            return result(command, startedAt, false, "Citizen training can start.", List.of("citizen:" + command.argument("citizenId")), Map.of(), true);
        }
        CitizenData updated = getCitizenTrainingStartHandler().startTraining(CitizenId.of(command.argument("citizenId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Citizen training started.", List.of("citizen:" + updated.citizenId()), metadataFor(updated), true);
    }

    private ControlCommandResult promoteCommand(ControlCommand command, Instant startedAt) {
        if (command.dryRun()) {
            return result(command, startedAt, false, "Citizen can be promoted if trained and eligible.", List.of("citizen:" + command.argument("citizenId")), Map.of(), true);
        }
        CitizenData updated = getCitizenTroopPromotionHandler().promoteToTroop(CitizenId.of(command.argument("citizenId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Citizen promoted to troop without changing citizenId.", List.of("citizen:" + updated.citizenId()), metadataFor(updated), true);
    }

    private ControlCommandResult demoteCommand(ControlCommand command, Instant startedAt) {
        if (command.dryRun()) {
            return result(command, startedAt, false, "Troop can be demoted if active.", List.of("citizen:" + command.argument("citizenId")), Map.of(), true);
        }
        CitizenData updated = getCitizenTroopDemotionHandler().demoteToCitizen(CitizenId.of(command.argument("citizenId")), startedAt.toEpochMilli());
        return result(command, startedAt, true, "Troop demoted to citizen without changing citizenId.", List.of("citizen:" + updated.citizenId()), metadataFor(updated), true);
    }

    private ControlCommandResult updateAgeStageCommand(ControlCommand command, Instant startedAt) {
        CitizenData citizen = getCitizenRepository().findCitizen(CitizenId.of(command.argument("citizenId"))).orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        CitizenAgeStage stage = CitizenAgeStage.valueOf(command.argument("ageStage").toUpperCase());
        if (command.dryRun()) {
            return result(command, startedAt, false, "Citizen age stage can be updated: " + stage + ".", List.of("citizen:" + citizen.citizenId()), Map.of("ageStage", stage.name()), true);
        }
        CitizenData updated = getCitizenRepository().saveCitizen(citizen.withAgeStage(stage, startedAt.toEpochMilli()));
        getCitizenCacheInvalidationHandler().invalidateCitizenScopes(updated);
        return result(command, startedAt, true, "Citizen age stage updated: " + stage + ".", List.of("citizen:" + updated.citizenId()), metadataFor(updated), true);
    }

    private ControlCommandResult debugSetAgeCommand(ControlCommand command, Instant startedAt) {
        CitizenData citizen = getCitizenRepository().findCitizen(CitizenId.of(command.argument("citizenId"))).orElseThrow(() -> new CitizenValidationException("Citizen was not found."));
        int years = Integer.parseInt(command.argument("years"));
        CitizenAgingConfig agingConfig = getCitizenAgingConfig();
        long bornAt = startedAt.toEpochMilli() - Math.round(years * (double) agingConfig.gameDaysPerYear() * agingConfig.realMillisPerGameDay());
        CitizenAgeStage stage = getCitizenAgingCalculationHandler().calculateAgeStage(citizen.withBornAtForDebug(bornAt, citizen.ageStage(), startedAt.toEpochMilli()), startedAt.toEpochMilli(), agingConfig);
        if (command.dryRun()) {
            return result(command, startedAt, false, "Citizen debug age can be set: " + years + " years.", List.of("citizen:" + citizen.citizenId()), Map.of("derivedStage", stage.name()), true);
        }
        CitizenData updated = getCitizenRepository().saveCitizen(citizen.withBornAtForDebug(bornAt, stage, startedAt.toEpochMilli()));
        return result(command, startedAt, true, "Citizen debug age set: " + years + " years stage=" + stage + ".", List.of("citizen:" + updated.citizenId()), metadataFor(updated), true);
    }

    private ControlCommandResult maintenanceCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        if (command.dryRun()) {
            return result(command, startedAt, false, "Citizen maintenance dry-run accepted for " + kingdomId + ".", List.of("kingdom:" + kingdomId), Map.of("kingdomId", kingdomId), true);
        }
        List<CitizenData> updated = getCitizenAgingMaintenanceHandler().runMaintenance(kingdomId, startedAt.toEpochMilli(), getCitizenAgingConfig());
        return result(command, startedAt, true, "Citizen maintenance updated stages: " + updated.size() + ".", updated.stream().map(citizen -> "citizen:" + citizen.citizenId()).toList(), Map.of("updated", Integer.toString(updated.size())), true);
    }

    private ControlCommandResult conditionCommand(ControlCommand command, Instant startedAt) {
        CitizenHealthState health = optionalEnum(command, "healthState", CitizenHealthState.class);
        CitizenMoraleState morale = optionalEnum(command, "moraleState", CitizenMoraleState.class);
        CitizenNutritionState nutrition = optionalEnum(command, "nutritionState", CitizenNutritionState.class);
        CitizenHousingState housing = optionalEnum(command, "housingState", CitizenHousingState.class);
        if (command.dryRun()) {
            return result(command, startedAt, false, "Citizen condition update dry-run accepted.", List.of("citizen:" + command.argument("citizenId")), Map.of(), true);
        }
        CitizenData updated = getCitizenConditionUpdateHandler().updateConditions(CitizenId.of(command.argument("citizenId")), health, morale, nutrition, housing, startedAt.toEpochMilli());
        return result(command, startedAt, true, "Citizen condition updated.", List.of("citizen:" + updated.citizenId()), metadataFor(updated), true);
    }

    private ControlCommandResult effectsCommand(ControlCommand command, Instant startedAt) {
        CitizenSummaryScope scope = summaryScope(command);
        CitizenSummaryBundle summary = getCitizenSummaryCacheRefreshHandler().refreshSummary(scope, startedAt);
        return result(command, startedAt, true, "Citizen condition effects evaluated for " + scope.cacheKey() + ".", List.of("citizen-summary:" + scope.cacheKey()), Map.of("effectiveProductivity", Double.toString(summary.productivitySummary().effectiveProductivity())), true);
    }

    private ControlCommandResult refreshSummaryCommand(ControlCommand command, Instant startedAt) {
        CitizenSummaryScope scope = summaryScope(command);
        CitizenSummaryBundle summary = getCitizenSummaryCacheRefreshHandler().refreshSummary(scope, startedAt);
        return result(command, startedAt, true, "Citizen summary cache refreshed.", List.of("citizen-summary:" + scope.cacheKey()), Map.of("activeCitizens", Integer.toString(summary.populationSummary().activeCitizens())), true);
    }

    private ControlCommandResult refreshDisplaysCommand(ControlCommand command, Instant startedAt) {
        CitizenSummaryScope scope = summaryScope(command);
        CitizenDisplayAnchorProjection citizens = projectAnchor(scope, CitizenAnchorType.CITIZEN_COUNT, startedAt);
        CitizenDisplayAnchorProjection troops = projectAnchor(scope, CitizenAnchorType.TROOP_COUNT, startedAt);
        return result(command, startedAt, false, "Citizen display projections refreshed: " + citizens.displayText() + ", " + troops.displayText() + ".", List.of(citizens.anchorId(), troops.anchorId()), Map.of("citizens", citizens.displayText(), "troops", troops.displayText()), true);
    }

    private List<CitizenData> scopedCitizens(ControlCommand command, Instant startedAt) {
        if (command.arguments().containsKey("ownerPlayerId")) {
            return getCitizenReadHandler().findCitizensForPlayer(playerId(command), startedAt.toEpochMilli(), getCitizenAgingConfig());
        }
        return getCitizenReadHandler().findCitizensForKingdom(argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID), startedAt.toEpochMilli(), getCitizenAgingConfig());
    }

    private CitizenSummaryScope summaryScope(ControlCommand command) {
        if (command.arguments().containsKey("ownerPlayerId")) {
            return CitizenSummaryScope.player(playerId(command));
        }
        return CitizenSummaryScope.kingdom(argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID));
    }

    private UniversalPlayerId playerId(ControlCommand command) {
        return UniversalPlayerId.of(UUID.fromString(command.argument("ownerPlayerId")));
    }

    private <E extends Enum<E>> E optionalEnum(ControlCommand command, String key, Class<E> enumClass) {
        String value = command.arguments().get(key);
        return value == null || value.isBlank() ? null : Enum.valueOf(enumClass, value.toUpperCase());
    }

    private Map<String, String> metadataFor(CitizenData citizen) {
        return Map.of(
                "citizenId", citizen.citizenId().toString(),
                "ownerPlayerId", citizen.ownerPlayerId().toString(),
                "kingdomId", citizen.kingdomId(),
                "ageStage", citizen.ageStage().name(),
                "status", citizen.status().name(),
                "jobType", citizen.jobType().name(),
                "trainingState", citizen.trainingState().name(),
                "troopLinkState", citizen.troopLinkState().name(),
                "canonicalOwner", "plain-java-control-server"
        );
    }

    private String argumentOrDefault(ControlCommand command, String key, String fallback) {
        String value = command.arguments().get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private ControlCommandResult result(ControlCommand command, Instant startedAt, boolean mutated, String message, List<String> changedObjectIds, Map<String, String> metadata, boolean success) {
        LinkedHashMap<String, String> resultMetadata = new LinkedHashMap<>(metadata);
        resultMetadata.put("mutatedCanonicalState", Boolean.toString(mutated && !command.dryRun()));
        resultMetadata.put("canonicalOwner", "plain-java-control-server");
        CommandExecutionState state = command.dryRun() ? CommandExecutionState.DRY_RUN_COMPLETED : (success ? CommandExecutionState.COMPLETED : CommandExecutionState.REJECTED);
        return new ControlCommandResult(command.commandId(), state, success, message, List.of(), changedObjectIds, success ? List.of() : List.of(message), startedAt, Instant.now(), resultMetadata);
    }
}
