package com.moigferdsrte.gravitychanger.entity.ai;

import com.moigferdsrte.gravitychanger.util.RotationUtil;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public final class DirectionalGroundPathNavigation extends GroundPathNavigation {
    private final Direction gravityDirection;

    public DirectionalGroundPathNavigation(final Mob mob, final Level level, final Direction gravityDirection) {
        super(mob, level);
        this.gravityDirection = gravityDirection;
    }

    public Direction gravityDirection() {
        return this.gravityDirection;
    }

    @Override
    protected @NonNull PathFinder createPathFinder(final int maxVisitedNodes) {
        this.nodeEvaluator = new DirectionalGroundNodeEvaluator();
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    public @NonNull Path createPath(final @NonNull BlockPos pos, final int reachRange) {
        return this.createPath(Set.of(pos), reachRange);
    }

    @Override
    public @NonNull Path createPath(final Entity target, final int reachRange) {
        Vec3 projectedTarget = DirectionalMobAiUtil.projectOntoMovementPlane(
            this.mob.position(),
            target.position(),
            this.gravityDirection
        );
        BlockPos targetNode = DirectionalGroundNodeEvaluator.nodePosition(projectedTarget, this.gravityDirection);
        return this.createPath(Set.of(targetNode), 16, false, reachRange);
    }

    @Override
    protected Vec3 getTempMobPos() {
        return this.mob.position();
    }

    @Override
    protected double getGroundY(final Vec3 target) {
        return target.y;
    }

    @Override
    public boolean isStableDestination(final BlockPos pos) {
        return this.level.getBlockState(pos.relative(this.gravityDirection)).isSolidRender();
    }

    @Override
    protected void followThePath() {
        Vec3 mobPosition = this.mob.position();
        Vec3 nodePosition = this.nextEntityPosition();
        Vec3 localDelta = RotationUtil.vecWorldToPlayer(nodePosition.subtract(mobPosition), this.gravityDirection);
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F
            ? this.mob.getBbWidth() / 2.0F
            : 0.75F - this.mob.getBbWidth() / 2.0F;

        boolean closeEnough = Math.abs(localDelta.x) < this.maxDistanceToWaypoint
            && Math.abs(localDelta.z) < this.maxDistanceToWaypoint
            && Math.abs(localDelta.y) < this.getMaxVerticalDistanceToWaypoint();
        if (closeEnough) {
            this.path.advance();
        }

        this.doStuckDetection(mobPosition);
    }

    @Override
    public void tick() {
        this.tick++;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (this.isDone()) {
            return;
        }

        if (this.canUpdatePath()) {
            this.followThePath();
        }

        if (!this.isDone()) {
            Vec3 target = this.nextEntityPosition();
            this.mob.getMoveControl().setWantedPosition(target.x, target.y, target.z, this.speedModifier);
        }
    }

    private Vec3 nextEntityPosition() {
        return DirectionalGroundNodeEvaluator.entityPosition(this.path.getNextNodePos(), this.gravityDirection);
    }
}
