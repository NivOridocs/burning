package niv.heatlib.api;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockEntityApiProvider;
import net.minecraft.core.Direction;

public interface HeatStorageProvider extends BlockEntityApiProvider<HeatStorage, @Nullable Direction> {
}
