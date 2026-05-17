package com.tavall.resourcegame.services;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.dependency.interfaces.ICastleBuildingService;
import com.tavall.resourcegame.dependency.interfaces.ICastleBuildingVisualService;
import com.tavall.resourcegame.domain.CastleBuildingData;
import com.tavall.resourcegame.domain.CastleBuildingSummary;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.interactions.OpenFarmsteadInteraction;
import com.tavall.resourcegame.population.PromotionCost;
import com.tavall.resourcegame.world.CastleBuildingStructureService;
import com.tavall.resourcegame.world.CastleBuildingVisualRefs;
import com.tavall.resourcegame.world.ProtectedStructureType;
import com.tavall.resourcegame.tasks.WorldTasks;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renders placed kingdom buildings as labels, protected structures, and native Hytale model entities when available.
 */
public final class CastleBuildingVisualService implements ICastleBuildingVisualService, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(CastleBuildingVisualService.class.getName());
    private final ICastleBuildingService buildingService;
    private final CastleBuildingStructureService structureService;
    private final WorldLabelService worldLabelService;
    private final StructureProtectionService protectionService;
    private final Map<UUID, Map<UUID, CastleBuildingVisualRefs>> buildingRefs = new ConcurrentHashMap<>();

    public CastleBuildingVisualService(
            ICastleBuildingService buildingService,
            CastleBuildingStructureService structureService,
            WorldLabelService worldLabelService,
            StructureProtectionService protectionService
    ) {
        this.buildingService = buildingService;
        this.structureService = structureService;
        this.worldLabelService = worldLabelService;
        this.protectionService = protectionService;
    }

    @Override
    public void ensureBuildings(UUID playerId, PlayerGameState state) {
        rebuildBuildings(playerId, state);
    }

    @Override
    public void refreshBuildings(UUID playerId, PlayerGameState state) {
        rebuildBuildings(playerId, state);
    }

    @Override
    public void clearBuildings(UUID playerId) {
        Map<UUID, CastleBuildingVisualRefs> refsByBuilding = buildingRefs.remove(playerId);
        if (refsByBuilding == null) {
            return;
        }
        for (Map.Entry<UUID, CastleBuildingVisualRefs> entry : refsByBuilding.entrySet()) {
            protectionService.clearStructure(structureKey(entry.getKey()));
            CastleBuildingVisualRefs refs = entry.getValue();
            World world = Universe.get().getWorld(refs.worldName());
            if (world != null) {
                WorldTasks.executeSafe(world, "CastleBuildingVisualService.clearRefsOnWorld", () -> clearRefsOnWorld(world, refs));
                continue;
            }
            for (Ref<EntityStore> ref : refs.allRefs()) {
                removeRef(ref);
            }
        }
    }

    @Override
    public Optional<UUID> findBuildingId(UUID playerId, Ref<EntityStore> targetRef) {
        if (playerId == null || targetRef == null || !targetRef.isValid()) {
            return Optional.empty();
        }
        Map<UUID, CastleBuildingVisualRefs> refsByBuilding = buildingRefs.get(playerId);
        if (refsByBuilding == null) {
            return Optional.empty();
        }
        return refsByBuilding.entrySet().stream()
                .filter(entry -> entry.getValue().matches(targetRef))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private void rebuildBuildings(UUID playerId, PlayerGameState state) {
        Map<UUID, CastleBuildingVisualRefs> previousRefs = buildingRefs.remove(playerId);
        clearExistingBuildings(previousRefs);
        if (playerId == null || state == null) {
            return;
        }
        List<CastleBuildingData> buildings = buildingService.listBuildings(state);
        if (buildings.isEmpty()) {
            return;
        }
        Map<UUID, CastleBuildingVisualRefs> rebuilt = new ConcurrentHashMap<>();
        for (CastleBuildingData building : buildings) {
            CastleBuildingSummary summary = buildingService.summary(playerId, state, building, Instant.now());
            World world = Universe.get().getWorld(summary.worldName());
            if (world == null) {
                continue;
            }
            WorldTasks.executeSafe(world, "CastleBuildingVisualService.rebuildBuilding(" + building.buildingId() + ")", () -> {
                List<Ref<EntityStore>> labelRefs = spawnLabels(world, summary);
                List<Ref<EntityStore>> modelRefs = spawnModel(world, summary);
                protectionService.replaceStructure(
                        structureKey(building.buildingId()),
                        playerId,
                        ProtectedStructureType.BUILDING,
                        summary.worldName(),
                        building.buildingType().shortKey(),
                        structureService.ensureBuildingSite(world, summary)
                );
                rebuilt.put(
                        building.buildingId(),
                        new CastleBuildingVisualRefs(
                                summary.worldName(),
                                new Vector3d(summary.worldX(), summary.worldY(), summary.worldZ()),
                                labelRefs,
                                modelRefs
                        )
                );
            });
        }
        buildingRefs.put(playerId, rebuilt);
    }

    private void clearExistingBuildings(Map<UUID, CastleBuildingVisualRefs> refsByBuilding) {
        if (refsByBuilding == null || refsByBuilding.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, CastleBuildingVisualRefs> entry : refsByBuilding.entrySet()) {
            protectionService.clearStructure(structureKey(entry.getKey()));
            CastleBuildingVisualRefs refs = entry.getValue();
            World world = Universe.get().getWorld(refs.worldName());
            if (world != null) {
                WorldTasks.executeSafe(world, "CastleBuildingVisualService.clearRefsOnWorld", () -> clearRefsOnWorld(world, refs));
                continue;
            }
            for (Ref<EntityStore> ref : refs.allRefs()) {
                removeRef(ref);
            }
        }
    }

    private void clearRefsOnWorld(World world, CastleBuildingVisualRefs refs) {
        structureService.clearBuildingSite(world, refs.worldPosition());
        for (Ref<EntityStore> ref : refs.allRefs()) {
            removeRef(ref);
        }
    }

    private void removeRef(Ref<EntityStore> ref) {
        if (ref == null || !ref.isValid()) {
            return;
        }
        try {
            ref.getStore().removeEntity(ref, RemoveReason.REMOVE);
        } catch (Throwable ignored) {
        }
    }

    private List<Ref<EntityStore>> spawnLabels(World world, CastleBuildingSummary summary) {
        Vector3d labelPosition = new Vector3d(summary.worldX(), summary.worldY() + 2.6D, summary.worldZ());
        return worldLabelService.spawnLabelStack(world, labelPosition, buildingLabelLines(summary));
    }

    private List<Ref<EntityStore>> spawnModel(World world, CastleBuildingSummary summary) {
        Model model = resolveBuildingModel(summary);
        if (model == null) {
            return List.of();
        }
        try {
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            Vector3d position = new Vector3d(summary.worldX(), summary.worldY(), summary.worldZ());
            holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, Vector3f.ZERO));
            holder.ensureComponent(UUIDComponent.getComponentType());
            holder.ensureComponent(Intangible.getComponentType());
            holder.ensureComponent(Invulnerable.getComponentType());
            holder.ensureComponent(Frozen.getComponentType());
            holder.addComponent(DisplayNameComponent.getComponentType(), new DisplayNameComponent(Message.raw(summary.buildingData().buildingType().displayName())));
            holder.addComponent(Nameplate.getComponentType(), new Nameplate(summary.buildingData().buildingType().displayName()));
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
            Interactions interactions = new Interactions(Map.of(InteractionType.Secondary, OpenFarmsteadInteraction.ROOT_INTERACTION_ID));
            interactions.setInteractionHint("Right-click");
            holder.addComponent(Interactions.getComponentType(), interactions);
            Ref<EntityStore> ref = world.getEntityStore().getStore().addEntity(holder, AddReason.SPAWN);
            LOGGER.info(() -> "Spawned building model for " + summary.buildingData().buildingType().shortKey()
                    + " level " + summary.displayLevel() + " in " + summary.worldName() + ".");
            return ref == null ? List.of() : List.of(ref);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Failed to spawn building model for " + summary.buildingData().buildingType().shortKey() + ".", ex);
            return List.of();
        }
    }

    private Model resolveBuildingModel(CastleBuildingSummary summary) {
        for (String candidateId : buildingModelCandidates(summary)) {
            ModelAsset asset = (ModelAsset) ModelAsset.getAssetMap().getAsset(candidateId);
            if (asset == null) {
                continue;
            }
            LOGGER.info(() -> "Resolved building model asset '" + candidateId + "'.");
            return Model.createUnitScaleModel(asset);
        }
        LOGGER.warning(() -> "No building model asset found for " + summary.buildingData().buildingType().shortKey()
                + " from " + buildingModelCandidates(summary) + "; the building will retain labels and protected site bounds only.");
        return null;
    }

    private List<String> buildingModelCandidates(CastleBuildingSummary summary) {
        String buildingKey = summary.buildingData().buildingType().shortKey();
        if (summary.buildingData().buildingType() == com.tavall.resourcegame.domain.BuildingType.FARMSTEAD) {
            if (summary.isUnderConstruction()) {
                String stageKey = summary.constructionStage().name().toLowerCase(java.util.Locale.ROOT);
                return List.of(
                        "ResourceGame/Farmstead/Construction/" + titleAssetKey(stageKey),
                        "ResourceGame/Farmstead/Construction/" + stageKey,
                        "ResourceGame/Farmstead",
                        "Farmstead"
                );
            }
            String nativeLevelKey = farmsteadNativeLevelKey(summary.displayLevel());
            return List.of(
                    "ResourceGame/Farmstead/" + nativeLevelKey,
                    "Farmstead",
                    "ResourceGame/Farmstead",
                    "ResourceGame_Farmstead",
                    "resource_game:farmstead"
            );
        }
        if (summary.isUnderConstruction()) {
            String stageKey = summary.constructionStage().name().toLowerCase(java.util.Locale.ROOT);
            return List.of(
                    "resource_game:buildings/" + buildingKey + "/construction/" + stageKey,
                    "resource_game:" + buildingKey
            );
        }
        int level = Math.max(1, Math.min(summary.buildingData().buildingType().maxLevel(), summary.displayLevel()));
        String levelKey = String.format(java.util.Locale.ROOT, "level_%02d", level);
        return List.of(
                "resource_game:buildings/" + buildingKey + "/" + levelKey,
                "resource_game:" + buildingKey
        );
    }

    private String farmsteadNativeLevelKey(int displayLevel) {
        int bucket = Math.max(1, Math.min(5, ((Math.max(1, displayLevel) - 1) / 6) + 1));
        return String.format(java.util.Locale.ROOT, "Level_%02d", bucket);
    }

    private String titleAssetKey(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String[] tokens = key.split("_");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append("_");
            }
            builder.append(token.substring(0, 1).toUpperCase(java.util.Locale.ROOT));
            if (token.length() > 1) {
                builder.append(token.substring(1).toLowerCase(java.util.Locale.ROOT));
            }
        }
        return builder.toString();
    }

    private List<String> buildingLabelLines(CastleBuildingSummary summary) {
        StringBuilder bonusBuilder = new StringBuilder();
        if (summary.foodPerTickBonus() > 0) {
            appendToken(bonusBuilder, "+" + summary.foodPerTickBonus() + "F/t");
        }
        if (summary.woodPerTickBonus() > 0) {
            appendToken(bonusBuilder, "+" + summary.woodPerTickBonus() + "W/t");
        }
        if (summary.ironPerTickBonus() > 0) {
            appendToken(bonusBuilder, "+" + summary.ironPerTickBonus() + "I/t");
        }
        if (summary.constructionSpeedBonus() > 0.0D) {
            appendToken(bonusBuilder, "Build +" + (int) Math.round(summary.constructionSpeedBonus() * 100.0D) + "%");
        }
        PromotionCost promotionDiscount = summary.promotionDiscount();
        int totalDiscount = promotionDiscount.foodCost() + promotionDiscount.woodCost() + promotionDiscount.ironCost();
        if (totalDiscount > 0) {
            appendToken(
                    bonusBuilder,
                    "Promo -" + promotionDiscount.foodCost() + "F/"
                            + promotionDiscount.woodCost() + "W/"
                            + promotionDiscount.ironCost() + "I"
            );
        }
        String detailLine = bonusBuilder.length() == 0 ? "No passive bonuses yet" : bonusBuilder.toString();
        String actionLine = summary.isUnderConstruction()
                ? summary.remainingSeconds() + "s left"
                : "Right-click";
        return List.of(
                summary.buildingData().buildingType().displayName()
                        + " | Lv " + summary.displayLevel()
                        + " | " + summary.statusText(),
                detailLine,
                actionLine
        );
    }

    private void appendToken(StringBuilder builder, String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(" | ");
        }
        builder.append(token);
    }

    private String structureKey(UUID buildingId) {
        return "building:" + buildingId;
    }
}
