package niv.burning.api.base;

import java.util.Objects;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;

public class ForwardingBurningStorage implements BurningStorage {

    protected final Supplier<BurningStorage> target;

    public ForwardingBurningStorage(BurningStorage target) {
        this(() -> target);
        Objects.requireNonNull(target);
    }

    public ForwardingBurningStorage(Supplier<BurningStorage> target) {
        this.target = Objects.requireNonNull(target);
    }

    @Override
    public boolean supportsInsertion() {
        return this.target.get().supportsInsertion();
    }

    @Override
    public Burning insert(Burning burning, TransactionContext transaction) {
        return this.target.get().insert(burning, transaction);
    }

    @Override
    public boolean supportsExtraction() {
        return this.target.get().supportsExtraction();
    }

    @Override
    public Burning extract(Burning burning, TransactionContext transaction) {
        return this.target.get().extract(burning, transaction);
    }

    @Override
    public Burning getBurning() {
        return this.target.get().getBurning();
    }
}
