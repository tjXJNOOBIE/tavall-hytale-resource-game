package com.tavall.resourcegame.middleware.node;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ResourceNodeDependencyPolicyTest {
    @Test
    void resourceNodeHandlersUseDomainAccessorsInsteadOfCachedCollaborators() throws IOException {
        List<Path> handlerFiles = List.of(
                Path.of("src/main/java/com/tavall/resourcegame/middleware/node/ResourceNodeCreationHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/middleware/node/ResourceProductionTickHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/middleware/node/ResourceStorageHandler.java")
        );
        Pattern cachedCollaborator = Pattern.compile("private\\s+final\\s+[A-Za-z0-9]+(?:Repository|Handler)\\s+[a-zA-Z0-9]+\\s*;");

        for (Path handlerFile : handlerFiles) {
            String source = Files.readString(handlerFile);
            assertTrue(source.contains("implements IResourceNodeDomain"), handlerFile + " should use resource-node domain accessors.");
            assertFalse(cachedCollaborator.matcher(source).find(), handlerFile + " should not cache repositories or handlers.");
        }
    }

    @Test
    void resourceNodeDomainGeneratedOwnsDefaultDependencyLookup() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/middleware/node/IResourceNodeDomainGenerated.java"));

        assertTrue(source.contains("DependencyLoaderAccess.findOptionalInstance(ResourceNodeRepository.class)"));
        assertTrue(source.contains("new InMemoryResourceNodeRepository()"));
        assertTrue(source.contains("registerResourceNodeCreationHandler(new ResourceNodeCreationHandler())"));
        assertTrue(source.contains("registerResourceStorageHandler(new ResourceStorageHandler())"));
        assertTrue(source.contains("registerResourceProductionTickHandler(new ResourceProductionTickHandler())"));
    }

    @Test
    void resourceNodeMiddlewareDoesNotIntroduceServiceClasses() throws IOException {
        try (var paths = Files.walk(Path.of("src/main/java/com/tavall/resourcegame/middleware/node"))) {
            List<Path> serviceClasses = paths
                    .filter(path -> path.getFileName().toString().endsWith("Service.java"))
                    .toList();

            assertTrue(serviceClasses.isEmpty(), "Resource-node middleware should keep Handler naming: " + serviceClasses);
        }
    }
}
