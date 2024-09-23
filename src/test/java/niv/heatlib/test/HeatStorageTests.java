package niv.heatlib.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import niv.heatlib.api.Heat;
import niv.heatlib.api.HeatStorage;
import niv.heatlib.api.base.SimpleHeatStorage;
import niv.heatlib.impl.AbstractFurnaceHeatStorages;

class HeatStorageTests {

    private static Heat COAL;
    private static Heat BLAZE_ROD;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        COAL = Heat.of(Items.COAL).orElseThrow();
        BLAZE_ROD = Heat.of(Items.BLAZE_ROD).orElseThrow();
    }

    @Test
    void testSimpleHeatStorage() {
        testHeatStorage(new SimpleHeatStorage());
        testHeatStorage(new SimpleHeatStorage(stack -> Heat.defaultBurnDuration(stack) / 2));
    }

    @Test
    void testBoundHeatStorage() {
        testHeatStorage(AbstractFurnaceHeatStorages.newFurnaceInstance());
        testHeatStorage(AbstractFurnaceHeatStorages.newBlastFurnaceInstance());
        testHeatStorage(AbstractFurnaceHeatStorages.newSmokerInstance());
    }

    private void testHeatStorage(HeatStorage storage) {
        assertTrue(storage.supportsInsertion());
        assertTrue(storage.supportsExtraction());
        assertEquals(0, storage.getCurrentHeat().intValue());

        final var coal8 = COAL.withValue(800);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(COAL.zero(), storage.insert(COAL.one(), transaction));
            assertEquals(coal8, storage.extract(coal8, transaction));
            assertEquals(coal8, storage.getCurrentHeat());
        }

        assertEquals(0, storage.getCurrentHeat().intValue());

        try (var transaction = Transaction.openOuter()) {
            assertEquals(COAL.zero(), storage.insert(COAL.one(), transaction));
            assertEquals(coal8, storage.extract(coal8, transaction));
            assertEquals(coal8, storage.getCurrentHeat());

            transaction.commit();
        }

        assertEquals(coal8, storage.getCurrentHeat());

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.insert(COAL.one(), transaction));
            assertEquals(COAL.one(), storage.getCurrentHeat());

            transaction.commit();
        }

        assertEquals(COAL.one(), storage.getCurrentHeat());

        final var blaze10 = BLAZE_ROD.withValue(1000);
        final var blaze12 = BLAZE_ROD.withValue(1200);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(BLAZE_ROD.zero(), storage.insert(BLAZE_ROD.withValue(600), transaction));
            assertEquals(blaze10, storage.extract(blaze10, transaction));
            assertEquals(blaze12, storage.getCurrentHeat());

            transaction.commit();
        }

        assertEquals(blaze12, storage.getCurrentHeat());
    }
}
