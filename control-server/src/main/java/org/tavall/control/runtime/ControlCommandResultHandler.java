package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class ControlCommandResultHandler implements IControlCommandDomain, IDependencyInjectableConcrete {
    public ControlCommandResult recordResult(ControlCommandResult result) {
        return getControlCommandResultRepository().saveResult(result);
    }
}
