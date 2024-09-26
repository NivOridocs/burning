package niv.burning.api.event;

import java.util.Map;

import org.apache.logging.log4j.util.BiConsumer;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import niv.burning.api.BurningStorage;

public final class BurningStorageLifecycleEvents {
    private BurningStorageLifecycleEvents() {
    }

    public static final Event<BurningStorageRegistering> BURNING_STORAGE_REGISTERING = EventFactory.createArrayBacked(
            BurningStorageRegistering.class,
            listeners -> (server, map) -> {
                for (var listener : listeners) {
                    listener.accept(server, map);
                }
            });

    @FunctionalInterface
    public interface BurningStorageRegistering
            extends BiConsumer<MinecraftServer, Map<Block, BlockApiProvider<BurningStorage, @Nullable Direction>>> {
    }
}
