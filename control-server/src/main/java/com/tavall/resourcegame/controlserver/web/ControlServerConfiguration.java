package org.tavall.control.web;

import org.tavall.control.ControlServerDependencyModule;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControlServerConfiguration {
    @PostConstruct
    void registerControlServerDependencies() {
        new ControlServerDependencyModule().registerDependencies();
    }
}
