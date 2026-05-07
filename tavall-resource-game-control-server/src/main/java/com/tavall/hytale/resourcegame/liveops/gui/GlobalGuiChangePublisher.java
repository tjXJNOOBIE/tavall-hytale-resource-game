package com.tavall.hytale.resourcegame.liveops.gui;

@FunctionalInterface
public interface GlobalGuiChangePublisher {
    void publish(GlobalGuiChange change);
}
