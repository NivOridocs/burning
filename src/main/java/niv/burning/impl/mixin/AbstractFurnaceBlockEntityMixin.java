package niv.burning.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.BurningStorage;
import niv.burning.impl.AbstractFurnaceBlockEntityExtension;
import niv.burning.impl.AbstractFurnaceBurningStorage;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin implements AbstractFurnaceBlockEntityExtension {

    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String BLOCK_POS = "Lnet/minecraft/core/BlockPos;";
    private static final String BLOCK_STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String ENTITY = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;";
    private static final String ITEM_STACK = "Lnet/minecraft/world/item/ItemStack;";

    @Unique
    @SuppressWarnings("java:S116")
    private final BurningStorage burning_burningStorage = new AbstractFurnaceBurningStorage((AbstractFurnaceBlockEntity) (Object) this);

    @Unique
    @SuppressWarnings("java:S116")
    private Item burning_lastBurnedFuel;

    @Override
    public Item burning_getFuel() {
        return this.burning_lastBurnedFuel;
    }

    @Override
    public void burning_setFuel(Item fuel) {
        this.burning_lastBurnedFuel = fuel;
    }

    @Override
    public BurningStorage burning_getBurningStorage() {
        return burning_burningStorage;
    }

    @Inject( //
            method = "serverTick(" + LEVEL + BLOCK_POS + BLOCK_STATE + ENTITY + ")V", //
            at = @At(value = "INVOKE", shift = Shift.AFTER, //
                    target = ENTITY + "getBurnDuration(" + ITEM_STACK + ")I"))
    private static void injectAfterGetBurnDuration(CallbackInfo info,
            @Local AbstractFurnaceBlockEntity entity,
            @Local(ordinal = 0) ItemStack itemStack) {
        entity.burning_setFuel(itemStack.getItem());
    }
}
