package niv.heatlib.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.heatlib.api.Heat;
import niv.heatlib.api.HeatStorage;

public class GameTestHeatStorageBinder {

    private static final BlockPos POS = new BlockPos(0, 1, 0);

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void testFurnaceHeatStorage(GameTestHelper context) {
        context.setBlock(POS, Blocks.FURNACE);
        testCommonHeatStorage(context);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void testBlastFurnaceHeatStorage(GameTestHelper context) {
        context.setBlock(POS, Blocks.BLAST_FURNACE);
        testCommonHeatStorage(context);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void testSmokerHeatStorage(GameTestHelper context) {
        context.setBlock(POS, Blocks.SMOKER);
        testCommonHeatStorage(context);
    }

    private void testCommonHeatStorage(GameTestHelper context) {
        context.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.FALSE);

        var storage = HeatStorage.SIDED.find(context.getLevel(), context.absolutePos(POS), null);
        context.assertTrue(storage != null,
                "Expected HeatStorage, get null");
        context.assertTrue(storage.getCurrentHeat().intValue() == 0,
                "Expected 0, got " + storage.getCurrentHeat().intValue());

        var heat = Heat.of(800, Items.COAL).orElseThrow();

        try (var transaction = Transaction.openOuter()) {
            storage.insert(heat, transaction);
            transaction.commit();
        }

        context.assertTrue(storage.getCurrentHeat().intValue() == 800,
                "Expected 800, got " + storage.getCurrentHeat().intValue());
        context.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.TRUE);

        try (var transaction = Transaction.openOuter()) {
            storage.extract(heat.one(), transaction);
            transaction.commit();
        }

        context.assertTrue(storage.getCurrentHeat().intValue() == 0,
                "Expected 0, got " + storage.getCurrentHeat().intValue());
        context.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.FALSE);

        context.succeed();
    }
}
