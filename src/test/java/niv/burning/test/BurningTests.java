package niv.burning.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.ToIntFunction;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.include.com.google.common.base.Objects;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import niv.burning.api.Burning;

class BurningTests {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void testBurningBuilder() {
        var burningOneOptional = Burning.ofOptional(Items.LAVA_BUCKET);
        assertTrue(burningOneOptional.isPresent());

        var burningOne = burningOneOptional.get().withValue(10000);
        assertEquals(Items.LAVA_BUCKET, burningOne.getFuel());
        assertEquals(.5d, burningOne.getPercent());

        var burningTwoOptional = Burning.ofOptional(Items.LAVA_BUCKET, stack -> Burning.defaultBurnDuration(stack) / 2);
        assertTrue(burningTwoOptional.isPresent());

        var burningTwo = burningTwoOptional.get().withValue(5000, stack -> Burning.defaultBurnDuration(stack) / 2);
        assertEquals(Items.LAVA_BUCKET, burningTwo.getFuel());
        assertEquals(10000, burningTwo.getValue().intValue());

        assertTrue(Objects.equal(burningOne, burningTwo));
    }

    @Test
    void testOperations() {
        final var coal16 = Burning.COAL.withValue(1600);
        final var coal6 = Burning.COAL.withValue(600);
        final var coal4 = Burning.COAL.withValue(400);
        final var coal0 = Burning.COAL.withValue(0);

        final var blaze10 = Burning.BLAZE_ROD.withValue(1000);

        final var add1 = Burning.add(coal6, blaze10);
        assertEquals(coal16, add1);

        final var add2 = Burning.add(blaze10, coal6);
        assertEquals(coal16, add2);

        assertTrue(Objects.equal(add1, add2));

        final var sub1 = Burning.subtract(blaze10, coal6);
        assertEquals(coal4, sub1);

        final var sub2 = Burning.subtract(coal6, blaze10);
        assertEquals(coal0, sub2);

        assertFalse(Objects.equal(sub1, sub2));

        assertEquals(coal4, Burning.minValue(coal4, coal6));
        assertEquals(coal4, Burning.minValue(coal6, coal4));

        assertEquals(coal6, Burning.maxValue(coal4, coal6));
        assertEquals(coal6, Burning.maxValue(coal6, coal4));
    }

    @Test
    void testOperationsHalf() {
        final ToIntFunction<ItemStack> half = stack -> Burning.defaultBurnDuration(stack) / 2;

        final var coal16 = Burning.COAL.withValue(1600 / 2, half);
        final var coal6 = Burning.COAL.withValue(600 / 2, half);
        final var coal4 = Burning.COAL.withValue(400 / 2, half);
        final var coal0 = Burning.COAL.withValue(0, half);

        final var blaze10 = Burning.BLAZE_ROD.withValue(1000 / 2, half);

        final var add1 = Burning.add(coal6, blaze10, half);
        assertEquals(coal16, add1);

        final var add2 = Burning.add(blaze10, coal6, half);
        assertEquals(coal16, add2);

        assertTrue(Objects.equal(add1, add2));

        final var sub1 = Burning.subtract(blaze10, coal6, half);
        assertEquals(coal4, sub1);

        final var sub2 = Burning.subtract(coal6, blaze10, half);
        assertEquals(coal0, sub2);

        assertFalse(Objects.equal(sub1, sub2));

        assertEquals(coal4, Burning.minValue(coal4, coal6, half));
        assertEquals(coal4, Burning.minValue(coal6, coal4, half));

        assertEquals(coal6, Burning.maxValue(coal4, coal6, half));
        assertEquals(coal6, Burning.maxValue(coal6, coal4, half));
    }

    @Test
    void testOperationsSquare() {
        final ToIntFunction<ItemStack> square = stack -> Burning.defaultBurnDuration(stack) * Burning.defaultBurnDuration(stack);

        final var coal16 = Burning.COAL.withValue(1600 * 1600, square);
        final var coal6 = Burning.COAL.withValue(600 * 1600, square);
        final var coal4 = Burning.COAL.withValue(400 * 1600, square);
        final var coal0 = Burning.COAL.withValue(0, square);

        final var blaze10 = Burning.BLAZE_ROD.withValue(1000 * 1600, square);

        final var add1 = Burning.add(coal6, blaze10, square);
        assertEquals(coal16, add1);

        final var add2 = Burning.add(blaze10, coal6, square);
        assertEquals(coal16, add2);

        assertTrue(Objects.equal(add1, add2));

        final var sub1 = Burning.subtract(blaze10, coal6, square);
        assertEquals(coal4, sub1);

        final var sub2 = Burning.subtract(coal6, blaze10, square);
        assertEquals(coal0, sub2);

        assertFalse(Objects.equal(sub1, sub2));

        assertEquals(coal4, Burning.minValue(coal4, coal6, square));
        assertEquals(coal4, Burning.minValue(coal6, coal4, square));

        assertEquals(coal6, Burning.maxValue(coal4, coal6, square));
        assertEquals(coal6, Burning.maxValue(coal6, coal4, square));
    }
}
