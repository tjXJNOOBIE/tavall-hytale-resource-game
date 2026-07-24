package org.tavall.control.castle;
import org.tavall.control.protection.StructureProtectionHandler;
import org.tavall.control.world.WorldLabelHandler;

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
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.domain.CastleBuildingData;
import org.tavall.control.domain.CastleBuildingSummary;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.interactions.OpenFarmsteadInteraction;
import org.tavall.control.population.PromotionCost;
import org.tavall.control.world.CastleBuildingStructureHandler;
import org.tavall.control.world.CastleBuildingVisualRefs;
import org.tavall.control.world.ProtectedStructureType;
import org.tavall.control.tasks.WorldTasks;

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
public final class CastleBuildingVisualHandler implements ICastleBuildingVisualHandler, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(CastleBuildingVisualHandler.class.getName());
    private final ICastleBuildingHandler buildingHandler;
    private final CastleBuildingStructureHandler structureHandler;
    private final WorldLabelHandler worldLabelHandler;
    private final StructureProtectionHandler protectionHandler;
    private final Map<UUID, Map<UUID, CastleBuildingVisualRefs>> buildingRefs = new ConcurrentHashMap<>();

    public CastleBuildingVisualHandler(
            ICastleBuildingHandler buildingHandler,
            CastleBuildingStructureHandler structureHandler,
            WorldLabelHandler worldLabelHandler,
            StructureProtectionHandler protectionHandler
    ) {
        this.buildingHandler = buildingHandler;
        this.structureHandler = structureHandler;
        this.worldLabelHandler = worldLabelHandler;
        this.protectionHandler = protectionHandler;
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
            protectionHandler.clearStructure(structureKey(entry.getKey()));
            CastleBuildingVisualRefs refs = entry.getValue();
            World world = Universe.get().getWorld(refs.worldName());
            if (world != null) {
                WorldTasks.executeSafe(world, "CastleBuildingVisualHandler.clearRefsOnWorld", () -> clearRefsOnWorld(world, refs));
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
        List<CastleBuildingData> buildings = buildingHandler.listBuildings(state);
        if (buildings.isEmpty()) {
            return;
        }
        Map<UUID, CastleBuildingVisualRefs> rebuilt = new ConcurrentHashMap<>();
        for (CastleBuildingData building : buildings) {
            CastleBuildingSummary summary = buildingHandler.summary(playerId, state, building, Instant.now());
            World world = Universe.get().getWorld(summary.worldName());
            if (world == null) {
                continue;
            }
            WorldTasks.executeSafe(world, "CastleBuildingVisualHandler.rebuildBuilding(" + building.buildingId() + ")", () -> {
                List<Ref<EntityStore>> labelRefs = spawnLabels(world, summary);
                List<Ref<EntityStore>> modelRefs = spawnModel(world, summary);
                protectionHandler.replaceStructure(
                        structureKey(building.buildingId()),
                        playerId,
                        ProtectedStructureType.BUILDING,
                        summary.worldName(),
                        building.buildingType().shortKey(),
                        structureHandler.ensureBuildingSite(world, summary)
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
            protectionHandler.clearStructure(structureKey(entry.getKey()));
            CastleBuildingVisualRefs refs = entry.getValue();
            World world = Universe.get().getWorld(refs.worldName());
            if (world != null) {
                WorldTasks.executeSafe(world, "CastleBuildingVisualHandler.clearRefsOnWorld", () -> clearRefsOnWorld(world, refs));
                continue;
            }
            for (Ref<EntityStore> ref : refs.allRefs()) {
                removeRef(ref);
            }
        }
    }

    private void clearRefsOnWorld(World world, CastleBuildingVisualRefs refs) {
        structureHandler.clearBuildingSite(world, refs.worldPosition());
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
        return worldLabelHandler.spawnLabelStack(world, labelPosition, buildingLabelLines(summary));
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
        if (summary.buildingData().buildingType() == org.tavall.control.domain.BuildingType.FARMSTEAD) {
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

