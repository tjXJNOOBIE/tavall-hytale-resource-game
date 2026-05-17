package com.tavall.resourcegame.middleware.control;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationState;
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendPlatform;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class FrontendCommandIngressHandler implements IControlCommandDomain, IDependencyInjectableConcrete {
    public FrontendCommandVerificationResult ingest(FrontendCommandEnvelope envelope, Instant now) {
        Optional<GamePlatform> gamePlatform = toGamePlatform(envelope.platform());
        if (gamePlatform.isEmpty()) {
            return FrontendCommandVerificationResult.rejected(envelope, "Unsupported game frontend platform for control ingress: " + envelope.platform() + ".");
        }

        CommandIssuedFrom issuedFrom = toIssuedFrom(envelope.platform());
        try {
            if (envelope.rawInput() != null && !envelope.rawInput().isBlank()) {
                return ingestRawInput(envelope, gamePlatform.get(), issuedFrom, now);
            }
            return verifyFrontendAction(envelope, gamePlatform.get(), issuedFrom, now, categoryForAction(envelope.actionId()));
        } catch (ControlCommandValidationException exception) {
            return FrontendCommandVerificationResult.rejected(envelope, exception.getMessage());
        }
    }

    private FrontendCommandVerificationResult ingestRawInput(
            FrontendCommandEnvelope envelope,
            GamePlatform gamePlatform,
            CommandIssuedFrom issuedFrom,
            Instant now
    ) {
        String normalizedInput = normalizeRawInput(envelope.rawInput());
        String lowerInput = normalizedInput.toLowerCase(Locale.ROOT);
        if (lowerInput.startsWith("kd ") || lowerInput.equals("kd") || lowerInput.startsWith("kingdom ") || lowerInput.equals("kingdom")) {
            Optional<String> translatedCommand = getKdControlCommandTranslationHandler().translateKdCommand(normalizedInput, envelope.platformAccountId());
            if (translatedCommand.isPresent()) {
                return dispatchParsedCommand(envelope, gamePlatform, translatedCommand.get(), issuedFrom, now);
            }
            return verifyFrontendAction(envelope, gamePlatform, issuedFrom, now, getKdControlCommandTranslationHandler().category(normalizedInput));
        }
        return dispatchParsedCommand(envelope, gamePlatform, normalizedInput, issuedFrom, now);
    }

    private FrontendCommandVerificationResult dispatchParsedCommand(
            FrontendCommandEnvelope envelope,
            GamePlatform gamePlatform,
            String controlConsoleInput,
            CommandIssuedFrom issuedFrom,
            Instant now
    ) {
        ControlCommand parsedCommand = getControlCommandParsingHandler().parseConsoleCommand(controlConsoleInput, getControlOperator(), issuedFrom, now);
        ControlCommand command = withFrontendMetadata(parsedCommand, envelope, gamePlatform);
        ControlCommandResult result = getControlCommandDispatchHandler().dispatchCommand(command);
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("controlConsoleInput", controlConsoleInput);
        metadata.put("commandType", command.commandType().name());
        metadata.put("changedObjectIds", String.join(",", result.changedObjectIds()));
        instanceSwitchRequestId(result.changedObjectIds()).ifPresent(value -> metadata.put("instanceSwitchRequestId", value));
        if (command.commandType() == ControlCommandType.REQUEST_INSTANCE_SWITCH) {
            metadata.put("platform", command.arguments().getOrDefault("platform", ""));
            metadata.put("fromKingdomId", command.arguments().getOrDefault("fromKingdomId", ""));
            metadata.put("toKingdomId", command.arguments().getOrDefault("toKingdomId", ""));
            metadata.put("universalPlayerId", command.arguments().getOrDefault("universalPlayerId", ""));
        }
        return new FrontendCommandVerificationResult(
                envelope,
                FrontendCommandVerificationState.DISPATCHED,
                result.success(),
                result.message(),
                result.commandId().toString(),
                result.state().name(),
                metadata
        );
    }

    private ControlCommand withFrontendMetadata(ControlCommand command, FrontendCommandEnvelope envelope, GamePlatform gamePlatform) {
        Map<String, String> metadata = new LinkedHashMap<>(command.metadata());
        metadata.put("platform", gamePlatform.name());
        metadata.put("platformAccountId", envelope.platformAccountId() == null ? "" : envelope.platformAccountId());
        metadata.put("platformDisplayName", envelope.platformDisplayName() == null ? "" : envelope.platformDisplayName());
        metadata.put("correlationId", envelope.correlationId());
        metadata.putAll(envelope.sourceMetadata());
        return new ControlCommand(
                command.commandId(),
                command.commandType(),
                command.issuedBy(),
                command.issuedFrom(),
                command.targetScope(),
                command.targetPlatforms(),
                command.arguments(),
                command.dryRun(),
                command.createdAt(),
                metadata
        );
    }

    private Optional<String> instanceSwitchRequestId(List<String> changedObjectIds) {
        return changedObjectIds.stream()
                .filter(value -> value.startsWith("instance-switch:"))
                .map(value -> value.substring("instance-switch:".length()))
                .findFirst();
    }

    private FrontendCommandVerificationResult verifyFrontendAction(
            FrontendCommandEnvelope envelope,
            GamePlatform gamePlatform,
            CommandIssuedFrom issuedFrom,
            Instant now,
            String category
    ) {
        ControlCommand command = new ControlCommand(
                ControlCommandId.random(),
                ControlCommandType.VERIFY_FRONTEND_ACTION,
                getControlOperator(),
                issuedFrom,
                CommandTargetScope.PLATFORM,
                Set.of(gamePlatform),
                Map.of(
                        "platform", gamePlatform.name(),
                        "surface", envelope.surface().name(),
                        "category", category,
                        "input", envelope.rawInput() == null ? envelope.actionId() : envelope.rawInput()
                ),
                false,
                now,
                Map.of(
                        "platformAccountId", envelope.platformAccountId() == null ? "" : envelope.platformAccountId(),
                        "correlationId", envelope.correlationId()
                )
        );
        ControlCommandResult result = getControlCommandDispatchHandler().dispatchCommand(command);
        return new FrontendCommandVerificationResult(
                envelope,
                FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED,
                result.success(),
                result.message(),
                result.commandId().toString(),
                result.state().name(),
                Map.of("verifiedCategory", category)
        );
    }

    private Optional<GamePlatform> toGamePlatform(ResourceGameFrontendPlatform platform) {
        return switch (platform) {
            case HYTALE -> Optional.of(GamePlatform.HYTALE);
            case MINECRAFT -> Optional.of(GamePlatform.MINECRAFT);
            case ROBLOX -> Optional.of(GamePlatform.ROBLOX);
            case DISCORD -> Optional.of(GamePlatform.DISCORD);
            case ANDROID -> Optional.of(GamePlatform.ANDROID);
            case PC -> Optional.of(GamePlatform.PC);
        };
    }

    private CommandIssuedFrom toIssuedFrom(ResourceGameFrontendPlatform platform) {
        return switch (platform) {
            case HYTALE -> CommandIssuedFrom.HYTALE;
            case MINECRAFT -> CommandIssuedFrom.MINECRAFT;
            case ROBLOX -> CommandIssuedFrom.ROBLOX;
            case DISCORD -> CommandIssuedFrom.DISCORD;
            case ANDROID -> CommandIssuedFrom.ANDROID;
            case PC -> CommandIssuedFrom.PC;
        };
    }

    private String normalizeRawInput(String rawInput) {
        String normalizedInput = rawInput.trim();
        while (normalizedInput.startsWith("/")) {
            normalizedInput = normalizedInput.substring(1);
        }
        return normalizedInput;
    }

    private String categoryForAction(String actionId) {
        if (actionId == null || actionId.isBlank()) {
            return "action";
        }
        int firstSeparator = actionId.indexOf('.');
        if (firstSeparator < 0 || firstSeparator == actionId.length() - 1) {
            return "action";
        }
        int secondSeparator = actionId.indexOf('.', firstSeparator + 1);
        return secondSeparator < 0
                ? actionId.substring(firstSeparator + 1)
                : actionId.substring(firstSeparator + 1, secondSeparator);
    }
}
