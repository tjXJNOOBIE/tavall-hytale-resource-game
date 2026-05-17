package com.tavall.resourcegame.services;

@FunctionalInterface
interface CheckedBooleanSupplier {
    boolean getAsBoolean() throws Exception;
}
