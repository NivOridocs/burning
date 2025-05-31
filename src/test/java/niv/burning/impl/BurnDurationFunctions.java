package niv.burning.impl;

import java.util.function.IntUnaryOperator;

import niv.burning.api.BurningContext;

public class BurnDurationFunctions {

    private static final IntUnaryOperator HALVING = i -> i / 2;

    private static final IntUnaryOperator SQUARING = i -> i * i;

    public static final BurnDurationFunction HALVED = stack -> HALVING
            .applyAsInt(BurningContext.defaultBurnDuration(stack));

    public static final BurnDurationFunction SQUARED = stack -> SQUARING
            .applyAsInt(BurningContext.defaultBurnDuration(stack));
}
