package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import niv.burning.api.BurningStorage;

@ApiStatus.Internal
public final class AbstractFurnaceBurningStorageProvider implements BlockApiProvider<BurningStorage, @Nullable Direction> {

    AbstractFurnaceBurningStorageProvider() {
    }

    @Override
    public @Nullable BurningStorage find(Level level, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, @Nullable Direction context) {
        if (blockEntity instanceof AbstractFurnaceBlockEntity entity) {
            return new AbstractFurnaceBurningStorage(entity);
        } else {
            return null;
        }
    }
}
