package com.moigferdsrte.gravitychanger.block;

import com.moigferdsrte.gravitychanger.api.GravityMovementEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

public class GravityCoreBlock extends Block {

    public static final BooleanProperty ENABLE = BooleanProperty.create("enabled");

    public GravityCoreBlock(Properties properties) {
        super(properties.mapColor(DyeColor.BLACK).strength(8.5F, 16.0F).requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(ENABLE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        builder.add(ENABLE);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (player.isCreative() && player.isShiftKeyDown()) {
            level.setBlockAndUpdate(pos, state.cycle(ENABLE));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, player.getSoundSource(), 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void stepOn(
            final @NonNull Level level,
            final @NonNull BlockPos pos,
            final @NonNull BlockState state,
            final @NonNull Entity entity
    ) {
        if (entity instanceof LivingEntity livingEntity && state.getValue(ENABLE)) {
            if (entity instanceof GravityMovementEntity movementEntity) {
                movementEntity.gravitychanger$rememberGravityCore(pos, entity.tickCount);
            }
            GravityCoreTransitionHandler.tryTransition(level, pos, livingEntity);
        }
    }
}
