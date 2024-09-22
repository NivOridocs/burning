package niv.heatlib.api.event;

import java.util.Map;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import niv.heatlib.api.HeatStorageProvider;

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
    public interface HeatStorageBinding extends Consumer<Map<BlockEntityType<?>, HeatStorageProvider>> {
    }
}
