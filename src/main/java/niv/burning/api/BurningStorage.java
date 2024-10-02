package niv.burning.api;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import niv.burning.impl.BurningImpl;

public interface BurningStorage {

    BlockApiLookup<BurningStorage, @Nullable Direction> SIDED = BlockApiLookup.get(
            ResourceLocation.tryBuild(BurningImpl.MOD_ID, "burning_storage"),
            BurningStorage.class, Direction.class);

    default boolean supportsInsertion() {
        return true;
    }

    Burning insert(Burning burning, TransactionContext transaction);

    default boolean supportsExtraction() {
        return true;
    }

    Burning extract(Burning burning, TransactionContext transaction);

    Burning getBurning();

    public static Burning transfer(@Nullable BurningStorage from, @Nullable BurningStorage to,
            Burning burning, @Nullable TransactionContext transaction) {
        if (from != null && to != null) {
            Burning extracted;
            try (var test = Transaction.openNested(transaction)) {
                extracted = from.extract(burning, test);
            }
            try (var actual = Transaction.openNested(transaction)) {
                var inserted = to.insert(extracted, actual);
                if (Objects.equal(inserted, from.extract(inserted, actual))) {
                    actual.commit();
                    return inserted;
                }
            }
        }
        return burning.zero();
    }
}
