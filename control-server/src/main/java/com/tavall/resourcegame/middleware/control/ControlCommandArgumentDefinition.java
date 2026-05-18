package org.tavall.control.runtime;

public record ControlCommandArgumentDefinition(
        String argumentName,
        boolean required,
        String description
) {
    public ControlCommandArgumentDefinition {
        if (argumentName == null || argumentName.isBlank()) {
            throw new IllegalArgumentException("argumentName is required.");
        }
        description = description == null ? "" : description;
    }
}
