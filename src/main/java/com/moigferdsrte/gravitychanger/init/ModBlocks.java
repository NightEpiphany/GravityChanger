package com.moigferdsrte.gravitychanger.init;

import com.moigferdsrte.gravitychanger.GravityChanger;
import com.moigferdsrte.gravitychanger.block.GravityCoreBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static com.moigferdsrte.gravitychanger.GravityChanger.MOD_ID;

public final class ModBlocks {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Block GRAVITY_CORE =
            register(BlockItemId.create(GravityChanger.id("gravity_core"), GravityChanger.id("gravity_core")),
                    GravityCoreBlock::new,
                    BlockBehaviour.Properties.of());


    private static Block register(final BlockItemId id, final Function<BlockBehaviour.Properties, Block> factory, final BlockBehaviour.Properties properties) {
        return register(id.block(), factory, properties);
    }
    public static Block register(final ResourceKey<Block> id, final Function<BlockBehaviour.Properties, Block> factory, final BlockBehaviour.Properties properties) {
        Block block = factory.apply(properties.setId(id));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void init() {
        LOGGER.info("Registering Blocks for " + MOD_ID);
    }
}
