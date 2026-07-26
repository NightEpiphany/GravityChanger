package com.moigferdsrte.gravitychanger.item;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class GravityModificationItem extends ModifiableGravityItem {
    public GravityModificationItem(Modify modify, Properties properties) {
        super(modify, properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (!player.isCreative()) {
            player.sendOverlayMessage(Component.literal("X").withColor(TextColor.RED));
            return InteractionResult.FAIL;
        }
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

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(@NonNull ItemStack itemStack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag) {
        if (Minecraft.getInstance().hasShiftDown())
            builder.accept(Component.translatable("tooltip.gravity_changer.gravity_modification").setStyle(
                    Style.EMPTY.withBold(true).withColor(TextColor.RED).applyFormat(ChatFormatting.UNDERLINE)
            ));
        else builder.accept(Component.translatable("tooltip.gravity_changer.shift"));
    }
}
