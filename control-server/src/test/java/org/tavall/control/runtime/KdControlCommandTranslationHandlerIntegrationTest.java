package org.tavall.control.runtime;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("account debug player-1 on", translationHandler.translateKdCommand("kd account debug on player-1").orElseThrow());
        assertEquals("account status minecraft-player-1", translationHandler.translateKdCommand("kd account status", "minecraft-player-1").orElseThrow());
        assertEquals("account addxp minecraft-player-1 10", translationHandler.translateKdCommand("kd account addxp 10", "minecraft-player-1").orElseThrow());
        assertEquals("account setlevel minecraft-player-1 8", translationHandler.translateKdCommand("kd account setlevel 8", "minecraft-player-1").orElseThrow());
        assertEquals("companion list minecraft-player-1", translationHandler.translateKdCommand("kd companion list", "minecraft-player-1").orElseThrow());
        assertEquals("companion give minecraft-player-1 ARCANE", translationHandler.translateKdCommand("kd companion give ARCANE", "minecraft-player-1").orElseThrow());
        assertEquals("companion train minecraft-player-1 companion-1", translationHandler.translateKdCommand("kd companion train companion-1", "minecraft-player-1").orElseThrow());
        assertEquals("companion wall assign minecraft-player-1 companion-1 north", translationHandler.translateKdCommand("kd companion wall assign companion-1 north", "minecraft-player-1").orElseThrow());
        assertEquals("companion skill unlock minecraft-player-1 companion-1 arcane-lance", translationHandler.translateKdCommand("kd companion skill unlock companion-1 arcane-lance", "minecraft-player-1").orElseThrow());
        assertEquals("companion give player-1 ARCANE", translationHandler.translateKdCommand("kd companion give player-1 ARCANE").orElseThrow());
        assertEquals("companion wall assign player-1 companion-1 north", translationHandler.translateKdCommand("kd companion wall assign player-1 companion-1 north").orElseThrow());
        assertEquals("player debug minecraft-player-1", translationHandler.translateKdCommand("kd data", "minecraft-player-1").orElseThrow());
        assertEquals("citizens spawn minecraft-player-1 3 kingdom-1", translationHandler.translateKdCommand("kd citizens add 3", "minecraft-player-1").orElseThrow());
        assertEquals("resource give minecraft-player-1 resource.food 10", translationHandler.translateKdCommand("kd resources add food 10", "minecraft-player-1").orElseThrow());
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
        assertEquals("buildings", translationHandler.category("kd buildings list"));
        assertEquals("building", translationHandler.category("kd building list"));
        assertEquals("help", translationHandler.category("kd help"));
    }

    @Test
    void leavesUnimplementedKdCategoryAsVerifiedLocalActionInsteadOfFabricatingCommand() {
        KdControlCommandTranslationHandler translationHandler = new KdControlCommandTranslationHandler();

        assertEquals(
                "frontend kd market kd market listing inspect listing-1",
                translationHandler.translateKdCommand("kd market listing inspect listing-1").orElseThrow()
        );
        assertEquals(
                "frontend kd ui kd ui castle-main",
                translationHandler.translateKdCommand("kd ui castle-main").orElseThrow()
        );
        assertEquals("unknown", translationHandler.category("kd impossible command"));
    }

    @Test
    void translatesAndParsesMinecraftKingdomCommandPermutationsFromCommandSystem() {
        KdControlCommandTranslationHandler translationHandler = new KdControlCommandTranslationHandler();
        ControlCommandParsingHandler parsingHandler = new ControlCommandParsingHandler();
        Instant now = Instant.parse("2026-05-13T00:00:00Z");
        ControlOperator operator = ControlOperator.system(now);

        List<Map.Entry<String, String>> permutations = Arrays.asList(
                Map.entry("kd kingdom create --displayName First --worldId default --borderSize 1000", "kingdom create --displayName First --worldId default --borderSize 1000"),
                Map.entry("kd kingdom debug kingdom-1", "kingdom debug kingdom-1"),
                Map.entry("kd kingdom scaling evaluate", "kingdom scaling evaluate"),
                Map.entry("kd kingdom tick", "kingdom tick"),
                Map.entry("kd kingdom border create kingdom-1 -100 100 -100 100 default", "kingdom border create kingdom-1 -100 100 -100 100 default"),
                Map.entry("kd kingdom border update kingdom-1 -120 120 -120 120 default", "kingdom border update kingdom-1 -120 120 -120 120 default"),
                Map.entry("kd kingdom border debug default 10 64 20", "kingdom border debug default 10 64 20"),
                Map.entry("kd kingdom border resolve default 10 64 20", "kingdom border resolve default 10 64 20"),
                Map.entry("kd kingdom border simulate-crossing player-1 default 0 64 0 100 64 100", "kingdom border simulate-crossing player-1 default 0 64 0 100 64 100"),
                Map.entry("kd coord convert minecraft default 10 64 20", "coord convert minecraft default 10 64 20"),
                Map.entry("kd coord debug minecraft default 10 64 20", "coord debug minecraft default 10 64 20"),
                Map.entry("kd coord params minecraft platformScaleX=1 platformScaleY=1", "coord params minecraft platformScaleX=1 platformScaleY=1"),
                Map.entry("kd instance register minecraft kingdom-1 kingdom-1-minecraft-primary Kingdom_Primary", "instance register minecraft kingdom-1 kingdom-1-minecraft-primary Kingdom_Primary"),
                Map.entry("kd instance health kingdom-1-minecraft-primary ONLINE", "instance health kingdom-1-minecraft-primary ONLINE"),
                Map.entry("kd instance switch player-1 minecraft kingdom-1 kingdom-2", "instance switch player-1 minecraft kingdom-1 kingdom-2"),
                Map.entry("kd instance confirm switch-1", "instance confirm switch-1"),
                Map.entry("kd instance fail switch-1 denied", "instance fail switch-1 denied"),
                Map.entry("kd instance routing debug kingdom-1 minecraft", "instance routing debug kingdom-1 minecraft"),
                Map.entry("kd params list", "params list"),
                Map.entry("kd params get maxActivePlayers kingdom-1", "params get maxActivePlayers kingdom-1"),
                Map.entry("kd params set maxActivePlayers 300 KINGDOM kingdom-1", "params set maxActivePlayers 300 KINGDOM kingdom-1"),
                Map.entry("kd params dry-run maxActivePlayers 0 KINGDOM kingdom-1", "params dry-run maxActivePlayers 0 KINGDOM kingdom-1"),
                Map.entry("kd clock state kingdom-1", "clock state kingdom-1"),
                Map.entry("kd clock debug kingdom-1", "clock debug kingdom-1"),
                Map.entry("kd clock tick kingdom-1", "clock tick kingdom-1"),
                Map.entry("kd clock tick-all", "clock tick-all"),
                Map.entry("kd clock mode kingdom-1 ACCELERATED", "clock mode kingdom-1 ACCELERATED"),
                Map.entry("kd clock override kingdom-1 22:00", "clock override kingdom-1 22:00"),
                Map.entry("kd clock clear-override kingdom-1", "clock clear-override kingdom-1"),
                Map.entry("kd clock pause kingdom-1", "clock pause kingdom-1"),
                Map.entry("kd clock resume kingdom-1", "clock resume kingdom-1"),
                Map.entry("kd clock config kingdom-1 acceleratedTimeMultiplier=12", "clock config kingdom-1 acceleratedTimeMultiplier=12"),
                Map.entry("kd clock projection kingdom-1 minecraft", "clock projection kingdom-1 minecraft"),
                Map.entry("kd schedule active kingdom-1", "schedule active kingdom-1"),
                Map.entry("kd schedule debug kingdom-1", "schedule debug kingdom-1"),
                Map.entry("kd schedule create kingdom-1 SHOP_OPEN startHour=8 endHour=20", "schedule create kingdom-1 SHOP_OPEN startHour=8 endHour=20"),
                Map.entry("kd schedule enable schedule-1", "schedule enable schedule-1"),
                Map.entry("kd schedule disable schedule-1", "schedule disable schedule-1"),
                Map.entry("kd schedule apply kingdom-1", "schedule apply kingdom-1"),
                Map.entry("kd schedule projection kingdom-1 minecraft", "schedule projection kingdom-1 minecraft"),
                Map.entry("kd aging policy kingdom-1 enabled=true realMinutesPerAgeIncrement=60", "aging policy kingdom-1 enabled=true realMinutesPerAgeIncrement=60"),
                Map.entry("kd aging tick kingdom-1", "aging tick kingdom-1"),
                Map.entry("kd aging debug kingdom-1", "aging debug kingdom-1"),
                Map.entry("kd citizens spawn player-1 2 kingdom-1", "citizens spawn player-1 2 kingdom-1"),
                Map.entry("kd citizens create player-1 2 kingdom-1", "citizens create player-1 2 kingdom-1"),
                Map.entry("kd citizens migrate player-1 1 kingdom-1", "citizens migrate player-1 1 kingdom-1"),
                Map.entry("kd citizens list kingdom-1", "citizens list kingdom-1"),
                Map.entry("kd citizens summary kingdom-1", "citizens summary kingdom-1"),
                Map.entry("kd citizens refresh-cache kingdom-1", "citizens refresh-cache kingdom-1"),
                Map.entry("kd citizens refresh-displays kingdom-1", "citizens refresh-displays kingdom-1"),
                Map.entry("kd citizens debug citizen-1", "citizens debug citizen-1"),
                Map.entry("kd citizens get citizen-1", "citizens get citizen-1"),
                Map.entry("kd citizens age citizen-1 1", "citizens age citizen-1 1"),
                Map.entry("kd citizens ageall kingdom-1", "citizens ageall kingdom-1"),
                Map.entry("kd citizens maintenance kingdom-1", "citizens maintenance kingdom-1"),
                Map.entry("kd citizens setstage citizen-1 ADULT", "citizens setstage citizen-1 ADULT"),
                Map.entry("kd citizens setjob citizen-1 MINER", "citizens setjob citizen-1 MINER"),
                Map.entry("kd citizens clearjob citizen-1", "citizens clearjob citizen-1"),
                Map.entry("kd citizens train citizen-1", "citizens train citizen-1"),
                Map.entry("kd citizens promote citizen-1", "citizens promote citizen-1"),
                Map.entry("kd citizens demote citizen-1", "citizens demote citizen-1"),
                Map.entry("kd citizens health citizen-1 HEALTHY", "citizens health citizen-1 HEALTHY"),
                Map.entry("kd citizens morale citizen-1 HAPPY", "citizens morale citizen-1 HAPPY"),
                Map.entry("kd citizens nutrition citizen-1 FED", "citizens nutrition citizen-1 FED"),
                Map.entry("kd citizens housing citizen-1 HOUSED", "citizens housing citizen-1 HOUSED"),
                Map.entry("kd citizens food-effects kingdom-1", "citizens food-effects kingdom-1"),
                Map.entry("kd citizens morale-effects kingdom-1", "citizens morale-effects kingdom-1"),
                Map.entry("kd citizens night-rest kingdom-1", "citizens night-rest kingdom-1"),
                Map.entry("kd resources give player-1 resource.food 10", "resource give player-1 resource.food 10"),
                Map.entry("kd resources add food 10", "resource give minecraft-player-1 resource.food 10"),
                Map.entry("kd troops debug troop-1", "troop debug troop-1"),
                Map.entry("kd troops wound troop-1 GENERAL_WOUND MODERATE", "troop wound troop-1 GENERAL_WOUND MODERATE"),
                Map.entry("kd troops heal treatment player-1 troop-1 recipe.healing.general_wound.proper 4", "troop heal treatment player-1 troop-1 recipe.healing.general_wound.proper 4"),
                Map.entry("kd account status", "account status minecraft-player-1"),
                Map.entry("kd account debug on player-1", "account debug player-1 on"),
                Map.entry("kd companion list player-1", "companion list player-1"),
                Map.entry("kd companion list", "companion list minecraft-player-1"),
                Map.entry("kd companion give ARCANE", "companion give minecraft-player-1 ARCANE"),
                Map.entry("kd companion train companion-1", "companion train minecraft-player-1 companion-1"),
                Map.entry("kd companion skill unlock companion-1 arcane-lance", "companion skill unlock minecraft-player-1 companion-1 arcane-lance"),
                Map.entry("kd companion wall assign companion-1 north", "companion wall assign minecraft-player-1 companion-1 north"),
                Map.entry("kd companion give player-1 ARCANE", "companion give player-1 ARCANE"),
                Map.entry("kd companion wall assign player-1 companion-1 north", "companion wall assign player-1 companion-1 north"),
                Map.entry("kd data", "player debug minecraft-player-1"),
                Map.entry("kd ui castle-main", "frontend kd ui kd ui castle-main"),
                Map.entry("kd help", "frontend kd help kd help"),
                Map.entry("kd buildings list", "frontend kd buildings kd buildings list"),
                Map.entry("kd building list", "frontend kd building kd building list"),
                Map.entry("kd scene refresh", "frontend kd scene kd scene refresh")
        );

        for (Map.Entry<String, String> permutation : permutations) {
            String translated = translationHandler.translateKdCommand(permutation.getKey(), "minecraft-player-1").orElseThrow();

            assertEquals(permutation.getValue(), translated, permutation.getKey());
            parsingHandler.parseConsoleCommand(translated, operator, CommandIssuedFrom.MINECRAFT, now);
        }
    }
}
