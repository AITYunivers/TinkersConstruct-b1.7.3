package io.github.yunivers.tconstruct.blocks;

import io.github.yunivers.tconstruct.blocks.entity.CraftingStationEntity;
import io.github.yunivers.tconstruct.events.init.InitListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.gui.screen.container.GuiHelper;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;

import java.util.Random;

public class CraftingStationBlock extends TemplateBlockWithEntity
{
    private final Random random = new Random();

    public CraftingStationBlock(Identifier identifier) {
        super(identifier, Material.WOOD);
        setHardness(2f);
        setSoundGroup(WOOD_SOUND_GROUP);
    }

    @Override
    public int getTexture(int side, int meta) {
        if (side == 0)
            return 2;
        if (side == 1)
            return 0;

        return 1;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public BlockEntity createBlockEntity()
    {
        return new CraftingStationEntity();
    }

    @Override
    public boolean onUse(World world, int x, int y, int z, PlayerEntity player) {
        if (!world.isRemote) {
            InitListener.TempGuiX = x;
            InitListener.TempGuiY = y;
            InitListener.TempGuiZ = z;

            CraftingStationEntity logic = (CraftingStationEntity) world.getBlockEntity(x, y, z);
            GuiHelper.openGUI(player, Identifier.of("tconstruct:openCraftingStation"), logic, logic.getGuiContainer(player.inventory, world, x, y, z));
        }
        return true;
    }

    @Override
    public void onBreak(World world, int x, int y, int z) {
        CraftingStationEntity entity = (CraftingStationEntity)world.getBlockEntity(x, y, z);

        for (int i = 0; i < entity.size(); ++i) {
            ItemStack itemStack = entity.getStack(i);
            if (itemStack != null) {
                float posX = this.random.nextFloat() * 0.8F + 0.1F;
                float posY = this.random.nextFloat() * 0.8F + 0.1F;
                float posZ = this.random.nextFloat() * 0.8F + 0.1F;

                while (itemStack.count > 0) {
                    int dropCount = this.random.nextInt(21) + 10;
                    if (dropCount > itemStack.count) {
                        dropCount = itemStack.count;
                    }

                    itemStack.count -= dropCount;
                    ItemEntity itemEntity = new ItemEntity(world, (double)((float)x + posX), (double)((float)y + posY), (double)((float)z + posZ), new ItemStack(itemStack.itemId, dropCount, itemStack.getDamage()));
                    float multiplier = 0.05F;
                    itemEntity.velocityX = (double)((float)this.random.nextGaussian() * multiplier);
                    itemEntity.velocityY = (double)((float)this.random.nextGaussian() * multiplier + 0.2F);
                    itemEntity.velocityZ = (double)((float)this.random.nextGaussian() * multiplier);
                    world.spawnEntity(itemEntity);
                }
            }
        }

        super.onBreak(world, x, y, z);
    }
}
