package niv.burning.impl;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import niv.burning.api.BurningContext;

public class FuelValuesBurningContext implements BurningContext {

    private final FuelValues values;

    public FuelValuesBurningContext(FuelValues values) {
        this.values = values;
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return this.values.isFuel(itemStack);
    }

    @Override
    public int burnDuration(ItemStack itemStack) {
        return this.values.burnDuration(itemStack);
    }
}
