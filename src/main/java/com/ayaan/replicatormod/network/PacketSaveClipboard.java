package com.ayaan.replicatormod.network;

import com.ayaan.replicatormod.clipboard.ClipboardData;
import com.ayaan.replicatormod.clipboard.ClipboardStorage;
import com.ayaan.replicatormod.util.BlockCoord;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.io.IOException;

/** Client -> server: name was submitted in the naming prompt, capture and save the selection. */
public class PacketSaveClipboard implements IMessage {

    private int ax, ay, az, bx, by, bz;
    private String name;

    public PacketSaveClipboard() {
    }

    public PacketSaveClipboard(BlockCoord a, BlockCoord b, String name) {
        this.ax = a.x; this.ay = a.y; this.az = a.z;
        this.bx = b.x; this.by = b.y; this.bz = b.z;
        this.name = name;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(ax); buf.writeInt(ay); buf.writeInt(az);
        buf.writeInt(bx); buf.writeInt(by); buf.writeInt(bz);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ax = buf.readInt(); ay = buf.readInt(); az = buf.readInt();
        bx = buf.readInt(); by = buf.readInt(); bz = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketSaveClipboard, IMessage> {
        @Override
        public IMessage onMessage(PacketSaveClipboard message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            BlockCoord a = new BlockCoord(message.ax, message.ay, message.az);
            BlockCoord b = new BlockCoord(message.bx, message.by, message.bz);

            // Survival copies come out with empty containers so pasting can't dupe items; creative keeps contents.
            ClipboardData data = ClipboardData.capture(world, a, b, !player.capabilities.isCreativeMode);
            try {
                ClipboardStorage.save(player.getUniqueID(), message.name, data);
                String safeName = ClipboardStorage.sanitizeName(message.name);
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN
                        + "Saved clipboard '" + safeName + "' (" + data.sizeX + "x" + data.sizeY + "x" + data.sizeZ + ")"));
            } catch (IOException e) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                        + "Failed to save clipboard: " + e.getMessage()));
            }
            return null;
        }
    }
}
