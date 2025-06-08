package niv.burning.impl;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;
import niv.burning.api.base.SimpleBurningStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;

/**
 * A {@link BurningStorage} implementation for
 * {@link AbstractFurnaceBlockEntity}.
 * Handles insertion, extraction, and state management for furnace-like block
 * entities.
 */
public class AbstractFurnaceBurningStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements BurningStorage {

    private final AbstractFurnaceBlockEntity target;

    private final AbstractFurnaceBurningContext targetContext;

    /**
     * Constructs a storage wrapper for the given furnace block entity.
     *
     * @param target the furnace block entity to wrap
     */
    public AbstractFurnaceBurningStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
        this.targetContext = new AbstractFurnaceBurningContext(this.target);
    }

    private Burning getZero() {
        var fuel = this.target.burning_getFuel();
        return fuel == null ? Burning.MIN_VALUE : Burning.of(fuel, this.targetContext);
    }

    private void setZero(Burning zero) {
        this.target.burning_setFuel(zero.getFuel());
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        int fuelTime = burning.getBurnDuration(this.targetContext);
        int value = Math.min(
                Math.max(this.target.litDuration, fuelTime) - this.target.litTime,
                burning.getValue(this.targetContext).intValue());
        updateSnapshots(transaction);
        this.target.litTime += value;
        if ((this.target.litDuration > fuelTime && this.target.litTime <= fuelTime)
                || this.target.litTime > this.target.litDuration) {
            this.target.litDuration = fuelTime;
            setZero(burning);
        }
        return burning.withValue(value, this.targetContext);
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        int fuelTime = burning.getBurnDuration(this.targetContext);
        int value = Math.min(this.target.litTime, burning.getValue(this.targetContext).intValue());
        updateSnapshots(transaction);
        this.target.litTime -= value;
        if (this.target.litDuration > fuelTime && this.target.litTime <= fuelTime) {
            this.target.litDuration = fuelTime;
            setZero(burning);
        }
        return burning.withValue(value, this.targetContext);
    }

    @Override
    public Burning getBurning(BurningContext context) {
        return this.getZero().withValue(this.target.litTime, this.targetContext);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(
                this.target.litTime,
                this.target.litDuration,
                this.getZero());
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.target.litTime = snapshot.currentBurning();
        this.target.litDuration = snapshot.maxBurning();
        this.setZero(snapshot.zero());
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

    private static final class AbstractFurnaceBurningContext implements BurningContext {

        private final AbstractFurnaceBlockEntity target;

        public AbstractFurnaceBurningContext(AbstractFurnaceBlockEntity target) {
            this.target = target;
        }

        @Override
        public boolean isFuel(Item item) {
            return AbstractFurnaceBlockEntity.getFuel().containsKey(item);
        }

        @Override
        public boolean isFuel(ItemStack itemStack) {
            return AbstractFurnaceBlockEntity.getFuel().containsKey(itemStack.getItem());
        }

        @Override
        public int burnDuration(Item item) {
            return this.target.getBurnDuration(new ItemStack(item));
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return this.target.getBurnDuration(itemStack);
        }
    }
}
