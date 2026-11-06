package com.ayaan.replicatormod.network;

import com.ayaan.replicatormod.client.gui.GuiCopierName;
import com.ayaan.replicatormod.util.BlockCoord;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

/** Server -> client: both selection points are set, open the naming prompt. */
public class PacketOpenNameGui implements IMessage {

    private int ax, ay, az, bx, by, bz;

    public PacketOpenNameGui() {
    }

    public PacketOpenNameGui(BlockCoord a, BlockCoord b) {
        this.ax = a.x; this.ay = a.y; this.az = a.z;
        this.bx = b.x; this.by = b.y; this.bz = b.z;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(ax); buf.writeInt(ay); buf.writeInt(az);
        buf.writeInt(bx); buf.writeInt(by); buf.writeInt(bz);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ax = buf.readInt(); ay = buf.readInt(); az = buf.readInt();
        bx = buf.readInt(); by = buf.readInt(); bz = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketOpenNameGui, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenNameGui message, MessageContext ctx) {
            BlockCoord a = new BlockCoord(message.ax, message.ay, message.az);
            BlockCoord b = new BlockCoord(message.bx, message.by, message.bz);
            Minecraft.getMinecraft().displayGuiScreen(new GuiCopierName(a, b));
            return null;
        }
    }

    /**
     * Registered instead of Handler when PacketHandler.init() runs on a
     * dedicated server. SimpleNetworkWrapper.registerMessage always calls
     * newInstance() on the handler class regardless of the declared Side, and
     * a server can never load a class referencing GuiScreen. This message is
     * server-to-client only, so the server never needs to actually handle one.
     */
    public static class NoOpHandler implements IMessageHandler<PacketOpenNameGui, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenNameGui message, MessageContext ctx) {
            return null;
        }
    }
}
