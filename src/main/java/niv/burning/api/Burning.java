package niv.burning.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@SuppressWarnings("java:S1948")
public final class Burning extends Number implements Comparable<Burning> {

    private static final Map<Item, Burning> ZEROS = new HashMap<>(AbstractFurnaceBlockEntity.getFuel().size());
    private static final Map<Item, Burning> ONES = new HashMap<>(AbstractFurnaceBlockEntity.getFuel().size());

    private static Item maxFuel = null;

    private final double percent;
    private final Item fuel;

    private final transient ItemStack fuelStack;

    private Burning(double percent, Item fuel) {
        this.percent = percent;
        this.fuel = fuel;
        this.fuelStack = new ItemStack(fuel);
    }

    public double getPercent() {
        return percent;
    }

    public Item getFuel() {
        return fuel;
    }

    public ItemStack getFuelStack() {
        return fuelStack;
    }

    public int getBurnDuration() {
        return defaultBurnDuration(this.fuel);
    }

    public int getBurnDuration(ToIntFunction<ItemStack> customBurnDuration) {
        return customBurnDuration.applyAsInt(this.fuelStack);
    }

    @Override
    public int intValue() {
        return (int) this.doubleValue();
    }

    @Override
    public long longValue() {
        return (long) this.doubleValue();
    }

    @Override
    public float floatValue() {
        return (float) this.doubleValue();
    }

    @Override
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
            return of(fuel).orElse(this);
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
            return of(fuel).orElse(this);
        } else if ((max = customBurnDuration.applyAsInt(new ItemStack(fuel))) > 0
                && (x = this.percent * max / getBurnDuration()) <= 1d) {
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

    public static final Optional<Burning> of(Item fuel) {
        if (AbstractFurnaceBlockEntity.getFuel().containsKey(fuel)) {
            return Optional.of(ZEROS.computeIfAbsent(fuel, item -> new Burning(0, item)));
        } else {
            return Optional.empty();
        }
    }

    public static final Optional<Burning> of(double percent, Item fuel) {
        if (percent == 0) {
            return of(fuel);
        } else if (percent > 0 && percent <= 1 && AbstractFurnaceBlockEntity.getFuel().containsKey(fuel)) {
            return Optional.of(new Burning(percent, fuel));
        } else {
            return Optional.empty();
        }
    }

    public static final Optional<Burning> of(int value, Item fuel) {
        double max;
        if (value == 0) {
            return of(fuel);
        } else if (value > 0 && (max = defaultBurnDuration(fuel)) > 0
                && value <= max) {
            return Optional.of(new Burning(value / max, fuel));
        } else {
            return Optional.empty();
        }
    }

    public static final Optional<Burning> of(int value, Item fuel, ToIntFunction<ItemStack> customBurnDuration) {
        double max;
        if (value == 0) {
            return of(fuel);
        } else if (value > 0 && (max = customBurnDuration.applyAsInt(new ItemStack(fuel))) > 0 && value <= max) {
            return Optional.of(new Burning(value / max, fuel));
        } else {
            return Optional.empty();
        }
    }

    public static final Burning getMaxBurning() {
        if (maxFuel == null) {
            maxFuel = AbstractFurnaceBlockEntity.getFuel().entrySet().stream()
                    .max((a, b) -> Integer.compare(a.getValue(), b.getValue()))
                    .map(Map.Entry::getKey)
                    .orElse(Items.LAVA_BUCKET);
        }
        return ONES.computeIfAbsent(maxFuel, item -> new Burning(1d, item));
    }

    public static final int defaultBurnDuration(ItemStack stack) {
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
    }

    public static final int defaultBurnDuration(Item item) {
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(item, 0);
    }
}
