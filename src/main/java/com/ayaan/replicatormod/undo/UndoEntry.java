package com.ayaan.replicatormod.undo;

import com.ayaan.replicatormod.clipboard.ClipboardData;
import com.ayaan.replicatormod.util.BlockCoord;

public class UndoEntry {

    public final BlockCoord origin;
    public final ClipboardData snapshot;

    public UndoEntry(BlockCoord origin, ClipboardData snapshot) {
        this.origin = origin;
        this.snapshot = snapshot;
    }
}
