package com.ayaan.replicatormod;

import com.ayaan.replicatormod.client.ClientEventHandler;
import com.ayaan.replicatormod.client.hud.ChecklistHudOverlay;
import com.ayaan.replicatormod.client.hud.PastePreviewRenderer;
import com.ayaan.replicatormod.client.KeyBindings;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerClientHandlers() {
        KeyBindings.init();
        ClientEventHandler handler = new ClientEventHandler();
        FMLCommonHandler.instance().bus().register(handler);
        MinecraftForge.EVENT_BUS.register(handler);
        MinecraftForge.EVENT_BUS.register(new ChecklistHudOverlay());
        MinecraftForge.EVENT_BUS.register(new PastePreviewRenderer());
    }
}
