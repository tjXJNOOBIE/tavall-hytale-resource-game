package com.tavall.resourcegame.farmstead.ui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.dependency.interfaces.ICastleBuildingService;
import com.tavall.resourcegame.dependency.interfaces.IFarmsteadMenuService;
import com.tavall.resourcegame.dependency.interfaces.IPlayerSessionStore;
import com.tavall.resourcegame.dependency.interfaces.IUiNavigator;
import com.tavall.resourcegame.domain.BuildingType;
import com.tavall.resourcegame.domain.CastleBuildingData;
import com.tavall.resourcegame.domain.UiNavigationContext;
import com.tavall.resourcegame.services.PlayerSession;
import com.tavall.resourcegame.ui.UiPageType;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public final class FarmsteadMenuService implements IFarmsteadMenuService, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(FarmsteadMenuService.class.getName());

    private final IPlayerSessionStore sessionStore;
    private final ICastleBuildingService buildingService;
    private final IUiNavigator uiNavigator;

    public FarmsteadMenuService(
            IPlayerSessionStore sessionStore,
            ICastleBuildingService buildingService,
            IUiNavigator uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.buildingService = Objects.requireNonNull(buildingService, "buildingService");
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
        Optional<CastleBuildingData> farmstead = buildingService.resolveBuilding(session.gameState(), BuildingType.FARMSTEAD.shortKey());
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
