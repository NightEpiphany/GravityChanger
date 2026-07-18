package com.moigferdsrte.gravitychanger.mixin;

import com.moigferdsrte.gravitychanger.config.GravityChangerConfig;
import com.moigferdsrte.gravitychanger.config.GravityChangerConfigManager;
import com.moigferdsrte.gravitychanger.init.ModAttributes;
import com.moigferdsrte.gravitychanger.util.DirectionalFallTracker;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import com.moigferdsrte.gravitychanger.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Unique
    private static final double GRAVITYCHANGER_MINIMUM_FALL_SPEED = 1.0E-5;

    @Unique
    private static final float GRAVITYCHANGER_VOID_DAMAGE = 4.0F;

    @Unique
    private final DirectionalFallTracker gravitychanger$directionalFallTracker = new DirectionalFallTracker();

    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }
    @Shadow
    @Final
    public WalkAnimationState walkAnimation;

    @Shadow
    protected abstract double getEffectiveGravity();

    @Shadow
    protected abstract float getJumpPower();

    @Shadow
    private Vec3 handleRelativeFrictionAndCalculateMovement(final Vec3 input, final float friction) {
        throw new AssertionError();
    }

    @Shadow
    private static float computeModifiedFriction(final float friction, final float modifier) {
        throw new AssertionError();
    }

    @Shadow
    public abstract double getAttributeValue(final Holder<Attribute> attribute);

    @Shadow
    public abstract boolean shouldDiscardFriction();

    @Shadow
    public abstract @Nullable MobEffectInstance getEffect(final Holder<MobEffect> effect);

    @Shadow
    protected abstract void updateWalkAnimation(final float distance);

    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void gravitychanger$addGravityDirectionAttribute(final CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue()
            .add(ModAttributes.GRAVITY_DIRECTION)
            .add(ModAttributes.GRAVITY_STRENGTH);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void gravitychanger$limitDirectionalFallTime(final CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        GravityChangerConfig config = GravityChangerConfigManager.get();
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        boolean creativePlayer = entity instanceof Player player && player.isCreative();
        boolean falling = config.directionalFallLimitEnabled()
            && entity.level() instanceof ServerLevel
            && gravityDirection != Direction.DOWN
            && !creativePlayer
            && !entity.onGround()
            && !entity.isPassenger()
            && !entity.isNoGravity()
            && RotationUtil.vecWorldToPlayer(entity.getDeltaMovement(), gravityDirection).y < -GRAVITYCHANGER_MINIMUM_FALL_SPEED;
        int maximumTicks = config.directionalFallLimitSeconds() * 20;

        if (this.gravitychanger$directionalFallTracker.tick(gravityDirection, falling, maximumTicks)
            && entity.level() instanceof ServerLevel serverLevel) {
            entity.hurtServer(serverLevel, entity.damageSources().fellOutOfWorld(), GRAVITYCHANGER_VOID_DAMAGE);
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$jumpFromDirectionalGround(final CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        ci.cancel();
        float jumpPower = this.gravitychanger$getScaledJumpPower(entity);
        if (!(jumpPower <= 1.0E-5F)) {
            Vec3 localMovement = RotationUtil.vecWorldToPlayer(entity.getDeltaMovement(), gravityDirection);
            entity.setDeltaMovement(RotationUtil.vecPlayerToWorld(localMovement.x, Math.max(jumpPower, localMovement.y), localMovement.z, gravityDirection));
            if (entity.isSprinting()) {
                float angle = entity.getYRot() * (float)(Math.PI / 180.0);
                Vec3 localBoost = new Vec3(-Mth.sin(angle) * 0.2, 0.0, Mth.cos(angle) * 0.2);
                entity.addDeltaMovement(RotationUtil.vecPlayerToWorld(localBoost, gravityDirection));
            }

            this.needsSync = true;
        }
    }

    @Inject(method = "travelInAir", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$travelInDirectionalAir(final Vec3 input, final CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        ci.cancel();
        BlockPos posBelow = entity.getBlockPosBelowThatAffectsMyMovement();
        float blockFriction = entity.onGround()
            ? computeModifiedFriction(
                entity.level().getBlockState(posBelow).getBlock().getFriction(),
                (float)this.getAttributeValue(Attributes.FRICTION_MODIFIER)
            )
            : 1.0F;
        Vec3 movement = this.handleRelativeFrictionAndCalculateMovement(input, blockFriction);
        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        double localY = localMovement.y;
        MobEffectInstance levitationEffect = this.getEffect(MobEffects.LEVITATION);
        if (levitationEffect != null) {
            localY += (0.05 * (levitationEffect.getAmplifier() + 1) - localMovement.y) * 0.2;
        } else if (!entity.level().isClientSide() || entity.level().hasChunkAt(posBelow)) {
            double effectiveGravity = GravityDirectionUtil.getEffectiveGravity(
                entity.getGravity(),
                localMovement.y,
                entity.hasEffect(MobEffects.SLOW_FALLING)
            );
            localY -= GravityDirectionUtil.scaleGravity(entity, effectiveGravity);
        } else if (entity.getY() > entity.level().getMinY()) {
            localY = -0.1;
        } else {
            localY = 0.0;
        }

        Vec3 nextLocalMovement;
        if (this.shouldDiscardFriction()) {
            nextLocalMovement = new Vec3(localMovement.x, localY, localMovement.z);
        } else {
            float entityAirDragModifier = (float)this.getAttributeValue(Attributes.AIR_DRAG_MODIFIER);
            float airDrag = computeModifiedFriction(0.91F, entityAirDragModifier);
            float horizontalFriction = blockFriction * airDrag;
            float verticalFriction = this.omnidirectionalAirMover() ? airDrag : computeModifiedFriction(0.98F, entityAirDragModifier);
            nextLocalMovement = new Vec3(localMovement.x * horizontalFriction, localY * verticalFriction, localMovement.z * horizontalFriction);
        }

        entity.setDeltaMovement(RotationUtil.vecPlayerToWorld(nextLocalMovement, gravityDirection));
    }

    @Inject(method = "calculateEntityAnimation", at = @At("HEAD"), cancellable = true)
    private void gravitychanger$calculateDirectionalEntityAnimation(final boolean useY, final CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        ci.cancel();
        Vec3 worldDelta = new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
        Vec3 localDelta = RotationUtil.vecWorldToPlayer(worldDelta, gravityDirection);
        float distance = (float)Mth.length(localDelta.x, useY ? localDelta.y : 0.0, localDelta.z);
        if (!entity.isPassenger() && entity.isAlive()) {
            this.updateWalkAnimation(distance);
        } else {
            this.walkAnimation.stop();
        }
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
            ordinal = 0
        )
    )
    private double gravitychanger$getLocalWalkDeltaXForBodyRotation(final LivingEntity entity) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getX();
        }

        Vec3 worldDelta = this.position().subtract(this.xo, this.yo, this.zo);
        return this.xo + RotationUtil.vecWorldToPlayer(worldDelta, gravityDirection).x;
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
            ordinal = 0
        )
    )
    private double gravitychanger$getLocalWalkDeltaZForBodyRotation(final LivingEntity entity) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return entity.getZ();
        }

        Vec3 worldDelta = this.position().subtract(this.xo, this.yo, this.zo);
        return this.zo + RotationUtil.vecWorldToPlayer(worldDelta, gravityDirection).z;
    }

    @Redirect(
        method = "jumpFromGround",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V"
        )
    )
    private void gravitychanger$jumpInGravityDirection(final LivingEntity entity, final double x, final double y, final double z) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            entity.setDeltaMovement(x, y * GravityDirectionUtil.getJumpVelocityScale(entity), z);
            return;
        }

        Vec3 localMovement = RotationUtil.vecWorldToPlayer(entity.getDeltaMovement(), gravityDirection);
        double localY = y * GravityDirectionUtil.getJumpVelocityScale(entity);
        Vec3 worldMovement = RotationUtil.vecPlayerToWorld(localMovement.x, Math.max(localY, localMovement.y), localMovement.z, gravityDirection);
        entity.setDeltaMovement(worldMovement);
    }

    @Redirect(
        method = "jumpFromGround",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void gravitychanger$rotateSprintingJumpBoost(final LivingEntity entity, final Vec3 momentum) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(entity);
        entity.addDeltaMovement(gravityDirection == Direction.DOWN ? momentum : RotationUtil.vecPlayerToWorld(momentum, gravityDirection));
    }

    @Redirect(
        method = "goDownInWater",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$goDownInLocalWater(final Vec3 movement, final double x, final double y, final double z) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this);
        return gravityDirection == Direction.DOWN ? movement.add(x, y, z) : movement.add(RotationUtil.vecPlayerToWorld(x, y, z, gravityDirection));
    }

    @Redirect(
        method = "jumpInLiquid",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$jumpInLocalLiquid(final Vec3 movement, final double x, final double y, final double z) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this);
        return gravityDirection == Direction.DOWN ? movement.add(x, y, z) : movement.add(RotationUtil.vecPlayerToWorld(x, y, z, gravityDirection));
    }

    @Redirect(
        method = "wouldNotSuffocateAtTargetPose",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityDimensions;makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"
        )
    )
    private AABB gravitychanger$makeDirectionalPoseBox(final EntityDimensions dimensions, final Vec3 pos) {
        Direction gravityDirection = GravityDirectionUtil.getGravityDirection(this);
        return RotationUtil.makeBoxFromDimensions(dimensions, gravityDirection, pos);
    }

    @Redirect(
        method = "travelInAir",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getEffectiveGravity()D"
        )
    )
    private double gravitychanger$skipVanillaYGravity(final LivingEntity entity) {
        return GravityDirectionUtil.getGravityDirection(entity) == Direction.DOWN
            ? GravityDirectionUtil.scaleGravity(entity, this.getEffectiveGravity())
            : 0.0;
    }

    @Unique
    private float gravitychanger$getScaledJumpPower(final LivingEntity entity) {
        return (float)(this.getJumpPower() * GravityDirectionUtil.getJumpVelocityScale(entity));
    }
}
