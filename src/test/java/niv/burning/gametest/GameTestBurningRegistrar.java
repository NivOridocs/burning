package niv.burning.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;

public class GameTestBurningRegistrar {

    private static final BlockPos POS = new BlockPos(0, 1, 0);

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void testFurnaceBurningStorage(GameTestHelper context) {
        context.setBlock(POS, Blocks.FURNACE);
        testCommonBurningStorage(context);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void testBlastFurnaceBurningStorage(GameTestHelper context) {
        context.setBlock(POS, Blocks.BLAST_FURNACE);
        testCommonBurningStorage(context);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void testSmokerBurningStorage(GameTestHelper context) {
        context.setBlock(POS, Blocks.SMOKER);
        testCommonBurningStorage(context);
    }

    private void testCommonBurningStorage(GameTestHelper context) {
        context.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.FALSE);

        var storage = BurningStorage.SIDED.find(context.getLevel(), context.absolutePos(POS), null);
        context.assertTrue(storage != null,
                "Expected BurningStorage, get null");
        context.assertTrue(storage.getBurning().intValue() == 0,
                "Expected 0, got " + storage.getBurning().intValue());

        final var coal8 = Burning.COAL.withValue(800);

        try (var transaction = Transaction.openOuter()) {
            storage.insert(coal8, transaction);
            transaction.commit();
        }

        context.assertTrue(storage.getBurning().intValue() == 800,
                "Expected 800, got " + storage.getBurning().intValue());
        context.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.TRUE);

        try (var transaction = Transaction.openOuter()) {
            storage.extract(coal8.one(), transaction);
            transaction.commit();
        }

        context.assertTrue(storage.getBurning().intValue() == 0,
                "Expected 0, got " + storage.getBurning().intValue());
        context.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.FALSE);

        context.succeed();
    }
}
