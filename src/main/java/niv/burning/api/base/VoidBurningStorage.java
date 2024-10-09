package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;

public final class VoidBurningStorage implements BurningStorage {

    public static final VoidBurningStorage INSTANCE = new VoidBurningStorage();

    private VoidBurningStorage() {
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public Burning insert(Burning burning, TransactionContext transaction) {
        return burning;
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
