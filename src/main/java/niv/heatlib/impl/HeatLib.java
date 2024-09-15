package niv.heatlib.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.heatlib.api.Heat;
import niv.heatlib.api.HeatStorage;
import niv.heatlib.api.base.SimpleHeatStorage;
import niv.heatlib.impl.mixin.BlockEntityAccessor;

@ApiStatus.Internal
public class HeatLib implements ModInitializer {
    public static final String MOD_ID = "heatlib";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Initialize");

        HeatStorage.SIDED.registerForBlockEntity(HeatLib::getHeatStorage, BlockEntityType.FURNACE);
        HeatStorage.SIDED.registerForBlockEntity(HeatLib::getHeatStorage, BlockEntityType.BLAST_FURNACE);
        HeatStorage.SIDED.registerForBlockEntity(HeatLib::getHeatStorage, BlockEntityType.SMOKER);
    }

    public static final <T extends AbstractFurnaceBlockEntity> HeatStorage getHeatStorage(
            T that, @Nullable Direction dir) {
        return new SimpleHeatStorage(that::getBurnDuration) {

            @Override
            public Heat getCurrentHeat() {
                return this.zero.withPercent(that.litTime, this.getBurnDuration);
            }

            @Override
            public Heat extract(Heat heat, TransactionContext transaction) {
                int value = Math.min(that.litTime, heat.intValue(this.getBurnDuration));
                int fuelTime = heat.getBurnDuration(this.getBurnDuration);
                updateSnapshots(transaction);
                that.litTime -= value;
                if (that.litDuration > fuelTime && that.litTime <= fuelTime) {
                    that.litDuration = fuelTime;
                    this.zero = heat.zero();
                }
                return heat.withPercent(value);
            }

            @Override
            public Heat insert(Heat heat, TransactionContext transaction) {
                int value = heat.intValue(this.getBurnDuration);
                int fuelTime = heat.getBurnDuration(this.getBurnDuration);
                int delta = Math.min(Math.max(that.litDuration, fuelTime) - that.litTime, value);
                updateSnapshots(transaction);
                that.litTime += delta;
                if ((that.litDuration > fuelTime && that.litTime <= fuelTime) || that.litTime > that.litDuration) {
                    that.litDuration = fuelTime;
                    this.zero = heat.zero();
                }
                return heat.withPercent(value - delta);
            }

            @Override
            protected Snapshot createSnapshot() {
                return new Snapshot(that.litDuration, that.litTime, this.zero);
            }

            @Override
            protected void readSnapshot(Snapshot snapshot) {
                that.litTime = snapshot.currentHeat();
                that.litDuration = snapshot.maxHeat();
                this.zero = snapshot.zero();
            }

            @Override
            protected void onFinalCommit() {
                var state = that.level.getBlockState(that.worldPosition);
                var wasBurning = state.getValue(BlockStateProperties.LIT).booleanValue();
                var isBurning = that.litTime > 0;
                if (wasBurning != isBurning) {
                    state = state.setValue(BlockStateProperties.LIT, isBurning);
                    that.level.setBlockAndUpdate(that.worldPosition, state);
                    BlockEntityAccessor.invokeSetChanged(that.level, that.worldPosition, state);
                }
            }
        };
    }
}
