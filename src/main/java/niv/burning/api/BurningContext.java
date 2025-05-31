package niv.burning.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.impl.BurnDurationFunction;
import niv.burning.impl.SimpleBurningContext;

/**
 * A wrapper interface around the {@link isFuel} and {@link burnDuration}
 * methods and logic.
 */
public interface BurningContext {

    /**
     * Returns weather the provided {@link Item} is fuel according to this
     * {@link BurningContext} instance or not.
     *
     * @param item May not be null.
     * @return true if the provided {@link Item} is a fuel according to this
     *         {@link BurningContext} instance. Otherwise, false.
     */
    boolean isFuel(Item item);

    /**
     * Returns weather the provided {@link ItemStack} is fuel according to this
     * {@link BurningContext} instance or not.
     *
     * @param itemStack May not be null.
     * @return true if the provided {@link ItemStack} is a fuel according to this
     *         {@link BurningContext} instance. Otherwise, false.
     */
    boolean isFuel(ItemStack itemStack);

    /**
     * Returns the burn duration for the provided {@link Item} it it's a fuel.
     * Otherwise, zero.
     *
     * @param item May not be null.
     * @return A non-negative integer: the provided {@link Item}'s burn
     *         duration according to this {@link BurningContext}.
     */
    int burnDuration(Item item);

    /**
     * Returns the burn duration for the provided {@link ItemStack} it it's a fuel.
     * Otherwise, zero.
     *
     * @param itemStack May not be null.
     * @return A non-negative integer: the provided {@link ItemStack}'s burn
     *         duration according to this {@link BurningContext}.
     */
    int burnDuration(ItemStack itemStack);

    /**
     * Create a new {@link BurningContext} instance which will use the provided
     * {@link BurnDurationFunction} to evaluate every burn duration.
     *
     * @param burnDuration May not be null. It has the same signature of
     *                     {@link AbstractFurnaceBlockEntity#getBurnDuration}.
     * @return A new {@link BurningContext} instance.
     */
    BurningContext with(BurnDurationFunction burnDuration);

    /**
     * Default singleton lazy initialized instance.
     *
     * @return The same {@link BurningContext} instance every time.
     */
    public static BurningContext defaultInstance() {
        return SimpleBurningContext.instance();
    }

    /**
     * Level specific now or existing instance.
     *
     * @param level The level in which a burning storage exists.
     * @return The same {@link BurningContext} instance per level.
     */
    @SuppressWarnings("java:S1172")
    public static BurningContext defaultInstance(Level level) {
        return SimpleBurningContext.instance();
    }

    /**
     * Same as <code>BurningContext.defaultInstance().isFuel(item)</code>.
     */
    public static boolean defaultIsFuel(Item item) {
        return defaultInstance().isFuel(item);
    }

    /**
     * Same as <code>BurningContext.defaultInstance().isFuel(itemStack)</code>.
     */
    public static boolean defaultIsFuel(ItemStack itemStack) {
        return defaultInstance().isFuel(itemStack);
    }

    /**
     * Same as <code>BurningContext.defaultInstance().burnDuration(item)</code>.
     */
    public static int defaultBurnDuration(Item item) {
        return defaultInstance().burnDuration(item);
    }

    /**
     * Same as <code>BurningContext.defaultInstance().burnDuration(itemStack)</code>.
     */
    public static int defaultBurnDuration(ItemStack itemStack) {
        return defaultInstance().burnDuration(itemStack);
    }

    /**
     * Same as <code>BurningContext.defaultInstance().defaultWith(burnDuration)</code>.
     */
    public static BurningContext defaultWith(BurnDurationFunction burnDuration) {
        return defaultInstance().with(burnDuration);
    }
}
