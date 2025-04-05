package io.github.yunivers.tconstruct.blocks.smeltery;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

public class GenericSmelteryBlock extends TemplateBlockWithEntity implements ISmelteryBlock {
    public GenericSmelteryBlock(Identifier identifier, Material material) {
        super(identifier, material);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new MultiServantEntity();
    }

    @Override
    public void neighborUpdate(World world, int x, int y, int z, int id) {
        MultiServantEntity entity = (MultiServantEntity)world.getBlockEntity(x, y, z);
        entity.notifyMasterOfChange();
    }

    @Override
    public void onBreak(World world, int x, int y, int z) {
        MultiServantEntity entity = (MultiServantEntity)world.getBlockEntity(x, y, z);
        entity.notifyMasterOfChange();
    }
}
