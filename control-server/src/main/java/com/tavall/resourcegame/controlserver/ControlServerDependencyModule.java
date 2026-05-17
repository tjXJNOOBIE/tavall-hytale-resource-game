package com.tavall.resourcegame.controlserver;

import com.tavall.resourcegame.config.DatabaseConfig;
import com.tavall.resourcegame.controlserver.cli.ControlConsoleResultRenderer;
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
import com.tavall.resourcegame.controlserver.web.WebControlAuditQueryHandler;
import com.tavall.resourcegame.controlserver.web.WebControlCommandSubmissionHandler;
import com.tavall.resourcegame.controlserver.web.WebControlDashboardViewHandler;
import com.tavall.resourcegame.controlserver.web.WebControlHtmlHandler;
import com.tavall.resourcegame.controlserver.web.WebControlKingdomClockViewHandler;
import com.tavall.resourcegame.controlserver.web.WebControlKingdomViewHandler;
import com.tavall.resourcegame.controlserver.web.WebControlOperatorViewHandler;
import com.tavall.resourcegame.controlserver.web.WebControlPanelCommandHandler;
import com.tavall.resourcegame.controlserver.web.WebControlPlayerViewHandler;
import com.tavall.resourcegame.controlserver.web.WebControlPlatformStatusViewHandler;
import com.tavall.resourcegame.controlserver.web.WebControlTroopHealingViewHandler;
import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.dependency.IDependencyModule;
import com.tavall.resourcegame.middleware.cloud.CloudControlPanelViewHandler;
import com.tavall.resourcegame.middleware.cloud.CloudControlPlaneRuntimeFactory;
import com.tavall.resourcegame.middleware.cloud.ICloudControlPanelViewHandler;
import com.tavall.resourcegame.middleware.cloud.IMinecraftServerSnapshotIngressHandler;
import com.tavall.resourcegame.middleware.cloud.MinecraftServerSnapshotIngressHandler;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tavall.resourcegame.middleware.control.ControlOperator;
import com.tavall.resourcegame.middleware.punishment.InMemoryPunishmentRepository;
import com.tavall.resourcegame.middleware.punishment.PunishmentRepository;
import com.tavall.resourcegame.persistence.PostgresConnectionProvider;
import com.tavall.resourcegame.controlserver.api.PlayerDataApi;
import com.tavall.resourcegame.controlserver.api.PunishApi;
import com.tavall.resourcegame.controlserver.api.RankApi;
import com.tavall.resourcegame.controlserver.interaction.ControlPlaneInteractionService;

import java.time.Instant;

/**
 * Bridges Spring and CLI entry points into the Tavall DI registry without making
 * either adapter the owner of control-plane state.
 */
public final class ControlServerDependencyModule implements IDependencyModule {
    @Override
    public void registerDependencies() {
        DependencyLoaderAccess.clear();
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, createControlRuntime());
        CloudControlPlaneRuntimeFactory.createInMemoryRuntime();
        DependencyLoaderAccess.registerInstance(ControlOperator.class, ControlOperator.localOwner(Instant.now()));
        DependencyLoaderAccess.registerInstance(IControlConsoleResultRenderer.class, new ControlConsoleResultRenderer());
        DependencyLoaderAccess.registerInstance(IWebControlHtmlHandler.class, new WebControlHtmlHandler());
        DependencyLoaderAccess.registerInstance(IWebControlCommandSubmissionHandler.class, new WebControlCommandSubmissionHandler());
        DependencyLoaderAccess.registerInstance(IWebControlPanelCommandHandler.class, new WebControlPanelCommandHandler());
        DependencyLoaderAccess.registerInstance(IWebControlDashboardViewHandler.class, new WebControlDashboardViewHandler());
        DependencyLoaderAccess.registerInstance(IWebControlAuditQueryHandler.class, new WebControlAuditQueryHandler());
        DependencyLoaderAccess.registerInstance(IWebControlPlatformStatusViewHandler.class, new WebControlPlatformStatusViewHandler());
        DependencyLoaderAccess.registerInstance(IWebControlPlayerViewHandler.class, new WebControlPlayerViewHandler());
        DependencyLoaderAccess.registerInstance(IWebControlTroopHealingViewHandler.class, new WebControlTroopHealingViewHandler());
        DependencyLoaderAccess.registerInstance(IWebControlKingdomViewHandler.class, new WebControlKingdomViewHandler());
        DependencyLoaderAccess.registerInstance(IWebControlKingdomClockViewHandler.class, new WebControlKingdomClockViewHandler());
        DependencyLoaderAccess.registerInstance(IWebControlOperatorViewHandler.class, new WebControlOperatorViewHandler());
        DependencyLoaderAccess.registerInstance(ICloudControlPanelViewHandler.class, new CloudControlPanelViewHandler());
        DependencyLoaderAccess.registerInstance(IMinecraftServerSnapshotIngressHandler.class, new MinecraftServerSnapshotIngressHandler());
        DependencyLoaderAccess.registerInstance(ControlPlaneInteractionService.class, new ControlPlaneInteractionService());
        DependencyLoaderAccess.registerInstance(PlayerDataApi.class, new PlayerDataApi());
        DependencyLoaderAccess.registerInstance(PunishmentRepository.class, new InMemoryPunishmentRepository());
        DependencyLoaderAccess.registerInstance(PunishApi.class, new PunishApi());
        DependencyLoaderAccess.registerInstance(RankApi.class, new RankApi());
    }

    private ControlCommandRuntime createControlRuntime() {
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnv();
        if (databaseConfig.jdbcUrl() != null && !databaseConfig.jdbcUrl().isBlank()) {
            return ControlCommandRuntimeFactory.createPostgresRuntime(new PostgresConnectionProvider(databaseConfig));
        }
        return ControlCommandRuntimeFactory.createInMemoryRuntime();
    }
}
