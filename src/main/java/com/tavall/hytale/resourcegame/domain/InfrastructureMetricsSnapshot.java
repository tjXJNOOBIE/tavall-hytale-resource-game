package com.tavall.hytale.resourcegame.domain;

/**
 * Point-in-time persistence and cache metrics for operator/debug surfaces.
 */
public record InfrastructureMetricsSnapshot(
        long profileCacheHits,
        long profileCacheMisses,
        long gameStateCacheHits,
        long gameStateCacheMisses,
        long cacheReadFailures,
        long cacheWriteSuccesses,
        long cacheWriteFailures,
        long cacheReadTotalNanos,
        long cacheWriteTotalNanos,
        long profileStoreReadCount,
        long profileStoreReadFailures,
        long profileStoreReadTotalNanos,
        long gameStateStoreReadCount,
        long gameStateStoreReadFailures,
        long gameStateStoreReadTotalNanos,
        long profileSaveCount,
        long profileSaveFailures,
        long profileSaveTotalNanos,
        long gameStateSaveCount,
        long gameStateSaveFailures,
        long gameStateSaveTotalNanos
) {
    public static InfrastructureMetricsSnapshot empty() {
        return new InfrastructureMetricsSnapshot(
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L
        );
    }

    public long totalCacheHits() {
        return profileCacheHits + gameStateCacheHits;
    }

    public long totalCacheMisses() {
        return profileCacheMisses + gameStateCacheMisses;
    }

    public long successfulCacheReads() {
        return totalCacheHits() + totalCacheMisses();
    }

    public long totalCacheReads() {
        return successfulCacheReads() + cacheReadFailures;
    }

    public double cacheHitRate() {
        return ratio(totalCacheHits(), successfulCacheReads());
    }

    public double averageCacheReadMillis() {
        return averageMillis(cacheReadTotalNanos, totalCacheReads());
    }

    public double averageCacheWriteMillis() {
        return averageMillis(cacheWriteTotalNanos, cacheWriteSuccesses + cacheWriteFailures);
    }

    public double averageProfileStoreReadMillis() {
        return averageMillis(profileStoreReadTotalNanos, profileStoreReadCount + profileStoreReadFailures);
    }

    public double averageGameStateStoreReadMillis() {
        return averageMillis(gameStateStoreReadTotalNanos, gameStateStoreReadCount + gameStateStoreReadFailures);
    }

    public double averageProfileSaveMillis() {
        return averageMillis(profileSaveTotalNanos, profileSaveCount + profileSaveFailures);
    }

    public double averageGameStateSaveMillis() {
        return averageMillis(gameStateSaveTotalNanos, gameStateSaveCount + gameStateSaveFailures);
    }

    public String cacheHitRateSummary() {
        return String.format("%.1f%% (%d hits / %d reads)", cacheHitRate() * 100.0D, totalCacheHits(), successfulCacheReads());
    }

    public String saveLatencySummary() {
        return String.format(
                "profile %.2f ms, game-state %.2f ms",
                averageProfileSaveMillis(),
                averageGameStateSaveMillis()
        );
    }

    private static double ratio(long numerator, long denominator) {
        if (denominator <= 0L) {
            return 0.0D;
        }
        return (double) numerator / (double) denominator;
    }

    private static double averageMillis(long totalNanos, long count) {
        if (count <= 0L) {
            return 0.0D;
        }
        return ((double) totalNanos / (double) count) / 1_000_000.0D;
    }
}
