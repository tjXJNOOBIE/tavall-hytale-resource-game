package com.tavall.hytale.resourcegame.liveops.config;

@FunctionalInterface
public interface LiveConfigChangePublisher {
    void publish(LiveConfigChange change);
}
