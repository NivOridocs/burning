package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;

/**
 * A singleton {@link BurningStorage} that accepts all input but never stores anything and cannot be extracted from.
 */
public final class VoidBurningStorage implements BurningStorage {

    public static final VoidBurningStorage INSTANCE = new VoidBurningStorage();

    private VoidBurningStorage() {
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        return burning;
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        return burning.zero();
    }

    @Override
    public Burning getBurning(BurningContext context) {
        return Burning.MIN_VALUE;
    }

    @Override
    public void setBurning(Burning burning, BurningContext context) {
        // no-op
    }
}
