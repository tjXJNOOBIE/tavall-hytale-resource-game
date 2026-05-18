package org.tavall.minecraft.server;

import org.tavall.minecraft.server.protection.MinecraftBukkitStructureProtectionHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitStructureProtectionHandlerTest {
    @Test
    void castleAndBuildingRegionsAreProtectedUntilCleared() {
        MinecraftBukkitStructureProtectionHandler handler = new MinecraftBukkitStructureProtectionHandler();
        UUID ownerId = UUID.fromString("00000000-0000-0000-0000-00000000ca57");
        Location castleAnchor = new Location(world("kingdom"), 100.0D, 64.0D, 100.0D);
        Location buildingAnchor = new Location(world("kingdom"), 140.0D, 64.0D, 100.0D);

        handler.registerCastle(ownerId, castleAnchor);
        handler.registerBuilding(ownerId, buildingAnchor, "farmstead");

        assertTrue(handler.isProtected(new Location(world("kingdom"), 100.0D, 64.0D, 100.0D)));
        assertTrue(handler.isProtected(new Location(world("kingdom"), 140.0D, 64.0D, 100.0D)));
        assertTrue(handler.protectionReason(new Location(world("kingdom"), 100.0D, 64.0D, 100.0D)).orElseThrow().contains("Castle"));
        assertTrue(handler.protectionReason(new Location(world("kingdom"), 140.0D, 64.0D, 100.0D)).orElseThrow().contains("Building"));

        handler.clearCastle(ownerId);
        handler.clearBuilding(ownerId);

        assertFalse(handler.isProtected(new Location(world("kingdom"), 100.0D, 64.0D, 100.0D)));
        assertFalse(handler.isProtected(new Location(world("kingdom"), 140.0D, 64.0D, 100.0D)));
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
