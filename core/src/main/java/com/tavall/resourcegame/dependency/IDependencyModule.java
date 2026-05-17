package com.tavall.resourcegame.dependency;

/**
 * Composition root contract for repo-local DI setup.
 */
public interface IDependencyModule extends IDependencyInjectableConcrete {
    void registerDependencies();
}