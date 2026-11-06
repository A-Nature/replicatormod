package com.ayaan.replicatormod;

import com.ayaan.replicatormod.init.ModItems;
import com.ayaan.replicatormod.network.PacketHandler;
import com.ayaan.replicatormod.undo.UndoManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ReplicatorMod.MODID, name = ReplicatorMod.NAME, version = ReplicatorMod.VERSION)
public class ReplicatorMod {

    public static final String MODID = "ayaansinfinityreplication";
    public static final String NAME = "Ayaan's Replicator Mod";
    public static final String VERSION = "1.0.0";

    // The 1.7.10 custom-payload packet protocol hard-caps channel names at
    // 20 characters (PacketBuffer.readStringFromBuffer(20) in the reader) —
    // MODID is longer, so the network channel keeps its own short, already-
    // tested name instead of reusing MODID.
    public static final String NETWORK_CHANNEL = "replicatormod";

    @Instance(MODID)
    public static ReplicatorMod instance;

    @SidedProxy(clientSide = "com.ayaan.replicatormod.ClientProxy", serverSide = "com.ayaan.replicatormod.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModItems.init();
        ModItems.registerRecipes();
        PacketHandler.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerClientHandlers();
        FMLCommonHandler.instance().bus().register(new UndoManager.LogoutCleanup());
    }
}
