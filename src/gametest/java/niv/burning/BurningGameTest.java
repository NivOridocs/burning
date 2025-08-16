package niv.burning;

import static net.minecraft.network.chat.Component.literal;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;
import niv.burning.impl.FuelValuesBurningContext;

@SuppressWarnings("java:S2187")
public class BurningGameTest {

    private static final BlockPos POS = new BlockPos(0, 1, 0);

    @GameTest
    public void testFurnaceBurningStorage(GameTestHelper context) {
        context.setBlock(POS, Blocks.FURNACE);
        testCommonBurningStorage(context);
    }

    @GameTest
    public void testBlastFurnaceBurningStorage(GameTestHelper context) {
        context.setBlock(POS, Blocks.BLAST_FURNACE);
        testCommonBurningStorage(context);
    }

    @GameTest
    public void testSmokerBurningStorage(GameTestHelper context) {
        context.setBlock(POS, Blocks.SMOKER);
        testCommonBurningStorage(context);
    }

    private void testCommonBurningStorage(GameTestHelper game) {
        game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.FALSE);

        final var context = new FuelValuesBurningContext(game.getLevel().fuelValues());
        game.assertTrue(context != null,
                literal("Expected BurningContext, get null"));

        final var storage = BurningStorage.SIDED.find(game.getLevel(), game.absolutePos(POS), null);
        game.assertTrue(storage != null,
                literal("Expected BurningStorage, get null"));
        game.assertTrue(storage.getBurning(context).getValue(context).intValue() == 0,
                literal("Expected 0, got " + storage.getBurning(context).getValue(context).intValue()));

        final var coal8 = Burning.COAL.withValue(800, context);

        try (var transaction = Transaction.openOuter()) {
            storage.insert(coal8, context, transaction);
            transaction.commit();
        }

        game.assertTrue(storage.getBurning(context).getValue(context).intValue() == 800,
                literal("Expected 800, got " + storage.getBurning(context).getValue(context).intValue()));
        game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.TRUE);

        try (var transaction = Transaction.openOuter()) {
            storage.extract(coal8.one(), context, transaction);
            transaction.commit();
        }

        game.assertTrue(storage.getBurning(context).getValue(context).intValue() == 0,
                literal("Expected 0, got " + storage.getBurning(context).getValue(context).intValue()));
        game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.FALSE);

        game.succeed();
    }
}
