package com.moigferdsrte.gravitychanger.compat.seamlessportals.mixin;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import java.lang.reflect.Method;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

/**
 * Replaces SeamlessPortals' player fit check with the same check using a
 * gravity-oriented box, while retaining its active portal clipping.
 */
@Mixin(value = Player.class, priority = 900)
public abstract class SeamlessPortalsPlayerCollisionMixin {
    @Unique
    private static final String GRAVITYCHANGER_PORTAL_ENTITY_EXTENSION =
        "qouteall.imm_ptl.core.ducks.IEEntity";

    @Unique
    private static volatile boolean gravitychanger$portalMethodResolved;

    @Unique
    private static Method gravitychanger$activeCollisionBoxMethod;

    /**
     * @author GravityChanger
     * @reason Combine directional collision boxes with SeamlessPortals portal clipping.
     */
    @Overwrite
    public boolean canPlayerFitWithinBlocksAndEntitiesWhen(final Pose pose) {
        Player player = (Player)(Object)this;
        EntityDimensions dimensions = player.getDimensions(pose);
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(player);
        AABB box = gravityDirection == Direction.DOWN
            ? dimensions.makeBoundingBox(player.position())
            : RotationUtil.makeBoxFromDimensions(dimensions, gravityDirection, player.position());
        AABB activeCollisionBox = gravitychanger$getActiveCollisionBox(player, box);
        if (activeCollisionBox == null) {
            return true;
        }
        return player.level().noCollision(player, activeCollisionBox.deflate(1.0E-7));
    }

    @Unique
    private static AABB gravitychanger$getActiveCollisionBox(final Entity entity, final AABB box) {
        if (!gravitychanger$portalMethodResolved) {
            synchronized (SeamlessPortalsPlayerCollisionMixin.class) {
                if (!gravitychanger$portalMethodResolved) {
                    try {
                        Class<?> extension = Class.forName(GRAVITYCHANGER_PORTAL_ENTITY_EXTENSION);
                        gravitychanger$activeCollisionBoxMethod = extension.getMethod(
                            "ip_getActiveCollisionBox", AABB.class
                        );
                    } catch (ReflectiveOperationException | LinkageError ignored) {
                        gravitychanger$activeCollisionBoxMethod = null;
                    }
                    gravitychanger$portalMethodResolved = true;
                }
            }
        }

        Method method = gravitychanger$activeCollisionBoxMethod;
        if (method == null || !method.getDeclaringClass().isInstance(entity)) {
            return box;
        }

        try {
            return (AABB)method.invoke(entity, box);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return box;
        }
    }
}
