package niv.burning.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * An immutable object representing a percentage of a burning fuel.
 *
 * <p>
 * To create a simple instance:
 *
 * <pre>
 * BurningContext context = BurningContext.defaultInstance();
 * Burning.of(Items.COAL, context).withValue(800, context);
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
     * A zeroed instance with {@link Items#LAVA_BUCKET} as fuel. The same as
     * {@link #LAVA_BUCKET}.
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

        ZEROS = HashMap.newHashMap(50);
        ONES = HashMap.newHashMap(50);

        LAVA_BUCKET = new Burning(0d, Items.LAVA_BUCKET).one().zero();
        BLAZE_ROD = new Burning(0d, Items.BLAZE_ROD).one().zero();
        COAL = new Burning(0d, Items.COAL).one().zero();

        MIN_VALUE = LAVA_BUCKET.zero();
        MAX_VALUE = LAVA_BUCKET.one();
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
     * Get the fuel's burn duration as returned from the provided
     * {@link BurningContext#burnDuration(Item)}.
     *
     * <p>
     * Note: the result value corresponds to what it's usually stored in
     * {@link AbstractFurnaceBlockEntity#litDuration}.
     * </p>
     *
     * @param context Provided {@link BurningContext}.
     * @return A non-negative int: the fuel's burn duration.
     */
    public int getBurnDuration(BurningContext context) {
        return context.burnDuration(this.fuel);
    }

    /**
     * Get the burning amount this object represents. That is,
     * {@link #getBurnDuration(BurningContext)} * {@link #getPercent()}.
     *
     * <p>
     * Note: the result value corresponds to what it's usually stored in
     * {@link AbstractFurnaceBlockEntity#litTime}.
     * </p>
     *
     * @param context Provided {@link BurningContext}.
     * @return A non-negative Double: the burning amount.
     */
    public Double getValue(BurningContext context) {
        return context.burnDuration(this.fuel) * this.percent;
    }

    /**
     * Get the reverse of {@link #getValue(BurningContext)}.
     * That is, the difference between {@link #getBurnDuration(BurningContext)}
     * and {@link #getValue(BurningContext)}.
     *
     * @param context Provided {@link BurningContext}.
     * @return A non-negative Double: the burning missing amount.
     */
    public Double getReverseValue(BurningContext context) {
        return context.burnDuration(this.fuel) * (1d - this.percent);
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
     * @param value   A int representing a fraction of this' fuel burn duration.
     * @param context Provided {@link BurningContext}.
     * @return A non-null instance with same fuel but different percentage.
     */
    public Burning withValue(int value, BurningContext context) {
        double max;
        if (value <= 0) {
            return this.zero();
        } else if (value <= (max = this.getBurnDuration(context))) {
            return new Burning(value / max, this.fuel);
        } else {
            return this.one();
        }
    }

    /**
     * Return an instance with the provided fuel but which
     * {@link #getValue(BurningContext)}
     * should return the same number as this instance.
     *
     * @param fuel    A new fuel item.
     * @param context Provided {@link BurningContext}.
     * @return A non-null instance with different fuel.
     */
    public Burning withFuel(Item fuel, BurningContext context) {
        double max;
        double x;
        if (this.percent == 0) {
            return ofOptional(fuel, context).orElse(this);
        } else if ((max = context.burnDuration(fuel)) > 0
                && (x = this.getValue(context) / max) <= 1d) {
            return new Burning(x, fuel);
        } else {
            return ofOptional(fuel, context).orElse(this).one();
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
     * If the provided item is fuel according to the provided
     * {@link BurningContext#isFuel(Item)}, then return a zeroed instance with the
     * provided item as fuel. Otherwise, return null.
     *
     * @param fuel    A item which should be a fuel.
     * @param context Provided {@link BurningContext}.
     * @return A zeroed instance if fuel is indeed a fuel, null otherwise.
     */
    public static final @Nullable Burning of(Item fuel, BurningContext context) {
        return context.isFuel(fuel)
                ? ZEROS.computeIfAbsent(fuel, item -> new Burning(0, item))
                : null;
    }

    /**
     * Wraps the result of {@link #of(Item, BurningContext)} into an
     * {@link Optional}.
     */
    public static final Optional<Burning> ofOptional(Item fuel, BurningContext context) {
        return Optional.ofNullable(of(fuel, context));
    }

    /**
     * Add two instance together.
     *
     * <p>
     * The result can have a's fuel, b's fuel, or {@link #MIN_VALUE}'s fuel,
     * whichever is the smaller among those greater than a and b's values sum.
     * </p>
     *
     * @param a       May not be null.
     * @param b       May not be null.
     * @param context Provided {@link BurningContext}.
     * @return A instance of burning representing a and b's sum or
     *         {@link #MAX_VALUE}, whichever {@link #getValue(BurningContext)} is
     *         lower.
     */
    public static final Burning add(Burning a, Burning b, BurningContext context) {
        var value = a.getValue(context) + b.getValue(context);
        return a.getBurnDuration(context) >= b.getBurnDuration(context)
                ? combine(a, b, value, context)
                : combine(b, a, value, context);
    }

    /**
     * Subtract b from a.
     *
     * <p>
     * The result can have a's fuel or b's fuel, whichever is the smaller among
     * those greater than a and b's values difference.
     * </p>
     *
     * @param a       May not be null.
     * @param b       May not be null.
     * @param context Provided {@link BurningContext}.
     * @return A instance of burning representing a and b's difference.
     */
    public static final Burning subtract(Burning a, Burning b, BurningContext context) {
        var value = Math.max(0, a.getValue(context) - b.getValue(context));
        return a.getBurnDuration(context) >= b.getBurnDuration(context)
                ? combine(a, b, value, context)
                : combine(b, a, value, context);
    }

    /**
     * The same as calling:
     *
     * <pre>
     * Double.compare(a == null ? 0d : a.getValue(), b == null ? 0d : b.getValue())
     * </pre>
     *
     * @param a       May be null.
     * @param b       May be null.
     * @param context Provided {@link BurningContext}.
     * @return the value 0 if a's value is equal to b's value; a value less than 0
     *         if a's value is less than b's value; and a value greater than 0 if
     *         a's value is numerically greater than b' value.
     */
    public static final int compareValue(Burning a, Burning b, BurningContext context) {
        if (a == b) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return +1;
        } else {
            return Double.compare(a.getValue(context), b.getValue(context));
        }
    }

    /**
     * @param a       May be null.
     * @param b       May be null.
     * @param context Provided {@link BurningContext}.
     * @return a if <code>compareValue(a, b) >= 0</code>, b otherwise.
     */
    public static final Burning maxValue(Burning a, Burning b, BurningContext context) {
        return compareValue(a, b, context) >= 0 ? a : b;
    }

    /**
     * @param a       May be null.
     * @param b       May be null.
     * @param context Provided {@link BurningContext}.
     * @return a if <code>compareValue(a, b) <= 0</code>, b otherwise.
     */
    public static final Burning minValue(Burning a, Burning b, BurningContext context) {
        return compareValue(a, b, context) <= 0 ? a : b;
    }

    private static final Burning combine(Burning high, Burning low, double value, BurningContext context) {
        if (value <= low.getBurnDuration(context)) {
            return low.withValue((int) value, context);
        } else if (value <= high.getBurnDuration(context)) {
            return high.withValue((int) value, context);
        } else {
            return Burning.MIN_VALUE.withValue((int) value, context);
        }
    }

    public Tag save(HolderLookup.Provider provider, Tag tag) {
        return CODEC.encode(this, provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }

    public static Optional<Burning> parse(HolderLookup.Provider provider, Tag tag) {
        return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).result();
    }
}
