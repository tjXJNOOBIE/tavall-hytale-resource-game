package org.tavall.control.api;

import org.tavall.control.ControlServerDomain;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.punishment.PunishmentRepository;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.PunishResponse;

import java.time.Instant;

public final class PunishApi implements ControlServerDomain, IDependencyInjectableConcrete {
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
