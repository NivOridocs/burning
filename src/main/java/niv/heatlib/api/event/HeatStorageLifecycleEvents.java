package niv.heatlib.api.event;

import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import niv.heatlib.api.HeatStorage;

public final class HeatStorageLifecycleEvents {
    private HeatStorageLifecycleEvents() {
    }

    public static final Event<HeatStorageBinding> HEAT_STORAGE_BINDING = EventFactory.createArrayBacked(
            HeatStorageBinding.class,
            listeners -> map -> {
                for (var listener : listeners) {
                    listener.accept(map);
                }
            });

    @FunctionalInterface
    public interface HeatStorageBinding
            extends Consumer<Map<Block, BlockApiProvider<HeatStorage, @Nullable Direction>>> {
    }
}
