package com.ayaan.replicatormod.clipboard;

import com.ayaan.replicatormod.ReplicatorMod;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Saves/loads/lists per-player clipboards as gzipped NBT under the world save
 * folder. Clipboards are private: each player's files live in their own
 * UUID-named subfolder.
 */
public class ClipboardStorage {

    private static File clipboardRoot() {
        File saveDir = MinecraftServer.getServer().worldServerForDimension(0).getSaveHandler().getWorldDirectory();
        return new File(saveDir, ReplicatorMod.MODID + "/clipboards");
    }

    private static File playerDir(UUID uuid) {
        File dir = new File(clipboardRoot(), uuid.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /** Strips anything that isn't safe as a bare filename component. */
    public static String sanitizeName(String rawName) {
        String cleaned = rawName == null ? "" : rawName.replaceAll("[^a-zA-Z0-9 _-]", "").trim();
        if (cleaned.isEmpty()) {
            cleaned = "clipboard";
        }
        if (cleaned.length() > 64) {
            cleaned = cleaned.substring(0, 64);
        }
        return cleaned;
    }

    public static void save(UUID uuid, String name, ClipboardData data) throws IOException {
        File file = new File(playerDir(uuid), sanitizeName(name) + ".nbt");
        CompressedStreamTools.writeCompressed(data.writeToNBT(), new FileOutputStream(file));
    }

    public static ClipboardData load(UUID uuid, String name) throws IOException {
        File file = new File(playerDir(uuid), sanitizeName(name) + ".nbt");
        if (!file.exists()) {
            return null;
        }
        NBTTagCompound tag = CompressedStreamTools.readCompressed(new FileInputStream(file));
        return ClipboardData.readFromNBT(tag);
    }

    public static List<String> list(UUID uuid) {
        File[] files = playerDir(uuid).listFiles();
        List<String> names = new ArrayList<String>();
        if (files != null) {
            for (File f : files) {
                String n = f.getName();
                if (n.endsWith(".nbt")) {
                    names.add(n.substring(0, n.length() - 4));
                }
            }
        }
        Collections.sort(names);
        return names;
    }
}
