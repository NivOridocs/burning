package niv.heatlib.api.base;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.heatlib.api.Heat;
import niv.heatlib.api.HeatStorage;

public class SimpleHeatStorage
        extends SnapshotParticipant<SimpleHeatStorage.Snapshot>
        implements HeatStorage {

    public static final record Snapshot(int maxHeat, int currentHeat, Heat zero) {
    }

    protected final ToIntFunction<ItemStack> getBurnDuration;

    protected int maxHeat;

    protected int currentHeat;

    protected Heat zero;

    public SimpleHeatStorage() {
        this(SimpleHeatStorage::defaultBurnDuration);
    }

    public SimpleHeatStorage(@Nullable ToIntFunction<ItemStack> getBurnDuration) {
        this.getBurnDuration = getBurnDuration == null ? SimpleHeatStorage::defaultBurnDuration : getBurnDuration;
        this.maxHeat = 0;
        this.currentHeat = 0;
        this.zero = Heat.of(Items.COAL).get();
    }

    @Override
    public Heat getCurrentHeat() {
        return zero.withPercent(currentHeat, getBurnDuration);
    }

    @Override
    public Heat extract(Heat heat, TransactionContext transaction) {
        int value = Math.min(this.currentHeat, heat.intValue(this.getBurnDuration));
        int fuelTime = heat.getBurnDuration(this.getBurnDuration);
        updateSnapshots(transaction);
        this.currentHeat -= value;
        if (this.maxHeat > fuelTime && this.currentHeat <= fuelTime) {
            this.maxHeat = fuelTime;
            this.zero = heat.zero();
        }
        return heat.withPercent(value);
    }

    @Override
    public Heat insert(Heat heat, TransactionContext transaction) {
        int value = heat.intValue(this.getBurnDuration);
        int fuelTime = heat.getBurnDuration(this.getBurnDuration);
        int delta = Math.min(Math.max(this.maxHeat, fuelTime) - this.currentHeat, value);
        updateSnapshots(transaction);
        this.currentHeat += delta;
        if ((this.maxHeat > fuelTime && this.currentHeat <= fuelTime) || this.currentHeat > this.maxHeat) {
            this.maxHeat = fuelTime;
            this.zero = heat.zero();
        }
        return heat.withPercent(value - delta);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(maxHeat, currentHeat, zero);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.maxHeat = snapshot.maxHeat;
        this.currentHeat = snapshot.currentHeat;
        this.zero = snapshot.zero;
    }

    private static final int defaultBurnDuration(ItemStack stack) {
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
    }
}
