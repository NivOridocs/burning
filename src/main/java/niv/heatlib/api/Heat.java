package niv.heatlib.api;

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
public final class Heat extends Number implements Comparable<Heat> {

    private static final Map<Item, Heat> ZEROS = new HashMap<>(AbstractFurnaceBlockEntity.getFuel().size());
    private static final Map<Item, Heat> ONES = new HashMap<>(AbstractFurnaceBlockEntity.getFuel().size());

    private static Item maxFuel = null;

    private final double percent;
    private final Item fuel;

    private final transient ItemStack fuelStack;

    private Heat(double percent, Item fuel) {
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
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(this.fuel, 0);
    }

    public int getBurnDuration(ToIntFunction<ItemStack> custom) {
        return custom.applyAsInt(this.fuelStack);
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

    public int intValue(ToIntFunction<ItemStack> getBurnDuration) {
        return (int) this.doubleValue(getBurnDuration);
    }

    public long longValue(ToIntFunction<ItemStack> getBurnDuration) {
        return (long) this.doubleValue(getBurnDuration);
    }

    public float floatValue(ToIntFunction<ItemStack> getBurnDuration) {
        return (float) this.doubleValue(getBurnDuration);
    }

    public double doubleValue(ToIntFunction<ItemStack> getBurnDuration) {
        return this.getBurnDuration(getBurnDuration) * percent;
    }

    @Override
    public int compareTo(Heat that) {
        return Double.compare(this.doubleValue(), that.doubleValue());
    }

    public int compareTo(Heat that, ToIntFunction<ItemStack> getBurnDuration) {
        return Double.compare(this.doubleValue(getBurnDuration), that.doubleValue(getBurnDuration));
    }

    public Heat zero() {
        return ZEROS.computeIfAbsent(this.fuel, item -> new Heat(0, item));
    }

    public Heat one() {
        return ONES.computeIfAbsent(this.fuel, item -> new Heat(1, item));
    }

    public Heat withPercent(int value) {
        double max;
        if (value <= 0) {
            return this.zero();
        } else if (value <= (max = getBurnDuration())) {
            return new Heat(value / max, this.fuel);
        } else {
            return this.one();
        }
    }

    public Heat withPercent(int value, ToIntFunction<ItemStack> getBurnDuration) {
        double max;
        if (value <= 0) {
            return this.zero();
        } else if (value <= (max = getBurnDuration(getBurnDuration))) {
            return new Heat(value / max, this.fuel);
        } else {
            return this.one();
        }
    }

    public Heat withFuel(Item fuel) {
        double max;
        double x;
        if (this.percent == 0) {
            return of(fuel).orElse(this);
        } else if ((max = AbstractFurnaceBlockEntity.getFuel().getOrDefault(fuel, 0)) > 0
                && (x = this.percent * max / getBurnDuration()) <= 1d) {
            return new Heat(x, fuel);
        } else {
            return this;
        }
    }

    public Heat withFuel(Item fuel, ToIntFunction<ItemStack> getBurnDuration) {
        double max;
        double x;
        if (this.percent == 0) {
            return of(fuel).orElse(this);
        } else if ((max = getBurnDuration.applyAsInt(new ItemStack(fuel))) > 0
                && (x = this.percent * max / getBurnDuration()) <= 1d) {
            return new Heat(x, fuel);
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
        } else if (object instanceof Heat that) {
            return this.percent == that.percent
                    && Objects.equals(this.fuel, that.fuel);
        } else {
            return false;
        }
    }

    public static final Optional<Heat> of(Item fuel) {
        if (AbstractFurnaceBlockEntity.getFuel().containsKey(fuel)) {
            return Optional.of(ZEROS.computeIfAbsent(fuel, item -> new Heat(0, item)));
        } else {
            return Optional.empty();
        }
    }

    public static final Optional<Heat> of(double percent, Item fuel) {
        if (percent == 0) {
            return of(fuel);
        } else if (percent > 0 && percent <= 0 && AbstractFurnaceBlockEntity.getFuel().containsKey(fuel)) {
            return Optional.of(new Heat(percent, fuel));
        } else {
            return Optional.empty();
        }
    }

    public static final Optional<Heat> of(int value, Item fuel) {
        double max;
        if (value == 0) {
            return of(fuel);
        } else if (value > 0 && (max = AbstractFurnaceBlockEntity.getFuel().getOrDefault(fuel, 0)) > 0
                && value <= max) {
            return Optional.of(new Heat(value / max, fuel));
        } else {
            return Optional.empty();
        }
    }

    public static final Optional<Heat> of(int value, Item fuel, ToIntFunction<ItemStack> getBurnDuration) {
        double max;
        if (value == 0) {
            return of(fuel);
        } else if (value > 0 && (max = getBurnDuration.applyAsInt(new ItemStack(fuel))) > 0 && value <= max) {
            return Optional.of(new Heat(value / max, fuel));
        } else {
            return Optional.empty();
        }
    }

    public static final Heat getMaxHeat() {
        if (maxFuel == null) {
            maxFuel = AbstractFurnaceBlockEntity.getFuel().entrySet().stream()
                .max((a, b) -> Integer.compare(a.getValue(), b.getValue()))
                .map(Map.Entry::getKey)
                .orElse(Items.LAVA_BUCKET);
        }
        return ONES.computeIfAbsent(maxFuel, item -> new Heat(1d, item));
    }
}
