package niv.burning.api;

import java.util.function.ToIntFunction;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import niv.burning.impl.SimpleBurningContext;

public interface BurningContext {

    boolean isFuel(Item item);

    boolean isFuel(ItemStack itemStack);

    int burnDuration(Item item);

    int burnDuration(ItemStack itemStack);

    BurningContext with(ToIntFunction<ItemStack> burnDuration);

    public static BurningContext defaultInstance() {
        return SimpleBurningContext.instance();
    }

    public static boolean defaultIsFuel(Item item) {
        return defaultInstance().isFuel(item);
    }

    public static boolean defaultIsFuel(ItemStack itemStack) {
        return defaultInstance().isFuel(itemStack);
    }

    public static int defaultBurnDuration(Item item) {
        return defaultInstance().burnDuration(item);
    }

    public static int defaultBurnDuration(ItemStack itemStack) {
        return defaultInstance().burnDuration(itemStack);
    }

    public static BurningContext defaultWith(ToIntFunction<ItemStack> burnDuration) {
        return defaultInstance().with(burnDuration);
    }
}
