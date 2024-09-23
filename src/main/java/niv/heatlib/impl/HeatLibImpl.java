package niv.heatlib.impl;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@ApiStatus.Internal
public final class HeatLibImpl {

    public static final String MOD_ID;

    static {
        MOD_ID = "heatlib";

        ServerLifecycleEvents.SERVER_STARTING.register(new HeatLibRegistrar());
    }

    HeatLibImpl() {
    }

    public static final void initialize() {
        // Trigger static initialization
    }
}
