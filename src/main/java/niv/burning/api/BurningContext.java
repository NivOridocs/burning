package niv.burning.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Represents a context for determining fuel status and burn duration for items and item stacks.
 * <p>
 * Implementations define what counts as fuel and how long it burns.
 * </p>
 */
public interface BurningContext {

    /**
     * Returns whether the provided {@link Item} is considered fuel in this context.
     *
     * @param item must not be null
     * @return true if the provided {@link Item} is a fuel according to this context, false otherwise
     */
    boolean isFuel(Item item);

    /**
     * Returns whether the provided {@link ItemStack} is considered fuel in this context.
     *
     * @param itemStack must not be null
     * @return true if the provided {@link ItemStack} is a fuel according to this context, false otherwise
     */
    boolean isFuel(ItemStack itemStack);

    /**
     * Returns the burn duration for the provided {@link Item} if it is a fuel, or zero otherwise.
     *
     * @param item must not be null
     * @return a non-negative integer: the burn duration for the provided {@link Item} in this context
     */
    int burnDuration(Item item);

    /**
     * Returns the burn duration for the provided {@link ItemStack} if it is a fuel, or zero otherwise.
     *
     * @param itemStack must not be null
     * @return a non-negative integer: the burn duration for the provided {@link ItemStack} in this context
     */
    int burnDuration(ItemStack itemStack);
}
