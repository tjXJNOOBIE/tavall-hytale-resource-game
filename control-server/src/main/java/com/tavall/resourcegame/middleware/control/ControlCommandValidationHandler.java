package com.tavall.resourcegame.middleware.control;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.ArrayList;
import java.util.List;

public final class ControlCommandValidationHandler implements IControlCommandDomain, IDependencyInjectableConcrete {
    public List<String> validateCommand(ControlCommand command) {
        ArrayList<String> validationErrors = new ArrayList<>();
        ControlCommandDefinition definition;
        try {
            definition = getControlCommandRegistry().definition(command.commandType());
        } catch (ControlCommandValidationException exception) {
            validationErrors.add(exception.getMessage());
            return validationErrors;
        }
        if (!command.issuedBy().enabled()) {
            validationErrors.add("Control operator is disabled.");
        }
        ControlCommandPermissionRequirement requirement = definition.permissionRequirement();
        if (!command.issuedBy().role().hasPermission(requirement.permission())) {
            validationErrors.add("Operator lacks permission " + requirement.permission() + ".");
        }
        if (requirement.highRisk() && !command.issuedBy().role().elevatedForHighRiskCommand()) {
            validationErrors.add("High-risk command requires ADMIN, OWNER, or SYSTEM role with 2FA/high-risk policy hook.");
        }
        if (command.dryRun() && !definition.dryRunSupported()) {
            validationErrors.add("Command does not support dry-run.");
        }
        if (!definition.supportedTargetScopes().contains(command.targetScope())) {
            validationErrors.add("Target scope " + command.targetScope() + " is not supported for " + command.commandType() + ".");
        }
        for (ControlCommandArgumentDefinition argument : definition.arguments()) {
            String value = command.arguments().get(argument.argumentName());
            if (argument.required() && (value == null || value.isBlank())) {
                validationErrors.add("Missing required argument: " + argument.argumentName() + ".");
            }
        }
        return List.copyOf(validationErrors);
    }
}
