package org.tavall.control.services;

import org.tavall.control.domain.InfrastructureMetricsSnapshot;

import java.util.concurrent.atomic.LongAdder;

/**
 * Records lightweight cache and persistence timings without tying the plugin to a metrics backend.
 */
public final class InfrastructureMetricsRecorder {
    private static final InfrastructureMetricsRecorder DEFAULT_RECORDER = new InfrastructureMetricsRecorder();

    private final LongAdder profileCacheHits = new LongAdder();
    private final LongAdder profileCacheMisses = new LongAdder();
    private final LongAdder gameStateCacheHits = new LongAdder();
    private final LongAdder gameStateCacheMisses = new LongAdder();
    private final LongAdder cacheReadFailures = new LongAdder();
    private final LongAdder cacheWriteSuccesses = new LongAdder();
    private final LongAdder cacheWriteFailures = new LongAdder();
    private final LongAdder cacheReadTotalNanos = new LongAdder();
    private final LongAdder cacheWriteTotalNanos = new LongAdder();
    private final LongAdder profileStoreReadCount = new LongAdder();
    private final LongAdder profileStoreReadFailures = new LongAdder();
    private final LongAdder profileStoreReadTotalNanos = new LongAdder();
    private final LongAdder gameStateStoreReadCount = new LongAdder();
    private final LongAdder gameStateStoreReadFailures = new LongAdder();
    private final LongAdder gameStateStoreReadTotalNanos = new LongAdder();
    private final LongAdder profileSaveCount = new LongAdder();
    private final LongAdder profileSaveFailures = new LongAdder();
    private final LongAdder profileSaveTotalNanos = new LongAdder();
    private final LongAdder gameStateSaveCount = new LongAdder();
    private final LongAdder gameStateSaveFailures = new LongAdder();
    private final LongAdder gameStateSaveTotalNanos = new LongAdder();

    public static InfrastructureMetricsRecorder defaultRecorder() {
        return DEFAULT_RECORDER;
    }

    public static InfrastructureMetricsRecorder isolated() {
        return new InfrastructureMetricsRecorder();
    }

    public void recordProfileCacheRead(boolean hit, boolean success, long elapsedNanos) {
        recordCacheRead(profileCacheHits, profileCacheMisses, hit, success, elapsedNanos);
    }

    public void recordGameStateCacheRead(boolean hit, boolean success, long elapsedNanos) {
        recordCacheRead(gameStateCacheHits, gameStateCacheMisses, hit, success, elapsedNanos);
    }

    public void recordCacheWrite(boolean success, long elapsedNanos) {
        addNonNegative(cacheWriteTotalNanos, elapsedNanos);
        if (success) {
            cacheWriteSuccesses.increment();
        } else {
            cacheWriteFailures.increment();
        }
    }

    public void recordProfileStoreRead(boolean success, long elapsedNanos) {
        addNonNegative(profileStoreReadTotalNanos, elapsedNanos);
        if (success) {
            profileStoreReadCount.increment();
        } else {
            profileStoreReadFailures.increment();
        }
    }

    public void recordGameStateStoreRead(boolean success, long elapsedNanos) {
        addNonNegative(gameStateStoreReadTotalNanos, elapsedNanos);
        if (success) {
            gameStateStoreReadCount.increment();
        } else {
            gameStateStoreReadFailures.increment();
        }
    }

    public void recordProfileSave(boolean success, long elapsedNanos) {
        addNonNegative(profileSaveTotalNanos, elapsedNanos);
        if (success) {
            profileSaveCount.increment();
        } else {
            profileSaveFailures.increment();
        }
    }

    public void recordGameStateSave(boolean success, long elapsedNanos) {
        addNonNegative(gameStateSaveTotalNanos, elapsedNanos);
        if (success) {
            gameStateSaveCount.increment();
        } else {
            gameStateSaveFailures.increment();
        }
    }

    public InfrastructureMetricsSnapshot snapshot() {
        return new InfrastructureMetricsSnapshot(
                profileCacheHits.sum(),
                profileCacheMisses.sum(),
                gameStateCacheHits.sum(),
                gameStateCacheMisses.sum(),
                cacheReadFailures.sum(),
                cacheWriteSuccesses.sum(),
                cacheWriteFailures.sum(),
                cacheReadTotalNanos.sum(),
                cacheWriteTotalNanos.sum(),
                profileStoreReadCount.sum(),
                profileStoreReadFailures.sum(),
                profileStoreReadTotalNanos.sum(),
                gameStateStoreReadCount.sum(),
                gameStateStoreReadFailures.sum(),
                gameStateStoreReadTotalNanos.sum(),
                profileSaveCount.sum(),
                profileSaveFailures.sum(),
                profileSaveTotalNanos.sum(),
                gameStateSaveCount.sum(),
                gameStateSaveFailures.sum(),
                gameStateSaveTotalNanos.sum()
        );
    }

    private void recordCacheRead(LongAdder hits, LongAdder misses, boolean hit, boolean success, long elapsedNanos) {
        addNonNegative(cacheReadTotalNanos, elapsedNanos);
        if (!success) {
            cacheReadFailures.increment();
            return;
        }
        if (hit) {
            hits.increment();
        } else {
            misses.increment();
        }
    }

    private static void addNonNegative(LongAdder target, long elapsedNanos) {
        target.add(Math.max(0L, elapsedNanos));
    }
}
