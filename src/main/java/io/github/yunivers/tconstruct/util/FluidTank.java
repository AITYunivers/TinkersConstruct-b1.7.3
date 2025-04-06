package io.github.yunivers.tconstruct.util;

import io.github.yunivers.stationfluidapi.api.FluidStack;
import net.minecraft.block.LiquidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

public class FluidTank
{
    protected FluidStack fluid;
    protected int capacity;
    protected BlockEntity entity;

    public FluidTank(LiquidBlock fluid, int amount, int capacity)
    {
        this(new FluidStack(fluid, amount), capacity);
    }

    public FluidTank(FluidStack stack, int capacity)
    {
        this.fluid = stack;
        this.capacity = capacity;
    }

    public FluidTank(int capacity)
    {
        this.capacity = capacity;
    }

    public void setFluid(FluidStack fluid)
    {
        this.fluid = fluid;
    }

    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
    }

    /* IFluidTank (Forge) */
    public FluidStack getFluid()
    {
        return fluid;
    }

    public int getFluidAmount()
    {
        if (fluid == null)
        {
            return 0;
        }
        return fluid.amount;
    }

    public int getCapacity()
    {
        return capacity;
    }

    public int fill(FluidStack resource, boolean doFill)
    {
        if (resource == null)
        {
            return 0;
        }

        if (!doFill)
        {
            if (fluid == null)
            {
                return Math.min(capacity, resource.amount);
            }

            if (!fluid.isFluidEqual(resource))
            {
                return 0;
            }

            return Math.min(capacity - fluid.amount, resource.amount);
        }

        if (fluid == null)
        {
            fluid = new FluidStack(resource.getFluid(), Math.min(capacity, resource.amount));

            /*if (entity != null)
            {
                FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, entity.world, entity.x, entity.y, entity.z, this, fluid.amount));
            }*/
            return fluid.amount;
        }

        if (!fluid.isFluidEqual(resource))
        {
            return 0;
        }
        int filled = capacity - fluid.amount;

        if (resource.amount < filled)
        {
            fluid.amount += resource.amount;
            filled = resource.amount;
        }
        else
        {
            fluid.amount = capacity;
        }

        /*if (entity != null)
        {
            FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, entity.world, entity.x, entity.y, entity.z, this, filled));
        }*/
        return filled;
    }

    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        if (fluid == null)
        {
            return null;
        }

        int drained = maxDrain;
        if (fluid.amount < drained)
        {
            drained = fluid.amount;
        }

        FluidStack stack = new FluidStack(fluid.getFluid(), drained);
        if (doDrain)
        {
            fluid.amount -= drained;
            if (fluid.amount <= 0)
            {
                fluid = null;
            }

            /*if (entity != null)
            {
                FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(fluid, entity.world, entity.x, entity.y, entity.z, this, drained));
            }*/
        }
        return stack;
    }
}
