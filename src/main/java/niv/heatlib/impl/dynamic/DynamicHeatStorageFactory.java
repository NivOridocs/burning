package niv.heatlib.impl.dynamic;

import java.lang.reflect.Field;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import niv.heatlib.impl.HeatLib;

public final class DynamicHeatStorageFactory<T extends BlockEntity> {

    public static final ResourceKey<Registry<DynamicHeatStorageFactory<?>>> REGISTRY = ResourceKey
            .createRegistryKey(ResourceLocation.tryBuild(HeatLib.MOD_ID, "heat_storage"));

    public static final Codec<DynamicHeatStorageFactory<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(src -> src.type),
            Codec.STRING.fieldOf("lit_time").forGetter(src -> src.litTime.getName()),
            Codec.STRING.fieldOf("lit_duration").forGetter(src -> src.litDuration.getName()))
            .apply(instance, DynamicHeatStorageFactory::from));

    final BlockEntityType<T> type;

    final Field litTime;

    final Field litDuration;

    private DynamicHeatStorageFactory(BlockEntityType<T> type, Field litTime, Field litDuration) {
        this.type = type;
        this.litTime = litTime;
        this.litDuration = litDuration;
    }

    private static final <T extends BlockEntity>  DynamicHeatStorageFactory<T> from(BlockEntityType<T> type, String litTime, String litDuration) {
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
                return new DynamicHeatStorageFactory<>(type, litTimeField.get(), litDurationField.get());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> Optional<DynamicHeatStorage<T>> of(LevelReader level, T entity) {
        return level.registryAccess().registry(REGISTRY).stream().flatMap(Registry::stream)
                .filter(factory -> factory.type == entity.getType()).findFirst()
                .map(factory -> (DynamicHeatStorageFactory<T>) factory)
                .map(factory -> new DynamicHeatStorage<T>(factory, entity));
    }
}
