package com.moigferdsrte.gravitychanger.mixin;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique
    private static final double GRAVITYCHANGER_COLLISION_EPSILON = 1.0E-7;

    @Unique
    private static final double GRAVITYCHANGER_SUPPORT_EPSILON = 1.0E-3;

    @Unique
    private Vec3 gravitychanger$lastMoveDelta = Vec3.ZERO;

    @Unique
    private Vec3 gravitychanger$lastMoveMovement = Vec3.ZERO;

    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    private float eyeHeight;

    @Shadow
    public Optional<BlockPos> mainSupportingBlockPos;

    @Shadow
    private boolean onGroundNoBlocks;

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract boolean onGround();

    @Shadow
    public abstract float maxUpStep();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract Vec3 position();

    @Shadow
    protected abstract Vec3 getPassengerAttachmentPoint(final Entity passenger, final EntityDimensions dimensions, final float scale);

    @Shadow
    private Vec3 collide(final Vec3 movement) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract void checkFallDamage(
        final double ya,
        final boolean onGround,
        final BlockState onState,
        final BlockPos pos
    );

    @Shadow
    private void restituteMovementAfterCollisions(final BlockState effectState, final boolean xCollision, final boolean zCollision, final Vec3 movement) {
        throw new AssertionError();
    }

    @Shadow
    protected static Vec3 getInputVector(final Vec3 input, final float speed, final float yRot) {
        throw new AssertionError();
    }

    @Inject(method = "makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$makeDirectionalBoundingBox(final Vec3 position, final CallbackInfoReturnable<AABB> cir) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection != Direction.DOWN) {
            cir.setReturnValue(RotationUtil.makeBoxFromDimensions(this.dimensions, gravityDirection, position));
        }
    }

    @Inject(method = "calculateViewVector", at = @At("RETURN"), cancellable = true)
    private void gravitychanger$calculateDirectionalViewVector(
        final float xRot,
        final float yRot,
        final CallbackInfoReturnable<Vec3> cir
    ) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection != Direction.DOWN) {
            cir.setReturnValue(RotationUtil.vecPlayerToWorld(cir.getReturnValue(), gravityDirection));
        }
    }

    @Inject(method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$getDirectionalEyePosition(final CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection != Direction.DOWN) {
            cir.setReturnValue(entity.position().add(gravitychanger$getDirectionalEyeOffset(gravityDirection)));
        }
    }

    @Inject(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$getDirectionalEyePosition(final float partialTickTime, final CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection != Direction.DOWN) {
            Vec3 position = entity.getPosition(partialTickTime);
            cir.setReturnValue(position.add(gravitychanger$getDirectionalEyeOffset(gravityDirection)));
        }
    }

    @Inject(method = "getEyeY", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$getDirectionalEyeY(final CallbackInfoReturnable<Double> cir) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection != Direction.DOWN) {
            cir.setReturnValue(entity.position().y + gravitychanger$getDirectionalEyeOffset(gravityDirection).y);
        }
    }

    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$checkDirectionalEyeInWall(final CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        if (entity.noPhysics) {
            cir.setReturnValue(false);
            return;
        }

        float checkWidth = this.dimensions.width() * 0.8F;
        AABB eyeBox = RotationUtil.makeDirectionalEyeBox(entity.getEyePosition(), checkWidth, gravityDirection);
        boolean inWall = BlockPos.betweenClosedStream(eyeBox).anyMatch(pos -> {
            BlockState state = this.level().getBlockState(pos);
            return !state.isAir()
                && state.isSuffocating(this.level(), pos)
                && Shapes.joinIsNotEmpty(
                    state.getCollisionShape(this.level(), pos).move(pos),
                    Shapes.create(eyeBox),
                    BooleanOp.AND
                );
        });
        cir.setReturnValue(inWall);
    }

    @Inject(method = "checkSupportingBlock", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$checkDirectionalSupportingBlock(
        final boolean onGround,
        final Vec3 movement,
        final CallbackInfo ci
    ) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        ci.cancel();
        if (onGround) {
            AABB testArea = gravitychanger$getSupportArea(this.getBoundingBox(), gravityDirection);
            Optional<BlockPos> supportingBlock = this.level().findSupportingBlock(entity, testArea);
            if (supportingBlock.isPresent() || this.onGroundNoBlocks) {
                this.mainSupportingBlockPos = supportingBlock;
            } else if (movement != null) {
                Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
                Vec3 horizontalWorldMovement = RotationUtil.vecPlayerToWorld(localMovement.x, 0.0, localMovement.z, gravityDirection);
                supportingBlock = this.level().findSupportingBlock(entity, testArea.move(horizontalWorldMovement.reverse()));
                this.mainSupportingBlockPos = supportingBlock;
            }

            this.onGroundNoBlocks = supportingBlock.isEmpty();
        } else {
            this.onGroundNoBlocks = false;
            if (this.mainSupportingBlockPos.isPresent()) {
                this.mainSupportingBlockPos = Optional.empty();
            }
        }
    }

    @Inject(method = "getOnPos(F)Lnet/minecraft/core/BlockPos;", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$getDirectionalOnPos(final float offset, final CallbackInfoReturnable<BlockPos> cir) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        if (this.mainSupportingBlockPos.isPresent()) {
            cir.setReturnValue(this.mainSupportingBlockPos.get());
            return;
        }

        Vec3 probe = this.position().add(
            gravityDirection.getStepX() * offset,
            gravityDirection.getStepY() * offset,
            gravityDirection.getStepZ() * offset
        );
        cir.setReturnValue(BlockPos.containing(probe));
    }

    @Inject(method = "collide", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$collideInLocalSpace(final Vec3 movement, final CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN || movement.lengthSqr() == 0.0) {
            return;
        }

        AABB boundingBox = this.getBoundingBox();
        List<VoxelShape> entityColliders = this.level().getEntityCollisions(entity, boundingBox.expandTowards(movement));
        Vec3 movementStep = gravitychanger$collideBoundingBoxInLocalSpace(
            entity,
            movement,
            boundingBox,
            this.level(),
            entityColliders,
            gravityDirection
        );
        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        Vec3 localMovementStep = RotationUtil.vecWorldToPlayer(movementStep, gravityDirection);
        boolean xCollision = localMovement.x != localMovementStep.x;
        boolean yCollision = localMovement.y != localMovementStep.y;
        boolean zCollision = localMovement.z != localMovementStep.z;
        boolean onGroundAfterCollision = yCollision && localMovement.y < 0.0;

        if (this.maxUpStep() > 0.0F && (onGroundAfterCollision || this.onGround()) && (xCollision || zCollision)) {
            AABB groundedAABB = onGroundAfterCollision
                ? boundingBox.move(RotationUtil.vecPlayerToWorld(0.0, localMovementStep.y, 0.0, gravityDirection))
                : boundingBox;
            AABB stepUpAABB = groundedAABB.expandTowards(
                RotationUtil.vecPlayerToWorld(localMovement.x, this.maxUpStep(), localMovement.z, gravityDirection)
            );
            if (!onGroundAfterCollision) {
                stepUpAABB = stepUpAABB.expandTowards(RotationUtil.vecPlayerToWorld(0.0, -1.0E-5F, 0.0, gravityDirection));
            }

            List<VoxelShape> colliders = gravitychanger$collectCollidersIgnoringWorldBorder(entity, this.level(), entityColliders, stepUpAABB);
            float stepHeightToSkip = (float)localMovementStep.y;
            float[] candidateStepUpHeights = gravitychanger$collectCandidateStepUpHeights(
                groundedAABB,
                colliders,
                this.maxUpStep(),
                stepHeightToSkip,
                gravityDirection
            );

            for (float candidateStepUpHeight : candidateStepUpHeights) {
                Vec3 stepFromGround = gravitychanger$collideWithShapesInLocalSpace(
                    RotationUtil.vecPlayerToWorld(localMovement.x, candidateStepUpHeight, localMovement.z, gravityDirection),
                    groundedAABB,
                    colliders,
                    gravityDirection
                );
                Vec3 localStepFromGround = RotationUtil.vecWorldToPlayer(stepFromGround, gravityDirection);
                if (gravitychanger$horizontalDistanceSqr(localStepFromGround) > gravitychanger$horizontalDistanceSqr(localMovementStep)) {
                    double distanceToGround = gravitychanger$getLocalMinY(boundingBox, gravityDirection)
                        - gravitychanger$getLocalMinY(groundedAABB, gravityDirection);
                    cir.setReturnValue(stepFromGround.subtract(RotationUtil.vecPlayerToWorld(0.0, distanceToGround, 0.0, gravityDirection)));
                    return;
                }
            }
        }

        cir.setReturnValue(movementStep);
    }

    @Inject(
        method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gravitychanger$positionRiderOnDirectionalVehicle(
        final Entity passenger,
        final Entity.MoveFunction moveFunction,
        final CallbackInfo ci
    ) {
        Entity vehicle = (Entity)(Object)this;
        Direction vehicleGravityDirection = GravityDirectionUtil.getGravityDirection(vehicle);
        if (vehicleGravityDirection == Direction.DOWN) {
            return;
        }

        ci.cancel();
        Vec3 seatOffset = this.getPassengerAttachmentPoint(passenger, this.dimensions, 1.0F);
        Vec3 passengerAttachmentOffset = passenger.getVehicleAttachmentPoint(vehicle);
        Vec3 directionalSeatOffset = RotationUtil.vecPlayerToWorld(seatOffset, vehicleGravityDirection);
        Vec3 directionalPassengerAttachmentOffset = RotationUtil.vecPlayerToWorld(passengerAttachmentOffset, vehicleGravityDirection);
        Vec3 position = vehicle.position().add(directionalSeatOffset).subtract(directionalPassengerAttachmentOffset);
        moveFunction.accept(passenger, position.x, position.y, position.z);
    }

    @Redirect(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$captureMoveDeltaBeforeCollision(final Entity entity, final Vec3 movement) {
        this.gravitychanger$lastMoveDelta = movement;
        this.gravitychanger$lastMoveMovement = this.collide(movement);
        return this.gravitychanger$lastMoveMovement;
    }

    @Redirect(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;abs(D)D",
            ordinal = 0
        )
    )
    private double gravitychanger$useLocalVerticalMovement(final double a) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection((Entity)(Object)this);
        if (gravityDirection == Direction.DOWN) {
            return Math.abs(a);
        }

        return Math.abs(RotationUtil.vecWorldToPlayer(this.gravitychanger$lastMoveDelta, gravityDirection).y);
    }

    @Redirect(
        method = "moveRelative",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$rotateRelativeMovement(final Vec3 input, final float speed, final float yRot) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        Vec3 movement = getInputVector(input, speed * GravityDirectionUtil.getMovementSpeedScale(entity), yRot);
        return gravityDirection == Direction.DOWN ? movement : RotationUtil.vecPlayerToWorld(movement, gravityDirection);
    }

    @Redirect(
        method = "applyGravity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$applyDirectionalGravity(final Vec3 movement, final double x, final double y, final double z) {
        Entity entity = (Entity)(Object)this;
        if (GravityDirectionUtil.getGravityDirection(entity) == Direction.DOWN) {
            return movement.add(x, GravityDirectionUtil.scaleGravity(entity, y), z);
        }

        return movement.add(GravityDirectionUtil.getGravityVector(entity, Math.abs(y)));
    }

    @Redirect(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setOnGroundWithMovement(ZZLnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void gravitychanger$setLocalGroundState(
        final Entity entity,
        final boolean onGround,
        final boolean horizontalCollision,
        final Vec3 movement
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            entity.setOnGroundWithMovement(onGround, horizontalCollision, movement);
            return;
        }

        Vec3 intendedLocalMovement = RotationUtil.vecWorldToPlayer(this.gravitychanger$lastMoveDelta, gravityDirection);
        Vec3 clippedLocalMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        boolean localVerticalCollision = !Mth.equal(intendedLocalMovement.y, clippedLocalMovement.y);
        boolean localGround = localVerticalCollision && intendedLocalMovement.y < 0.0;
        boolean localHorizontalCollision = !Mth.equal(intendedLocalMovement.x, clippedLocalMovement.x)
            || !Mth.equal(intendedLocalMovement.z, clippedLocalMovement.z);
        entity.verticalCollision = localVerticalCollision;
        entity.verticalCollisionBelow = localGround;
        entity.setOnGroundWithMovement(localGround, localHorizontalCollision, movement);
    }

    @Redirect(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;restituteMovementAfterCollisions(Lnet/minecraft/world/level/block/state/BlockState;ZZLnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void gravitychanger$restituteDirectionalMovementAfterCollisions(
        final Entity entity,
        final BlockState effectState,
        final boolean xCollision,
        final boolean zCollision,
        final Vec3 movement
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            this.restituteMovementAfterCollisions(effectState, xCollision, zCollision, movement);
            return;
        }

        Vec3 intendedLocalMovement = RotationUtil.vecWorldToPlayer(this.gravitychanger$lastMoveDelta, gravityDirection);
        Vec3 clippedLocalMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        Vec3 currentLocalMovement = RotationUtil.vecWorldToPlayer(entity.getDeltaMovement(), gravityDirection);
        double localX = !Mth.equal(intendedLocalMovement.x, clippedLocalMovement.x) ? 0.0 : currentLocalMovement.x;
        double localY = !Mth.equal(intendedLocalMovement.y, clippedLocalMovement.y) ? 0.0 : currentLocalMovement.y;
        double localZ = !Mth.equal(intendedLocalMovement.z, clippedLocalMovement.z) ? 0.0 : currentLocalMovement.z;
        entity.setDeltaMovement(RotationUtil.vecPlayerToWorld(localX, localY, localZ, gravityDirection));
    }

    @Redirect(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$applyBlockSpeedFactorInLocalSpace(
        final Vec3 movement,
        final double xScale,
        final double yScale,
        final double zScale
    ) {
        Entity entity = (Entity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return movement.multiply(xScale, yScale, zScale);
        }

        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        Vec3 localScaledMovement = new Vec3(localMovement.x * xScale, localMovement.y * yScale, localMovement.z * zScale);
        return RotationUtil.vecPlayerToWorld(localScaledMovement, gravityDirection);
    }

    @Redirect(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
        )
    )
    private void gravitychanger$checkFallDamageWithLocalMovement(
        final Entity entity,
        final double ya,
        final boolean onGround,
        final BlockState onState,
        final BlockPos pos
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        double localY = gravityDirection == Direction.DOWN
            ? ya
            : RotationUtil.vecWorldToPlayer(this.gravitychanger$lastMoveMovement, gravityDirection).y;
        this.checkFallDamage(localY * GravityDirectionUtil.getFallDistanceScale(entity), onGround, onState, pos);
    }

    @Redirect(
        method = "doCheckFallDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
        )
    )
    private void gravitychanger$doCheckFallDamageWithLocalMovement(
        final Entity entity,
        final double ya,
        final boolean onGround,
        final BlockState onState,
        final BlockPos pos,
        final double xa,
        final double originalYa,
        final double za,
        final boolean originalOnGround
    ) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        double localY = gravityDirection == Direction.DOWN
            ? ya
            : RotationUtil.vecWorldToPlayer(xa, originalYa, za, gravityDirection).y;
        this.checkFallDamage(localY * GravityDirectionUtil.getFallDistanceScale(entity), onGround, onState, pos);
    }

    @Unique
    private static AABB gravitychanger$getSupportArea(final AABB boundingBox, final Direction gravityDirection) {
        double epsilon = GRAVITYCHANGER_SUPPORT_EPSILON;
        return switch (gravityDirection) {
            case DOWN -> new AABB(boundingBox.minX, boundingBox.minY - epsilon, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            case UP -> new AABB(boundingBox.minX, boundingBox.maxY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY + epsilon, boundingBox.maxZ);
            case NORTH -> new AABB(boundingBox.minX, boundingBox.minY, boundingBox.minZ - epsilon, boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            case SOUTH -> new AABB(boundingBox.minX, boundingBox.minY, boundingBox.maxZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ + epsilon);
            case WEST -> new AABB(boundingBox.minX - epsilon, boundingBox.minY, boundingBox.minZ, boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            case EAST -> new AABB(boundingBox.maxX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX + epsilon, boundingBox.maxY, boundingBox.maxZ);
        };
    }

    @Unique
    private Vec3 gravitychanger$getDirectionalEyeOffset(final Direction gravityDirection) {
        return RotationUtil.vecPlayerToWorld(0.0, this.eyeHeight, 0.0, gravityDirection);
    }

    @Unique
    private static Vec3 gravitychanger$collideWithShapesInLocalSpace(
        final Vec3 movement,
        final AABB boundingBox,
        final List<VoxelShape> colliders,
        final Direction gravityDirection
    ) {
        if (colliders.isEmpty()) {
            return movement;
        }

        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        AABB movedBox = boundingBox;
        double localY = gravitychanger$removeTinyCollisionMovement(
            gravitychanger$collideLocalAxis(Direction.UP, localMovement.y, movedBox, colliders, gravityDirection)
        );
        if (localY != 0.0) {
            movedBox = movedBox.move(RotationUtil.vecPlayerToWorld(0.0, localY, 0.0, gravityDirection));
        }

        boolean zFirst = Math.abs(localMovement.x) < Math.abs(localMovement.z);
        double localX = localMovement.x;
        double localZ = localMovement.z;
        if (zFirst) {
            localZ = gravitychanger$removeTinyCollisionMovement(
                gravitychanger$collideLocalAxis(Direction.SOUTH, localZ, movedBox, colliders, gravityDirection)
            );
            if (localZ != 0.0) {
                movedBox = movedBox.move(RotationUtil.vecPlayerToWorld(0.0, 0.0, localZ, gravityDirection));
            }
        }

        localX = gravitychanger$removeTinyCollisionMovement(
            gravitychanger$collideLocalAxis(Direction.EAST, localX, movedBox, colliders, gravityDirection)
        );
        if (!zFirst && localX != 0.0) {
            movedBox = movedBox.move(RotationUtil.vecPlayerToWorld(localX, 0.0, 0.0, gravityDirection));
        }

        if (!zFirst) {
            localZ = gravitychanger$removeTinyCollisionMovement(
                gravitychanger$collideLocalAxis(Direction.SOUTH, localZ, movedBox, colliders, gravityDirection)
            );
        }

        return RotationUtil.vecPlayerToWorld(localX, localY, localZ, gravityDirection);
    }

    @Unique
    private static Vec3 gravitychanger$collideBoundingBoxInLocalSpace(
        final Entity source,
        final Vec3 movement,
        final AABB boundingBox,
        final Level level,
        final List<VoxelShape> entityColliders,
        final Direction gravityDirection
    ) {
        List<VoxelShape> colliders = gravitychanger$collectCollidersIgnoringWorldBorder(
            source,
            level,
            entityColliders,
            boundingBox.expandTowards(movement)
        );
        return gravitychanger$collideWithShapesInLocalSpace(movement, boundingBox, colliders, gravityDirection);
    }

    @Unique
    private static List<VoxelShape> gravitychanger$collectCollidersIgnoringWorldBorder(
        final Entity source,
        final Level level,
        final List<VoxelShape> entityColliders,
        final AABB boundingBox
    ) {
        List<VoxelShape> colliders = new ArrayList<>(entityColliders.size() + 1);
        colliders.addAll(entityColliders);

        WorldBorder worldBorder = level.getWorldBorder();
        if (source != null && worldBorder.isInsideCloseToBorder(source, boundingBox)) {
            colliders.add(worldBorder.getCollisionShape());
        }

        for (VoxelShape blockCollider : level.getBlockCollisions(source, boundingBox)) {
            colliders.add(blockCollider);
        }

        return colliders;
    }

    @Unique
    private static float[] gravitychanger$collectCandidateStepUpHeights(
        final AABB boundingBox,
        final List<VoxelShape> colliders,
        final float maxStepHeight,
        final float stepHeightToSkip,
        final Direction gravityDirection
    ) {
        FloatSet candidates = new FloatArraySet(4);
        Direction localUp = gravitychanger$localDirectionToWorld(Direction.UP, gravityDirection);
        Direction.Axis worldAxis = localUp.getAxis();
        int sign = localUp.getAxisDirection().getStep();
        double localMinY = gravitychanger$getLocalMinY(boundingBox, gravityDirection);

        for (VoxelShape collider : colliders) {
            for (double coord : collider.getCoords(worldAxis)) {
                float relativeCoord = (float)(coord * sign - localMinY);
                if (relativeCoord >= 0.0F && relativeCoord != stepHeightToSkip && relativeCoord <= maxStepHeight) {
                    candidates.add(relativeCoord);
                }
            }
        }

        float[] sortedCandidates = candidates.toFloatArray();
        FloatArrays.unstableSort(sortedCandidates);
        return sortedCandidates;
    }

    @Unique
    private static double gravitychanger$getLocalMinY(final AABB boundingBox, final Direction gravityDirection) {
        return RotationUtil.boxWorldToPlayer(boundingBox, gravityDirection).minY;
    }

    @Unique
    private static double gravitychanger$horizontalDistanceSqr(final Vec3 localMovement) {
        return localMovement.x * localMovement.x + localMovement.z * localMovement.z;
    }

    @Unique
    private static double gravitychanger$removeTinyCollisionMovement(final double movement) {
        return Math.abs(movement) < GRAVITYCHANGER_COLLISION_EPSILON ? 0.0 : movement;
    }

    @Unique
    private static double gravitychanger$collideLocalAxis(
        final Direction localDirection,
        final double localMovement,
        final AABB boundingBox,
        final List<VoxelShape> colliders,
        final Direction gravityDirection
    ) {
        if (localMovement == 0.0) {
            return 0.0;
        }

        Direction worldDirection = gravitychanger$localDirectionToWorld(localDirection, gravityDirection);
        int sign = worldDirection.getAxisDirection().getStep();
        return Shapes.collide(worldDirection.getAxis(), boundingBox, colliders, localMovement * sign) * sign;
    }

    @Unique
    private static Direction gravitychanger$localDirectionToWorld(final Direction localDirection, final Direction gravityDirection) {
        Vec3 worldVector = RotationUtil.vecPlayerToWorld(Vec3.atLowerCornerOf(localDirection.getUnitVec3i()), gravityDirection);
        return Direction.getNearest(
            (int)Math.round(worldVector.x),
            (int)Math.round(worldVector.y),
            (int)Math.round(worldVector.z),
            Direction.DOWN
        );
    }
}
