package niv.burning.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;
import niv.burning.api.base.SimpleBurningStorage;
import niv.burning.impl.AbstractFurnaceBurningStorages;
import niv.burning.impl.DefaultBurningContext;
import niv.burning.impl.DynamicBurningStorageProviders;
import niv.burning.impl.DynamicBurningStorages;

class BurningStorageTests {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void testSimpleBurningStorage() {
        testBurningStorage(SimpleBurningStorage::new);
        testBurningStorage(() -> new SimpleBurningStorage(i -> i / 2));
    }

    @Test
    void testAbstractFurnaceBurningStorage() {
        testBurningStorage(AbstractFurnaceBurningStorages::createFurnace);
        testBurningStorage(AbstractFurnaceBurningStorages::createBlastFurnace);
        testBurningStorage(AbstractFurnaceBurningStorages::createSmoker);
    }

    @Test
    void testDynamicBurningStorage() {
        assertNotNull(DynamicBurningStorageProviders.createFurnace());
        assertNotNull(DynamicBurningStorageProviders.createBlastFurnace());
        assertNotNull(DynamicBurningStorageProviders.createSmoker());

        testBurningStorage(DynamicBurningStorages::createFurnace);
        testBurningStorage(DynamicBurningStorages::createBlastFurnace);
        testBurningStorage(DynamicBurningStorages::createSmoker);
    }

    private void testBurningStorage(Supplier<? extends BurningStorage> constructor) {
        var context = DefaultBurningContext.instance();
        assertNotNull(context);

        var storage = constructor.get();

        assertTrue(storage.supportsInsertion());
        assertTrue(storage.supportsExtraction());
        assertEquals(0, storage.getBurning(context).getValue(context).intValue());

        final var coalOne = Burning.COAL.one();
        final var coal8 = Burning.COAL.withValue(800, context);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coalOne, storage.insert(coalOne, context, transaction));
            assertEquals(coal8, storage.extract(coal8, context, transaction));
            assertEquals(coal8, storage.getBurning(context));
        }

        assertEquals(0, storage.getBurning(context).getValue(context).intValue());

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coalOne, storage.insert(coalOne, context, transaction));
            assertEquals(coal8, storage.extract(coal8, context, transaction));
            assertEquals(coal8, storage.getBurning(context));

            transaction.commit();
        }

        assertEquals(coal8, storage.getBurning(context));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.insert(coalOne, context, transaction));
            assertEquals(coalOne, storage.getBurning(context));

            transaction.commit();
        }

        assertEquals(coalOne, storage.getBurning(context));

        final var blaze6 = Burning.BLAZE_ROD.withValue(600, context);
        final var blaze10 = Burning.BLAZE_ROD.withValue(1000, context);
        final var blaze12 = Burning.BLAZE_ROD.withValue(1200, context);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(blaze6, storage.insert(blaze6, context, transaction));
            assertEquals(blaze10, storage.extract(blaze10, context, transaction));
            assertEquals(blaze12, storage.getBurning(context));

            transaction.commit();
        }

        assertEquals(blaze12, storage.getBurning(context));

        testTransfer(constructor);
    }

    private void testTransfer(Supplier<? extends BurningStorage> constructor) {
        testTransfer(constructor, SimpleBurningStorage::new);
        testTransfer(constructor, () -> new SimpleBurningStorage(i -> i / 2));

        testTransfer(constructor, AbstractFurnaceBurningStorages::createFurnace);
        testTransfer(constructor, AbstractFurnaceBurningStorages::createBlastFurnace);
        testTransfer(constructor, AbstractFurnaceBurningStorages::createSmoker);

        testTransfer(constructor, DynamicBurningStorages::createFurnace);
        testTransfer(constructor, DynamicBurningStorages::createBlastFurnace);
        testTransfer(constructor, DynamicBurningStorages::createSmoker);
    }

    private void testTransfer(
            Supplier<? extends BurningStorage> sourceConstructor,
            Supplier<? extends BurningStorage> targetConstructor) {
        var context = DefaultBurningContext.instance();
        assertNotNull(context);

        var source = sourceConstructor.get();

        assertTrue(source.supportsInsertion());
        assertTrue(source.supportsExtraction());
        assertEquals(0, source.getBurning(context).getValue(context).intValue());

        final var blaze12 = Burning.BLAZE_ROD.withValue(1200, context);
        final var blaze10 = Burning.BLAZE_ROD.withValue(1000, context);
        final var blaze8 = Burning.BLAZE_ROD.withValue(800, context);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(blaze10, source.insert(blaze10, context, transaction));
            transaction.commit();
        }

        var target = targetConstructor.get();
        assertTrue(target.supportsInsertion());
        assertTrue(target.supportsExtraction());
        assertEquals(0, target.getBurning(context).getValue(context).intValue());

        try (var transaction = Transaction.openOuter()) {
            assertEquals(blaze10, BurningStorage.transfer(source, target, blaze12, context, transaction));
            transaction.commit();
        }

        try (var transaction = Transaction.openOuter()) {
            assertEquals(blaze8, BurningStorage.transfer(target, source, blaze8, context, transaction));
            transaction.commit();
        }
    }
}
