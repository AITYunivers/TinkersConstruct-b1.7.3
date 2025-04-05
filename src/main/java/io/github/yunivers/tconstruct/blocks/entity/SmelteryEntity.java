package io.github.yunivers.tconstruct.blocks.entity;

import io.github.yunivers.tconstruct.blocks.smeltery.*;
import io.github.yunivers.tconstruct.events.init.InitListener;
import io.github.yunivers.tconstruct.inventory.SmelteryHandler;
import io.github.yunivers.tconstruct.mixin.MinecraftAccessor;
import io.github.yunivers.tconstruct.util.CoordTuple;
import io.github.yunivers.tconstruct.util.FluidStack;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.Direction;

import java.util.ArrayList;
import java.util.Random;

public class SmelteryEntity extends BlockEntity implements Inventory, IMasterEntity {
    protected ItemStack[] inventory;

    public boolean validStructure;
    public boolean tempValidStructure;
    byte direction;
    int internalTemp;
    public int useTime;
    public int fuelGague;
    public int fuelAmount;

    ArrayList<CoordTuple> lavaTanks;
    CoordTuple activeLavaTank;
    public CoordTuple centerPos;

    public int[] activeTemps;
    public int[] meltingTemps;

    public ArrayList<FluidStack> moltenMetal = new ArrayList<FluidStack>();
    int maxLiquid;
    int currentLiquid;

    int numBricks;

    Random rand = new Random();
    boolean needsUpdate;

    private static int nextID = 0;
    public int ID;

    public SmelteryEntity() {
        lavaTanks = new ArrayList<CoordTuple>();
        activeTemps = new int[0];
        meltingTemps = new int[0];
        inventory = new ItemStack[0];

        ID = nextID++;
    }

    public int getLayers() {
        World world = MinecraftAccessor.getInstance().world;
        BlockState state = world.getBlockState(x, y, z);
        return state.get(SmelteryController.LAYERS_PROPERTY);
    }

    public void setLayers(int layers) {
        World world = MinecraftAccessor.getInstance().world;
        BlockState state = world.getBlockState(x, y, z);
        updateBlockState(world, x, y, z, state.with(SmelteryController.LAYERS_PROPERTY, layers));
    }

    public void updateBlockState(World world, int x, int y, int z, BlockState newState)
    {
        Block block = Block.BLOCKS[world.getBlockId(x, y, z)];
        if (block instanceof SmelteryController smeltery)
            smeltery.updateBlockState(world, x, y, z, newState);
    }

