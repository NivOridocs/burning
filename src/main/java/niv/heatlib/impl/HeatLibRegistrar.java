package niv.heatlib.impl;

import java.util.HashMap;
import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.heatlib.api.HeatStorage;
import niv.heatlib.api.event.HeatStorageLifecycleEvents;

@ApiStatus.Internal
public final class HeatLibRegistrar implements ServerStarting {

    HeatLibRegistrar() {
    }

    @Override
    public void onServerStarting(MinecraftServer server) {
        var map = new HashMap<Block, BlockApiProvider<HeatStorage, @Nullable Direction>>();
        addAbstractFurnaceHeatStorages(server.registryAccess(), map::putIfAbsent);
        HeatStorageLifecycleEvents.HEAT_STORAGE_REGISTERING.invoker().accept(server, ImmutableMap.copyOf(map));
        map.forEach((block, provider) -> HeatStorage.SIDED.registerForBlocks(provider, block));
    }

    private void addAbstractFurnaceHeatStorages(RegistryAccess registries,
            BiFunction<Block, BlockApiProvider<HeatStorage, @Nullable Direction>, BlockApiProvider<HeatStorage, @Nullable Direction>> function) {
        registries.registryOrThrow(Registries.BLOCK).stream()
                .filter(this::isAbsent)
                .filter(this::byEntity)
                .forEach(block -> function.apply(block, AbstractFurnaceHeatStorage::find));
    }

    private boolean byEntity(Block block) {
        if (block instanceof EntityBlock entityBlock) {
            var blockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            return blockEntity instanceof AbstractFurnaceBlockEntity;
        } else {
            return false;
        }
    }

    private boolean isAbsent(Block block) {
        return HeatStorage.SIDED.getProvider(block) == null;
    }
}
