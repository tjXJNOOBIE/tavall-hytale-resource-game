package org.tavall.control.events;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GameEventDependencyPolicyTest {
    @Test
    void eventHandlersUseGeneratedDefaultDependenciesInsteadOfConstructorGraphWiring() throws IOException {
        List<Path> files = List.of(
                Path.of("src/main/java/com/tavall/resourcegame/events/dispatch/GameEventDispatchHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/events/dispatch/InteractionBridgeHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/events/middleware/EventAuditMiddleware.java"),
                Path.of("src/main/java/com/tavall/resourcegame/events/middleware/EventDistributedForwardingMiddleware.java"),
                Path.of("src/main/java/com/tavall/resourcegame/events/middleware/EventLoggingMiddleware.java"),
                Path.of("src/main/java/com/tavall/resourcegame/events/middleware/EventPermissionMiddleware.java"),
                Path.of("src/main/java/com/tavall/resourcegame/events/middleware/EventRateLimitMiddleware.java")
        );
        Pattern constructorDependency = Pattern.compile("public\\s+[A-Za-z0-9]+\\s*\\([^)]*[A-Z][A-Za-z0-9_<>?, ]+\\s+[a-z]");
        Pattern privateDependencyField = Pattern.compile("private\\s+final\\s+(GameEvent|Event|DistributedEvent|Clock|Duration|List<).*;");

        for (Path file : files) {
            String source = Files.readString(file);
            assertTrue(!constructorDependency.matcher(source).find(), file + " should not constructor-wire handler dependencies.");
            assertTrue(!privateDependencyField.matcher(source).find(), file + " should not cache handler dependencies in fields.");
        }
    }

    @Test
    void middlewareChainIsCreatedThroughFactoryInsteadOfPublicConstructorGraph() throws IOException {
        Path file = Path.of("src/main/java/com/tavall/resourcegame/events/middleware/GameEventMiddlewareChain.java");
        String source = Files.readString(file);

        assertTrue(
                !source.contains("public GameEventMiddlewareChain("),
                file + " should expose chain creation through a named factory, not public constructor wiring."
        );
        assertTrue(
                source.contains("public static GameEventMiddlewareChain start("),
                file + " should keep middleware chain creation explicit."
        );
    }

    @Test
    void redisEventAdaptersUseGeneratedDefaultDependenciesInsteadOfConstructorGraphWiring() throws IOException {
        List<Path> files = List.of(
                Path.of("src/main/java/com/tavall/resourcegame/events/dispatch/RedisGameEventSubscriber.java"),
                Path.of("src/main/java/com/tavall/resourcegame/events/middleware/RedisGameEventForwarder.java")
        );
        Pattern redisConstructorDependency = Pattern.compile(
                "public\\s+[A-Za-z0-9]+\\s*\\([^)]*(JedisPool|ObjectMapper|GameEventDispatchHandler|String\\s+channel)[^)]*\\)"
        );
        Pattern redisPrivateDependencyField = Pattern.compile(
                "private\\s+final\\s+(JedisPool|ObjectMapper|String|GameEventDispatchHandler)\\s+\\w+"
        );

        for (Path file : files) {
            String source = Files.readString(file);
            assertTrue(!redisConstructorDependency.matcher(source).find(), file + " should not constructor-wire Redis event dependencies.");
            assertTrue(!redisPrivateDependencyField.matcher(source).find(), file + " should not cache Redis event dependencies in fields.");
        }
    }
}
