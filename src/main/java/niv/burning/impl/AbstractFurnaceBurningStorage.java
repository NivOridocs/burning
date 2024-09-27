package niv.burning.impl;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.MapMaker;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;

@ApiStatus.Internal
public class AbstractFurnaceBurningStorage
        extends SnapshotParticipant<Snapshot>
        implements BurningStorage {

    private static final record LevelPos(Level level, BlockPos pos) {
        @Override
        public String toString() {
            return DebugMessages.forGlobalPos(level, pos);
        }
    }

    private static final Map<LevelPos, AbstractFurnaceBurningStorage> CACHE = new MapMaker()
            .concurrencyLevel(1).weakValues().makeMap();

    private final AbstractFurnaceBlockEntity target;

    private Burning zero;

    AbstractFurnaceBurningStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
        this.zero = Burning.MIN_VALUE;
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
            this.zero = burning.zero();
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
            this.zero = burning.zero();
        }
        return burning.withValue(value, this.target::getBurnDuration);
    }

    @Override
    public Burning getBurning() {
        if (this.target.litTime > zero.getBurnDuration(this.target::getBurnDuration)) {
            this.zero = Burning.MIN_VALUE;
        }
        return zero.withValue(this.target.litTime, this.target::getBurnDuration);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(
                this.target.litTime,
                this.target.litDuration,
                this.zero);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.target.litTime = snapshot.burning();
        this.target.litDuration = snapshot.maxBurning();
        this.zero = snapshot.zero();
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

    @SuppressWarnings("java:S1172")
    static final BurningStorage find(
            Level level, BlockPos pos, BlockState state, BlockEntity blockEntity, Direction direction) {
        if (blockEntity instanceof AbstractFurnaceBlockEntity entity) {
            return CACHE.computeIfAbsent(new LevelPos(level, pos), key -> new AbstractFurnaceBurningStorage(entity));
        } else {
            return null;
        }
    }
}
