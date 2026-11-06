package com.ayaan.replicatormod.items;

import com.ayaan.replicatormod.network.PacketHandler;
import com.ayaan.replicatormod.network.PacketOpenNameGui;
import com.ayaan.replicatormod.selection.SelectionManager;
import com.ayaan.replicatormod.util.BlockCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemInfinityCopier extends Item {

    public ItemInfinityCopier() {
        this.setMaxDamage(0);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        BlockCoord clicked = new BlockCoord(x, y, z);

        if (!player.isSneaking()) {
            SelectionManager.setPointA(player.getUniqueID(), clicked);
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Point A set: " + clicked));
            return true;
        }

        BlockCoord pointA = SelectionManager.getPointA(player.getUniqueID());
        if (pointA == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Set point A first"));
            return true;
        }

        SelectionManager.setPointB(player.getUniqueID(), clicked);
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA
                + "Point A: " + pointA + " — Point B: " + clicked));

        PacketHandler.INSTANCE.sendTo(new PacketOpenNameGui(pointA, clicked), (EntityPlayerMP) player);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List list, boolean advanced) {
        list.add(EnumChatFormatting.GRAY + "Right-click a block: set Point A");
        list.add(EnumChatFormatting.GRAY + "Shift + right-click a block: set Point B");
        list.add(EnumChatFormatting.GRAY + "Setting Point B opens a naming prompt");
        list.add(EnumChatFormatting.GRAY + "to save the selection as a clipboard");
        list.add("");
        list.add(EnumChatFormatting.DARK_GRAY + "Recipe:");
        list.add(EnumChatFormatting.DARK_GRAY + "Eye of Ender x3, Diamond Block x2,");
        list.add(EnumChatFormatting.DARK_GRAY + "Nether Star, Redstone Block x2, Golden Axe");
    }
}
