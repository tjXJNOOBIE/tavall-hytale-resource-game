package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class FrontendCommandIngressHandler {
    private final ControlCommandParsingHandler parsingHandler;
    private final ControlCommandDispatchHandler dispatchHandler;
    private final KdControlCommandTranslationHandler kdTranslationHandler;
    private final ControlOperator operator;

    public FrontendCommandIngressHandler(
            ControlCommandParsingHandler parsingHandler,
            ControlCommandDispatchHandler dispatchHandler,
            KdControlCommandTranslationHandler kdTranslationHandler,
            ControlOperator operator
    ) {
        this.parsingHandler = parsingHandler;
        this.dispatchHandler = dispatchHandler;
        this.kdTranslationHandler = kdTranslationHandler;
        this.operator = operator;
    }

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
            Optional<String> translatedCommand = kdTranslationHandler.translateKdCommand(normalizedInput);
            if (translatedCommand.isPresent()) {
                return dispatchParsedCommand(envelope, translatedCommand.get(), issuedFrom, now);
            }
            return verifyFrontendAction(envelope, gamePlatform, issuedFrom, now, kdTranslationHandler.category(normalizedInput));
        }
        return dispatchParsedCommand(envelope, normalizedInput, issuedFrom, now);
    }

    private FrontendCommandVerificationResult dispatchParsedCommand(
            FrontendCommandEnvelope envelope,
            String controlConsoleInput,
            CommandIssuedFrom issuedFrom,
            Instant now
    ) {
        ControlCommand command = parsingHandler.parseConsoleCommand(controlConsoleInput, operator, issuedFrom, now);
        ControlCommandResult result = dispatchHandler.dispatchCommand(command);
        return new FrontendCommandVerificationResult(
                envelope,
                FrontendCommandVerificationState.DISPATCHED,
                result.success(),
                result.message(),
                result.commandId().toString(),
                result.state().name(),
                Map.of("controlConsoleInput", controlConsoleInput)
        );
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
                operator,
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
        ControlCommandResult result = dispatchHandler.dispatchCommand(command);
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
            case ANDROID, PC -> Optional.empty();
        };
    }

    private CommandIssuedFrom toIssuedFrom(ResourceGameFrontendPlatform platform) {
        return switch (platform) {
            case HYTALE -> CommandIssuedFrom.HYTALE;
            case MINECRAFT -> CommandIssuedFrom.MINECRAFT;
            case ROBLOX -> CommandIssuedFrom.ROBLOX;
            case DISCORD -> CommandIssuedFrom.DISCORD;
            case ANDROID, PC -> CommandIssuedFrom.SYSTEM;
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
