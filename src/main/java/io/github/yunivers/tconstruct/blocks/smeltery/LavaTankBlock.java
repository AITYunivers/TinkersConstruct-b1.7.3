package io.github.yunivers.tconstruct.blocks.smeltery;

import io.github.yunivers.stationfluidapi.api.FluidStack;
import io.github.yunivers.tconstruct.blocks.entity.LavaTankEntity;
import io.github.yunivers.tconstruct.events.init.InitListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.LiquidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.state.StateManager;
import net.modificationstation.stationapi.api.state.property.BooleanProperty;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

import java.util.Random;

public class LavaTankBlock extends TemplateBlockWithEntity
{
    public static final BooleanProperty hasKnob = BooleanProperty.of("has_knob");

    public LavaTankBlock(Identifier identifier) {
        super(identifier, Material.STONE);
        setHardness(3F);
        setResistance(20F);
        setSoundGroup(GLASS_SOUND_GROUP);
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(hasKnob);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public int getTexture(int side) {
        if (side == 0)
            return InitListener.LavaTankBottom;
        else if (side == 1)
            return InitListener.LavaTankTop;
        return InitListener.LavaTankSide;
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new LavaTankEntity();
    }

    @Override
    public boolean onUse(World world, int x, int y, int z, PlayerEntity player)
    {
        ItemStack heldItem = player.inventory.getSelectedItem();
        if (heldItem != null)
        {
            FluidStack liquid = new FluidStack(heldItem);
            LavaTankEntity logic = (LavaTankEntity) world.getBlockEntity(x, y, z);
            if (liquid.fluid != null)
            {
                int amount = logic.fill(liquid, false);
                if (amount == liquid.amount)
                {
                    logic.fill(liquid, true);
                    player.inventory.setStack(player.inventory.selectedSlot, consumeItem(heldItem));
                    return true;
                }
                else
                    return true;
            }
            else if (heldItem.getItem() instanceof BucketItem)
            {
                LiquidBlock fluid = logic.tank.getFluid().fluid;
                if (heldItem.getItem() == Item.BUCKET && logic.tank.getFluidAmount() >= 1000)
                {
                    logic.drain(1000, true);
                    ItemStack stack;
                    if (fluid.id == Block.WATER.id || fluid.id == Block.FLOWING_WATER.id)
                        stack = new ItemStack(Item.WATER_BUCKET);
                    else if (fluid.id == Block.LAVA.id || fluid.id == Block.FLOWING_LAVA.id)
                        stack = new ItemStack(Item.LAVA_BUCKET);
                    else return false;

                    if (heldItem.count == 1)
                    {
                        player.inventory.setStack(player.inventory.selectedSlot, stack);
                    }
                    else
                    {
                        player.inventory.setStack(player.inventory.selectedSlot, consumeItem(heldItem));

                        if (!player.inventory.addStack(stack))
                        {
                            player.dropItem(stack);
                        }
                    }
                    return true;
                }
                else
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static ItemStack consumeItem (ItemStack stack)
    {
        if (stack.count == 1)
        {
            // Would be incompatible with modded fluid containers. Rework later!!!
            if (stack.getItem() instanceof BucketItem && stack.itemId != Item.BUCKET.id)
                return new ItemStack(Item.BUCKET);
            else
                return null;
        }
        else
        {
            stack.split(1);

            return stack;
        }
    }

    /* Updates */
    public void neighborUpdate(World world, int x, int y, int z, int nBlockID)
    {
        BlockEntity entity = world.getBlockEntity(x, y, z);
        if (entity instanceof IServantEntity)
            ((IServantEntity)entity).notifyMasterOfChange();

        updateKnob(world, x, y, z);
        super.neighborUpdate(world, x, y, z, nBlockID);
    }

    @Override
    public int getDroppedItemCount(Random random) {
        return 1;
    }

    /*@Override
    public void onBreak (World world, int x, int y, int z)
    {
        ItemStack stack = new ItemStack(this.id, 1, 0);
        LavaTankEntity logic = (LavaTankEntity) world.getBlockEntity(x, y, z);
        FluidStack liquid = logic.tank.getFluid();
        if (liquid != null)
        {
            NbtCompound tag = new NbtCompound();
            NbtCompound liquidTag = new NbtCompound();
            liquid.writeToNBT(liquidTag);
            tag.put("Fluid", liquidTag);
            stack.readNbt(tag);
        }
        dropTankBlock(world, x, y, z, stack);
    }*/

    protected void dropTankBlock (World world, int x, int y, int z, ItemStack stack)
    {
        if (!world.isRemote)
        {
            float f = 0.7F;
            double d0 = (double) (world.random.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            double d1 = (double) (world.random.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            double d2 = (double) (world.random.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            ItemEntity entityitem = new ItemEntity(world, (double) x + d0, (double) y + d1, (double) z + d2, stack);
            entityitem.pickupDelay = 10;
            world.spawnEntity(entityitem);
        }
    }

    @Override
    public void onPlaced(World world, int x, int y, int z, LivingEntity placer)
    {
        // Very possibly won't work (yep it doesn't) Wait maybe it does (It does!)
        ItemStack stack = ((PlayerEntity)placer).inventory.getSelectedItem();
        NbtCompound nbt = stack.getStationNbt();
        if (nbt.contains("Fluid"))
        {
            NbtCompound liquidTag = nbt.getCompound("Fluid");
            if (liquidTag.contains("FluidID") && liquidTag.contains("Amount"))
            {
                FluidStack liquid = new FluidStack(liquidTag);
                LavaTankEntity logic = (LavaTankEntity) world.getBlockEntity(x, y, z);
                logic.tank.setFluid(liquid);
            }
        }

        updateKnob(world, x, y, z);
        super.onPlaced(world, x, y, z);
    }

    public void updateKnob(World world, int x, int y, int z)
    {
        BlockState state = world.getBlockState(x, y, z);
        state = state.with(hasKnob, world.getBlockId(x, y + 1, z) == 0);
        world.setBlockState(x, y, z, state);
    }
}
