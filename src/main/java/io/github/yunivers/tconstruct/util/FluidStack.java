package io.github.yunivers.tconstruct.util;

import net.minecraft.block.Block;
import net.minecraft.block.LiquidBlock;
import net.minecraft.block.material.FluidMaterial;
import net.minecraft.block.material.Material;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.lang.reflect.Field;

public class FluidStack
{
    public final FluidMaterial fluidType;
    public int amount;
    public NbtCompound tag;

    public FluidStack(String fluidName, int amount)
    {
        this.fluidType = (FluidMaterial)getMaterialByName(fluidName);
        this.amount = amount;
    }

    public FluidStack(FluidMaterial fluidType, int amount)
    {
        this.fluidType = fluidType;
        this.amount = amount;
    }

    public FluidStack(FluidMaterial fluidType, int amount, NbtCompound nbt)
    {
        this(fluidType, amount);
        if (nbt != null)
            tag = nbt;
    }

    public FluidStack(FluidStack stack, int amount)
    {
        this(stack.getFluid(), amount, stack.tag);
    }

    public FluidStack(ItemStack stack)
    {
        if (stack.getItem() instanceof BucketItem item)
        {
            if (item.id == Item.WATER_BUCKET.id)
            {
                this.fluidType = (FluidMaterial) Material.WATER;
                this.amount = 1000;
            }
            else if (item.id == Item.LAVA_BUCKET.id)
            {
                this.fluidType = (FluidMaterial) Material.LAVA;
                this.amount = 1000;
            }
            else
                this.fluidType = null;
        }
        else
            this.fluidType = null;
    }

    public static FluidStack loadFluidStackFromNBT(NbtCompound nbt)
    {
        if (nbt == null)
            return null;

        String fluidName = nbt.getString("FluidName");

        if (fluidName == null || getMaterialByName(fluidName) == null)
            return null;

        FluidStack stack = new FluidStack((FluidMaterial) getMaterialByName(fluidName), nbt.getInt("Amount"));

        if (nbt.contains("Tag"))
        {
            stack.tag = nbt.getCompound("Tag");
        }
        return stack;
    }

    public NbtCompound writeToNBT(NbtCompound nbt)
    {
        nbt.putString("FluidName", getNameFromMaterial(getFluid()));
        nbt.putInt("Amount", amount);

        if (tag != null)
            nbt.put("Tag", tag);
        return nbt;
    }

    public final FluidMaterial getFluid()
    {
        return fluidType;
    }

    public final String getFluidName()
    {
        return getNameFromMaterial(fluidType);
    }

    /**
     * @return A copy of this FluidStack
     */
    public FluidStack copy()
    {
        return new FluidStack(getFluid(), amount, tag);
    }

    /**
     * Determines if the FluidIDs and NBT Tags are equal. This does not check amounts.
     *
     * @param other The FluidStack for comparison
     * @return true if the Fluids (IDs and NBT Tags) are the same
     */
    public boolean isFluidEqual(FluidStack other)
    {
        return other != null && getFluid() == other.getFluid() && isFluidStackTagEqual(other);
    }

    private boolean isFluidStackTagEqual(FluidStack other)
    {
        return tag == null ? other.tag == null : other.tag != null && tag.equals(other.tag);
    }

    /**
     * Determines if the NBT Tags are equal. Useful if the FluidIDs are known to be equal.
     */
    public static boolean areFluidStackTagsEqual(FluidStack stack1, FluidStack stack2)
    {
        return stack1 == null && stack2 == null || (stack1 != null && stack2 != null && stack1.isFluidStackTagEqual(stack2));
    }

    /**
     * Determines if the Fluids are equal and this stack is larger.
     *
     * @return true if this FluidStack contains the other FluidStack (same fluid and >= amount)
     */
    public boolean containsFluid(FluidStack other)
    {
        return isFluidEqual(other) && amount >= other.amount;
    }

    /**
     * Determines if the FluidIDs, Amounts, and NBT Tags are all equal.
     *
     * @param other
     *            - the FluidStack for comparison
     * @return true if the two FluidStacks are exactly the same
     */
    public boolean isFluidStackIdentical(FluidStack other)
    {
        return isFluidEqual(other) && amount == other.amount;
    }

    /**
     * Determines if the FluidIDs and NBT Tags are equal compared to a registered container
     * ItemStack. This does not check amounts.
     *
     * @param other
     *            The ItemStack for comparison
     * @return true if the Fluids (IDs and NBT Tags) are the same
     */
    public boolean isFluidEqual(ItemStack other)
    {
        if (other == null)
        {
            return false;
        }

        if (other.getItem() instanceof BucketItem item)
        {
            if (item.id == Item.WATER_BUCKET.id)
                return fluidType == Material.WATER;
            else if (item.id == Item.LAVA_BUCKET.id)
                return fluidType == Material.LAVA;
        }

        return false;
    }

    public boolean isBright()
    {
        return fluidType == Material.LAVA;
    }

    public static LiquidBlock GetBlock(FluidMaterial fluidType)
    {
        if (fluidType == Material.WATER)
            return (LiquidBlock)Block.WATER;
        else if (fluidType == Material.LAVA)
            return (LiquidBlock)Block.LAVA;
        return null;
    }

    private static Material getMaterialByName(String name)
    {
        try
        {
            Field field = Material.class.getDeclaredField(name.toUpperCase());

            if (field.getType().equals(Material.class))
                return (Material) field.get(null);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            System.err.println("Material not found: " + name);
        }
        return null;
    }

    public static String getNameFromMaterial(Material material) {
        if (material == null)
            return null;

        try
        {
            Field[] fields = Material.class.getDeclaredFields();
            for (Field field : fields)
            {
                if (field.getType().equals(Material.class))
                {
                    Material value = (Material) field.get(null);
                    if (material.equals(value))
                        return field.getName();
                }
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
