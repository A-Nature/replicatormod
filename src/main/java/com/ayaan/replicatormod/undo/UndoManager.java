package com.ayaan.replicatormod.undo;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side, in-memory-only undo stacks, one per player. Intentionally not
 * persisted to disk: cleared on logout and on server restart.
 */
public class UndoManager {

    private static final Map<UUID, Deque<UndoEntry>> STACKS = new HashMap<UUID, Deque<UndoEntry>>();

    public static void push(UUID player, UndoEntry entry) {
        Deque<UndoEntry> stack = STACKS.get(player);
        if (stack == null) {
            stack = new ArrayDeque<UndoEntry>();
            STACKS.put(player, stack);
        }
        stack.push(entry);
    }

    public static UndoEntry pop(UUID player) {
        Deque<UndoEntry> stack = STACKS.get(player);
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.pop();
    }

    public static void clear(UUID player) {
        STACKS.remove(player);
    }

    public static class LogoutCleanup {
        @SubscribeEvent
        public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            clear(event.player.getUniqueID());
        }
    }
}
