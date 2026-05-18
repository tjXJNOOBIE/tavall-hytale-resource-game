package org.tavall.control.liveops.config;

@FunctionalInterface
public interface LiveConfigChangePublisher {
    void publish(LiveConfigChange change);
}
