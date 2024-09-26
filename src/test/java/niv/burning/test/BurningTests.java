package niv.burning.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.include.com.google.common.base.Objects;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
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
        assertEquals(10000, burningTwo.intValue());

        assertTrue(Objects.equal(burningOne, burningTwo));
    }
}
