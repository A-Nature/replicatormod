package com.ayaan.replicatormod.init;

import com.ayaan.replicatormod.ReplicatorMod;
import com.ayaan.replicatormod.items.ItemInfinityCopier;
import com.ayaan.replicatormod.items.ItemInfinityReplicator;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ModItems {

    public static Item infinityCopier;
    public static Item infinityReplicator;

    public static void init() {
        infinityCopier = new ItemInfinityCopier()
                .setUnlocalizedName("infinity_copier")
                .setTextureName(ReplicatorMod.MODID + ":infinity_copier")
                .setCreativeTab(CreativeTabs.tabTools)
                .setMaxStackSize(1);

        infinityReplicator = new ItemInfinityReplicator()
                .setUnlocalizedName("infinity_replicator")
                .setTextureName(ReplicatorMod.MODID + ":infinity_replicator")
                .setCreativeTab(CreativeTabs.tabTools)
                .setMaxStackSize(1);

        GameRegistry.registerItem(infinityCopier, "infinity_copier");
        GameRegistry.registerItem(infinityReplicator, "infinity_replicator");
    }

    public static void registerRecipes() {
        GameRegistry.addRecipe(new ItemStack(infinityCopier),
                "EEE",
                "DND",
                "RGR",
                'E', Items.ender_eye,
                'D', Blocks.diamond_block,
                'N', Items.nether_star,
                'R', Blocks.redstone_block,
                'G', Items.golden_axe);

        GameRegistry.addRecipe(new ItemStack(infinityReplicator),
                "PPP",
                "PHP",
                "LLL",
                'P', Items.ender_pearl,
                'H', Items.golden_hoe,
                'L', Blocks.lapis_block);
    }
}
