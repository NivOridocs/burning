package niv.burning.api.base;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;
import niv.burning.impl.BurnDurationFunction;

public class SimpleBurningStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements BurningStorage {

    public static final String BURNING_TAG = "Burning";

    public static final record Snapshot(int currentBurning, int maxBurning, Burning zero) {
    }

    protected final BurnDurationFunction burnDuration;

    protected final BurningContext context;

    protected int currentBurning;

    protected int maxBurning;

    protected Burning zero;

    public SimpleBurningStorage() {
        this(null);
    }

    public SimpleBurningStorage(@Nullable BurnDurationFunction burnDuration) {
        this.burnDuration = burnDuration;
        this.context = this.burnDuration == null ? BurningContext.defaultInstance() : BurningContext.defaultWith(this.burnDuration);
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
        if (this.currentBurning <= value) {
            this.maxBurning = value;
        }
    }

    public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        Burning.parse(provider, compoundTag.get(BURNING_TAG)).ifPresent(burning -> {
            this.currentBurning = burning.getValue(this.context).intValue();
            this.maxBurning = burning.getBurnDuration(this.context);
            this.zero = burning.zero();
        });
    }

    public void save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.put(BURNING_TAG, this.getBurning(this.context).save(provider, new CompoundTag()));
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        context = burnDuration == null ? context : context.with(burnDuration);
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
        context = burnDuration == null ? context : context.with(burnDuration);
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
        context = burnDuration == null ? context : context.with(burnDuration);
        return this.zero.withValue(this.currentBurning, context);
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(this.currentBurning, this.maxBurning, this.zero);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.currentBurning = snapshot.currentBurning;
        this.maxBurning = snapshot.maxBurning;
        this.zero = snapshot.zero;
    }

    public static final SimpleBurningStorage getForBlockEntity(BlockEntity blockEntity) {
        return getForBlockEntity(blockEntity, null);
    }

    public static final SimpleBurningStorage getForBlockEntity(BlockEntity blockEntity,
            @Nullable BurnDurationFunction customBurnDuration) {
        return new SimpleBurningStorage(customBurnDuration) {
            @Override
            protected void onFinalCommit() {
                var pos = blockEntity.worldPosition;
                var level = blockEntity.level;
                var state = level.getBlockState(pos);
                var wasBurning = state.getOptionalValue(BlockStateProperties.LIT).orElse(Boolean.FALSE).booleanValue();
                var isBurning = this.getCurrentBurning() > 0;
                if (wasBurning != isBurning) {
                    state = state.trySetValue(BlockStateProperties.LIT, isBurning);
                    level.setBlockAndUpdate(pos, state);
                    BlockEntity.setChanged(level, pos, state);
                }
            }
        };
    }

    public static final ContainerData getDefaultContainerData(SimpleBurningStorage burningStorage) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                switch (index) {
                    case 0:
                        return burningStorage.getCurrentBurning();
                    case 1:
                        return burningStorage.getMaxBurning();
                    default:
                        return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0:
                        burningStorage.setCurrentBurning(value);
                        break;
                    case 1:
                        burningStorage.setMaxBurning(value);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }
}
