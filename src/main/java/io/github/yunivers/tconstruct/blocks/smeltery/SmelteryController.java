package io.github.yunivers.tconstruct.blocks.smeltery;

import io.github.yunivers.tconstruct.blocks.entity.SmelteryEntity;
import io.github.yunivers.tconstruct.events.init.InitListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

public class SmelteryController extends TemplateBlockWithEntity implements ISmelteryBlock, Inventory
{
    public SmelteryController(Identifier identifier) {
        super(identifier, Material.STONE);
        setResistance(10.0F);
        setHardness(2.0F);
        setSoundGroup(Block.METAL_SOUND_GROUP);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new SmelteryEntity();
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
        return "Smeltery Controller";
    }

    @Override
    public int getMaxCountPerStack() {
        return 64;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onPlaced(World world, int x, int y, int z, LivingEntity placer) {
        BlockEntity logic = world.getBlockEntity(x, y, z);
        int facing = MathHelper.floor((double)(placer.yaw * 4.0F / 360.0F) + (double)0.5F) & 3;
        byte direction = 0;
        if (facing == 0)
            world.setBlockMeta(x, y, z, direction = 2);
        else if (facing == 1)
            world.setBlockMeta(x, y, z, direction = 5);
        else if (facing == 2)
            world.setBlockMeta(x, y, z, direction = 3);
        else
            world.setBlockMeta(x, y, z, direction = 4);

        if (logic instanceof SmelteryEntity smelteryEntity)
            smelteryEntity.setDirection(direction);

        onBlockPlacedElsewhere(world, x, y, z, placer);
    }

    @Environment(EnvType.CLIENT)
    public int getTextureId(BlockView blockView, int x, int y, int z, int side) {
        int facing = blockView.getBlockMeta(x, y, z);
        if (side != facing) {
            return InitListener.SmelterySide;
        } else {
            return isLit(blockView.getBlockEntity(x, y, z)) ? InitListener.SmelteryFrontActive : InitListener.SmelteryFront;
        }
    }

    public boolean isLit(BlockEntity entity) {
        return entity instanceof SmelteryEntity && ((SmelteryEntity)entity).validStructure;
    }

    public int getTexture(int side) {
        if (side == 3)
            return InitListener.SmelteryFront;

        return InitListener.SmelterySide;
    }

    public void onBlockPlacedElsewhere (World world, int x, int y, int z, LivingEntity entityliving)
    {
        SmelteryEntity logic = (SmelteryEntity) world.getBlockEntity(x, y, z);
        logic.checkValidPlacement();
    }
}
