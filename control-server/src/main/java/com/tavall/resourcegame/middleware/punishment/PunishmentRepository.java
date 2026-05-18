package org.tavall.control.punishment;

import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.PunishResponse;

import java.time.Instant;

public interface PunishmentRepository {
    PunishResponse inspect(PunishRequest request, Instant now);
}
