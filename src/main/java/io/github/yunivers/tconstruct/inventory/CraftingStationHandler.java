package io.github.yunivers.tconstruct.inventory;

import io.github.yunivers.tconstruct.blocks.entity.CraftingStationEntity;
import io.github.yunivers.tconstruct.events.init.InitListener;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class CraftingStationHandler extends ScreenHandler
{
    private CraftingStationEntity logic;
    private World worldObj;
    private int posX;
    private int posY;
    private int posZ;

    public CraftingStationHandler(PlayerInventory inventorplayer, CraftingStationEntity tileEntity, int x, int y, int z)
    {
        this.worldObj = tileEntity.world;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.logic = tileEntity;
        tileEntity.handler = this;

        this.addSlot(new CraftingResultSlot(inventorplayer.player, tileEntity, tileEntity.result, 0, 124, 35));

        int row;
        int column;

        for (row = 0; row < 3; ++row)
        {
            for (column = 0; column < 3; ++column)
            {
                this.addSlot(new Slot(tileEntity, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }

        //Player Inventory
        for (row = 0; row < 3; ++row)
        {
            for (column = 0; column < 9; ++column)
            {
                this.addSlot(new Slot(inventorplayer, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (column = 0; column < 9; ++column)
        {
            this.addSlot(new Slot(inventorplayer, column, 8 + column * 18, 142));
        }

        //Side inventory
        /*if (tileEntity.chest != null)
        {
            Inventory chest = tileEntity.chest.get();
            Inventory doubleChest = tileEntity.doubleChest == null ? null : tileEntity.doubleChest.get();
            int count = 0;
            for (column = 0; column < 9; column++)
            {
                for (row = 0; row < 6; row++)
                {
                    int value = count < 27 ? count : count - 27;
                    this.addSlotToContainer(new Slot(count < 27 ? chest : doubleChest, value, -108 + row * 18, 19 + column * 18));
                    count++;
                    if (count >= 27 && doubleChest == null)
                        break;
                }
                if (count >= 27 && doubleChest == null)
                    break;
            }
        }*/

        this.onSlotUpdate(logic);
    }

    public void onSlotUpdate(Inventory par1IInventory)
    {
        if (par1IInventory instanceof CraftingInventory)
            return;

        ItemStack tool = modifyTool();
        if (tool != null)
            this.logic.result.setStack(0, tool);
        else
            this.logic.result.setStack(0, logic.craft(this));
    }

    public ItemStack modifyTool()
    {
        /*ItemStack input = craftMatrix.getStack(4);
        if (input != null)
        {
            Item item = input.getItem();
            if (item instanceof ToolCore)
            {
                ItemStack[] slots = new ItemStack[8];
                for (int i = 0; i < 4; i++)
                {
                    slots[i] = craftMatrix.getStackInSlot(i);
                    slots[i + 4] = craftMatrix.getStackInSlot(i + 5);
                }
                ItemStack output = ToolBuilder.instance.modifyTool(input, slots, "");
                if (output != null)
                    return output;
            }
            else if (item instanceof ArmorCore)
            {
                ItemStack[] slots = new ItemStack[8];
                for (int i = 0; i < 4; i++)
                {
                    slots[i] = craftMatrix.getStackInSlot(i);
                    slots[i + 4] = craftMatrix.getStackInSlot(i + 5);
                }
                ItemStack output = ToolBuilder.instance.modifyArmor(input, slots, "");
                if (output != null)
                    return output;
            }
        }*/
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        Block block = Block.BLOCKS[worldObj.getBlockId(this.posX, this.posY, this.posZ)];
        if (block != InitListener.craftingStation && block != InitListener.craftingSlab)
            return false;
        return player.getSquaredDistance((double) this.posX + 0.5D, (double) this.posY + 0.5D, (double) this.posZ + 0.5D) <= 64.0D;
    }

    /*public ItemStack transferStackInSlot(PlayerEntity par1EntityPlayer, int par2)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.slots.get(par2);

        if (slot != null && slot.hasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (par2 == 0)
            {
                if (!this.mergeItemStack(itemstack1, 10, 46, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (par2 >= 10 && par2 < 37)
            {
                if (!this.mergeItemStack(itemstack1, 37, 46, false))
                {
                    return null;
                }
            }
            else if (par2 >= 37 && par2 < 46)
            {
                if (!this.mergeItemStack(itemstack1, 10, 37, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 10, 46, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.setStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.count == itemstack.count)
            {
                return null;
            }

            slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
        }

        return itemstack;
    }

    public boolean func_94530_a(ItemStack par1ItemStack, Slot par2Slot)
    {
        return par2Slot.inventory != this.craftResult && super.func_94530_a(par1ItemStack, par2Slot);
    }*/
}
