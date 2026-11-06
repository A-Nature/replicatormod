package com.ayaan.replicatormod.clipboard;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A single "need N of this item" line, resolved down to an actual Item the
 * player could hold (as opposed to the raw block registry name used in
 * ClipboardData).
 */
public class MaterialRequirement {

    public final String itemName;
    public final int meta;
    public final int required;

    public MaterialRequirement(String itemName, int meta, int required) {
        this.itemName = itemName;
        this.meta = meta;
        this.required = required;
    }

    /**
     * Blocks whose Item.getItemFromBlock lookup doesn't point back to the
     * item that actually places them (doors, beds, and a handful of other
     * vanilla oddities where the placed Block has no directly-linked item).
     */
    private static final Map<Block, Item> BLOCK_ITEM_OVERRIDES = new HashMap<Block, Item>();
    static {
        BLOCK_ITEM_OVERRIDES.put(Blocks.wooden_door, Items.wooden_door);
        BLOCK_ITEM_OVERRIDES.put(Blocks.iron_door, Items.iron_door);
        BLOCK_ITEM_OVERRIDES.put(Blocks.bed, Items.bed);
        BLOCK_ITEM_OVERRIDES.put(Blocks.skull, Items.skull);
        BLOCK_ITEM_OVERRIDES.put(Blocks.cauldron, Items.cauldron);
        BLOCK_ITEM_OVERRIDES.put(Blocks.flower_pot, Items.flower_pot);
        BLOCK_ITEM_OVERRIDES.put(Blocks.brewing_stand, Items.brewing_stand);
        BLOCK_ITEM_OVERRIDES.put(Blocks.cake, Items.cake);
        BLOCK_ITEM_OVERRIDES.put(Blocks.reeds, Items.reeds);
    }

    /** True for the "second half" of a two-block placement (door top, bed head) — not counted on its own. */
    private static boolean isSecondaryHalf(Block block, int meta) {
        return (block == Blocks.wooden_door || block == Blocks.iron_door || block == Blocks.bed) && (meta & 8) != 0;
    }

    public Item resolveItem() {
        return (Item) Item.itemRegistry.getObject(itemName);
    }

    public int countInInventory(EntityPlayer player) {
        Item item = resolveItem();
        if (item == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() == item && stack.getItemDamage() == meta) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    /**
     * Removes up to `required` matching items from the player's inventory.
     * Returns how many were actually removed (caller should only call this
     * after confirming enough are present).
     */
    public int consumeFromInventory(EntityPlayer player) {
        Item item = resolveItem();
        if (item == null) {
            return 0;
        }
        int remaining = required;
        ItemStack[] main = player.inventory.mainInventory;
        for (int i = 0; i < main.length && remaining > 0; i++) {
            ItemStack stack = main[i];
            if (stack != null && stack.getItem() == item && stack.getItemDamage() == meta) {
                int take = Math.min(remaining, stack.stackSize);
                stack.stackSize -= take;
                remaining -= take;
                if (stack.stackSize <= 0) {
                    main[i] = null;
                }
            }
        }
        return required - remaining;
    }

    /** Converts a ClipboardData block-name requirement map into resolvable item requirements. */
    public static List<MaterialRequirement> fromBlockRequirements(Map<String, Integer> blockRequirements) {
        Map<String, Integer> merged = new LinkedHashMap<String, Integer>();

        for (Map.Entry<String, Integer> entry : blockRequirements.entrySet()) {
            String key = entry.getKey();
            int sep = key.lastIndexOf(':');
            String blockName = key.substring(0, sep);
            int meta = Integer.parseInt(key.substring(sep + 1));

            Block block = (Block) Block.blockRegistry.getObject(blockName);
            if (block == null || isSecondaryHalf(block, meta)) {
                continue;
            }

            Item item = BLOCK_ITEM_OVERRIDES.get(block);
            int itemMeta;
            if (item != null) {
                itemMeta = 0;
            } else {
                item = Item.getItemFromBlock(block);
                itemMeta = item != null ? block.damageDropped(meta) : 0;
            }
            if (item == null) {
                continue;
            }
            String itemName = (String) Item.itemRegistry.getNameForObject(item);

            String mergedKey = itemName + ":" + itemMeta;
            Integer existing = merged.get(mergedKey);
            merged.put(mergedKey, existing == null ? entry.getValue() : existing + entry.getValue());
        }

        List<MaterialRequirement> result = new ArrayList<MaterialRequirement>();
        for (Map.Entry<String, Integer> entry : merged.entrySet()) {
            String key = entry.getKey();
            int sep = key.lastIndexOf(':');
            String itemName = key.substring(0, sep);
            int meta = Integer.parseInt(key.substring(sep + 1));
            result.add(new MaterialRequirement(itemName, meta, entry.getValue()));
        }
        return result;
    }
}
