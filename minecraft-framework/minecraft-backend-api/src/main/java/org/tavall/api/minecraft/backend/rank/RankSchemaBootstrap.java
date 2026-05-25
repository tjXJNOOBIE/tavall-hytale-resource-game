package org.tavall.api.minecraft.backend.rank;

import org.tavall.api.minecraft.backend.BackendConnectionProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

public final class RankSchemaBootstrap {
    private static final List<String> SCHEMA_RESOURCES = List.of(
            "schema/postgres/008_minecraft_rank_system.sql"
    );

    public boolean ensureSchema(BackendConnectionProvider connectionProvider) {
        Objects.requireNonNull(connectionProvider, "connectionProvider");
        try (Connection connection = connectionProvider.open()) {
            connection.setAutoCommit(false);
            for (String resourcePath : SCHEMA_RESOURCES) {
                runScript(connection, resourcePath);
            }
            connection.commit();
            return true;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to verify rank schema: " + exception.getMessage(), exception);
        }
    }

    private void runScript(Connection connection, String resourcePath) throws IOException, SQLException {
        String script = loadResource(resourcePath);
        for (String statementText : splitStatements(script)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(statementText);
            }
        }
    }

    private String loadResource(String resourcePath) throws IOException {
        ClassLoader classLoader = RankSchemaBootstrap.class.getClassLoader();
        InputStream stream = classLoader == null ? null : classLoader.getResourceAsStream(resourcePath);
        if (stream == null) {
            stream = RankSchemaBootstrap.class.getResourceAsStream("/" + resourcePath);
        }
        try (InputStream inputStream = stream) {
            if (inputStream == null) {
                throw new IOException("Schema resource not found: " + resourcePath);
            }
            String script = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            if (!script.isEmpty() && script.charAt(0) == '\uFEFF') {
                return script.substring(1);
            }
            return script;
        }
    }

    private List<String> splitStatements(String script) {
        return script.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("--"))
                .reduce(new java.util.ArrayList<String>(), (statements, line) -> {
                    if (statements.isEmpty()) {
                        statements.add(line);
                    } else {
                        int lastIndex = statements.size() - 1;
                        statements.set(lastIndex, statements.get(lastIndex) + System.lineSeparator() + line);
                    }
                    if (line.endsWith(";")) {
                        int lastIndex = statements.size() - 1;
                        statements.set(lastIndex, statements.get(lastIndex).substring(0, statements.get(lastIndex).length() - 1).trim());
                        statements.add("");
                    }
                    return statements;
                }, (left, right) -> {
                    left.addAll(right);
                    return left;
                }).stream()
                .map(String::trim)
                .filter(statement -> !statement.isBlank())
                .toList();
    }
}
