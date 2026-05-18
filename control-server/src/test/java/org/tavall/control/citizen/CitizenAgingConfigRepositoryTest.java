package org.tavall.control.citizen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CitizenAgingConfigRepositoryTest {
    @Test
    void inMemoryRepositorySeedsAndPersistsTheCurrentConfig() {
        InMemoryCitizenAgingConfigRepository repository = new InMemoryCitizenAgingConfigRepository();

        CitizenAgingConfig seed = repository.current();
        assertEquals(CitizenAgingConfig.defaults(), seed);

        CitizenAgingConfig updated = new CitizenAgingConfig(
                false,
                42L,
                8,
                CitizenAgingConfig.defaultThresholds(),
                99L,
                java.util.Map.of("seed", "test")
        );
        repository.save(updated);

        assertEquals(updated, repository.current());
    }
}
