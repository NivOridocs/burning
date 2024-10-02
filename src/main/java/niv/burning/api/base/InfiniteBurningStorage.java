package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;

public final class InfiniteBurningStorage implements BurningStorage {

    public static final InfiniteBurningStorage INSTANCE = new InfiniteBurningStorage();

    private InfiniteBurningStorage() {
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
    public Burning extract(Burning burning, TransactionContext transaction) {
        return burning;
    }

    @Override
    public Burning getBurning() {
        return Burning.MAX_VALUE;
    }
}
