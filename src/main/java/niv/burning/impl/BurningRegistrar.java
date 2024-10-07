package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.BurningStorage;

@ApiStatus.Internal
public final class BurningRegistrar implements ServerStarting {

    BurningRegistrar() {
    }

    @Override
    public void onServerStarting(MinecraftServer server) {
        registerAbstractFurnaceBurningStorages(server.registryAccess());
        registerDynamicBurningStorages(server.registryAccess());
    }

    private void registerAbstractFurnaceBurningStorages(RegistryAccess registries) {
        var provider = new AbstractFurnaceBurningStorageProvider();
        registries.registryOrThrow(Registries.BLOCK).stream()
                .filter(this::isAbsent)
                .filter(this::byEntity)
                .forEach(block -> BurningStorage.SIDED.registerForBlocks(
                        new CachedBurningStorageProvider(provider), block));
    }

    private boolean byEntity(Block block) {
        return block instanceof EntityBlock e
                && e.newBlockEntity(BlockPos.ZERO, block.defaultBlockState()) instanceof AbstractFurnaceBlockEntity;
    }

    private void registerDynamicBurningStorages(RegistryAccess registries) {
        registries.registry(DynamicBurningStorageProvider.REGISTRY).stream()
                .flatMap(Registry::stream)
                .forEach(provider -> ((BlockEntityTypeAccessor) provider.type).getBlocks().stream()
                        .filter(this::isAbsent)
                        .forEach(block -> BurningStorage.SIDED.registerForBlocks(
                                new CachedBurningStorageProvider(provider), block)));
    }

    private boolean isAbsent(Block block) {
        return BurningStorage.SIDED.getProvider(block) == null;
    }
}
