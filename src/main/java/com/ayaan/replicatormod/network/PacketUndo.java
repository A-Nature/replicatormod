package com.ayaan.replicatormod.network;

import com.ayaan.replicatormod.undo.UndoEntry;
import com.ayaan.replicatormod.undo.UndoManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/** Client -> server: undo keybind was pressed, pop and restore this player's top undo entry. */
public class PacketUndo implements IMessage {

    public PacketUndo() {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<PacketUndo, IMessage> {
        @Override
        public IMessage onMessage(PacketUndo message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            UndoEntry entry = UndoManager.pop(player.getUniqueID());
            if (entry == null) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Nothing to undo"));
                return null;
            }
            entry.snapshot.paste(player.worldObj, entry.origin);
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Undo complete"));
            return null;
        }
    }
}
