package com.tavall.resourcegame.liveops.gui;

@FunctionalInterface
public interface GlobalGuiChangePublisher {
    void publish(GlobalGuiChange change);
}
