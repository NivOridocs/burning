package niv.burning.impl;

import java.util.HashMap;
import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.BurningStorage;
import niv.burning.api.event.BurningStorageLifecycleEvents;
import niv.heatlib.impl.DynamicHeatStorageProvider;

@ApiStatus.Internal
public final class BurningRegistrar implements ServerStarting {

    private static interface PutIfAbsent extends
            BiFunction<Block, BlockApiProvider<BurningStorage, @Nullable Direction>, BlockApiProvider<BurningStorage, @Nullable Direction>> {
    }

    BurningRegistrar() {
    }

    @Override
    public void onServerStarting(MinecraftServer server) {
        var map = new HashMap<Block, BlockApiProvider<BurningStorage, @Nullable Direction>>();
        addAbstractFurnaceBurningStorages(server.registryAccess(), map::putIfAbsent);
        addDynamicHeatStorages(server.registryAccess(), map::putIfAbsent);
        BurningStorageLifecycleEvents.BURNING_STORAGE_REGISTERING.invoker().accept(server, ImmutableMap.copyOf(map));
        map.forEach((block, provider) -> BurningStorage.SIDED.registerForBlocks(provider, block));
    }

    private void addAbstractFurnaceBurningStorages(RegistryAccess registries, PutIfAbsent function) {
        registries.registryOrThrow(Registries.BLOCK).stream()
                .filter(this::isAbsent)
                .filter(this::byEntity)
                .forEach(block -> function.apply(block, AbstractFurnaceBurningStorage::find));
    }

    private boolean byEntity(Block block) {
        if (block instanceof EntityBlock entityBlock) {
            var blockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            return blockEntity instanceof AbstractFurnaceBlockEntity;
        } else {
            return false;
        }
    }

    private void addDynamicHeatStorages(RegistryAccess registries, PutIfAbsent function) {
        registries.registryOrThrow(DynamicHeatStorageProvider.REGISTRY).stream()
                .forEach(provider -> this.addDynamicHeatStorage(provider, function));
    }

    private void addDynamicHeatStorage(DynamicHeatStorageProvider provider, PutIfAbsent function) {
        ((BlockEntityTypeAccessor) provider.type).getBlocks().stream()
                .filter(this::isAbsent)
                .forEach(block -> function.apply(block, provider));
    }

    private boolean isAbsent(Block block) {
        return BurningStorage.SIDED.getProvider(block) == null;
    }
}
