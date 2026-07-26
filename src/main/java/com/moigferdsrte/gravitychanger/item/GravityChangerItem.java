package com.moigferdsrte.gravitychanger.item;

import com.moigferdsrte.gravitychanger.util.GravityDirectionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
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

public class GravityChangerItem extends DirectionalGravityItem {
    public GravityChangerItem(Direction direction, Properties properties) {
        super(direction, properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public @NonNull InteractionResult use(final @NonNull Level level, final @NonNull Player player, final @NonNull InteractionHand hand) {
        if (!player.isCreative()) {
            player.sendOverlayMessage(Component.literal("X").withColor(TextColor.RED));
            return InteractionResult.FAIL;
        }
        if (GravityDirectionUtil.getOwnGravityDirection(player).equals(this.getDirection())) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CRAFTER_FAIL, player.getSoundSource(), 1.0F, 0.5F);
            return InteractionResult.CONSUME;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, player.getSoundSource(), 1.0F, 1.0F);
        if (!level.isClientSide()) {
            return GravityDirectionUtil.setGravityDirectionWithDownLift(player, this.getDirection()) ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
        }

        return GravityDirectionUtil.getGravityDirection(player) == this.getDirection() ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(@NonNull ItemStack itemStack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag) {
        if (Minecraft.getInstance().hasShiftDown())
            builder.accept(Component.translatable("tooltip.gravity_changer.gravity_changer").setStyle(
                    Style.EMPTY.withBold(true).withColor(TextColor.RED).applyFormat(ChatFormatting.UNDERLINE)
            ));
        else builder.accept(Component.translatable("tooltip.gravity_changer.shift"));
    }
}
