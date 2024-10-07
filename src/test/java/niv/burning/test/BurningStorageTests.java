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
import net.minecraft.world.level.block.Blocks;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;
import niv.burning.api.base.SimpleBurningStorage;
import niv.burning.impl.AbstractFurnaceBurningStorages;
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
        testBurningStorage(() -> new SimpleBurningStorage(stack -> Burning.defaultBurnDuration(stack) / 2));
    }

    @Test
    void testAbstractFurnaceBurningStorage() {
        testBurningStorage(AbstractFurnaceBurningStorages::newFurnaceInstance);
        testBurningStorage(AbstractFurnaceBurningStorages::newBlastFurnaceInstance);
        testBurningStorage(AbstractFurnaceBurningStorages::newSmokerInstance);
    }

    @Test
    void testDynamicBurningStorage() {
        final var provider1 = DynamicBurningStorageProviders.newFurnaceInstance();
        assertNotNull(provider1);
        testBurningStorage(() -> DynamicBurningStorages.newInstance(provider1, Blocks.FURNACE));

        final var provider2 = DynamicBurningStorageProviders.newBlastFurnaceInstance();
        assertNotNull(provider2);
        testBurningStorage(() -> DynamicBurningStorages.newInstance(provider2, Blocks.BLAST_FURNACE));

        final var provider3 = DynamicBurningStorageProviders.newSmokerInstance();
        assertNotNull(provider3);
        testBurningStorage(() -> DynamicBurningStorages.newInstance(provider3, Blocks.SMOKER));
    }

    private void testBurningStorage(Supplier<? extends BurningStorage> constructor) {
        var storage = constructor.get();

        assertTrue(storage.supportsInsertion());
        assertTrue(storage.supportsExtraction());
        assertEquals(0, storage.getBurning().getValue().intValue());

        final var coalOne = Burning.COAL.one();
        final var coal8 = Burning.COAL.withValue(800);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coalOne, storage.insert(coalOne, transaction));
            assertEquals(coal8, storage.extract(coal8, transaction));
            assertEquals(coal8, storage.getBurning());
        }

        assertEquals(0, storage.getBurning().getValue().intValue());

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coalOne, storage.insert(coalOne, transaction));
            assertEquals(coal8, storage.extract(coal8, transaction));
            assertEquals(coal8, storage.getBurning());

            transaction.commit();
        }

        assertEquals(coal8, storage.getBurning());

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.insert(coalOne, transaction));
            assertEquals(coalOne, storage.getBurning());

            transaction.commit();
        }

        assertEquals(coalOne, storage.getBurning());

        final var blaze6 = Burning.BLAZE_ROD.withValue(600);
        final var blaze10 = Burning.BLAZE_ROD.withValue(1000);
        final var blaze12 = Burning.BLAZE_ROD.withValue(1200);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(blaze6, storage.insert(blaze6, transaction));
            assertEquals(blaze10, storage.extract(blaze10, transaction));
            assertEquals(blaze12, storage.getBurning());

            transaction.commit();
        }

        assertEquals(blaze12, storage.getBurning());

        testTransfer(constructor);
    }

    private void testTransfer(Supplier<? extends BurningStorage> constructor) {
        testTransfer(constructor, SimpleBurningStorage::new);
        testTransfer(constructor, () -> new SimpleBurningStorage(stack -> Burning.defaultBurnDuration(stack) / 2));

        testTransfer(constructor, AbstractFurnaceBurningStorages::newFurnaceInstance);
        testTransfer(constructor, AbstractFurnaceBurningStorages::newBlastFurnaceInstance);
        testTransfer(constructor, AbstractFurnaceBurningStorages::newSmokerInstance);

        testTransfer(constructor, () -> DynamicBurningStorages.newInstance(
                DynamicBurningStorageProviders.newFurnaceInstance(), Blocks.FURNACE));
        testTransfer(constructor, () -> DynamicBurningStorages.newInstance(
                DynamicBurningStorageProviders.newBlastFurnaceInstance(), Blocks.BLAST_FURNACE));
        testTransfer(constructor, () -> DynamicBurningStorages.newInstance(
                DynamicBurningStorageProviders.newSmokerInstance(), Blocks.SMOKER));
    }

    private void testTransfer(
            Supplier<? extends BurningStorage> sourceConstructor,
            Supplier<? extends BurningStorage> targetConstructor) {
        var source = sourceConstructor.get();

        assertTrue(source.supportsInsertion());
        assertTrue(source.supportsExtraction());
        assertEquals(0, source.getBurning().getValue().intValue());

        final var blaze12 = Burning.BLAZE_ROD.withValue(1200);
        final var blaze10 = Burning.BLAZE_ROD.withValue(1000);
        final var blaze8 = Burning.BLAZE_ROD.withValue(800);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(blaze10, source.insert(blaze10, transaction));
            transaction.commit();
        }

        var target = targetConstructor.get();
        assertTrue(target.supportsInsertion());
        assertTrue(target.supportsExtraction());
        assertEquals(0, target.getBurning().getValue().intValue());

        try (var transaction = Transaction.openOuter()) {
            assertEquals(blaze10, BurningStorage.transfer(source, target, blaze12, transaction));
            transaction.commit();
        }

        try (var transaction = Transaction.openOuter()) {
            assertEquals(blaze8, BurningStorage.transfer(target, source, blaze8, transaction));
            transaction.commit();
        }
    }
}
