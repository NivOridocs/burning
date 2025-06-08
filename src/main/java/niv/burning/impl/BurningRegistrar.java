package niv.burning.impl;

import static niv.burning.impl.BurningImpl.LOGGER;
import static niv.burning.impl.BurningImpl.MOD_NAME;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
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
        var eligibleBlocks = registries.registryOrThrow(Registries.BLOCK).stream()
                .filter(this::isNotBlacklisted)
                .filter(this::isAbsent)
                .filter(this::byEntity)
                .toArray(Block[]::new);
        if (eligibleBlocks.length > 0) {
            BurningStorage.SIDED.registerForBlocks(
                    (world, pos, state, blockEntity, context) -> {
                        if (blockEntity instanceof AbstractFurnaceBlockEntity entity) {
                            return entity.burning_getBurningStorage();
                        }
                        return null;
                    }, eligibleBlocks);
        }
    }

    private void registerDynamicBurningStorages(RegistryAccess registries) {
        registries.registry(DynamicBurningStorageProvider.REGISTRY).stream()
                .flatMap(Registry::stream)
                .forEach(provider -> {
                    var providerBlocks = ((BlockEntityTypeAccessor) provider.type).getBlocks().stream()
                            .filter(this::isAbsent)
                            .toArray(Block[]::new);
                    if (providerBlocks.length > 0) {
                        BurningStorage.SIDED.registerForBlocks(provider, providerBlocks);
                    }
                });
    }

    private boolean byEntity(Block block) {
        try {
            if (block instanceof EntityBlock entityBlock) {
                return entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState()) instanceof AbstractFurnaceBlockEntity;
            }
            return false;
        } catch (RuntimeException rex) {
            LOGGER.warn(
                    "[{}] Cannot create an entity from {} due to a runtime exception, skipped. Exception message: {}",
                    MOD_NAME, block, rex.getMessage());
            return false;
        }
    }

    private boolean isAbsent(Block block) {
        return BurningStorage.SIDED.getProvider(block) == null;
    }

    private boolean isNotBlacklisted(Block block) {
        return !BuiltInRegistries.BLOCK.wrapAsHolder(block).is(BurningTags.BLACKLIST);
    }
}
