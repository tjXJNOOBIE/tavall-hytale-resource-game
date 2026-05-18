package org.tavall.control;

import org.tavall.control.api.PlayerDataApi;
import org.tavall.control.api.PunishApi;
import org.tavall.control.api.RankApi;
import org.tavall.control.interaction.ControlPlaneInteractionService;
import org.tavall.control.cli.IControlConsoleResultRenderer;
import org.tavall.control.web.IWebControlAuditQueryHandler;
import org.tavall.control.web.IWebControlCommandSubmissionHandler;
import org.tavall.control.web.IWebControlDashboardViewHandler;
import org.tavall.control.web.IWebControlHtmlHandler;
import org.tavall.control.web.IWebControlKingdomClockViewHandler;
import org.tavall.control.web.IWebControlKingdomViewHandler;
import org.tavall.control.web.IWebControlOperatorViewHandler;
import org.tavall.control.web.IWebControlPanelCommandHandler;
import org.tavall.control.web.IWebControlPlayerViewHandler;
import org.tavall.control.web.IWebControlPlatformStatusViewHandler;
import org.tavall.control.web.IWebControlTroopHealingViewHandler;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.cloud.ICloudControlPanelViewHandler;
import org.tavall.control.cloud.ICloudControlCliHandler;
import org.tavall.control.cloud.IMinecraftServerSnapshotIngressHandler;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.control.runtime.ControlOperator;

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