    void adjustLayers (int lay, boolean forceAdjust)
    {
        if (lay != getLayers() || forceAdjust)
        {
            needsUpdate = true;
            setLayers(lay);
            maxLiquid = 20000 * lay;
            int[] tempActive = activeTemps;
            activeTemps = new int[9 * lay];
            int activeLength = Math.min(tempActive.length, activeTemps.length);
            System.arraycopy(tempActive, 0, activeTemps, 0, activeLength);

            int[] tempMelting = meltingTemps;
            meltingTemps = new int[9 * lay];
            int meltingLength = Math.min(tempMelting.length, meltingTemps.length);
            System.arraycopy(tempMelting, 0, meltingTemps, 0, meltingLength);

            ItemStack[] tempInv = inventory;
            inventory = new ItemStack[9 * lay];
            int invLength = Math.min(tempInv.length, inventory.length);
            System.arraycopy(tempInv, 0, inventory, 0, invLength);

            if (activeTemps.length > 0 && activeTemps.length > tempActive.length)
            {
                for (int i = tempActive.length; i < activeTemps.length; i++)
                {
                    activeTemps[i] = 20;
                    meltingTemps[i] = 20;
                }
            }

            if (tempInv.length > inventory.length)
            {
                for (int i = inventory.length; i < tempInv.length; i++)
                {
                    ItemStack stack = tempInv[i];
                    if (stack != null)
                    {
                        float jumpX = rand.nextFloat() * 0.8F + 0.1F;
                        float jumpY = rand.nextFloat() * 0.8F + 0.1F;
                        float jumpZ = rand.nextFloat() * 0.8F + 0.1F;

                        int offsetX = 0;
                        int offsetZ = 0;
                        switch (getRenderDirection())
                        {
                            case SOUTH: // +z
                                offsetZ = -1;
                                break;
                            case NORTH: // -z
                                offsetZ = 1;
                                break;
                            case EAST: // +x
                                offsetX = -1;
                                break;
                            case WEST: // -x
                                offsetX = 1;
                                break;
                        }

                        while (stack.count > 0)
                        {
                            int itemSize = rand.nextInt(21) + 10;

                            if (itemSize > stack.count)
                            {
                                itemSize = stack.count;
                            }

                            stack.count -= itemSize;
                            ItemEntity entityitem = new ItemEntity(world, (double) ((float) x + jumpX + offsetX), (double) ((float) y + jumpY),
                                    (double) ((float) z + jumpZ + offsetZ), new ItemStack(stack.itemId, itemSize, stack.getDamage()));

                            NbtCompound nbt = stack.getStationNbt();
                            if (nbt != null)
                                entityitem.readNbt(nbt.copy());

                            float offset = 0.05F;
                            entityitem.velocityX = (double) ((float) rand.nextGaussian() * offset);
                            entityitem.velocityX = (double) ((float) rand.nextGaussian() * offset + 0.2F);
                            entityitem.velocityZ = (double) ((float) rand.nextGaussian() * offset);
                            world.spawnEntity(entityitem);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Smeltery";
    }

    public ScreenHandler getGuiContainer(PlayerInventory inventoryplayer, World world, int x, int y, int z)
    {
        return new SmelteryHandler(inventoryplayer, this);
    }

    public Direction getRenderDirection ()
    {
        BlockState state = MinecraftAccessor.getInstance().world.getBlockState(x, y, z);
        return state.get(SmelteryController.FACING_PROPERTY);
    }

    public boolean getActive()
    {
        return validStructure;
    }

    public void setActive()
    {
        needsUpdate = true;
        world.setBlockDirty(x, y, z);
    }

    public int getScaledFuelGague (int scale)
    {
        int ret = (fuelGague * scale) / 52;
        if (ret < 1)
            ret = 1;
        return ret;
    }

    public int getInternalTemperature ()
    {
        return internalTemp;
    }

    public int getTempForSlot (int slot)
    {
        return activeTemps[slot];
    }

    public int getMeltingPointForSlot (int slot)
    {
        return meltingTemps[slot];
    }

    @Override
    public int size() {
        return inventory.length;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory[slot];
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (inventory[slot] != null)
        {
            if (inventory[slot].count <= amount)
            {
                ItemStack stack = inventory[slot];
                inventory[slot] = null;
                return stack;
            }
            ItemStack split = inventory[slot].split(amount);
            if (inventory[slot].count == 0)
            {
                inventory[slot] = null;
            }
            return split;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory[slot] = stack;
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public void updateFuelDisplay ()
    {
        if (activeLavaTank == null || useTime > 0)
            return;

        if (world.getBlockId(activeLavaTank.x, activeLavaTank.y, activeLavaTank.z) == 0)
        {
            fuelAmount = 0;
            fuelGague = 0;
            return;
        }

        BlockEntity tankContainer = world.getBlockEntity(activeLavaTank.x, activeLavaTank.y, activeLavaTank.z);
        if (tankContainer == null)
        {
            fuelAmount = 0;
            fuelGague = 0;
            return;
        }
        if (tankContainer instanceof LavaTankEntity lavaTank)
        {
            needsUpdate = true;
            FluidStack liquid = lavaTank.drain(150, false);
            if (liquid != null && liquid.getFluid() == Material.LAVA)
            {
                int capacity = lavaTank.tank.getCapacity();
                fuelAmount = liquid.amount;
                fuelGague = liquid.amount * 52 / capacity;
            }
            else
            {
                fuelAmount = 0;
                fuelGague = 0;
            }
        }
    }

    /* Multiblock */
    public void notifyChange (IServantEntity servant, int x, int y, int z)
    {
        checkValidPlacement();
    }

    public void checkValidPlacement ()
    {
        // Inverted bc it wasts to go backwards
        switch (getRenderDirection())
        {
            case NORTH: // +z
                alignInitialPlacement(x, y, z + 2);
                break;
            case SOUTH: // -z
                alignInitialPlacement(x, y, z - 2);
                break;
            case WEST: // +x
                alignInitialPlacement(x + 2, y, z);
                break;
            case EAST: // -x
                alignInitialPlacement(x - 2, y, z);
                break;
        }

        World world = MinecraftAccessor.getInstance().world;
        BlockState blockState = world.getBlockState(x, y, z);
        updateBlockState(world, x, y, z, blockState
            .with(SmelteryController.LUMINANCE_PROPERTY, validStructure ? 13 : 0)
            .with(SmelteryController.ACTIVE_PROPERTY, validStructure));
    }

    @SuppressWarnings("ConstantValue")
    public void alignInitialPlacement (int x, int y, int z)
    {
        int northID = world.getBlockId(x, y, z + 1);
        int southID = world.getBlockId(x, y, z - 1);
        int eastID = world.getBlockId(x + 1, y, z);
        int westID = world.getBlockId(x - 1, y, z);

        Block northBlock = Block.BLOCKS[northID];
        Block southBlock = Block.BLOCKS[southID];
        Block eastBlock = Block.BLOCKS[eastID];
        Block westBlock = Block.BLOCKS[westID];

        boolean hasNorth = northBlock != null && northBlock.id != 0;
        boolean hasSouth = southBlock != null && southBlock.id != 0;
        boolean hasEast = eastBlock != null && eastBlock.id != 0;
        boolean hasWest = westBlock != null && westBlock.id != 0;

        if (!hasNorth && !hasSouth && !hasEast && !hasWest)
            checkValidStructure(x, y, z);
        else if (hasNorth && !hasSouth && !hasEast && !hasWest)
            checkValidStructure(x, y, z - 1);
        else if (!hasNorth && hasSouth && !hasEast && !hasWest)
            checkValidStructure(x, y, z + 1);
        else if (!hasNorth && !hasSouth && hasEast && !hasWest)
            checkValidStructure(x - 1, y, z);
        else if (!hasNorth && !hasSouth && !hasEast && hasWest)
            checkValidStructure(x + 1, y, z);

        //Not valid, sorry
    }

    public void checkValidStructure (int x, int y, int z)
    {
        int checkLayers = 0;
        tempValidStructure = false;
        if (checkSameLevel(x, y, z))
        {
            checkLayers++;
            checkLayers += recurseStructureUp(x, y + 1, z, 0);
            checkLayers += recurseStructureDown(x, y - 1, z, 0);
        }

        if (tempValidStructure != validStructure || checkLayers != getLayers())
        {
            if (tempValidStructure)
            {
                internalTemp = 900;
                activeLavaTank = lavaTanks.get(0);
                adjustLayers(checkLayers, false);
                world.setBlockDirty(x, y, z);
                validStructure = true;
            }
            else
            {
                internalTemp = 20;
                validStructure = false;
            }
        }
    }

    public boolean checkSameLevel (int x, int y, int z)
    {
        numBricks = 0;
        lavaTanks.clear();
        Block block;

        //Check inside
        for (int xPos = x - 1; xPos <= x + 1; xPos++)
        {
            for (int zPos = z - 1; zPos <= z + 1; zPos++)
            {
                block = Block.BLOCKS[world.getBlockId(xPos, y, zPos)];
                if (block != null && block.id != 0)
                    return false;
            }
        }

        //Check outer layer
        for (int xPos = x - 1; xPos <= x + 1; xPos++)
        {
            numBricks += checkBricks(xPos, y, z - 2);
            numBricks += checkBricks(xPos, y, z + 2);
        }

        for (int zPos = z - 1; zPos <= z + 1; zPos++)
        {
            numBricks += checkBricks(x - 2, y, zPos);
            numBricks += checkBricks(x + 2, y, zPos);
        }

        return numBricks == 12 && !lavaTanks.isEmpty();
    }

    public int recurseStructureUp (int x, int y, int z, int count)
    {
        numBricks = 0;
        //Check inside
        for (int xPos = x - 1; xPos <= x + 1; xPos++)
        {
            for (int zPos = z - 1; zPos <= z + 1; zPos++)
            {
                Block block = Block.BLOCKS[world.getBlockId(xPos, y, zPos)];
                if (block != null && block.id != 0)
                    return count;
            }
        }

        //Check outer layer
        for (int xPos = x - 1; xPos <= x + 1; xPos++)
        {
            numBricks += checkBricks(xPos, y, z - 2);
            numBricks += checkBricks(xPos, y, z + 2);
        }

        for (int zPos = z - 1; zPos <= z + 1; zPos++)
        {
            numBricks += checkBricks(x - 2, y, zPos);
            numBricks += checkBricks(x + 2, y, zPos);
        }

        if (numBricks != 12)
            return count;

        count++;
        return recurseStructureUp(x, y + 1, z, count);
    }

    public int recurseStructureDown (int x, int y, int z, int count)
    {
        numBricks = 0;
        //Check inside
        for (int xPos = x - 1; xPos <= x + 1; xPos++)
        {
            for (int zPos = z - 1; zPos <= z + 1; zPos++)
            {
                int blockID = world.getBlockId(xPos, y, zPos);
                Block block = Block.BLOCKS[blockID];
                if (block != null && block.id != 0)
                {
                    if (validBlockID(blockID))
                        return validateBottom(x, y, z, count);
                    else
                        return count;
                }
            }
        }

        //Check outer layer
        for (int xPos = x - 1; xPos <= x + 1; xPos++)
        {
            numBricks += checkBricks(xPos, y, z - 2);
            numBricks += checkBricks(xPos, y, z + 2);
        }

        for (int zPos = z - 1; zPos <= z + 1; zPos++)
        {
            numBricks += checkBricks(x - 2, y, zPos);
            numBricks += checkBricks(x + 2, y, zPos);
        }

        if (numBricks != 12)
            return count;

        count++;
        return recurseStructureDown(x, y - 1, z, count);
    }

    public int validateBottom (int x, int y, int z, int count)
    {
        int bottomBricks = 0;
        for (int xPos = x - 1; xPos <= x + 1; xPos++)
        {
            for (int zPos = z - 1; zPos <= z + 1; zPos++)
            {
                if (validBlockID(world.getBlockId(xPos, y, zPos)) &&
                    (world.getBlockId(xPos, y, zPos) != InitListener.smelteryController.id))
                    // MAKE SURE THIS ALSO DOES `!= InitListener.smelteryDrain.id`
                {
                    BlockEntity entity = world.getBlockEntity(xPos, y, zPos);
                    if (entity instanceof MultiServantEntity servant)
                    {
                        if (servant.hasValidMaster())
                            servant.verifyMaster(this, this.x, this.y, this.z);
                        else
                            servant.setMaster(this.x, this.y, this.z);
                    }
                    bottomBricks++;
                }
            }
        }

        if (bottomBricks == 9)
        {
            tempValidStructure = true;
            centerPos = new CoordTuple(x, y + 1, z);
        }
        return count;
    }

    /* Returns whether the brick is a lava tank or not.
     * Increments bricks, sets them as part of the structure, and adds tanks to the list.
     */
    int checkBricks (int x, int y, int z)
    {
        int tempBricks = 0;
        int blockID = world.getBlockId(x, y, z);
        if (validBlockID(blockID) || validTankID(blockID))
        {
            BlockEntity te = world.getBlockEntity(x, y, z);
            if (te == this)
            {
                tempBricks++;
            }
            else if (te instanceof MultiServantEntity servant)
            {
                if (servant.hasValidMaster())
                {
                    if (servant.verifyMaster(this, this.x, this.y, this.z))
                        tempBricks++;
                }
                else if (servant.setMaster(this.x, this.y, this.z))
                {
                    tempBricks++;
                }

                if (te instanceof LavaTankEntity)
                {
                    lavaTanks.add(new CoordTuple(x, y, z));
                }
            }
        }
        return tempBricks;
    }

    boolean validBlockID (int blockID)
    {
        return Block.BLOCKS[blockID] instanceof ISmelteryBlock;
    }

    boolean validTankID (int blockID)
    {
        return blockID == InitListener.smelteryTank.id;// || blockID == TContent.lavaTankNether.blockID;
    }

    public int getCapacity ()
    {
        return maxLiquid;
    }

    public int getTotalLiquid ()
    {
        return currentLiquid;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        readInventoryFromNbt(nbt);

        internalTemp = nbt.getInt("InternalTemp");
        //inUse = nbt.getBoolean("InUse");

        int[] center = nbt.getIntArray("CenterPos");
        if (center.length > 2)
            centerPos = new CoordTuple(center[0], center[1], center[2]);
        else
            centerPos = new CoordTuple(x, y, z);

        direction = nbt.getByte("Direction");
        useTime = nbt.getInt("UseTime");
        currentLiquid = nbt.getInt("CurrentLiquid");
        maxLiquid = nbt.getInt("MaxLiquid");
        meltingTemps = nbt.getIntArray("MeltingTemps");
        activeTemps = nbt.getIntArray("ActiveTemps");

        NbtList liquidTag = nbt.getList("Liquids");
        moltenMetal.clear();

        for (int iter = 0; iter < liquidTag.size(); iter++)
        {
            NbtCompound liquidNbt = (NbtCompound) liquidTag.get(iter);
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(liquidNbt);
            if (fluid != null)
                moltenMetal.add(fluid);
        }
    }

    public void readInventoryFromNbt(NbtCompound nbt)
    {
        NbtList nbtList = nbt.getList("Items");
        inventory = new ItemStack[nbt.getInt("InventorySize")];
        for (int i = 0; i < nbtList.size(); i++)
        {
            NbtCompound tagList = (NbtCompound)nbtList.get(i);
            byte slotID = tagList.getByte("Slot");
            if (slotID >= 0 && slotID < inventory.length)
                inventory[slotID] = new ItemStack(tagList);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        writeInventoryToNbt(nbt);

        nbt.putInt("InternalTemp", internalTemp);
        //nbt.putBoolean("InUse", inUse);

        int[] center = new int[3];// { centerPos.x, centerPos.y, centerPos.z };
        if (centerPos == null)
            center = new int[] { x, y, z };
        else
            center = new int[] { centerPos.x, centerPos.y, centerPos.z };
        nbt.put("CenterPos", center);

        nbt.putByte("Direction", direction);
        nbt.putInt("UseTime", useTime);
        nbt.putInt("CurrentLiquid", currentLiquid);
        nbt.putInt("MaxLiquid", maxLiquid);
        nbt.put("MeltingTemps", meltingTemps);
        nbt.put("ActiveTemps", activeTemps);

        NbtList taglist = new NbtList();
        for (FluidStack liquid : moltenMetal)
        {
            NbtCompound liquidNbt = new NbtCompound();
            liquid.writeToNBT(liquidNbt);
            taglist.add(nbt);
        }

        nbt.put("Liquids", taglist);
    }

    public void writeInventoryToNbt(NbtCompound nbt)
    {
        nbt.putInt("InventorySize", size());

        NbtList nbtList = new NbtList();
        for (int iter = 0; iter < inventory.length; iter++)
        {
            if (inventory[iter] != null)
            {
                NbtCompound tagList = new NbtCompound();
                tagList.putByte("Slot", (byte)iter);
                inventory[iter].writeNbt(tagList);
                nbtList.add(tagList);
            }
        }

        nbt.put("Items", nbtList);
    }
}
