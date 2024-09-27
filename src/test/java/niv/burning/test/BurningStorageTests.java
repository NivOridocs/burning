package niv.burning.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import niv.burning.impl.DynamicBurningStorageProvider;
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
        testBurningStorage(new SimpleBurningStorage());
        testBurningStorage(new SimpleBurningStorage(stack -> Burning.defaultBurnDuration(stack) / 2));
    }

    @Test
    void testAbstractFurnaceBurningStorage() {
        testBurningStorage(AbstractFurnaceBurningStorages.newFurnaceInstance());
        testBurningStorage(AbstractFurnaceBurningStorages.newBlastFurnaceInstance());
        testBurningStorage(AbstractFurnaceBurningStorages.newSmokerInstance());
    }

    @Test
    void testDynamicBurningStorage() {
        DynamicBurningStorageProvider provider;

        provider = DynamicBurningStorageProviders.newFurnaceInstance();
        assertNotNull(provider);
        testBurningStorage(DynamicBurningStorages.newInstance(provider, Blocks.FURNACE));

        provider = DynamicBurningStorageProviders.newBlastFurnaceInstance();
        assertNotNull(provider);
        testBurningStorage(DynamicBurningStorages.newInstance(provider, Blocks.BLAST_FURNACE));

        provider = DynamicBurningStorageProviders.newSmokerInstance();
        assertNotNull(provider);
        testBurningStorage(DynamicBurningStorages.newInstance(provider, Blocks.SMOKER));
    }

    private void testBurningStorage(BurningStorage storage) {
        assertTrue(storage.supportsInsertion());
        assertTrue(storage.supportsExtraction());
        assertEquals(0, storage.getBurning().intValue());

        final var coalOne = Burning.COAL.one();
        final var coal8 = Burning.COAL.withValue(800);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coalOne, storage.insert(coalOne, transaction));
            assertEquals(coal8, storage.extract(coal8, transaction));
            assertEquals(coal8, storage.getBurning());
        }

        assertEquals(0, storage.getBurning().intValue());

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
    }
}
