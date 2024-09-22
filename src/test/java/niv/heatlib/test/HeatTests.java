package niv.heatlib.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.include.com.google.common.base.Objects;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import niv.heatlib.api.Heat;

class HeatTests {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void testHeatBuilder() {
        var heatOneOptional = Heat.of(10000, Items.LAVA_BUCKET);
        assertTrue(heatOneOptional.isPresent());

        var heatOne = heatOneOptional.get();
        assertEquals(Items.LAVA_BUCKET, heatOne.getFuel());
        assertEquals(.5d, heatOne.getPercent());

        var heatTwoOptional = Heat.of(.5d, Items.LAVA_BUCKET);
        assertTrue(heatTwoOptional.isPresent());

        var heatTwo = heatTwoOptional.get();
        assertEquals(Items.LAVA_BUCKET, heatTwo.getFuel());
        assertEquals(10000, heatTwo.intValue());

        assertTrue(Objects.equal(heatOne, heatTwo));
        assertTrue(Objects.equal(heatTwo, heatOne));

        var heatThreeOptional = Heat.of(5000, Items.LAVA_BUCKET, stack -> Heat.defaultBurnDuration(stack) / 2);
        assertTrue(heatThreeOptional.isPresent());

        var heatThree = heatThreeOptional.get();
        assertEquals(Items.LAVA_BUCKET, heatThree.getFuel());
        assertEquals(10000, heatThree.intValue());

        assertTrue(Objects.equal(heatOne, heatThree));
        assertTrue(Objects.equal(heatTwo, heatThree));
    }
}
