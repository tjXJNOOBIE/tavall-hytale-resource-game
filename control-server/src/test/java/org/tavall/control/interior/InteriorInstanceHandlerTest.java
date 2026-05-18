package org.tavall.control.interior;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class InteriorInstanceHandlerTest {
    @Test
    void worldNameForUsesSharedInteriorWorld() {
        InteriorInstanceHandler service = new InteriorInstanceHandler();

        String firstWorldName = service.worldNameFor(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        String secondWorldName = service.worldNameFor(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"));

        assertEquals("kingdom-interiors", firstWorldName);
        assertEquals(firstWorldName, secondWorldName);
    }
}
