package com.ayaan.replicatormod.client;

import com.ayaan.replicatormod.clipboard.MaterialRequirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client-only session state: which clipboard is "active" for the Infinity
 * Replicator, and the material requirements last reported by the server for
 * it (used by the HUD overlay). Plain data holder only — no Minecraft client
 * imports — so it's harmless to reference from common-side code too.
 */
public class ClientState {

    private static String activeClipboardName;
    private static List<MaterialRequirement> activeRequirements = new ArrayList<MaterialRequirement>();
    private static int activeSizeX;
    private static int activeSizeY;
    private static int activeSizeZ;
    private static int rotation;

    public static void setActiveClipboard(String name, int sizeX, int sizeY, int sizeZ, List<MaterialRequirement> requirements) {
        activeClipboardName = name;
        activeSizeX = sizeX;
        activeSizeY = sizeY;
        activeSizeZ = sizeZ;
        activeRequirements = requirements;
        rotation = 0;
    }

    public static void setRotation(int steps) {
        rotation = steps & 3;
    }

    public static int getRotation() {
        return rotation;
    }

    public static String getActiveClipboardName() {
        return activeClipboardName;
    }

    public static List<MaterialRequirement> getActiveRequirements() {
        return Collections.unmodifiableList(activeRequirements);
    }

    public static int getActiveSizeX() {
        return activeSizeX;
    }

    public static int getActiveSizeY() {
        return activeSizeY;
    }

    public static int getActiveSizeZ() {
        return activeSizeZ;
    }
}
