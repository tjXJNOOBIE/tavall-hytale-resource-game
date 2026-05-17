package com.tavall.resourcegame.dependency;

import com.tavall.resourcegame.ResourceGamePlugin;
import com.tavall.resourcegame.dependency.composition.domains.IResourceGameDomain;
import com.tavall.resourcegame.dependency.injection.helpers.DependencyInjectorHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ResourceGameDependencyBootstrapTest {
    @AfterEach
    void clearRegistry() {
        DependencyLoaderAccess.clear();
    }

    @Test
    void helperRegistersDomainTokens() {
        TestResourceGameDependencyModule module = new TestResourceGameDependencyModule();
        DependencyInjectorHelper helper = new DependencyInjectorHelper();
        TestResourceGameDomain domain = new TestResourceGameDomain();

        helper.setupDISystem(module);

        assertSame(module.playerDataService(), domain.getPlayerDataService());
        assertSame(module.castleInteractionService(), domain.getCastleInteractionService());
        assertSame(module.castleProximityPromptService(), domain.getCastleProximityPromptService());
        assertSame(module.debugCommandService(), domain.getDebugCommandService());
        assertSame(module.kingdomClockService(), domain.getKingdomClockService());
    }

    @Test
    void pluginImplementsResourceGameDomain() {
        assertTrue(IResourceGameDomain.class.isAssignableFrom(ResourceGamePlugin.class));
    }

    @Test
    void pluginBootstrapRegistersCoreFrontendControlBridge() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/dependency/modules/ResourceGameDependencyModule.java"));

        assertTrue(source.contains("IFrontendControlConfig.class"));
        assertTrue(source.contains("IFrontendControlCommandClient.class"));
        assertTrue(source.contains("IFrontendCommandVerificationService.class"));
        assertTrue(source.contains("FrontendCommandVerificationHandler"));
        assertTrue(source.contains("FrontendTcpControlCommandClient"));
    }
}
