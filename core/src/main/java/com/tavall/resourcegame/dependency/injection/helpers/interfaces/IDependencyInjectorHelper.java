package com.tavall.resourcegame.dependency.injection.helpers.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.dependency.IDependencyModule;

/**
 * Bootstraps the repo-local DI registry.
 */
public interface IDependencyInjectorHelper extends IDependencyInjectableConcrete {
    void setupDISystem(IDependencyModule module);
}