package niv.heatlib.impl.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BoundHeatStorages {
    private BoundHeatStorages() {
    }

    public static BoundHeatStorage newFurnaceInstance() {
        return newInstance(BlockEntityType.FURNACE, Blocks.FURNACE);
    }

    public static BoundHeatStorage newBlastFurnaceInstance() {
        return newInstance(BlockEntityType.BLAST_FURNACE, Blocks.BLAST_FURNACE);
    }

    public static BoundHeatStorage newSmokerInstance() {
        return newInstance(BlockEntityType.SMOKER, Blocks.SMOKER);
    }

    private static <T extends AbstractFurnaceBlockEntity> BoundHeatStorage newInstance(BlockEntityType<T> type, Block block) {
        return new BoundHeatStorage(type.create(BlockPos.ZERO, block.defaultBlockState())) {
            @Override
            protected void onFinalCommit() {
                // no-op
            }
        };
    }
}
