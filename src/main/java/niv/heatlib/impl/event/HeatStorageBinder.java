package niv.heatlib.impl.event;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import niv.heatlib.api.HeatStorage;
import niv.heatlib.api.HeatStorageProvider;
import niv.heatlib.api.event.HeatStorageLifecycleEvents;

@ApiStatus.Internal
public class HeatStorageBinder implements ServerStarting {

    private static final HeatStorageProvider DEFAULT_PROVIDER = HeatStorageBinder::defaultHeatStorage;

    @Override
    public void onServerStarting(MinecraftServer server) {
        var entities = server.registryAccess().registryOrThrow(Registries.BLOCK_ENTITY_TYPE);
        var result = entities.stream()
                .filter(this::byEntity)
                .collect(toMap(type -> type, type -> DEFAULT_PROVIDER, (a, b) -> a,
                        HashMap<BlockEntityType<?>, HeatStorageProvider>::new));
        HeatStorageLifecycleEvents.HEAT_STORAGE_BINDING.invoker().accept(result);
        result.forEach((type, provider) -> HeatStorage.SIDED.registerForBlockEntities(provider, type));
    }

    private boolean byEntity(BlockEntityType<?> type) {
        return ((BlockEntityTypeAccessor) type).getBlocks().stream().findFirst()
                .map(block -> type.create(BlockPos.ZERO, block.defaultBlockState()))
                .filter(AbstractFurnaceBlockEntity.class::isInstance)
                .isPresent();
    }

    private static final HeatStorage defaultHeatStorage(BlockEntity entity, @Nullable Direction direction) {
        if (entity instanceof AbstractFurnaceBlockEntity target) {
            return new BoundHeatStorage(target);
        } else {
            throw new IllegalArgumentException("'entity' isn't an AbstractFurnaceBlockEntity");
        }
    }
}
