package niv.burning.impl;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import niv.burning.api.BurningContext;

public class BurningContexts {

    public static final BurningContext HALVED = new BurningContext() {
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
            return DefaultBurningContext.instance().burnDuration(item) / 2;
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return DefaultBurningContext.instance().burnDuration(itemStack) / 2;
        }
    };

    public static final BurningContext SQUARED = new BurningContext() {
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
            return square(DefaultBurningContext.instance().burnDuration(item));
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return square(DefaultBurningContext.instance().burnDuration(itemStack));
        }
    };

    private static final int square(int i) {
        return i * i;
    }
}
