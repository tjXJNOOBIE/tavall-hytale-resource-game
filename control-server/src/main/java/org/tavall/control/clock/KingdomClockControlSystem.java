package org.tavall.control.clock;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.runtime.CommandExecutionState;
import org.tavall.control.runtime.ControlCommand;
import org.tavall.control.runtime.ControlCommandResult;
import org.tavall.control.runtime.ControlCommandValidationException;
import org.tavall.control.event.DomainEvent;
import org.tavall.control.event.DomainEventPublisher;
import org.tavall.control.event.RecordingDomainEventPublisher;
import org.tavall.control.persistence.PostgresConnectionProvider;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class KingdomClockControlSystem implements IClockDomain {
    public static final String DEFAULT_KINGDOM_ID = "kingdom-1";

    public KingdomClockControlSystem() {
        seedDefaultScheduleRules(DEFAULT_KINGDOM_ID, getSystemClock().instant());
    }

    public KingdomClockControlSystem(KingdomClockRepository repository, DomainEventPublisher eventPublisher, Clock systemClock) {
        registerKingdomClockRepository(Objects.requireNonNull(repository, "repository"));
        registerDomainEventPublisher(Objects.requireNonNull(eventPublisher, "eventPublisher"));
        registerSystemClock(Objects.requireNonNull(systemClock, "systemClock"));
        registerKingdomClockPhaseCalculationHandler(new KingdomClockPhaseCalculationHandler());
        registerKingdomScheduleWindowContainmentHandler(new KingdomScheduleWindowContainmentHandler());
    }

    public static KingdomClockControlSystem inMemory() {
        return inMemory(new RecordingDomainEventPublisher(), Clock.systemUTC());
    }

    public static KingdomClockControlSystem inMemory(DomainEventPublisher eventPublisher) {
        return inMemory(eventPublisher, Clock.systemUTC());
    }

    public static KingdomClockControlSystem inMemory(DomainEventPublisher eventPublisher, Clock clock) {
        InMemoryKingdomClockRepository repository = new InMemoryKingdomClockRepository();
        KingdomClockControlSystem system = new KingdomClockControlSystem(repository, eventPublisher, clock);
        system.seedDefaultScheduleRules(DEFAULT_KINGDOM_ID, clock.instant());
        return system;
    }

    public static KingdomClockControlSystem postgres(PostgresConnectionProvider connectionProvider, DomainEventPublisher eventPublisher, Clock clock) {
        PostgresKingdomClockRepository repository = new PostgresKingdomClockRepository(connectionProvider);
        KingdomClockControlSystem system = new KingdomClockControlSystem(repository, eventPublisher, clock);
        system.seedDefaultScheduleRules(DEFAULT_KINGDOM_ID, clock.instant());
        return system;
    }

    public KingdomClockRepository repository() {
        return getKingdomClockRepository();
    }

    public KingdomClockState getCurrentClockState(String kingdomId) {
        return ensureState(kingdomIdOrDefault(kingdomId), getSystemClock().instant());
    }

    public boolean isNight(String kingdomId) {
        return getCurrentClockState(kingdomId).isNight();
    }

    public boolean isDay(String kingdomId) {
        return getCurrentClockState(kingdomId).isDay();
    }

    public KingdomTimePhase getCurrentPhase(String kingdomId) {
        return getCurrentClockState(kingdomId).currentPhase();
    }

    public int getCurrentHour(String kingdomId) {
        return getCurrentClockState(kingdomId).currentHour();
    }

    public int getCurrentMinute(String kingdomId) {
        return getCurrentClockState(kingdomId).currentMinute();
    }

    public long getCurrentEpochMinute(String kingdomId) {
        return getCurrentClockState(kingdomId).currentEpochMinute();
    }

    public KingdomClockTickResult tickClock(String kingdomId) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        Instant now = getSystemClock().instant();
        KingdomClockState previousState = ensureState(resolvedKingdomId, now);
        KingdomClockConfig config = configFor(resolvedKingdomId);
        KingdomClockState currentState = switch (config.clockMode()) {
            case PAUSED -> previousState;
            case FIXED_OVERRIDE -> stateForOverride(previousState, config, now);
            case REAL_TIME_SYNCED -> stateForRealTime(previousState, config, now);
            case ACCELERATED -> stateForAccelerated(previousState, config, now);
        };
        repository().saveClockState(currentState);
        boolean advanced = currentState.currentEpochMinute() != previousState.currentEpochMinute()
                || currentState.currentHour() != previousState.currentHour()
                || currentState.currentMinute() != previousState.currentMinute();
        boolean phaseChanged = currentState.currentPhase() != previousState.currentPhase();
        List<String> events = new ArrayList<>();
        events.add("KingdomClockTickedEvent");
        publish("KingdomClockTickedEvent", now, Map.of("kingdomId", resolvedKingdomId, "phase", currentState.currentPhase().name()));
        if (phaseChanged) {
            events.add("KingdomClockPhaseChangedEvent");
            publish("KingdomClockPhaseChangedEvent", now, Map.of("kingdomId", resolvedKingdomId, "from", previousState.currentPhase().name(), "to", currentState.currentPhase().name()));
        }
        return new KingdomClockTickResult(resolvedKingdomId, previousState, currentState, advanced, phaseChanged, events);
    }

    public List<KingdomClockTickResult> tickAllKingdomClocks() {
        Set<String> kingdomIds = repository().knownKingdomIds();
        if (kingdomIds.isEmpty()) {
            kingdomIds = Set.of(DEFAULT_KINGDOM_ID);
        }
        return kingdomIds.stream().sorted().map(this::tickClock).toList();
    }

    public KingdomClockConfig updateClockConfig(String kingdomId, Map<String, String> updates) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        KingdomClockConfig updated = applyConfigUpdates(configFor(resolvedKingdomId), updates).forKingdom(resolvedKingdomId);
        repository().saveClockConfig(updated);
        publish("KingdomClockConfigUpdatedEvent", getSystemClock().instant(), Map.of("kingdomId", resolvedKingdomId));
        return updated;
    }

    public KingdomClockState setTimeOverride(String kingdomId, LocalTime time) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        KingdomClockConfig config = configFor(resolvedKingdomId).withMode(KingdomClockMode.FIXED_OVERRIDE);
        repository().saveClockConfig(config);
        KingdomClockState previousState = ensureState(resolvedKingdomId, getSystemClock().instant());
        KingdomClockState updated = stateForClock(previousState, config, time.getHour(), time.getMinute(), Optional.of(time), getSystemClock().instant());
        repository().saveClockState(updated);
        publish("KingdomClockOverrideSetEvent", getSystemClock().instant(), Map.of("kingdomId", resolvedKingdomId, "time", time.toString()));
        return updated;
    }

    public KingdomClockState clearTimeOverride(String kingdomId) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        KingdomClockConfig config = configFor(resolvedKingdomId);
        KingdomClockMode nextMode = config.clockMode() == KingdomClockMode.FIXED_OVERRIDE ? KingdomClockMode.REAL_TIME_SYNCED : config.clockMode();
        repository().saveClockConfig(config.withMode(nextMode));
        KingdomClockState previousState = ensureState(resolvedKingdomId, getSystemClock().instant());
        KingdomClockState updated = new KingdomClockState(resolvedKingdomId, nextMode, previousState.currentKingdomDay(), previousState.currentHour(),
                previousState.currentMinute(), previousState.currentPhase(), previousState.currentEpochMinute(), previousState.timezoneId(),
                previousState.realTimeSource(), Optional.empty(), previousState.lastTickAt(), getSystemClock().instant(), previousState.metadata());
        repository().saveClockState(updated);
        publish("KingdomClockOverrideClearedEvent", getSystemClock().instant(), Map.of("kingdomId", resolvedKingdomId));
        return tickClock(resolvedKingdomId).currentState();
    }

    public KingdomClockState pauseClock(String kingdomId) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        repository().saveClockConfig(configFor(resolvedKingdomId).withMode(KingdomClockMode.PAUSED));
        KingdomClockState updated = ensureState(resolvedKingdomId, getSystemClock().instant()).withMode(KingdomClockMode.PAUSED, getSystemClock().instant());
        repository().saveClockState(updated);
        publish("KingdomClockModeChangedEvent", getSystemClock().instant(), Map.of("kingdomId", resolvedKingdomId, "mode", KingdomClockMode.PAUSED.name()));
        return updated;
    }

    public KingdomClockState resumeClock(String kingdomId) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        repository().saveClockConfig(configFor(resolvedKingdomId).withMode(KingdomClockMode.REAL_TIME_SYNCED));
        KingdomClockState updated = ensureState(resolvedKingdomId, getSystemClock().instant()).withMode(KingdomClockMode.REAL_TIME_SYNCED, getSystemClock().instant());
        repository().saveClockState(updated);
        publish("KingdomClockModeChangedEvent", getSystemClock().instant(), Map.of("kingdomId", resolvedKingdomId, "mode", KingdomClockMode.REAL_TIME_SYNCED.name()));
        return tickClock(resolvedKingdomId).currentState();
    }

    public boolean isWithinWindow(String kingdomId, KingdomScheduleWindow window) {
        KingdomClockState state = getCurrentClockState(kingdomId);
        return getKingdomScheduleWindowContainmentHandler().isWithinWindow(state, window);
    }

    public List<KingdomScheduleRule> getActiveRules(String kingdomId) {
        KingdomClockState state = getCurrentClockState(kingdomId);
        return repository().findScheduleRules(kingdomIdOrDefault(kingdomId)).stream()
                .filter(KingdomScheduleRule::enabled)
                .filter(rule -> rule.kingdomId().isEmpty() || rule.kingdomId().orElseThrow().equals(state.kingdomId()))
                .filter(rule -> getKingdomScheduleWindowContainmentHandler().isWithinWindow(state, rule.window()))
                .sorted(Comparator.comparingInt(KingdomScheduleRule::priority).reversed())
                .toList();
    }

    public KingdomScheduleRule createScheduleRule(String kingdomId, KingdomScheduleRuleType ruleType, KingdomScheduleWindow window, int priority, Map<String, String> payload) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        Instant now = getSystemClock().instant();
        KingdomScheduleRule rule = new KingdomScheduleRule(
                "schedule-rule-" + UUID.randomUUID(),
                Optional.of(resolvedKingdomId),
                ruleType,
                ruleType.name().replace('_', ' '),
                true,
                window,
                priority,
                KingdomScheduleTargetScope.KINGDOM,
                Optional.empty(),
                Optional.empty(),
                KingdomScheduledEffectType.UPDATE_PROJECTION,
                payload,
                now,
                now,
                Map.of()
        );
        repository().saveScheduleRule(rule);
        publish("KingdomScheduleRuleCreatedEvent", now, Map.of("kingdomId", resolvedKingdomId, "scheduleRuleId", rule.scheduleRuleId()));
        return rule;
    }

    public KingdomScheduleRule setScheduleRuleEnabled(String scheduleRuleId, boolean enabled) {
        KingdomScheduleRule current = repository().findScheduleRule(scheduleRuleId)
                .orElseThrow(() -> new ControlCommandValidationException("Schedule rule was not found: " + scheduleRuleId + "."));
        KingdomScheduleRule updated = current.withEnabled(enabled, getSystemClock().instant());
        repository().saveScheduleRule(updated);
        publish(enabled ? "KingdomScheduleRuleActivatedEvent" : "KingdomScheduleRuleDeactivatedEvent", getSystemClock().instant(), Map.of("scheduleRuleId", scheduleRuleId));
        return updated;
    }

    public KingdomScheduledStateChangeResult applyScheduledStateChanges(String kingdomId) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        KingdomClockState state = getCurrentClockState(resolvedKingdomId);
        List<KingdomScheduleRule> activeRules = getActiveRules(resolvedKingdomId);
        List<String> effects = activeRules.stream()
                .map(rule -> rule.effectType().name() + ":" + rule.ruleType().name())
                .toList();
        publish("KingdomScheduledStateChangeAppliedEvent", getSystemClock().instant(), Map.of("kingdomId", resolvedKingdomId, "effectCount", Integer.toString(effects.size())));
        return new KingdomScheduledStateChangeResult(resolvedKingdomId, state.currentEpochMinute(), activeRules, effects, Map.of("source", "control-plane"));
    }

    public AgingTickPolicy updateAgingTickPolicy(String kingdomId, Map<String, String> updates) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        AgingTickPolicy policy = repository().findAgingTickPolicy(resolvedKingdomId).orElse(AgingTickPolicy.defaults().forKingdom(resolvedKingdomId));
        policy = applyAgingPolicyUpdates(policy, updates);
        repository().saveAgingTickPolicy(policy);
        return policy;
    }

    public AgingTickResult runAgingTick(String kingdomId) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        KingdomClockState state = getCurrentClockState(resolvedKingdomId);
        AgingTickPolicy policy = repository().findAgingTickPolicy(resolvedKingdomId).orElse(AgingTickPolicy.defaults().forKingdom(resolvedKingdomId));
        Long lastEpochMinute = repository().findLastAgingTickEpochMinute(resolvedKingdomId).orElse(null);
        if (!policy.enabled()) {
            return new AgingTickResult(resolvedKingdomId, state.currentEpochMinute(), 0, 0, List.of(), Map.of("skippedReason", "disabled"));
        }
        if (lastEpochMinute != null && state.currentEpochMinute() - lastEpochMinute < policy.realMinutesPerAgeIncrement()) {
            return new AgingTickResult(resolvedKingdomId, state.currentEpochMinute(), 0, 0, List.of(), Map.of("skippedReason", "interval-not-reached"));
        }
        repository().saveLastAgingTickEpochMinute(resolvedKingdomId, state.currentEpochMinute());
        List<String> events = List.of("KingdomAgingTickCompletedEvent");
        publish("KingdomAgingTickCompletedEvent", getSystemClock().instant(), Map.of("kingdomId", resolvedKingdomId, "epochMinute", Long.toString(state.currentEpochMinute())));
        return new AgingTickResult(resolvedKingdomId, state.currentEpochMinute(), policy.appliesToCitizens() ? 1 : 0, policy.appliesToCompanions() ? 1 : 0, events, Map.of("ageIncrementAmount", Integer.toString(policy.ageIncrementAmount())));
    }

    public CitizenScheduledState evaluateCitizenScheduledState(String kingdomId, KingdomScheduleWindow workWindow, KingdomScheduleWindow sleepWindow) {
        KingdomClockState state = getCurrentClockState(kingdomId);
        if (getKingdomScheduleWindowContainmentHandler().isWithinWindow(state, sleepWindow)) {
            return CitizenScheduledState.SLEEPING;
        }
        if (getKingdomScheduleWindowContainmentHandler().isWithinWindow(state, workWindow)) {
            return CitizenScheduledState.WORKING;
        }
        return CitizenScheduledState.FREE_TIME;
    }

    public TownScheduleState evaluateTownScheduleState(String kingdomId, String townId) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        KingdomClockState state = getCurrentClockState(resolvedKingdomId);
        List<KingdomScheduleRule> activeRules = getActiveRules(resolvedKingdomId);
        boolean shopsOpen = activeRules.stream().anyMatch(rule -> rule.ruleType() == KingdomScheduleRuleType.SHOP_OPEN);
        List<KingdomScheduleWindow> activeWindows = activeRules.stream().map(KingdomScheduleRule::window).toList();
        return new TownScheduleState(townId, resolvedKingdomId, shopsOpen, visualMoodFor(state), activeWindows, getSystemClock().instant(), Map.of());
    }

    public InteriorScheduleState evaluateInteriorScheduleState(String kingdomId, String interiorId) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        KingdomClockState state = getCurrentClockState(resolvedKingdomId);
        boolean lightsEnabled = state.isNight();
        return new InteriorScheduleState(interiorId, resolvedKingdomId, lightsEnabled, lightsEnabled ? "RESTING" : "ACTIVE", visualMoodFor(state), getSystemClock().instant(), Map.of());
    }

    public TroopTrainingScheduleModifier evaluateTroopTrainingModifier(String kingdomId) {
        KingdomClockState state = getCurrentClockState(kingdomId);
        boolean active = getActiveRules(kingdomId).stream().anyMatch(rule -> rule.ruleType() == KingdomScheduleRuleType.TROOP_TRAINING);
        return new TroopTrainingScheduleModifier(state.kingdomId(), active, state.currentPhase(), active ? 1.15d : 1.0d, active ? "training-window" : "outside-training-window", Map.of());
    }

    public BuildingProductivityScheduleModifier evaluateBuildingProductivityModifier(String kingdomId, Optional<String> buildingId) {
        KingdomClockState state = getCurrentClockState(kingdomId);
        boolean active = getActiveRules(kingdomId).stream().anyMatch(rule -> rule.ruleType() == KingdomScheduleRuleType.BUILDING_PRODUCTIVITY);
        return new BuildingProductivityScheduleModifier(state.kingdomId(), buildingId, active, active ? 1.0d : 0.65d, active ? "working-hours" : "off-hours", Map.of());
    }

    public MoraleScheduleModifier evaluateMoraleModifier(String kingdomId) {
        KingdomClockState state = getCurrentClockState(kingdomId);
        boolean penalty = state.isNight() && getActiveRules(kingdomId).stream().anyMatch(rule -> rule.ruleType() == KingdomScheduleRuleType.CITIZEN_JOB_SHIFT);
        return new MoraleScheduleModifier(state.kingdomId(), KingdomScheduleTargetScope.KINGDOM, penalty ? -10 : 0, penalty ? "labor-overlaps-sleep-window" : "normal-schedule", penalty, Map.of());
    }

    public KingdomClockProjection projectClockState(String kingdomId, GamePlatform platform) {
        KingdomClockState state = getCurrentClockState(kingdomId);
        List<KingdomScheduleRule> activeRules = getActiveRules(state.kingdomId());
        return new KingdomClockProjection(
                state.kingdomId(),
                state.currentKingdomDay(),
                state.currentHour(),
                state.currentMinute(),
                state.currentPhase(),
                state.isDay(),
                state.isNight(),
                state.clockMode(),
                state.timezoneId().orElse("UTC"),
                activeRules,
                visualMoodFor(state),
                Map.of(
                        "platform", platform.name(),
                        "globalAssetId", globalAssetForPhase(state.currentPhase()),
                        "canonicalOwner", "plain-java-control-server"
                )
        );
    }

    public KingdomScheduleProjection projectScheduleState(String kingdomId, GamePlatform platform) {
        KingdomClockState state = getCurrentClockState(kingdomId);
        List<KingdomScheduleRule> activeRules = getActiveRules(state.kingdomId());
        List<KingdomScheduleWindow> activeWindows = activeRules.stream().map(KingdomScheduleRule::window).toList();
        return new KingdomScheduleProjection(
                state.kingdomId(),
                activeWindows,
                List.of(evaluateCitizenScheduledState(state.kingdomId(), workWindow(), sleepWindow()).name()),
                List.of(evaluateTownScheduleState(state.kingdomId(), "town.default").currentMood().name()),
                List.of(evaluateInteriorScheduleState(state.kingdomId(), "interior.default").activeMood().name()),
                List.of(activeRules.stream().anyMatch(rule -> rule.ruleType() == KingdomScheduleRuleType.SHOP_OPEN) ? "OPEN" : "CLOSED"),
                List.of(evaluateTroopTrainingModifier(state.kingdomId()).reason()),
                List.of(evaluateBuildingProductivityModifier(state.kingdomId(), Optional.empty()).reason()),
                List.of(evaluateMoraleModifier(state.kingdomId()).reason()),
                Map.of("platform", platform.name(), "canonicalOwner", "plain-java-control-server")
        );
    }

    public ControlCommandResult handleControlCommand(ControlCommand command, Instant startedAt) {
        try {
            return switch (command.commandType()) {
                case GET_KINGDOM_CLOCK_STATE, DEBUG_KINGDOM_CLOCK -> result(command, startedAt, false, "Clock state: " + describe(getCurrentClockState(argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID))), List.of(), Map.of());
                case TICK_KINGDOM_CLOCK -> clockTickCommand(command, startedAt);
                case TICK_ALL_KINGDOM_CLOCKS -> tickAllCommand(command, startedAt);
                case SET_KINGDOM_CLOCK_MODE, PAUSE_KINGDOM_CLOCK, RESUME_KINGDOM_CLOCK -> modeCommand(command, startedAt);
                case SET_KINGDOM_TIME_OVERRIDE -> overrideCommand(command, startedAt);
                case CLEAR_KINGDOM_TIME_OVERRIDE -> clearOverrideCommand(command, startedAt);
                case UPDATE_KINGDOM_CLOCK_CONFIG -> configCommand(command, startedAt);
                case CREATE_KINGDOM_SCHEDULE_RULE -> createScheduleRuleCommand(command, startedAt);
                case ENABLE_KINGDOM_SCHEDULE_RULE, DISABLE_KINGDOM_SCHEDULE_RULE -> scheduleEnableCommand(command, startedAt);
                case LIST_ACTIVE_KINGDOM_SCHEDULE_RULES, DEBUG_KINGDOM_SCHEDULE -> activeRulesCommand(command, startedAt);
                case APPLY_KINGDOM_SCHEDULED_STATE_CHANGES -> scheduleApplyCommand(command, startedAt);
                case UPDATE_AGING_TICK_POLICY -> agingPolicyCommand(command, startedAt);
                case RUN_AGING_TICK, DEBUG_AGING_TICK -> agingTickCommand(command, startedAt);
                case REFRESH_KINGDOM_CLOCK_PROJECTION -> projectionCommand(command, startedAt, true);
                case REFRESH_KINGDOM_SCHEDULE_PROJECTION -> projectionCommand(command, startedAt, false);
                default -> result(command, startedAt, false, "Unsupported clock command: " + command.commandType(), List.of(), Map.of());
            };
        } catch (RuntimeException exception) {
            return new ControlCommandResult(command.commandId(), CommandExecutionState.FAILED, false, exception.getMessage(), List.of(), List.of(), List.of(exception.getMessage()), startedAt, getSystemClock().instant(), Map.of());
        }
    }

    private ControlCommandResult clockTickCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        if (command.dryRun()) {
            return result(command, startedAt, true, "Clock tick dry-run accepted for " + kingdomId + ".", List.of("kingdom-clock:" + kingdomId), Map.of("dryRun", "true"));
        }
        KingdomClockTickResult tickResult = tickClock(kingdomId);
        return result(command, startedAt, true, "Clock ticked: " + describe(tickResult.currentState()), List.of("kingdom-clock:" + kingdomId), Map.of("phaseChanged", Boolean.toString(tickResult.phaseChanged())));
    }

    private ControlCommandResult tickAllCommand(ControlCommand command, Instant startedAt) {
        if (command.dryRun()) {
            return result(command, startedAt, true, "All kingdom clock tick dry-run accepted.", List.of(), Map.of("dryRun", "true"));
        }
        List<KingdomClockTickResult> results = tickAllKingdomClocks();
        return result(command, startedAt, true, "Ticked " + results.size() + " kingdom clocks.", results.stream().map(result -> "kingdom-clock:" + result.kingdomId()).toList(), Map.of("count", Integer.toString(results.size())));
    }

    private ControlCommandResult modeCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        KingdomClockMode mode = switch (command.commandType()) {
            case PAUSE_KINGDOM_CLOCK -> KingdomClockMode.PAUSED;
            case RESUME_KINGDOM_CLOCK -> KingdomClockMode.REAL_TIME_SYNCED;
            default -> KingdomClockMode.valueOf(command.argument("mode").toUpperCase());
        };
        if (command.dryRun()) {
            return result(command, startedAt, true, "Clock mode can be set to " + mode + ".", List.of("kingdom-clock:" + kingdomId), Map.of("dryRun", "true"));
        }
        repository().saveClockConfig(configFor(kingdomId).withMode(mode));
        KingdomClockState updated = ensureState(kingdomId, getSystemClock().instant()).withMode(mode, getSystemClock().instant());
        repository().saveClockState(updated);
        publish("KingdomClockModeChangedEvent", getSystemClock().instant(), Map.of("kingdomId", kingdomId, "mode", mode.name()));
        return result(command, startedAt, true, "Clock mode updated: " + mode + ".", List.of("kingdom-clock:" + kingdomId), Map.of("mode", mode.name()));
    }

    private ControlCommandResult overrideCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        LocalTime time = LocalTime.parse(command.argument("time"));
        if (command.dryRun()) {
            return result(command, startedAt, true, "Time override can be set to " + time + ".", List.of("kingdom-clock:" + kingdomId), Map.of("dryRun", "true"));
        }
        KingdomClockState state = setTimeOverride(kingdomId, time);
        return result(command, startedAt, true, "Time override set: " + describe(state), List.of("kingdom-clock:" + kingdomId), Map.of("time", time.toString()));
    }

    private ControlCommandResult clearOverrideCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        if (command.dryRun()) {
            return result(command, startedAt, true, "Time override can be cleared.", List.of("kingdom-clock:" + kingdomId), Map.of("dryRun", "true"));
        }
        KingdomClockState state = clearTimeOverride(kingdomId);
        return result(command, startedAt, true, "Time override cleared: " + describe(state), List.of("kingdom-clock:" + kingdomId), Map.of());
    }

    private ControlCommandResult configCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        if (command.dryRun()) {
            applyConfigUpdates(configFor(kingdomId), command.arguments());
            return result(command, startedAt, true, "Clock config update dry-run passed.", List.of("kingdom-clock-config:" + kingdomId), Map.of("dryRun", "true"));
        }
        KingdomClockConfig config = updateClockConfig(kingdomId, command.arguments());
        return result(command, startedAt, true, "Clock config updated for " + kingdomId + ".", List.of("kingdom-clock-config:" + kingdomId), Map.of("mode", config.clockMode().name()));
    }

    private ControlCommandResult createScheduleRuleCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        KingdomScheduleRuleType ruleType = KingdomScheduleRuleType.valueOf(argumentOrDefault(command, "ruleType", "CUSTOM").toUpperCase());
        KingdomScheduleWindow window = new KingdomScheduleWindow(
                argumentOrDefault(command, "windowId", "window-" + UUID.randomUUID()),
                argumentOrDefault(command, "displayName", ruleType.name()),
                Integer.parseInt(argumentOrDefault(command, "startHour", "8")),
                Integer.parseInt(argumentOrDefault(command, "startMinute", "0")),
                Integer.parseInt(argumentOrDefault(command, "endHour", "17")),
                Integer.parseInt(argumentOrDefault(command, "endMinute", "0")),
                Set.of(),
                Boolean.parseBoolean(argumentOrDefault(command, "wrapsMidnight", "false")),
                Map.of()
        );
        if (command.dryRun()) {
            return result(command, startedAt, true, "Schedule rule dry-run passed for " + ruleType + ".", List.of("kingdom-schedule:" + kingdomId), Map.of("dryRun", "true"));
        }
        KingdomScheduleRule rule = createScheduleRule(kingdomId, ruleType, window, Integer.parseInt(argumentOrDefault(command, "priority", "100")), command.arguments());
        return result(command, startedAt, true, "Schedule rule created: " + rule.scheduleRuleId() + ".", List.of(rule.scheduleRuleId()), Map.of("ruleType", rule.ruleType().name()));
    }

    private ControlCommandResult scheduleEnableCommand(ControlCommand command, Instant startedAt) {
        String ruleId = command.argument("scheduleRuleId");
        boolean enabled = command.commandType().name().startsWith("ENABLE");
        if (command.dryRun()) {
            return result(command, startedAt, true, "Schedule rule enabled flag can be changed.", List.of(ruleId), Map.of("dryRun", "true"));
        }
        KingdomScheduleRule rule = setScheduleRuleEnabled(ruleId, enabled);
        return result(command, startedAt, true, "Schedule rule " + (enabled ? "enabled" : "disabled") + ": " + ruleId + ".", List.of(ruleId), Map.of("enabled", Boolean.toString(rule.enabled())));
    }

    private ControlCommandResult activeRulesCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        List<KingdomScheduleRule> activeRules = getActiveRules(kingdomId);
        return result(command, startedAt, false, "Active schedule rules: " + activeRules.size() + ".", activeRules.stream().map(KingdomScheduleRule::scheduleRuleId).toList(), Map.of("count", Integer.toString(activeRules.size())));
    }

    private ControlCommandResult scheduleApplyCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        if (command.dryRun()) {
            return result(command, startedAt, true, "Scheduled state changes can be applied.", List.of("kingdom-schedule:" + kingdomId), Map.of("dryRun", "true"));
        }
        KingdomScheduledStateChangeResult applied = applyScheduledStateChanges(kingdomId);
        return result(command, startedAt, true, "Applied scheduled effects: " + applied.appliedEffects().size() + ".", List.of("kingdom-schedule:" + kingdomId), Map.of("effectCount", Integer.toString(applied.appliedEffects().size())));
    }

    private ControlCommandResult agingPolicyCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        if (command.dryRun()) {
            applyAgingPolicyUpdates(repository().findAgingTickPolicy(kingdomId).orElse(AgingTickPolicy.defaults().forKingdom(kingdomId)), command.arguments());
            return result(command, startedAt, true, "Aging policy dry-run passed.", List.of("aging-policy:" + kingdomId), Map.of("dryRun", "true"));
        }
        AgingTickPolicy policy = updateAgingTickPolicy(kingdomId, command.arguments());
        return result(command, startedAt, true, "Aging policy updated.", List.of("aging-policy:" + kingdomId), Map.of("enabled", Boolean.toString(policy.enabled())));
    }

    private ControlCommandResult agingTickCommand(ControlCommand command, Instant startedAt) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        if (command.dryRun()) {
            return result(command, startedAt, true, "Aging tick dry-run accepted.", List.of("aging:" + kingdomId), Map.of("dryRun", "true"));
        }
        AgingTickResult tick = runAgingTick(kingdomId);
        return result(command, startedAt, true, "Aging tick evaluated: citizens=" + tick.affectedCitizenCount() + ".", List.of("aging:" + kingdomId), tick.metadata());
    }

    private ControlCommandResult projectionCommand(ControlCommand command, Instant startedAt, boolean clockProjection) {
        String kingdomId = argumentOrDefault(command, "kingdomId", DEFAULT_KINGDOM_ID);
        GamePlatform platform = GamePlatform.valueOf(argumentOrDefault(command, "platform", "HYTALE").toUpperCase());
        Map<String, String> metadata = clockProjection ? projectClockState(kingdomId, platform).metadata() : projectScheduleState(kingdomId, platform).metadata();
        return result(command, startedAt, false, (clockProjection ? "Clock" : "Schedule") + " projection refreshed for " + platform + ".", List.of("kingdom-clock:" + kingdomId), metadata);
    }

    private KingdomClockState ensureState(String kingdomId, Instant now) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        Optional<KingdomClockState> existing = repository().findClockState(resolvedKingdomId);
        if (existing.isPresent()) {
            return existing.orElseThrow();
        }
        KingdomClockConfig config = configFor(resolvedKingdomId);
        KingdomClockState state = stateForRealTime(newSeedState(resolvedKingdomId, config, now), config, now);
        repository().saveClockState(state);
        return state;
    }

    private KingdomClockConfig configFor(String kingdomId) {
        String resolvedKingdomId = kingdomIdOrDefault(kingdomId);
        return repository().findClockConfig(resolvedKingdomId).orElseGet(() -> {
            KingdomClockConfig config = KingdomClockConfig.defaults().forKingdom(resolvedKingdomId);
            repository().saveClockConfig(config);
            return config;
        });
    }

    private KingdomClockState newSeedState(String kingdomId, KingdomClockConfig config, Instant now) {
        return new KingdomClockState(kingdomId, config.clockMode(), 1, 0, 0, KingdomTimePhase.NIGHT, 0,
                Optional.of(config.timezoneId()), KingdomClockRealTimeSource.CONFIGURED_TIMEZONE, Optional.empty(), now, now, Map.of("createdBy", "KingdomClockStateCreationHandler"));
    }

    private KingdomClockState stateForOverride(KingdomClockState previousState, KingdomClockConfig config, Instant now) {
        LocalTime override = previousState.timeOverride().orElse(LocalTime.of(previousState.currentHour(), previousState.currentMinute()));
        return stateForClock(previousState, config, override.getHour(), override.getMinute(), Optional.of(override), now);
    }

    private KingdomClockState stateForRealTime(KingdomClockState previousState, KingdomClockConfig config, Instant now) {
        ZoneId zoneId = config.useServerLocalTimezone() ? ZoneId.systemDefault() : ZoneId.of(config.timezoneId());
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(now, zoneId);
        long epochMinute = now.getEpochSecond() / 60L;
        long day = Math.max(1L, epochMinute / config.realMinutesPerKingdomDay() + 1L);
        return new KingdomClockState(previousState.kingdomId(), config.clockMode(), day, zonedDateTime.getHour(), zonedDateTime.getMinute(),
                getKingdomClockPhaseCalculationHandler().calculatePhase(zonedDateTime.getHour(), config), epochMinute, Optional.of(zoneId.getId()),
                config.useServerLocalTimezone() ? KingdomClockRealTimeSource.SERVER_LOCAL_TIME : KingdomClockRealTimeSource.CONFIGURED_TIMEZONE,
                Optional.empty(), now, now, previousState.metadata());
    }

    private KingdomClockState stateForAccelerated(KingdomClockState previousState, KingdomClockConfig config, Instant now) {
        long elapsedMillis = Math.max(0L, Duration.between(previousState.lastTickAt(), now).toMillis());
        long acceleratedMinutes = (long) Math.floor((elapsedMillis / 60000.0d) * config.acceleratedTimeMultiplier());
        if (acceleratedMinutes <= 0L) {
            return previousState.withMode(KingdomClockMode.ACCELERATED, now);
        }
        long epochMinute = previousState.currentEpochMinute() + acceleratedMinutes;
        long day = Math.max(1L, epochMinute / config.realMinutesPerKingdomDay() + 1L);
        int minuteOfDay = (int) Math.floorMod(epochMinute, 24L * 60L);
        int hour = minuteOfDay / 60;
        int minute = minuteOfDay % 60;
        return stateForClock(previousState, config, day, hour, minute, epochMinute, Optional.empty(), now);
    }

    private KingdomClockState stateForClock(KingdomClockState previousState, KingdomClockConfig config, int hour, int minute, Optional<LocalTime> override, Instant now) {
        long epochMinute = (previousState.currentKingdomDay() - 1L) * 24L * 60L + hour * 60L + minute;
        return stateForClock(previousState, config, previousState.currentKingdomDay(), hour, minute, epochMinute, override, now);
    }

    private KingdomClockState stateForClock(KingdomClockState previousState, KingdomClockConfig config, long day, int hour, int minute, long epochMinute, Optional<LocalTime> override, Instant now) {
        return new KingdomClockState(previousState.kingdomId(), config.clockMode(), day, hour, minute, getKingdomClockPhaseCalculationHandler().calculatePhase(hour, config),
                epochMinute, Optional.of(config.timezoneId()), KingdomClockRealTimeSource.CONFIGURED_TIMEZONE, override, now, now, previousState.metadata());
    }

    private KingdomClockConfig applyConfigUpdates(KingdomClockConfig config, Map<String, String> updates) {
        KingdomClockConfig updated = config;
        if (updates.containsKey("mode")) {
            updated = updated.withMode(KingdomClockMode.valueOf(updates.get("mode").toUpperCase()));
        }
        if (updates.containsKey("clockMode")) {
            updated = updated.withMode(KingdomClockMode.valueOf(updates.get("clockMode").toUpperCase()));
        }
        if (updates.containsKey("timezoneId")) {
            updated = updated.withTimezone(updates.get("timezoneId"));
        }
        if (updates.containsKey("realMinutesPerKingdomDay")) {
            updated = updated.withRealMinutesPerKingdomDay(Integer.parseInt(updates.get("realMinutesPerKingdomDay")));
        }
        if (updates.containsKey("acceleratedTimeMultiplier")) {
            updated = updated.withAcceleratedTimeMultiplier(Double.parseDouble(updates.get("acceleratedTimeMultiplier")));
        }
        int dawn = updates.containsKey("dawnStartHour") ? Integer.parseInt(updates.get("dawnStartHour")) : updated.dawnStartHour();
        int day = updates.containsKey("dayStartHour") ? Integer.parseInt(updates.get("dayStartHour")) : updated.dayStartHour();
        int dusk = updates.containsKey("duskStartHour") ? Integer.parseInt(updates.get("duskStartHour")) : updated.duskStartHour();
        int night = updates.containsKey("nightStartHour") ? Integer.parseInt(updates.get("nightStartHour")) : updated.nightStartHour();
        updated = updated.withPhaseStarts(dawn, day, dusk, night);
        if (updates.containsKey("agingTickIntervalMinutes")) {
            updated = updated.withAgingTickIntervalMinutes(Integer.parseInt(updates.get("agingTickIntervalMinutes")));
        }
        return updated;
    }

    private AgingTickPolicy applyAgingPolicyUpdates(AgingTickPolicy policy, Map<String, String> updates) {
        AgingTickPolicy updated = policy;
        if (updates.containsKey("enabled")) {
            updated = updated.withEnabled(Boolean.parseBoolean(updates.get("enabled")));
        }
        if (updates.containsKey("realMinutesPerAgeIncrement")) {
            updated = updated.withRealMinutesPerAgeIncrement(Integer.parseInt(updates.get("realMinutesPerAgeIncrement")));
        }
        return updated;
    }

    private KingdomVisualMood visualMoodFor(KingdomClockState state) {
        return switch (state.currentPhase()) {
            case DAWN -> KingdomVisualMood.DAWN;
            case DAY -> KingdomVisualMood.BRIGHT_DAY;
            case DUSK -> KingdomVisualMood.EVENING;
            case NIGHT -> KingdomVisualMood.DARK_NIGHT;
        };
    }

    private String globalAssetForPhase(KingdomTimePhase phase) {
        return switch (phase) {
            case DAWN -> "kingdom_clock.phase.dawn";
            case DAY -> "kingdom_clock.phase.day";
            case DUSK -> "kingdom_clock.phase.dusk";
            case NIGHT -> "kingdom_clock.phase.night";
        };
    }

    private KingdomScheduleWindow workWindow() {
        return KingdomScheduleWindow.of("citizen.work", "Citizen Work", 8, 17);
    }

    private KingdomScheduleWindow sleepWindow() {
        return new KingdomScheduleWindow("citizen.sleep", "Citizen Sleep", 22, 0, 6, 0, Set.of(KingdomTimePhase.NIGHT), true, Map.of());
    }

    private void seedDefaultScheduleRules(String kingdomId, Instant now) {
        saveSeedRule(kingdomId, KingdomScheduleRuleType.CITIZEN_JOB_SHIFT, workWindow(), 100, now);
        saveSeedRule(kingdomId, KingdomScheduleRuleType.SHOP_OPEN, KingdomScheduleWindow.of("shop.open", "Shop Open", 8, 20), 95, now);
        saveSeedRule(kingdomId, KingdomScheduleRuleType.BUILDING_PRODUCTIVITY, KingdomScheduleWindow.of("building.productivity", "Building Productivity", 7, 21), 90, now);
        saveSeedRule(kingdomId, KingdomScheduleRuleType.TROOP_TRAINING, KingdomScheduleWindow.of("troop.training", "Troop Training", 6, 18), 80, now);
        saveSeedRule(kingdomId, KingdomScheduleRuleType.NIGHT_EVENT_WINDOW, new KingdomScheduleWindow("night.events", "Night Events", 21, 0, 5, 0, Set.of(KingdomTimePhase.NIGHT), true, Map.of()), 70, now);
    }

    private void saveSeedRule(String kingdomId, KingdomScheduleRuleType ruleType, KingdomScheduleWindow window, int priority, Instant now) {
        repository().saveScheduleRule(new KingdomScheduleRule(
                "default-" + kingdomId + "-" + ruleType.name().toLowerCase(),
                Optional.of(kingdomId),
                ruleType,
                ruleType.name().replace('_', ' '),
                true,
                window,
                priority,
                KingdomScheduleTargetScope.KINGDOM,
                Optional.empty(),
                Optional.empty(),
                KingdomScheduledEffectType.UPDATE_PROJECTION,
                Map.of("seeded", "true"),
                now,
                now,
                Map.of("source", "KingdomScheduleRuleCreationHandler")
        ));
    }

    private ControlCommandResult result(ControlCommand command, Instant startedAt, boolean mutated, String message, List<String> changedObjectIds, Map<String, String> metadata) {
        Map<String, String> resultMetadata = new LinkedHashMap<>(metadata);
        resultMetadata.put("mutatedCanonicalState", Boolean.toString(mutated && !command.dryRun()));
        resultMetadata.put("canonicalOwner", "plain-java-control-server");
        CommandExecutionState state = command.dryRun() ? CommandExecutionState.DRY_RUN_COMPLETED : CommandExecutionState.COMPLETED;
        return new ControlCommandResult(command.commandId(), state, true, message, List.of(), changedObjectIds, List.of(), startedAt, getSystemClock().instant(), resultMetadata);
    }

    private String describe(KingdomClockState state) {
        return state.kingdomId() + " day=" + state.currentKingdomDay() + " " + String.format("%02d:%02d", state.currentHour(), state.currentMinute()) + " phase=" + state.currentPhase() + " mode=" + state.clockMode();
    }

    private String argumentOrDefault(ControlCommand command, String key, String fallback) {
        String value = command.arguments().get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private String kingdomIdOrDefault(String kingdomId) {
        return kingdomId == null || kingdomId.isBlank() ? DEFAULT_KINGDOM_ID : kingdomId;
    }

    private void publish(String eventType, Instant occurredAt, Map<String, String> attributes) {
        getDomainEventPublisher().publish(new KingdomClockDomainEvent(eventType, occurredAt, attributes));
    }
}

