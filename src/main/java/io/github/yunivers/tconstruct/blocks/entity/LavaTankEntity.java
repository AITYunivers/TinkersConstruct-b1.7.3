package io.github.yunivers.tconstruct.blocks.entity;

import io.github.yunivers.tconstruct.blocks.smeltery.MultiServantEntity;
import io.github.yunivers.tconstruct.util.FluidStack;
import io.github.yunivers.tconstruct.util.FluidTank;
import net.minecraft.block.Block;
import net.minecraft.block.LiquidBlock;
import net.minecraft.nbt.NbtCompound;

public class LavaTankEntity extends MultiServantEntity
{
    public FluidTank tank;
    public int renderOffset;

    // For rendering
    public boolean compiled;
    public int list;
    public float oldAmount;

    public LavaTankEntity()
    {
        tank = new FluidTank(1000 * 4); // 4 Buckets
    }

    public int fill(FluidStack resource, boolean doFill)
    {
        int amount = tank.fill(resource, doFill);
        if (amount > 0 && doFill)
        {
            renderOffset = resource.amount;
            world.setBlockDirty(x, y, z);
        }

        return amount;
    }

    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        FluidStack amount = tank.drain(maxDrain, doDrain);
        if (amount != null && doDrain)
        {
            renderOffset = -maxDrain;
            world.setBlockDirty(x, y, z);
        }
        return amount;
    }

    public float getFluidAmountScaled ()
    {
        return (float) (tank.getFluid().amount - renderOffset) / (float) (tank.getCapacity() * 1.01F);
    }

    public boolean containsFluid ()
    {
        return tank.getFluid() != null;
    }

    public int getBrightness ()
    {
        if (containsFluid())
        {
            LiquidBlock block = FluidStack.GetBlock(tank.getFluid().fluidType);
            if (tank.getFluid().isBright() && block != null)
                return Block.BLOCKS_LIGHT_LUMINANCE[block.id];
        }
        return 0;
    }

    @Override
    public void readNbt (NbtCompound tags)
    {
        super.readNbt(tags);
        readCustomNBT(tags);
    }

    @Override
    public void writeNbt (NbtCompound tags)
    {
        super.writeNbt(tags);
        writeCustomNBT(tags);
    }

    public void readCustomNBT (NbtCompound tags)
    {
        if (tags.getBoolean("hasFluid"))
            tank.setFluid(new FluidStack(tags.getString("fluidName"), tags.getInt("amount")));
        else
            tank.setFluid(null);
    }

    public void writeCustomNBT (NbtCompound tags)
    {
        FluidStack liquid = tank.getFluid();
        tags.putBoolean("hasFluid", liquid != null);
        if (liquid != null)
        {
            tags.putString("fluidName", liquid.getFluidName());
            tags.putInt("amount", liquid.amount);
        }
    }

    /* Updating */
    public boolean canUpdate ()
    {
        return true;
    }

    public void updateEntity ()
    {
        if (renderOffset > 0)
        {
            renderOffset -= 6;
            world.setBlockDirty(x, y, z);
        }
    }
}
