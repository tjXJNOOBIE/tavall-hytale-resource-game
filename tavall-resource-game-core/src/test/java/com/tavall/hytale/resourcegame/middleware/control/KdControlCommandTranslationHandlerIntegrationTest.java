package com.tavall.hytale.resourcegame.middleware.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KdControlCommandTranslationHandlerIntegrationTest {
    @Test
    void translatesCanonicalKdCommandsToControlConsoleCommands() {
        KdControlCommandTranslationHandler translationHandler = new KdControlCommandTranslationHandler();

        assertEquals("tick healing 2", translationHandler.translateKdCommand("kd tick run 2").orElseThrow());
        assertEquals(
                "kingdom create --displayName First --worldId default --borderSize 1000",
                translationHandler.translateKdCommand("kd kingdom create --displayName First --worldId default --borderSize 1000").orElseThrow()
        );
        assertEquals(
                "coord convert minecraft default 10 64 20",
                translationHandler.translateKdCommand("kd coord convert minecraft default 10 64 20").orElseThrow()
        );
        assertEquals(
                "instance routing debug kingdom-1 minecraft",
                translationHandler.translateKdCommand("kd instance routing debug kingdom-1 minecraft").orElseThrow()
        );
        assertEquals("params list", translationHandler.translateKdCommand("kd params list").orElseThrow());
        assertEquals(
                "resource give player-1 resource.food.rations 10",
                translationHandler.translateKdCommand("kingdom resources give player-1 resource.food.rations 10").orElseThrow()
        );
        assertEquals("troop debug troop-1", translationHandler.translateKdCommand("kd troops debug troop-1").orElseThrow());
        assertEquals("player debug player-1", translationHandler.translateKdCommand("kd account debug player-1").orElseThrow());
        assertEquals("companion give player-1 ARCANE", translationHandler.translateKdCommand("kd companion give player-1 ARCANE").orElseThrow());
        assertEquals("companion wall assign player-1 companion-1 north", translationHandler.translateKdCommand("kd companion wall assign player-1 companion-1 north").orElseThrow());
    }

    @Test
    void recognizesFrontendLocalKdCategoriesForControlAuditVerification() {
        KdControlCommandTranslationHandler translationHandler = new KdControlCommandTranslationHandler();

        assertEquals("ui", translationHandler.category("kd ui castle-main"));
        assertEquals("trade", translationHandler.category("kd trade route inspect route-1"));
        assertEquals("market", translationHandler.category("kd market listing inspect listing-1"));
        assertEquals("scout", translationHandler.category("kd scout report report-1"));
        assertEquals("recon", translationHandler.category("kd recon profile player-1"));
        assertEquals("intel", translationHandler.category("kd intel report report-1"));
        assertEquals("retaliation", translationHandler.category("kd retaliation debug rule-1"));
        assertEquals("companion", translationHandler.category("kd companion list player-1"));
    }

    @Test
    void leavesUnimplementedKdCategoryAsVerifiedLocalActionInsteadOfFabricatingCommand() {
        KdControlCommandTranslationHandler translationHandler = new KdControlCommandTranslationHandler();

        assertTrue(translationHandler.translateKdCommand("kd market listing inspect listing-1").isEmpty());
        assertEquals("unknown", translationHandler.category("kd impossible command"));
    }
}
