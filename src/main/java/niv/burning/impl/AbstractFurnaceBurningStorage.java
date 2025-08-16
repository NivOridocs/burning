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

public class AbstractFurnaceBurningStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements BurningStorage {

    private final AbstractFurnaceBlockEntity target;

    public AbstractFurnaceBurningStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
    }

    private Burning getZero() {
        var fuel = this.target.burning_getFuel();
        return fuel == null ? Burning.MIN_VALUE : Burning.ofZero(fuel);
    }

    private void setZero(Burning zero) {
        this.target.burning_setFuel(zero.getFuel());
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        context = new Context(this.target, context);
        int fuelTime = burning.getBurnDuration(context);
        int value = Math.min(
                Math.max(this.target.litTotalTime, fuelTime) - this.target.litTimeRemaining,
                burning.getValue(context).intValue());
        updateSnapshots(transaction);
        this.target.litTimeRemaining += value;
        if ((this.target.litTotalTime > fuelTime && this.target.litTimeRemaining <= fuelTime)
                || this.target.litTimeRemaining > this.target.litTotalTime) {
            this.target.litTotalTime = fuelTime;
            setZero(burning);
        }
        return burning.withValue(value, context);
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        context = new Context(this.target, context);
        int fuelTime = burning.getBurnDuration(context);
        int value = Math.min(this.target.litTimeRemaining, burning.getValue(context).intValue());
        updateSnapshots(transaction);
        this.target.litTimeRemaining -= value;
        if (this.target.litTotalTime > fuelTime && this.target.litTimeRemaining <= fuelTime) {
            this.target.litTotalTime = fuelTime;
            this.setZero(burning);
        }
        return burning.withValue(value, context);
    }

    @Override
    public Burning getBurning(BurningContext context) {
        context = new Context(this.target, context);
        return this.getZero().withValue(this.target.litTimeRemaining, context);
    }

    @Override
    public void setBurning(Burning burning, BurningContext context) {
        context = new Context(this.target, context);
        this.target.litTimeRemaining = burning.getValue(context).intValue();
        this.target.litTotalTime = burning.getBurnDuration(context);
        this.setZero(burning);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(
                this.target.litTimeRemaining,
                this.target.litTotalTime,
                this.getZero());
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.target.litTimeRemaining = snapshot.currentBurning();
        this.target.litTotalTime = snapshot.maxBurning();
        this.setZero(snapshot.zero());
    }

    @Override
    protected void onFinalCommit() {
        var state = this.target.level.getBlockState(this.target.worldPosition);
        var wasBurning = state.getValue(BlockStateProperties.LIT).booleanValue();
        var isBurning = this.target.litTimeRemaining > 0;
        if (wasBurning != isBurning) {
            state = state.setValue(BlockStateProperties.LIT, isBurning);
            this.target.level.setBlockAndUpdate(this.target.worldPosition, state);
            BlockEntity.setChanged(this.target.level, this.target.worldPosition, state);
        }
    }

    protected final class Context implements BurningContext {

        private final AbstractFurnaceBlockEntity target;

        private final BurningContext source;

        public Context(AbstractFurnaceBlockEntity target, BurningContext context) {
            this.target = target;
            this.source = context;
        }

        @Override
        public boolean isFuel(Item item) {
            return this.source.isFuel(new ItemStack(item));
        }

        @Override
        public boolean isFuel(ItemStack itemStack) {
            return this.source.isFuel(itemStack);
        }

        @Override
        public int burnDuration(Item item) {
            return this.target.getBurnDuration(new ForwardingFuelValues(this.source), new ItemStack(item));
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return this.target.getBurnDuration(new ForwardingFuelValues(this.source), itemStack);
        }
    }
}