final class KingdomClockPhaseCalculationHandler {
    KingdomTimePhase calculatePhase(int hour, KingdomClockConfig config) {
        if (hour >= config.nightStartHour() || hour < config.dawnStartHour()) {
            return KingdomTimePhase.NIGHT;
        }
        if (hour >= config.duskStartHour()) {
            return KingdomTimePhase.DUSK;
        }
        if (hour >= config.dayStartHour()) {
            return KingdomTimePhase.DAY;
        }
        return KingdomTimePhase.DAWN;
    }
}

final class KingdomScheduleWindowContainmentHandler {
    boolean isWithinWindow(KingdomClockState state, KingdomScheduleWindow window) {
        return window.contains(state.currentHour(), state.currentMinute(), state.currentPhase());
    }
}

interface KingdomClockRepository {
    Optional<KingdomClockState> findClockState(String kingdomId);

    void saveClockState(KingdomClockState state);

    Optional<KingdomClockConfig> findClockConfig(String kingdomId);

    void saveClockConfig(KingdomClockConfig config);

    List<KingdomScheduleRule> findScheduleRules(String kingdomId);

    Optional<KingdomScheduleRule> findScheduleRule(String scheduleRuleId);

    void saveScheduleRule(KingdomScheduleRule rule);

    Optional<AgingTickPolicy> findAgingTickPolicy(String kingdomId);

