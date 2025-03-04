package niv.burning.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class DynamicBurningStorages {
    private DynamicBurningStorages() {
    }

    public static final DynamicBurningStorage createFurnace() {
        return create(DynamicBurningStorageProviders.createFurnace(), Blocks.FURNACE);
    }

    public static final DynamicBurningStorage createBlastFurnace() {
        return create(DynamicBurningStorageProviders.createBlastFurnace(), Blocks.BLAST_FURNACE);
    }

    public static final DynamicBurningStorage createSmoker() {
        return create(DynamicBurningStorageProviders.createSmoker(), Blocks.SMOKER);
    }

    public static final DynamicBurningStorage create(DynamicBurningStorageProvider provider, Block block) {
        return new DynamicBurningStorage(provider, provider.type.create(BlockPos.ZERO, block.defaultBlockState())) {
            @Override
            protected void onFinalCommit() {
                // no-op
            }
        };
    }
}
