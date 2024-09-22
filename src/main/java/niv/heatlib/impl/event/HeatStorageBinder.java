package niv.heatlib.impl.event;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import niv.heatlib.api.HeatStorage;
import niv.heatlib.api.event.HeatStorageLifecycleEvents;

@ApiStatus.Internal
public class HeatStorageBinder implements ServerStarting {

    private static final BlockApiProvider<HeatStorage, @Nullable Direction> DEFAULT_PROVIDER = HeatStorageBinder::defaultHeatStorage;

    @Override
    public void onServerStarting(MinecraftServer server) {
        var map = server.registryAccess().registryOrThrow(Registries.BLOCK).stream()
                .filter(this::byEntity)
                .collect(toMap(block -> block, block -> DEFAULT_PROVIDER, (a, b) -> a,
                        HashMap<Block, BlockApiProvider<HeatStorage, @Nullable Direction>>::new));
        HeatStorageLifecycleEvents.HEAT_STORAGE_BINDING.invoker().accept(ImmutableMap.copyOf(map));
        map.forEach((block, provider) -> HeatStorage.SIDED.registerForBlocks(provider, block));
    }

    private boolean byEntity(Block block) {
        if (block instanceof EntityBlock entityBlock) {
            var blockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            return blockEntity instanceof AbstractFurnaceBlockEntity;
        } else {
            return false;
        }
    }

    private static final HeatStorage defaultHeatStorage(Level level, BlockPos pos, BlockState state,
            BlockEntity blockEntity, Direction direction) {
        if (blockEntity instanceof AbstractFurnaceBlockEntity entity) {
            return BoundHeatStorage.of(level, pos, entity);
        } else {
            return null;
        }
    }
}
