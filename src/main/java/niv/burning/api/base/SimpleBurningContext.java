package niv.burning.api.base;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import niv.burning.api.BurningContext;

/**
 * Simple immutable {@link BurningContext} backed by an {@link Object2IntMap} between items and burn times (ticks).
 * Items not mapped have 0 burn time and are not fuel.
 */
public class SimpleBurningContext implements BurningContext {

    private final Object2IntMap<Item> values;

    /**
     * Creates an empty context (no items are fuel).
     */
    public SimpleBurningContext() {
        this.values = new Object2IntOpenHashMap<>();
    }

    /**
     * Creates a context using a copy of the given map.
     * @param values item-to-burn duration map (null = empty)
     */
    public SimpleBurningContext(Map<Item, Integer> values) {
        this();
        if (values != null)
            this.values.putAll(values);
    }

    @Override
    public boolean isFuel(Item item) {
        return this.values.getInt(item) > 0;
    }

    @Override
    public boolean isFuel(ItemStack stack) {
        return this.values.getInt(stack.getItem()) > 0;
    }

    @Override
    public int burnDuration(Item item) {
        return this.values.getInt(item);
    }

    @Override
    public int burnDuration(ItemStack stack) {
        return this.values.getInt(stack.getItem());
    }
}
