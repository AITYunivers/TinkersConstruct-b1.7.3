package io.github.yunivers.tconstruct.util;

import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockRenderManager;
import net.modificationstation.stationapi.api.client.render.block.StationRendererBlockRenderManager;

public interface ICustomBlockModel {
    boolean renderBlock(BlockRenderManager renderer, Block block, int x, int y, int z);
}
