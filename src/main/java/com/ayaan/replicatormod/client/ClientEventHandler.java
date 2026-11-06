package com.ayaan.replicatormod.client;

import com.ayaan.replicatormod.init.ModItems;
import com.ayaan.replicatormod.network.PacketHandler;
import com.ayaan.replicatormod.network.PacketSetRotation;
import com.ayaan.replicatormod.network.PacketUndo;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ClientEventHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }

        while (KeyBindings.undoKey.isPressed()) {
            PacketHandler.INSTANCE.sendToServer(new PacketUndo());
        }

        ItemStack held = mc.thePlayer.getCurrentEquippedItem();
        boolean holdingReplicator = held != null && held.getItem() == ModItems.infinityReplicator;
        while (KeyBindings.rotateKey.isPressed()) {
            if (holdingReplicator) {
                ClientState.setRotation(ClientState.getRotation() + 1);
                PacketHandler.INSTANCE.sendToServer(new PacketSetRotation(ClientState.getRotation()));
            }
        }
    }
}
