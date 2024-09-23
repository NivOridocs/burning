package niv.heatlib.api;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import niv.heatlib.impl.HeatLibImpl;

public interface HeatStorage {

    BlockApiLookup<HeatStorage, @Nullable Direction> SIDED = BlockApiLookup.get(
            ResourceLocation.tryBuild(HeatLibImpl.MOD_ID, "heat_storage"),
            HeatStorage.class, Direction.class);

    default boolean supportsInsertion() {
        return true;
    }

    Heat insert(Heat heat, TransactionContext transaction);

    default boolean supportsExtraction() {
        return true;
    }

    Heat extract(Heat heat, TransactionContext transaction);

    Heat getCurrentHeat();
}
