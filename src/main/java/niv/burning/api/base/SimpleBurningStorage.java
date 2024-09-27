package niv.burning.api.base;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;

public class SimpleBurningStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements BurningStorage {

    public static final record Snapshot(int burning, int maxBurning, Burning zero) {
    }

    protected final ToIntFunction<ItemStack> getBurnDuration;

    protected int burning;

    protected int maxBurning;

    protected Burning zero;

    public SimpleBurningStorage() {
        this(null);
    }

    public SimpleBurningStorage(@Nullable ToIntFunction<ItemStack> getBurnDuration) {
        this.getBurnDuration = getBurnDuration == null ? Burning::defaultBurnDuration : getBurnDuration;
        this.burning = 0;
        this.maxBurning = 0;
        this.zero = Burning.MIN_VALUE;
    }

    @Override
    public Burning insert(Burning burning, TransactionContext transaction) {
        int fuelTime = burning.getBurnDuration(this.getBurnDuration);
        int value = Math.min(
                Math.max(this.maxBurning, fuelTime) - this.burning,
                burning.intValue(this.getBurnDuration));
        updateSnapshots(transaction);
        this.burning += value;
        if ((this.maxBurning > fuelTime && this.burning <= fuelTime) || this.burning > this.maxBurning) {
            this.maxBurning = fuelTime;
            this.zero = burning.zero();
        }
        return burning.withValue(value, this.getBurnDuration);
    }

    @Override
    public Burning extract(Burning burning, TransactionContext transaction) {
        int fuelTime = burning.getBurnDuration(this.getBurnDuration);
        int value = Math.min(this.burning, burning.intValue(this.getBurnDuration));
        updateSnapshots(transaction);
        this.burning -= value;
        if (this.maxBurning > fuelTime && this.burning <= fuelTime) {
            this.maxBurning = fuelTime;
            this.zero = burning.zero();
        }
        return burning.withValue(value, this.getBurnDuration);
    }

    @Override
    public Burning getBurning() {
        return zero.withValue(this.burning, this.getBurnDuration);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(this.burning, this.maxBurning, this.zero);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.burning = snapshot.burning;
        this.maxBurning = snapshot.maxBurning;
        this.zero = snapshot.zero;
    }
}
