package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;

public final class EmptyBurningStorage implements BurningStorage {

    public static final EmptyBurningStorage INSTANCE = new EmptyBurningStorage();

    private EmptyBurningStorage() {
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public Burning insert(Burning burning, TransactionContext transaction) {
        return burning.zero();
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public Burning extract(Burning burning, TransactionContext transaction) {
        return burning.zero();
    }

    @Override
    public Burning getBurning() {
        return Burning.MIN_VALUE;
    }
}
