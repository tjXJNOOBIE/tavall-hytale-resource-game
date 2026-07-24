package org.tavall.control.events;

import org.tavall.dependency.IDependencyInjectableConcrete;

public record GameEventRedisConfig(
        String channel,
        String redisUrl,
        boolean distributedEventsEnabled
) implements IDependencyInjectableConcrete {
    public static final String DEFAULT_CHANNEL = "resource-game:events";

    public GameEventRedisConfig(String channel) {
        this(channel, null, false);
    }

    public GameEventRedisConfig {
        if (channel == null || channel.isBlank()) {
            channel = DEFAULT_CHANNEL;
        }
        if (redisUrl != null && redisUrl.isBlank()) {
            redisUrl = null;
        }
        distributedEventsEnabled = distributedEventsEnabled && redisUrl != null;
    }

    public static GameEventRedisConfig fromEnvironment() {
        return new GameEventRedisConfig(
                envOrDefault("TAVALL_GAME_EVENT_REDIS_CHANNEL", DEFAULT_CHANNEL),
                firstPresent("TAVALL_GAME_EVENT_REDIS_URL", "RESOURCE_GAME_EVENT_REDIS_URL", "REDIS_URL"),
                firstPresent("TAVALL_GAME_EVENT_REDIS_URL", "RESOURCE_GAME_EVENT_REDIS_URL", "REDIS_URL") != null
        );
    }

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String firstPresent(String first, String second, String third) {
        String firstValue = System.getenv(first);
        if (firstValue != null && !firstValue.isBlank()) {
            return firstValue;
        }
        String secondValue = System.getenv(second);
        if (secondValue != null && !secondValue.isBlank()) {
            return secondValue;
        }
        String thirdValue = System.getenv(third);
        if (thirdValue != null && !thirdValue.isBlank()) {
            return thirdValue;
        }
        return null;
    }
}
