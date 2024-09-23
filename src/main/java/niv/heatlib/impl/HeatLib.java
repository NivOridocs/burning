package niv.heatlib.impl;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@ApiStatus.Internal
public class HeatLib implements ModInitializer {
    public static final String MOD_ID = "heatlib";

    public static final Logger LOGGER = LoggerFactory.getLogger("HeatLib");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(new HeatLibRegistrar());
    }
}
