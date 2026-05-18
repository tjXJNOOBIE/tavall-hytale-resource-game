package org.tavall.control.kingdom;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.common.MetadataMaps;
import org.tavall.control.runtime.ControlCommand;
import org.tavall.control.runtime.ControlCommandResult;
import org.tavall.control.runtime.ControlCommandType;
import org.tavall.control.runtime.ControlCommandValidationException;
import org.tavall.control.runtime.CommandExecutionState;
import org.tavall.control.event.DomainEventPublisher;
import org.tavall.control.event.SimpleDomainEvent;
import org.tavall.control.persistence.PostgresConnectionProvider;
import org.tavall.control.persistence.PostgresUniversalKingdomRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UniversalKingdomSimulationSystem implements IKingdomDomain {
    private static final double DEFAULT_BORDER_SIZE = 1000.0;
    private static final String DEFAULT_WORLD_ID = "default";

    public UniversalKingdomSimulationSystem() {
        initializeDefaults();
    }

    public UniversalKingdomSimulationSystem(UniversalKingdomRepository repository, DomainEventPublisher eventPublisher) {
        registerUniversalKingdomRepository(Objects.requireNonNull(repository, "repository"));
        registerDomainEventPublisher(Objects.requireNonNull(eventPublisher, "eventPublisher"));
        initializeDefaults();
    }

    public static UniversalKingdomSimulationSystem inMemory(DomainEventPublisher eventPublisher) {
        return new UniversalKingdomSimulationSystem(new InMemoryUniversalKingdomRepository(), eventPublisher);
    }

    public static UniversalKingdomSimulationSystem postgres(PostgresConnectionProvider connectionProvider, DomainEventPublisher eventPublisher) {
        return new UniversalKingdomSimulationSystem(new PostgresUniversalKingdomRepository(connectionProvider), eventPublisher);
    }

    public UniversalKingdomRepository repository() {
        return getUniversalKingdomRepository();
    }

    private KingdomFolderNamingPolicy folderNamingPolicy() {
        return new KingdomFolderNamingPolicy("kingdom");
    }

    private void initializeDefaults() {
        if (repository().editableParameterDefinitions().isEmpty()) {
            new EditableParameterDefinitionRegistryHandler(repository()).registerDefaults();
        }
        ensureDefaultCoordinateParameters();
    }

    public UniversalKingdom createKingdom(String displayName, String worldId, double borderSize, Instant now) {
        int kingdomNumber = repository().nextKingdomNumber();
        KingdomId kingdomId = new KingdomId(folderNamingPolicy().folderName(kingdomNumber));
        String folderName = folderNamingPolicy().folderName(kingdomNumber);
        KingdomStorageNamespace namespace = new KingdomStorageNamespace(
                kingdomId,
                folderName,
                "kingdoms/" + folderName,
                now,
                Map.of("namespacePolicy", "numeric-folder")
        );
        repository().saveNamespace(namespace);

        String borderDefinitionId = "border-" + kingdomId.value();
        String routingProfileId = "routing-" + kingdomId.value();
        UniversalKingdom kingdom = new UniversalKingdom(
                kingdomId,
                kingdomNumber,
                folderName,
                displayName == null || displayName.isBlank() ? "Kingdom " + kingdomNumber : displayName,
                KingdomState.PROTECTED,
                borderDefinitionId,
                worldId == null || worldId.isBlank() ? DEFAULT_WORLD_ID : worldId,
                routingProfileId,
                now,
                now,
                KingdomPopulationStats.defaults(),
                KingdomPowerStats.empty(),
                KingdomEditableParameters.defaults(),
                Map.of("storagePath", namespace.storagePath())
        );
        repository().saveKingdom(kingdom);
        createDefaultBorder(kingdom, borderSize <= 0 ? DEFAULT_BORDER_SIZE : borderSize, now);
        createDefaultRoutingProfiles(kingdom, now);
        publish("KingdomCreatedEvent", now, Map.of("kingdomId", kingdomId.value(), "folderName", folderName));
        return kingdom;
    }

    public KingdomBorderDefinition createDefaultBorder(UniversalKingdom kingdom, double borderSize, Instant now) {
        double minX = (kingdom.kingdomNumber() - 1) * borderSize;
        double maxX = minX + borderSize;
        KingdomBorderDefinition border = new KingdomBorderDefinition(
                kingdom.borderDefinitionId(),
                kingdom.kingdomId(),
                kingdom.worldId(),
                KingdomBorderShape.RECTANGLE,
                minX,
                maxX,
                null,
                null,
                0,
                borderSize,
                minX + borderSize / 2.0,
                null,
                borderSize / 2.0,
                null,
                List.of(),
                now,
                now,
                Map.of("defaultBorderSize", Double.toString(borderSize))
        );
        return updateBorder(border, true, now);
    }

    public KingdomBorderDefinition updateBorder(KingdomBorderDefinition border, boolean rejectOverlap, Instant now) {
        border.validate();
        if (rejectOverlap) {
            for (KingdomBorderDefinition existing : repository().kingdomBorders()) {
                if (!existing.borderDefinitionId().equals(border.borderDefinitionId())
                        && existing.worldId().equals(border.worldId())
                        && rectanglesOverlap(existing, border)) {
                    throw new ControlCommandValidationException("Kingdom border overlaps existing border: " + existing.borderDefinitionId() + ".");
                }
            }
        }
        KingdomBorderDefinition saved = new KingdomBorderDefinition(
                border.borderDefinitionId(),
                border.kingdomId(),
                border.worldId(),
                border.borderShape(),
                border.minX(),
                border.maxX(),
                border.minY(),
                border.maxY(),
                border.minZ(),
                border.maxZ(),
                border.centerX(),
                border.centerY(),
                border.centerZ(),
                border.radius(),
                border.neighboringKingdomIds(),
                border.createdAt(),
                now,
                border.metadata()
        );
        repository().saveBorder(saved);
        publish("KingdomBorderUpdatedEvent", now, Map.of("kingdomId", saved.kingdomId().value(), "borderDefinitionId", saved.borderDefinitionId()));
        return saved;
    }

    public Optional<KingdomId> resolveKingdom(CanonicalWorldCoordinate coordinate) {
        coordinate.validate();
        return repository().kingdomBorders().stream()
                .filter(border -> border.contains(coordinate))
                .sorted(Comparator.comparing(KingdomBorderDefinition::createdAt))
                .map(KingdomBorderDefinition::kingdomId)
                .findFirst();
    }

    public CoordinateConversionResult convertToCanonical(PlatformWorldCoordinate platformCoordinate) {
        platformCoordinate.validate();
        if (platformCoordinate.platform() == GamePlatform.DISCORD) {
            throw new ControlCommandValidationException("Discord has no physical world coordinate conversion.");
        }
        CoordinateConversionParameters parameters = repository().findCoordinateParameters(platformCoordinate.platform())
                .orElse(CoordinateConversionParameters.defaults(platformCoordinate.platform()));
        CanonicalWorldCoordinate canonical = parameters.toCanonical(platformCoordinate);
        List<String> warnings = new ArrayList<>();
        if (parameters.debugEnabled()) {
            warnings.add("debug conversion platform=" + platformCoordinate.platform() + " mapping=" + parameters.axisMapping());
        }
        return new CoordinateConversionResult(platformCoordinate.platform(), Optional.empty(), canonical, Optional.of(platformCoordinate), warnings, Map.of());
    }

    public CoordinateConversionResult convertToPlatform(CanonicalWorldCoordinate canonicalCoordinate, GamePlatform platform) {
        canonicalCoordinate.validate();
        if (platform == GamePlatform.DISCORD) {
            return new CoordinateConversionResult(platform, Optional.of(platform), canonicalCoordinate, Optional.empty(), List.of("Discord displays canonical summaries only."), Map.of());
        }
        CoordinateConversionParameters parameters = repository().findCoordinateParameters(platform)
                .orElse(CoordinateConversionParameters.defaults(platform));
        PlatformWorldCoordinate platformCoordinate = parameters.toPlatform(canonicalCoordinate);
        return new CoordinateConversionResult(platform, Optional.of(platform), canonicalCoordinate, Optional.of(platformCoordinate), parameters.debugEnabled() ? List.of("debug roundtrip platform=" + platform) : List.of(), Map.of());
    }

    public PlayerLocationUpdateResult updatePlayerLocation(String universalPlayerId, PlatformWorldCoordinate platformCoordinate, Optional<String> platformInstanceId, Instant now) {
        CoordinateConversionResult conversion = convertToCanonical(platformCoordinate);
        CanonicalWorldCoordinate canonical = conversion.canonicalCoordinate();
        Optional<KingdomId> resolvedKingdom = resolveKingdom(canonical);
        Optional<PlayerKingdomLocation> previousLocation = repository().findPlayerLocation(universalPlayerId);
        Optional<KingdomId> previousKingdom = previousLocation.map(PlayerKingdomLocation::currentKingdomId);
        KingdomId currentKingdom = resolvedKingdom.orElse(KingdomId.unclaimed());
        PlayerKingdomLocation location = new PlayerKingdomLocation(
                universalPlayerId,
                currentKingdom,
                previousKingdom,
                canonical.withKingdomId(currentKingdom.value()),
                platformCoordinate.platform(),
                platformInstanceId,
                now,
                previousKingdom.filter(previous -> !previous.equals(currentKingdom)).map(previous -> now).or(() -> previousLocation.flatMap(PlayerKingdomLocation::lastBorderCrossingAt)),
                Map.of("platformWorldId", platformCoordinate.platformWorldId())
        );
        repository().savePlayerLocation(location);
        publish("PlayerKingdomLocationUpdatedEvent", now, Map.of("universalPlayerId", universalPlayerId, "kingdomId", currentKingdom.value()));

        Optional<PlayerKingdomTransition> transition = Optional.empty();
        Optional<InstanceSwitchRequest> switchRequest = Optional.empty();
        if (resolvedKingdom.isPresent() && previousKingdom.isPresent() && !previousKingdom.get().equals(currentKingdom)) {
            transition = Optional.of(createTransition(universalPlayerId, previousKingdom.get(), currentKingdom, platformCoordinate.platform(), canonical, platformInstanceId, now));
            switchRequest = Optional.of(requestInstanceSwitch(universalPlayerId, platformCoordinate.platform(), previousKingdom.get(), currentKingdom, InstanceSwitchReason.KINGDOM_BORDER_CROSSED, now));
        }
        return new PlayerLocationUpdateResult(location, transition, switchRequest, conversion);
    }

    public PlayerKingdomTransition createTransition(String universalPlayerId, KingdomId fromKingdomId, KingdomId toKingdomId, GamePlatform platform, CanonicalWorldCoordinate crossingCoordinate, Optional<String> fromInstanceId, Instant now) {
        PlayerKingdomTransition transition = new PlayerKingdomTransition(
                UUID.randomUUID().toString(),
                universalPlayerId,
                fromKingdomId,
                toKingdomId,
                fromInstanceId,
                Optional.empty(),
                platform,
                crossingCoordinate,
                KingdomTransitionState.DETECTED,
                now,
                Optional.empty(),
                Map.of()
        );
        repository().saveTransition(transition);
        publish("PlayerKingdomTransitionCreatedEvent", now, Map.of("transitionId", transition.transitionId(), "toKingdomId", toKingdomId.value()));
        return transition;
    }

    public InstanceSwitchRequest requestInstanceSwitch(String universalPlayerId, GamePlatform platform, KingdomId fromKingdomId, KingdomId toKingdomId, InstanceSwitchReason reason, Instant now) {
        PlatformInstance selected = new PlatformInstanceSelectionHandler().selectInstance(toKingdomId, platform)
                .orElseThrow(() -> new ControlCommandValidationException("No healthy target instance exists for " + toKingdomId.value() + " on " + platform + "."));
        InstanceSwitchRequest request = new InstanceSwitchRequest(
                UUID.randomUUID().toString(),
                universalPlayerId,
                platform,
                fromKingdomId,
                toKingdomId,
                Optional.empty(),
                selected.platformInstanceId(),
                reason,
                InstanceSwitchState.REQUESTED,
                now,
                Optional.empty(),
                Map.of("backendOwned", "true")
        );
        repository().saveSwitchRequest(request);
        repository().findLatestTransition(universalPlayerId)
                .filter(transition -> transition.toKingdomId().equals(toKingdomId))
                .ifPresent(transition -> repository().saveTransition(transition.withState(KingdomTransitionState.ROUTING_REQUESTED, Optional.of(selected.platformInstanceId()), Optional.empty(), now)));
        publish("InstanceSwitchRequestedEvent", now, Map.of("switchRequestId", request.switchRequestId(), "platform", platform.name(), "toInstanceId", selected.platformInstanceId()));
        return request;
    }

    public InstanceSwitchRequest confirmInstanceSwitch(String switchRequestId, Instant now) {
        InstanceSwitchRequest request = repository().findSwitchRequest(switchRequestId)
                .orElseThrow(() -> new ControlCommandValidationException("Switch request was not found."));
        InstanceSwitchRequest completed = request.withState(InstanceSwitchState.CONFIRMED, now, Map.of("confirmed", "true"));
        repository().saveSwitchRequest(completed);
        repository().findLatestTransition(request.universalPlayerId())
                .filter(transition -> transition.toKingdomId().equals(request.toKingdomId()))
                .ifPresent(transition -> repository().saveTransition(transition.withState(KingdomTransitionState.SWITCH_CONFIRMED, Optional.of(request.toInstanceId()), Optional.of(now), now)));
        publish("InstanceSwitchConfirmedEvent", now, Map.of("switchRequestId", switchRequestId));
        return completed;
    }

    public InstanceSwitchRequest failInstanceSwitch(String switchRequestId, String reason, Instant now) {
        InstanceSwitchRequest request = repository().findSwitchRequest(switchRequestId)
                .orElseThrow(() -> new ControlCommandValidationException("Switch request was not found."));
        InstanceSwitchRequest failed = request.withState(InstanceSwitchState.FAILED, now, Map.of("failureReason", reason == null ? "unknown" : reason));
        repository().saveSwitchRequest(failed);
        repository().findLatestTransition(request.universalPlayerId())
                .filter(transition -> transition.toKingdomId().equals(request.toKingdomId()))
                .ifPresent(transition -> repository().saveTransition(transition.withState(KingdomTransitionState.FAILED, Optional.of(request.toInstanceId()), Optional.of(now), now)));
        publish("InstanceSwitchFailedEvent", now, Map.of("switchRequestId", switchRequestId, "reason", reason == null ? "unknown" : reason));
        return failed;
    }

    public KingdomInstanceRoutingProfile registerPlatformInstance(GamePlatform platform, KingdomId kingdomId, String instanceId, String instanceName, PlatformInstanceState state, Instant now) {
        PlatformInstance instance = new PlatformInstance(
                instanceId,
                platform,
                kingdomId,
                Optional.empty(),
                Optional.empty(),
                instanceName == null || instanceName.isBlank() ? instanceId : instanceName,
                state == null ? PlatformInstanceState.ONLINE : state,
                0,
                250,
                Optional.of(now),
                Map.of()
        );
        repository().savePlatformInstance(instance);
        KingdomInstanceRoutingProfile profile = repository().findRoutingProfile(kingdomId, platform)
                .orElse(new KingdomInstanceRoutingProfile("routing-" + kingdomId.value() + "-" + platform.name().toLowerCase(), kingdomId, platform, instanceId, List.of(), RoutingState.ENABLED, 250, 0, instance.healthy(), Map.of()));
        KingdomInstanceRoutingProfile saved = new KingdomInstanceRoutingProfile(
                profile.instanceRoutingProfileId(),
                kingdomId,
                platform,
                instanceId,
                profile.fallbackInstanceIds(),
                profile.routingState(),
                profile.maxPlayers(),
                profile.currentPlayers(),
                instance.healthy(),
                profile.metadata()
        );
        repository().saveRoutingProfile(saved);
        publish("PlatformInstanceRegisteredEvent", now, Map.of("platformInstanceId", instanceId, "kingdomId", kingdomId.value()));
        return saved;
    }

    public PlatformInstance updatePlatformInstanceHealth(String instanceId, PlatformInstanceState state, boolean healthy, Instant now) {
        PlatformInstance existing = repository().findPlatformInstance(instanceId)
                .orElseThrow(() -> new ControlCommandValidationException("Platform instance was not found."));
        PlatformInstance updated = existing.withState(state, healthy, now);
        repository().savePlatformInstance(updated);
        repository().findRoutingProfile(existing.kingdomId(), existing.platform()).ifPresent(profile -> repository().saveRoutingProfile(profile.withHealth(healthy, updated.currentPlayers())));
        publish("PlatformInstanceHealthUpdatedEvent", now, Map.of("platformInstanceId", instanceId, "state", state.name()));
        return updated;
    }

    public KingdomScalingEvaluation evaluateScaling(Instant now) {
        List<String> recommendedKingdomIds = new ArrayList<>();
        for (UniversalKingdom kingdom : repository().kingdoms()) {
            boolean full = kingdom.populationStats().currentPlayers() >= kingdom.editableParameters().maxActivePlayers()
                    || kingdom.populationStats().registeredPlayers() >= kingdom.editableParameters().maxRegisteredPlayers();
            boolean overpowered = kingdom.powerStats().topGuildPower() > kingdom.editableParameters().maxTopGuildPower()
                    || kingdom.powerStats().averagePlayerPower() > kingdom.editableParameters().maxAveragePlayerPower();
            if ((full || overpowered) && kingdom.editableParameters().newKingdomCreationEnabled()) {
                recommendedKingdomIds.add(kingdom.kingdomId().value());
                publish(full ? "KingdomBecameFullEvent" : "KingdomBecameOverpoweredEvent", now, Map.of("kingdomId", kingdom.kingdomId().value()));
            }
        }
        KingdomScalingEvaluation evaluation = new KingdomScalingEvaluation(recommendedKingdomIds, !recommendedKingdomIds.isEmpty(), now, Map.of("policy", "population-power"));
        publish("KingdomScalingEvaluatedEvent", now, Map.of("recommendedCount", Integer.toString(recommendedKingdomIds.size())));
        return evaluation;
    }

    public Optional<UniversalKingdom> evaluateNewPlayerKingdom() {
        return repository().kingdoms().stream()
                .filter(kingdom -> kingdom.state() == KingdomState.OPEN || kingdom.state() == KingdomState.PROTECTED)
                .filter(kingdom -> kingdom.populationStats().newPlayerAllowed())
                .filter(kingdom -> kingdom.populationStats().currentPlayers() < kingdom.editableParameters().maxActivePlayers())
                .filter(kingdom -> kingdom.powerStats().topGuildPower() <= kingdom.editableParameters().maxTopGuildPower())
                .min(Comparator.comparingDouble(kingdom -> kingdom.powerStats().averagePlayerPower() + kingdom.populationStats().currentPlayers()));
    }

    public EditableParameterValue updateEditableParameter(String scopeType, Optional<String> scopeId, String key, String value, String updatedBy, boolean dryRun, Instant now) {
        EditableParameterDefinition definition = repository().findEditableParameterDefinition(key)
                .orElseThrow(() -> new ControlCommandValidationException("Editable parameter definition was not found: " + key + "."));
        new EditableParameterValidationHandler().validate(definition, value);
        EditableParameterValue parameterValue = new EditableParameterValue(
                key,
                EditableParameterScopeType.valueOf(scopeType.toUpperCase()),
                scopeId,
                value,
                updatedBy == null || updatedBy.isBlank() ? "system" : updatedBy,
                now,
                Map.of("dryRun", Boolean.toString(dryRun))
        );
        if (!dryRun) {
            repository().saveEditableParameterValue(parameterValue);
            applyParameterToCanonicalState(parameterValue, now);
            publish("EditableParameterUpdatedEvent", now, Map.of("parameterKey", key, "scopeType", parameterValue.scopeType().name()));
        }
        return parameterValue;
    }

    public ControlCommandResult handleControlCommand(ControlCommand command, Instant startedAt) {
        return switch (command.commandType()) {
            case CREATE_KINGDOM -> handleCreateKingdom(command, startedAt);
            case ARCHIVE_KINGDOM -> handleArchiveKingdom(command, startedAt);
            case DEBUG_KINGDOM_STATE -> handleDebugKingdom(command, startedAt);
            case EVALUATE_KINGDOM_SCALING -> handleEvaluateScaling(command, startedAt);
            case RUN_KINGDOM_SIMULATION_TICK -> handleKingdomTick(command, startedAt);
            case CREATE_KINGDOM_BORDER, UPDATE_KINGDOM_BORDER -> handleUpdateBorder(command, startedAt);
            case RESOLVE_COORDINATE_KINGDOM, DEBUG_KINGDOM_BORDER -> handleResolveCoordinate(command, startedAt);
            case SIMULATE_BORDER_CROSSING -> handleSimulateBorderCrossing(command, startedAt);
            case CONVERT_PLATFORM_COORDINATE, DEBUG_COORDINATE_CONVERSION -> handleConvertCoordinate(command, startedAt);
            case UPDATE_COORDINATE_CONVERSION_PARAMETERS -> handleCoordinateParameterUpdate(command, startedAt);
            case UPDATE_PLAYER_LOCATION -> handleUpdatePlayerLocation(command, startedAt);
            case DEBUG_PLAYER_KINGDOM_LOCATION -> handleDebugPlayerLocation(command, startedAt);
            case FORCE_PLAYER_KINGDOM_TRANSITION -> handleForceTransition(command, startedAt);
            case REGISTER_PLATFORM_INSTANCE -> handleRegisterInstance(command, startedAt);
            case UPDATE_PLATFORM_INSTANCE_HEALTH -> handleInstanceHealth(command, startedAt);
            case REQUEST_INSTANCE_SWITCH -> handleRequestSwitch(command, startedAt);
            case CONFIRM_INSTANCE_SWITCH -> handleConfirmSwitch(command, startedAt);
            case FAIL_INSTANCE_SWITCH -> handleFailSwitch(command, startedAt);
            case DEBUG_INSTANCE_ROUTING, UPDATE_KINGDOM_INSTANCE_ROUTING -> handleDebugInstanceRouting(command, startedAt);
            case LIST_EDITABLE_PARAMETERS -> handleListParameters(command, startedAt);
            case GET_EDITABLE_PARAMETER -> handleGetParameter(command, startedAt);
            case UPDATE_EDITABLE_PARAMETER, DRY_RUN_EDITABLE_PARAMETER_UPDATE -> handleUpdateParameter(command, startedAt);
            case EVALUATE_NEW_PLAYER_KINGDOM, ASSIGN_NEW_PLAYER_KINGDOM -> handleEvaluateNewPlayer(command, startedAt);
            default -> throw new ControlCommandValidationException("Not a universal kingdom simulation command: " + command.commandType() + ".");
        };
    }

    public List<KingdomProjection> kingdomProjections(GamePlatform platform) {
        return repository().kingdoms().stream()
                .map(kingdom -> new KingdomProjection(
                        kingdom.kingdomId().value(),
                        "KINGDOM",
                        kingdom.state().name(),
                        globalAssetForKingdom(kingdom),
                        kingdom.displayName(),
                        platform,
                        List.of("DEBUG_KINGDOM_STATE", "UPDATE_EDITABLE_PARAMETER", "RESOLVE_COORDINATE_KINGDOM"),
                        List.of(),
                        Map.of(
                                "folderName", kingdom.folderName(),
                                "worldId", kingdom.worldId(),
                                "currentPlayers", Integer.toString(kingdom.populationStats().currentPlayers())
                        )
                ))
                .toList();
    }

    public List<KingdomTransitionProjection> transitionProjections(GamePlatform platform) {
        return repository().transitions().stream()
                .map(transition -> new KingdomTransitionProjection(
                        transition.transitionId(),
                        "PLAYER_KINGDOM_TRANSITION",
                        transition.transitionState().name(),
                        "kingdom.transition.border_crossing",
                        transition.universalPlayerId(),
                        platform,
                        List.of("CONFIRM_INSTANCE_SWITCH", "FAIL_INSTANCE_SWITCH"),
                        List.of(),
                        Map.of(
                                "fromKingdomId", transition.fromKingdomId().value(),
                                "toKingdomId", transition.toKingdomId().value(),
                                "coordinate", transition.crossingCoordinate().compact()
                        )
                ))
                .toList();
    }

    private ControlCommandResult handleCreateKingdom(ControlCommand command, Instant startedAt) {
        String displayName = command.arguments().getOrDefault("displayName", "");
        String worldId = command.arguments().getOrDefault("worldId", DEFAULT_WORLD_ID);
        double borderSize = parseDouble(command.arguments().getOrDefault("borderSize", Double.toString(DEFAULT_BORDER_SIZE)), "borderSize");
        if (command.dryRun()) {
            int next = repository().nextKingdomNumber();
            return dryRun(command, startedAt, "Kingdom can be created: kingdom-" + next + ".", List.of("kingdom:kingdom-" + next));
        }
        UniversalKingdom kingdom = createKingdom(displayName, worldId, borderSize, startedAt);
        return completed(command, startedAt, "Kingdom created: " + kingdom.kingdomId().value() + " folder=" + kingdom.folderName() + ".", List.of("kingdom:" + kingdom.kingdomId().value(), "border:" + kingdom.borderDefinitionId()));
    }

    private ControlCommandResult handleArchiveKingdom(ControlCommand command, Instant startedAt) {
        KingdomId kingdomId = KingdomId.of(command.argument("kingdomId"));
        UniversalKingdom kingdom = repository().findKingdom(kingdomId)
                .orElseThrow(() -> new ControlCommandValidationException("Kingdom was not found."));
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Kingdom can be archived: " + kingdomId.value() + ".", List.of("kingdom:" + kingdomId.value()));
        }
        UniversalKingdom archived = new UniversalKingdom(
                kingdom.kingdomId(),
                kingdom.kingdomNumber(),
                kingdom.folderName(),
                kingdom.displayName(),
                KingdomState.ARCHIVED,
                kingdom.borderDefinitionId(),
                kingdom.worldId(),
                kingdom.instanceRoutingProfileId(),
                kingdom.createdAt(),
                startedAt,
                kingdom.populationStats(),
                kingdom.powerStats(),
                kingdom.editableParameters(),
                kingdom.metadata()
        );
        repository().saveKingdom(archived);
        publish("KingdomArchivedEvent", startedAt, Map.of("kingdomId", kingdomId.value()));
        return completed(command, startedAt, "Kingdom archived: " + kingdomId.value() + ".", List.of("kingdom:" + kingdomId.value()));
    }

    private ControlCommandResult handleDebugKingdom(ControlCommand command, Instant startedAt) {
        KingdomId kingdomId = KingdomId.of(command.argument("kingdomId"));
        UniversalKingdom kingdom = repository().findKingdom(kingdomId)
                .orElseThrow(() -> new ControlCommandValidationException("Kingdom was not found."));
        return informational(command, startedAt, "kingdom=" + kingdom.kingdomId().value() + " state=" + kingdom.state() + " folder=" + kingdom.folderName() + " world=" + kingdom.worldId(), List.of("kingdom:" + kingdom.kingdomId().value()));
    }

    private ControlCommandResult handleEvaluateScaling(ControlCommand command, Instant startedAt) {
        KingdomScalingEvaluation evaluation = evaluateScaling(startedAt);
        return informational(command, startedAt, "scaling recommended=" + evaluation.newKingdomRecommended() + " sources=" + String.join(",", evaluation.sourceKingdomIds()), evaluation.sourceKingdomIds().stream().map(id -> "kingdom:" + id).toList());
    }

    private ControlCommandResult handleKingdomTick(ControlCommand command, Instant startedAt) {
        KingdomScalingEvaluation evaluation = evaluateScaling(startedAt);
        return informational(command, startedAt, "kingdom tick completed scalingRecommended=" + evaluation.newKingdomRecommended(), List.of());
    }

    private ControlCommandResult handleUpdateBorder(ControlCommand command, Instant startedAt) {
        KingdomId kingdomId = KingdomId.of(command.argument("kingdomId"));
        UniversalKingdom kingdom = repository().findKingdom(kingdomId)
                .orElseThrow(() -> new ControlCommandValidationException("Kingdom was not found."));
        Optional<KingdomBorderDefinition> existingBorder = repository().findBorderForKingdom(kingdomId);
        if (command.commandType() == ControlCommandType.UPDATE_KINGDOM_BORDER && existingBorder.isEmpty()) {
            throw new ControlCommandValidationException("Kingdom border was not found.");
        }
        KingdomBorderDefinition existing = existingBorder.orElse(new KingdomBorderDefinition(
                kingdom.borderDefinitionId(),
                kingdomId,
                kingdom.worldId(),
                KingdomBorderShape.RECTANGLE,
                0,
                DEFAULT_BORDER_SIZE,
                Optional.empty(),
                Optional.empty(),
                0,
                DEFAULT_BORDER_SIZE,
                DEFAULT_BORDER_SIZE / 2.0,
                Optional.empty(),
                DEFAULT_BORDER_SIZE / 2.0,
                Optional.empty(),
                List.of(),
                startedAt,
                startedAt,
                Map.of("createdByCommand", "true")
        ));
        KingdomBorderDefinition updated = new KingdomBorderDefinition(
                existing.borderDefinitionId(),
                kingdomId,
                command.arguments().getOrDefault("worldId", kingdom.worldId()),
                KingdomBorderShape.RECTANGLE,
                parseDouble(command.arguments().getOrDefault("minX", Double.toString(existing.minX())), "minX"),
                parseDouble(command.arguments().getOrDefault("maxX", Double.toString(existing.maxX())), "maxX"),
                optionalDouble(command.arguments().get("minY")),
                optionalDouble(command.arguments().get("maxY")),
                parseDouble(command.arguments().getOrDefault("minZ", Double.toString(existing.minZ())), "minZ"),
                parseDouble(command.arguments().getOrDefault("maxZ", Double.toString(existing.maxZ())), "maxZ"),
                parseDouble(command.arguments().getOrDefault("centerX", Double.toString(existing.centerX())), "centerX"),
                optionalDouble(command.arguments().get("centerY")),
                parseDouble(command.arguments().getOrDefault("centerZ", Double.toString(existing.centerZ())), "centerZ"),
                optionalDouble(command.arguments().get("radius")),
                existing.neighboringKingdomIds(),
                existing.createdAt(),
                startedAt,
                existing.metadata()
        );
        if (command.dryRun()) {
            updated.validate();
            return dryRun(command, startedAt, "Kingdom border can be updated: " + updated.borderDefinitionId() + ".", List.of("border:" + updated.borderDefinitionId()));
        }
        KingdomBorderDefinition saved = updateBorder(updated, true, startedAt);
        return completed(command, startedAt, "Kingdom border updated: " + saved.borderDefinitionId() + ".", List.of("border:" + saved.borderDefinitionId()));
    }

    private ControlCommandResult handleResolveCoordinate(ControlCommand command, Instant startedAt) {
        CanonicalWorldCoordinate coordinate = canonicalFromCommand(command);
        Optional<KingdomId> kingdomId = resolveKingdom(coordinate);
        return informational(command, startedAt, "coordinate=" + coordinate.compact() + " kingdom=" + kingdomId.map(KingdomId::value).orElse("unclaimed"), kingdomId.map(id -> List.of("kingdom:" + id.value())).orElse(List.of()));
    }

    private ControlCommandResult handleSimulateBorderCrossing(ControlCommand command, Instant startedAt) {
        CanonicalWorldCoordinate from = new CanonicalWorldCoordinate(command.argument("worldId"), parseDouble(command.argument("fromX"), "fromX"), parseDouble(command.arguments().getOrDefault("fromY", "0"), "fromY"), parseDouble(command.argument("fromZ"), "fromZ"), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Map.of());
        CanonicalWorldCoordinate to = new CanonicalWorldCoordinate(command.argument("worldId"), parseDouble(command.argument("toX"), "toX"), parseDouble(command.arguments().getOrDefault("toY", "0"), "toY"), parseDouble(command.argument("toZ"), "toZ"), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Map.of());
        Optional<KingdomId> fromKingdom = resolveKingdom(from);
        Optional<KingdomId> toKingdom = resolveKingdom(to);
        boolean crossed = fromKingdom.isPresent() && toKingdom.isPresent() && !fromKingdom.equals(toKingdom);
        return informational(command, startedAt, "crossed=" + crossed + " from=" + fromKingdom.map(KingdomId::value).orElse("unclaimed") + " to=" + toKingdom.map(KingdomId::value).orElse("unclaimed"), crossed ? List.of("kingdom:" + fromKingdom.get().value(), "kingdom:" + toKingdom.get().value()) : List.of());
    }

    private ControlCommandResult handleConvertCoordinate(ControlCommand command, Instant startedAt) {
        GamePlatform platform = GamePlatform.valueOf(command.argument("platform").toUpperCase());
        PlatformWorldCoordinate platformCoordinate = new PlatformWorldCoordinate(platform, command.argument("worldId"), parseDouble(command.argument("x"), "x"), parseDouble(command.arguments().getOrDefault("y", "0"), "y"), parseDouble(command.argument("z"), "z"), optionalDouble(command.arguments().get("yaw")), optionalDouble(command.arguments().get("pitch")), Map.of());
        CoordinateConversionResult result = convertToCanonical(platformCoordinate);
        return informational(command, startedAt, "canonical=" + result.canonicalCoordinate().compact() + " warnings=" + result.conversionWarnings().size(), List.of("coordinate:" + platform.name().toLowerCase()));
    }

    private ControlCommandResult handleCoordinateParameterUpdate(ControlCommand command, Instant startedAt) {
        GamePlatform platform = GamePlatform.valueOf(command.argument("platform").toUpperCase());
        CoordinateConversionParameters existing = repository().findCoordinateParameters(platform).orElse(CoordinateConversionParameters.defaults(platform));
        CoordinateConversionParameters updated = existing.withCommandArguments(command.arguments());
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Coordinate conversion parameters can be updated for " + platform + ".", List.of("coordinate-parameters:" + platform));
        }
        repository().saveCoordinateParameters(updated);
        publish("CoordinateConversionParameterUpdatedEvent", startedAt, Map.of("platform", platform.name()));
        return completed(command, startedAt, "Coordinate conversion parameters updated for " + platform + ".", List.of("coordinate-parameters:" + platform));
    }

    private ControlCommandResult handleUpdatePlayerLocation(ControlCommand command, Instant startedAt) {
        GamePlatform platform = GamePlatform.valueOf(command.argument("platform").toUpperCase());
        PlatformWorldCoordinate coordinate = new PlatformWorldCoordinate(platform, command.argument("worldId"), parseDouble(command.argument("x"), "x"), parseDouble(command.arguments().getOrDefault("y", "0"), "y"), parseDouble(command.argument("z"), "z"), optionalDouble(command.arguments().get("yaw")), optionalDouble(command.arguments().get("pitch")), Map.of());
        if (command.dryRun()) {
            CoordinateConversionResult conversion = convertToCanonical(coordinate);
            return dryRun(command, startedAt, "Player location can resolve kingdom=" + resolveKingdom(conversion.canonicalCoordinate()).map(KingdomId::value).orElse("unclaimed") + ".", List.of("player:" + command.argument("universalPlayerId")));
        }
        PlayerLocationUpdateResult result = updatePlayerLocation(command.argument("universalPlayerId"), coordinate, Optional.ofNullable(command.arguments().get("platformInstanceId")), startedAt);
        ArrayList<String> changed = new ArrayList<>(List.of("player:" + result.location().universalPlayerId(), "kingdom:" + result.location().currentKingdomId().value()));
        result.transition().ifPresent(transition -> changed.add("transition:" + transition.transitionId()));
        result.switchRequest().ifPresent(request -> {
            changed.add("instance-switch:" + request.switchRequestId());
            changed.add("platform-instance:" + request.toInstanceId());
        });
        return completed(command, startedAt, "player location updated kingdom=" + result.location().currentKingdomId().value() + " transition=" + result.transition().isPresent(), changed);
    }

    private ControlCommandResult handleDebugPlayerLocation(ControlCommand command, Instant startedAt) {
        PlayerKingdomLocation location = repository().findPlayerLocation(command.argument("universalPlayerId"))
                .orElseThrow(() -> new ControlCommandValidationException("Player kingdom location was not found."));
        return informational(command, startedAt, "player=" + location.universalPlayerId() + " kingdom=" + location.currentKingdomId().value() + " coordinate=" + location.canonicalCoordinate().compact(), List.of("player:" + location.universalPlayerId()));
    }

    private ControlCommandResult handleForceTransition(ControlCommand command, Instant startedAt) {
        KingdomId fromKingdomId = KingdomId.of(command.argument("fromKingdomId"));
        KingdomId toKingdomId = KingdomId.of(command.argument("toKingdomId"));
        GamePlatform platform = GamePlatform.valueOf(command.arguments().getOrDefault("platform", "MINECRAFT").toUpperCase());
        CanonicalWorldCoordinate coordinate = canonicalFromCommand(command);
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Player transition can be forced to " + toKingdomId.value() + ".", List.of("player:" + command.argument("universalPlayerId")));
        }
        PlayerKingdomTransition transition = createTransition(command.argument("universalPlayerId"), fromKingdomId, toKingdomId, platform, coordinate, Optional.empty(), startedAt);
        InstanceSwitchRequest request = requestInstanceSwitch(command.argument("universalPlayerId"), platform, fromKingdomId, toKingdomId, InstanceSwitchReason.CONTROL_COMMAND, startedAt);
        return completed(command, startedAt, "forced transition created: " + transition.transitionId() + ".", List.of("transition:" + transition.transitionId(), "instance-switch:" + request.switchRequestId(), "platform-instance:" + request.toInstanceId()));
    }

    private ControlCommandResult handleRegisterInstance(ControlCommand command, Instant startedAt) {
        GamePlatform platform = GamePlatform.valueOf(command.argument("platform").toUpperCase());
        KingdomId kingdomId = KingdomId.of(command.argument("kingdomId"));
        String instanceId = command.argument("platformInstanceId");
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Platform instance can be registered: " + instanceId + ".", List.of("platform-instance:" + instanceId));
        }
        KingdomInstanceRoutingProfile profile = registerPlatformInstance(platform, kingdomId, instanceId, command.arguments().getOrDefault("instanceName", instanceId), PlatformInstanceState.ONLINE, startedAt);
        return completed(command, startedAt, "instance registered profile=" + profile.instanceRoutingProfileId() + ".", List.of("platform-instance:" + instanceId, "routing-profile:" + profile.instanceRoutingProfileId()));
    }

    private ControlCommandResult handleInstanceHealth(ControlCommand command, Instant startedAt) {
        PlatformInstanceState state = PlatformInstanceState.valueOf(command.argument("state").toUpperCase());
        boolean healthy = Boolean.parseBoolean(command.arguments().getOrDefault("healthy", Boolean.toString(state == PlatformInstanceState.ONLINE)));
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Instance health can be updated.", List.of("platform-instance:" + command.argument("platformInstanceId")));
        }
        PlatformInstance instance = updatePlatformInstanceHealth(command.argument("platformInstanceId"), state, healthy, startedAt);
        return completed(command, startedAt, "instance health updated state=" + instance.state() + " healthy=" + instance.healthy() + ".", List.of("platform-instance:" + instance.platformInstanceId()));
    }

    private ControlCommandResult handleRequestSwitch(ControlCommand command, Instant startedAt) {
        GamePlatform platform = GamePlatform.valueOf(command.argument("platform").toUpperCase());
        KingdomId from = KingdomId.of(command.argument("fromKingdomId"));
        KingdomId to = KingdomId.of(command.argument("toKingdomId"));
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Instance switch can be requested to " + to.value() + ".", List.of("player:" + command.argument("universalPlayerId")));
        }
        InstanceSwitchRequest request = requestInstanceSwitch(command.argument("universalPlayerId"), platform, from, to, InstanceSwitchReason.CONTROL_COMMAND, startedAt);
        return completed(command, startedAt, "instance switch requested: " + request.switchRequestId() + ".", List.of("instance-switch:" + request.switchRequestId(), "platform-instance:" + request.toInstanceId()));
    }

    private ControlCommandResult handleConfirmSwitch(ControlCommand command, Instant startedAt) {
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Instance switch confirmation can be applied.", List.of("instance-switch:" + command.argument("switchRequestId")));
        }
        InstanceSwitchRequest request = confirmInstanceSwitch(command.argument("switchRequestId"), startedAt);
        return completed(command, startedAt, "instance switch confirmed: " + request.switchRequestId() + ".", List.of("instance-switch:" + request.switchRequestId()));
    }

    private ControlCommandResult handleFailSwitch(ControlCommand command, Instant startedAt) {
        if (command.dryRun()) {
            return dryRun(command, startedAt, "Instance switch failure can be recorded.", List.of("instance-switch:" + command.argument("switchRequestId")));
        }
        InstanceSwitchRequest request = failInstanceSwitch(command.argument("switchRequestId"), command.arguments().getOrDefault("reason", "unknown"), startedAt);
        return completed(command, startedAt, "instance switch failed: " + request.switchRequestId() + ".", List.of("instance-switch:" + request.switchRequestId()));
    }

    private ControlCommandResult handleDebugInstanceRouting(ControlCommand command, Instant startedAt) {
        KingdomId kingdomId = KingdomId.of(command.argument("kingdomId"));
        GamePlatform platform = GamePlatform.valueOf(command.argument("platform").toUpperCase());
        Optional<KingdomInstanceRoutingProfile> profile = repository().findRoutingProfile(kingdomId, platform);
        return informational(command, startedAt, "routing kingdom=" + kingdomId.value() + " platform=" + platform + " profile=" + profile.map(KingdomInstanceRoutingProfile::instanceRoutingProfileId).orElse("none"), profile.map(item -> List.of("routing-profile:" + item.instanceRoutingProfileId())).orElse(List.of()));
    }

    private ControlCommandResult handleListParameters(ControlCommand command, Instant startedAt) {
        return informational(command, startedAt, "editable parameters=" + repository().editableParameterDefinitions().size(), repository().editableParameterDefinitions().stream().map(definition -> "parameter:" + definition.parameterKey()).toList());
    }

    private ControlCommandResult handleGetParameter(ControlCommand command, Instant startedAt) {
        String key = command.argument("parameterKey");
        EditableParameterDefinition definition = repository().findEditableParameterDefinition(key)
                .orElseThrow(() -> new ControlCommandValidationException("Editable parameter definition was not found."));
        Optional<EditableParameterValue> value = repository().findEditableParameterValue(key, Optional.ofNullable(command.arguments().get("scopeId")));
        return informational(command, startedAt, "parameter=" + key + " type=" + definition.valueType() + " value=" + value.map(EditableParameterValue::value).orElse(definition.defaultValue()), List.of("parameter:" + key));
    }

    private ControlCommandResult handleUpdateParameter(ControlCommand command, Instant startedAt) {
        boolean dryRun = command.dryRun() || command.commandType() == ControlCommandType.DRY_RUN_EDITABLE_PARAMETER_UPDATE;
        EditableParameterValue value = updateEditableParameter(command.arguments().getOrDefault("scopeType", "GLOBAL"), Optional.ofNullable(command.arguments().get("scopeId")), command.argument("parameterKey"), command.argument("value"), command.issuedBy().displayName(), dryRun, startedAt);
        return dryRun
                ? dryRun(command, startedAt, "parameter update is valid: " + value.parameterKey() + ".", List.of("parameter:" + value.parameterKey()))
                : completed(command, startedAt, "parameter updated: " + value.parameterKey() + "=" + value.value() + ".", List.of("parameter:" + value.parameterKey()));
    }

    private ControlCommandResult handleEvaluateNewPlayer(ControlCommand command, Instant startedAt) {
        Optional<UniversalKingdom> kingdom = evaluateNewPlayerKingdom();
        return informational(command, startedAt, "new player kingdom=" + kingdom.map(item -> item.kingdomId().value()).orElse("none"), kingdom.map(item -> List.of("kingdom:" + item.kingdomId().value())).orElse(List.of()));
    }

    private void createDefaultRoutingProfiles(UniversalKingdom kingdom, Instant now) {
        for (GamePlatform platform : List.of(GamePlatform.MINECRAFT, GamePlatform.ROBLOX, GamePlatform.DISCORD, GamePlatform.ANDROID, GamePlatform.PC)) {
            String instanceId = kingdom.kingdomId().value() + "-" + platform.name().toLowerCase() + "-primary";
            PlatformInstance instance = new PlatformInstance(instanceId, platform, kingdom.kingdomId(), Optional.empty(), Optional.empty(), kingdom.displayName() + " " + platform.name(), PlatformInstanceState.ONLINE, 0, 250, Optional.of(now), Map.of("default", "true"));
            repository().savePlatformInstance(instance);
            repository().saveRoutingProfile(new KingdomInstanceRoutingProfile("routing-" + kingdom.kingdomId().value() + "-" + platform.name().toLowerCase(), kingdom.kingdomId(), platform, instanceId, List.of(), RoutingState.ENABLED, 250, 0, true, Map.of("createdWithKingdom", "true")));
        }
    }

    private void ensureDefaultCoordinateParameters() {
        for (GamePlatform platform : List.of(GamePlatform.MINECRAFT, GamePlatform.ROBLOX, GamePlatform.ANDROID, GamePlatform.PC)) {
            if (repository().findCoordinateParameters(platform).isEmpty()) {
                repository().saveCoordinateParameters(CoordinateConversionParameters.defaults(platform));
            }
        }
    }

    private void applyParameterToCanonicalState(EditableParameterValue parameterValue, Instant now) {
        if (parameterValue.scopeType() == EditableParameterScopeType.KINGDOM && parameterValue.scopeId().isPresent()) {
            KingdomId kingdomId = KingdomId.of(parameterValue.scopeId().get());
            repository().findKingdom(kingdomId).ifPresent(kingdom -> repository().saveKingdom(kingdom.withEditableParameter(parameterValue.parameterKey(), parameterValue.value(), now)));
        }
        if (parameterValue.scopeType() == EditableParameterScopeType.COORDINATE_CONVERSION && parameterValue.scopeId().isPresent()) {
            GamePlatform platform = GamePlatform.valueOf(parameterValue.scopeId().get().toUpperCase());
            CoordinateConversionParameters existing = repository().findCoordinateParameters(platform).orElse(CoordinateConversionParameters.defaults(platform));
            repository().saveCoordinateParameters(existing.withEditableParameter(parameterValue.parameterKey(), parameterValue.value()));
        }
    }

    private CanonicalWorldCoordinate canonicalFromCommand(ControlCommand command) {
        return new CanonicalWorldCoordinate(
                command.arguments().getOrDefault("worldId", DEFAULT_WORLD_ID),
                parseDouble(command.argument("x"), "x"),
                parseDouble(command.arguments().getOrDefault("y", "0"), "y"),
                parseDouble(command.argument("z"), "z"),
                optionalDouble(command.arguments().get("yaw")),
                optionalDouble(command.arguments().get("pitch")),
                Optional.empty(),
                Optional.empty(),
                Map.of()
        );
    }

    private boolean rectanglesOverlap(KingdomBorderDefinition left, KingdomBorderDefinition right) {
        if (left.borderShape() != KingdomBorderShape.RECTANGLE || right.borderShape() != KingdomBorderShape.RECTANGLE) {
            return false;
        }
        boolean xOverlap = left.minX() < right.maxX() && right.minX() < left.maxX();
        boolean zOverlap = left.minZ() < right.maxZ() && right.minZ() < left.maxZ();
        return xOverlap && zOverlap;
    }

    private String globalAssetForKingdom(UniversalKingdom kingdom) {
        return switch (kingdom.state()) {
            case PROTECTED -> "kingdom.protected";
            case FULL -> "kingdom.full";
            case OVERPOWERED -> "kingdom.overpowered";
            default -> "kingdom.default";
        };
    }

    private void publish(String eventType, Instant now, Map<String, String> attributes) {
        getDomainEventPublisher().publish(new SimpleDomainEvent(eventType, now, attributes));
    }

    private double parseDouble(String value, String name) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new ControlCommandValidationException(name + " must be a number.");
        }
    }

    private Optional<Double> optionalDouble(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(parseDouble(value, "number"));
    }

    private ControlCommandResult dryRun(ControlCommand command, Instant startedAt, String message, List<String> changedObjectIds) {
        return new ControlCommandResult(command.commandId(), CommandExecutionState.DRY_RUN_COMPLETED, true, message, List.of(), changedObjectIds, List.of(), startedAt, Instant.now(), Map.of("dryRun", "true"));
    }

    private ControlCommandResult informational(ControlCommand command, Instant startedAt, String message, List<String> changedObjectIds) {
        return command.dryRun()
                ? dryRun(command, startedAt, message, changedObjectIds)
                : new ControlCommandResult(command.commandId(), CommandExecutionState.COMPLETED, true, message, List.of(), changedObjectIds, List.of(), startedAt, Instant.now(), Map.of("mutatedCanonicalState", "false"));
    }

    private ControlCommandResult completed(ControlCommand command, Instant startedAt, String message, List<String> changedObjectIds) {
        return new ControlCommandResult(command.commandId(), CommandExecutionState.COMPLETED, true, message, List.of(), changedObjectIds, List.of(), startedAt, Instant.now(), Map.of("mutatedCanonicalState", "true"));
    }

    public interface UniversalKingdomRepository {
        int nextKingdomNumber();

        UniversalKingdom saveKingdom(UniversalKingdom kingdom);

        Optional<UniversalKingdom> findKingdom(KingdomId kingdomId);

        List<UniversalKingdom> kingdoms();

        KingdomStorageNamespace saveNamespace(KingdomStorageNamespace namespace);

        Optional<KingdomStorageNamespace> findNamespace(KingdomId kingdomId);

        KingdomBorderDefinition saveBorder(KingdomBorderDefinition border);

        Optional<KingdomBorderDefinition> findBorderForKingdom(KingdomId kingdomId);

        List<KingdomBorderDefinition> kingdomBorders();

        void saveCoordinateParameters(CoordinateConversionParameters parameters);

        Optional<CoordinateConversionParameters> findCoordinateParameters(GamePlatform platform);

        PlayerKingdomLocation savePlayerLocation(PlayerKingdomLocation location);

        Optional<PlayerKingdomLocation> findPlayerLocation(String universalPlayerId);

        PlayerKingdomTransition saveTransition(PlayerKingdomTransition transition);

        Optional<PlayerKingdomTransition> findLatestTransition(String universalPlayerId);

        List<PlayerKingdomTransition> transitions();

        PlatformInstance savePlatformInstance(PlatformInstance instance);

        Optional<PlatformInstance> findPlatformInstance(String platformInstanceId);

        List<PlatformInstance> platformInstances(KingdomId kingdomId, GamePlatform platform);

        KingdomInstanceRoutingProfile saveRoutingProfile(KingdomInstanceRoutingProfile profile);

        Optional<KingdomInstanceRoutingProfile> findRoutingProfile(KingdomId kingdomId, GamePlatform platform);

        InstanceSwitchRequest saveSwitchRequest(InstanceSwitchRequest request);

        Optional<InstanceSwitchRequest> findSwitchRequest(String switchRequestId);

        List<InstanceSwitchRequest> switchRequests();

        EditableParameterDefinition saveEditableParameterDefinition(EditableParameterDefinition definition);

        List<EditableParameterDefinition> editableParameterDefinitions();

        Optional<EditableParameterDefinition> findEditableParameterDefinition(String key);

        EditableParameterValue saveEditableParameterValue(EditableParameterValue value);

        Optional<EditableParameterValue> findEditableParameterValue(String key, Optional<String> scopeId);
    }

    public static final class InMemoryUniversalKingdomRepository implements UniversalKingdomRepository {
        private final Map<KingdomId, UniversalKingdom> kingdoms = new ConcurrentHashMap<>();
        private final Map<KingdomId, KingdomStorageNamespace> namespaces = new ConcurrentHashMap<>();
        private final Map<String, KingdomBorderDefinition> borders = new ConcurrentHashMap<>();
        private final Map<GamePlatform, CoordinateConversionParameters> coordinateParameters = new ConcurrentHashMap<>();
        private final Map<String, PlayerKingdomLocation> playerLocations = new ConcurrentHashMap<>();
        private final Map<String, PlayerKingdomTransition> transitions = new ConcurrentHashMap<>();
        private final Map<String, PlatformInstance> platformInstances = new ConcurrentHashMap<>();
        private final Map<String, KingdomInstanceRoutingProfile> routingProfiles = new ConcurrentHashMap<>();
        private final Map<String, InstanceSwitchRequest> switchRequests = new ConcurrentHashMap<>();
        private final Map<String, EditableParameterDefinition> parameterDefinitions = new ConcurrentHashMap<>();
        private final Map<String, EditableParameterValue> parameterValues = new ConcurrentHashMap<>();

        @Override
        public int nextKingdomNumber() {
            return kingdoms.values().stream().mapToInt(UniversalKingdom::kingdomNumber).max().orElse(0) + 1;
        }

        @Override
        public UniversalKingdom saveKingdom(UniversalKingdom kingdom) {
            kingdoms.put(kingdom.kingdomId(), kingdom);
            return kingdom;
        }

        @Override
        public Optional<UniversalKingdom> findKingdom(KingdomId kingdomId) {
            return Optional.ofNullable(kingdoms.get(kingdomId));
        }

        @Override
        public List<UniversalKingdom> kingdoms() {
            return kingdoms.values().stream().sorted(Comparator.comparingInt(UniversalKingdom::kingdomNumber)).toList();
        }

        @Override
        public KingdomStorageNamespace saveNamespace(KingdomStorageNamespace namespace) {
            namespaces.put(namespace.kingdomId(), namespace);
            return namespace;
        }

        @Override
        public Optional<KingdomStorageNamespace> findNamespace(KingdomId kingdomId) {
            return Optional.ofNullable(namespaces.get(kingdomId));
        }

        @Override
        public KingdomBorderDefinition saveBorder(KingdomBorderDefinition border) {
            borders.put(border.borderDefinitionId(), border);
            return border;
        }

        @Override
        public Optional<KingdomBorderDefinition> findBorderForKingdom(KingdomId kingdomId) {
            return borders.values().stream().filter(border -> border.kingdomId().equals(kingdomId)).findFirst();
        }

        @Override
        public List<KingdomBorderDefinition> kingdomBorders() {
            return borders.values().stream().sorted(Comparator.comparing(KingdomBorderDefinition::borderDefinitionId)).toList();
        }

        @Override
        public void saveCoordinateParameters(CoordinateConversionParameters parameters) {
            coordinateParameters.put(parameters.platform(), parameters);
        }

        @Override
        public Optional<CoordinateConversionParameters> findCoordinateParameters(GamePlatform platform) {
            return Optional.ofNullable(coordinateParameters.get(platform));
        }

        @Override
        public PlayerKingdomLocation savePlayerLocation(PlayerKingdomLocation location) {
            playerLocations.put(location.universalPlayerId(), location);
            return location;
        }

        @Override
        public Optional<PlayerKingdomLocation> findPlayerLocation(String universalPlayerId) {
            return Optional.ofNullable(playerLocations.get(universalPlayerId));
        }

        @Override
        public PlayerKingdomTransition saveTransition(PlayerKingdomTransition transition) {
            transitions.put(transition.transitionId(), transition);
            return transition;
        }

        @Override
        public Optional<PlayerKingdomTransition> findLatestTransition(String universalPlayerId) {
            return transitions.values().stream()
                    .filter(transition -> transition.universalPlayerId().equals(universalPlayerId))
                    .max(Comparator.comparing(PlayerKingdomTransition::createdAt));
        }

        @Override
        public List<PlayerKingdomTransition> transitions() {
            return transitions.values().stream().sorted(Comparator.comparing(PlayerKingdomTransition::createdAt)).toList();
        }

        @Override
        public PlatformInstance savePlatformInstance(PlatformInstance instance) {
            platformInstances.put(instance.platformInstanceId(), instance);
            return instance;
        }

        @Override
        public Optional<PlatformInstance> findPlatformInstance(String platformInstanceId) {
            return Optional.ofNullable(platformInstances.get(platformInstanceId));
        }

        @Override
        public List<PlatformInstance> platformInstances(KingdomId kingdomId, GamePlatform platform) {
            return platformInstances.values().stream()
                    .filter(instance -> instance.kingdomId().equals(kingdomId) && instance.platform() == platform)
                    .sorted(Comparator.comparing(PlatformInstance::platformInstanceId))
                    .toList();
        }

        @Override
        public KingdomInstanceRoutingProfile saveRoutingProfile(KingdomInstanceRoutingProfile profile) {
            routingProfiles.put(routingKey(profile.kingdomId(), profile.platform()), profile);
            return profile;
        }

        @Override
        public Optional<KingdomInstanceRoutingProfile> findRoutingProfile(KingdomId kingdomId, GamePlatform platform) {
            return Optional.ofNullable(routingProfiles.get(routingKey(kingdomId, platform)));
        }

        @Override
        public InstanceSwitchRequest saveSwitchRequest(InstanceSwitchRequest request) {
            switchRequests.put(request.switchRequestId(), request);
            return request;
        }

        @Override
        public Optional<InstanceSwitchRequest> findSwitchRequest(String switchRequestId) {
            return Optional.ofNullable(switchRequests.get(switchRequestId));
        }

        @Override
        public List<InstanceSwitchRequest> switchRequests() {
            return switchRequests.values().stream().sorted(Comparator.comparing(InstanceSwitchRequest::createdAt)).toList();
        }

        @Override
        public EditableParameterDefinition saveEditableParameterDefinition(EditableParameterDefinition definition) {
            parameterDefinitions.put(definition.parameterKey(), definition);
            return definition;
        }

        @Override
        public List<EditableParameterDefinition> editableParameterDefinitions() {
            return parameterDefinitions.values().stream().sorted(Comparator.comparing(EditableParameterDefinition::parameterKey)).toList();
        }

        @Override
        public Optional<EditableParameterDefinition> findEditableParameterDefinition(String key) {
            return Optional.ofNullable(parameterDefinitions.get(key));
        }

        @Override
        public EditableParameterValue saveEditableParameterValue(EditableParameterValue value) {
            parameterValues.put(value.parameterKey() + ":" + value.scopeId().orElse("global"), value);
            return value;
        }

        @Override
        public Optional<EditableParameterValue> findEditableParameterValue(String key, Optional<String> scopeId) {
            return Optional.ofNullable(parameterValues.get(key + ":" + scopeId.orElse("global")));
        }

        private String routingKey(KingdomId kingdomId, GamePlatform platform) {
            return kingdomId.value() + ":" + platform.name();
        }
    }

    public record KingdomId(String value) {
        public KingdomId {
            if (value == null || value.isBlank()) {
                throw new ControlCommandValidationException("kingdomId is required.");
            }
            if (!value.equals("unclaimed") && !value.matches("kingdom-\\d+")) {
                throw new ControlCommandValidationException("Invalid kingdom id: " + value + ".");
            }
        }

        public static KingdomId of(String value) {
            return new KingdomId(value);
        }

        public static KingdomId unclaimed() {
            return new KingdomId("unclaimed");
        }
    }

    public enum KingdomState {
        INITIALIZING,
        OPEN,
        PROTECTED,
        FULL,
        OVERPOWERED,
        LOCKED,
        MIGRATING,
        ARCHIVED
    }

    public record UniversalKingdom(
            KingdomId kingdomId,
            int kingdomNumber,
            String folderName,
            String displayName,
            KingdomState state,
            String borderDefinitionId,
            String worldId,
            String instanceRoutingProfileId,
            Instant createdAt,
            Instant updatedAt,
            KingdomPopulationStats populationStats,
            KingdomPowerStats powerStats,
            KingdomEditableParameters editableParameters,
            Map<String, String> metadata
    ) {
        public UniversalKingdom {
            Objects.requireNonNull(kingdomId, "kingdomId");
            if (kingdomNumber <= 0) {
                throw new ControlCommandValidationException("kingdomNumber must be positive.");
            }
            folderName = folderName == null || folderName.isBlank() ? kingdomId.value() : folderName;
            displayName = displayName == null || displayName.isBlank() ? folderName : displayName;
            state = state == null ? KingdomState.INITIALIZING : state;
            worldId = worldId == null || worldId.isBlank() ? DEFAULT_WORLD_ID : worldId;
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(updatedAt, "updatedAt");
            populationStats = populationStats == null ? KingdomPopulationStats.defaults() : populationStats;
            powerStats = powerStats == null ? KingdomPowerStats.empty() : powerStats;
            editableParameters = editableParameters == null ? KingdomEditableParameters.defaults() : editableParameters;
            metadata = MetadataMaps.immutable(metadata);
        }

        public UniversalKingdom withEditableParameter(String key, String value, Instant now) {
            return new UniversalKingdom(kingdomId, kingdomNumber, folderName, displayName, state, borderDefinitionId, worldId, instanceRoutingProfileId, createdAt, now, populationStats.withParameter(key, value), powerStats, editableParameters.withParameter(key, value), metadata);
        }
    }

    public record KingdomPopulationStats(int currentPlayers, int activePlayers, int registeredPlayers, int maxPlayers, boolean newPlayerAllowed) {
        public static KingdomPopulationStats defaults() {
            return new KingdomPopulationStats(0, 0, 0, 250, true);
        }

        public KingdomPopulationStats withParameter(String key, String value) {
            return switch (key) {
                case "maxActivePlayers", "maxPlayers" -> new KingdomPopulationStats(currentPlayers, activePlayers, registeredPlayers, Integer.parseInt(value), newPlayerAllowed);
                case "newPlayerAllowed" -> new KingdomPopulationStats(currentPlayers, activePlayers, registeredPlayers, maxPlayers, Boolean.parseBoolean(value));
                default -> this;
            };
        }
    }

    public record KingdomPowerStats(double totalGuildPower, double topGuildPower, double averagePlayerPower, double resourceProductionScore, double militaryScore, double economyScore, double newPlayerSafetyScore) {
        public static KingdomPowerStats empty() {
            return new KingdomPowerStats(0, 0, 0, 0, 0, 0, 100);
        }
    }

    public record KingdomEditableParameters(
            int maxActivePlayers,
            int maxRegisteredPlayers,
            double newPlayerPowerThreshold,
            double maxGuildDominanceRatio,
            double maxTopGuildPower,
            double maxAveragePlayerPower,
            boolean newKingdomCreationEnabled,
            boolean borderSwitchingEnabled,
            boolean autoInstanceSwitchingEnabled,
            boolean protectedNewPlayerEntryEnabled,
            boolean coordinateConversionDebugEnabled,
            boolean powerCalculationEnabled,
            boolean populationCalculationEnabled,
            boolean instanceHealthRequiredForRouting,
            Map<String, String> metadata
    ) {
        public KingdomEditableParameters {
            metadata = MetadataMaps.immutable(metadata);
        }

        public static KingdomEditableParameters defaults() {
            return new KingdomEditableParameters(250, 1000, 100, 0.6, 10000, 1000, true, true, true, true, false, true, true, true, Map.of());
        }

        public KingdomEditableParameters withParameter(String key, String value) {
            return switch (key) {
                case "maxActivePlayers" -> new KingdomEditableParameters(Integer.parseInt(value), maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "maxRegisteredPlayers" -> new KingdomEditableParameters(maxActivePlayers, Integer.parseInt(value), newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "newPlayerPowerThreshold" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, Double.parseDouble(value), maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "maxGuildDominanceRatio" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, Double.parseDouble(value), maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "maxTopGuildPower" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, Double.parseDouble(value), maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "maxAveragePlayerPower" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, Double.parseDouble(value), newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "newKingdomCreationEnabled" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, Boolean.parseBoolean(value), borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "borderSwitchingEnabled" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, Boolean.parseBoolean(value), autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "autoInstanceSwitchingEnabled" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, Boolean.parseBoolean(value), protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "protectedNewPlayerEntryEnabled" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, Boolean.parseBoolean(value), coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "coordinateConversionDebugEnabled" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, Boolean.parseBoolean(value), powerCalculationEnabled, populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "powerCalculationEnabled" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, Boolean.parseBoolean(value), populationCalculationEnabled, instanceHealthRequiredForRouting, metadata);
                case "populationCalculationEnabled" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, Boolean.parseBoolean(value), instanceHealthRequiredForRouting, metadata);
                case "instanceHealthRequiredForRouting" -> new KingdomEditableParameters(maxActivePlayers, maxRegisteredPlayers, newPlayerPowerThreshold, maxGuildDominanceRatio, maxTopGuildPower, maxAveragePlayerPower, newKingdomCreationEnabled, borderSwitchingEnabled, autoInstanceSwitchingEnabled, protectedNewPlayerEntryEnabled, coordinateConversionDebugEnabled, powerCalculationEnabled, populationCalculationEnabled, Boolean.parseBoolean(value), metadata);
                default -> this;
            };
        }
    }

    public record KingdomStorageNamespace(KingdomId kingdomId, String folderName, String storagePath, Instant createdAt, Map<String, String> metadata) {
        public KingdomStorageNamespace {
            Objects.requireNonNull(kingdomId, "kingdomId");
            folderName = folderName == null || folderName.isBlank() ? kingdomId.value() : folderName;
            storagePath = storagePath == null || storagePath.isBlank() ? "kingdoms/" + folderName : storagePath;
            Objects.requireNonNull(createdAt, "createdAt");
            metadata = MetadataMaps.immutable(metadata);
        }
    }

    public record KingdomFolderNamingPolicy(String prefix) {
        public String folderName(int kingdomNumber) {
            if (kingdomNumber <= 0) {
                throw new ControlCommandValidationException("kingdomNumber must be positive.");
            }
            return (prefix == null || prefix.isBlank() ? "kingdom" : prefix) + "-" + kingdomNumber;
        }
    }

    public enum KingdomBorderShape {
        RECTANGLE,
        CIRCLE,
        POLYGON,
        CUSTOM
    }

    public record KingdomBorderDefinition(
            String borderDefinitionId,
            KingdomId kingdomId,
            String worldId,
            KingdomBorderShape borderShape,
            double minX,
            double maxX,
            Optional<Double> minY,
            Optional<Double> maxY,
            double minZ,
            double maxZ,
            double centerX,
            Optional<Double> centerY,
            double centerZ,
            Optional<Double> radius,
            List<KingdomId> neighboringKingdomIds,
            Instant createdAt,
            Instant updatedAt,
            Map<String, String> metadata
    ) {
        public KingdomBorderDefinition {
            if (borderDefinitionId == null || borderDefinitionId.isBlank()) {
                throw new ControlCommandValidationException("borderDefinitionId is required.");
            }
            Objects.requireNonNull(kingdomId, "kingdomId");
            worldId = worldId == null || worldId.isBlank() ? DEFAULT_WORLD_ID : worldId;
            borderShape = borderShape == null ? KingdomBorderShape.RECTANGLE : borderShape;
            minY = minY == null ? Optional.empty() : minY;
            maxY = maxY == null ? Optional.empty() : maxY;
            centerY = centerY == null ? Optional.empty() : centerY;
            radius = radius == null ? Optional.empty() : radius;
            neighboringKingdomIds = neighboringKingdomIds == null ? List.of() : List.copyOf(neighboringKingdomIds);
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(updatedAt, "updatedAt");
            metadata = MetadataMaps.immutable(metadata);
        }

        public void validate() {
            if (borderShape != KingdomBorderShape.RECTANGLE) {
                throw new ControlCommandValidationException("Only RECTANGLE borders are implemented.");
            }
            if (maxX <= minX || maxZ <= minZ) {
                throw new ControlCommandValidationException("Border max values must be greater than min values.");
            }
            if (minY.isPresent() && maxY.isPresent() && maxY.get() <= minY.get()) {
                throw new ControlCommandValidationException("Border maxY must be greater than minY.");
            }
        }

        public boolean contains(CanonicalWorldCoordinate coordinate) {
            if (!worldId.equals(coordinate.worldId())) {
                return false;
            }
            boolean insideY = minY.map(value -> coordinate.y() >= value).orElse(true)
                    && maxY.map(value -> coordinate.y() <= value).orElse(true);
            return coordinate.x() >= minX && coordinate.x() < maxX && coordinate.z() >= minZ && coordinate.z() < maxZ && insideY;
        }
    }

    public record CanonicalWorldCoordinate(String worldId, double x, double y, double z, Optional<Double> yaw, Optional<Double> pitch, Optional<String> kingdomId, Optional<String> regionId, Map<String, String> metadata) {
        public CanonicalWorldCoordinate {
            worldId = worldId == null || worldId.isBlank() ? DEFAULT_WORLD_ID : worldId;
            yaw = yaw == null ? Optional.empty() : yaw;
            pitch = pitch == null ? Optional.empty() : pitch;
            kingdomId = kingdomId == null ? Optional.empty() : kingdomId;
            regionId = regionId == null ? Optional.empty() : regionId;
            metadata = MetadataMaps.immutable(metadata);
        }

        public void validate() {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new ControlCommandValidationException("Canonical coordinate values must be finite.");
            }
        }

        public CanonicalWorldCoordinate withKingdomId(String kingdomId) {
            return new CanonicalWorldCoordinate(worldId, x, y, z, yaw, pitch, Optional.ofNullable(kingdomId), regionId, metadata);
        }

        public String compact() {
            return worldId + ":" + x + "," + y + "," + z;
        }
    }

    public record PlatformWorldCoordinate(GamePlatform platform, String platformWorldId, double x, double y, double z, Optional<Double> yaw, Optional<Double> pitch, Map<String, String> metadata) {
        public PlatformWorldCoordinate {
            Objects.requireNonNull(platform, "platform");
            platformWorldId = platformWorldId == null || platformWorldId.isBlank() ? DEFAULT_WORLD_ID : platformWorldId;
            yaw = yaw == null ? Optional.empty() : yaw;
            pitch = pitch == null ? Optional.empty() : pitch;
            metadata = MetadataMaps.immutable(metadata);
        }

        public void validate() {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new ControlCommandValidationException("Platform coordinate values must be finite.");
            }
        }
    }

    public record CoordinateConversionResult(GamePlatform sourcePlatform, Optional<GamePlatform> targetPlatform, CanonicalWorldCoordinate canonicalCoordinate, Optional<PlatformWorldCoordinate> platformCoordinate, List<String> conversionWarnings, Map<String, String> metadata) {
        public CoordinateConversionResult {
            Objects.requireNonNull(sourcePlatform, "sourcePlatform");
            targetPlatform = targetPlatform == null ? Optional.empty() : targetPlatform;
            Objects.requireNonNull(canonicalCoordinate, "canonicalCoordinate");
            platformCoordinate = platformCoordinate == null ? Optional.empty() : platformCoordinate;
            conversionWarnings = conversionWarnings == null ? List.of() : List.copyOf(conversionWarnings);
            metadata = MetadataMaps.immutable(metadata);
        }
    }

    public record CoordinateConversionParameters(GamePlatform platform, double platformScaleX, double platformScaleY, double platformScaleZ, String platformXAxisCanonicalAxis, String platformYAxisCanonicalAxis, String platformZAxisCanonicalAxis, double originOffsetX, double originOffsetY, double originOffsetZ, boolean invertX, boolean invertY, boolean invertZ, double rotationOffsetYaw, boolean debugEnabled, Map<String, String> metadata) {
        public CoordinateConversionParameters {
            Objects.requireNonNull(platform, "platform");
            metadata = MetadataMaps.immutable(metadata);
        }

        public static CoordinateConversionParameters defaults(GamePlatform platform) {
            String mapping = platform == GamePlatform.ROBLOX ? "x,y,z-explicit" : "x,y,z";
            return new CoordinateConversionParameters(platform, 1, 1, 1, "x", "y", "z", 0, 0, 0, false, false, false, 0, false, Map.of("axisMapping", mapping));
        }

        public String axisMapping() {
            return platformXAxisCanonicalAxis + "," + platformYAxisCanonicalAxis + "," + platformZAxisCanonicalAxis;
        }

        public CanonicalWorldCoordinate toCanonical(PlatformWorldCoordinate coordinate) {
            EnumMap<Axis, Double> values = new EnumMap<>(Axis.class);
            putAxis(values, platformXAxisCanonicalAxis, convertIn(coordinate.x(), platformScaleX, originOffsetX, invertX));
            putAxis(values, platformYAxisCanonicalAxis, convertIn(coordinate.y(), platformScaleY, originOffsetY, invertY));
            putAxis(values, platformZAxisCanonicalAxis, convertIn(coordinate.z(), platformScaleZ, originOffsetZ, invertZ));
            return new CanonicalWorldCoordinate(coordinate.platformWorldId(), values.getOrDefault(Axis.X, 0.0), values.getOrDefault(Axis.Y, 0.0), values.getOrDefault(Axis.Z, 0.0), coordinate.yaw().map(yaw -> yaw + rotationOffsetYaw), coordinate.pitch(), Optional.empty(), Optional.empty(), coordinate.metadata());
        }

        public PlatformWorldCoordinate toPlatform(CanonicalWorldCoordinate coordinate) {
            double px = convertOut(axisValue(coordinate, platformXAxisCanonicalAxis), platformScaleX, originOffsetX, invertX);
            double py = convertOut(axisValue(coordinate, platformYAxisCanonicalAxis), platformScaleY, originOffsetY, invertY);
            double pz = convertOut(axisValue(coordinate, platformZAxisCanonicalAxis), platformScaleZ, originOffsetZ, invertZ);
            return new PlatformWorldCoordinate(platform, coordinate.worldId(), px, py, pz, coordinate.yaw().map(yaw -> yaw - rotationOffsetYaw), coordinate.pitch(), coordinate.metadata());
        }

        public CoordinateConversionParameters withCommandArguments(Map<String, String> arguments) {
            return new CoordinateConversionParameters(
                    platform,
                    Double.parseDouble(arguments.getOrDefault("platformScaleX", Double.toString(platformScaleX))),
                    Double.parseDouble(arguments.getOrDefault("platformScaleY", Double.toString(platformScaleY))),
                    Double.parseDouble(arguments.getOrDefault("platformScaleZ", Double.toString(platformScaleZ))),
                    arguments.getOrDefault("platformXAxisCanonicalAxis", platformXAxisCanonicalAxis),
                    arguments.getOrDefault("platformYAxisCanonicalAxis", platformYAxisCanonicalAxis),
                    arguments.getOrDefault("platformZAxisCanonicalAxis", platformZAxisCanonicalAxis),
                    Double.parseDouble(arguments.getOrDefault("originOffsetX", Double.toString(originOffsetX))),
                    Double.parseDouble(arguments.getOrDefault("originOffsetY", Double.toString(originOffsetY))),
                    Double.parseDouble(arguments.getOrDefault("originOffsetZ", Double.toString(originOffsetZ))),
                    Boolean.parseBoolean(arguments.getOrDefault("invertX", Boolean.toString(invertX))),
                    Boolean.parseBoolean(arguments.getOrDefault("invertY", Boolean.toString(invertY))),
                    Boolean.parseBoolean(arguments.getOrDefault("invertZ", Boolean.toString(invertZ))),
                    Double.parseDouble(arguments.getOrDefault("rotationOffsetYaw", Double.toString(rotationOffsetYaw))),
                    Boolean.parseBoolean(arguments.getOrDefault("coordinateConversionDebugEnabled", Boolean.toString(debugEnabled))),
                    metadata
            );
        }

        public CoordinateConversionParameters withEditableParameter(String key, String value) {
            return withCommandArguments(Map.of(key, value));
        }

        private double convertIn(double value, double scale, double offset, boolean inverted) {
            double adjusted = (value - offset) / scale;
            return inverted ? -adjusted : adjusted;
        }

        private double convertOut(double value, double scale, double offset, boolean inverted) {
            double adjusted = inverted ? -value : value;
            return adjusted * scale + offset;
        }

        private void putAxis(EnumMap<Axis, Double> values, String axis, double value) {
            values.put(Axis.valueOf(axis.trim().toUpperCase()), value);
        }

        private double axisValue(CanonicalWorldCoordinate coordinate, String axis) {
            return switch (Axis.valueOf(axis.trim().toUpperCase())) {
                case X -> coordinate.x();
                case Y -> coordinate.y();
                case Z -> coordinate.z();
            };
        }

        private enum Axis {
            X,
            Y,
            Z
        }
    }

    public record PlayerKingdomLocation(String universalPlayerId, KingdomId currentKingdomId, Optional<KingdomId> previousKingdomId, CanonicalWorldCoordinate canonicalCoordinate, GamePlatform platform, Optional<String> platformInstanceId, Instant lastUpdatedAt, Optional<Instant> lastBorderCrossingAt, Map<String, String> metadata) {
        public PlayerKingdomLocation {
            if (universalPlayerId == null || universalPlayerId.isBlank()) {
                throw new ControlCommandValidationException("universalPlayerId is required.");
            }
            Objects.requireNonNull(currentKingdomId, "currentKingdomId");
            previousKingdomId = previousKingdomId == null ? Optional.empty() : previousKingdomId;
            Objects.requireNonNull(canonicalCoordinate, "canonicalCoordinate");
            Objects.requireNonNull(platform, "platform");
            platformInstanceId = platformInstanceId == null ? Optional.empty() : platformInstanceId;
            Objects.requireNonNull(lastUpdatedAt, "lastUpdatedAt");
            lastBorderCrossingAt = lastBorderCrossingAt == null ? Optional.empty() : lastBorderCrossingAt;
            metadata = MetadataMaps.immutable(metadata);
        }
    }

    public record PlayerKingdomTransition(String transitionId, String universalPlayerId, KingdomId fromKingdomId, KingdomId toKingdomId, Optional<String> fromInstanceId, Optional<String> toInstanceId, GamePlatform platform, CanonicalWorldCoordinate crossingCoordinate, KingdomTransitionState transitionState, Instant createdAt, Optional<Instant> completedAt, Map<String, String> metadata) {
        public PlayerKingdomTransition {
            transitionId = transitionId == null || transitionId.isBlank() ? UUID.randomUUID().toString() : transitionId;
            fromInstanceId = fromInstanceId == null ? Optional.empty() : fromInstanceId;
            toInstanceId = toInstanceId == null ? Optional.empty() : toInstanceId;
            transitionState = transitionState == null ? KingdomTransitionState.DETECTED : transitionState;
            completedAt = completedAt == null ? Optional.empty() : completedAt;
            metadata = MetadataMaps.immutable(metadata);
        }

        public PlayerKingdomTransition withState(KingdomTransitionState state, Optional<String> toInstanceId, Optional<Instant> completedAt, Instant now) {
            return new PlayerKingdomTransition(transitionId, universalPlayerId, fromKingdomId, toKingdomId, fromInstanceId, toInstanceId, platform, crossingCoordinate, state, createdAt, completedAt, metadata);
        }
    }

    public enum KingdomTransitionState {
        DETECTED,
        ROUTING_REQUESTED,
        INSTANCE_SWITCH_SENT,
        SWITCH_CONFIRMED,
        FAILED,
        CANCELLED
    }

    public record PlayerLocationUpdateResult(PlayerKingdomLocation location, Optional<PlayerKingdomTransition> transition, Optional<InstanceSwitchRequest> switchRequest, CoordinateConversionResult conversionResult) {
        public PlayerLocationUpdateResult {
            transition = transition == null ? Optional.empty() : transition;
            switchRequest = switchRequest == null ? Optional.empty() : switchRequest;
        }
    }

    public record KingdomInstanceRoutingProfile(String instanceRoutingProfileId, KingdomId kingdomId, GamePlatform platform, String primaryInstanceId, List<String> fallbackInstanceIds, RoutingState routingState, int maxPlayers, int currentPlayers, boolean healthy, Map<String, String> metadata) {
        public KingdomInstanceRoutingProfile {
            fallbackInstanceIds = fallbackInstanceIds == null ? List.of() : List.copyOf(fallbackInstanceIds);
            routingState = routingState == null ? RoutingState.ENABLED : routingState;
            metadata = MetadataMaps.immutable(metadata);
        }

        public KingdomInstanceRoutingProfile withHealth(boolean healthy, int currentPlayers) {
            return new KingdomInstanceRoutingProfile(instanceRoutingProfileId, kingdomId, platform, primaryInstanceId, fallbackInstanceIds, routingState, maxPlayers, currentPlayers, healthy, metadata);
        }
    }

    public enum RoutingState {
        ENABLED,
        DISABLED,
        DRAINING
    }

    public record PlatformInstance(String platformInstanceId, GamePlatform platform, KingdomId kingdomId, Optional<String> serverAddress, Optional<Integer> serverPort, String instanceName, PlatformInstanceState state, int currentPlayers, int maxPlayers, Optional<Instant> lastHealthCheckAt, Map<String, String> metadata) {
        public PlatformInstance {
            if (platformInstanceId == null || platformInstanceId.isBlank()) {
                throw new ControlCommandValidationException("platformInstanceId is required.");
            }
            serverAddress = serverAddress == null ? Optional.empty() : serverAddress;
            serverPort = serverPort == null ? Optional.empty() : serverPort;
            state = state == null ? PlatformInstanceState.STARTING : state;
            lastHealthCheckAt = lastHealthCheckAt == null ? Optional.empty() : lastHealthCheckAt;
            metadata = MetadataMaps.immutable(metadata);
        }

        public boolean healthy() {
            return state == PlatformInstanceState.ONLINE && currentPlayers < maxPlayers;
        }

        public PlatformInstance withState(PlatformInstanceState state, boolean healthy, Instant now) {
            PlatformInstanceState effectiveState = healthy && state == PlatformInstanceState.DEGRADED ? PlatformInstanceState.ONLINE : state;
            return new PlatformInstance(platformInstanceId, platform, kingdomId, serverAddress, serverPort, instanceName, effectiveState, currentPlayers, maxPlayers, Optional.of(now), metadata);
        }
    }

    public enum PlatformInstanceState {
        STARTING,
        ONLINE,
        FULL,
        DEGRADED,
        OFFLINE,
        DRAINING,
        ARCHIVED
    }

    public record InstanceSwitchRequest(String switchRequestId, String universalPlayerId, GamePlatform platform, KingdomId fromKingdomId, KingdomId toKingdomId, Optional<String> fromInstanceId, String toInstanceId, InstanceSwitchReason reason, InstanceSwitchState state, Instant createdAt, Optional<Instant> completedAt, Map<String, String> metadata) {
        public InstanceSwitchRequest {
            switchRequestId = switchRequestId == null || switchRequestId.isBlank() ? UUID.randomUUID().toString() : switchRequestId;
            fromInstanceId = fromInstanceId == null ? Optional.empty() : fromInstanceId;
            reason = reason == null ? InstanceSwitchReason.CONTROL_COMMAND : reason;
            state = state == null ? InstanceSwitchState.REQUESTED : state;
            completedAt = completedAt == null ? Optional.empty() : completedAt;
            metadata = MetadataMaps.immutable(metadata);
        }

        public InstanceSwitchRequest withState(InstanceSwitchState state, Instant now, Map<String, String> extraMetadata) {
            HashMap<String, String> combined = new HashMap<>(metadata);
            if (extraMetadata != null) {
                combined.putAll(extraMetadata);
            }
            return new InstanceSwitchRequest(switchRequestId, universalPlayerId, platform, fromKingdomId, toKingdomId, fromInstanceId, toInstanceId, reason, state, createdAt, Optional.of(now), combined);
        }
    }

    public enum InstanceSwitchReason {
        KINGDOM_BORDER_CROSSED,
        CONTROL_COMMAND,
        NEW_PLAYER_ROUTING,
        INSTANCE_FULL,
        INSTANCE_DEGRADED,
        KINGDOM_MIGRATION
    }

    public enum InstanceSwitchState {
        REQUESTED,
        SENT,
        CONFIRMED,
        FAILED,
        CANCELLED
    }

    public static final class PlatformInstanceSelectionHandler implements IKingdomDomain {
        public PlatformInstanceSelectionHandler() {
        }

        public PlatformInstanceSelectionHandler(UniversalKingdomRepository repository) {
            registerUniversalKingdomRepository(repository);
        }

        public UniversalKingdomRepository repository() {
            return getUniversalKingdomRepository();
        }

        public Optional<PlatformInstance> selectInstance(KingdomId kingdomId, GamePlatform platform) {
            Optional<KingdomInstanceRoutingProfile> profile = repository().findRoutingProfile(kingdomId, platform);
            if (profile.isEmpty() || profile.get().routingState() != RoutingState.ENABLED) {
                return Optional.empty();
            }
            Optional<PlatformInstance> primary = repository().findPlatformInstance(profile.get().primaryInstanceId()).filter(PlatformInstance::healthy);
            if (primary.isPresent()) {
                return primary;
            }
            for (String fallbackInstanceId : profile.get().fallbackInstanceIds()) {
                Optional<PlatformInstance> fallback = repository().findPlatformInstance(fallbackInstanceId).filter(PlatformInstance::healthy);
                if (fallback.isPresent()) {
                    return fallback;
                }
            }
            return repository().platformInstances(kingdomId, platform).stream().filter(PlatformInstance::healthy).findFirst();
        }
    }

    public record EditableParameterDefinition(String parameterKey, String displayName, String description, EditableParameterValueType valueType, String defaultValue, Optional<Double> minValue, Optional<Double> maxValue, List<String> allowedValues, boolean editableFromControlCommand, boolean editableFromWebPanel, boolean requiresRestart, boolean requiresAudit, Map<String, String> metadata) {
        public EditableParameterDefinition {
            minValue = minValue == null ? Optional.empty() : minValue;
            maxValue = maxValue == null ? Optional.empty() : maxValue;
            allowedValues = allowedValues == null ? List.of() : List.copyOf(allowedValues);
            metadata = MetadataMaps.immutable(metadata);
        }
    }

    public enum EditableParameterValueType {
        BOOLEAN,
        INTEGER,
        DOUBLE,
        STRING
    }

    public record EditableParameterValue(String parameterKey, EditableParameterScopeType scopeType, Optional<String> scopeId, String value, String updatedBy, Instant updatedAt, Map<String, String> metadata) {
        public EditableParameterValue {
            scopeId = scopeId == null ? Optional.empty() : scopeId;
            metadata = MetadataMaps.immutable(metadata);
        }
    }

    public enum EditableParameterScopeType {
        GLOBAL,
        KINGDOM,
        PLATFORM,
        PLATFORM_INSTANCE,
        COORDINATE_CONVERSION,
        NEW_PLAYER_ROUTING
    }

    public static final class EditableParameterDefinitionRegistryHandler implements IKingdomDomain {
        public EditableParameterDefinitionRegistryHandler() {
        }

        public EditableParameterDefinitionRegistryHandler(UniversalKingdomRepository repository) {
            registerUniversalKingdomRepository(repository);
        }

        public UniversalKingdomRepository repository() {
            return getUniversalKingdomRepository();
        }

        public void registerDefaults() {
            define("maxActivePlayers", EditableParameterValueType.INTEGER, "250", 1, 100000, "Kingdom active player cap.");
            define("maxRegisteredPlayers", EditableParameterValueType.INTEGER, "1000", 1, 1000000, "Kingdom registered player cap.");
            define("newPlayerAllowed", EditableParameterValueType.BOOLEAN, "true", null, null, "Allows new players to be assigned to this kingdom.");
            define("newKingdomCreationEnabled", EditableParameterValueType.BOOLEAN, "true", null, null, "Allows scaling to create/recommend new kingdoms.");
            define("borderSwitchingEnabled", EditableParameterValueType.BOOLEAN, "true", null, null, "Allows backend border crossing transitions.");
            define("autoInstanceSwitchingEnabled", EditableParameterValueType.BOOLEAN, "true", null, null, "Allows automatic instance switch requests.");
            define("newPlayerPowerThreshold", EditableParameterValueType.DOUBLE, "100", 0, null, "Maximum new player power for protected entry decisions.");
            define("maxGuildDominanceRatio", EditableParameterValueType.DOUBLE, "0.6", 0, 1, "Maximum top guild dominance ratio before a kingdom is considered unsafe.");
            define("maxTopGuildPower", EditableParameterValueType.DOUBLE, "10000", 0, null, "Maximum top guild power for new player safety.");
            define("maxAveragePlayerPower", EditableParameterValueType.DOUBLE, "1000", 0, null, "Maximum average player power for new player safety.");
            define("coordinateConversionDebugEnabled", EditableParameterValueType.BOOLEAN, "false", null, null, "Enables conversion warning/debug metadata.");
            define("powerCalculationEnabled", EditableParameterValueType.BOOLEAN, "true", null, null, "Enables kingdom power calculations.");
            define("populationCalculationEnabled", EditableParameterValueType.BOOLEAN, "true", null, null, "Enables kingdom population calculations.");
            define("instanceHealthRequiredForRouting", EditableParameterValueType.BOOLEAN, "true", null, null, "Requires healthy target instances for routing.");
            define("borderShape", EditableParameterValueType.STRING, "RECTANGLE", null, null, "Editable border shape.", List.of("RECTANGLE", "CIRCLE", "POLYGON", "CUSTOM"));
            define("minX", EditableParameterValueType.DOUBLE, "0", null, null, "Border minimum canonical X.");
            define("maxX", EditableParameterValueType.DOUBLE, "1000", null, null, "Border maximum canonical X.");
            define("minY", EditableParameterValueType.DOUBLE, "0", null, null, "Optional border minimum canonical Y.");
            define("maxY", EditableParameterValueType.DOUBLE, "256", null, null, "Optional border maximum canonical Y.");
            define("minZ", EditableParameterValueType.DOUBLE, "0", null, null, "Border minimum canonical Z.");
            define("maxZ", EditableParameterValueType.DOUBLE, "1000", null, null, "Border maximum canonical Z.");
            define("neighboringKingdomIds", EditableParameterValueType.STRING, "", null, null, "Comma-separated neighboring kingdom IDs.");
            define("primaryInstanceId", EditableParameterValueType.STRING, "", null, null, "Primary frontend instance ID for a kingdom/platform route.");
            define("fallbackInstanceIds", EditableParameterValueType.STRING, "", null, null, "Comma-separated fallback frontend instance IDs.");
            define("maxPlayers", EditableParameterValueType.INTEGER, "250", 1, 100000, "Maximum players for an instance/routing profile.");
            define("routingEnabled", EditableParameterValueType.BOOLEAN, "true", null, null, "Enables route selection for a kingdom/platform profile.");
            define("platformScaleX", EditableParameterValueType.DOUBLE, "1", 0, null, "Platform X scale into canonical space.");
            define("platformScaleY", EditableParameterValueType.DOUBLE, "1", 0, null, "Platform Y scale into canonical space.");
            define("platformScaleZ", EditableParameterValueType.DOUBLE, "1", 0, null, "Platform Z scale into canonical space.");
            define("platformXAxisCanonicalAxis", EditableParameterValueType.STRING, "x", null, null, "Canonical axis sourced by platform X.", List.of("x", "y", "z"));
            define("platformYAxisCanonicalAxis", EditableParameterValueType.STRING, "y", null, null, "Canonical axis sourced by platform Y.", List.of("x", "y", "z"));
            define("platformZAxisCanonicalAxis", EditableParameterValueType.STRING, "z", null, null, "Canonical axis sourced by platform Z.", List.of("x", "y", "z"));
            define("originOffsetX", EditableParameterValueType.DOUBLE, "0", null, null, "Platform X origin offset.");
            define("originOffsetY", EditableParameterValueType.DOUBLE, "0", null, null, "Platform Y origin offset.");
            define("originOffsetZ", EditableParameterValueType.DOUBLE, "0", null, null, "Platform Z origin offset.");
            define("invertX", EditableParameterValueType.BOOLEAN, "false", null, null, "Invert platform X before canonical mapping.");
            define("invertY", EditableParameterValueType.BOOLEAN, "false", null, null, "Invert platform Y before canonical mapping.");
            define("invertZ", EditableParameterValueType.BOOLEAN, "false", null, null, "Invert platform Z before canonical mapping.");
            define("rotationOffsetYaw", EditableParameterValueType.DOUBLE, "0", null, null, "Yaw offset applied during platform/canonical conversion.");
            define("preferredNewPlayerKingdomId", EditableParameterValueType.STRING, "", null, null, "Optional preferred kingdom ID for new players.");
            define("safestKingdomRoutingEnabled", EditableParameterValueType.BOOLEAN, "true", null, null, "Routes new players toward safest eligible kingdom.");
            define("fullKingdomAvoidanceEnabled", EditableParameterValueType.BOOLEAN, "true", null, null, "Avoids full kingdoms for new player assignment.");
            define("overpoweredKingdomAvoidanceEnabled", EditableParameterValueType.BOOLEAN, "true", null, null, "Avoids overpowered kingdoms for new player assignment.");
        }

        private void define(String key, EditableParameterValueType type, String defaultValue, Integer min, Integer max, String description) {
            define(key, type, defaultValue, min, max, description, List.of());
        }

        private void define(String key, EditableParameterValueType type, String defaultValue, Integer min, Integer max, String description, List<String> allowedValues) {
            repository().saveEditableParameterDefinition(new EditableParameterDefinition(key, key, description, type, defaultValue, min == null ? Optional.empty() : Optional.of(min.doubleValue()), max == null ? Optional.empty() : Optional.of(max.doubleValue()), allowedValues, true, true, false, true, Map.of()));
        }
    }

    public static final class EditableParameterValidationHandler {
        public void validate(EditableParameterDefinition definition, String value) {
            if (!definition.editableFromControlCommand()) {
                throw new ControlCommandValidationException("Parameter is not editable from control commands.");
            }
            switch (definition.valueType()) {
                case BOOLEAN -> {
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        throw new ControlCommandValidationException("Parameter value must be boolean.");
                    }
                }
                case INTEGER -> validateNumber(definition, Integer.parseInt(value));
                case DOUBLE -> validateNumber(definition, Double.parseDouble(value));
                case STRING -> {
                    if (!definition.allowedValues().isEmpty() && !definition.allowedValues().contains(value)) {
                        throw new ControlCommandValidationException("Parameter value is not allowed.");
                    }
                }
            }
        }

        private void validateNumber(EditableParameterDefinition definition, double value) {
            if (definition.parameterKey().startsWith("platformScale") && value <= 0) {
                throw new ControlCommandValidationException("Coordinate conversion scale must be greater than zero.");
            }
            if (definition.minValue().isPresent() && value < definition.minValue().get()) {
                throw new ControlCommandValidationException("Parameter value is below minimum.");
            }
            if (definition.maxValue().isPresent() && value > definition.maxValue().get()) {
                throw new ControlCommandValidationException("Parameter value is above maximum.");
            }
        }
    }

    public record KingdomScalingEvaluation(List<String> sourceKingdomIds, boolean newKingdomRecommended, Instant evaluatedAt, Map<String, String> metadata) {
        public KingdomScalingEvaluation {
            sourceKingdomIds = sourceKingdomIds == null ? List.of() : List.copyOf(sourceKingdomIds);
            metadata = MetadataMaps.immutable(metadata);
        }
    }

    public record KingdomProjection(String objectId, String objectType, String state, String globalAssetId, String displayName, GamePlatform platform, List<String> availableActions, List<String> disabledReasons, Map<String, String> metadata) {
        public KingdomProjection {
            availableActions = availableActions == null ? List.of() : List.copyOf(availableActions);
            disabledReasons = disabledReasons == null ? List.of() : List.copyOf(disabledReasons);
            metadata = MetadataMaps.immutable(metadata);
        }
    }

    public record KingdomTransitionProjection(String objectId, String objectType, String state, String globalAssetId, String displayName, GamePlatform platform, List<String> availableActions, List<String> disabledReasons, Map<String, String> metadata) {
        public KingdomTransitionProjection {
            availableActions = availableActions == null ? List.of() : List.copyOf(availableActions);
            disabledReasons = disabledReasons == null ? List.of() : List.copyOf(disabledReasons);
            metadata = MetadataMaps.immutable(metadata);
        }
    }

    public record KingdomCreationPolicy(boolean enabled, int maxActivePlayersBeforeNewKingdom, int maxRegisteredPlayersBeforeNewKingdom, double maxAveragePlayerPowerForNewPlayers, double maxTopGuildDominanceRatio, double maxTopGuildPowerForNewPlayers, boolean requireHealthyFrontendInstances, String defaultWorldTemplateId, double defaultBorderSize, String defaultProtectionStateDuration, Map<String, String> metadata) {
        public KingdomCreationPolicy {
            metadata = MetadataMaps.immutable(metadata);
        }
    }
}
