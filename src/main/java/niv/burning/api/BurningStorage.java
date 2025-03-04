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

    /**
     * Sided block access to burning storages.
     * The {@code Direction} parameter may be null, meaning that the full storage
     * (ignoring side restrictions) should be queried.
     * Refer to {@link BlockApiLookup} for documentation on how to use this field.
     */
    BlockApiLookup<BurningStorage, @Nullable Direction> SIDED = BlockApiLookup.get(
            ResourceLocation.tryBuild(BurningImpl.MOD_ID, "burning_storage"),
            BurningStorage.class, Direction.class);

    /**
     * Return false if calling {@link #insert} will absolutely always return a
     * zeroed {@link Burning}, or true otherwise or in doubt.
     *
     * <p>
     * Note: This function is meant to be used by pipes or other devices that can
     * transfer {@link Burning} to know if
     * they should interact with this storage at all.
     *
     * @return true if {@link #insert} can return something other than a zeroed
     *         {@link Burning}, false otherwise.
     */
    default boolean supportsInsertion() {
        return true;
    }

    /**
     * Try to insert the provided {@link Burning} into this storage.
     *
     * @param burning     The {@link Burning} to insert.
     * @param context     The provided {@link BurningContext}.
     * @param transaction The transaction this operation is part of.
     * @return Another instance of {@link Burning} with the same fuel and less or
     *         equal percentage than the one passed as argument: the amount that was
     *         inserted.
     */
    Burning insert(Burning burning, BurningContext context, TransactionContext transaction);

    /**
     * Return false if calling {@link #extract} will absolutely always return a
     * zeroed {@link Burning}, or true otherwise or in doubt.
     *
     * <p>
     * Note: This function is meant to be used by pipes or other devices that can
     * transfer {@link Burning} to know if
     * they should interact with this storage at all.
     *
     * @return true if {@link #extract} can return something other than a zeroed
     *         {@link Burning}, false otherwise.
     */
    default boolean supportsExtraction() {
        return true;
    }

    /**
     * Try to extract up to the provided {@link Burning} from this storage.
     *
     * @param burning     The {@link Burning} to extract.
     * @param context     The provided {@link BurningContext}.
     * @param transaction The transaction this operation is part of.
     * @return Another instance of {@link Burning} with the same fuel and less or
     *         equal percentage than the one passed as argument: the amount that was
     *         extracted.
     */
    Burning extract(Burning burning, BurningContext context, TransactionContext transaction);

    /**
     * Return the currently contained {@link Burning}.
     *
     * @param context The provided {@link BurningContext}.
     * @return the currently contained {@link Burning}.
     */
    Burning getBurning(BurningContext context);

    /**
     * Transfer {@link Burning} between two burning storages, and return the amount
     * that was successfully transferred.
     *
     * @param from        The source storage. May be null.
     * @param to          The target storage. May be null.
     * @param burning     The maximum burning that may be moved.
     * @param context     The provided {@link BurningContext}.
     * @param transaction The transaction this transfer is part of,
     *                    or {@code null} if a transaction should be opened just for
     *                    this transfer.
     * @return The amount of {@link Burning} that was successfully transferred.
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
