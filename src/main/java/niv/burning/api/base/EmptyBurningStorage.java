package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;

/**
 * A singleton {@link BurningStorage} that is always empty and does not support insertion or extraction.
 */
public final class EmptyBurningStorage implements BurningStorage {

    public static final EmptyBurningStorage INSTANCE = new EmptyBurningStorage();

    private EmptyBurningStorage() {
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        return burning.zero();
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        return burning.zero();
    }

    @Override
    public Burning getBurning(BurningContext context) {
        return Burning.MIN_VALUE;
    }
}
