package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
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
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        return burning.zero();
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        return burning;
    }

    @Override
    public Burning getBurning(BurningContext context) {
        return Burning.MAX_VALUE;
    }
}
