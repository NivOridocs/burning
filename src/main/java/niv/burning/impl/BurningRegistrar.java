package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import niv.burning.api.BurningStorage;

@ApiStatus.Internal
public final class BurningRegistrar implements ServerStarting {

    private static final BlockApiProvider<BurningStorage, @Nullable Direction> ABSTRACT_FURNACE_PROVIDER = BurningRegistrar::getAbstractFurnaceProvider;

    BurningRegistrar() {
    }

    @Override
    public void onServerStarting(MinecraftServer server) {
        registerAbstractFurnaceBurningStorages(server.registryAccess());
        registerDynamicBurningStorages(server.registryAccess());
    }

    private void registerAbstractFurnaceBurningStorages(RegistryAccess registries) {
        var blocks = registries.registryOrThrow(Registries.BLOCK).stream()
                .filter(this::isAbsent)
                .filter(this::byEntity)
                .toArray(Block[]::new);
        BurningStorage.SIDED.registerForBlocks(ABSTRACT_FURNACE_PROVIDER, blocks);
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

    private static final BurningStorage getAbstractFurnaceProvider(Level world, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, Direction context) {
        if (blockEntity instanceof AbstractFurnaceBlockEntity entity) {
            return ((AbstractFurnaceBlockEntityExtension) entity).burning_getBurningStorage();
        } else {
            return null;
        }
    }
}
