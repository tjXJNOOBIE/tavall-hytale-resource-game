package org.tavall.control.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.ui.UiPageFactory;
import org.tavall.control.ui.UiPageType;

public interface IUiPageRegistry extends IDependencyInjectableInterface {
    void register(UiPageType type, UiPageFactory factory);

    UiPageFactory get(UiPageType type);
}