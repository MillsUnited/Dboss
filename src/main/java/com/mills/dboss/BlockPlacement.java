package com.mills.dboss;

import org.bukkit.block.BlockFace;

public class BlockPlacement {
    final int x, y, z;
    final BlockFace face;

    public BlockPlacement(int x, int y, int z, BlockFace face) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
    }
}
