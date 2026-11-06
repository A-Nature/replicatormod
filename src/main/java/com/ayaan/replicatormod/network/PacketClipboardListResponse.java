package com.ayaan.replicatormod.network;

import com.ayaan.replicatormod.client.gui.GuiClipboardPicker;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/** Server -> client: this player's saved clipboard names, open the picker GUI. */
public class PacketClipboardListResponse implements IMessage {

    private List<String> names = new ArrayList<String>();

    public PacketClipboardListResponse() {
    }

    public PacketClipboardListResponse(List<String> names) {
        this.names = names;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(names.size());
        for (String name : names) {
            ByteBufUtils.writeUTF8String(buf, name);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        names = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) {
            names.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class Handler implements IMessageHandler<PacketClipboardListResponse, IMessage> {
        @Override
        public IMessage onMessage(PacketClipboardListResponse message, MessageContext ctx) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiClipboardPicker(message.names));
            return null;
        }
    }

    /** See PacketOpenNameGui.NoOpHandler — same reason, same fix. */
    public static class NoOpHandler implements IMessageHandler<PacketClipboardListResponse, IMessage> {
        @Override
        public IMessage onMessage(PacketClipboardListResponse message, MessageContext ctx) {
            return null;
        }
    }
}
