package niv.burning.api;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BurningStorageHelper {

    private BurningStorageHelper() {
        // no-op
    }

    public static final boolean tryUpdateLitProperty(BlockEntity entity, BurningStorage storage) {
        var level = entity.level;
        var pos = entity.worldPosition;
        if (level == null || pos == null)
            return false;

        var state = level.getBlockState(pos);
        var wasBurning = state.getOptionalValue(BlockStateProperties.LIT).orElse(null);
        if (wasBurning == null)
            return false;

        var isBurning = storage.isBurning();
        if (wasBurning.equals(isBurning))
            return false;

        state = state.setValue(BlockStateProperties.LIT, isBurning);
        level.setBlockAndUpdate(pos, state);
        return true;
    }
}
