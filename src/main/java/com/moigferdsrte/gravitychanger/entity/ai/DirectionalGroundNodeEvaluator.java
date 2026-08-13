package com.moigferdsrte.gravitychanger.entity.ai;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public final class DirectionalGroundNodeEvaluator extends NodeEvaluator {
    private static final double SUPPORT_PROBE_DISTANCE = 0.05;
    private static final Direction[] LOCAL_HORIZONTAL_DIRECTIONS = {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST
    };

    private Direction gravityDirection = Direction.DOWN;

    @Override
    public void prepare(final @NonNull PathNavigationRegion level, final @NonNull Mob entity) {
        super.prepare(level, entity);
        this.gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
    }

    @Override
    public @NonNull Node getStart() {
        return this.getNode(nodePosition(this.mob.position(), this.gravityDirection));
    }

    @Override
    public @NonNull Target getTarget(final double x, final double y, final double z) {
        return this.getTargetNodeAt(x, y, z);
    }

    @Override
    public int getNeighbors(final Node @NonNull [] neighbors, final Node current) {
        int count = 0;
        BlockPos currentPos = current.asBlockPos();
        int maxStep = Math.max(1, (int)Math.ceil(this.mob.maxUpStep()));
        int maxFall = Math.clamp(this.mob.getMaxFallDistance(), 1, 4);

        for (Direction localDirection : LOCAL_HORIZONTAL_DIRECTIONS) {
            Direction worldDirection = localToWorld(localDirection, this.gravityDirection);
            BlockPos candidate = currentPos.relative(worldDirection);
            BlockPos accepted = this.findStandableNode(candidate, maxStep, maxFall);
            if (accepted != null) {
                Node node = this.getNode(accepted);
                node.type = PathType.WALKABLE;
                node.costMalus = Math.max(node.costMalus, this.mob.getPathfindingMalus(PathType.WALKABLE));
                if (isUsableNeighbor(node)) {
                    neighbors[count++] = node;
                }
            }
        }

        return count;
    }

    @Override
    public @NonNull PathType getPathTypeOfMob(
        final @NonNull PathfindingContext context,
        final int x,
        final int y,
        final int z,
        final @NonNull Mob mob
    ) {
        Direction direction = GravityDirectionUtil.getGravityDirection(mob);
        return this.isStandable(context, mob, direction, new BlockPos(x, y, z))
            ? PathType.WALKABLE
            : PathType.BLOCKED;
    }

    @Override
    public @NonNull PathType getPathType(final @NonNull PathfindingContext context, final int x, final int y, final int z) {
        if (this.mob == null) {
            return PathType.BLOCKED;
        }

        return this.isStandable(context, this.mob, this.gravityDirection, new BlockPos(x, y, z))
            ? PathType.WALKABLE
            : PathType.BLOCKED;
    }

    @Override
    public @NonNull PathType getPathType(final @NonNull Mob mob, final @NonNull BlockPos pos) {
        Direction direction = GravityDirectionUtil.getGravityDirection(mob);
        return this.isStandable(new PathfindingContext(mob.level(), mob), mob, direction, pos)
            ? PathType.WALKABLE
            : PathType.BLOCKED;
    }

    private BlockPos findStandableNode(final BlockPos candidate, final int maxStep, final int maxFall) {
        if (this.isStandable(candidate)) {
            return candidate;
        }

        Direction localUp = this.gravityDirection.getOpposite();
        for (int step = 1; step <= maxStep; step++) {
            BlockPos steppedUp = candidate.relative(localUp, step);
            if (this.isStandable(steppedUp)) {
                return steppedUp;
            }
        }

        for (int fall = 1; fall <= maxFall; fall++) {
            BlockPos steppedDown = candidate.relative(this.gravityDirection, fall);
            if (this.isStandable(steppedDown)) {
                return steppedDown;
            }
        }

        return null;
    }

    private boolean isStandable(final BlockPos nodePos) {
        return this.isStandable(this.currentContext, this.mob, this.gravityDirection, nodePos);
    }

    private boolean isStandable(
        final PathfindingContext context,
        final Mob mob,
        final Direction direction,
        final BlockPos nodePos
    ) {
        Vec3 entityPosition = entityPosition(nodePos, direction);
        AABB entityBox = RotationUtil.makeBoxFromDimensions(
            mob.getDimensions(mob.getPose()),
            direction,
            entityPosition
        ).deflate(1.0E-5);
        if (!context.level().noCollision(mob, entityBox)) {
            return false;
        }

        Vec3 supportProbe = Vec3.atLowerCornerOf(direction.getUnitVec3i()).scale(SUPPORT_PROBE_DISTANCE);
        return !context.level().noCollision(mob, entityBox.move(supportProbe));
    }

    public static BlockPos nodePosition(final Vec3 entityPosition, final Direction gravityDirection) {
        Vec3 insideNode = entityPosition.subtract(
            gravityDirection.getStepX() * 1.0E-4,
            gravityDirection.getStepY() * 1.0E-4,
            gravityDirection.getStepZ() * 1.0E-4
        );
        return BlockPos.containing(insideNode);
    }

    public static Vec3 entityPosition(final BlockPos nodePos, final Direction gravityDirection) {
        return Vec3.atCenterOf(nodePos).add(
            gravityDirection.getStepX() * 0.5,
            gravityDirection.getStepY() * 0.5,
            gravityDirection.getStepZ() * 0.5
        );
    }

    static Direction localToWorld(final Direction localDirection, final Direction gravityDirection) {
        Vec3 worldVector = RotationUtil.vecPlayerToWorld(
            Vec3.atLowerCornerOf(localDirection.getUnitVec3i()),
            gravityDirection
        );
        return Direction.getNearest(
            (int)Math.round(worldVector.x),
            (int)Math.round(worldVector.y),
            (int)Math.round(worldVector.z),
            Direction.NORTH
        );
    }

    static boolean isUsableNeighbor(final Node node) {
        // 原版 PathFinder 依赖求值器阻止已关闭节点重新进入搜索，否则 cameFrom 可能形成环。
        return !node.closed && node.costMalus >= 0.0F;
    }
}
