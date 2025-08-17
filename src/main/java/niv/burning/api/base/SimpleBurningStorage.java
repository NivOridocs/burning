package niv.burning.api.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.IntUnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;
import niv.burning.api.BurningStorageListener;

/**
 * A basic {@link BurningStorage} implementation that tracks burning state and
 * supports snapshotting.
 * Can be used for simple block entities or as a utility for custom burning
 * logic.
 */
public class SimpleBurningStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements BurningStorage {

    public static final Codec<Snapshot> SNAPSHOT_CODEC = Codec
            .lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("currentBurning").forGetter(Snapshot::currentBurning),
                    Codec.INT.fieldOf("maxBurning").forGetter(Snapshot::maxBurning),
                    Burning.ZERO_CODEC.fieldOf("zero").forGetter(Snapshot::zero))
                    .apply(instance, Snapshot::new)));

    public static final record Snapshot(int currentBurning, int maxBurning, Burning zero) {
    }

    protected final IntUnaryOperator operator;

    protected int currentBurning;

    protected int maxBurning;

    protected Burning zero;

    private Collection<BurningStorageListener> listeners;

    public SimpleBurningStorage() {
        this(null);
    }

    public SimpleBurningStorage(IntUnaryOperator operator) {
        this.operator = operator == null ? IntUnaryOperator.identity() : operator;
        this.currentBurning = 0;
        this.maxBurning = 0;
        this.zero = Burning.MIN_VALUE;
    }

    public int getCurrentBurning() {
        return currentBurning;
    }

    public void setCurrentBurning(int value) {
        this.currentBurning = Math.clamp(value, 0, this.maxBurning);
    }

    public int getMaxBurning() {
        return maxBurning;
    }

    public void setMaxBurning(int value) {
        this.maxBurning = Math.max(0, value);
        if (this.currentBurning > this.maxBurning) {
            this.currentBurning = this.maxBurning;
        }
    }

    public void addListener(BurningStorageListener burningStorageListener) {
        if (this.listeners == null)
            this.listeners = new ArrayList<>();
        this.listeners.add(burningStorageListener);
    }

    public void removeListener(BurningStorageListener burningStorageListener) {
        if (this.listeners != null)
            this.listeners.remove(burningStorageListener);
    }

	protected void setChanged() {
        if (this.listeners != null)
            for (var burningStorageListener : this.listeners)
                burningStorageListener.burningStorageChanged(this);
    }

    // From {@link SnapshotParticipant}

    @Override
    public Snapshot createSnapshot() {
        return new Snapshot(this.currentBurning, this.maxBurning, this.zero);
    }

    @Override
    public void readSnapshot(Snapshot snapshot) {
        this.currentBurning = snapshot.currentBurning;
        this.maxBurning = snapshot.maxBurning;
        this.zero = snapshot.zero;
    }

    @Override
    protected void onFinalCommit() {
        this.setChanged();
    }

    // From {@link BurningStorage}

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        context = new Context(context, this.operator);
        int fuelTime = burning.getBurnDuration(context);
        int value = Math.min(
                Math.max(this.maxBurning, fuelTime) - this.currentBurning,
                burning.getValue(context).intValue());
        updateSnapshots(transaction);
        this.currentBurning += value;
        if ((this.maxBurning > fuelTime && this.currentBurning <= fuelTime) || this.currentBurning > this.maxBurning) {
            this.maxBurning = fuelTime;
            this.zero = burning.zero();
        }
        return burning.withValue(value, context);
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        context = new Context(context, this.operator);
        int fuelTime = burning.getBurnDuration(context);
        int value = Math.min(this.currentBurning, burning.getValue(context).intValue());
        updateSnapshots(transaction);
        this.currentBurning -= value;
        if (this.maxBurning > fuelTime && this.currentBurning <= fuelTime) {
            this.maxBurning = fuelTime;
            this.zero = burning.zero();
        }
        return burning.withValue(value, context);
    }

    @Override
    public Burning getBurning(BurningContext context) {
        context = new Context(context, this.operator);
        return this.zero.withValue(this.currentBurning, context);
    }

    @Override
    public boolean isBurning() {
        return this.currentBurning > 0;
    }

    private static final class Context implements BurningContext {

        private final BurningContext source;

        private final IntUnaryOperator operator;

        public Context(BurningContext source, IntUnaryOperator operator) {
            this.source = source;
            this.operator = operator;
        }

        @Override
        public boolean isFuel(ItemStack itemStack) {
            return this.source.isFuel(itemStack);
        }

        @Override
        public boolean isFuel(Item item) {
            return this.source.isFuel(item);
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return this.operator.applyAsInt(this.source.burnDuration(itemStack));
        }

        @Override
        public int burnDuration(Item item) {
            return this.operator.applyAsInt(this.source.burnDuration(item));
        }
    }
}
