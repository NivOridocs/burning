package niv.burning.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import niv.burning.api.base.AbstractFurnaceBurningStorage;

public class AbstractFurnaceBurningStorages {
    private AbstractFurnaceBurningStorages() {
    }

    public static AbstractFurnaceBurningStorage createFurnace() {
        return create(BlockEntityType.FURNACE, Blocks.FURNACE);
    }

    public static AbstractFurnaceBurningStorage createBlastFurnace() {
        return create(BlockEntityType.BLAST_FURNACE, Blocks.BLAST_FURNACE);
    }

    public static AbstractFurnaceBurningStorage createSmoker() {
        return create(BlockEntityType.SMOKER, Blocks.SMOKER);
    }

    private static <T extends AbstractFurnaceBlockEntity> AbstractFurnaceBurningStorage create(
            BlockEntityType<T> type, Block block) {
        return new AbstractFurnaceBurningStorage(type.create(BlockPos.ZERO, block.defaultBlockState())) {
            @Override
            protected void onFinalCommit() {
                // no-op
            }
        };
    }
}
