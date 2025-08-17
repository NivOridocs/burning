package niv.burning.example;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;
import niv.burning.api.BurningStorageHelper;
import niv.burning.api.BurningStorageListener;
import niv.burning.api.base.SimpleBurningStorage;
import niv.burning.impl.FuelValuesBurningContext;

public class MyBlockEntity extends BlockEntity implements BurningStorageListener {

    // What you need
    private static final BlockEntityType<MyBlockEntity> MY_BLOCK_ENTITY = null;

    // Add a simple burning storage to the entity
    public final SimpleBurningStorage simpleBurningStorage = new SimpleBurningStorage();

    public MyBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MY_BLOCK_ENTITY, blockPos, blockState);

        this.simpleBurningStorage.addListener(this);
    }

    // And don't forget to add the following
    @Override
    public void burningStorageChanged(BurningStorage burningStorage) {
        BurningStorageHelper.tryUpdateLitProperty(this, burningStorage);
        this.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        // ...
        valueInput
                .read("Burning", SimpleBurningStorage.SNAPSHOT_CODEC)
                .ifPresent(this.simpleBurningStorage::readSnapshot);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        // ...
        valueOutput
                .store("Burning", SimpleBurningStorage.SNAPSHOT_CODEC, this.simpleBurningStorage.createSnapshot());
    }

    public final void useBurningStorage() {
        this.simpleBurningStorage.getCurrentBurning(); // as the `litTimeRemaining` equivalent
        this.simpleBurningStorage.setCurrentBurning(800);
        this.simpleBurningStorage.getMaxBurning(); // as the `litTotalTime` equivalent
        this.simpleBurningStorage.setMaxBurning(1600);

        // What you need
        Level level = this.level;

        BurningStorage source = null, target = null;

        // Before 1.21.2, create a burning context, you can use the SimpleBurningContext class or implement one yourself
        BurningContext context1 = null;

        // After 1.21.2, you can also use the FuelValuesBurningContext wrapper class
        BurningContext context = new FuelValuesBurningContext(level.fuelValues());

        // Create the maximum amount of burning fuel to transfer, for instance, half a COAL worth of burning fuel
        Burning burning1 = Burning.of(Items.COAL, context).withValue(800, context);
        // or if you are using COAL, BLAZE_ROD, or LAVA_BUCKET
        Burning burning = Burning.COAL.withValue(800, context);

        // How to
        Burning transferred = BurningStorage.transfer(
                source, // transfer from this storage
                target, // to this storage
                burning, // up to this amount of burning fuel
                context, // with this burning context
                null); // creating a new transaction in doing so
        // `transferred` will have the same fuel as `burning`

        try (Transaction transaction = Transaction.openOuter()) {
            Burning transferred1 = BurningStorage.transfer(source, target, burning, context, transaction);
            if (burning.equals(transferred)) {
                transaction.commit();
            }
        }
    }

    public static final void registerBurningStorage() {
        // How to
        BurningStorage.SIDED.registerForBlockEntity(
                (myBlockEntity, direction) -> myBlockEntity.simpleBurningStorage, MY_BLOCK_ENTITY);
    }
}
