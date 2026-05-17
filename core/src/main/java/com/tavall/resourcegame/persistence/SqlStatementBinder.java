package com.tavall.resourcegame.persistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface SqlStatementBinder {
    void bind(PreparedStatement statement) throws SQLException;
}
