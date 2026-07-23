package org.tavall.control.commands;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.bootstrap.ResourceGameDomain;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.PlacementRequest;
import org.tavall.control.domain.PlacementResult;
import org.tavall.control.resources.ResourceType;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Handles `/kd place ...` command flows.
 */
public final class KingdomPlacementCommandSupport implements ResourceGameDomain, IDependencyInjectableConcrete {
    public void handle(CommandContext context, Player player, List<String> tokens) {
        if (tokens.size() < 2) {
            context.sendMessage(Message.raw("Usage: /kd place castle|node <type>|building <type>|confirm [here]|move <dx> [dy] <dz>|cancel|status|preview").color("yellow"));
            return;
        }
        String action = tokens.get(1).toLowerCase(Locale.ROOT);
        switch (action) {
            case "castle" -> armCastlePlacement(context, player);
            case "node" -> armNodePlacement(context, player, tokens);
            case "building" -> armBuildingPlacement(context, player, tokens);
            case "confirm" -> sendResult(context, confirmPlacement(context, player, tokens));
            case "move" -> sendResult(context, handleMove(context, player, tokens));
            case "cancel" -> sendResult(context, getPlacementModeHandler().cancelPlacement(player.getUuid()));
            case "status" -> sendStatus(context, player);
            case "preview" -> sendResult(context, getPlacementModeHandler().refreshPreview(player));
            default -> context.sendMessage(Message.raw("Unknown place action.").color("red"));
        }
    }

    private void armCastlePlacement(CommandContext context, Player player) {
        getPlacementModeHandler().armCastlePlacement(player);
        context.sendMessage(Message.raw("Castle placement armed. Look at the ground and click, or use /kd place confirm.").color("green"));
    }

    private void armNodePlacement(CommandContext context, Player player, List<String> tokens) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd place node <food|wood|iron>").color("yellow"));
            return;
        }
        ResourceType resourceType = parseResource(tokens.get(2));
        if (resourceType == null) {
            context.sendMessage(Message.raw("Unknown resource type.").color("red"));
            return;
        }
        getPlacementModeHandler().armNodePlacement(player, resourceType);
        context.sendMessage(Message.raw(resourceType + " node placement armed. Look at the ground and click, or use /kd place confirm.").color("green"));
    }

    private void armBuildingPlacement(CommandContext context, Player player, List<String> tokens) {
        if (tokens.size() < 3) {
            context.sendMessage(Message.raw("Usage: /kd place building <farmstead|lumber_mill|iron_works|barracks|workshop>").color("yellow"));
            return;
        }
        BuildingType buildingType = BuildingType.parse(tokens.get(2));
        if (buildingType == null) {
            context.sendMessage(Message.raw("Unknown building type.").color("red"));
            return;
        }
        getPlacementModeHandler().armBuildingPlacement(player, buildingType);
        context.sendMessage(Message.raw(buildingType.displayName() + " placement armed. Look at the ground and click, or use /kd place confirm.").color("green"));
    }

    private void sendStatus(CommandContext context, Player player) {
        Optional<PlacementRequest> request = getPlacementModeHandler().activePlacement(player.getUuid());
        if (request.isEmpty()) {
            context.sendMessage(Message.raw("No active placement.").color("yellow"));
            return;
        }
        context.sendMessage(Message.raw(request.get().summary() + " active in " + request.get().armedWorldName() + ".").color("yellow"));
    }

    private void sendResult(CommandContext context, PlacementResult result) {
        if (!result.handled()) {
            context.sendMessage(Message.raw("No active placement.").color("yellow"));
            return;
        }
        context.sendMessage(Message.raw(result.message()).color(result.success() ? "green" : "red"));
    }

    private ResourceType parseResource(String token) {
        return switch (token.toLowerCase(Locale.ROOT)) {
            case "food" -> ResourceType.FOOD;
            case "wood" -> ResourceType.WOOD;
            case "iron" -> ResourceType.IRON;
            default -> null;
        };
    }

    private PlacementResult confirmPlacement(CommandContext context, Player player, List<String> tokens) {
        if (tokens.size() >= 3 && "here".equalsIgnoreCase(tokens.get(2))) {
            Vector3i currentBlock = currentStandingBlock(player);
            if (currentBlock == null) {
                context.sendMessage(Message.raw("Unable to resolve current standing block.").color("red"));
                return PlacementResult.failure("Unable to resolve current standing block.");
            }
            return getPlacementModeHandler().confirmPlacement(player, currentBlock);
        }
        return getPlacementModeHandler().confirmPlacementFromAim(player);
    }

    private PlacementResult handleMove(CommandContext context, Player player, List<String> tokens) {
        if (tokens.size() < 4) {
            context.sendMessage(Message.raw("Usage: /kd place move <dx> [dy] <dz>").color("yellow"));
            return PlacementResult.failure("Missing move arguments.");
        }
        try {
            int dx = Integer.parseInt(tokens.get(2));
            int dy;
            int dz;
            if (tokens.size() >= 5) {
                dy = Integer.parseInt(tokens.get(3));
                dz = Integer.parseInt(tokens.get(4));
            } else {
                dy = 0;
                dz = Integer.parseInt(tokens.get(3));
            }
            return getPlacementModeHandler().moveStagedPlacement(player, new Vector3i(dx, dy, dz));
        } catch (NumberFormatException ex) {
            context.sendMessage(Message.raw("Move values must be whole numbers.").color("red"));
            return PlacementResult.failure("Move values must be whole numbers.");
        }
    }

    private Vector3i currentStandingBlock(Player player) {
        TransformComponent transform = player.getTransformComponent();
        if (transform == null) {
            return null;
        }
        Vector3d position = transform.getPosition();
        if (position == null) {
            return null;
        }
        return new Vector3i(
                (int) Math.floor(position.getX()),
                (int) Math.floor(position.getY()) - 1,
                (int) Math.floor(position.getZ())
        );
    }
}

