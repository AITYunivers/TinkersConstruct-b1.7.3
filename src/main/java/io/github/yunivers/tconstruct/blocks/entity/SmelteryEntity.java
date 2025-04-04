package io.github.yunivers.tconstruct.blocks.entity;

import io.github.yunivers.tconstruct.blocks.smeltery.*;
import io.github.yunivers.tconstruct.events.init.InitListener;
import io.github.yunivers.tconstruct.util.CoordTuple;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.Random;

public class SmelteryEntity extends BlockEntity implements Inventory, IMasterEntity {
    protected ItemStack[] inventory;

    public boolean validStructure;
    public boolean tempValidStructure;
    byte direction;
    int internalTemp;

    ArrayList<CoordTuple> lavaTanks;
    CoordTuple activeLavaTank;
    public CoordTuple centerPos;

    public int[] activeTemps;
    public int[] meltingTemps;

    int maxLiquid;
    public int layers;

    int numBricks;

    Random rand = new Random();
    boolean needsUpdate;

    public SmelteryEntity() {
        lavaTanks = new ArrayList<CoordTuple>();
        activeTemps = new int[0];
        meltingTemps = new int[0];
        inventory = new ItemStack[0];
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public ItemStack getStack(int slot) {
        return null;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return null;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {

    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getMaxCountPerStack() {
        return 0;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    void adjustLayers (int lay, boolean forceAdjust)
    {
        if (lay != layers || forceAdjust)
        {
            needsUpdate = true;
            layers = lay;
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
                            case 2: // +z
                                offsetZ = -1;
                                break;
                            case 3: // -z
                                offsetZ = 1;
                                break;
                            case 4: // +x
                                offsetX = -1;
                                break;
                            case 5: // -x
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

    public byte getRenderDirection ()
    {
        return direction;
    }

    public void setDirection (byte direction)
    {
        this.direction = direction;
    }

    /* Multiblock */
    public void notifyChange (IServantEntity servant, int x, int y, int z)
    {
        checkValidPlacement();
    }

    public void checkValidPlacement ()
    {
        switch (getRenderDirection())
        {
            case 2: // +z
                alignInitialPlacement(x, y, z + 2);
                break;
            case 3: // -z
                alignInitialPlacement(x, y, z - 2);
                break;
            case 4: // +x
                alignInitialPlacement(x + 2, y, z);
                break;
            case 5: // -x
                alignInitialPlacement(x - 2, y, z);
                break;
        }
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

        if (tempValidStructure != validStructure || checkLayers != this.layers)
        {
            if (tempValidStructure)
            {
                internalTemp = 900;
                //activeLavaTank = lavaTanks.get(0);
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

        return numBricks == 12;// && !lavaTanks.isEmpty();
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
                    bottomBricks++;
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

                /*if (te instanceof LavaTankLogic)
                {
                    lavaTanks.add(new CoordTuple(x, y, z));
                }*/
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
        return false;//blockID == TContent.lavaTank.blockID || blockID == TContent.lavaTankNether.blockID;
    }
}
