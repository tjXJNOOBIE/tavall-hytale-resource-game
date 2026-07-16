package org.tavall.control.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.citizen.cache.CitizenAgingConfigCache;
import org.tavall.control.transport.JsonMapperProvider;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class InMemoryCitizenAgingConfigRepository implements CitizenAgingConfigRepository, IDependencyInjectableConcrete {
    private final AtomicReference<CitizenAgingConfig> current;
    private final CitizenAgingConfigCache cache;

    public InMemoryCitizenAgingConfigRepository() {
        this(CitizenAgingConfig.defaults(), createDefaultCache());
    }

    public InMemoryCitizenAgingConfigRepository(CitizenAgingConfig seed, CitizenAgingConfigCache cache) {
        this.current = new AtomicReference<>(seed);
        this.cache = cache;
        this.cache.prime(seed);
    }

    @Override
    public CitizenAgingConfig current() {
        return cache.read().orElseGet(() -> {
            CitizenAgingConfig config = current.get();
            cache.prime(config);
            return config;
        });
    }

    @Override
    public CitizenAgingConfig save(CitizenAgingConfig config) {
        current.set(config);
        cache.write(config);
        return config;
    }

    private static CitizenAgingConfigCache createDefaultCache() {
        String cacheSuffix = UUID.randomUUID().toString();
        ObjectMapper objectMapper = new JsonMapperProvider().mapper();
        return CitizenAgingConfigCache.openInMemory(cacheSuffix, objectMapper);
    }
}
