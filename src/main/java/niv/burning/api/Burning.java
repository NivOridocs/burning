package niv.burning.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * An immutable object representing a percentage of a burning fuel.
 *
 * <p>
 * To create a simple instance:
 *
 * <pre>
 * Burning.of(Items.COAL).withValue(800);
 * </pre>
 * </p>
 */
public final class Burning {

    public static final Codec<Burning> CODEC;

    private static final Map<Item, Burning> ZEROS;
    private static final Map<Item, Burning> ONES;

    /**
     * A zeroed instance with {@link Items#LAVA_BUCKET} as fuel.
     */
    public static final Burning LAVA_BUCKET;

    /**
     * A zeroed instance with {@link Items#BLAZE_ROD} as fuel.
     */
    public static final Burning BLAZE_ROD;

    /**
     * A zeroed instance with {@link Items#COAL} as fuel.
     */
    public static final Burning COAL;

    /**
     * A zeroed instance with, as fuel, the item with higher burn duration.
     */
    public static final Burning MIN_VALUE;

    /**
     * The result of <code>Burning.MIN_VALUE.one()</code>.
     */
    public static final Burning MAX_VALUE;

    static {
        CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance
                .group(
                        Codec.doubleRange(0d, 1d).fieldOf("percent").orElse(0d).forGetter(Burning::getPercent),
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("fuel").forGetter(Burning::getFuel))
                .apply(instance, Burning::new)));

        ZEROS = new HashMap<>(AbstractFurnaceBlockEntity.getFuel().size());
        ONES = new HashMap<>(AbstractFurnaceBlockEntity.getFuel().size());

        LAVA_BUCKET = of(Items.LAVA_BUCKET).one().zero();
        BLAZE_ROD = of(Items.BLAZE_ROD).one().zero();
        COAL = of(Items.COAL).one().zero();

        MIN_VALUE = AbstractFurnaceBlockEntity.getFuel().entrySet().stream()
                .max((a, b) -> Integer.compare(a.getValue(), b.getValue()))
                .map(Map.Entry::getKey)
                .map(Burning::of)
                .orElse(LAVA_BUCKET);
        MAX_VALUE = MIN_VALUE.one();
    }

    private final double percent;
    private final Item fuel;

    private Burning(double percent, Item fuel) {
        this.percent = percent;
        this.fuel = fuel;
    }

    /**
     * Get the percent of burning fuel this object represents.
     *
     * @return A double between 0 and 1.
     */
    public double getPercent() {
        return percent;
    }

    /**
     * Get the burning fuel of which percentage this object represents.
     *
     * @return A non-null instance of {@link Item}.
     */
    public Item getFuel() {
        return fuel;
    }

    /**
     * Get the fuel's burn duration as returned from
     * {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * <p>
     * Note: the result value corresponds to what it's usually stored in
     * {@link AbstractFurnaceBlockEntity#litDuration}.
     * </p>
     *
     * @return A non-negative int: the fuel's burn duration.
     */
    public int getBurnDuration() {
        return defaultBurnDuration(this.fuel);
    }

    /**
     * Same as {@link #getBurnDuration()} but using the provided custom
     * implementation instead of
     * {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return A non-negative int: the fuel's burn duration.
     */
    public int getBurnDuration(ToIntFunction<ItemStack> customBurnDuration) {
        return customBurnDuration.applyAsInt(new ItemStack(this.fuel));
    }

    /**
     * Get the burning amount this object represents. That is,
     * {@link #getBurnDuration()} * {@link #getPercent()}.
     *
     * <p>
     * Note: the result value corresponds to what it's usually stored in
     * {@link AbstractFurnaceBlockEntity#litTime}.
     * </p>
     *
     * @return A non-negative Double: the burning amount.
     */
    public Double getValue() {
        return this.getBurnDuration() * this.percent;
    }

    /**
     * Same as {@link #getValue()} but using the provided custom implementation
     * instead of {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return A non-negative Double: the burning amount.
     */
    public Double getValue(ToIntFunction<ItemStack> customBurnDuration) {
        return this.getBurnDuration(customBurnDuration) * this.percent;
    }

    /**
     * Get the reverse of {@link #getValue()}. That is, the difference between
     * {@link #getBurnDuration()} and {@link #getValue()}.
     *
     * @return A non-negative Double: the burning missing amount.
     */
    public Double getReverseValue() {
        return this.getBurnDuration() * (1d - this.percent);
    }

    /**
     * Same as {@link #getReverseValue()} but using the provided custom
     * implementation instead of
     * {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return A non-negative Double: the burning missing amount.
     */
    public Double getReverseValue(ToIntFunction<ItemStack> customBurnDuration) {
        return this.getBurnDuration(customBurnDuration) * (1d - this.percent);
    }

    /**
     * Return a zeroed instance with the same fuel as this. That is, an instance
     * with 0 percentage.
     *
     * @return A non-null instance. It's cached.
     */
    public Burning zero() {
        return ZEROS.computeIfAbsent(this.fuel, item -> new Burning(0, item));
    }

    /**
     * Return an instance with the same fuel as this but with percentage equals to
     * 1.
     *
     * @return A non-null instance. It's cached.
     */
    public Burning one() {
        return ONES.computeIfAbsent(this.fuel, item -> new Burning(1, item));
    }

    /**
     * Return an instance with the same fuel as this but with a percentage equal to
     * the ratio between the provided value and this' fuel burn duration.
     *
     * <p>
     * If value is less than or equal to zero, this method is identical to
     * {@link #zero()}.
     * </p>
     *
     * <p>
     * If value equals or exceeds this' fuel burn duration, this method is identical
     * to {@link #one()}.
     * </p>
     *
     * @param value A int representing a fraction of this' fuel burn duration.
     * @return A non-null instance with same fuel but different percentage.
     */
    public Burning withValue(int value) {
        double max;
        if (value <= 0) {
            return this.zero();
        } else if (value <= (max = getBurnDuration())) {
            return new Burning(value / max, this.fuel);
        } else {
            return this.one();
        }
    }

    /**
     * Same as {@link #withValue(int)} but using the provided custom implementation
     * instead of {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param value              A int representing a fraction of this' fuel burn
     *                           duration.
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return A non-null instance with same fuel but different percentage.
     */
    public Burning withValue(int value, ToIntFunction<ItemStack> customBurnDuration) {
        double max;
        if (value <= 0) {
            return this.zero();
        } else if (value <= (max = getBurnDuration(customBurnDuration))) {
            return new Burning(value / max, this.fuel);
        } else {
            return this.one();
        }
    }

    /**
     * Return an instance with the provided fuel but which {@link #getValue()}
     * should return the same number as this instance.
     *
     * @param fuel A new fuel item.
     * @return A non-null instance with different fuel.
     */
    public Burning withFuel(Item fuel) {
        double max;
        double x;
        if (this.percent == 0) {
            return ofOptional(fuel).orElse(this);
        } else if ((max = defaultBurnDuration(fuel)) > 0
                && (x = this.percent * max / getBurnDuration()) <= 1d) {
            return new Burning(x, fuel);
        } else {
            return ofOptional(fuel).orElse(this).one();
        }
    }

    /**
     * Same as {@link #withFuel(Item)} but using the provided custom implementation
     * instead of {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param fuel               A new fuel item.
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return A non-null instance with different fuel.
     */
    public Burning withFuel(Item fuel, ToIntFunction<ItemStack> customBurnDuration) {
        double max;
        double x;
        if (this.percent == 0) {
            return ofOptional(fuel, customBurnDuration).orElse(this);
        } else if ((max = customBurnDuration.applyAsInt(new ItemStack(fuel))) > 0
                && (x = this.percent * max / getBurnDuration(customBurnDuration)) <= 1d) {
            return new Burning(x, fuel);
        } else {
            return this;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.percent);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((this.fuel == null) ? 0 : this.fuel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object == null) {
            return false;
        } else if (object instanceof Burning that) {
            return this.percent == that.percent
                    && Objects.equals(this.fuel, that.fuel);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Burning [" + percent + " of " + fuel + "]";
    }

    /**
     * Return a zeroed instance with the provided fuel if
     * {@link AbstractFurnaceBlockEntity#getFuel()} contains the latter as a key.
     * Otherwise, return null.
     *
     * @param fuel A item which should be a fuel.
     * @return A zeroed instance if fuel is indeed a fuel, null otherwise.
     */
    public static final @Nullable Burning of(Item fuel) {
        return AbstractFurnaceBlockEntity.getFuel().containsKey(fuel)
                ? ZEROS.computeIfAbsent(fuel, item -> new Burning(0, item))
                : null;
    }

    /**
     * Same as {@link #of(Item)} but using the provided custom implementation to
     * check if fuel is indeed a fuel.
     *
     * @param fuel               A item which should be a fuel.
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return A zeroed instance if fuel is indeed a fuel, null otherwise.
     */
    public static final @Nullable Burning of(Item fuel, ToIntFunction<ItemStack> customBurnDuration) {
        return customBurnDuration.applyAsInt(new ItemStack(fuel)) > 0
                ? ZEROS.computeIfAbsent(fuel, item -> new Burning(0, item))
                : null;
    }

    /**
     * Wraps the result of {@link #of(Item)} into an {@link Optional}.
     */
    public static final Optional<Burning> ofOptional(Item fuel) {
        return Optional.ofNullable(of(fuel));
    }

    /**
     * Wraps the result of {@link #of(Item, ToIntFunction)} into an
     * {@link Optional}.
     */
    public static final Optional<Burning> ofOptional(Item fuel, ToIntFunction<ItemStack> customBurnDuration) {
        return Optional.ofNullable(of(fuel, customBurnDuration));
    }

    /**
     * Utility method that returns the stack's burn duration from
     * {@link AbstractFurnaceBlockEntity#getFuel()}.
     *
     * @param stack A stack of potential fuel.
     * @return The fuel's burn duration if stack is a fuel, zero otherwise.
     */
    public static final int defaultBurnDuration(ItemStack stack) {
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
    }

    /**
     * Utility method that returns the item's burn duration from
     * {@link AbstractFurnaceBlockEntity#getFuel()}.
     *
     * @param stack A potential fuel item.
     * @return The fuel's burn duration if item is a fuel, zero otherwise.
     */
    public static final int defaultBurnDuration(Item item) {
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(item, 0);
    }

    /**
     * Add two instance together.
     *
     * <p>
     * The result can have a's fuel, b's fuel, or {@link #MIN_VALUE}'s fuel,
     * whichever is the smaller among those greater than a and b's values sum.
     * </p>
     *
     * @return A instance of burning representing a and b's sum or
     *         {@link #MAX_VALUE}, whichever {@link #getValue()} is lower.
     */
    public static final Burning add(Burning a, Burning b) {
        var value = a.getValue() + b.getValue();
        return a.getBurnDuration() >= b.getBurnDuration()
                ? combine(a, b, value)
                : combine(b, a, value);
    }

    /**
     * Same as {@link #add(Burning, Burning)} but using the provided custom
     * implementation instead of
     * {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return A instance of burning representing a and b's sum or
     *         {@link #MAX_VALUE}, whichever {@link #getValue()} is lower.
     */
    public static final Burning add(Burning a, Burning b, ToIntFunction<ItemStack> customBurnDuration) {
        var value = a.getValue(customBurnDuration) + b.getValue(customBurnDuration);
        return a.getBurnDuration(customBurnDuration) >= b.getBurnDuration(customBurnDuration)
                ? combine(a, b, value, customBurnDuration)
                : combine(b, a, value, customBurnDuration);
    }

    /**
     * Subtract b from a.
     *
     * <p>
     * The result can have a's fuel or b's fuel, whichever is the smaller among
     * those greater than a and b's values difference.
     * </p>
     *
     * @return A instance of burning representing a and b's difference.
     */
    public static final Burning subtract(Burning a, Burning b) {
        var value = Math.max(0, a.getValue() - b.getValue());
        return a.getBurnDuration() >= b.getBurnDuration()
                ? combine(a, b, value)
                : combine(b, a, value);
    }

    /**
     * Same as {@link #subtract(Burning, Burning)} but using the provided custom
     * implementation instead of
     * {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return A instance of burning representing a and b's difference.
     */
    public static final Burning subtract(Burning a, Burning b, ToIntFunction<ItemStack> customBurnDuration) {
        var value = Math.max(0, a.getValue(customBurnDuration) - b.getValue(customBurnDuration));
        return a.getBurnDuration(customBurnDuration) >= b.getBurnDuration(customBurnDuration)
                ? combine(a, b, value, customBurnDuration)
                : combine(b, a, value, customBurnDuration);
    }

    /**
     * The same as calling:
     *
     * <pre>
     * Double.compare(a == null ? 0d : a.getValue(), b == null ? 0d : b.getValue())
     * </pre>
     *
     * @param a May be null.
     * @param b May be null.
     * @return the value 0 if a's value is equal to b's value; a value less than 0
     *         if a's value is less than b's value; and a value greater than 0 if
     *         a's value is numerically greater than b' value.
     */
    public static final int compareValue(Burning a, Burning b) {
        if (a == b) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return +1;
        } else {
            return Double.compare(a.getValue(), b.getValue());
        }
    }

    /**
     * Same as {@link #compareValue(Burning, Burning)} but using the provided custom
     * implementation instead of
     * {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param a                  May be null.
     * @param b                  May be null.
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return the value 0 if a's value is equal to b's value; a value less than 0
     *         if a's value is less than b's value; and a value greater than 0 if
     *         a's value is numerically greater than b' value.
     */
    public static final int compareValue(Burning a, Burning b, ToIntFunction<ItemStack> customBurnDuration) {
        if (a == b) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return +1;
        } else {
            return Double.compare(a.getValue(customBurnDuration), b.getValue(customBurnDuration));
        }
    }

    /**
     * @param a May be null.
     * @param b May be null.
     * @return a if <code>compareValue(a, b) >= 0</code>, b otherwise.
     */
    public static final Burning maxValue(Burning a, Burning b) {
        return compareValue(a, b) >= 0 ? a : b;
    }

    /**
     * Same as {@link #maxValue(Burning, Burning)} but using the provided custom
     * implementation instead of
     * {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param a                  May be null.
     * @param b                  May be null.
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return a if <code>compareValue(a, b, customBurnDuration) >= 0</code>, b
     *         otherwise.
     */
    public static final Burning maxValue(Burning a, Burning b, ToIntFunction<ItemStack> customBurnDuration) {
        return compareValue(a, b, customBurnDuration) >= 0 ? a : b;
    }

    /**
     * @param a May be null.
     * @param b May be null.
     * @return a if <code>compareValue(a, b) <= 0</code>, b otherwise.
     */
    public static final Burning minValue(Burning a, Burning b) {
        return compareValue(a, b) <= 0 ? a : b;
    }

    /**
     * Same as {@link #minValue(Burning, Burning)} but using the provided custom
     * implementation instead of
     * {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     *
     * @param a                  May be null.
     * @param b                  May be null.
     * @param customBurnDuration A custom implementation of
     *                           {@link AbstractFurnaceBlockEntity#getBurnDuration(ItemStack)}.
     * @return a if <code>compareValue(a, b, customBurnDuration) <= 0</code>, b
     *         otherwise.
     */
    public static final Burning minValue(Burning a, Burning b, ToIntFunction<ItemStack> customBurnDuration) {
        return compareValue(a, b, customBurnDuration) <= 0 ? a : b;
    }

    private static final Burning combine(Burning high, Burning low, double value) {
        if (value <= low.getBurnDuration()) {
            return low.withValue((int) value);
        } else if (value <= high.getBurnDuration()) {
            return high.withValue((int) value);
        } else {
            return Burning.MIN_VALUE.withValue((int) value);
        }
    }

    private static final Burning combine(Burning high, Burning low, double value, ToIntFunction<ItemStack> custom) {
        if (value <= low.getBurnDuration(custom)) {
            return low.withValue((int) value, custom);
        } else if (value <= high.getBurnDuration(custom)) {
            return high.withValue((int) value, custom);
        } else {
            return Burning.MIN_VALUE.withValue((int) value, custom);
        }
    }

    public Tag save(HolderLookup.Provider provider, Tag tag) {
        return CODEC.encode(this, provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }

    public static Optional<Burning> parse(HolderLookup.Provider provider, Tag tag) {
        return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).result();
    }
}
