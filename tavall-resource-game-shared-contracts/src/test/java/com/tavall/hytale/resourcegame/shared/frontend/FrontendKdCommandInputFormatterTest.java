package com.tavall.hytale.resourcegame.shared.frontend;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrontendKdCommandInputFormatterTest {
    @Test
    void prependsKdWhenAdapterReceivesCommandArgumentsOnly() {
        FrontendKdCommandInputFormatter formatter = new FrontendKdCommandInputFormatter();

        assertEquals("/kd resources add food 10", formatter.rawKdInput(List.of("resources", "add", "food", "10")));
    }

    @Test
    void preservesNativeKdAliasWhenAdapterReceivesFullCommand() {
        FrontendKdCommandInputFormatter formatter = new FrontendKdCommandInputFormatter();

        assertEquals("/kingdom troops debug troop-1", formatter.rawKdInput(List.of("kingdom", "troops", "debug", "troop-1")));
    }
}
