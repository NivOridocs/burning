package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

@ApiStatus.Internal
public final class BurningImpl {

    public static final String MOD_ID;

    public static final String MOD_NAME;

    public static final Logger LOGGER;

    static {
        MOD_ID = "burning";
        MOD_NAME = "Burning";
        LOGGER = LoggerFactory.getLogger(MOD_NAME);

        ServerLifecycleEvents.SERVER_STARTING.register(new BurningRegistrar());
        DynamicRegistries.register(DynamicBurningStorageProvider.REGISTRY, DynamicBurningStorageProvider.CODEC);
    }

    BurningImpl() {
    }

    public static final void initialize() {
        // Trigger static initialization
    }
}
