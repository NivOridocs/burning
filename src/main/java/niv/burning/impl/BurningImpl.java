package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

@ApiStatus.Internal
public final class BurningImpl {

    public static final String MOD_ID;

    static {
        MOD_ID = "burning";

        ServerLifecycleEvents.SERVER_STARTING.register(new BurningRegistrar());
        DynamicRegistries.register(DynamicBurningStorageProvider.REGISTRY, DynamicBurningStorageProvider.CODEC);
    }

    BurningImpl() {
    }

    public static final void initialize() {
        // Trigger static initialization
    }
}