package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlRepositoryJsonCodecIntegrationTest {
    @Test
    void roundTripsControlRepositoryJsonShapes() {
        ControlRepositoryJsonCodec jsonCodec = new ControlRepositoryJsonCodec();
        PlatformCommandResult platformResult = new PlatformCommandResult(
                GamePlatform.DISCORD,
                true,
                "projection refreshed",
                List.of("event-1"),
                List.of("projection-1"),
                Map.of("correlation", "abc")
        );

        List<PlatformCommandResult> platformResults = jsonCodec.readPlatformResults(jsonCodec.writePlatformResults(List.of(platformResult)));
        Set<GamePlatform> platforms = jsonCodec.readPlatforms(jsonCodec.writePlatforms(Set.of(GamePlatform.MINECRAFT, GamePlatform.HYTALE)));
        List<String> strings = jsonCodec.readStringList(jsonCodec.writeStringList(List.of("one", "two")));
        Map<String, String> stringMap = jsonCodec.readStringMap(jsonCodec.writeStringMap(Map.of("token", "[REDACTED]", "amount", "10")));

        assertEquals(GamePlatform.DISCORD, platformResults.getFirst().platform());
        assertEquals("event-1", platformResults.getFirst().frontendEventIds().getFirst());
        assertEquals("abc", platformResults.getFirst().metadata().get("correlation"));
        assertTrue(platforms.contains(GamePlatform.MINECRAFT));
        assertTrue(platforms.contains(GamePlatform.HYTALE));
        assertEquals(List.of("one", "two"), strings);
        assertEquals("[REDACTED]", stringMap.get("token"));
    }
}
