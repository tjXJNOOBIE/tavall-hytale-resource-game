package org.tavall.control.dependency;

import org.tavall.control.ResourceGamePlugin;
import org.tavall.control.bootstrap.ResourceGameDomain;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        TestResourceGameDomain domain = new TestResourceGameDomain();

        module.registerDependencies();

        assertSame(module.playerDataHandler(), domain.getPlayerDataHandler());
        assertSame(module.castleInteractionHandler(), domain.getCastleInteractionHandler());
        assertSame(module.castleProximityPromptHandler(), domain.getCastleProximityPromptHandler());
        assertSame(module.debugCommandHandler(), domain.getDebugCommandHandler());
        assertSame(module.kingdomClockHandler(), domain.getKingdomClockHandler());
        assertSame(module.uiData(), domain.getUIData());
    }

    @Test
    void pluginImplementsResourceGameDomain() {
        assertTrue(ResourceGameDomain.class.isAssignableFrom(ResourceGamePlugin.class));
    }

    @Test
    void pluginBootstrapRegistersCoreFrontendControlBridge() throws IOException {
        String source = Files.readString(Path.of("src/main/java/org/tavall/control/bootstrap/ResourceGameDependencyModule.java"));

        assertTrue(source.contains("IFrontendControlConfig.class"));
        assertTrue(source.contains("IFrontendControlCommandClient.class"));
        assertTrue(source.contains("IFrontendCommandVerificationHandler.class"));
        assertTrue(source.contains("FrontendCommandVerificationHandler"));
        assertTrue(source.contains("FrontendTcpControlCommandClient"));
        assertFalse(source.contains("UiPageDefinition"));
        assertFalse(source.contains("UiNavigator"));
        assertFalse(source.contains("IUiPageRegistry"));
        assertFalse(source.contains("UiPageRegistry"));
        assertFalse(source.contains("IUiActionHandler"));
        assertFalse(source.contains("UiActionHandler"));
    }
}
