package com.tavall.resourcegame.liveops.config;

@FunctionalInterface
public interface LiveConfigChangePublisher {
    void publish(LiveConfigChange change);
}