    void saveAgingTickPolicy(AgingTickPolicy policy);

    Optional<Long> findLastAgingTickEpochMinute(String kingdomId);

    void saveLastAgingTickEpochMinute(String kingdomId, long epochMinute);

    Set<String> knownKingdomIds();
}

final class InMemoryKingdomClockRepository implements KingdomClockRepository {
    private final Map<String, KingdomClockState> statesByKingdomId = new ConcurrentHashMap<>();
    private final Map<String, KingdomClockConfig> configsByKingdomId = new ConcurrentHashMap<>();
    private final Map<String, KingdomScheduleRule> scheduleRulesById = new ConcurrentHashMap<>();
    private final Map<String, AgingTickPolicy> agingPoliciesByKingdomId = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAgingTickByKingdomId = new ConcurrentHashMap<>();

    @Override
    public Optional<KingdomClockState> findClockState(String kingdomId) {
        return Optional.ofNullable(statesByKingdomId.get(kingdomId));
    }

    @Override
    public void saveClockState(KingdomClockState state) {
        statesByKingdomId.put(state.kingdomId(), state);
    }

    @Override
    public Optional<KingdomClockConfig> findClockConfig(String kingdomId) {
        return Optional.ofNullable(configsByKingdomId.get(kingdomId));
    }

    @Override
    public void saveClockConfig(KingdomClockConfig config) {
        config.kingdomId().ifPresent(kingdomId -> configsByKingdomId.put(kingdomId, config));
    }

