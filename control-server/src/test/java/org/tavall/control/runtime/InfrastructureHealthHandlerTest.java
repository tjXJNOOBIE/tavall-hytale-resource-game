package org.tavall.control.runtime;
import org.tavall.control.runtime.InfrastructureMetricsRecorder;

import org.tavall.control.config.CacheConfig;
import org.tavall.control.config.DatabaseConfig;
import org.tavall.control.domain.InfrastructureHealthSnapshot;
import org.tavall.control.domain.InfrastructureMetricsSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class InfrastructureHealthHandlerTest {
    @Test
    void snapshotReportsLocalFallbackWhenExternalSystemsAreNotConfigured() {
        InfrastructureHealthHandler service = new InfrastructureHealthHandler(
                new CacheConfig("", 6379, "", false),
                new DatabaseConfig("", "", "")
        );

        InfrastructureHealthSnapshot snapshot = service.snapshot();

        assertFalse(snapshot.redisConfigured());
        assertFalse(snapshot.postgresConfigured());
        assertEquals("memory-only (Redis not configured)", snapshot.cacheSummary());
        assertEquals("in-memory fallback (Postgres not configured)", snapshot.persistenceSummary());
    }

    @Test
    void metricsSnapshotUsesSharedRecorder() {
        InfrastructureMetricsRecorder metricsRecorder = InfrastructureMetricsRecorder.isolated();
        metricsRecorder.recordProfileCacheRead(true, true, 1_000_000L);
        metricsRecorder.recordProfileSave(true, 2_000_000L);
        InfrastructureHealthHandler service = new InfrastructureHealthHandler(
                new CacheConfig("", 6379, "", false),
                new DatabaseConfig("", "", ""),
                metricsRecorder
        );

        InfrastructureMetricsSnapshot snapshot = service.metricsSnapshot();

        assertEquals(1L, snapshot.profileCacheHits());
        assertEquals(1.0D, snapshot.cacheHitRate());
        assertEquals(2.0D, snapshot.averageProfileSaveMillis());
    }
}
