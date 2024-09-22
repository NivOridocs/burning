package niv.heatlib.impl.mixin;

import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@ApiStatus.Internal
@Mixin(BlockEntity.class)
public interface BlockEntityAccessor {

    @Invoker("setChanged")
    static void invokeSetChanged(Level level, BlockPos blockPos, BlockState blockState) {
        throw new AssertionError();
    }

}
