package com.tavall.resourcegame.controlserver;

import com.tavall.resourcegame.controlserver.api.PlayerDataApi;
import com.tavall.resourcegame.controlserver.api.PunishApi;
import com.tavall.resourcegame.controlserver.api.RankApi;
import com.tavall.resourcegame.controlserver.interaction.ControlPlaneInteractionService;
import com.tavall.resourcegame.controlserver.cli.IControlConsoleResultRenderer;
import com.tavall.resourcegame.controlserver.web.IWebControlAuditQueryHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlCommandSubmissionHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlDashboardViewHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlHtmlHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlKingdomClockViewHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlKingdomViewHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlOperatorViewHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlPanelCommandHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlPlayerViewHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlPlatformStatusViewHandler;
import com.tavall.resourcegame.controlserver.web.IWebControlTroopHealingViewHandler;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.cloud.ICloudControlPanelViewHandler;
import com.tavall.resourcegame.middleware.cloud.ICloudControlCliHandler;
import com.tavall.resourcegame.middleware.cloud.IMinecraftServerSnapshotIngressHandler;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.resourcegame.middleware.control.ControlOperator;

/**
 * Generated-domain equivalent for control-server adapter dependencies.
 */
public interface IControlServerDomainGenerated {
    default ControlCommandRuntime getControlCommandRuntime() {
        return DependencyLoaderAccess.findInstance(ControlCommandRuntime.class);
    }

    default ICloudControlCliHandler getCloudControlCliHandler() {
        return DependencyLoaderAccess.findInstance(ICloudControlCliHandler.class);
    }

    default ControlOperator getWebControlOperator() {
        return DependencyLoaderAccess.findInstance(ControlOperator.class);
    }

    default IControlConsoleResultRenderer getControlConsoleResultRenderer() {
        return DependencyLoaderAccess.findInstance(IControlConsoleResultRenderer.class);
    }

    default IWebControlHtmlHandler getWebControlHtmlHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlHtmlHandler.class);
    }

    default IWebControlCommandSubmissionHandler getWebControlCommandSubmissionHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlCommandSubmissionHandler.class);
    }

    default IWebControlPanelCommandHandler getWebControlPanelCommandHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlPanelCommandHandler.class);
    }

    default IWebControlDashboardViewHandler getWebControlDashboardViewHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlDashboardViewHandler.class);
    }

    default IWebControlAuditQueryHandler getWebControlAuditQueryHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlAuditQueryHandler.class);
    }

    default IWebControlPlatformStatusViewHandler getWebControlPlatformStatusViewHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlPlatformStatusViewHandler.class);
    }

    default IWebControlPlayerViewHandler getWebControlPlayerViewHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlPlayerViewHandler.class);
    }

    default IWebControlTroopHealingViewHandler getWebControlTroopHealingViewHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlTroopHealingViewHandler.class);
    }

    default IWebControlKingdomViewHandler getWebControlKingdomViewHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlKingdomViewHandler.class);
    }

    default IWebControlKingdomClockViewHandler getWebControlKingdomClockViewHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlKingdomClockViewHandler.class);
    }

    default IWebControlOperatorViewHandler getWebControlOperatorViewHandler() {
        return DependencyLoaderAccess.findInstance(IWebControlOperatorViewHandler.class);
    }

    default ICloudControlPanelViewHandler getCloudControlPanelViewHandler() {
        return DependencyLoaderAccess.findInstance(ICloudControlPanelViewHandler.class);
    }

    default IMinecraftServerSnapshotIngressHandler getMinecraftServerSnapshotIngressHandler() {
        return DependencyLoaderAccess.findInstance(IMinecraftServerSnapshotIngressHandler.class);
    }

    default ControlPlaneInteractionService getControlPlaneInteractionService() {
        return DependencyLoaderAccess.findInstance(ControlPlaneInteractionService.class);
    }

    default PlayerDataApi getPlayerDataApi() {
        return DependencyLoaderAccess.findInstance(PlayerDataApi.class);
    }

    default RankApi getRankApi() {
        return DependencyLoaderAccess.findInstance(RankApi.class);
    }

    default PunishApi getPunishApi() {
        return DependencyLoaderAccess.findInstance(PunishApi.class);
    }
}
