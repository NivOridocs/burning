package niv.burning.api.base;

import java.util.function.IntUnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;

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
        if (this.currentBurning <= value) {
            this.maxBurning = value;
        }
    }

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
    public void setBurning(Burning burning, BurningContext context) {
        context = new Context(context, this.operator);
        this.currentBurning = burning.getValue(context).intValue();
        this.maxBurning = burning.getBurnDuration(context);
        this.zero = burning.zero();
    }

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

    /**
     * Creates a {@link SimpleBurningStorage} for the given block entity and
     * operator,
     * with automatic block state updates on commit.
     *
     * @param blockEntity the block entity to associate with the storage
     * @param operator    the {@link IntUnaryOperator} to apply to burning values
     *                    (e.g., for scaling or modifying burn times)
     * @return a new {@link SimpleBurningStorage} instance associated with the block
     *         entity and using the specified operator
     */
    public static final SimpleBurningStorage getForBlockEntity(BlockEntity blockEntity, IntUnaryOperator operator) {
        return new SimpleBurningStorage(operator) {
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

    /**
     * Returns a {@link ContainerData} view for the given burning storage, exposing
     * current and max burning values.
     *
     * @param burningStorage the storage to wrap
     * @return a {@link ContainerData} for use in menus or GUIs
     */
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

    protected static final class Context implements BurningContext {

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
