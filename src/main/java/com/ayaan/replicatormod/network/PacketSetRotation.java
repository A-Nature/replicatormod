package com.ayaan.replicatormod.network;

import com.ayaan.replicatormod.selection.ActiveClipboardManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/** Client -> server: the rotate keybind was pressed while holding the Replicator. */
public class PacketSetRotation implements IMessage {

    private int rotation;

    public PacketSetRotation() {
    }

    public PacketSetRotation(int rotation) {
        this.rotation = rotation;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(rotation);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        rotation = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketSetRotation, IMessage> {
        @Override
        public IMessage onMessage(PacketSetRotation message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ActiveClipboardManager.setRotation(player.getUniqueID(), message.rotation);
            return null;
        }
    }
}
