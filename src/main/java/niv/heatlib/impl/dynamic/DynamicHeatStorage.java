package niv.heatlib.impl.dynamic;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;

import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import niv.heatlib.api.Heat;
import niv.heatlib.api.HeatStorage;
import niv.heatlib.impl.mixin.BlockEntityAccessor;
import niv.heatlib.impl.util.FieldExtra;

public final class DynamicHeatStorage<T extends BlockEntity>
        extends SnapshotParticipant<DynamicHeatStorage.Snapshot>
        implements HeatStorage {

    static final record Snapshot(double maxHeat, double currentHeat, Heat zero) {
    }

    private final DynamicHeatStorageFactory<T> factory;

    private final T target;

    private Heat zero;

    DynamicHeatStorage(DynamicHeatStorageFactory<T> factory, T target) {
        this.factory = factory;
        this.target = target;
        this.zero = Heat.getMaxHeat().zero();
    }

    private double currentHeat() {
        return FieldExtra.getInt(this.factory.litTime, this.target);
    }

    private void currentHeat(double value) {
        FieldExtra.setInt(this.factory.litTime, this.target, (int) value);
    }

    private double maxHeat() {
        return FieldExtra.getInt(this.factory.litDuration, this.target);
    }

    private void maxHeat(double value) {
        FieldExtra.setInt(this.factory.litDuration, this.target, (int) value);
    }

    private int getBurnDuration(ItemStack stack) {
        return Heat.defaultBurnDuration(stack);
    }

    @Override
    public Heat insert(Heat heat, TransactionContext transaction) {
        double value = heat.doubleValue(this::getBurnDuration);
        int fuelTime = heat.getBurnDuration(this::getBurnDuration);
        double currentHeat = currentHeat();
        double maxHeat = maxHeat();
        double delta = Math.min(Math.max(maxHeat, fuelTime) - currentHeat, value);
        updateSnapshots(transaction);
        currentHeat += delta;
        currentHeat(currentHeat);
        if ((maxHeat > fuelTime && currentHeat <= fuelTime) || currentHeat > maxHeat) {
            maxHeat(fuelTime);
            this.zero = heat.zero();
        }
        return heat.withValue((int) (value - delta), this::getBurnDuration);
    }

    @Override
    public Heat extract(Heat heat, TransactionContext transaction) {
        double currentHeat = currentHeat();
        double value = Math.min(currentHeat, heat.doubleValue(this::getBurnDuration));
        int fuelTime = heat.getBurnDuration(this::getBurnDuration);
        updateSnapshots(transaction);
        currentHeat -= value;
        currentHeat(currentHeat);
        if (maxHeat() > fuelTime && currentHeat <= fuelTime) {
            maxHeat(fuelTime);
            this.zero = heat.zero();
        }
        return heat.withValue((int) value, this::getBurnDuration);
    }

    @Override
    public Heat getCurrentHeat() {
        if (maxHeat() != zero.getBurnDuration(this::getBurnDuration)) {
            var value = multiplyByLava(maxHeat());
            this.zero = AbstractFurnaceBlockEntity.getFuel().entrySet().stream()
                    .filter(entry -> entry.getValue() == value)
                    .map(Map.Entry::getKey).findFirst()
                    .flatMap(Heat::of).orElseGet(Heat::getMaxHeat)
                    .zero();
        }
        return zero.withValue((int) currentHeat());
    }

    private double multiplyByLava(double value) {
        return value * Heat.getMaxHeat().getBurnDuration()
                / Heat.getMaxHeat().getBurnDuration(this::getBurnDuration);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(maxHeat(), currentHeat(), zero);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        maxHeat(snapshot.maxHeat);
        currentHeat(snapshot.currentHeat);
        this.zero = snapshot.zero;
    }

    @Override
    protected void onFinalCommit() {
        var state = this.target.level.getBlockState(this.target.worldPosition);
        var wasBurning = state.getValue(LIT).booleanValue();
        var isBurning = currentHeat() > 0;
        if (wasBurning != isBurning) {
            state = state.setValue(LIT, isBurning);
            this.target.level.setBlockAndUpdate(this.target.worldPosition, state);
            BlockEntityAccessor.invokeSetChanged(this.target.level, this.target.worldPosition, state);
        }
    }
}
