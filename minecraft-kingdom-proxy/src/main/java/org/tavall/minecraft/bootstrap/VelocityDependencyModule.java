package org.tavall.minecraft.bootstrap;

import org.tavall.api.minecraft.cache.KingdomCacheConfig;
import org.tavall.api.minecraft.cache.IKingdomSemanticCacheGateway;
import org.tavall.api.minecraft.cache.KingdomSemanticCacheGateway;
import org.tavall.api.minecraft.backend.BackendConnectionProvider;
import org.tavall.api.minecraft.backend.rank.InMemoryRankRepository;
import org.tavall.api.minecraft.backend.rank.PostgresRankRepository;
import org.tavall.api.minecraft.backend.rank.RankAccess;
import org.tavall.api.minecraft.backend.rank.RankSchemaBootstrap;
import org.tavall.minecraft.bootstrap.VelocityDependencies;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.dependency.injection.helpers.DependencyInjectorHelper;
import org.tavall.minecraft.commands.IGuild;
import org.tavall.minecraft.commands.IRank;
import org.tavall.minecraft.commands.ISim;
import org.tavall.minecraft.permissions.IVelocityCommandPermissionHandler;
import org.tavall.minecraft.permissions.IVelocityPermissionResolver;
import org.tavall.minecraft.permissions.VelocityCommandPermissionHandler;
import org.tavall.minecraft.permissions.VelocityPermissionResolver;
import org.tavall.dependency.DependencyLoaderAccess;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.net.URI;
import java.util.Map;

/**
 * Registers Velocity frontend collaborators behind Tavall DI tokens while the Velocity plugin
 * remains the only class constructed by the proxy bootstrap.
 */
public final class VelocityDependencyModule {
    public void registerDependencies() {
        registerDependencies(VelocityProxyConfig.fromEnvironment(System.getenv()));
    }

    public void registerDependencies(VelocityProxyConfig config) {
        registerDependencies(config, resolveRankAccess(System.getenv()));
    }

    public void registerDependencies(VelocityProxyConfig config, RankAccess rankAccess) {
        registerOrReplace(IVelocityProxyConfig.class, config);
        registerOrReplace(VelocityProxyConfig.class, config);
        KingdomCacheConfig kingdomCacheConfig = resolveKingdomCacheConfig();
        IKingdomSemanticCacheGateway kingdomSemanticCacheGateway = KingdomSemanticCacheGateway.open(kingdomCacheConfig);
        IVelocityPermissionResolver velocityPermissionResolver = new VelocityPermissionResolver();
        IVelocityCommandPermissionHandler velocityCommandPermissionHandler = new VelocityCommandPermissionHandler();
        registerCoreOrReplace(KingdomCacheConfig.class, kingdomCacheConfig);
        registerCoreOrReplace(IKingdomSemanticCacheGateway.class, kingdomSemanticCacheGateway);
        registerCoreOrReplace(RankAccess.class, rankAccess);
        registerGuildDependencies();
        registerIfMissing(IVelocityPermissionResolver.class, velocityPermissionResolver);
        registerIfMissing(IVelocityCommandPermissionHandler.class, velocityCommandPermissionHandler);
        registerCommandDependencies();
        registerIfMissing(VelocityDependencies.class, new VelocityDependencies(
                config,
                kingdomSemanticCacheGateway,
                velocityPermissionResolver,
                velocityCommandPermissionHandler,
                rankAccess,
                DependencyLoaderAccess.findInstance(ISim.class),
                DependencyLoaderAccess.findInstance(IGuild.class),
                DependencyLoaderAccess.findInstance(IRank.class)
        ));
    }

    private void registerGuildDependencies() {
        DependencyInjectorHelper<IDependencyInjectableInterface, IDependencyInjectableConcrete> helper = new DependencyInjectorHelper<>();
        helper.setBasePackage("org.tavall.api.minecraft.guild");
        helper.setupDISystem(getClass().getClassLoader());
    }

    private void registerCommandDependencies() {
        DependencyInjectorHelper<IDependencyInjectableInterface, IDependencyInjectableConcrete> helper = new DependencyInjectorHelper<>();
        helper.setBasePackage("org.tavall.minecraft.commands");
        helper.setupDISystem(getClass().getClassLoader());
    }

    private <T> void registerIfMissing(Class<T> token, T instance) {
        if (!DependencyLoaderAccess.isInstanceRegistered(token)) {
            DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private <T> void registerOrReplace(Class<T> token, T instance) {
        if (DependencyLoaderAccess.isInstanceRegistered(token)) {
            DependencyLoaderAccess.replaceInstance(token, () -> instance);
        } else {
            DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private <T> void registerCoreIfMissing(Class<T> token, T instance) {
        if (!org.tavall.dependency.DependencyLoaderAccess.findOptionalInstance(token).isPresent()) {
            org.tavall.dependency.DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private <T> void registerCoreOrReplace(Class<T> token, T instance) {
        if (org.tavall.dependency.DependencyLoaderAccess.findOptionalInstance(token).isPresent()) {
            org.tavall.dependency.DependencyLoaderAccess.replaceInstance(token, () -> instance);
        } else {
            org.tavall.dependency.DependencyLoaderAccess.registerInstance(token, instance);
        }
    }

    private RankAccess resolveRankAccess(Map<String, String> environment) {
        String jdbcUrl = firstOrBlank(
                environment.get("TAVALL_POSTGRES_URL"),
                environment.get("RESOURCE_GAME_POSTGRES_URL")
        );
        if (jdbcUrl.isBlank()) {
            return new InMemoryRankRepository();
        }
        String username = firstOrBlank(
                environment.get("TAVALL_POSTGRES_USER"),
                environment.get("RESOURCE_GAME_POSTGRES_USER")
        );
        String password = firstOrBlank(
                environment.get("TAVALL_POSTGRES_PASSWORD"),
                environment.get("RESOURCE_GAME_POSTGRES_PASSWORD")
        );
        BackendConnectionProvider connectionProvider = () -> {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException exception) {
                throw new SQLException("Postgres JDBC driver is not available.", exception);
            }
            return DriverManager.getConnection(jdbcUrl, username, password);
        };
        new RankSchemaBootstrap().ensureSchema(connectionProvider);
        return new PostgresRankRepository(connectionProvider);
    }

    private KingdomCacheConfig resolveKingdomCacheConfig() {
        return org.tavall.dependency.DependencyLoaderAccess.findOptionalInstance(KingdomCacheConfig.class)
                .map(KingdomCacheConfig.class::cast)
                .orElseGet(() -> KingdomCacheConfig.fromEnvironment(System.getenv()));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new IllegalStateException("No Postgres JDBC URL is configured for rank storage.");
    }

    private String firstOrBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}



