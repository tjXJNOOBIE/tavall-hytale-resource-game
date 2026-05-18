package org.tavall.control.guild;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GuildDependencyPolicyTest {
    @Test
    void guildHandlersUseDomainAccessorsInsteadOfCachedCollaborators() throws IOException {
        List<Path> handlerFiles = List.of(
                Path.of("src/main/java/org/tavall/control/guild/GuildActionValidationHandler.java"),
                Path.of("src/main/java/org/tavall/control/guild/GuildAuthorityTierHandler.java"),
                Path.of("src/main/java/org/tavall/control/guild/GuildBuffActivationHandler.java"),
                Path.of("src/main/java/org/tavall/control/guild/GuildCreationHandler.java"),
                Path.of("src/main/java/org/tavall/control/guild/GuildDomainActionBuffHandler.java"),
                Path.of("src/main/java/org/tavall/control/guild/GuildJobAssignmentHandler.java"),
                Path.of("src/main/java/org/tavall/control/guild/GuildMembershipHandler.java"),
                Path.of("src/main/java/org/tavall/control/guild/GuildStatModifierCalculationHandler.java"),
                Path.of("src/main/java/org/tavall/control/guild/GuildUpgradePurchaseHandler.java")
        );
        Pattern cachedCollaborator = Pattern.compile("private\\s+final\\s+[A-Za-z0-9]+(?:Repository|Handler)\\s+[a-zA-Z0-9]+\\s*;");

        for (Path handlerFile : handlerFiles) {
            String source = Files.readString(handlerFile);
            assertTrue(source.contains("implements IGuildDomain"), handlerFile + " should use guild domain accessors.");
            assertFalse(cachedCollaborator.matcher(source).find(), handlerFile + " should not cache repositories or handlers.");
        }
    }

    @Test
    void guildDomainGeneratedOwnsDefaultDependencyLookup() throws IOException {
        String source = Files.readString(Path.of("src/main/java/org/tavall/control/guild/IGuildDomainGenerated.java"));

        assertTrue(source.contains("DependencyLoaderAccess.findOptionalInstance(GuildRepository.class)"));
        assertTrue(source.contains("new InMemoryGuildRepository()"));
        assertTrue(source.contains("DependencyLoaderAccess.findOptionalInstance(GuildBuffRepository.class)"));
        assertTrue(source.contains("new InMemoryGuildBuffRepository()"));
        assertTrue(source.contains("registerGuildJobBuffCalculationHandler(new GuildJobBuffCalculationHandler())"));
        assertTrue(source.contains("registerGuildPermissionValidationHandler(new GuildPermissionValidationHandler())"));
    }

    @Test
    void guildMiddlewareDoesNotIntroduceServiceClasses() throws IOException {
        try (var paths = Files.walk(Path.of("src/main/java/org/tavall/control/guild"))) {
            List<Path> serviceClasses = paths
                    .filter(path -> path.getFileName().toString().endsWith("Service.java"))
                    .toList();

            assertTrue(serviceClasses.isEmpty(), "Guild middleware should not add Service classes: " + serviceClasses);
        }
    }
}
