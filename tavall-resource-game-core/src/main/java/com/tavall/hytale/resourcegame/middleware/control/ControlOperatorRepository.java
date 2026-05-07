package com.tavall.hytale.resourcegame.middleware.control;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ControlOperatorRepository {
    ControlOperator saveOperator(ControlOperator operator);

    Optional<ControlOperator> findOperator(UUID operatorId);

    List<ControlOperator> findOperators();
}
