package org.tavall.control.interaction;

import org.tavall.api.minecraft.interaction.InteractionMenuElement;
import org.tavall.api.minecraft.interaction.InteractionMenuModel;
import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.interaction.InteractionResultType;
import org.tavall.api.minecraft.interaction.InteractionTargetType;
import org.tavall.api.minecraft.ui.UiActions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ControlPlaneInteractionHandler {
    private static final int DEFAULT_BUILDING_MAX_LEVEL = 3;

    private final ConcurrentMap<String, NpcDefinition> npcDefinitions = new ConcurrentHashMap<String, NpcDefinition>();
    private final ConcurrentMap<String, NpcInstance> npcInstances = new ConcurrentHashMap<String, NpcInstance>();
    private final ConcurrentMap<String, BuildingInteractionState> buildingStates = new ConcurrentHashMap<String, BuildingInteractionState>();

    public InteractionResult handle(InteractionRequest request, Instant now) {
        if (request == null) {
            return result(null, InteractionResultType.ERROR, false, "Interaction request is required.", null, "Request was null.", Map.of());
        }
        if (request.targetType() == null || request.targetType() == InteractionTargetType.UNKNOWN) {
            return result(request.requestId(), InteractionResultType.NOT_FOUND, false, "Unknown interaction target.", null, "Unknown target.", metadata(request, Map.of()));
        }
        return switch (request.targetType()) {
            case NPC -> handleNpc(request, now);
            case BUILDING -> handleBuilding(request, now);
            case UNKNOWN -> result(request.requestId(), InteractionResultType.NOT_FOUND, false, "Unknown interaction target.", null, "Unknown target.", metadata(request, Map.of()));
        };
    }

    public InteractionResult inspectNpc(String npcId, Instant now) {
        NpcInstance instance = npcInstances.get(npcId);
        if (instance == null) {
            return result(npcId, InteractionResultType.NOT_FOUND, false, "NPC was not found.", null, "NPC not found.", Map.of("targetId", npcId));
        }
        NpcDefinition definition = npcDefinitions.get(instance.npcDefinitionId());
        InteractionMenuModel menu = npcMenu(instance, definition, now);
        return result(npcId, InteractionResultType.OPEN_MENU, true, menu.title() + " opened.", menu, null, menu.metadata());
    }

    public InteractionResult inspectBuilding(String buildingId, Instant now) {
        BuildingInteractionState state = buildingStates.get(buildingId);
        if (state == null) {
            return result(buildingId, InteractionResultType.NOT_FOUND, false, "Building was not found.", null, "Building not found.", Map.of("targetId", buildingId));
        }
        InteractionMenuModel menu = buildingMenu(state, now);
        return result(buildingId, InteractionResultType.OPEN_MENU, true, menu.title() + " opened.", menu, null, menu.metadata());
    }

    private InteractionResult handleNpc(InteractionRequest request, Instant now) {
        NpcDefinition definition = resolveNpcDefinition(request);
        NpcInstance instance = resolveNpcInstance(request, definition);
        String actionId = actionId(request);
        if (isOpenRequest(request) || actionId == null) {
            InteractionMenuModel menu = npcMenu(instance, definition, now);
            return result(request.requestId(), InteractionResultType.OPEN_MENU, true, menu.title() + " opened.", menu, null, menu.metadata());
        }
        if (UiActions.CLOSE.equals(actionId)) {
            return result(request.requestId(), InteractionResultType.EXECUTE_ACTION, true, "Menu closed.", null, null, metadata(request, Map.of("actionId", actionId)));
        }
        if (UiActions.OPEN_NPC_MAIN.equals(actionId) || UiActions.NPC_DEBUG.equals(actionId)) {
            InteractionMenuModel menu = npcMenu(instance, definition, now);
            return result(request.requestId(), InteractionResultType.OPEN_MENU, true, menu.title() + " opened.", menu, null, menu.metadata());
        }
        if (UiActions.NPC_OPEN_BUILDING.equals(actionId) || UiActions.OPEN_FARMSTEAD_MENU.equals(actionId)) {
            BuildingInteractionState buildingState = resolveAttachedBuilding(request, instance, definition);
            if (buildingState == null) {
                return result(request.requestId(), InteractionResultType.DENIED, false, "No building is attached to this NPC.", null, "No building is attached.", metadata(request, Map.of("actionId", actionId)));
            }
            InteractionMenuModel menu = buildingMenu(buildingState, now);
            return result(request.requestId(), InteractionResultType.OPEN_MENU, true, menu.title() + " opened.", menu, null, menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_MAIN.equals(actionId) || UiActions.OPEN_FARMSTEAD_UPGRADE.equals(actionId) || UiActions.OPEN_BUILDING_UPGRADE.equals(actionId) || UiActions.BUILDING_START_UPGRADE.equals(actionId)) {
            BuildingInteractionState buildingState = resolveAttachedBuilding(request, instance, definition);
            if (buildingState == null) {
                return result(request.requestId(), InteractionResultType.DENIED, false, "No building is attached to this NPC.", null, "No building is attached.", metadata(request, Map.of("actionId", actionId)));
            }
            return handleBuildingAction(request, buildingState, actionId, now);
        }
        InteractionMenuModel menu = npcMenu(instance, definition, now);
        return result(request.requestId(), InteractionResultType.ERROR, false, "Unsupported NPC action: " + actionId, menu, "Unsupported action.", menu.metadata());
    }

    private InteractionResult handleBuilding(InteractionRequest request, Instant now) {
        BuildingInteractionState state = resolveBuildingState(request);
        String actionId = actionId(request);
        if (isOpenRequest(request) || actionId == null) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.OPEN_MENU, true, menu.title() + " opened.", menu, null, menu.metadata());
        }
        if (UiActions.CLOSE.equals(actionId)) {
            return result(request.requestId(), InteractionResultType.EXECUTE_ACTION, true, "Menu closed.", null, null, metadata(request, Map.of("actionId", actionId)));
        }
        if (UiActions.OPEN_BUILDING_MAIN.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.OPEN_MENU, true, menu.title() + " opened.", menu, null, menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_STORAGE.equals(actionId) || UiActions.OPEN_BUILDING_PRODUCTION.equals(actionId) || UiActions.OPEN_BUILDING_UPGRADE.equals(actionId) || UiActions.BUILDING_OPEN_DETAIL.equals(actionId) || UiActions.OPEN_BUILDING_WORKERS.equals(actionId) || UiActions.OPEN_BUILDING_HEALING.equals(actionId)) {
            return handleBuildingAction(request, state, actionId, now);
        }
        if (UiActions.BUILDING_START_UPGRADE.equals(actionId) || UiActions.OPEN_FARMSTEAD_UPGRADE.equals(actionId)) {
            return handleBuildingAction(request, state, actionId, now);
        }
        InteractionMenuModel menu = buildingMenu(state, now);
        return result(request.requestId(), InteractionResultType.ERROR, false, "Unsupported building action: " + actionId, menu, "Unsupported action.", menu.metadata());
    }

    private InteractionResult handleBuildingAction(InteractionRequest request, BuildingInteractionState state, String actionId, Instant now) {
        if (UiActions.OPEN_BUILDING_STORAGE.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.DENIED, false, "Storage wiring is not modelled yet.", menu, "Storage wiring is not modelled yet.", menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_PRODUCTION.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.DENIED, false, "Production wiring is not modelled yet.", menu, "Production wiring is not modelled yet.", menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_UPGRADE.equals(actionId) || UiActions.BUILDING_START_UPGRADE.equals(actionId) || UiActions.OPEN_FARMSTEAD_UPGRADE.equals(actionId)) {
            int maxLevel = maxLevelFor(state.buildingType());
            if (state.buildingLevel() >= maxLevel) {
                InteractionMenuModel menu = buildingMenu(state, now);
                return result(request.requestId(), InteractionResultType.INVALID_STATE, false, "Building is already at max level.", menu, "Already max level.", menu.metadata());
            }
            BuildingInteractionState upgraded = state.withLevel(state.buildingLevel() + 1);
            buildingStates.put(upgraded.buildingId(), upgraded);
            InteractionMenuModel menu = buildingMenu(upgraded, now);
            return result(request.requestId(), InteractionResultType.EXECUTE_ACTION, true, state.buildingType() + " upgraded to level " + upgraded.buildingLevel() + ".", menu, null, menu.metadata());
        }
        if (UiActions.BUILDING_OPEN_DETAIL.equals(actionId) || UiActions.OPEN_BUILDING_MAIN.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.OPEN_MENU, true, menu.title() + " opened.", menu, null, menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_WORKERS.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.DENIED, false, "Workers wiring is not modelled yet.", menu, "Workers wiring is not modelled yet.", menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_HEALING.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.DENIED, false, "Healing wiring is not modelled yet.", menu, "Healing wiring is not modelled yet.", menu.metadata());
        }
        InteractionMenuModel menu = buildingMenu(state, now);
        return result(request.requestId(), InteractionResultType.ERROR, false, "Unsupported building action: " + actionId, menu, "Unsupported action.", menu.metadata());
    }

    private NpcDefinition resolveNpcDefinition(InteractionRequest request) {
        String npcType = normalize(request.context().getOrDefault("npcType", "unknown"));
        String definitionId = request.context().getOrDefault("npcDefinitionId", npcType);
        return npcDefinitions.computeIfAbsent(definitionId, key -> new NpcDefinition(
                key,
                npcType,
                humanize(request.context().getOrDefault("displayName", npcType)),
                true,
                isFarmer(npcType) ? "farmstead-menu" : "npc-main",
                Map.copyOf(request.context())
        ));
    }

    private NpcInstance resolveNpcInstance(InteractionRequest request, NpcDefinition definition) {
        return npcInstances.compute(request.targetId(), (key, existing) -> {
            if (existing != null) {
                return existing;
            }
            String attachedBuildingId = request.context().get("attachedBuildingId");
            if ((attachedBuildingId == null || attachedBuildingId.isBlank()) && isFarmer(definition.npcType())) {
                attachedBuildingId = "building:" + key;
            }
            if (attachedBuildingId != null && !attachedBuildingId.isBlank()) {
                buildingStates.computeIfAbsent(attachedBuildingId, value -> new BuildingInteractionState(
                        value,
                        request.context().getOrDefault("buildingType", isFarmer(definition.npcType()) ? "farmstead" : "building"),
                        parseInt(request.context().get("buildingLevel"), 1),
                        true,
                        List.of(key),
                        Map.copyOf(request.context())
                ));
            }
            return new NpcInstance(
                    key,
                    definition.npcDefinitionId(),
                    attachedBuildingId == null ? "" : attachedBuildingId,
                    true,
                    Map.copyOf(request.context())
            );
        });
    }

    private BuildingInteractionState resolveBuildingState(InteractionRequest request) {
        return buildingStates.compute(request.targetId(), (key, existing) -> {
            if (existing != null) {
                return existing;
            }
            return new BuildingInteractionState(
                    key,
                    request.context().getOrDefault("buildingType", "building"),
                    parseInt(request.context().get("buildingLevel"), 1),
                    true,
                    parseAttachedNpcIds(request.context().get("attachedNpcIds")),
                    Map.copyOf(request.context())
            );
        });
    }

    private BuildingInteractionState resolveAttachedBuilding(InteractionRequest request, NpcInstance instance, NpcDefinition definition) {
        String buildingId = instance.attachedBuildingId();
        if (buildingId == null || buildingId.isBlank()) {
            buildingId = request.context().get("attachedBuildingId");
        }
        if (buildingId == null || buildingId.isBlank()) {
            if (!isFarmer(definition.npcType())) {
                return null;
            }
            buildingId = "building:" + request.targetId();
        }
        String finalBuildingId = buildingId;
        return buildingStates.compute(finalBuildingId, (key, existing) -> {
            if (existing != null) {
                return existing;
            }
            return new BuildingInteractionState(
                    key,
                    request.context().getOrDefault("buildingType", isFarmer(definition.npcType()) ? "farmstead" : "building"),
                    parseInt(request.context().get("buildingLevel"), 1),
                    true,
                    List.of(request.targetId()),
                    Map.copyOf(request.context())
            );
        });
    }

    private InteractionMenuModel npcMenu(NpcInstance instance, NpcDefinition definition, Instant now) {
        if (isFarmer(definition.npcType()) || (instance.attachedBuildingId() != null && !instance.attachedBuildingId().isBlank())) {
            BuildingInteractionState buildingState = resolveAttachedBuildingFromInstance(instance, definition);
            if (buildingState != null) {
                return buildingMenu(buildingState, now, definition.displayName(), instance.npcInstanceId(), "npc");
            }
        }
        List<InteractionMenuElement> elements = new ArrayList<InteractionMenuElement>();
        elements.add(element(10, "VILLAGER_SPAWN_EGG", definition.displayName() + " Overview", UiActions.OPEN_NPC_MAIN, true, null, List.of("NPC type: " + definition.npcType()), Map.of("npcDefinitionId", definition.npcDefinitionId())));
        if (instance.attachedBuildingId() != null && !instance.attachedBuildingId().isBlank()) {
            elements.add(element(12, "CRAFTING_TABLE", "Open Building", UiActions.NPC_OPEN_BUILDING, true, null, List.of("Attached building: " + instance.attachedBuildingId()), Map.of("attachedBuildingId", instance.attachedBuildingId())));
        } else {
            elements.add(element(12, "CRAFTING_TABLE", "Open Building", UiActions.NPC_OPEN_BUILDING, false, "No building is attached.", List.of("No building is attached."), Map.of()));
        }
        elements.add(element(14, "WRITABLE_BOOK", "Debug Info", UiActions.NPC_DEBUG, true, null, List.of("Inspect this NPC record."), Map.of("npcInstanceId", instance.npcInstanceId())));
        elements.add(element(22, "BARRIER", "Close", UiActions.CLOSE, true, null, List.of("Close this menu."), Map.of()));
        Map<String, String> metadata = metadata(Map.of(
                "npcDefinitionId", definition.npcDefinitionId(),
                "npcType", definition.npcType(),
                "npcInstanceId", instance.npcInstanceId(),
                "attachedBuildingId", instance.attachedBuildingId() == null ? "" : instance.attachedBuildingId()
        ));
        return new InteractionMenuModel("npc-main", definition.displayName(), 27, InteractionTargetType.NPC, instance.npcInstanceId(), elements, metadata);
    }

    private InteractionMenuModel buildingMenu(BuildingInteractionState state, Instant now) {
        return buildingMenu(state, now, humanize(state.buildingType()), state.buildingId(), "building");
    }

    private InteractionMenuModel buildingMenu(BuildingInteractionState state, Instant now, String displayName, String sourceTargetId, String sourceTargetType) {
        List<InteractionMenuElement> elements = new ArrayList<InteractionMenuElement>();
        elements.add(element(10, "BEACON", "Overview", UiActions.OPEN_BUILDING_MAIN, true, null, List.of(displayName + " level " + state.buildingLevel()), Map.of("buildingId", state.buildingId())));
        elements.add(element(11, "CHEST", "Storage", UiActions.OPEN_BUILDING_STORAGE, false, "Storage wiring is not modelled yet.", List.of("Storage wiring is not modelled yet."), Map.of("buildingId", state.buildingId())));
        elements.add(element(12, "FURNACE", "Production", UiActions.OPEN_BUILDING_PRODUCTION, false, "Production wiring is not modelled yet.", List.of("Production wiring is not modelled yet."), Map.of("buildingId", state.buildingId())));
        boolean canUpgrade = state.buildingLevel() < maxLevelFor(state.buildingType());
        elements.add(element(13, "ANVIL", "Upgrade", UiActions.BUILDING_START_UPGRADE, canUpgrade, canUpgrade ? null : "Already max level.", List.of("Current level: " + state.buildingLevel(), "Max level: " + maxLevelFor(state.buildingType())), Map.of("buildingId", state.buildingId())));
        elements.add(element(14, "CRAFTING_TABLE", "Workers", UiActions.OPEN_BUILDING_WORKERS, false, "Workers wiring is not modelled yet.", List.of("Workers wiring is not modelled yet."), Map.of("buildingId", state.buildingId())));
        elements.add(element(15, "CAULDRON", "Healing", UiActions.OPEN_BUILDING_HEALING, false, "Healing wiring is not modelled yet.", List.of("Healing wiring is not modelled yet."), Map.of("buildingId", state.buildingId())));
        elements.add(element(22, "BARRIER", "Close", UiActions.CLOSE, true, null, List.of("Close this menu."), Map.of()));
        Map<String, String> metadata = metadata(Map.of(
                "buildingId", state.buildingId(),
                "buildingType", state.buildingType(),
                "buildingLevel", String.valueOf(state.buildingLevel()),
                "maxLevel", String.valueOf(maxLevelFor(state.buildingType())),
                "sourceTargetId", sourceTargetId,
                "sourceTargetType", sourceTargetType,
                "sourceTargetName", displayName
        ));
        return new InteractionMenuModel("building-detail", humanize(state.buildingType()) + " Building", 27, InteractionTargetType.BUILDING, state.buildingId(), elements, metadata);
    }

    private BuildingInteractionState resolveAttachedBuildingFromInstance(NpcInstance instance, NpcDefinition definition) {
        String buildingId = instance.attachedBuildingId();
        if (buildingId == null || buildingId.isBlank()) {
            return null;
        }
        return buildingStates.computeIfAbsent(buildingId, key -> new BuildingInteractionState(
                key,
                isFarmer(definition.npcType()) ? "farmstead" : "building",
                parseInt(instance.metadata().get("buildingLevel"), 1),
                true,
                List.of(instance.npcInstanceId()),
                Map.copyOf(instance.metadata())
        ));
    }

    private InteractionMenuElement element(int slot, String material, String title, String actionId, boolean enabled, String disabledReason, List<String> lore, Map<String, String> metadata) {
        return new InteractionMenuElement(
                material + "-" + slot,
                slot,
                material,
                title,
                lore == null ? List.of() : lore,
                enabled,
                disabledReason,
                actionId,
                metadata == null ? Map.of() : metadata
        );
    }

    private String actionId(InteractionRequest request) {
        String value = request.context().get("actionId");
        if (value == null || value.isBlank()) {
            value = request.context().get("elementActionId");
        }
        return value;
    }

    private boolean isOpenRequest(InteractionRequest request) {
        String interactionType = request.interactionType();
        if (interactionType == null) {
            return true;
        }
        String normalized = interactionType.toLowerCase(Locale.ROOT);
        return normalized.contains("open") || normalized.contains("menu") || normalized.contains("view");
    }

    private boolean isFarmer(String npcType) {
        return "farmer".equalsIgnoreCase(npcType) || "farmstead".equalsIgnoreCase(npcType);
    }

    private int maxLevelFor(String buildingType) {
        if ("farmstead".equalsIgnoreCase(buildingType)) {
            return 3;
        }
        if ("workshop".equalsIgnoreCase(buildingType)) {
            return 4;
        }
        return DEFAULT_BUILDING_MAX_LEVEL;
    }

    private List<String> parseAttachedNpcIds(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return List.of();
        }
        String[] values = rawValue.split(",");
        ArrayList<String> result = new ArrayList<String>();
        for (String value : values) {
            if (!value.isBlank()) {
                result.add(value.trim());
            }
        }
        return List.copyOf(result);
    }

    /*
    private NpcDefinition resolveNpcDefinition(InteractionRequest request) {
        String npcType = normalize(request.context().getOrDefault("npcType", "unknown"));
        String definitionId = request.context().getOrDefault("npcDefinitionId", npcType);
        return npcDefinitions.computeIfAbsent(definitionId, key -> new NpcDefinition(
                key,
                npcType,
                humanize(request.context().getOrDefault("displayName", npcType)),
                true,
                isFarmer(npcType) ? "farmstead-menu" : "npc-main",
                Map.copyOf(request.context())
        ));
    }

    private NpcInstance resolveNpcInstance(InteractionRequest request, NpcDefinition definition) {
        return npcInstances.compute(request.targetId(), (key, existing) -> {
            if (existing != null) {
                return existing;
            }
            String attachedBuildingId = request.context().get("attachedBuildingId");
            if ((attachedBuildingId == null || attachedBuildingId.isBlank()) && isFarmer(definition.npcType())) {
                attachedBuildingId = "building:" + key;
            }
            if (attachedBuildingId != null && !attachedBuildingId.isBlank()) {
                buildingStates.computeIfAbsent(attachedBuildingId, value -> new BuildingInteractionState(
                        value,
                        request.context().getOrDefault("buildingType", isFarmer(definition.npcType()) ? "farmstead" : "building"),
                        parseInt(request.context().get("buildingLevel"), 1),
                        true,
                        List.of(key),
                        Map.copyOf(request.context())
                ));
            }
            return new NpcInstance(
                    key,
                    definition.npcDefinitionId(),
                    attachedBuildingId == null ? "" : attachedBuildingId,
                    true,
                    Map.copyOf(request.context())
            );
        });
    }

    private BuildingInteractionState resolveBuildingState(InteractionRequest request) {
        return buildingStates.compute(request.targetId(), (key, existing) -> {
            if (existing != null) {
                return existing;
            }
            return new BuildingInteractionState(
                    key,
                    request.context().getOrDefault("buildingType", "building"),
                    parseInt(request.context().get("buildingLevel"), 1),
                    true,
                    parseAttachedNpcIds(request.context().get("attachedNpcIds")),
                    Map.copyOf(request.context())
            );
        });
    }

    private InteractionResult handleBuildingAction(InteractionRequest request, BuildingInteractionState state, String actionId, Instant now) {
        if (UiActions.OPEN_BUILDING_STORAGE.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.DENIED, false, "Storage wiring is not modelled yet.", menu, "Storage wiring is not modelled yet.", menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_PRODUCTION.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.DENIED, false, "Production wiring is not modelled yet.", menu, "Production wiring is not modelled yet.", menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_WORKERS.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.DENIED, false, "Workers wiring is not modelled yet.", menu, "Workers wiring is not modelled yet.", menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_HEALING.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.DENIED, false, "Healing wiring is not modelled yet.", menu, "Healing wiring is not modelled yet.", menu.metadata());
        }
        if (UiActions.OPEN_BUILDING_UPGRADE.equals(actionId) || UiActions.BUILDING_START_UPGRADE.equals(actionId) || UiActions.OPEN_FARMSTEAD_UPGRADE.equals(actionId)) {
            int maxLevel = maxLevelFor(state.buildingType());
            if (state.buildingLevel() >= maxLevel) {
                InteractionMenuModel menu = buildingMenu(state, now);
                return result(request.requestId(), InteractionResultType.INVALID_STATE, false, "Building is already at max level.", menu, "Already max level.", menu.metadata());
            }
            BuildingInteractionState upgraded = state.withLevel(state.buildingLevel() + 1);
            buildingStates.put(upgraded.buildingId(), upgraded);
            InteractionMenuModel menu = buildingMenu(upgraded, now);
            return result(request.requestId(), InteractionResultType.EXECUTE_ACTION, true, state.buildingType() + " upgraded to level " + upgraded.buildingLevel() + ".", menu, null, menu.metadata());
        }
        if (UiActions.BUILDING_OPEN_DETAIL.equals(actionId) || UiActions.OPEN_BUILDING_MAIN.equals(actionId)) {
            InteractionMenuModel menu = buildingMenu(state, now);
            return result(request.requestId(), InteractionResultType.OPEN_MENU, true, menu.title() + " opened.", menu, null, menu.metadata());
        }
        InteractionMenuModel menu = buildingMenu(state, now);
        return result(request.requestId(), InteractionResultType.ERROR, false, "Unsupported building action: " + actionId, menu, "Unsupported action.", menu.metadata());
    }
    */

    private InteractionResult result(
            String requestId,
            InteractionResultType resultType,
            boolean success,
            String message,
            InteractionMenuModel menu,
            String disabledReason,
            Map<String, String> metadata
    ) {
        return new InteractionResult(
                requestId == null || requestId.isBlank() ? "interaction-" + System.currentTimeMillis() : requestId,
                resultType,
                success,
                message,
                menu,
                disabledReason,
                metadata == null ? Map.of() : Map.copyOf(metadata)
        );
    }

    private Map<String, String> metadata(InteractionRequest request, Map<String, String> extra) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("targetType", request.targetType().name());
        metadata.put("targetId", request.targetId());
        metadata.put("interactionType", request.interactionType());
        metadata.put("serverId", request.serverId());
        metadata.put("worldName", request.worldName() == null ? "" : request.worldName());
        metadata.putAll(request.context());
        metadata.putAll(extra);
        return Map.copyOf(metadata);
    }

    private Map<String, String> metadata(Map<String, String> values) {
        return values == null ? Map.of() : Map.copyOf(values);
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private String normalize(String token) {
        if (token == null) {
            return "unknown";
        }
        String normalized = token.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+", "");
        normalized = normalized.replaceAll("_+$", "");
        return normalized.isBlank() ? "unknown" : normalized;
    }

    private String humanize(String token) {
        if (token == null || token.isBlank()) {
            return "Unknown";
        }
        String normalized = token.replace('_', ' ').trim();
        if (normalized.isEmpty()) {
            return token;
        }
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private record NpcDefinition(
            String npcDefinitionId,
            String npcType,
            String displayName,
            boolean active,
            String defaultMenuId,
            Map<String, String> metadata
    ) {
        private NpcDefinition {
            Objects.requireNonNull(npcDefinitionId, "npcDefinitionId");
            Objects.requireNonNull(npcType, "npcType");
            Objects.requireNonNull(displayName, "displayName");
            Objects.requireNonNull(defaultMenuId, "defaultMenuId");
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    private record NpcInstance(
            String npcInstanceId,
            String npcDefinitionId,
            String attachedBuildingId,
            boolean active,
            Map<String, String> metadata
    ) {
        private NpcInstance {
            Objects.requireNonNull(npcInstanceId, "npcInstanceId");
            Objects.requireNonNull(npcDefinitionId, "npcDefinitionId");
            attachedBuildingId = attachedBuildingId == null ? "" : attachedBuildingId;
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    private record BuildingInteractionState(
            String buildingId,
            String buildingType,
            int buildingLevel,
            boolean interactionEnabled,
            List<String> attachedNpcIds,
            Map<String, String> metadata
    ) {
        private BuildingInteractionState {
            Objects.requireNonNull(buildingId, "buildingId");
            Objects.requireNonNull(buildingType, "buildingType");
            attachedNpcIds = attachedNpcIds == null ? List.of() : List.copyOf(attachedNpcIds);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }

        private BuildingInteractionState withLevel(int nextLevel) {
            return new BuildingInteractionState(buildingId, buildingType, nextLevel, interactionEnabled, attachedNpcIds, metadata);
        }
    }
}
