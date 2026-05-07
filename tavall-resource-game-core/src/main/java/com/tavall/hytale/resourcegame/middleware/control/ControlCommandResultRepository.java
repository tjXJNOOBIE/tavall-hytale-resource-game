package com.tavall.hytale.resourcegame.middleware.control;

import java.util.List;
import java.util.Optional;

public interface ControlCommandResultRepository {
    ControlCommandResult saveResult(ControlCommandResult result);

    Optional<ControlCommandResult> findResult(ControlCommandId commandId);

    List<ControlCommandResult> findRecentResults(int limit);
}
