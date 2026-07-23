package org.tavall.control;

import org.tavall.control.config.DatabaseConfig;
import org.tavall.control.cli.ControlConsoleResultRenderer;
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
import org.tavall.control.web.WebControlAuditQueryHandler;
import org.tavall.control.web.WebControlCommandSubmissionHandler;
import org.tavall.control.web.WebControlDashboardViewHandler;
import org.tavall.control.web.WebControlHtmlHandler;
import org.tavall.control.web.WebControlKingdomClockViewHandler;
import org.tavall.control.web.WebControlKingdomViewHandler;
import org.tavall.control.web.WebControlOperatorViewHandler;
import org.tavall.control.web.WebControlPanelCommandHandler;
import org.tavall.control.web.WebControlPlayerViewHandler;
import org.tavall.control.web.WebControlPlatformStatusViewHandler;
import org.tavall.control.web.WebControlTroopHealingViewHandler;
import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.dependency.IDependencyModule;
import org.tavall.control.cloud.CloudControlPanelViewHandler;
import org.tavall.control.cloud.CloudControlPlaneRuntimeFactory;
import org.tavall.control.cloud.ICloudControlPanelViewHandler;
import org.tavall.control.cloud.IMinecraftServerSnapshotIngressHandler;
import org.tavall.control.cloud.MinecraftServerSnapshotIngressHandler;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.control.runtime.ControlCommandRuntimeFactory;
import org.tavall.control.runtime.ControlOperator;
import org.tavall.control.punishment.InMemoryPunishmentRepository;
import org.tavall.control.punishment.PunishmentRepository;
import org.tavall.control.persistence.PostgresConnectionProvider;
import org.tavall.control.api.PlayerDataApi;
import org.tavall.control.api.PunishApi;
import org.tavall.api.minecraft.backend.rank.RankApi;
import org.tavall.control.interaction.ControlPlaneInteractionHandler;

import java.time.Instant;

/**
 * Bridges Spring and CLI entry points into the Tavall DI registry without making
 * either adapter the owner of control-plane state.
 */
public final class ControlServerDependencyModule implements IDependencyModule {
    @Override
    public void registerDependencies() {
        DependencyLoaderAccess.clear();
        ControlCommandRuntime runtime = createControlRuntime();
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, runtime);
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
        DependencyLoaderAccess.registerInstance(ControlPlaneInteractionHandler.class, new ControlPlaneInteractionHandler());
        DependencyLoaderAccess.registerInstance(PlayerDataApi.class, new PlayerDataApi());
        DependencyLoaderAccess.registerInstance(PunishmentRepository.class, new InMemoryPunishmentRepository());
        DependencyLoaderAccess.registerInstance(PunishApi.class, new PunishApi());
        DependencyLoaderAccess.registerInstance(RankApi.class, new RankApi(runtime.rankRepository()));
    }

    private ControlCommandRuntime createControlRuntime() {
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnv();
        if (databaseConfig.jdbcUrl() != null && !databaseConfig.jdbcUrl().isBlank()) {
            return ControlCommandRuntimeFactory.createPostgresRuntime(new PostgresConnectionProvider(databaseConfig));
        }
        return ControlCommandRuntimeFactory.createInMemoryRuntime();
    }
}
