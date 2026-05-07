package com.tavall.hytale.resourcegame.ui;

import au.ellie.hyui.events.DynamicPageData;
import au.ellie.hyui.events.UIEventActions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class HyUiActionBindingTest {
    @Test
    void normalizesBotSelectorsToHyUiElementIds() {
        HyUiActionBinding binding = HyUiActionBinding.action("#EnterInteriorButton.Text", UiActions.ENTER_INTERIOR);

        assertEquals("EnterInteriorButton", binding.elementId());
        assertEquals(UiActions.ENTER_INTERIOR, binding.eventData().action());
    }

    @Test
    void preservesPayloadForInternalUiActions() {
        HyUiActionBinding binding = HyUiActionBinding.action(
                "#StageFarmsteadButton",
                UiActions.BUILDING_STAGE,
                "farmstead",
                UiPageType.CASTLE_BUILDINGS
        );

        assertEquals("StageFarmsteadButton", binding.elementId());
        assertEquals(UiActions.BUILDING_STAGE, binding.eventData().action());
        assertEquals(UiPageType.CASTLE_BUILDINGS.name() + UiActionService.COMMAND_RETURN_SEPARATOR + "farmstead", binding.eventData().payload());
    }

    @Test
    void preservesDirectActionEventsForBotHarnesses() {
        DynamicPageData data = new DynamicPageData();
        data.action = UiActions.OPEN_CASTLE_MAIN;
        data.values.put(UiActionEventData.KEY_PAYLOAD, "castle");

        UiActionEventData eventData = BaseUiPage.directAction(data);

        assertEquals(UiActions.OPEN_CASTLE_MAIN, eventData.action());
        assertEquals("castle", eventData.payload());
    }

    @Test
    void ignoresHyUiButtonEventsAlreadyHandledByCallbacks() {
        DynamicPageData data = new DynamicPageData();
        data.action = UIEventActions.BUTTON_CLICKED;

        assertNull(BaseUiPage.directAction(data));
    }
}
