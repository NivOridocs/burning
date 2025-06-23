package niv.burning.impl;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DynamicBurningStorageProviders {
    private DynamicBurningStorageProviders() {
    }

    private static final String LIT_TIME = "litTimeRemaining";
    private static final String LIT_DURATION = "litTotalTime";

    public static final DynamicBurningStorageProvider createFurnace() {
        return create(BlockEntityType.FURNACE);
    }

    public static final DynamicBurningStorageProvider createBlastFurnace() {
        return create(BlockEntityType.BLAST_FURNACE);
    }

    public static final DynamicBurningStorageProvider createSmoker() {
        return create(BlockEntityType.SMOKER);
    }

    private static final DynamicBurningStorageProvider create(
            BlockEntityType<? extends AbstractFurnaceBlockEntity> type) {
        return DynamicBurningStorageProvider.from(type, LIT_TIME, LIT_DURATION);
    }
}
