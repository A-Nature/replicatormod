package com.ayaan.replicatormod.clipboard;

import com.ayaan.replicatormod.util.BlockCoord;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An in-memory snapshot of a rectangular region of blocks (+ tile entity data),
 * relative to its own minimum corner. Air is stored explicitly so a paste
 * always overwrites the full target box, not just non-air blocks.
 */
public class ClipboardData {

    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    private final BlockEntry[] blocks;

    private ClipboardData(int sizeX, int sizeY, int sizeZ, BlockEntry[] blocks) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.blocks = blocks;
    }

    private int index(int x, int y, int z) {
        return (x * sizeY + y) * sizeZ + z;
    }

    /**
     * @param stripInventoryContents if true, containers (chests, furnaces,
     *        hoppers, etc.) are captured empty. Used for the Copier's saved
     *        clipboards, so pasting one repeatedly can't duplicate whatever
     *        items happened to be sitting inside at copy time. Undo snapshots
     *        must NOT strip contents — they need to restore exactly what was
     *        overwritten, including a real chest's real contents.
     */
    public static ClipboardData capture(World world, BlockCoord a, BlockCoord b, boolean stripInventoryContents) {
        int minX = BlockCoord.min(a.x, b.x);
        int minY = BlockCoord.min(a.y, b.y);
        int minZ = BlockCoord.min(a.z, b.z);
        int maxX = BlockCoord.max(a.x, b.x);
        int maxY = BlockCoord.max(a.y, b.y);
        int maxZ = BlockCoord.max(a.z, b.z);

        int sx = maxX - minX + 1;
        int sy = maxY - minY + 1;
        int sz = maxZ - minZ + 1;

        BlockEntry[] entries = new BlockEntry[sx * sy * sz];
        ClipboardData data = new ClipboardData(sx, sy, sz, entries);

        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    int wx = minX + x;
                    int wy = minY + y;
                    int wz = minZ + z;

                    Block block = world.getBlock(wx, wy, wz);
                    int meta = world.getBlockMetadata(wx, wy, wz);
                    String name = (String) Block.blockRegistry.getNameForObject(block);

                    NBTTagCompound teNbt = null;
                    TileEntity te = world.getTileEntity(wx, wy, wz);
                    if (te != null && !(stripInventoryContents && te instanceof IInventory)) {
                        teNbt = new NBTTagCompound();
                        te.writeToNBT(teNbt);
                    }

                    entries[data.index(x, y, z)] = new BlockEntry(name, meta, teNbt);
                }
            }
        }

        return data;
    }

    public void paste(World world, BlockCoord origin) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    BlockEntry entry = blocks[index(x, y, z)];
                    int wx = origin.x + x;
                    int wy = origin.y + y;
                    int wz = origin.z + z;

                    Block block = (Block) Block.blockRegistry.getObject(entry.blockName);
                    if (block == null) {
                        block = Blocks.air;
                    }

                    // Flag 2 = client render update only, no neighbor-change
                    // notification. Placing a multi-block structure one
                    // block at a time with notifications on would let blocks
                    // like doors/beds see their other half as "missing" mid
                    // paste and immediately break themselves, dropping an
                    // item. World.setBlock: bit0=notify neighbors, bit1=sync/render.
                    world.setBlock(wx, wy, wz, block, entry.meta, 2);

                    if (entry.tileEntityNbt != null) {
                        NBTTagCompound teNbt = (NBTTagCompound) entry.tileEntityNbt.copy();
                        teNbt.setInteger("x", wx);
                        teNbt.setInteger("y", wy);
                        teNbt.setInteger("z", wz);
                        TileEntity te = TileEntity.createAndLoadEntity(teNbt);
                        if (te != null) {
                            world.setTileEntity(wx, wy, wz, te);
                        }
                    } else {
                        world.removeTileEntity(wx, wy, wz);
                    }
                }
            }
        }

        // Chests, furnaces, dispensers etc. run onBlockAdded inside setBlock
        // (regardless of flags) and recompute their facing from neighbouring
        // blocks, overwriting the metadata we just set. Re-apply the saved
        // metadata now that every block is in place.
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    BlockEntry entry = blocks[index(x, y, z)];
                    int wx = origin.x + x;
                    int wy = origin.y + y;
                    int wz = origin.z + z;
                    if (world.getBlockMetadata(wx, wy, wz) != entry.meta
                            && world.getBlock(wx, wy, wz) != Blocks.air) {
                        world.setBlockMetadataWithNotify(wx, wy, wz, entry.meta, 2);
                    }
                }
            }
        }
    }

    /** Returns a new ClipboardData rotated by 90 degrees * steps around the Y axis. */
    public ClipboardData rotate(int steps) {
        ClipboardData current = this;
        int n = steps & 3;
        for (int i = 0; i < n; i++) {
            current = current.rotateOnce();
        }
        return current;
    }

    private ClipboardData rotateOnce() {
        int newSizeX = sizeZ;
        int newSizeZ = sizeX;
        ClipboardData rotated = new ClipboardData(newSizeX, sizeY, newSizeZ, new BlockEntry[sizeX * sizeY * sizeZ]);

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    BlockEntry entry = blocks[index(x, y, z)];
                    int newX = sizeZ - 1 - z;
                    int newZ = x;

                    Block block = (Block) Block.blockRegistry.getObject(entry.blockName);
                    int newMeta = block != null ? BlockRotation.rotateMeta(block, entry.meta, 1) : entry.meta;

                    rotated.blocks[rotated.index(newX, y, newZ)] = new BlockEntry(entry.blockName, newMeta, entry.tileEntityNbt);
                }
            }
        }
        return rotated;
    }

    /** Block registry name + meta -> required count, excluding air. */
    public Map<String, Integer> computeMaterialRequirements() {
        Map<String, Integer> requirements = new LinkedHashMap<String, Integer>();
        for (BlockEntry entry : blocks) {
            if ("minecraft:air".equals(entry.blockName)) {
                continue;
            }
            String key = entry.blockName + ":" + entry.meta;
            Integer count = requirements.get(key);
            requirements.put(key, count == null ? 1 : count + 1);
        }
        return requirements;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound root = new NBTTagCompound();
        root.setInteger("SizeX", sizeX);
        root.setInteger("SizeY", sizeY);
        root.setInteger("SizeZ", sizeZ);

        NBTTagList list = new NBTTagList();
        for (BlockEntry entry : blocks) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Name", entry.blockName);
            tag.setByte("Meta", (byte) entry.meta);
            if (entry.tileEntityNbt != null) {
                tag.setTag("TE", entry.tileEntityNbt);
            }
            list.appendTag(tag);
        }
        root.setTag("Blocks", list);
        return root;
    }

    public static ClipboardData readFromNBT(NBTTagCompound root) {
        int sx = root.getInteger("SizeX");
        int sy = root.getInteger("SizeY");
        int sz = root.getInteger("SizeZ");

        NBTTagList list = root.getTagList("Blocks", 10); // 10 = TAG_Compound
        BlockEntry[] entries = new BlockEntry[sx * sy * sz];
        for (int i = 0; i < entries.length; i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            String name = tag.getString("Name");
            int meta = tag.getByte("Meta") & 0xFF;
            NBTTagCompound teNbt = tag.hasKey("TE") ? tag.getCompoundTag("TE") : null;
            entries[i] = new BlockEntry(name, meta, teNbt);
        }

        return new ClipboardData(sx, sy, sz, entries);
    }

    public static class BlockEntry {
        public final String blockName;
        public final int meta;
        public final NBTTagCompound tileEntityNbt;

        BlockEntry(String blockName, int meta, NBTTagCompound tileEntityNbt) {
            this.blockName = blockName;
            this.meta = meta;
            this.tileEntityNbt = tileEntityNbt;
        }
    }
}