    @Override
    public List<KingdomScheduleRule> findScheduleRules(String kingdomId) {
        return scheduleRulesById.values().stream()
                .filter(rule -> rule.kingdomId().isEmpty() || rule.kingdomId().orElseThrow().equals(kingdomId))
                .toList();
    }

    @Override
    public Optional<KingdomScheduleRule> findScheduleRule(String scheduleRuleId) {
        return Optional.ofNullable(scheduleRulesById.get(scheduleRuleId));
    }

    @Override
    public void saveScheduleRule(KingdomScheduleRule rule) {
        scheduleRulesById.put(rule.scheduleRuleId(), rule);
    }

    @Override
    public Optional<AgingTickPolicy> findAgingTickPolicy(String kingdomId) {
        return Optional.ofNullable(agingPoliciesByKingdomId.get(kingdomId));
    }

    @Override
    public void saveAgingTickPolicy(AgingTickPolicy policy) {
        policy.kingdomId().ifPresent(kingdomId -> agingPoliciesByKingdomId.put(kingdomId, policy));
    }

    @Override
    public Optional<Long> findLastAgingTickEpochMinute(String kingdomId) {
        return Optional.ofNullable(lastAgingTickByKingdomId.get(kingdomId));
    }

    @Override
    public void saveLastAgingTickEpochMinute(String kingdomId, long epochMinute) {
        lastAgingTickByKingdomId.put(kingdomId, epochMinute);
    }

    @Override
    public Set<String> knownKingdomIds() {
        return Set.copyOf(statesByKingdomId.keySet());
    }
}

record KingdomClockDomainEvent(String eventType, Instant occurredAt, Map<String, String> attributes) implements DomainEvent {
    public KingdomClockDomainEvent {
        attributes = attributes == null ? Map.of() : Map.copyOf(new HashMap<>(attributes));
    }
}

