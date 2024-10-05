package niv.burning.impl;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.MapMaker;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import niv.burning.api.BurningStorage;

@ApiStatus.Internal
public final class CachedBurningStorageProvider implements BlockApiProvider<BurningStorage, @Nullable Direction> {

    private static final record LevelPos(Level level, BlockPos pos) {
        @Override
        public String toString() {
            return DebugMessages.forGlobalPos(level, pos);
        }
    }

    private static final Map<LevelPos, BurningStorage> CACHE = new MapMaker()
            .concurrencyLevel(1).weakValues().makeMap();

    private final BlockApiProvider<BurningStorage, @Nullable Direction> target;

    CachedBurningStorageProvider(BlockApiProvider<BurningStorage, @Nullable Direction> target) {
        this.target = target;
    }

    @Override
    public @Nullable BurningStorage find(Level level, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, @Nullable Direction context) {
        return CACHE.computeIfAbsent(new LevelPos(level, pos), key -> target.find(level, pos, state, blockEntity, context));
    }
}
