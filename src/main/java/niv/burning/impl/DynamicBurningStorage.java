package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.level.block.entity.BlockEntity;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;
import niv.burning.api.BurningStorageHelper;

@ApiStatus.Internal
public class DynamicBurningStorage
        extends SnapshotParticipant<DynamicBurningStorage.Snapshot>
        implements BurningStorage {

    static final record Snapshot(double burning, double maxBurning, Burning zero) {
    }

    private final DynamicBurningStorageProvider provider;

    private final BlockEntity target;

    private Burning zero;

    DynamicBurningStorage(DynamicBurningStorageProvider provider, BlockEntity target) {
        this.provider = provider;
        this.target = target;
        this.zero = Burning.MIN_VALUE;
    }

    private double burning() {
        return this.provider.litTime.get(target);
    }

    @SuppressWarnings("java:S3011")
    private void burning(double value) {
        this.provider.litTime.set(target, value);
    }

    private double maxBurning() {
        return this.provider.litDuration.get(target);
    }

    @SuppressWarnings("java:S3011")
    private void maxBurning(double value) {
        this.provider.litDuration.set(target, value);
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        double currentBurning = burning();
        double maxBurning = maxBurning();
        int fuelTime = burning.getBurnDuration(context);
        double value = Math.min(
                Math.max(maxBurning, fuelTime) - currentBurning,
                burning.getValue(context));
        updateSnapshots(transaction);
        currentBurning += value;
        burning(currentBurning);
        if ((maxBurning > fuelTime && currentBurning <= fuelTime) || currentBurning > maxBurning) {
            maxBurning(fuelTime);
            this.zero = burning.zero();
        }
        return burning.withValue((int) value, context);
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        double currentBurning = burning();
        int fuelTime = burning.getBurnDuration(context);
        double value = Math.min(currentBurning, burning.getValue(context));
        updateSnapshots(transaction);
        currentBurning -= value;
        burning(currentBurning);
        if (maxBurning() > fuelTime && currentBurning <= fuelTime) {
            maxBurning(fuelTime);
            this.zero = burning.zero();
        }
        return burning.withValue((int) value, context);
    }

    @Override
    public Burning getBurning(BurningContext context) {
        double burning = burning();
        if (burning > zero.getBurnDuration(context)) {
            this.zero = Burning.MIN_VALUE;
        }
        return zero.withValue((int) burning, context);
    }

    @Override
    public boolean isBurning() {
        return this.burning() > 0d;
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(burning(), maxBurning(), zero);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        burning(snapshot.burning);
        maxBurning(snapshot.maxBurning);
        this.zero = snapshot.zero;
    }

    @Override
    protected void onFinalCommit() {
        BurningStorageHelper.tryUpdateLitProperty(this.target, this);
        this.target.setChanged();
    }
}
