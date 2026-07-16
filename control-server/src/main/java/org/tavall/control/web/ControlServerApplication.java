package org.tavall.control.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.tavall.control.web")
public class ControlServerApplication {
    public static void main(String[] args) {
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
        SpringApplication.run(ControlServerApplication.class, args);
    }
}
