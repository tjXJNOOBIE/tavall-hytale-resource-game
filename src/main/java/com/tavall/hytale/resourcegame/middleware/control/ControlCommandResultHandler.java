package com.tavall.hytale.resourcegame.middleware.control;

public final class ControlCommandResultHandler {
    private final ControlCommandResultRepository resultRepository;

    public ControlCommandResultHandler(ControlCommandResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public ControlCommandResult recordResult(ControlCommandResult result) {
        return resultRepository.saveResult(result);
    }
}
