package io.github.yunivers.tconstruct.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class ActiveSlot extends Slot
{
    protected boolean active;
    public int activeSlotNumber;

    public ActiveSlot(Inventory inventory, int index, int x, int y, boolean active)
    {
        super(inventory, index, x, y);
        this.active = active;
    }

    public void setActive (boolean flag)
    {
        active = flag;
    }

    public boolean getActive ()
    {
        return active;
    }
}
