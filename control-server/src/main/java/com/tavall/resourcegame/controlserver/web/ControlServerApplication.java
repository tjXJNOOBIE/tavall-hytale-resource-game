package com.tavall.resourcegame.controlserver.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.tavall.resourcegame.controlserver.web")
public class ControlServerApplication {
    public static void main(String[] args) {
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
        SpringApplication.run(ControlServerApplication.class, args);
    }
}
