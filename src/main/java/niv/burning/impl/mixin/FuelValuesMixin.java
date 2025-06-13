package niv.burning.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import niv.burning.api.BurningContext;

@Mixin(FuelValues.class)
public abstract class FuelValuesMixin implements BurningContext {

    @Override
    public boolean isFuel(Item item) {
        return ((FuelValues) (Object) this).isFuel(new ItemStack(item));
    }

    @Override
    public int burnDuration(Item item) {
        return ((FuelValues) (Object) this).burnDuration(new ItemStack(item));
    }
}
