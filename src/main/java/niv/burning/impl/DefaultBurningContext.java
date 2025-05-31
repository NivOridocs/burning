package niv.burning.impl;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.BurningContext;

public class DefaultBurningContext implements BurningContext {

    private static DefaultBurningContext instance;

    private DefaultBurningContext() {
    }

    @Override
    public boolean isFuel(Item item) {
        return AbstractFurnaceBlockEntity.getFuel().containsKey(item);
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return AbstractFurnaceBlockEntity.getFuel().containsKey(itemStack.getItem());
    }

    @Override
    public int burnDuration(Item item) {
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(item, 0);
    }

    @Override
    public int burnDuration(ItemStack itemStack) {
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(itemStack.getItem(), 0);
    }

    public static synchronized BurningContext instance() {
        if (instance == null) {
            instance = new DefaultBurningContext();
        }
        return instance;
    }
}
