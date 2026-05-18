package org.tavall.control.services;

@FunctionalInterface
interface CheckedBooleanSupplier {
    boolean getAsBoolean() throws Exception;
}
