package org.tavall.control.runtime;

import org.tavall.dependency.IDependencyInjectableConcrete;

public final class ControlCommandResultHandler implements ControlCommandDomain, IDependencyInjectableConcrete {
    public ControlCommandResult recordResult(ControlCommandResult result) {
        return getControlCommandResultRepository().saveResult(result);
    }
}
