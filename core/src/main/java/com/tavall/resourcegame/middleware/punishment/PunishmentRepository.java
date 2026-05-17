package com.tavall.resourcegame.middleware.punishment;

import com.tavall.resourcegame.shared.frontend.PunishRequest;
import com.tavall.resourcegame.shared.frontend.PunishResponse;

import java.time.Instant;

public interface PunishmentRepository {
    PunishResponse inspect(PunishRequest request, Instant now);
}
