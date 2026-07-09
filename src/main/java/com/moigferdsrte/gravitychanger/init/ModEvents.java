package com.moigferdsrte.gravitychanger.init;

import com.moigferdsrte.gravitychanger.item.GravityAnchorItem;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ModEvents {
    private static final Map<UUID, Direction> GRAVITY_BEFORE_ANCHOR = new HashMap<>();

    private ModEvents() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickGravityAnchor(player);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((listener, _) -> restoreGravityBeforeAnchor(listener.player));
    }

    private static void tickGravityAnchor(final ServerPlayer player) {
        Direction anchorDirection = getHeldAnchorDirection(player);

        if (anchorDirection == null) {
            restoreGravityBeforeAnchor(player);
            return;
        }
        UUID uuid = player.getUUID();
        GRAVITY_BEFORE_ANCHOR.computeIfAbsent(uuid, _ -> GravityDirectionUtil.getGravityDirection(player));
        if (GravityDirectionUtil.getGravityDirection(player) != anchorDirection) {
            GravityDirectionUtil.setGravityDirectionWithDownLift(player, anchorDirection);
        }
    }

    private static Direction getHeldAnchorDirection(final ServerPlayer player) {
        Item mainHand = player.getMainHandItem().getItem();
        if (mainHand instanceof GravityAnchorItem anchorItem) {
            return anchorItem.getDirection();
        }

        Item offHand = player.getOffhandItem().getItem();
        if (offHand instanceof GravityAnchorItem anchorItem) {
            return anchorItem.getDirection();
        }

        return null;
    }

    private static void restoreGravityBeforeAnchor(final ServerPlayer player) {
        Direction previousDirection = GRAVITY_BEFORE_ANCHOR.remove(player.getUUID());
        if (previousDirection != null && GravityDirectionUtil.getGravityDirection(player) != previousDirection) {
            GravityDirectionUtil.setGravityDirection(player, previousDirection);
        }
    }
}
