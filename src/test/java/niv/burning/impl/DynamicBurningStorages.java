package niv.burning.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class DynamicBurningStorages {
    private DynamicBurningStorages() {
    }

    public static final DynamicBurningStorage newInstance(DynamicBurningStorageProvider provider, Block block) {
        return new DynamicBurningStorage(provider, provider.type.create(BlockPos.ZERO, block.defaultBlockState())) {
            @Override
            protected void onFinalCommit() {
                // no-op
            }
        };
    }
}
