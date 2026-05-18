package org.tavall.control.dependency;

import org.tavall.control.dependency.interfaces.ICastleInteractionService;
import org.tavall.control.dependency.interfaces.ICastleProximityPromptService;
import org.tavall.control.dependency.interfaces.IDebugCommandService;
import org.tavall.control.dependency.interfaces.IKingdomClockService;
import org.tavall.control.dependency.interfaces.IPlayerDataService;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;

/**
 * Minimal test composition root for the repo-local DI layer.
 */
public final class TestResourceGameDependencyModule implements IDependencyModule {
    private final TestPlayerDataService playerDataService = new TestPlayerDataService();
    private final TestCastleInteractionService castleInteractionService = new TestCastleInteractionService();
    private final TestCastleProximityPromptService castleProximityPromptService = new TestCastleProximityPromptService();
    private final TestDebugCommandService debugCommandService = new TestDebugCommandService();
    private final TestKingdomClockService kingdomClockService = new TestKingdomClockService();

    @Override
    public void registerDependencies() {
        DependencyLoaderAccess.registerInstance(IPlayerDataService.class, playerDataService);
        DependencyLoaderAccess.registerInstance(ICastleInteractionService.class, castleInteractionService);
        DependencyLoaderAccess.registerInstance(ICastleProximityPromptService.class, castleProximityPromptService);
        DependencyLoaderAccess.registerInstance(IDebugCommandService.class, debugCommandService);
        DependencyLoaderAccess.registerInstance(IKingdomClockService.class, kingdomClockService);
    }

    public TestPlayerDataService playerDataService() {
        return playerDataService;
    }

    public TestCastleInteractionService castleInteractionService() {
        return castleInteractionService;
    }

    public TestCastleProximityPromptService castleProximityPromptService() {
        return castleProximityPromptService;
    }

    public TestDebugCommandService debugCommandService() {
        return debugCommandService;
    }

    public TestKingdomClockService kingdomClockService() {
        return kingdomClockService;
    }
}
