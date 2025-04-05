package io.github.yunivers.tconstruct.blocks.smeltery;

import io.github.yunivers.tconstruct.blocks.entity.CraftingStationEntity;
import io.github.yunivers.tconstruct.blocks.entity.SmelteryEntity;
import io.github.yunivers.tconstruct.events.init.InitListener;
import io.github.yunivers.tconstruct.mixin.MinecraftAccessor;
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
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.gui.screen.container.GuiHelper;
import net.modificationstation.stationapi.api.state.StateManager;
import net.modificationstation.stationapi.api.state.property.EnumProperty;
import net.modificationstation.stationapi.api.state.property.IntProperty;
import net.modificationstation.stationapi.api.state.property.BooleanProperty;
import net.modificationstation.stationapi.api.template.block.TemplateBlockWithEntity;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.math.Direction;

import java.util.Random;

public class SmelteryController extends TemplateBlockWithEntity implements ISmelteryBlock, Inventory
{
    public static final EnumProperty<Direction> FACING_PROPERTY = EnumProperty.of("facing", Direction.class);
    public static final IntProperty LUMINANCE_PROPERTY = IntProperty.of("luminance", 0, 15);
    public static final BooleanProperty ACTIVE_PROPERTY = BooleanProperty.of("active");
    public static final IntProperty LAYERS_PROPERTY = IntProperty.of("layers", 0, 128); // I think 128 is the build height?
    public static boolean UpdatingBlockState; // No use yet but will get one soon enough

    public SmelteryController(Identifier identifier) {
        super(identifier, Material.STONE);
        setResistance(10.0F);
        setHardness(2.0F);
        setSoundGroup(Block.METAL_SOUND_GROUP);

        setDefaultState(getStateManager().getDefaultState().with(LUMINANCE_PROPERTY, 0));
        setDefaultState(getStateManager().getDefaultState().with(FACING_PROPERTY, Direction.NORTH));
        setDefaultState(getStateManager().getDefaultState().with(ACTIVE_PROPERTY, false));
        setDefaultState(getStateManager().getDefaultState().with(LAYERS_PROPERTY, 0));
        setLuminance((blockState) -> blockState.get(LUMINANCE_PROPERTY));
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LUMINANCE_PROPERTY);
        builder.add(FACING_PROPERTY);
        builder.add(ACTIVE_PROPERTY);
        builder.add(LAYERS_PROPERTY);
    }

    @Override
    protected BlockEntity createBlockEntity()
    {
        return new SmelteryEntity();
    }

    public void updateBlockState(World world, int x, int y, int z, BlockState newState) {
        BlockEntity entity = world.getBlockEntity(x, y, z);
        UpdatingBlockState = true;
        world.setBlockState(x, y, z, newState);
        UpdatingBlockState = false;
        entity.cancelRemoval();
        world.setBlockEntity(x, y, z, entity);
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
    public boolean onUse(World world, int x, int y, int z, PlayerEntity player) {
        if (!world.isRemote) {
            InitListener.TempGuiX = x;
            InitListener.TempGuiY = y;
            InitListener.TempGuiZ = z;

            SmelteryEntity logic = (SmelteryEntity) world.getBlockEntity(x, y, z);
            GuiHelper.openGUI(player, Identifier.of("tconstruct:openSmeltery"), logic, logic.getGuiContainer(player.inventory, world, x, y, z));
        }
        return true;
    }

    @Override
    public void onPlaced(World world, int x, int y, int z, LivingEntity placer) {
        int facing = MathHelper.floor((double)(placer.yaw * 4.0F / 360.0F) + (double)0.5F) & 3;
        updateBlockState(world, x, y, z, getDefaultState().with(FACING_PROPERTY,
                switch(facing) {
                    case 0 -> Direction.NORTH;
                    case 1 -> Direction.EAST;
                    case 2 -> Direction.SOUTH;
                    default -> Direction.WEST;
                }));

        onBlockPlacedElsewhere(world, x, y, z, placer);
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

    @Override
    public void neighborUpdate(World world, int x, int y, int z, int id) {
        SmelteryEntity logic = (SmelteryEntity) world.getBlockEntity(x, y, z);
        logic.checkValidPlacement();
    }

    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        BlockState state = world.getBlockState(x, y, z);
        if (state.get(ACTIVE_PROPERTY)) {
            Direction dir = state.get(FACING_PROPERTY);
            float offsetX = (float)x + 0.5F;
            float offsetY = (float)y + 0.5F + random.nextFloat() * 6.0F / 16.0F;
            float offsetZ = (float)z + 0.5F;
            float horiOffset = 0.52F;
            float variation = random.nextFloat() * 0.6F - 0.3F;
            switch (dir) {
                case NORTH:
                    world.addParticle("smoke", (double)(offsetX + variation), (double)offsetY, (double)(offsetZ + horiOffset), (double)0.0F, (double)0.0F, (double)0.0F);
                    world.addParticle("flame", (double)(offsetX + variation), (double)offsetY, (double)(offsetZ + horiOffset), (double)0.0F, (double)0.0F, (double)0.0F);
                    break;
                case SOUTH:
                    world.addParticle("smoke", (double)(offsetX + variation), (double)offsetY, (double)(offsetZ - horiOffset), (double)0.0F, (double)0.0F, (double)0.0F);
                    world.addParticle("flame", (double)(offsetX + variation), (double)offsetY, (double)(offsetZ - horiOffset), (double)0.0F, (double)0.0F, (double)0.0F);
                    break;
                case EAST:
                    world.addParticle("smoke", (double)(offsetX + horiOffset), (double)offsetY, (double)(offsetZ + variation), (double)0.0F, (double)0.0F, (double)0.0F);
                    world.addParticle("flame", (double)(offsetX + horiOffset), (double)offsetY, (double)(offsetZ + variation), (double)0.0F, (double)0.0F, (double)0.0F);
                    break;
                case WEST:
                    world.addParticle("smoke", (double)(offsetX - horiOffset), (double)offsetY, (double)(offsetZ + variation), (double)0.0F, (double)0.0F, (double)0.0F);
                    world.addParticle("flame", (double)(offsetX - horiOffset), (double)offsetY, (double)(offsetZ + variation), (double)0.0F, (double)0.0F, (double)0.0F);
                    break;
            }
        }
    }
}
