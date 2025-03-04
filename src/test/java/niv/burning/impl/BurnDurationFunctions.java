package niv.burning.impl;

import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

import net.minecraft.world.item.ItemStack;
import niv.burning.api.BurningContext;

public class BurnDurationFunctions {

    private static final IntUnaryOperator HALVING = i -> i / 2;

    private static final IntUnaryOperator SQUARING = i -> i * i;

    public static final ToIntFunction<ItemStack> HALVED = stack -> HALVING
            .applyAsInt(BurningContext.defaultBurnDuration(stack));

    public static final ToIntFunction<ItemStack> SQUARED = stack -> SQUARING
            .applyAsInt(BurningContext.defaultBurnDuration(stack));
}
