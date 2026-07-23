package org.tavall.control.citizen;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface CitizenAgingConfigRepository extends IDependencyInjectableInterface {
    CitizenAgingConfig current();

    CitizenAgingConfig save(CitizenAgingConfig config);
}
