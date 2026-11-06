package com.ayaan.replicatormod.clipboard;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;

import java.util.HashSet;
import java.util.Set;

/**
 * Best-effort rotation of block orientation metadata to match a 90-degrees-
 * per-step turn of the whole structure around the Y axis. The compass cycle
 * used throughout (south -> west -> north -> east -> south) and each table
 * below were derived directly from the decompiled 1.7.10 placement code
 * (ItemDoor, BlockBed, BlockStairs, BlockTorch, BlockLadder, BlockFurnace),
 * not guessed. Blocks outside this set (rails, logs, anvils, etc.) keep their
 * original metadata on rotation — their position still moves with the
 * structure, just not their facing. Documented known simplification.
 */
public final class BlockRotation {

    private static final int[] STAIR_STEP = {2, 3, 1, 0};
    private static final int[] TORCH_STEP = {0, 3, 4, 2, 1, 5};
    private static final int[] LADDER_STEP = {0, 0, 5, 4, 2, 3};
    private static final int[] DIRECTIONAL_STEP = {0, 1, 5, 4, 2, 3};

    private static final Set<Block> STANDARD_DIRECTIONAL = new HashSet<Block>();
    static {
        STANDARD_DIRECTIONAL.add(Blocks.furnace);
        STANDARD_DIRECTIONAL.add(Blocks.lit_furnace);
        STANDARD_DIRECTIONAL.add(Blocks.dispenser);
        STANDARD_DIRECTIONAL.add(Blocks.dropper);
        STANDARD_DIRECTIONAL.add(Blocks.chest);
        STANDARD_DIRECTIONAL.add(Blocks.ender_chest);
    }

    private BlockRotation() {
    }

    public static int rotateMeta(Block block, int meta, int steps) {
        int result = meta;
        int n = steps & 3;
        for (int i = 0; i < n; i++) {
            result = rotateMetaOnce(block, result);
        }
        return result;
    }

    private static int rotateMetaOnce(Block block, int meta) {
        if (block instanceof BlockDoor) {
            if ((meta & 8) != 0) {
                return meta;
            }
            return (((meta & 3) + 1) & 3) | (meta & 4);
        }
        if (block instanceof BlockBed) {
            // Bits 0-1 store the foot->head compass direction directly (same
            // cycle as doors); bits 2 (occupied) and 3 (head half) untouched.
            return (((meta & 3) + 1) & 3) | (meta & 0xC);
        }
        if (block instanceof BlockStairs) {
            return STAIR_STEP[meta & 3] | (meta & 4);
        }
        if (block instanceof BlockTorch) {
            return meta >= 1 && meta <= 5 ? TORCH_STEP[meta] : meta;
        }
        if (block instanceof BlockLadder) {
            return meta >= 2 && meta <= 5 ? LADDER_STEP[meta] : meta;
        }
        if (STANDARD_DIRECTIONAL.contains(block)) {
            return meta >= 2 && meta <= 5 ? DIRECTIONAL_STEP[meta] : meta;
        }
        return meta;
    }
}
