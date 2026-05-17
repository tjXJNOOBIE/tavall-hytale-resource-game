package com.tavall.resourcegame.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.ui.UiPageFactory;
import com.tavall.resourcegame.ui.UiPageType;

public interface IUiPageRegistry extends IDependencyInjectableInterface {
    void register(UiPageType type, UiPageFactory factory);

    UiPageFactory get(UiPageType type);
}