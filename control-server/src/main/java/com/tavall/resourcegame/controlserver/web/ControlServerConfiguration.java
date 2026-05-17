package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.ControlServerDependencyModule;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControlServerConfiguration {
    @PostConstruct
    void registerControlServerDependencies() {
        new ControlServerDependencyModule().registerDependencies();
    }
}
