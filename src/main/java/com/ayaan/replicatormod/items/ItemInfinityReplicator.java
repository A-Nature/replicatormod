package com.ayaan.replicatormod.items;

import com.ayaan.replicatormod.client.ClientState;
import com.ayaan.replicatormod.clipboard.ClipboardData;
import com.ayaan.replicatormod.clipboard.ClipboardStorage;
import com.ayaan.replicatormod.clipboard.MaterialRequirement;
import com.ayaan.replicatormod.network.PacketClipboardListResponse;
import com.ayaan.replicatormod.network.PacketHandler;
import com.ayaan.replicatormod.selection.ActiveClipboardManager;
import com.ayaan.replicatormod.undo.UndoEntry;
import com.ayaan.replicatormod.undo.UndoManager;
import com.ayaan.replicatormod.util.BlockCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemInfinityReplicator extends Item {

    public ItemInfinityReplicator() {
        this.setMaxDamage(0);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        if (player.isSneaking()) {
            sendClipboardList(player);
        } else {
            ForgeDirection dir = ForgeDirection.getOrientation(side);
            BlockCoord target = new BlockCoord(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
            attemptPaste(player, world, target);
        }
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && player.isSneaking()) {
            sendClipboardList(player);
        }
        return stack;
    }

    private void sendClipboardList(EntityPlayer player) {
        List<String> names = ClipboardStorage.list(player.getUniqueID());
        PacketHandler.INSTANCE.sendTo(new PacketClipboardListResponse(names), (EntityPlayerMP) player);
    }

    private void attemptPaste(EntityPlayer player, World world, BlockCoord target) {
        String activeName = ActiveClipboardManager.getActive(player.getUniqueID());
        if (activeName == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "No active clipboard — shift+right-click to pick one"));
            return;
        }

        ClipboardData data;
        try {
            data = ClipboardStorage.load(player.getUniqueID(), activeName);
        } catch (IOException e) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to load clipboard: " + e.getMessage()));
            return;
        }
        if (data == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Clipboard '" + activeName + "' not found"));
            return;
        }

        int rotation = ActiveClipboardManager.getRotation(player.getUniqueID());
        if (rotation != 0) {
            data = data.rotate(rotation);
        }

        boolean creative = player.capabilities.isCreativeMode;
        List<MaterialRequirement> requirements =
                MaterialRequirement.fromBlockRequirements(data.computeMaterialRequirements());

        if (!creative) {
            List<String> missing = new ArrayList<String>();
            for (MaterialRequirement req : requirements) {
                int have = req.countInInventory(player);
                if (have < req.required) {
                    Item item = req.resolveItem();
                    String displayName = item != null
                            ? new ItemStack(item, 1, req.meta).getDisplayName()
                            : req.itemName;
                    missing.add(displayName + ": need " + (req.required - have) + " more");
                }
            }
            if (!missing.isEmpty()) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Missing materials:"));
                for (String line : missing) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + " - " + line));
                }
                return;
            }
            for (MaterialRequirement req : requirements) {
                req.consumeFromInventory(player);
            }
        }

        BlockCoord snapshotMax = new BlockCoord(
                target.x + data.sizeX - 1,
                target.y + data.sizeY - 1,
                target.z + data.sizeZ - 1);
        ClipboardData beforeSnapshot = ClipboardData.capture(world, target, snapshotMax, false);
        UndoManager.push(player.getUniqueID(), new UndoEntry(target, beforeSnapshot));

        data.paste(world, target);
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Pasted '" + activeName + "'"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List list, boolean advanced) {
        list.add(EnumChatFormatting.GRAY + "Shift + right-click: pick an active clipboard");
        list.add(EnumChatFormatting.GRAY + "Right-click a block: paste the active clipboard");
        list.add(EnumChatFormatting.GRAY + "R: rotate the active clipboard 90°");
        String active = ClientState.getActiveClipboardName();
        if (active != null) {
            list.add(EnumChatFormatting.YELLOW + "Active clipboard: " + active);
        } else {
            list.add(EnumChatFormatting.DARK_GRAY + "No active clipboard");
        }
        list.add("");
        list.add(EnumChatFormatting.DARK_GRAY + "Recipe:");
        list.add(EnumChatFormatting.DARK_GRAY + "Ender Pearl x6, Golden Hoe, Lapis Block x3");
    }
}
