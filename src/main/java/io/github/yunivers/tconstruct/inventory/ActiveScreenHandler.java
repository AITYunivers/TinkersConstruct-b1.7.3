package io.github.yunivers.tconstruct.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

public class ActiveScreenHandler extends ScreenHandler
{
    public List<ActiveSlot> activeInventorySlots = new ArrayList<ActiveSlot>();

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }

    protected ActiveSlot addDualSlot(ActiveSlot slot)
    {
        slot.activeSlotNumber = this.activeInventorySlots.size();
        this.activeInventorySlots.add(slot);
        this.addSlot(slot);
        return slot;
    }

    public void updateSlots(Inventory inventory)
    {
        for (ActiveSlot slot : activeInventorySlots) {
            if (slot.active)
                this.slots.set(slot.id, slot);
            else
                this.slots.set(slot.id, new Slot(inventory, slot.id, Integer.MIN_VALUE, Integer.MIN_VALUE));
        }
    }
}
