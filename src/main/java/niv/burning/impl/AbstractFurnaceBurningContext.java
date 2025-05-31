package niv.burning.impl;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.BurningContext;

public class AbstractFurnaceBurningContext implements BurningContext {

    private final AbstractFurnaceBlockEntity target;

    public AbstractFurnaceBurningContext(AbstractFurnaceBlockEntity entity) {
        this.target = entity;
    }

    @Override
    public boolean isFuel(Item item) {
        return DefaultBurningContext.instance().isFuel(item);
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return DefaultBurningContext.instance().isFuel(itemStack);
    }

    @Override
    public int burnDuration(Item item) {
        return this.target.getBurnDuration(new ItemStack(item));
    }

    @Override
    public int burnDuration(ItemStack itemStack) {
        return this.target.getBurnDuration(itemStack);
    }
}
