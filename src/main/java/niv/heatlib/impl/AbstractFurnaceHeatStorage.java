package niv.heatlib.impl;

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
import niv.heatlib.api.Heat;
import niv.heatlib.api.HeatStorage;

@ApiStatus.Internal
public class AbstractFurnaceHeatStorage
        extends SnapshotParticipant<AbstractFurnaceHeatStorage.Snapshot>
        implements HeatStorage {

    static final record Snapshot(int maxHeat, int currentHeat, Heat zero) {
    }

    private static final record LevelPos(Level level, BlockPos pos) {
        @Override
        public String toString() {
            return DebugMessages.forGlobalPos(level, pos);
        }
    }

    private static final Map<LevelPos, AbstractFurnaceHeatStorage> CACHE = new MapMaker()
            .concurrencyLevel(1)
            .weakValues()
            .makeMap();

    private final AbstractFurnaceBlockEntity target;

    private Heat zero;

    AbstractFurnaceHeatStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
        this.zero = Heat.getMaxHeat().zero();
    }

    @Override
    public Heat insert(Heat heat, TransactionContext transaction) {
        int value = heat.intValue(this.target::getBurnDuration);
        int fuelTime = heat.getBurnDuration(this.target::getBurnDuration);
        int delta = Math.min(Math.max(this.target.litDuration, fuelTime) - this.target.litTime, value);
        updateSnapshots(transaction);
        this.target.litTime += delta;
        if ((this.target.litDuration > fuelTime && this.target.litTime <= fuelTime)
                || this.target.litTime > this.target.litDuration) {
            this.target.litDuration = fuelTime;
            this.zero = heat.zero();
        }
        return heat.withValue(value - delta, this.target::getBurnDuration);
    }

    @Override
    public Heat extract(Heat heat, TransactionContext transaction) {
        int value = Math.min(this.target.litTime, heat.intValue(this.target::getBurnDuration));
        int fuelTime = heat.getBurnDuration(this.target::getBurnDuration);
        updateSnapshots(transaction);
        this.target.litTime -= value;
        if (this.target.litDuration > fuelTime && this.target.litTime <= fuelTime) {
            this.target.litDuration = fuelTime;
            this.zero = heat.zero();
        }
        return heat.withValue(value, this.target::getBurnDuration);
    }

    @Override
    public Heat getCurrentHeat() {
        if (this.target.litDuration != zero.getBurnDuration(this.target::getBurnDuration)) {
            int value = multiplyByLava(this.target.litDuration);
            this.zero = AbstractFurnaceBlockEntity.getFuel().entrySet().stream()
                    .filter(entry -> entry.getValue() == value)
                    .map(Map.Entry::getKey).findFirst()
                    .flatMap(Heat::of).orElseGet(Heat::getMaxHeat)
                    .zero();
        }
        return zero.withValue(this.target.litTime, this.target::getBurnDuration);
    }

    private int multiplyByLava(int value) {
        return value * Heat.getMaxHeat().getBurnDuration()
                / Heat.getMaxHeat().getBurnDuration(this.target::getBurnDuration);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(
                this.target.litDuration,
                this.target.litTime,
                this.zero);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.target.litTime = snapshot.currentHeat();
        this.target.litDuration = snapshot.maxHeat();
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
    static final HeatStorage find(
            Level level, BlockPos pos, BlockState state, BlockEntity blockEntity, Direction direction) {
        if (blockEntity instanceof AbstractFurnaceBlockEntity entity) {
            return CACHE.computeIfAbsent(new LevelPos(level, pos), key -> new AbstractFurnaceHeatStorage(entity));
        } else {
            return null;
        }
    }
}
