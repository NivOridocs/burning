package niv.burning.impl;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;

import java.util.Map;

import com.google.common.collect.MapMaker;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;
import niv.burning.impl.util.FieldExtra;

public class DynamicBurningStorage
        extends SnapshotParticipant<DynamicBurningStorage.Snapshot>
        implements BurningStorage {

    static final record Snapshot(double burning, double maxBurning, Burning zero) {
    }

    private static final record LevelPos(Level level, BlockPos pos) {
        @Override
        public String toString() {
            return DebugMessages.forGlobalPos(level, pos);
        }
    }

    private static final Map<LevelPos, DynamicBurningStorage> CACHE = new MapMaker()
            .concurrencyLevel(1)
            .weakValues()
            .makeMap();

    private final DynamicBurningStorageProvider provider;

    private final BlockEntity target;

    private Burning zero;

    DynamicBurningStorage(DynamicBurningStorageProvider provider, BlockEntity target) {
        this.provider = provider;
        this.target = target;
        this.zero = Burning.MIN_VALUE;
    }

    private double burning() {
        return FieldExtra.getInt(this.provider.litTime, this.target);
    }

    private void burning(double value) {
        FieldExtra.setInt(this.provider.litTime, this.target, (int) value);
    }

    private double maxBurning() {
        return FieldExtra.getInt(this.provider.litDuration, this.target);
    }

    private void maxBurning(double value) {
        FieldExtra.setInt(this.provider.litDuration, this.target, (int) value);
    }

    private int getBurnDuration(ItemStack stack) {
        return Burning.defaultBurnDuration(stack);
    }

    @Override
    public Burning insert(Burning burning, TransactionContext transaction) {
        double value = burning.doubleValue(this::getBurnDuration);
        int fuelTime = burning.getBurnDuration(this::getBurnDuration);
        double currentBurning = burning();
        double maxBurning = maxBurning();
        double delta = Math.min(Math.max(maxBurning, fuelTime) - currentBurning, value);
        updateSnapshots(transaction);
        currentBurning += delta;
        burning(currentBurning);
        if ((maxBurning > fuelTime && currentBurning <= fuelTime) || currentBurning > maxBurning) {
            maxBurning(fuelTime);
            this.zero = burning.zero();
        }
        return burning.withValue((int) (value - delta), this::getBurnDuration);
    }

    @Override
    public Burning extract(Burning burning, TransactionContext transaction) {
        double currentBurning = burning();
        double value = Math.min(currentBurning, burning.doubleValue(this::getBurnDuration));
        int fuelTime = burning.getBurnDuration(this::getBurnDuration);
        updateSnapshots(transaction);
        currentBurning -= value;
        burning(currentBurning);
        if (maxBurning() > fuelTime && currentBurning <= fuelTime) {
            maxBurning(fuelTime);
            this.zero = burning.zero();
        }
        return burning.withValue((int) value, this::getBurnDuration);
    }

    @Override
    public Burning getBurning() {
        double burning = burning();
        if (burning > zero.getBurnDuration(this::getBurnDuration)) {
            this.zero = Burning.MIN_VALUE;
        }
        return zero.withValue((int) burning);
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
        var state = this.target.level.getBlockState(this.target.worldPosition);
        var wasBurning = state.getValue(LIT).booleanValue();
        var isBurning = burning() > 0;
        if (wasBurning != isBurning) {
            state = state.setValue(LIT, isBurning);
            this.target.level.setBlockAndUpdate(this.target.worldPosition, state);
            BlockEntity.setChanged(this.target.level, this.target.worldPosition, state);
        }
    }

    static final BurningStorage of(Level level, BlockPos pos,
            DynamicBurningStorageProvider provider, BlockEntity entity) {
        return CACHE.computeIfAbsent(new LevelPos(level, pos), key -> new DynamicBurningStorage(provider, entity));
    }
}
