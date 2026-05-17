package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.config.DatabaseConfig;
import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tavall.resourcegame.persistence.PostgresConnectionProvider;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class MinecraftBukkitDirectControlRuntimeHandler implements IMinecraftBukkitDirectControlRuntimeHandler, IDependencyInjectableConcrete {
    @Override
    public ControlCommandRuntime controlRuntime() {
        return DependencyLoaderAccess.findOptionalInstance(ControlCommandRuntime.class)
                .orElseGet(this::createControlRuntime);
    }

    private ControlCommandRuntime createControlRuntime() {
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnv();
        ControlCommandRuntime runtime = databaseConfig.jdbcUrl() == null || databaseConfig.jdbcUrl().isBlank()
                ? ControlCommandRuntimeFactory.createInMemoryRuntime()
                : ControlCommandRuntimeFactory.createPostgresRuntime(new PostgresConnectionProvider(databaseConfig));
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, runtime);
        return runtime;
    }
}
