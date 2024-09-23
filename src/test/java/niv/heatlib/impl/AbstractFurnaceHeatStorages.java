package niv.heatlib.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AbstractFurnaceHeatStorages {
    private AbstractFurnaceHeatStorages() {
    }

    public static AbstractFurnaceHeatStorage newFurnaceInstance() {
        return newInstance(BlockEntityType.FURNACE, Blocks.FURNACE);
    }

    public static AbstractFurnaceHeatStorage newBlastFurnaceInstance() {
        return newInstance(BlockEntityType.BLAST_FURNACE, Blocks.BLAST_FURNACE);
    }

    public static AbstractFurnaceHeatStorage newSmokerInstance() {
        return newInstance(BlockEntityType.SMOKER, Blocks.SMOKER);
    }

    private static <T extends AbstractFurnaceBlockEntity> AbstractFurnaceHeatStorage newInstance(BlockEntityType<T> type, Block block) {
        return new AbstractFurnaceHeatStorage(type.create(BlockPos.ZERO, block.defaultBlockState())) {
            @Override
            protected void onFinalCommit() {
                // no-op
            }
        };
    }
}
