package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface CitizenAgingConfigRepository extends IDependencyInjectableInterface {
    CitizenAgingConfig current();

    CitizenAgingConfig save(CitizenAgingConfig config);
}
