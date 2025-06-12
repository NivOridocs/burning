package niv.burning.impl;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import niv.burning.api.BurningContext;

public class DefaultBurningContext implements BurningContext {

    private static DefaultBurningContext instance;

    private final Map<Item, Integer> fuels;

    private DefaultBurningContext(Map<Item, Integer> fuels) {
        this.fuels = fuels;
    }

    @Override
    public boolean isFuel(Item item) {
        return this.fuels.containsKey(item);
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return this.fuels.containsKey(itemStack.getItem());
    }

    @Override
    public int burnDuration(Item item) {
        return this.fuels.getOrDefault(item, 0);
    }

    @Override
    public int burnDuration(ItemStack itemStack) {
        return this.fuels.getOrDefault(itemStack.getItem(), 0);
    }

    public static synchronized BurningContext instance() {
        if (instance == null)
            instance = new DefaultBurningContext(ImmutableMap.<Item, Integer>builder()
                    .put(Items.LAVA_BUCKET, 20000)
                    .put(Blocks.COAL_BLOCK.asItem(), 16000)
                    .put(Items.BLAZE_ROD, 2400)
                    .put(Items.COAL, 1600)
                    .put(Items.CHARCOAL, 1600)
                    .put(Blocks.BAMBOO_MOSAIC.asItem(), 300)
                    .put(Blocks.BAMBOO_MOSAIC_STAIRS.asItem(), 300)
                    .put(Blocks.BAMBOO_MOSAIC_SLAB.asItem(), 150)
                    .put(Blocks.NOTE_BLOCK.asItem(), 300)
                    .put(Blocks.BOOKSHELF.asItem(), 300)
                    .put(Blocks.CHISELED_BOOKSHELF.asItem(), 300)
                    .put(Blocks.LECTERN.asItem(), 300)
                    .put(Blocks.JUKEBOX.asItem(), 300)
                    .put(Blocks.CHEST.asItem(), 300)
                    .put(Blocks.TRAPPED_CHEST.asItem(), 300)
                    .put(Blocks.CRAFTING_TABLE.asItem(), 300)
                    .put(Blocks.DAYLIGHT_DETECTOR.asItem(), 300)
                    .put(Items.BOW, 300)
                    .put(Items.FISHING_ROD, 300)
                    .put(Blocks.LADDER.asItem(), 300)
                    .put(Items.WOODEN_SHOVEL, 200)
                    .put(Items.WOODEN_SWORD, 200)
                    .put(Items.WOODEN_HOE, 200)
                    .put(Items.WOODEN_AXE, 200)
                    .put(Items.WOODEN_PICKAXE, 200)
                    .put(Items.STICK, 100)
                    .put(Items.BOWL, 100)
                    .put(Blocks.DRIED_KELP_BLOCK.asItem(), 4001)
                    .put(Items.CROSSBOW, 300)
                    .put(Blocks.BAMBOO.asItem(), 50)
                    .put(Blocks.DEAD_BUSH.asItem(), 100)
                    .put(Blocks.SCAFFOLDING.asItem(), 50)
                    .put(Blocks.LOOM.asItem(), 300)
                    .put(Blocks.BARREL.asItem(), 300)
                    .put(Blocks.CARTOGRAPHY_TABLE.asItem(), 300)
                    .put(Blocks.FLETCHING_TABLE.asItem(), 300)
                    .put(Blocks.SMITHING_TABLE.asItem(), 300)
                    .put(Blocks.COMPOSTER.asItem(), 300)
                    .put(Blocks.AZALEA.asItem(), 100)
                    .put(Blocks.FLOWERING_AZALEA.asItem(), 100)
                    .put(Blocks.MANGROVE_ROOTS.asItem(), 300)
                    .build());
        return instance;
    }
}
