package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class ControlServerConfiguration {
    @Bean
    ControlCommandRuntime controlCommandRuntime() {
        return ControlCommandRuntimeFactory.createInMemoryRuntime();
    }

    @Bean
    ControlOperator webControlOperator() {
        return ControlOperator.localOwner(Instant.now());
    }
}
