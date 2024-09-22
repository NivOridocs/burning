package niv.heatlib.impl.event;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.heatlib.api.Heat;
import niv.heatlib.api.HeatStorage;
import niv.heatlib.impl.mixin.BlockEntityAccessor;

@ApiStatus.Internal
public final class BoundHeatStorage
        extends SnapshotParticipant<BoundHeatStorage.Snapshot>
        implements HeatStorage {

    static final record Snapshot(int maxHeat, int currentHeat) {
    }

    private final AbstractFurnaceBlockEntity target;

    BoundHeatStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
    }

    @Override
    public Heat insert(Heat heat, TransactionContext transaction) {
        int value = heat.intValue(target::getBurnDuration);
        int fuelTime = heat.getBurnDuration(target::getBurnDuration);
        int delta = Math.min(Math.max(target.litDuration, fuelTime) - target.litTime, value);
        updateSnapshots(transaction);
        target.litTime += delta;
        if ((target.litDuration > fuelTime && target.litTime <= fuelTime) || target.litTime > target.litDuration) {
            target.litDuration = fuelTime;
        }
        return heat.withPercent(value - delta);
    }

    @Override
    public Heat extract(Heat heat, TransactionContext transaction) {
        int value = Math.min(target.litTime, heat.intValue(target::getBurnDuration));
        int fuelTime = heat.getBurnDuration(target::getBurnDuration);
        updateSnapshots(transaction);
        target.litTime -= value;
        if (target.litDuration > fuelTime && target.litTime <= fuelTime) {
            target.litDuration = fuelTime;
        }
        return heat.withPercent(value);
    }

    @Override
    public Heat getCurrentHeat() {
        return Heat.getMaxHeat().withPercent(target.litTime, target::getBurnDuration);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(target.litDuration, target.litTime);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        target.litTime = snapshot.currentHeat();
        target.litDuration = snapshot.maxHeat();
    }

    @Override
    protected void onFinalCommit() {
        var state = target.level.getBlockState(target.worldPosition);
        var wasBurning = state.getValue(BlockStateProperties.LIT).booleanValue();
        var isBurning = target.litTime > 0;
        if (wasBurning != isBurning) {
            state = state.setValue(BlockStateProperties.LIT, isBurning);
            target.level.setBlockAndUpdate(target.worldPosition, state);
            BlockEntityAccessor.invokeSetChanged(target.level, target.worldPosition, state);
        }
    }
}
