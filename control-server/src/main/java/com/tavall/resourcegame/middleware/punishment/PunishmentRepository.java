package com.tavall.resourcegame.middleware.punishment;

import com.tavall.resourcegame.api.internal.permissions.PunishRequest;
import com.tavall.resourcegame.api.internal.permissions.PunishResponse;

import java.time.Instant;

public interface PunishmentRepository {
    PunishResponse inspect(PunishRequest request, Instant now);
}
