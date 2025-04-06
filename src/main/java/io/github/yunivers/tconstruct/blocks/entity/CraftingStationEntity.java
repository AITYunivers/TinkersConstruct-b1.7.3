package io.github.yunivers.tconstruct.blocks.entity;

import io.github.yunivers.tconstruct.inventory.CraftingStationHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.CraftingRecipeManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;

public class CraftingStationEntity extends BlockEntity implements Inventory
{
   /* public WeakReference<Inventory> chest; //TODO: These are prototypes
    public WeakReference<Inventory> doubleChest;
    public WeakReference<Inventory> patternChest;
    public WeakReference<Inventory> furnace;
    public boolean tinkerTable;
    public boolean stencilTable;*/

    public ItemStack[] stacks;
    public CraftingResultInventory result;
    public ScreenHandler handler;
    public ItemEntity[] renderedItems;

    public CraftingStationEntity()
    {
        super();
        stacks = new ItemStack[3 * 3];
        result = new CraftingResultInventory();
    }

    public ScreenHandler getGuiContainer(PlayerInventory inventoryplayer, World world, int x, int y, int z)
    {
        /*chest = null;
        doubleChest = null;
        patternChest = null;
        furnace = null;
        tinkerTable = false;
        for (int yPos = y - 1; yPos <= y + 1; yPos++)
        {
            for (int xPos = x - 1; xPos <= x + 1; xPos++)
            {
                for (int zPos = z - 1; zPos <= z + 1; zPos++)
                {
                    TileEntity tile = world.getBlockTileEntity(xPos, yPos, zPos);
                    if (chest == null && tile instanceof TileEntityChest)
                    {
                        chest = new WeakReference(tile);
                        checkForChest(world, xPos + 1, yPos, zPos);
                        checkForChest(world, xPos - 1, yPos, zPos);
                        checkForChest(world, xPos, yPos, zPos + 1);
                        checkForChest(world, xPos, yPos, zPos - 1);
                    }
                    else if (patternChest == null && tile instanceof PatternChestLogic)
                        patternChest = new WeakReference(tile);
                    else if (furnace == null && (tile instanceof TileEntityFurnace || tile instanceof FurnaceLogic))
                        furnace = new WeakReference(tile);
                    else if (tinkerTable == false && tile instanceof ToolStationLogic)
                        tinkerTable = true;
                }
            }
        }
*/
        return new CraftingStationHandler(inventoryplayer, this, x, y, z);
    }

    void checkForChest(World world, int x, int y, int z)
    {
        /*BlockEntity tile = world.getBlockEntity(x, y, z);
        if (tile instanceof ChestBlockEntity)
            doubleChest = new WeakReference(tile);*/
    }

    @Override
    public void readNbt(NbtCompound tags)
    {
        super.readNbt(tags);

        NbtList nbttaglist = tags.getList("Items");
        for (int i = 0; i < nbttaglist.size(); i++)
        {
            NbtCompound tagList = (NbtCompound)nbttaglist.get(i);
            byte slotID = tagList.getByte("Slot");
            if (slotID >= 0 && slotID < size())
            {
                setStack(slotID, new ItemStack(tagList));
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound tags)
    {
        super.writeNbt(tags);

        NbtList nbttaglist = new NbtList();
        for (int i = 0; i < size(); i++)
        {
            ItemStack stack = getStack(i);
            if (stack != null)
            {
                NbtCompound tagList = new NbtCompound();
                tagList.put("Slot", new NbtByte((byte)i));
                stack.writeNbt(tagList);
                nbttaglist.add(tagList);
            }
        }

        tags.put("Items", nbttaglist);
    }

    @Override
    public int size() {
        return stacks.length;
    }

    @Override
    public ItemStack getStack(int slot) {
        return stacks[slot];
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (this.stacks[slot] != null) {
            if (this.stacks[slot].count <= amount) {
                ItemStack var4 = this.stacks[slot];
                this.stacks[slot] = null;
                this.handler.onSlotUpdate(this);
                return var4;
            } else {
                ItemStack var3 = this.stacks[slot].split(amount);
                if (this.stacks[slot].count == 0) {
                    this.stacks[slot] = null;
                }

                this.handler.onSlotUpdate(this);
                return var3;
            }
        } else {
            return null;
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.stacks[slot] = stack;
        if (handler != null) { // For loading into the world
            this.handler.onSlotUpdate(this);
        }
    }

    @Override
    public String getName() {
        return "Crafting Station";
    }

    @Override
    public int getMaxCountPerStack() {
        return 64;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public ItemStack craft(ScreenHandler handler) {
        CraftingInventory craftingMatrix = new CraftingInventory(handler, 3, 3);
        for (int i = 0; i < 9; i++) {
            craftingMatrix.setStack(i, getStack(i));
        }
        return CraftingRecipeManager.getInstance().craft(craftingMatrix);
    }
}
