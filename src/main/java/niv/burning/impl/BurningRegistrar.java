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
        var blocks = registries.registryOrThrow(Registries.BLOCK).stream()
                .filter(this::isAbsent)
                .filter(this::byEntity)
                .toArray(Block[]::new);
        if (blocks.length > 0) {
            BurningStorage.SIDED.registerForBlocks(
                    (world, pos, state, blockEntity, context) -> {
                        if (blockEntity instanceof AbstractFurnaceBlockEntity entity) {
                            return ((AbstractFurnaceBlockEntityExtension) entity).burning_getBurningStorage();
                        } else {
                            return null;
                        }
                    }, blocks);
        }
    }

    private void registerDynamicBurningStorages(RegistryAccess registries) {
        registries.registry(DynamicBurningStorageProvider.REGISTRY).stream()
                .flatMap(Registry::stream)
                .forEach(provider -> {
                    var blocks = ((BlockEntityTypeAccessor) provider.type).getBlocks().stream()
                            .filter(this::isAbsent)
                            .toArray(Block[]::new);
                    if (blocks.length > 0) {
                        BurningStorage.SIDED.registerForBlocks(provider, blocks);
                    }
                });
    }

    private boolean byEntity(Block block) {
        return block instanceof EntityBlock e
                && e.newBlockEntity(BlockPos.ZERO, block.defaultBlockState()) instanceof AbstractFurnaceBlockEntity;
    }

    private boolean isAbsent(Block block) {
        return BurningStorage.SIDED.getProvider(block) == null;
    }
}
