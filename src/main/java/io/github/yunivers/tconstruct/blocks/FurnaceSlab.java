package io.github.yunivers.tconstruct.blocks;

import io.github.yunivers.tconstruct.events.init.InitListener;
import io.github.yunivers.tconstruct.mixin.FurnaceBlockAccessor;
import io.github.yunivers.tconstruct.mixin.MinecraftAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.state.StateManager;
import net.modificationstation.stationapi.api.state.property.IntProperty;
import net.modificationstation.stationapi.api.template.block.BlockTemplate;
import net.modificationstation.stationapi.api.util.Identifier;

import java.util.Random;

public class FurnaceSlab extends FurnaceBlock implements BlockTemplate
{
    public static final IntProperty LUMINANCE_PROPERTY = IntProperty.of("luminance", 0, 15);

    public FurnaceSlab(Identifier identifier, boolean lit) {
        super(BlockTemplate.getNextId(), lit);
        BlockTemplate.onConstructor(this, identifier);

        setHardness(3.5F);
        setSoundGroup(STONE_SOUND_GROUP);
        ignoreMetaUpdates();
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);

        setDefaultState(getStateManager().getDefaultState().with(LUMINANCE_PROPERTY, 0));
        setLuminance((blockState) -> blockState.get(LUMINANCE_PROPERTY));
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LUMINANCE_PROPERTY);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new FurnaceBlockEntity();
    }

    @Environment(EnvType.CLIENT)
    public int getTextureId(BlockView blockView, int x, int y, int z, int side) {
        if (side == 0)
            return this.textureId + 17;
        else if (side == 1)
            return this.textureId + 17;

        int var6 = blockView.getBlockMeta(x, y, z);
        if (side != var6) {
            return InitListener.FurnaceSlabSide;
        } else {
            return isLit(blockView.getBlockEntity(x, y, z)) ? InitListener.FurnaceSlabFrontActive : InitListener.FurnaceSlabFront;
        }
    }

    public boolean isOpaque() {
        return false;
    }

    public int getDroppedItemId(int blockMeta, Random random) {
        return InitListener.furnaceSlab.id;
    }

    public int getDroppedItemCount(Random random) {
        return 1;
    }

    protected int getDroppedItemMeta(int blockMeta) {
        return blockMeta;
    }

    public boolean isFullCube() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public int getRenderType() {
        return 16;
    }

    @Environment(EnvType.CLIENT)
    public boolean isSideVisible(BlockView blockView, int x, int y, int z, int side) {
        if (this != InitListener.craftingSlab) {
            super.isSideVisible(blockView, x, y, z, side);
        }

        if (side == 1) {
            return true;
        } else if (!super.isSideVisible(blockView, x, y, z, side)) {
            return false;
        } else if (side == 0) {
            return true;
        } else {
            return blockView.getBlockId(x, y, z) != this.id;
        }
    }

    public static boolean isLit(BlockView blockView, int x, int y, int z)
    {
        return ((FurnaceBlockEntity)blockView.getBlockEntity(x, y, z)).burnTime > 0;
    }

    public static boolean isLit(BlockEntity blockEntity)
    {
        return ((FurnaceBlockEntity)blockEntity).burnTime > 0;
    }

    public static void updateLitState(boolean lit, World world, int x, int y, int z) {
        int var5 = world.getBlockMeta(x, y, z);
        BlockEntity var6 = world.getBlockEntity(x, y, z);
        FurnaceBlockAccessor.setIgnoreBlockRemoval(true);
        world.setBlock(x, y, z, InitListener.furnaceSlab.id);
        BlockState blockState = world.getBlockState(x, y, z);
        world.setBlockState(x, y, z, blockState.with(LUMINANCE_PROPERTY, isLit(var6) ? 13 : 0));
        FurnaceBlockAccessor.setIgnoreBlockRemoval(false);
        world.setBlockMeta(x, y, z, var5);
        var6.cancelRemoval();
        world.setBlockEntity(x, y, z, var6);
    }

    public static BlockPos getPositionInChunk(int x, int y, int z) {
        BlockPos pos = new BlockPos(x % 16, y, z % 16);
        if (x < 0)
            pos = pos.add(16, 0, 0);
        if (z < 0)
            pos = pos.add(0, 0, 16);
        return pos;
    }

    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        if (this.isLit(world.getBlockEntity(x, y, z))) {
            int var6 = world.getBlockMeta(x, y, z);
            float var7 = (float)x + 0.5F;
            float var8 = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float var9 = (float)z + 0.5F;
            float var10 = 0.52F;
            float var11 = random.nextFloat() * 0.6F - 0.3F;
            if (var6 == 4) {
                world.addParticle("smoke", (double)(var7 - var10), (double)var8, (double)(var9 + var11), (double)0.0F, (double)0.0F, (double)0.0F);
                world.addParticle("flame", (double)(var7 - var10), (double)var8, (double)(var9 + var11), (double)0.0F, (double)0.0F, (double)0.0F);
            } else if (var6 == 5) {
                world.addParticle("smoke", (double)(var7 + var10), (double)var8, (double)(var9 + var11), (double)0.0F, (double)0.0F, (double)0.0F);
                world.addParticle("flame", (double)(var7 + var10), (double)var8, (double)(var9 + var11), (double)0.0F, (double)0.0F, (double)0.0F);
            } else if (var6 == 2) {
                world.addParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 - var10), (double)0.0F, (double)0.0F, (double)0.0F);
                world.addParticle("flame", (double)(var7 + var11), (double)var8, (double)(var9 - var10), (double)0.0F, (double)0.0F, (double)0.0F);
            } else if (var6 == 3) {
                world.addParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 + var10), (double)0.0F, (double)0.0F, (double)0.0F);
                world.addParticle("flame", (double)(var7 + var11), (double)var8, (double)(var9 + var10), (double)0.0F, (double)0.0F, (double)0.0F);
            }

        }
    }

    @Environment(EnvType.CLIENT)
    public float getLuminance(BlockView blockView, int x, int y, int z) {
        BlockEntity entity = blockView.getBlockEntity(x, y, z);
        int luminance = 0;
        if (entity != null && entity.getBlock().id == id && isLit(entity))
            luminance = (int)(15.0F * 0.875f);
        return blockView.getNaturalBrightness(x, y, z, luminance);
    }
}
