package niv.burning.impl;

import niv.burning.api.base.SimpleBurningStorage;

public class SimpleBurningStorages {
    private SimpleBurningStorages() {
    }

    public static SimpleBurningStorage createWhole() {
        return new SimpleBurningStorage();
    }

    public static SimpleBurningStorage createHalved() {
        return new SimpleBurningStorage(BurnDurationFunctions.HALVED);
    }

    public static SimpleBurningStorage createSquared() {
        return new SimpleBurningStorage(BurnDurationFunctions.SQUARED);
    }
}
