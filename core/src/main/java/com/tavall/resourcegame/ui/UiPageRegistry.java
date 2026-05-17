package com.tavall.resourcegame.ui;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.dependency.interfaces.IUiPageRegistry;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry of UI page factories.
 */
public final class UiPageRegistry implements IUiPageRegistry, IDependencyInjectableConcrete {
    private final Map<UiPageType, UiPageFactory> factories = new EnumMap<>(UiPageType.class);

    public void register(UiPageType type, UiPageFactory factory) {
        factories.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(factory, "factory"));
    }

    public UiPageFactory get(UiPageType type) {
        return factories.get(type);
    }
}
