package org.tavall.control.liveops.gui;

@FunctionalInterface
public interface GlobalGuiChangePublisher {
    void publish(GlobalGuiChange change);
}
