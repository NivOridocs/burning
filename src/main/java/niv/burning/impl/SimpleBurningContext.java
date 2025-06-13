package niv.burning.impl;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import niv.burning.api.BurningContext;

public class SimpleBurningContext implements BurningContext {

    private final AbstractFurnaceBlockEntity target;

    private final FuelValues values;

    public SimpleBurningContext(AbstractFurnaceBlockEntity target, BurningContext context) {
        this.target = target;
        if (context instanceof FuelValues fuels) {
            this.values = fuels;
        } else {
            this.values = new ForwardingFuelValues(context);
        }
    }

    @Override
    public boolean isFuel(Item item) {
        return this.values.isFuel(new ItemStack(item));
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return this.values.isFuel(itemStack);
    }

    @Override
    public int burnDuration(Item item) {
        return this.target.getBurnDuration(this.values, new ItemStack(item));
    }

    @Override
    public int burnDuration(ItemStack itemStack) {
        return this.target.getBurnDuration(this.values, itemStack);
    }

    private static final class ForwardingFuelValues extends FuelValues {

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
}
