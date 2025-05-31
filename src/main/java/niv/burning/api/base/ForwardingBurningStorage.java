package niv.burning.api.base;

import java.util.Objects;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;

/**
 * A {@link BurningStorage} implementation that delegates all operations to another storage instance.
 * Useful for wrapping or dynamically forwarding to a target storage.
 */
public class ForwardingBurningStorage implements BurningStorage {

    protected final Supplier<? extends BurningStorage> target;

    public ForwardingBurningStorage(BurningStorage target) {
        this(() -> target);
        Objects.requireNonNull(target);
    }

    public ForwardingBurningStorage(Supplier<? extends BurningStorage> target) {
        this.target = Objects.requireNonNull(target);
    }

    @Override
    public boolean supportsInsertion() {
        return this.target.get().supportsInsertion();
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        return this.target.get().insert(burning, context, transaction);
    }

    @Override
    public boolean supportsExtraction() {
        return this.target.get().supportsExtraction();
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        return this.target.get().extract(burning, context, transaction);
    }

    @Override
    public Burning getBurning(BurningContext context) {
        return this.target.get().getBurning(context);
    }
}
