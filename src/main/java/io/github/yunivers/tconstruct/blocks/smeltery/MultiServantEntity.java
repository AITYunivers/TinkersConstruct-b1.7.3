package io.github.yunivers.tconstruct.blocks.smeltery;

import io.github.yunivers.tconstruct.util.CoordTuple;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class MultiServantEntity extends BlockEntity implements IServantEntity
{
    boolean hasMaster;
    CoordTuple master;
    short masterID;
    byte masterMeat; //Typo, it stays!

    public boolean canUpdate ()
    {
        return false;
    }

    public boolean hasValidMaster ()
    {
        if (!hasMaster)
            return false;

        if (world.getBlockId(master.x, master.y, master.z) == masterID && world.getBlockMeta(master.x, master.y, master.z) == masterMeat)
            return true;

        else
        {
            hasMaster = false;
            master = null;
            return false;
        }
    }

    public CoordTuple getMasterPosition ()
    {
        return master;
    }

    public void overrideMaster (int x, int y, int z)
    {
        hasMaster = true;
        master = new CoordTuple(x, y, z);
        masterID = (short) world.getBlockId(x, y, z);
        masterMeat = (byte) world.getBlockMeta(x, y, z);
    }

    public void removeMaster ()
    {
        hasMaster = false;
        master = null;
        masterID = 0;
        masterMeat = 0;
    }

    @Deprecated
    public boolean verifyMaster (IMasterEntity logic, int x, int y, int z)
    {
        if (master.equalCoords(x, y, z) && world.getBlockId(x, y, z) == masterID && world.getBlockMeta(x, y, z) == masterMeat)
            return true;
        else
            return false;
    }

    @Deprecated
    public boolean setMaster (int x, int y, int z)
    {
        if (!hasMaster || world.getBlockId(master.x, master.y, master.z) != masterID || (world.getBlockMeta(master.x, master.y, master.z) != masterMeat))
        {
            overrideMaster(x, y, z);
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean setPotentialMaster (IMasterEntity master, World world, int x, int y, int z)
    {
        return !hasMaster;
    }

    @Override
    public boolean verifyMaster (IMasterEntity logic, World world, int x, int y, int z)
    {
        if (hasMaster)
        {
            return hasValidMaster();
        }
        else
        {
            overrideMaster(x, y, z);
            return true;
        }
    }

    @Override
    public void invalidateMaster (IMasterEntity master, World world, int x, int y, int z)
    {
        hasMaster = false;
        master = null;
    }

    public void notifyMasterOfChange ()
    {
        if (hasValidMaster())
        {
            IMasterEntity logic = (IMasterEntity) world.getBlockEntity(master.x, master.y, master.z);
            logic.notifyChange(this, x, y, z);
        }
    }

    public void readCustomNBT (NbtCompound tags)
    {
        hasMaster = tags.getBoolean("TiedToMaster");
        if (hasMaster)
        {
            int xCenter = tags.getInt("xCenter");
            int yCenter = tags.getInt("yCenter");
            int zCenter = tags.getInt("zCenter");
            master = new CoordTuple(xCenter, yCenter, zCenter);
            masterID = tags.getShort("MasterID");
            masterMeat = tags.getByte("masterMeat");
        }
    }

    public void writeCustomNBT (NbtCompound tags)
    {
        tags.putBoolean("TiedToMaster", hasMaster);
        if (hasMaster)
        {
            tags.putInt("xCenter", master.x);
            tags.putInt("yCenter", master.y);
            tags.putInt("zCenter", master.z);
            tags.putShort("MasterID", masterID);
            tags.putByte("masterMeat", masterMeat);
        }
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
}
