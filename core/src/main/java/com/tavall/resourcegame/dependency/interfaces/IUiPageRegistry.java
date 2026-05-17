package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.ui.UiPageFactory;
import com.tavall.resourcegame.ui.UiPageType;

public interface IUiPageRegistry extends IDependencyInjectableInterface {
    void register(UiPageType type, UiPageFactory factory);

    UiPageFactory get(UiPageType type);
}