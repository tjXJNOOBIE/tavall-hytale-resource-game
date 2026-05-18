package org.tavall.control.runtime;
import org.tavall.control.npc.NpcRoleResolver;
import org.tavall.control.npc.NpcVisualSpawner;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.runtime.ICustomEntitySpawnHandler;
import org.tavall.control.farmstead.ui.IFarmsteadMenuHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.api.UIData;
import org.tavall.control.domain.CastleBuildingData;
import org.tavall.control.domain.CitizenJobType;
import org.tavall.control.domain.CustomEntitySpawnRole;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.farmstead.npc.FarmsteadStewardSpawner;
import org.tavall.control.tasks.WorldTasks;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.tavall.control.world.VectorMath;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spawns debug NPCs that route right-clicks into the building and citizen UI surfaces.
 */
public final class CustomEntitySpawnHandler implements ICustomEntitySpawnHandler, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(CustomEntitySpawnHandler.class.getName());
    private static final double SPAWN_DISTANCE = 2.0D;
    private static final float SPAWN_SCALE = 1.0F;

    private final NpcVisualSpawner npcVisualSpawner;
    private final FarmsteadStewardSpawner farmsteadStewardSpawner;
    private final NpcRoleResolver npcRoleResolver;
    private final IPlayerSessionStore sessionStore;
    private final ICastleBuildingHandler buildingHandler;
    private final UIData uiNavigator;
    private final IFarmsteadMenuHandler farmsteadMenuHandler;
    private final Map<UUID, Map<Ref<EntityStore>, CustomEntitySpawnRole>> refsByPlayer = new ConcurrentHashMap<>();

    public CustomEntitySpawnHandler(
            NpcVisualSpawner npcVisualSpawner,
            FarmsteadStewardSpawner farmsteadStewardSpawner,
            NpcRoleResolver npcRoleResolver,
            IPlayerSessionStore sessionStore,
            ICastleBuildingHandler buildingHandler,
            UIData uiNavigator,
            IFarmsteadMenuHandler farmsteadMenuHandler
    ) {
        this.npcVisualSpawner = Objects.requireNonNull(npcVisualSpawner, "npcVisualSpawner");
        this.farmsteadStewardSpawner = Objects.requireNonNull(farmsteadStewardSpawner, "farmsteadStewardSpawner");
        this.npcRoleResolver = Objects.requireNonNull(npcRoleResolver, "npcRoleResolver");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.buildingHandler = Objects.requireNonNull(buildingHandler, "buildingHandler");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
        this.farmsteadMenuHandler = Objects.requireNonNull(farmsteadMenuHandler, "farmsteadMenuHandler");
    }

    @Override
    public void spawn(Player player, String roleToken) {
        if (player == null) {
            return;
        }
        CustomEntitySpawnRole role = CustomEntitySpawnRole.parse(roleToken);
        if (role == null) {
            player.sendMessage(Message.raw("Usage: /kd entity spawn " + CustomEntitySpawnRole.commandChoices()).color("yellow"));
            return;
        }
        World world = player.getWorld();
        TransformComponent transform = player.getTransformComponent();
        if (world == null || transform == null || transform.getPosition() == null) {
            player.sendMessage(Message.raw("Entity spawn failed: player world or position is unavailable.").color("red"));
            return;
        }
        int roleIndex = role == CustomEntitySpawnRole.FARMSTEAD_STEWARD
                ? 0
                : npcRoleResolver.resolveRoleIndex(role.preferredNpcRoleName());
        if (role != CustomEntitySpawnRole.FARMSTEAD_STEWARD && roleIndex < 0) {
            player.sendMessage(Message.raw("Entity spawn failed: no usable NPC role was found for " + role.displayName() + ".").color("red"));
            LOGGER.warning(() -> "Custom entity spawn failed for " + role.name() + " because no NPC role index was resolved.");
            return;
        }
        Vector3d spawnPosition = spawnPosition(transform);
        UUID playerId = player.getUuid();
        WorldTasks.executeSafe(world, "CustomEntitySpawnHandler.spawn(" + role.name() + ")", () -> {
            try {
                Ref<EntityStore> ref = spawnRoleEntity(world, role, roleIndex, spawnPosition);
                if (ref == null || !ref.isValid()) {
                    player.sendMessage(Message.raw("Entity spawn failed: NPC plugin returned no entity.").color("red"));
                    LOGGER.warning(() -> "Custom entity spawn failed for " + role.name() + " because no valid entity reference was returned.");
                    return;
                }
                refsByPlayer.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>()).put(ref, role);
                String spawnSummary = formatPosition(spawnPosition);
                String entityUuid = resolveEntityUuid(ref);
                player.sendMessage(Message.raw("Spawned " + role.displayName() + " at " + spawnSummary
                        + " uuid " + entityUuid + ". Right-click it to open the linked UI.").color("green"));
                LOGGER.info(() -> "Spawned custom entity " + role.name() + " for " + playerId + " in " + world.getName()
                        + " at " + spawnSummary + " uuid=" + entityUuid + ".");
            } catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE, "Failed to spawn custom entity " + role.name() + " for " + playerId + ".", ex);
                player.sendMessage(Message.raw("Entity spawn failed: " + safeMessage(ex)).color("red"));
            }
        });
    }

    @Override
    public void clear(Player player) {
        if (player == null) {
            return;
        }
        Map<Ref<EntityStore>, CustomEntitySpawnRole> refs = refsByPlayer.remove(player.getUuid());
        if (refs == null || refs.isEmpty()) {
            player.sendMessage(Message.raw("No custom entities to clear.").color("yellow"));
            return;
        }
        refs.keySet().forEach(this::removeSafely);
        player.sendMessage(Message.raw("Custom entities cleared.").color("green"));
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        if (event == null || event.isCancelled() || event.getPlayer() == null || event.getTargetRef() == null) {
            return;
        }
        openFromTarget(event.getPlayer(), event.getTargetRef());
    }

    @Override
    public boolean openFromTarget(Player player, Ref<EntityStore> targetRef) {
        if (player == null || targetRef == null) {
            return false;
        }
        PlayerSession session = sessionStore.get(player.getUuid());
        if (session == null) {
            return false;
        }
        CustomEntitySpawnRole role = resolveRole(player.getUuid(), targetRef);
        if (role == null) {
            LOGGER.fine(() -> "Custom entity interaction ignored for " + player.getUuid() + " because target " + targetRef + " is not tracked.");
            return false;
        }
        if (role == CustomEntitySpawnRole.FARMSTEAD_STEWARD) {
            boolean opened = farmsteadMenuHandler.openFarmsteadMenu(player);
            if (opened) {
                LOGGER.info(() -> "Farmstead Steward interaction opened Farmstead menu for " + player.getDisplayName()
                        + " target=" + targetRef + ".");
                return true;
            }
            LOGGER.warning(() -> "Farmstead Steward interaction did not open a menu for " + player.getDisplayName()
                    + " target=" + targetRef + ".");
            return false;
        }
        if (role.buildingRole()) {
            openBuildingRole(player, session, role);
            return true;
        }
        if (role.citizenRole()) {
            openCitizenRole(player, session, role.citizenJobType());
            return true;
        }
        return false;
    }

    private Ref<EntityStore> spawnRoleEntity(World world, CustomEntitySpawnRole role, int roleIndex, Vector3d spawnPosition) {
        if (role == CustomEntitySpawnRole.FARMSTEAD_STEWARD) {
            return farmsteadStewardSpawner.spawnFarmsteadStewardAt(world.getEntityStore().getStore(), spawnPosition);
        }
        return npcVisualSpawner.spawnNamed(
                world.getEntityStore().getStore(),
                roleIndex,
                spawnPosition,
                role.displayName(),
                SPAWN_SCALE
        );
    }

    private CustomEntitySpawnRole resolveRole(UUID playerId, Ref<EntityStore> targetRef) {
        Map<Ref<EntityStore>, CustomEntitySpawnRole> refs = refsByPlayer.get(playerId);
        if (refs == null || targetRef == null || !targetRef.isValid()) {
            return null;
        }
        return refs.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getKey().equals(targetRef))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private void openBuildingRole(Player player, PlayerSession session, CustomEntitySpawnRole role) {
        Optional<CastleBuildingData> building = buildingHandler.resolveBuilding(session.gameState(), role.buildingType().shortKey());
        UiNavigationContext context = new UiNavigationContext(player.getUuid(), player.getDisplayName())
                .withFeedback(role.displayName() + " selected.");
        if (building.isEmpty()) {
            uiNavigator.open(UiScreenKey.CASTLE_BUILDINGS, player, context.withFeedback(role.displayName() + " selected. Place "
                    + role.buildingType().displayName() + " first."), session.gameState());
            return;
        }
        uiNavigator.open(
                UiScreenKey.BUILDING_DETAIL,
                player,
                context.withSelectedBuildingId(building.get().buildingId()),
                session.gameState()
        );
    }

    private void openCitizenRole(Player player, PlayerSession session, CitizenJobType jobType) {
        UiScreenKey pageType = jobType == CitizenJobType.SOLDIER || jobType == CitizenJobType.TRAINEE
                ? UiScreenKey.CASTLE_TROOPS
                : UiScreenKey.CASTLE_CITIZENS;
        uiNavigator.open(
                pageType,
                player,
                new UiNavigationContext(player.getUuid(), player.getDisplayName())
                        .withFeedback(jobType.name() + " citizen selected."),
                session.gameState()
        );
    }

    private Vector3d spawnPosition(TransformComponent transform) {
        Vector3d base = transform.getPosition();
        Vector3d lookVector = VectorMath.normalize(VectorMath.lookVector(transform.getRotation()));
        return new Vector3d(
                base.getX() + (lookVector.getX() * SPAWN_DISTANCE),
                base.getY(),
                base.getZ() + (lookVector.getZ() * SPAWN_DISTANCE)
        );
    }

    private void removeSafely(Ref<EntityStore> ref) {
        if (ref == null || !ref.isValid()) {
            return;
        }
        Runnable remove = () -> {
            if (ref.isValid()) {
                ref.getStore().removeEntity(ref, RemoveReason.REMOVE);
            }
        };
        if (ref.getStore().getExternalData() instanceof EntityStore entityStore && entityStore.getWorld() != null) {
            WorldTasks.executeSafe(entityStore.getWorld(), "CustomEntitySpawnHandler.removeSafely", remove);
            return;
        }
        remove.run();
    }

    private String formatPosition(Vector3d position) {
        return String.format(
                java.util.Locale.ROOT,
                "%.2f %.2f %.2f",
                position.getX(),
                position.getY(),
                position.getZ()
        );
    }

    private String resolveEntityUuid(Ref<EntityStore> ref) {
        if (ref == null || !ref.isValid()) {
            return "unavailable";
        }
        UUIDComponent uuidComponent = ref.getStore().getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null || uuidComponent.getUuid() == null) {
            LOGGER.warning(() -> "Spawned custom entity " + ref + " has no UUID component.");
            return "unavailable";
        }
        return uuidComponent.getUuid().toString();
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return "unknown error";
        }
        return throwable.getMessage();
    }
}
