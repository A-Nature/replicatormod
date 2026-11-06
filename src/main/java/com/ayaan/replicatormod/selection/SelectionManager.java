package com.ayaan.replicatormod.selection;

import com.ayaan.replicatormod.util.BlockCoord;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side, per-player point A / point B selection state for the Infinity Copier.
 */
public class SelectionManager {

    private static final Map<UUID, BlockCoord> POINT_A = new HashMap<UUID, BlockCoord>();
    private static final Map<UUID, BlockCoord> POINT_B = new HashMap<UUID, BlockCoord>();

    public static void setPointA(UUID player, BlockCoord coord) {
        POINT_A.put(player, coord);
    }

    public static void setPointB(UUID player, BlockCoord coord) {
        POINT_B.put(player, coord);
    }

    public static BlockCoord getPointA(UUID player) {
        return POINT_A.get(player);
    }

    public static BlockCoord getPointB(UUID player) {
        return POINT_B.get(player);
    }

    public static void clear(UUID player) {
        POINT_A.remove(player);
        POINT_B.remove(player);
    }
}
