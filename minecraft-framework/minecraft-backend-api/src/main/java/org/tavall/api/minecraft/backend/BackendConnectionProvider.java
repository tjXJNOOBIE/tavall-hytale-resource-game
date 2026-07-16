package org.tavall.api.minecraft.backend;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface BackendConnectionProvider {
    Connection open() throws SQLException;
}
