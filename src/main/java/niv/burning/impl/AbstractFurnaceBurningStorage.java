package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;

@ApiStatus.Internal
public class AbstractFurnaceBurningStorage
        extends SnapshotParticipant<Snapshot>
        implements BurningStorage {

    private final AbstractFurnaceBlockEntity target;

    AbstractFurnaceBurningStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
    }

    private Burning zero() {
        var fuel = ((FuelCache) this.target).burning_getFuel();
        return fuel == null ? Burning.MIN_VALUE : Burning.of(fuel, this.target::getBurnDuration);
    }

    private void zero(Burning zero) {
        ((FuelCache) this.target).burning_setFuel(zero.getFuel());
    }

    @Override
    public Burning insert(Burning burning, TransactionContext transaction) {
        int fuelTime = burning.getBurnDuration(this.target::getBurnDuration);
        int value = Math.min(
                Math.max(this.target.litDuration, fuelTime) - this.target.litTime,
                burning.intValue(this.target::getBurnDuration));
        updateSnapshots(transaction);
        this.target.litTime += value;
        if ((this.target.litDuration > fuelTime && this.target.litTime <= fuelTime)
                || this.target.litTime > this.target.litDuration) {
            this.target.litDuration = fuelTime;
            zero(burning.zero());
        }
        return burning.withValue(value, this.target::getBurnDuration);
    }

    @Override
    public Burning extract(Burning burning, TransactionContext transaction) {
        int fuelTime = burning.getBurnDuration(this.target::getBurnDuration);
        int value = Math.min(this.target.litTime, burning.intValue(this.target::getBurnDuration));
        updateSnapshots(transaction);
        this.target.litTime -= value;
        if (this.target.litDuration > fuelTime && this.target.litTime <= fuelTime) {
            this.target.litDuration = fuelTime;
            zero(burning.zero());
        }
        return burning.withValue(value, this.target::getBurnDuration);
    }

    @Override
    public Burning getBurning() {
        return this.zero().withValue(this.target.litTime, this.target::getBurnDuration);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(
                this.target.litTime,
                this.target.litDuration,
                this.zero());
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.target.litTime = snapshot.burning();
        this.target.litDuration = snapshot.maxBurning();
        this.zero(snapshot.zero());
    }

    @Override
    protected void onFinalCommit() {
        var state = this.target.level.getBlockState(this.target.worldPosition);
        var wasBurning = state.getValue(BlockStateProperties.LIT).booleanValue();
        var isBurning = this.target.litTime > 0;
        if (wasBurning != isBurning) {
            state = state.setValue(BlockStateProperties.LIT, isBurning);
            this.target.level.setBlockAndUpdate(this.target.worldPosition, state);
            BlockEntity.setChanged(this.target.level, this.target.worldPosition, state);
        }
    }
}
