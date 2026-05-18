package org.tavall.control;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.tavall.control.dependency.composition.domains.IResourceGameDomain;
import org.tavall.control.bootstrap.ControlServerBootstrap;

import javax.annotation.Nonnull;

public class ResourceGamePlugin extends JavaPlugin implements IResourceGameDomain {
    private final ControlServerBootstrap bootstrap = new ControlServerBootstrap();

    public ResourceGamePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        bootstrap.setup(this);
    }

    @Override
    protected void start() {
        bootstrap.start(this);
    }

    @Override
    protected void shutdown() {
        bootstrap.shutdown(this);
    }
}
