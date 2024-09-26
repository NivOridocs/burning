package niv.burning.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.MapMaker;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public final class Burning implements Comparable<Burning> {

    private static final Map<Item, Burning> ZEROS;
    private static final Map<Item, Burning> ONES;
    private static final Map<Item, ItemStack> STACKS;

    public static final Burning LAVA_BUCKET;
    public static final Burning BLAZE_ROD;
    public static final Burning COAL;

    public static final Burning MIN_VALUE;
    public static final Burning MAX_VALUE;

    static {
        ZEROS = new HashMap<>(AbstractFurnaceBlockEntity.getFuel().size());
        ONES = new HashMap<>(AbstractFurnaceBlockEntity.getFuel().size());
        STACKS = new MapMaker().concurrencyLevel(1).weakValues().makeMap();

        LAVA_BUCKET = of(Items.LAVA_BUCKET).one().zero();
        BLAZE_ROD = of(Items.BLAZE_ROD).one().zero();
        COAL = of(Items.COAL).one().zero();

        MIN_VALUE = AbstractFurnaceBlockEntity.getFuel().entrySet().stream()
                .max((a, b) -> Integer.compare(a.getValue(), b.getValue()))
                .map(Map.Entry::getKey)
                .flatMap(Burning::ofOptional)
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
        return customBurnDuration.applyAsInt(toStack(this.fuel));
    }

    public int intValue() {
        return (int) this.doubleValue();
    }

    public long longValue() {
        return (long) this.doubleValue();
    }

    public float floatValue() {
        return (float) this.doubleValue();
    }

    public double doubleValue() {
        return this.getBurnDuration() * percent;
    }

    public int intValue(ToIntFunction<ItemStack> customBurnDuration) {
        return (int) this.doubleValue(customBurnDuration);
    }

    public long longValue(ToIntFunction<ItemStack> customBurnDuration) {
        return (long) this.doubleValue(customBurnDuration);
    }

    public float floatValue(ToIntFunction<ItemStack> customBurnDuration) {
        return (float) this.doubleValue(customBurnDuration);
    }

    public double doubleValue(ToIntFunction<ItemStack> customBurnDuration) {
        return this.getBurnDuration(customBurnDuration) * percent;
    }

    @Override
    public int compareTo(Burning that) {
        var result = Double.compare(this.doubleValue(), that.doubleValue());
        if (result == 0) {
            result = Integer.compare(this.getBurnDuration(), that.getBurnDuration());
        }
        if (result == 0) {
            result = Integer.compare(Item.getId(this.fuel), Item.getId(that.fuel));
        }
        return result;
    }

    public int compareTo(Burning that, ToIntFunction<ItemStack> customBurnDuration) {
        var result = Double.compare(
                this.doubleValue(customBurnDuration),
                that.doubleValue(customBurnDuration));
        if (result == 0) {
            result = Integer.compare(
                    this.getBurnDuration(customBurnDuration),
                    that.getBurnDuration(customBurnDuration));
        }
        if (result == 0) {
            result = Integer.compare(Item.getId(this.fuel), Item.getId(that.fuel));
        }
        return result;
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
        } else if ((max = customBurnDuration.applyAsInt(toStack(fuel))) > 0
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

    public static final @Nullable Burning of(Item fuel) {
        return AbstractFurnaceBlockEntity.getFuel().containsKey(fuel)
                ? ZEROS.computeIfAbsent(fuel, item -> new Burning(0, item))
                : null;
    }

    public static final @Nullable Burning of(Item fuel, ToIntFunction<ItemStack> customBurnDuration) {
        return customBurnDuration.applyAsInt(toStack(fuel)) > 0
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

    private static final ItemStack toStack(Item item) {
        return STACKS.computeIfAbsent(item, ItemStack::new);
    }
}
