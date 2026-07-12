package com.moigferdsrte.gravitychanger.item;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class GravityModificationItem extends ModifiableGravityItem{
    public GravityModificationItem(Modify modify, Properties properties) {
        super(modify, properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (GravityDirectionUtil.getGravityStrength(player) >= 15.0D && this.getModify() == Modify.INCREASE) {
            player.sendOverlayMessage(Component.translatable("tooltip.gravity_changer.gravity_intensity_maximal"));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CRAFTER_FAIL, player.getSoundSource(), 1.0F, 0.5F);
            return InteractionResult.CONSUME;
        } else if (GravityDirectionUtil.getGravityStrength(player) <= 5.0D && this.getModify() == Modify.DECREASE) {
            player.sendOverlayMessage(Component.translatable("tooltip.gravity_changer.gravity_intensity_minimal"));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CRAFTER_FAIL, player.getSoundSource(), 1.0F, 0.5F);
            return InteractionResult.CONSUME;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, player.getSoundSource(), 1.0F, 1.0F);
        double strength = GravityDirectionUtil.getGravityStrength(player);
        double modifiedStrength = this.getModify() == Modify.INCREASE
            ? strength + MODIFICATION_STEP
            : strength - MODIFICATION_STEP;
        modifiedStrength = Math.round(modifiedStrength * 10.0D) / 10.0D;
        GravityDirectionUtil.setGravityStrength(player, modifiedStrength);
        player.sendOverlayMessage(Component.translatable("tooltip.gravity_changer.gravity_intensity_modified", modifiedStrength));
        return InteractionResult.SUCCESS;
    }
}
