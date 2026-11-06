package com.ayaan.replicatormod.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

    public static KeyBinding undoKey;
    public static KeyBinding rotateKey;

    public static void init() {
        undoKey = new KeyBinding("key.ayaansinfinityreplication.undo", Keyboard.KEY_X, "key.categories.ayaansinfinityreplication");
        ClientRegistry.registerKeyBinding(undoKey);

        rotateKey = new KeyBinding("key.ayaansinfinityreplication.rotate", Keyboard.KEY_R, "key.categories.ayaansinfinityreplication");
        ClientRegistry.registerKeyBinding(rotateKey);
    }
}
