package org.tavall.control.runtime;

@FunctionalInterface
interface CheckedBooleanSupplier {
    boolean getAsBoolean() throws Exception;
}
