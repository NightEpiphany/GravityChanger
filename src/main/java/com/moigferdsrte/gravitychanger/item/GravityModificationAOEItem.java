package com.moigferdsrte.gravitychanger.item;

import com.moigferdsrte.gravitychanger.init.ModEntityTags;
import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class GravityModificationAOEItem extends ModifiableGravityItem{
    private static final double RADIUS = 5.0;
    private static final double RADIUS_SQUARED = RADIUS * RADIUS;
    private static final int RANGE_PARTICLE_POINTS_PER_RING = 28;
    private static final DustParticleOptions RANGE_PARTICLE = new DustParticleOptions(0x86E8FF, 0.75F);

    public GravityModificationAOEItem(Modify modify, Properties properties) {
        super(modify, properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public @NonNull InteractionResult use(final @NonNull Level level, final @NonNull Player player, final @NonNull InteractionHand hand) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, player.getSoundSource(), 1.0F, 1.0F);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel)level;
        spawnRangeParticles(serverLevel, player);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS), target -> this.canAffect(player, target))) {
            double strength = GravityDirectionUtil.getGravityStrength(target);
            double modifiedStrength = this.getModify() == Modify.INCREASE
                ? Math.min(strength + MODIFICATION_STEP, GravityDirectionUtil.MAX_GRAVITY_STRENGTH)
                : Math.max(strength - MODIFICATION_STEP, GravityDirectionUtil.MIN_GRAVITY_STRENGTH);
            GravityDirectionUtil.setGravityStrength(target, modifiedStrength);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    private boolean canAffect(final Player player, final LivingEntity target) {
        return target.isAlive()
            && !target.is(player)
            && target.distanceToSqr(player) <= RADIUS_SQUARED
            && !target.getType().builtInRegistryHolder().is(ModEntityTags.GRAVITY_FIXED);
    }

    private static void spawnRangeParticles(final ServerLevel level, final Player player) {
        double centerX = player.getX();
        double centerY = player.getY(0.5);
        double centerZ = player.getZ();

        for (int i = 0; i < RANGE_PARTICLE_POINTS_PER_RING; i++) {
            double angle = Math.PI * 2.0 * i / RANGE_PARTICLE_POINTS_PER_RING;
            double sin = Math.sin(angle) * RADIUS;
            double cos = Math.cos(angle) * RADIUS;

            sendRangeParticle(level, centerX + cos, centerY, centerZ + sin);
            sendRangeParticle(level, centerX + cos, centerY + sin, centerZ);
            sendRangeParticle(level, centerX, centerY + cos, centerZ + sin);
        }
    }

    private static void sendRangeParticle(final ServerLevel level, final double x, final double y, final double z) {
        level.sendParticles(RANGE_PARTICLE, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
    }
}
