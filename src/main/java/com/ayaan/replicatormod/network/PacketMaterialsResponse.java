package com.ayaan.replicatormod.network;

import com.ayaan.replicatormod.client.ClientState;
import com.ayaan.replicatormod.clipboard.MaterialRequirement;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/** Server -> client: material requirements for the just-activated clipboard, for the HUD. */
public class PacketMaterialsResponse implements IMessage {

    private String clipboardName;
    private int sizeX, sizeY, sizeZ;
    private List<MaterialRequirement> requirements = new ArrayList<MaterialRequirement>();

    public PacketMaterialsResponse() {
    }

    public PacketMaterialsResponse(String clipboardName, int sizeX, int sizeY, int sizeZ, List<MaterialRequirement> requirements) {
        this.clipboardName = clipboardName;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.requirements = requirements;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, clipboardName);
        buf.writeInt(sizeX);
        buf.writeInt(sizeY);
        buf.writeInt(sizeZ);
        buf.writeInt(requirements.size());
        for (MaterialRequirement req : requirements) {
            ByteBufUtils.writeUTF8String(buf, req.itemName);
            buf.writeInt(req.meta);
            buf.writeInt(req.required);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        clipboardName = ByteBufUtils.readUTF8String(buf);
        sizeX = buf.readInt();
        sizeY = buf.readInt();
        sizeZ = buf.readInt();
        int count = buf.readInt();
        requirements = new ArrayList<MaterialRequirement>(count);
        for (int i = 0; i < count; i++) {
            String itemName = ByteBufUtils.readUTF8String(buf);
            int meta = buf.readInt();
            int required = buf.readInt();
            requirements.add(new MaterialRequirement(itemName, meta, required));
        }
    }

    public static class Handler implements IMessageHandler<PacketMaterialsResponse, IMessage> {
        @Override
        public IMessage onMessage(PacketMaterialsResponse message, MessageContext ctx) {
            ClientState.setActiveClipboard(message.clipboardName, message.sizeX, message.sizeY, message.sizeZ, message.requirements);
            return null;
        }
    }
}
