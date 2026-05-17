package com.tavall.resourcegame.controlserver.api;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.middleware.punishment.PunishmentRepository;
import com.tavall.resourcegame.api.internal.permissions.PunishRequest;
import com.tavall.resourcegame.api.internal.permissions.PunishResponse;

import java.time.Instant;

public final class PunishApi implements IControlServerDomain, IDependencyInjectableConcrete {
    public PunishResponse inspect(PunishRequest request, Instant now) {
        if (request == null) {
            return PunishResponse.unavailable("punish-unavailable", "Punish request was null.");
        }
        try {
            return repository().inspect(request, now);
        } catch (RuntimeException exception) {
            return PunishResponse.unavailable(request.requestId(), "Punish API error: " + safeMessage(exception));
        }
    }

    private PunishmentRepository repository() {
        return com.tjxjnoobie.api.dependency.DependencyLoaderAccess.findInstance(PunishmentRepository.class);
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }
}
