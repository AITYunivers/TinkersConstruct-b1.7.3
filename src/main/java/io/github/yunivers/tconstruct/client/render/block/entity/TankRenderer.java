package io.github.yunivers.tconstruct.client.render.block.entity;

import io.github.yunivers.tconstruct.blocks.entity.LavaTankEntity;
import io.github.yunivers.tconstruct.mixin.MinecraftAccessor;
import io.github.yunivers.tconstruct.mixin.WorldRendererAccessor;
import io.github.yunivers.tconstruct.util.FluidStack;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.FluidMaterial;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.modificationstation.stationapi.api.client.StationRenderAPI;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.client.texture.atlas.ExpandableAtlas;
import net.modificationstation.stationapi.mixin.render.client.BlockRenderManagerAccessor;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class TankRenderer extends BlockEntityRenderer
{
    public static final double FLUID_INSET = 0.005d;

    @Override
    public void render(BlockEntity blockEntity, double x, double y, double z, float tickDelta) {
        if (blockEntity instanceof LavaTankEntity te)
        {
            if(te.containsFluid())
            {
                FluidStack liquid = te.tank.getFluid();

                float height = (float)liquid.amount / (float)te.tank.getCapacity();
                if (te.oldAmount != liquid.amount) {
                    te.compiled = false;
                    te.oldAmount = liquid.amount;
                }

                // Tweening looked non-beta, also had bugs so
                /*if (oldAmount != te.renderOffset) {
                    compiled = false;
                    oldAmount = te.renderOffset;
                }

                if(te.renderOffset > 1.2f || te.renderOffset < -1.2f) {
                    te.renderOffset -= (int) ((te.renderOffset / 12f + 0.1f) * tickDelta);
                }
                else {
                    te.renderOffset = 0;
                }*/

                BlockRenderManager brm = ((WorldRendererAccessor)MinecraftAccessor.getInstance().worldRenderer).getBlockRenderManager();
                RenderFluid(brm, te, x, y, z, height);
            }
            else {
                te.compiled = false;
            }
        }
    }

    private void RenderFluid(BlockRenderManager renderer, LavaTankEntity tank, double x, double y, double z, float height)
    {
        FluidMaterial fluidType = tank.tank.getFluid().fluidType;
        Block block = null;
        if (fluidType == FluidMaterial.WATER)
            block = Block.WATER;
        else if (fluidType == FluidMaterial.LAVA)
            block = Block.LAVA;

        if (block != null)
            renderFluid(block, tank, ((BlockRenderManagerAccessor)renderer).getBlockView(), x, y, z, height);
    }

    public void renderFluid(Block block, LavaTankEntity entity, BlockView blockView, double x, double y, double z, float height)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        LogGLError("renderFluid:1");

        if (!entity.compiled)
            compileList(block, entity, blockView, x, y, z, height);
        GL11.glCallList(entity.list);

        LogGLError("renderFluid:2");
        GL11.glPopMatrix();
    }

    public static void LogGLError(String phase) {
        int var2 = GL11.glGetError();
        if (var2 != 0) {
            String var3 = GLU.gluErrorString(var2);
            System.out.println("########## GL ERROR ##########");
            System.out.println("@ " + phase);
            System.out.println(var2 + ": " + var3);
        }
    }

    private void compileList(Block block, LavaTankEntity entity, BlockView blockView, double x, double y, double z, float height) {
        entity.list = GlAllocationUtils.generateDisplayLists(1);
        GL11.glNewList(entity.list, GL11.GL_COMPILE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        LogGLError("compileList:1");

        int colorMultiplier = block.getColorMultiplier(blockView, entity.x, entity.y, entity.z);
        float r = ((colorMultiplier >> 16) & 255) / 255.0F;
        float g = ((colorMultiplier >> 8) & 255) / 255.0F;
        float b = (colorMultiplier & 255) / 255.0F;

        float brightnessTop = 1.0F;
        float brightnessNorthSouth = 0.8F;
        float brightnessEastWest = 0.6F;

        int meta = blockView.getBlockMeta(entity.x, entity.y, entity.z);
        int texture = block.getTexture(1, meta);

        double size = 1d - FLUID_INSET;
        height -= (float) FLUID_INSET;

        float light = block.getLuminance(blockView, entity.x, entity.y, entity.z);
        LogGLError("compileList:2");

        StationRenderAPI.getBakedModelManager().getAtlas(Atlases.GAME_ATLAS_TEXTURE).bindTexture();

        ExpandableAtlas atlas = Atlases.getTerrain();
        Atlas.Sprite sprite = atlas.getTexture(texture);

        Tessellator t = Tessellator.INSTANCE;
        t.startQuads();
        t.color(brightnessTop * light * r, brightnessTop * light * g, brightnessTop * light * b);
        t.vertex(FLUID_INSET, height, FLUID_INSET, sprite.getEndU(),   sprite.getEndV());
        t.vertex(FLUID_INSET, height, size,        sprite.getEndU(),   sprite.getStartV());
        t.vertex(size,        height, size,        sprite.getStartU(), sprite.getStartV());
        t.vertex(size,        height, FLUID_INSET, sprite.getStartU(), sprite.getEndV());

        LogGLError("compileList:3");

        for (int side = 0; side < 4; ++side) {
            int nx = entity.x + (side == 3 ? 1 : side == 2 ? -1 : 0);
            int nz = entity.z + (side == 1 ? 1 : side == 0 ? -1 : 0);

            int sideTexture = block.getTexture(side + 2, meta);

            double x1, x2, z1, z2;
            if (side == 0) {
                x1 = FLUID_INSET;
                x2 = size;
                z1 = FLUID_INSET;
                z2 = FLUID_INSET;
            } else if (side == 1) {
                x1 = size;
                x2 = FLUID_INSET;
                z1 = size;
                z2 = size;
            } else if (side == 2) {
                x1 = FLUID_INSET;
                x2 = FLUID_INSET;
                z1 = size;
                z2 = FLUID_INSET;
            } else {
                x1 = size;
                x2 = size;
                z1 = FLUID_INSET;
                z2 = size;
            }

            float sideLuminance = block.getLuminance(blockView, nx, entity.y, nz);
            float sideBrightness = (side < 2 ? brightnessNorthSouth : brightnessEastWest) * sideLuminance;

            Atlas.Sprite sideSprite = atlas.getTexture(sideTexture);
            int imgWidth = sideSprite.getWidth();
            int imgHeight = sideSprite.getHeight();
            double widthScale = imgWidth / 16d;
            double heightScale = imgHeight / 16d;

            double u1 = sideSprite.getStartU();
            double u2 = u1 + (sideSprite.getEndU() - u1) / widthScale;
            double v1 = sideSprite.getStartV();
            double v2 = v1 + (sideSprite.getEndV() - v1) / heightScale;
            v2 = v1 + (v2 - v1) * height;

            t.color(brightnessTop * sideBrightness * r, brightnessTop * sideBrightness * g, brightnessTop * sideBrightness * b);
            t.vertex(x1, height,      z1, u1, v1); // Top Left
            t.vertex(x2, height,      z2, u2, v1); // Top Right
            t.vertex(x2, FLUID_INSET, z2, u2, v2); // Bottom Right
            t.vertex(x1, FLUID_INSET, z1, u1, v2); // Bottom Left

            LogGLError("compileList:" + (4 + side));
        }

        block.minY = 0.0F;
        block.maxY = 1.0F;

        t.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEndList();
        entity.compiled = true;
    }
}
