package org.tavall.control.trade;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TradeDependencyPolicyTest {
    @Test
    void tradeHandlersUseDomainAccessorsInsteadOfCachedCollaborators() throws IOException {
        Pattern cachedCollaborator = Pattern.compile("private\\s+final\\s+.*(Repository|Handler|Service|System|Registry|Resolver).*;");
        try (var paths = Files.walk(Path.of("src/main/java/com/tavall/resourcegame/middleware/trade"))) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                if (path.getFileName().toString().endsWith("Handler.java")) {
                    assertTrue(source.contains("implements ITradeDomain"), path + " should use trade domain accessors.");
                    assertFalse(cachedCollaborator.matcher(source).find(), path + " should not cache dependencies.");
                }
                if (!path.getFileName().toString().contains("DomainGenerated")) {
                    assertFalse(source.contains("DependencyLoaderAccess."), path + " should not access the dependency loader directly.");
                }
                assertFalse(path.getFileName().toString().endsWith("Service.java"));
            }
        }
    }
}
