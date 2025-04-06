package io.github.yunivers.tconstruct.client.render.block.entity;

import io.github.yunivers.tconstruct.blocks.CraftingStationBlock;
import io.github.yunivers.tconstruct.blocks.entity.CraftingStationEntity;
import io.github.yunivers.tconstruct.events.init.InitListener;
import io.github.yunivers.tconstruct.mixin.MinecraftAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class CraftingStationRenderer extends BlockEntityRenderer
{
    @Override
    public void render(BlockEntity blockEntity, double x, double y, double z, float tickDelta) {
        if (blockEntity instanceof CraftingStationEntity station)
        {
            if (station.world.getBlockId(station.x, station.y, station.z) != InitListener.craftingSlab.id &&
                station.world.getBlockId(station.x, station.y + 1, station.z) != 0)
                return;

            if (station.renderedItems == null || station.renderedItems.length != station.stacks.length)
                station.renderedItems = new ItemEntity[station.stacks.length];
            for (int i = 0; i < station.stacks.length; i++)
            {
                ItemStack stack = station.stacks[i];
                if (stack != null)
                {
                    ItemEntity renderedItem = station.renderedItems[i];
                    if (renderedItem == null || renderedItem.stack.itemId != stack.itemId)
                    {
                        renderedItem = new ItemEntity(MinecraftAccessor.getInstance().world);
                        renderedItem.setPosition(station.x, station.y + 1, station.z);
                        renderedItem.stack = stack.copy().split(1);
                        renderedItem.initialRotationAngle = 0;
                        station.renderedItems[i] = renderedItem;
                    }

                    float itemOffset = 0.1875F; // 3/16
                    float itemOffsetX = i % 3 == 0 ? -itemOffset : (i % 3 == 2 ? itemOffset : 0);
                    float itemOffsetZ = i / 3 == 0 ? -itemOffset : (i / 3 == 2 ? itemOffset : 0);

                    float posX = (float)x + 0.5F;
                    float posY = (float)y + (station.getBlock().id == InitListener.craftingSlab.id ? 0.5F : 1F);
                    float posZ = (float)z + 0.5F;

                    float rotation = 0F;

                    switch (station.world.getBlockState(station.x, station.y, station.z).get(CraftingStationBlock.FACING_PROPERTY))
                    {
                        case NORTH:
                            posX -= itemOffsetX;
                            posZ -= itemOffsetZ;
                            rotation = 0F;
                            break;
                        case EAST:
                            posX += itemOffsetZ;
                            posZ += itemOffsetX;
                            rotation = 90F;
                            break;
                        case SOUTH:
                            posX += itemOffsetX;
                            posZ += itemOffsetZ;
                            rotation = 180F;
                            break;
                        case WEST:
                            posX -= itemOffsetZ;
                            posZ -= itemOffsetX;
                            rotation = 270F;
                            break;
                    }

                    GL11.glPushMatrix();
                    GL11.glTranslatef(posX, posY, posZ);
                    if (stack.getItem() instanceof BlockItem) {
                        GL11.glScalef(0.5F, 0.5F, 0.5F); // 2/16 / 0.25
                        GL11.glRotatef(rotation, 0F, 1F, 0F);
                    }
                    else {
                        GL11.glScalef(0.25F, 0.25F, 0.25F); // 2/16 / 0.5
                        GL11.glRotatef(0F, 0F, 0F, 0F);
                    }
                    EntityRenderDispatcher.INSTANCE.render(renderedItem, 0.0, 0.0, 0.0, 0.0F, 0.0F);
                    GL11.glPopMatrix();
                }
            }
        }
    }
}
