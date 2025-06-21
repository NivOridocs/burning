package niv.burning.impl;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import niv.burning.api.BurningContext;

final class ForwardingFuelValues extends FuelValues {

    private final BurningContext context;

    ForwardingFuelValues(BurningContext context) {
        super(null);
        this.context = context;
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return this.context.isFuel(itemStack);
    }

    @Override
    public int burnDuration(ItemStack itemStack) {
        return this.context.burnDuration(itemStack);
    }
}
