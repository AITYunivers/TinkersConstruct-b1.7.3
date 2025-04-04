package io.github.yunivers.tconstruct.blocks.smeltery;

import io.github.yunivers.tconstruct.util.CoordTuple;
import net.minecraft.world.World;

public interface IServantEntity
{
    public CoordTuple getMasterPosition ();

    /** The block should already have a valid master */
    public void notifyMasterOfChange ();

    /** Checks if this block can be tied to this master
     *
     * @param xMaster xCoord of master
     * @param yMaster yCoord of master
     * @param zMaster zCoord of master
     * @return whether the servant can be tied to this master
     */

    public boolean setPotentialMaster (IMasterEntity master, World world, int xMaster, int yMaster, int zMaster);

    /** Used to set and verify that this is the block's master
     *
     * @param xMaster xCoord of master
     * @param yMaster yCoord of master
     * @param zMaster zCoord of master
     * @return Is this block tied to this master?
     */

    public boolean verifyMaster (IMasterEntity master, World world, int xMaster, int yMaster, int zMaster);

    /** Exactly what it says on the tin
     *
     * @param xMaster xCoord of master
     * @param yMaster yCoord of master
     * @param zMaster zCoord of master
     */

    public void invalidateMaster (IMasterEntity master, World world, int xMaster, int yMaster, int zMaster);
}