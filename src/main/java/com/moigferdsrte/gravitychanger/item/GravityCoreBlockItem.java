package com.moigferdsrte.gravitychanger.item;

import com.moigferdsrte.gravitychanger.init.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class GravityCoreBlockItem extends BlockItem {
    public GravityCoreBlockItem(Properties properties) {
        super(ModBlocks.GRAVITY_CORE, properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(@NonNull ItemStack itemStack, @NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag) {
        if (Minecraft.getInstance().hasShiftDown()) {
            builder.accept(Component.translatable("tooltip.gravity_changer.gravity_core").setStyle(
                    Style.EMPTY.withBold(true).withColor(TextColor.LIGHT_PURPLE).applyFormat(ChatFormatting.UNDERLINE)
            ));
        }
        else builder.accept(Component.translatable("tooltip.gravity_changer.shift"));
    }
}
