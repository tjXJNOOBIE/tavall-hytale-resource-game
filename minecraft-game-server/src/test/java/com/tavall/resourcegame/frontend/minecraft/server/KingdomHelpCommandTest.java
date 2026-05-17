package com.tavall.resourcegame.frontend.minecraft.server;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class KingdomHelpCommandTest {
    @Test
    void helpLinesDescribeBothPlayerDataEntryPoints() {
        List<String> lines = KingdomHelpCommand.helpLines(false);

        assertTrue(lines.stream().anyMatch(line -> line.contains("/kd ui account")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/kd account")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("/kd data")));
    }
}
