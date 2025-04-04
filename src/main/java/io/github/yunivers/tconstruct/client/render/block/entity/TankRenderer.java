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
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.modificationstation.stationapi.mixin.render.client.BlockRenderManagerAccessor;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import static org.lwjgl.opengl.GL11.*;

public class TankRenderer extends BlockEntityRenderer
{
    // This will be applied to the renderer once it actually works
    public static final float FLUID_INSET = 0.005f;
    private boolean compiled;
    private int list;
    private float oldAmount;

    @Override
    public void render(BlockEntity blockEntity, double x, double y, double z, float tickDelta) {
        if (blockEntity instanceof LavaTankEntity te)
        {
            if(te.containsFluid()) {
                FluidStack liquid = te.tank.getFluid();

                float height = ((float)liquid.amount - te.renderOffset) / (float)te.tank.getCapacity();
                if (oldAmount != te.renderOffset) {
                    compiled = false;
                    oldAmount = te.renderOffset;
                }

                if(te.renderOffset > 1.2f || te.renderOffset < -1.2f) {
                    te.renderOffset -= (int) ((te.renderOffset / 12f + 0.1f) * tickDelta);
                }
                else {
                    te.renderOffset = 0;
                }

                BlockRenderManager brm = ((WorldRendererAccessor)MinecraftAccessor.getInstance().worldRenderer).getBlockRenderManager();
                RenderFluid(brm, te, x, y, z, height);
            }
        }
    }

    private void RenderFluid(BlockRenderManager renderer, LavaTankEntity tank, double x, double y, double z, float height)
    {
        FluidMaterial fluidType = tank.tank.getFluid().fluidType;
        Block block = null;
        if (fluidType == FluidMaterial.WATER)
            block = Block.FLOWING_WATER;
        else if (fluidType == FluidMaterial.LAVA)
            block = Block.FLOWING_LAVA;

        // Using +2 for debugging
        if (block != null)
            renderFluid(block, tank, ((BlockRenderManagerAccessor)renderer).getBlockView(), x, y, z, height);
    }

    public void renderFluid(Block block, LavaTankEntity entity, BlockView blockView, double x, double y, double z, float height)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        LogGLError("renderFluid:1");

        compileList(block, entity, blockView, x, y, z, height);
        GL11.glCallList(this.list);

        LogGLError("renderFluid:2");
//        glPolygonMode(GL_FRONT_AND_BACK, GL11.GL_FILL);
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
        this.list = GlAllocationUtils.generateDisplayLists(1);
        GL11.glNewList(this.list, GL11.GL_COMPILE);

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
        int u = (texture & 15) << 4;
        int v = texture & 240;

        double uMid = (u + 8) / 256.0;
        double vMid = (v + 8) / 256.0;
        double uMax = (u + 16) / 256.0;
        double vMax = (v + 16) / 256.0;

        float sin0 = MathHelper.sin(0) * 8.0F / 256.0F;
        float cos0 = MathHelper.cos(0) * 8.0F / 256.0F;

        float light = block.getLuminance(blockView, entity.x, entity.y, entity.z);
        LogGLError("compileList:2");

        MinecraftAccessor.getInstance().textureManager.bindTexture(texture);
        Tessellator t = Tessellator.INSTANCE;
        t.startQuads();
        t.color(brightnessTop * light * r, brightnessTop * light * g, brightnessTop * light * b);
        t.vertex(0, height, 0, uMax - cos0 - sin0, vMax - cos0 + sin0);
        t.vertex(0, height, 1, uMax - cos0 + sin0, vMax + cos0 + sin0);
        t.vertex(1, height, 1, uMax + cos0 + sin0, vMax + cos0 - sin0);
        t.vertex(1, height, 0, uMax + cos0 - sin0, vMax - cos0 - sin0);

        LogGLError("compileList:3");

        for (int side = 0; side < 4; ++side) {
            int nx = entity.x + (side == 3 ? 1 : side == 2 ? -1 : 0);
            int nz = entity.z + (side == 1 ? 1 : side == 0 ? -1 : 0);

            int sideTexture = block.getTexture(side + 2, meta);
            int su = (sideTexture & 15) << 4;
            int sv = sideTexture & 240;

            double x1, x2, z1, z2;
            if (side == 0) {
                x1 = 0;
                x2 = 1;
                z1 = 0;
                z2 = 0;
            } else if (side == 1) {
                x1 = 1;
                x2 = 0;
                z1 = 1;
                z2 = 1;
            } else if (side == 2) {
                x1 = 0;
                x2 = 0;
                z1 = 1;
                z2 = 0;
            } else {
                x1 = 1;
                x2 = 1;
                z1 = 0;
                z2 = 1;
            }

            double u0 = su / 256.0;
            double u1 = (su + 16 - 0.01) / 256.0;
            double v0 = (sv + (1.0F - height) * 16.0F) / 256.0;
            double v1 = (sv + (1.0F - height) * 16.0F) / 256.0;
            double v2 = (sv + 16 - 0.01) / 256.0;

            float sideLuminance = block.getLuminance(blockView, nx, entity.y, nz);
            float sideBrightness = (side < 2 ? brightnessNorthSouth : brightnessEastWest) * sideLuminance;

            t.color(brightnessTop * sideBrightness * r, brightnessTop * sideBrightness * g, brightnessTop * sideBrightness * b);
            t.vertex(x1, height, z1, u0, v0);
            t.vertex(x2, height, z2, u1, v1);
            t.vertex(x2, 0, z2, u1, v2);
            t.vertex(x1, 0, z1, u0, v2);

            LogGLError("compileList:" + (4 + side));
        }

        block.minY = 0.0F;
        block.maxY = 1.0F;

        t.draw();
        GL11.glEndList();
        this.compiled = true;
    }
}
