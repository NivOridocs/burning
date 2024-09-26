package niv.burning.impl;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DynamicBurningStorageProviders {
    private DynamicBurningStorageProviders() {
    }

    private static final String LIT_TIME = "litTime";
    private static final String LIT_DURATION = "litDuration";

    public static final DynamicBurningStorageProvider newFurnaceInstance() {
        return newVanillaInstance(BlockEntityType.FURNACE);
    }

    public static final DynamicBurningStorageProvider newBlastFurnaceInstance() {
        return newVanillaInstance(BlockEntityType.BLAST_FURNACE);
    }

    public static final DynamicBurningStorageProvider newSmokerInstance() {
        return newVanillaInstance(BlockEntityType.SMOKER);
    }

    private static final DynamicBurningStorageProvider newVanillaInstance(
            BlockEntityType<? extends AbstractFurnaceBlockEntity> type) {
        return DynamicBurningStorageProvider.from(type, LIT_TIME, LIT_DURATION);
    }
}
