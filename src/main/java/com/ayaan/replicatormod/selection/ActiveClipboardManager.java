package com.ayaan.replicatormod.selection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side record of which saved clipboard each player has activated in
 * their Infinity Replicator's picker GUI. Set when the client sends
 * PacketActivateClipboard; read back when that player pastes, so pasting
 * doesn't need its own round trip.
 */
public class ActiveClipboardManager {

    private static final Map<UUID, String> ACTIVE = new HashMap<UUID, String>();
    private static final Map<UUID, Integer> ROTATION = new HashMap<UUID, Integer>();

    public static void setActive(UUID player, String clipboardName) {
        ACTIVE.put(player, clipboardName);
        ROTATION.put(player, 0);
    }

    public static String getActive(UUID player) {
        return ACTIVE.get(player);
    }

    public static void setRotation(UUID player, int rotationSteps) {
        ROTATION.put(player, rotationSteps & 3);
    }

    public static int getRotation(UUID player) {
        Integer r = ROTATION.get(player);
        return r == null ? 0 : r;
    }

    public static void clear(UUID player) {
        ACTIVE.remove(player);
        ROTATION.remove(player);
    }
}
