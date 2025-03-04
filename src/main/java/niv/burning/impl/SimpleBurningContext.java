package niv.burning.impl;

import java.util.function.ToIntFunction;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.BurningContext;

public class SimpleBurningContext implements BurningContext {

    private static SimpleBurningContext instance;

    private SimpleBurningContext() {
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

    @Override
    public BurningContext with(final ToIntFunction<ItemStack> burnDuration) {
        return burnDuration == null ? this : new BurningContext() {
            @Override
            public boolean isFuel(Item item) {
                return SimpleBurningContext.this.isFuel(item);
            }

            @Override
            public boolean isFuel(ItemStack itemStack) {
                return SimpleBurningContext.this.isFuel(itemStack);
            }

            @Override
            public int burnDuration(ItemStack itemStack) {
                return burnDuration.applyAsInt(itemStack);
            }

            @Override
            public int burnDuration(Item item) {
                return burnDuration.applyAsInt(new ItemStack(item));
            }

            @Override
            public BurningContext with(ToIntFunction<ItemStack> burnDuration) {
                return SimpleBurningContext.this.with(burnDuration);
            }
        };
    }

    public static synchronized SimpleBurningContext instance() {
        if (instance == null) {
            instance = new SimpleBurningContext();
        }
        return instance;
    }
}
