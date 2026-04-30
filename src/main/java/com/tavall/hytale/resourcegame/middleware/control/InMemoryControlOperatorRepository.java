package com.tavall.hytale.resourcegame.middleware.control;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryControlOperatorRepository implements ControlOperatorRepository {
    private final Map<UUID, ControlOperator> operatorsById = new ConcurrentHashMap<>();

    @Override
    public ControlOperator saveOperator(ControlOperator operator) {
        operatorsById.put(operator.operatorId(), operator);
        return operator;
    }

    @Override
    public Optional<ControlOperator> findOperator(UUID operatorId) {
        return Optional.ofNullable(operatorsById.get(operatorId));
    }

    @Override
    public List<ControlOperator> findOperators() {
        return operatorsById.values().stream()
                .sorted(Comparator.comparing(ControlOperator::displayName))
                .toList();
    }
}
