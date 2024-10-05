package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.item.Item;

@ApiStatus.Internal
@SuppressWarnings("java:S100")
public interface FuelCache {

    Item burning_getFuel();

    void burning_setFuel(Item fuel);
}
