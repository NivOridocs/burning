package niv.burning.impl;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.BurningContext;

public class SimpleBurningContext implements BurningContext {

    private final AbstractFurnaceBlockEntity target;

    private final BurningContext context;

    public SimpleBurningContext(AbstractFurnaceBlockEntity target, BurningContext context) {
        this.target = target;
        this.context = context;
    }

    @Override
    public boolean isFuel(Item item) {
        return this.context.isFuel(new ItemStack(item));
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return this.context.isFuel(itemStack);
    }

    @Override
    public int burnDuration(Item item) {
        return this.target.getBurnDuration(new ForwardingFuelValues(this.context), new ItemStack(item));
    }

    @Override
    public int burnDuration(ItemStack itemStack) {
        return this.target.getBurnDuration(new ForwardingFuelValues(this.context), itemStack);
    }
}
