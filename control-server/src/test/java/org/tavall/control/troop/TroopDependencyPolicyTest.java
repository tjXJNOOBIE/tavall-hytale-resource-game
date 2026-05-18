package org.tavall.control.troop;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TroopDependencyPolicyTest {
    @Test
    void troopHandlersUseDomainAccessorsInsteadOfCachedCollaborators() throws IOException {
        Pattern cachedCollaborator = Pattern.compile("private\\s+final\\s+.*(Repository|Handler|Handler|System|Registry|Resolver).*;");
        try (var paths = Files.walk(Path.of("src/main/java/org/tavall/control/troop"))) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                if (path.getFileName().toString().endsWith("Handler.java")) {
                    assertTrue(source.contains("implements ITroopDomain"), path + " should use troop domain accessors.");
                    assertFalse(cachedCollaborator.matcher(source).find(), path + " should not cache dependencies.");
                }
                if (!path.getFileName().toString().contains("Domain.java")) {
                    assertFalse(source.contains("DependencyLoaderAccess."), path + " should not access the dependency loader directly.");
                }
                assertFalse(path.getFileName().toString().endsWith("Service.java"));
            }
        }
    }
}
