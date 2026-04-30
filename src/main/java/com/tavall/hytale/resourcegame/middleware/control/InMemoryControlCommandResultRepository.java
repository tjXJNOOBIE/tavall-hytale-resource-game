package com.tavall.hytale.resourcegame.middleware.control;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryControlCommandResultRepository implements ControlCommandResultRepository {
    private final Map<ControlCommandId, ControlCommandResult> resultsByCommand = new ConcurrentHashMap<>();

    @Override
    public ControlCommandResult saveResult(ControlCommandResult result) {
        resultsByCommand.put(result.commandId(), result);
        return result;
    }

    @Override
    public Optional<ControlCommandResult> findResult(ControlCommandId commandId) {
        return Optional.ofNullable(resultsByCommand.get(commandId));
    }

    @Override
    public List<ControlCommandResult> findRecentResults(int limit) {
        return resultsByCommand.values().stream()
                .sorted(Comparator.comparing(ControlCommandResult::startedAt).reversed())
                .limit(Math.max(0, limit))
                .toList();
    }
}
