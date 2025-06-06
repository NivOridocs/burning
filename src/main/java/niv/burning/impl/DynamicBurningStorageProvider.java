package niv.burning.impl;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import niv.burning.api.BurningStorage;

@ApiStatus.Internal
public final class DynamicBurningStorageProvider implements BlockApiProvider<BurningStorage, @Nullable Direction> {

    public static final ResourceKey<Registry<DynamicBurningStorageProvider>> REGISTRY = ResourceKey
            .createRegistryKey(ResourceLocation.tryBuild(BurningImpl.MOD_ID, "dynamic_storage"));

    public static final Codec<DynamicBurningStorageProvider> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(src -> src.type),
                    Codec.STRING.fieldOf("lit_time").forGetter(src -> src.litTime.getName()),
                    Codec.STRING.fieldOf("lit_duration").forGetter(src -> src.litDuration.getName()))
            .apply(instance, DynamicBurningStorageProvider::from));

    private static final record LevelPos(Level level, BlockPos pos) {
        @Override
        public String toString() {
            return DebugMessages.forGlobalPos(level, pos);
        }
    }

    final BlockEntityType<?> type;

    final DynamicField litTime;

    final DynamicField litDuration;

    private final Map<LevelPos, BurningStorage> cache;

    private DynamicBurningStorageProvider(BlockEntityType<?> type, DynamicField litTime, DynamicField litDuration) {
        this.type = type;
        this.litTime = litTime;
        this.litDuration = litDuration;
        this.cache = new MapMaker().concurrencyLevel(1).weakValues().makeMap();
    }

    @Override
    public @Nullable BurningStorage find(Level level, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, @Nullable Direction context) {
        if (blockEntity != null && this.type.equals(blockEntity.getType())) {
            return this.cache.computeIfAbsent(new LevelPos(level, pos),
                    key -> new DynamicBurningStorage(this, blockEntity));
        } else {
            return null;
        }
    }

    static final DynamicBurningStorageProvider from(BlockEntityType<?> type,
            String litTime, String litDuration) {
        Class<?> clazz = ((BlockEntityTypeAccessor) type).getBlocks()
                .stream().findAny()
                .map(Block::defaultBlockState)
                .map(state -> type.create(BlockPos.ZERO, state).getClass())
                .orElse(null);
        if (clazz != null) {
            var litTimeField = Optional.ofNullable(FieldUtils
                    .getField(clazz, litTime, true))
                    .flatMap(DynamicField::of);

            var litDurationField = Optional.ofNullable(FieldUtils
                    .getField(clazz, litDuration, true))
                    .flatMap(DynamicField::of);

            if (litTimeField.isPresent() && litDurationField.isPresent()) {
                return new DynamicBurningStorageProvider(type, litTimeField.get(), litDurationField.get());
            }
        }
        return null;
    }
}
