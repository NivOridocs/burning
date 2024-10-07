package niv.burning.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public final class Burning {

    private static final Map<Item, Burning> ZEROS;
    private static final Map<Item, Burning> ONES;

    public static final Burning LAVA_BUCKET;
    public static final Burning BLAZE_ROD;
    public static final Burning COAL;

    public static final Burning MIN_VALUE;
    public static final Burning MAX_VALUE;

    static {
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

    public double getPercent() {
        return percent;
    }

    public Item getFuel() {
        return fuel;
    }

    public int getBurnDuration() {
        return defaultBurnDuration(this.fuel);
    }

    public int getBurnDuration(ToIntFunction<ItemStack> customBurnDuration) {
        return customBurnDuration.applyAsInt(new ItemStack(this.fuel));
    }

    public Double getValue() {
        return this.getBurnDuration() * this.percent;
    }

    public Double getValue(ToIntFunction<ItemStack> customBurnDuration) {
        return this.getBurnDuration(customBurnDuration) * this.percent;
    }

    public Double getReverseValue() {
        return this.getBurnDuration() * (1d - this.percent);
    }

    public Double getReverseValue(ToIntFunction<ItemStack> customBurnDuration) {
        return this.getBurnDuration(customBurnDuration) * (1d - this.percent);
    }

    public Burning zero() {
        return ZEROS.computeIfAbsent(this.fuel, item -> new Burning(0, item));
    }

    public Burning one() {
        return ONES.computeIfAbsent(this.fuel, item -> new Burning(1, item));
    }

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

    public Burning withFuel(Item fuel) {
        double max;
        double x;
        if (this.percent == 0) {
            return ofOptional(fuel).orElse(this);
        } else if ((max = defaultBurnDuration(fuel)) > 0
                && (x = this.percent * max / getBurnDuration()) <= 1d) {
            return new Burning(x, fuel);
        } else {
            return this;
        }
    }

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

    public static final @Nullable Burning of(Item fuel) {
        return AbstractFurnaceBlockEntity.getFuel().containsKey(fuel)
                ? ZEROS.computeIfAbsent(fuel, item -> new Burning(0, item))
                : null;
    }

    public static final @Nullable Burning of(Item fuel, ToIntFunction<ItemStack> customBurnDuration) {
        return customBurnDuration.applyAsInt(new ItemStack(fuel)) > 0
                ? ZEROS.computeIfAbsent(fuel, item -> new Burning(0, item))
                : null;
    }

    public static final Optional<Burning> ofOptional(Item fuel) {
        return Optional.ofNullable(of(fuel));
    }

    public static final Optional<Burning> ofOptional(Item fuel, ToIntFunction<ItemStack> customBurnDuration) {
        return Optional.ofNullable(of(fuel, customBurnDuration));
    }

    public static final int defaultBurnDuration(ItemStack stack) {
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
    }

    public static final int defaultBurnDuration(Item item) {
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(item, 0);
    }

    public static final Burning add(Burning a, Burning b) {
        var value = a.getValue() + b.getValue();
        return a.getBurnDuration() >= b.getBurnDuration()
                ? combine(a, b, value)
                : combine(b, a, value);
    }

    public static final Burning add(Burning a, Burning b, ToIntFunction<ItemStack> customBurnDuration) {
        var value = a.getValue(customBurnDuration) + b.getValue(customBurnDuration);
        return a.getBurnDuration(customBurnDuration) >= b.getBurnDuration(customBurnDuration)
                ? combine(a, b, value, customBurnDuration)
                : combine(b, a, value, customBurnDuration);
    }

    public static final Burning subtract(Burning a, Burning b) {
        var value = Math.max(0, a.getValue() - b.getValue());
        return a.getBurnDuration() >= b.getBurnDuration()
                ? combine(a, b, value)
                : combine(b, a, value);
    }

    public static final Burning subtract(Burning a, Burning b, ToIntFunction<ItemStack> customBurnDuration) {
        var value = Math.max(0, a.getValue(customBurnDuration) - b.getValue(customBurnDuration));
        return a.getBurnDuration(customBurnDuration) >= b.getBurnDuration(customBurnDuration)
                ? combine(a, b, value, customBurnDuration)
                : combine(b, a, value, customBurnDuration);
    }

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

    public static final Burning maxValue(Burning a, Burning b) {
        return compareValue(a, b) >= 0 ? a : b;
    }

    public static final Burning maxValue(Burning a, Burning b, ToIntFunction<ItemStack> customBurnDuration) {
        return compareValue(a, b, customBurnDuration) >= 0 ? a : b;
    }

    public static final Burning minValue(Burning a, Burning b) {
        return compareValue(a, b) <= 0 ? a : b;
    }

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
}
