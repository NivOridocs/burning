package niv.burning.api;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import niv.burning.impl.BurningImpl;

/**
 * Represents a storage for {@link Burning} fuel values, supporting insertion, extraction,
 * and querying of burning state. Implementations may represent block entities or other
 * in-world objects that can store and transfer burning energy.
 */
public interface BurningStorage {

    /**
     * Sided block access to burning storages.
     * <p>
     * The {@code Direction} parameter may be null, meaning that the full storage
     * (ignoring side restrictions) should be queried.
     * Refer to {@link BlockApiLookup} for documentation on how to use this field.
     * </p>
     */
    BlockApiLookup<BurningStorage, @Nullable Direction> SIDED = BlockApiLookup.get(
            ResourceLocation.tryBuild(BurningImpl.MOD_ID, "burning_storage"),
            BurningStorage.class, Direction.class);

    /**
     * Indicates whether this storage supports insertion of {@link Burning} values.
     * <p>
     * Returns false if calling {@link #insert} will always return a zeroed {@link Burning},
     * true otherwise or in doubt.
     * <p>
     * Note: This function is meant to be used by pipes or other devices that can
     * transfer {@link Burning} to know if they should interact with this storage at all.
     *
     * @return true if {@link #insert} can return something other than a zeroed
     *         {@link Burning}, false otherwise
     */
    default boolean supportsInsertion() {
        return true;
    }

    /**
     * Attempts to insert the provided {@link Burning} into this storage.
     *
     * @param burning     the {@link Burning} to insert
     * @param context     the {@link BurningContext} to use
     * @param transaction the transaction this operation is part of
     * @return an instance of {@link Burning} with the same fuel and less than or equal percentage
     *         than the one passed as argument: the amount that was inserted
     */
    Burning insert(Burning burning, BurningContext context, TransactionContext transaction);

    /**
     * Indicates whether this storage supports extraction of {@link Burning} values.
     * <p>
     * Returns false if calling {@link #extract} will always return a zeroed {@link Burning},
     * true otherwise or in doubt.
     * <p>
     * Note: This function is meant to be used by pipes or other devices that can
     * transfer {@link Burning} to know if they should interact with this storage at all.
     *
     * @return true if {@link #extract} can return something other than a zeroed
     *         {@link Burning}, false otherwise
     */
    default boolean supportsExtraction() {
        return true;
    }

    /**
     * Attempts to extract up to the provided {@link Burning} from this storage.
     *
     * @param burning     the {@link Burning} to extract
     * @param context     the {@link BurningContext} to use
     * @param transaction the transaction this operation is part of
     * @return an instance of {@link Burning} with the same fuel and less than or equal percentage
     *         than the one passed as argument: the amount that was extracted
     */
    Burning extract(Burning burning, BurningContext context, TransactionContext transaction);

    /**
     * Returns the currently contained {@link Burning} in this storage.
     *
     * @param context the {@link BurningContext} to use
     * @return the currently contained {@link Burning}
     */
    Burning getBurning(BurningContext context);

    /**
     * Transfers {@link Burning} between two burning storages, and returns the amount
     * that was successfully transferred.
     *
     * @param from        the source storage (may be null)
     * @param to          the target storage (may be null)
     * @param burning     the maximum burning that may be moved
     * @param context     the {@link BurningContext} to use
     * @param transaction the transaction this transfer is part of,
     *                    or {@code null} if a transaction should be opened just for
     *                    this transfer
     * @return the amount of {@link Burning} that was successfully transferred
     */
    public static Burning transfer(
            @Nullable BurningStorage from, @Nullable BurningStorage to,
            Burning burning, BurningContext context, @Nullable TransactionContext transaction) {
        if (from != null && to != null) {
            Burning extracted;
            try (var test = Transaction.openNested(transaction)) {
                extracted = from.extract(burning, context, test);
            }
            try (var actual = Transaction.openNested(transaction)) {
                var inserted = to.insert(extracted, context, actual);
                if (Objects.equal(inserted, from.extract(inserted, context, actual))) {
                    actual.commit();
                    return inserted;
                }
            }
        }
        return burning.zero();
    }
}
