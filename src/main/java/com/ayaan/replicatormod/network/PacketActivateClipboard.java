package com.ayaan.replicatormod.network;

import com.ayaan.replicatormod.clipboard.ClipboardData;
import com.ayaan.replicatormod.clipboard.ClipboardStorage;
import com.ayaan.replicatormod.clipboard.MaterialRequirement;
import com.ayaan.replicatormod.selection.ActiveClipboardManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.List;

/**
 * Client -> server: player picked a clipboard in the picker GUI. Server
 * records it as that player's active clipboard (so paste doesn't need its
 * own round trip) and replies with the material requirements for the HUD.
 */
public class PacketActivateClipboard implements IMessage {

    private String name;

    public PacketActivateClipboard() {
    }

    public PacketActivateClipboard(String name) {
        this.name = name;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        name = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketActivateClipboard, IMessage> {
        @Override
        public IMessage onMessage(PacketActivateClipboard message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ActiveClipboardManager.setActive(player.getUniqueID(), message.name);

            try {
                ClipboardData data = ClipboardStorage.load(player.getUniqueID(), message.name);
                if (data == null) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                            + "Clipboard '" + message.name + "' not found"));
                    return null;
                }
                List<MaterialRequirement> requirements =
                        MaterialRequirement.fromBlockRequirements(data.computeMaterialRequirements());
                PacketHandler.INSTANCE.sendTo(
                        new PacketMaterialsResponse(ClipboardStorage.sanitizeName(message.name),
                                data.sizeX, data.sizeY, data.sizeZ, requirements),
                        player);
            } catch (IOException e) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                        + "Failed to load clipboard: " + e.getMessage()));
            }
            return null;
        }
    }
}
