package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.shared.frontend.InteractionTargetType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitInteractionTargetResolverTest {
    private final MinecraftBukkitInteractionTargetResolver resolver = new MinecraftBukkitInteractionTargetResolver();

    @Test
    void taggedVillagerNpcResolvesWithNpcMetadata() {
        UUID entityId = UUID.fromString("00000000-0000-0000-0000-000000000123");
        Entity entity = proxy(Entity.class, Map.of(
                "getUniqueId", entityId,
                "getType", EntityType.VILLAGER,
                "getCustomName", "Farmer",
                "getWorld", world("kingdom"),
                "getScoreboardTags", Set.of("tavall.kingdom.npc", "tavall.kingdom.helper")
        ));

        Optional<MinecraftBukkitInteractionTarget> resolved = resolver.resolve(entity);

        assertTrue(resolved.isPresent());
        assertEquals(InteractionTargetType.NPC, resolved.orElseThrow().targetType());
        assertEquals(entityId.toString(), resolved.orElseThrow().targetId());
        assertEquals("Farmer", resolved.orElseThrow().displayName());
        assertEquals("VILLAGER", resolved.orElseThrow().metadata().get("entityType"));
        assertEquals("kingdom", resolved.orElseThrow().metadata().get("worldName"));
        assertEquals("farmer", resolved.orElseThrow().metadata().get("npcType"));
    }

    @Test
    void taggedBuildingMarkerResolvesWithBuildingMetadata() {
        UUID entityId = UUID.fromString("00000000-0000-0000-0000-000000000456");
        Entity entity = proxy(Entity.class, Map.of(
                "getUniqueId", entityId,
                "getType", EntityType.ARMOR_STAND,
                "getCustomName", "Farmstead",
                "getWorld", world("kingdom"),
                "getScoreboardTags", Set.of("tavall.kingdom.building", "tavall.kingdom.helper")
        ));

        Optional<MinecraftBukkitInteractionTarget> resolved = resolver.resolve(entity);

        assertTrue(resolved.isPresent());
        assertEquals(InteractionTargetType.BUILDING, resolved.orElseThrow().targetType());
        assertEquals(entityId.toString(), resolved.orElseThrow().targetId());
        assertEquals("Farmstead", resolved.orElseThrow().displayName());
        assertEquals("farmstead", resolved.orElseThrow().metadata().get("buildingType"));
    }

    @Test
    void taggedCastleMarkerResolvesWithCastleMetadata() {
        UUID entityId = UUID.fromString("00000000-0000-0000-0000-000000000457");
        Entity entity = proxy(Entity.class, Map.of(
                "getUniqueId", entityId,
                "getType", EntityType.ARMOR_STAND,
                "getCustomName", "Castle Keep",
                "getWorld", world("kingdom"),
                "getScoreboardTags", Set.of("tavall.kingdom.castle", "tavall.kingdom.helper")
        ));

        Optional<MinecraftBukkitInteractionTarget> resolved = resolver.resolve(entity);

        assertTrue(resolved.isPresent());
        assertEquals(InteractionTargetType.BUILDING, resolved.orElseThrow().targetType());
        assertEquals(entityId.toString(), resolved.orElseThrow().targetId());
        assertEquals("Castle Keep", resolved.orElseThrow().displayName());
        assertEquals("castle", resolved.orElseThrow().metadata().get("structureKind"));
        assertEquals("castle_keep", resolved.orElseThrow().metadata().get("castleType"));
    }

    @Test
    void untaggedPlayerOrEntityDoesNotResolve() {
        Entity player = proxy(Player.class, Map.of(
                "getUniqueId", UUID.fromString("00000000-0000-0000-0000-000000000789"),
                "getType", EntityType.PLAYER,
                "getCustomName", "Miner",
                "getWorld", world("kingdom"),
                "getScoreboardTags", Set.of("tavall.kingdom.npc")
        ));
        Entity plainEntity = proxy(Entity.class, Map.of(
                "getUniqueId", UUID.fromString("00000000-0000-0000-0000-000000000321"),
                "getType", EntityType.ARMOR_STAND,
                "getCustomName", "Marker",
                "getWorld", world("kingdom"),
                "getScoreboardTags", Set.of("minecraft:glow")
        ));

        assertTrue(resolver.resolve(player).isEmpty());
        assertTrue(resolver.resolve(plainEntity).isEmpty());
    }

    private World world(String name) {
        return proxy(World.class, Map.of("getName", name));
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Map<String, Object> values) {
        Map<String, Object> safeValues = new LinkedHashMap<String, Object>(values);
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String methodName = method.getName();
                if (safeValues.containsKey(methodName)) {
                    return safeValues.get(methodName);
                }
                if ("toString".equals(methodName)) {
                    return type.getSimpleName() + safeValues;
                }
                if ("hashCode".equals(methodName)) {
                    return System.identityHashCode(proxy);
                }
                if ("equals".equals(methodName)) {
                    return proxy == args[0];
                }
                return defaultValue(method.getReturnType());
            }
        };
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private Object defaultValue(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0.0F;
        }
        if (returnType == double.class) {
            return 0.0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
