package org.tavall.control.dependency;

import org.tavall.control.castle.ICastleInteractionHandler;
import org.tavall.control.castle.ICastleProximityPromptHandler;
import org.tavall.control.api.UIData;
import org.tavall.control.runtime.IDebugCommandHandler;
import org.tavall.control.clock.IKingdomClockHandler;
import org.tavall.control.player.IPlayerDataHandler;
import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.dependency.IDependencyModule;

/**
 * Minimal test composition root for the repo-local DI layer.
 */
public final class TestResourceGameDependencyModule implements IDependencyModule {
    private final TestPlayerDataHandler playerDataHandler = new TestPlayerDataHandler();
    private final TestCastleInteractionHandler castleInteractionHandler = new TestCastleInteractionHandler();
    private final TestCastleProximityPromptHandler castleProximityPromptHandler = new TestCastleProximityPromptHandler();
    private final TestDebugCommandHandler debugCommandHandler = new TestDebugCommandHandler();
    private final TestKingdomClockHandler kingdomClockHandler = new TestKingdomClockHandler();
    private final UIData uiData = new UIData();

    @Override
    public void registerDependencies() {
        DependencyLoaderAccess.registerInstance(IPlayerDataHandler.class, playerDataHandler);
        DependencyLoaderAccess.registerInstance(ICastleInteractionHandler.class, castleInteractionHandler);
        DependencyLoaderAccess.registerInstance(ICastleProximityPromptHandler.class, castleProximityPromptHandler);
        DependencyLoaderAccess.registerInstance(IDebugCommandHandler.class, debugCommandHandler);
        DependencyLoaderAccess.registerInstance(IKingdomClockHandler.class, kingdomClockHandler);
        DependencyLoaderAccess.registerInstance(UIData.class, uiData);
    }

    public TestPlayerDataHandler playerDataHandler() {
        return playerDataHandler;
    }

    public TestCastleInteractionHandler castleInteractionHandler() {
        return castleInteractionHandler;
    }

    public TestCastleProximityPromptHandler castleProximityPromptHandler() {
        return castleProximityPromptHandler;
    }

    public TestDebugCommandHandler debugCommandHandler() {
        return debugCommandHandler;
    }

    public TestKingdomClockHandler kingdomClockHandler() {
        return kingdomClockHandler;
    }

    public UIData uiData() {
        return uiData;
    }
}
