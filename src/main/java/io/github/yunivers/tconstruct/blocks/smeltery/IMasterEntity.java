package io.github.yunivers.tconstruct.blocks.smeltery;

public interface IMasterEntity
{
    /** Called when servants change their state
     *
     * @param x Servant X
     * @param y Servant Y
     * @param z Servant Z
     */
    public void notifyChange (IServantEntity servant, int x, int y, int z);
}
