package com.ayaan.replicatormod.network;

import com.ayaan.replicatormod.ReplicatorMod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ReplicatorMod.NETWORK_CHANNEL);

        // preInit runs on every physical side, and registerMessage always
        // instantiates the handler class it's given regardless of the Side
        // argument. A dedicated server must never instantiate a handler that
        // touches GuiScreen/Minecraft, so those two swap in a no-op handler
        // server-side; the discriminator order below stays identical on both
        // sides either way, so client/server still agree on every packet ID.
        boolean isClient = FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
        Class<? extends IMessageHandler<PacketOpenNameGui, IMessage>> openNameGuiHandler =
                isClient ? PacketOpenNameGui.Handler.class : PacketOpenNameGui.NoOpHandler.class;
        Class<? extends IMessageHandler<PacketClipboardListResponse, IMessage>> clipboardListHandler =
                isClient ? PacketClipboardListResponse.Handler.class : PacketClipboardListResponse.NoOpHandler.class;

        int id = 0;
        INSTANCE.registerMessage(openNameGuiHandler, PacketOpenNameGui.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSaveClipboard.Handler.class, PacketSaveClipboard.class, id++, Side.SERVER);
        INSTANCE.registerMessage(clipboardListHandler, PacketClipboardListResponse.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(PacketActivateClipboard.Handler.class, PacketActivateClipboard.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketMaterialsResponse.Handler.class, PacketMaterialsResponse.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(PacketUndo.Handler.class, PacketUndo.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketSetRotation.Handler.class, PacketSetRotation.class, id++, Side.SERVER);
    }
}
