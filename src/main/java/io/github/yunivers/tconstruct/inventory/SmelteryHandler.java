package io.github.yunivers.tconstruct.inventory;

import io.github.yunivers.tconstruct.blocks.entity.SmelteryEntity;
import io.github.yunivers.tconstruct.events.init.InitListener;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SmelteryHandler extends ActiveScreenHandler
{
    public SmelteryEntity smeltery;
    public PlayerInventory playerInv;
    public int fuel = 0;
    int slotRow;

    public SmelteryHandler(PlayerInventory inventoryplayer, SmelteryEntity smeltery)
    {
        this.smeltery = smeltery;
        playerInv = inventoryplayer;
        slotRow = -1;

        /* Smeltery inventory */

        for (int y = 0; y < smeltery.getLayers() * 3; y++)
        {
            for (int x = 0; x < 3; x++)
            {
                this.addDualSlot(new ActiveSlot(smeltery, x + y * 3, 2 + x * 22, 8 + y * 18, y < 8));
            }
        }

        /* Player inventory */
        for (int column = 0; column < 3; column++)
        {
            for (int row = 0; row < 9; row++)
            {
                this.addSlot(new Slot(inventoryplayer, row + column * 9 + 9, 90 + row * 18, 84 + column * 18));
            }
        }

        for (int column = 0; column < 9; column++)
        {
            this.addSlot(new Slot(inventoryplayer, column, 90 + column * 18, 142));
        }

        updateRows(0);
    }

    public int updateRows (int invRow)
    {
        if (invRow != slotRow)
        {
            slotRow = invRow;
            //TConstruct.logger.info(invRow);
            int basePos = invRow * 3;
            for (int iter = 0; iter < activeInventorySlots.size(); iter++)
            {
                ActiveSlot slot = (ActiveSlot) activeInventorySlots.get(iter);
                if (slot.activeSlotNumber >= basePos && slot.activeSlotNumber < basePos + 24)
                {
                    slot.setActive(true);
                }
                else
                {
                    slot.setActive(false);
                }
                int xPos = (iter - basePos) % 3;
                int yPos = (iter - basePos) / 3;
                slot.x = 2 + 22 * xPos;
                slot.y = 8 + 18 * yPos;
            }
            updateSlots(smeltery);
            return slotRow;
        }
        return -1;
    }

    public int scrollTo (float scrollPos)
    {
        float total = (float) (smeltery.size() - 24) / 3;
        int rowPos = (int) (total * scrollPos);
        return updateRows(rowPos);
    }

    public void updateProgressBar (int id, int value)
    {
        if (id == 0)
        {
            smeltery.fuelGague = value;
        }
    }

    @Override
    public boolean canUse(PlayerEntity player)
    {
        Block block = Block.BLOCKS[smeltery.world.getBlockId(smeltery.x, smeltery.y, smeltery.z)];
        if (block != InitListener.smelteryController)
            return false;
        return smeltery.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(int slotID)
    {
        ItemStack stack = null;
        Slot slot = (Slot) this.slots.get(slotID);

        if (slot != null && slot.hasStack())
        {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();

            if (slotID < smeltery.size())
            {
                if (!this.mergeItemStack(slotStack, smeltery.size(), this.slots.size(), true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(slotStack, 0, smeltery.size(), false))
            {
                return null;
            }

            if (slotStack.count == 0)
            {
                slot.setStack((ItemStack) null);
            }
            else
            {
                slot.markDirty();
            }
        }

        return stack;
    }

    protected boolean mergeItemStack (ItemStack inputStack, int startSlot, int endSlot, boolean flag)
    {
        //TConstruct.logger.info("Merge");
        boolean merged = false;
        int slotPos = startSlot;

        if (flag)
        {
            slotPos = endSlot - 1;
        }

        Slot slot;
        ItemStack slotStack;

        if (inputStack.isStackable() && startSlot >= smeltery.size())
        {
            while (inputStack.count > 0 && (!flag && slotPos < endSlot || flag && slotPos >= startSlot))
            {
                slot = (Slot) this.slots.get(slotPos);
                slotStack = slot.getStack();

                if (slotStack != null && slotStack.itemId == inputStack.itemId && (!inputStack.hasSubtypes() || inputStack.getDamage() == slotStack.getDamage()))
                {
                    int l = slotStack.count + inputStack.count;

                    if (l <= inputStack.getMaxCount())
                    {
                        inputStack.count = 0;
                        slotStack.count = l;
                        slot.markDirty();
                        merged = true;
                    }
                    else if (slotStack.count < inputStack.getMaxCount())
                    {
                        inputStack.count -= inputStack.getMaxCount() - slotStack.count;
                        slotStack.count = inputStack.getMaxCount();
                        slot.markDirty();
                        merged = true;
                    }
                }

                if (flag)
                {
                    --slotPos;
                }
                else
                {
                    ++slotPos;
                }
            }
        }

        if (inputStack.count > 0)
        {
            if (flag)
            {
                slotPos = endSlot - 1;
            }
            else
            {
                slotPos = startSlot;
            }

            while (!flag && slotPos < endSlot || flag && slotPos >= startSlot)
            {
                slot = (Slot) this.slots.get(slotPos);
                slotStack = slot.getStack();

                if (slotStack == null)
                {
                    ItemStack newStack = inputStack.copy();
                    newStack.count = 1;
                    slot.setStack(newStack);
                    slot.markDirty();
                    inputStack.count -= 1;
                    merged = true;
                    break;
                }

                if (flag)
                {
                    --slotPos;
                }
                else
                {
                    ++slotPos;
                }
            }
        }

        return merged;
    }
}
