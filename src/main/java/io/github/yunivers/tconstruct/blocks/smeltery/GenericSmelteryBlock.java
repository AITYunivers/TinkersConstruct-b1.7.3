package io.github.yunivers.tconstruct.blocks.smeltery;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
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
}
