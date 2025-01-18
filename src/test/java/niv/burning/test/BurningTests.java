package niv.burning.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.include.com.google.common.base.Objects;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.impl.BurnDurationFunctions;

class BurningTests {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void testBurningBuilder() {
        var contextOne = BurningContext.defaultInstance();
        assertNotNull(contextOne);

        var burningOneOptional = Burning.ofOptional(Items.LAVA_BUCKET, contextOne);
        assertTrue(burningOneOptional.isPresent());

        var burningOne = burningOneOptional.get().withValue(10000, contextOne);
        assertEquals(Items.LAVA_BUCKET, burningOne.getFuel());
        assertEquals(.5d, burningOne.getPercent());

        var contextTwo = BurningContext.defaultWith(BurnDurationFunctions.HALVED);
        assertNotNull(contextTwo);

        var burningTwoOptional = Burning.ofOptional(Items.LAVA_BUCKET, contextTwo);
        assertTrue(burningTwoOptional.isPresent());

        var burningTwo = burningTwoOptional.get().withValue(5000, contextTwo);
        assertEquals(Items.LAVA_BUCKET, burningTwo.getFuel());
        assertEquals(10000, burningTwo.getValue(contextOne).intValue());

        assertTrue(Objects.equal(burningOne, burningTwo));
    }

    @Test
    void testOperations() {
        var context = BurningContext.defaultInstance();

        final var coal16 = Burning.COAL.withValue(1600, context);
        final var coal6 = Burning.COAL.withValue(600, context);
        final var coal4 = Burning.COAL.withValue(400, context);
        final var coal0 = Burning.COAL.withValue(0, context);

        final var blaze10 = Burning.BLAZE_ROD.withValue(1000, context);

        final var add1 = Burning.add(coal6, blaze10, context);
        assertEquals(coal16, add1);

        final var add2 = Burning.add(blaze10, coal6, context);
        assertEquals(coal16, add2);

        assertTrue(Objects.equal(add1, add2));

        final var sub1 = Burning.subtract(blaze10, coal6, context);
        assertEquals(coal4, sub1);

        final var sub2 = Burning.subtract(coal6, blaze10, context);
        assertEquals(coal0, sub2);

        assertFalse(Objects.equal(sub1, sub2));

        assertEquals(coal4, Burning.minValue(coal4, coal6, context));
        assertEquals(coal4, Burning.minValue(coal6, coal4, context));

        assertEquals(coal6, Burning.maxValue(coal4, coal6, context));
        assertEquals(coal6, Burning.maxValue(coal6, coal4, context));
    }

    @Test
    void testOperationsHalf() {
        final var halved = BurningContext.defaultWith(BurnDurationFunctions.HALVED);
        assertNotNull(halved);

        final var coal16 = Burning.COAL.withValue(1600 / 2, halved);
        final var coal6 = Burning.COAL.withValue(600 / 2, halved);
        final var coal4 = Burning.COAL.withValue(400 / 2, halved);
        final var coal0 = Burning.COAL.withValue(0, halved);

        final var blaze10 = Burning.BLAZE_ROD.withValue(1000 / 2, halved);

        final var add1 = Burning.add(coal6, blaze10, halved);
        assertEquals(coal16, add1);

        final var add2 = Burning.add(blaze10, coal6, halved);
        assertEquals(coal16, add2);

        assertTrue(Objects.equal(add1, add2));

        final var sub1 = Burning.subtract(blaze10, coal6, halved);
        assertEquals(coal4, sub1);

        final var sub2 = Burning.subtract(coal6, blaze10, halved);
        assertEquals(coal0, sub2);

        assertFalse(Objects.equal(sub1, sub2));

        assertEquals(coal4, Burning.minValue(coal4, coal6, halved));
        assertEquals(coal4, Burning.minValue(coal6, coal4, halved));

        assertEquals(coal6, Burning.maxValue(coal4, coal6, halved));
        assertEquals(coal6, Burning.maxValue(coal6, coal4, halved));
    }

    @Test
    void testOperationsSquare() {
        final var squared = BurningContext.defaultWith(BurnDurationFunctions.SQUARED);
        assertNotNull(squared);

        final var coal16 = Burning.COAL.withValue(1600 * 1600, squared);
        final var coal6 = Burning.COAL.withValue(600 * 1600, squared);
        final var coal4 = Burning.COAL.withValue(400 * 1600, squared);
        final var coal0 = Burning.COAL.withValue(0, squared);

        final var blaze10 = Burning.BLAZE_ROD.withValue(1000 * 1600, squared);

        final var add1 = Burning.add(coal6, blaze10, squared);
        assertEquals(coal16, add1);

        final var add2 = Burning.add(blaze10, coal6, squared);
        assertEquals(coal16, add2);

        assertTrue(Objects.equal(add1, add2));

        final var sub1 = Burning.subtract(blaze10, coal6, squared);
        assertEquals(coal4, sub1);

        final var sub2 = Burning.subtract(coal6, blaze10, squared);
        assertEquals(coal0, sub2);

        assertFalse(Objects.equal(sub1, sub2));

        assertEquals(coal4, Burning.minValue(coal4, coal6, squared));
        assertEquals(coal4, Burning.minValue(coal6, coal4, squared));

        assertEquals(coal6, Burning.maxValue(coal4, coal6, squared));
        assertEquals(coal6, Burning.maxValue(coal6, coal4, squared));
    }
}
