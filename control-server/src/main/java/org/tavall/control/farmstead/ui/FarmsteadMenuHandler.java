package org.tavall.control.farmstead.ui;
import org.tavall.control.player.PlayerSessionStore;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.farmstead.ui.IFarmsteadMenuHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.ui.IUiNavigator;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleBuildingData;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.player.PlayerSession;
import org.tavall.control.ui.UiPageType;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public final class FarmsteadMenuHandler implements IFarmsteadMenuHandler, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(FarmsteadMenuHandler.class.getName());

    private final IPlayerSessionStore sessionStore;
    private final ICastleBuildingHandler buildingHandler;
    private final IUiNavigator uiNavigator;

    public FarmsteadMenuHandler(
            IPlayerSessionStore sessionStore,
            ICastleBuildingHandler buildingHandler,
            IUiNavigator uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.buildingHandler = Objects.requireNonNull(buildingHandler, "buildingHandler");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
    }

    @Override
    public boolean openFarmsteadMenu(Player player) {
        if (player == null) {
            LOGGER.warning("Farmstead menu open skipped because the player was null.");
            return false;
        }
        PlayerSession session = sessionStore.get(player.getUuid());
        if (session == null) {
            LOGGER.warning(() -> "Farmstead menu open skipped for " + player.getDisplayName() + " because the player session is not ready.");
            player.sendMessage(Message.raw("Farmstead menu is not ready yet.").color("yellow"));
            return false;
        }
        Optional<CastleBuildingData> farmstead = buildingHandler.resolveBuilding(session.gameState(), BuildingType.FARMSTEAD.shortKey());
        UiNavigationContext context = new UiNavigationContext(player.getUuid(), player.getDisplayName())
                .withFeedback("Farmstead Steward selected.");
        if (farmstead.isPresent()) {
            context = context.withSelectedBuildingId(farmstead.get().buildingId());
        }
        LOGGER.info(() -> "Opening Farmstead menu for " + player.getDisplayName()
                + " selectedBuildingId=" + farmstead.map(CastleBuildingData::buildingId).map(Objects::toString).orElse("none") + ".");
        uiNavigator.open(UiPageType.FARMSTEAD_MENU, player, context, session.gameState());
        return true;
    }
}

