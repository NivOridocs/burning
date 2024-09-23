package niv.heatlib.impl;

import java.lang.reflect.Field;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
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
import niv.heatlib.api.HeatStorage;

public final class DynamicHeatStorageProvider implements BlockApiProvider<HeatStorage, @Nullable Direction> {

    public static final ResourceKey<Registry<DynamicHeatStorageProvider>> REGISTRY = ResourceKey
            .createRegistryKey(ResourceLocation.tryBuild(HeatLibImpl.MOD_ID, "heat_storage"));

    public static final Codec<DynamicHeatStorageProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(src -> src.type),
            Codec.STRING.fieldOf("lit_time").forGetter(src -> src.litTime.getName()),
            Codec.STRING.fieldOf("lit_duration").forGetter(src -> src.litDuration.getName()))
            .apply(instance, DynamicHeatStorageProvider::from));

    final BlockEntityType<?> type;

    final Field litTime;

    final Field litDuration;

    private DynamicHeatStorageProvider(BlockEntityType<?> type, Field litTime, Field litDuration) {
        this.type = type;
        this.litTime = litTime;
        this.litDuration = litDuration;
    }

    @Override
    public @Nullable HeatStorage find(
            Level level, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, @Nullable Direction context) {
        if (blockEntity != null && this.type == blockEntity.getType()) {
            return DynamicHeatStorage.of(level, pos, this, blockEntity);
        } else {
            return null;
        }
    }

    private static final DynamicHeatStorageProvider from(BlockEntityType<?> type, String litTime, String litDuration) {
        Class<?> clazz = ((BlockEntityTypeAccessor) type).getBlocks()
                .stream().findAny()
                .map(Block::defaultBlockState)
                .map(state -> type.create(BlockPos.ZERO, state).getClass())
                .orElse(null);
        if (clazz != null) {
            var litTimeField = Optional.ofNullable(FieldUtils
                    .getField(clazz, litTime, true));

            var litDurationField = Optional.ofNullable(FieldUtils
                    .getField(clazz, litDuration, true));

            if (litTimeField.isPresent() && litDurationField.isPresent()) {
                return new DynamicHeatStorageProvider(type, litTimeField.get(), litDurationField.get());
            }
        }
        return null;
    }
}
