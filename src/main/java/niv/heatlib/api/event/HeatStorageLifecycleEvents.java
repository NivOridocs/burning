package niv.heatlib.api.event;

import java.util.Map;

import org.apache.logging.log4j.util.BiConsumer;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import niv.heatlib.api.HeatStorage;

public final class HeatStorageLifecycleEvents {
    private HeatStorageLifecycleEvents() {
    }

    public static final Event<HeatStorageRegistering> HEAT_STORAGE_REGISTERING = EventFactory.createArrayBacked(
            HeatStorageRegistering.class,
            listeners -> (server, map) -> {
                for (var listener : listeners) {
                    listener.accept(server, map);
                }
            });

    @FunctionalInterface
    public interface HeatStorageRegistering
            extends BiConsumer<MinecraftServer, Map<Block, BlockApiProvider<HeatStorage, @Nullable Direction>>> {
    }
}
