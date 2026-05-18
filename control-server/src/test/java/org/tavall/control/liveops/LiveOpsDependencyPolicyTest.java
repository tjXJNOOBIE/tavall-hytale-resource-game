package org.tavall.control.liveops;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class LiveOpsDependencyPolicyTest {
    @Test
    void liveOpsHandlersUseGeneratedDefaultDependenciesInsteadOfConstructorGraphWiring() throws IOException {
        List<Path> files = List.of(
                Path.of("src/main/java/org/tavall/control/liveops/config/FeatureFlagHandler.java"),
                Path.of("src/main/java/org/tavall/control/liveops/config/GameRuleHandler.java"),
                Path.of("src/main/java/org/tavall/control/liveops/config/LiveConfigMutationHandler.java"),
                Path.of("src/main/java/org/tavall/control/liveops/config/LiveConfigRegistry.java"),
                Path.of("src/main/java/org/tavall/control/liveops/config/SystemToggleHandler.java"),
                Path.of("src/main/java/org/tavall/control/liveops/gui/GlobalGuiMutationHandler.java"),
                Path.of("src/main/java/org/tavall/control/liveops/gui/GlobalGuiRegistry.java")
        );
        Pattern constructorDependency = Pattern.compile("public\\s+[A-Za-z0-9]+\\s*\\([^)]*[A-Z][A-Za-z0-9_<>?, ]+\\s+[a-z]");
        Pattern privateDependencyField = Pattern.compile("private\\s+final\\s+(LiveConfig|GlobalGui|ObjectMapper|Optional|GameEventDispatchHandler).*;");

        for (Path file : files) {
            String source = Files.readString(file);
            assertTrue(!constructorDependency.matcher(source).find(), file + " should not constructor-wire LiveOps dependencies.");
            assertTrue(!privateDependencyField.matcher(source).find(), file + " should not cache LiveOps dependencies in fields.");
        }
    }

    @Test
    void liveOpsRedisAndPostgresAdaptersUseGeneratedDefaultDependencies() throws IOException {
        List<Path> files = List.of(
                Path.of("src/main/java/org/tavall/control/liveops/config/RedisLiveConfigPublisher.java"),
                Path.of("src/main/java/org/tavall/control/liveops/config/RedisLiveConfigSubscriber.java"),
                Path.of("src/main/java/org/tavall/control/liveops/config/PostgresLiveConfigRepository.java"),
                Path.of("src/main/java/org/tavall/control/liveops/gui/RedisGlobalGuiPublisher.java"),
                Path.of("src/main/java/org/tavall/control/liveops/gui/RedisGlobalGuiSubscriber.java"),
                Path.of("src/main/java/org/tavall/control/liveops/gui/PostgresGlobalGuiRepository.java")
        );
        Pattern adapterConstructorDependency = Pattern.compile(
                "public\\s+[A-Za-z0-9]+\\s*\\([^)]*(JedisPool|PostgresConnectionProvider|ObjectMapper|LiveConfigRegistry|GlobalGuiRegistry|String\\s+channel)[^)]*\\)"
        );
        Pattern adapterPrivateDependencyField = Pattern.compile(
                "private\\s+final\\s+(JedisPool|PostgresConnectionProvider|ObjectMapper|LiveConfigRegistry|GlobalGuiRegistry|String)\\s+\\w+"
        );

        for (Path file : files) {
            String source = Files.readString(file);
            assertTrue(!adapterConstructorDependency.matcher(source).find(), file + " should not constructor-wire LiveOps adapter dependencies.");
            assertTrue(!adapterPrivateDependencyField.matcher(source).find(), file + " should not cache LiveOps adapter dependencies in fields.");
        }
    }

    @Test
    void liveOpsRuntimeFactoryDoesNotRegisterConcreteEventDispatcherTokens() throws IOException {
        Path file = Path.of("src/main/java/org/tavall/control/liveops/LiveOpsRuntimeFactory.java");
        String source = Files.readString(file);

        assertTrue(!source.contains("registerInstance(GameEventDispatchHandler.class"),
                file + " should rely on the event module's DI registration instead of concrete-token bridge wiring.");
    }
}
