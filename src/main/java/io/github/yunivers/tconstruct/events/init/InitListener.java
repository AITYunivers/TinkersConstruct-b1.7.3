package io.github.yunivers.tconstruct.events.init;

import io.github.yunivers.tconstruct.blocks.CraftingSlab;
import io.github.yunivers.tconstruct.blocks.CraftingStationBlock;
import io.github.yunivers.tconstruct.blocks.FurnaceSlab;
import io.github.yunivers.tconstruct.blocks.entity.CraftingStationEntity;
import io.github.yunivers.tconstruct.blocks.entity.LavaTankEntity;
import io.github.yunivers.tconstruct.blocks.entity.SmelteryEntity;
import io.github.yunivers.tconstruct.blocks.smeltery.GenericSmelteryBlock;
import io.github.yunivers.tconstruct.blocks.smeltery.LavaTankBlock;
import io.github.yunivers.tconstruct.blocks.smeltery.MultiServantEntity;
import io.github.yunivers.tconstruct.blocks.smeltery.SmelteryController;
import io.github.yunivers.tconstruct.client.gui.SmelteryControllerScreen;
import io.github.yunivers.tconstruct.client.render.block.entity.TankRenderer;
import io.github.yunivers.tconstruct.client.gui.CraftingStationScreen;
import io.github.yunivers.tconstruct.item.armor.WoodenArmorItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.modificationstation.stationapi.api.client.event.block.entity.BlockEntityRendererRegisterEvent;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.client.gui.screen.GuiHandler;
import net.modificationstation.stationapi.api.client.registry.GuiHandlerRegistry;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.client.texture.atlas.ExpandableAtlas;
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.event.registry.GuiHandlerRegistryEvent;
import net.modificationstation.stationapi.api.event.registry.ItemRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.registry.Registry;
import net.modificationstation.stationapi.api.template.item.TemplateItem;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class InitListener {
    static {
        EntrypointManager.registerLookup(MethodHandles.lookup());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static final Namespace NAMESPACE = Namespace.resolve();

    public static final Logger LOGGER = NAMESPACE.getLogger();

    @EventListener
    private static void serverInit(InitEvent event) {
        LOGGER.info(NAMESPACE.toString());
    }

    // Blocks
    public static Block searedBricks;
    public static Block craftingStation;
    public static Block craftingSlab;
    public static Block furnaceSlab;
    public static Block smelteryController;
    public static Block smelteryTank;

    @EventListener
    public void registerBlocks(BlockRegistryEvent event) {
        searedBricks = new GenericSmelteryBlock(NAMESPACE.id("seared_bricks"), Material.STONE)
                .setResistance(10.0F)
                .setHardness(2.0F)
                .setSoundGroup(Block.METAL_SOUND_GROUP)
                .setTranslationKey(NAMESPACE, "seared_bricks");

        craftingStation = new CraftingStationBlock(NAMESPACE.id("crafting_station"))
                .setTranslationKey(NAMESPACE, "crafting_station");

        craftingSlab = new CraftingSlab(NAMESPACE.id("crafting_slab"))
                .setTranslationKey(NAMESPACE, "crafting_slab");

        furnaceSlab = new FurnaceSlab(NAMESPACE.id("furnace_slab"), false)
                .setTranslationKey(NAMESPACE, "furnace_slab");

        smelteryController = new SmelteryController(NAMESPACE.id("smeltery_controller"))
                .setTranslationKey(NAMESPACE, "smeltery_controller");

        smelteryTank = new LavaTankBlock(NAMESPACE.id("smeltery_tank"))
                .setTranslationKey(NAMESPACE, "smeltery_tank");
    }

    // Block Entities

    @EventListener
    public static void registerBlockEntities(BlockEntityRegisterEvent event) {
        event.register(CraftingStationEntity.class, String.valueOf(Identifier.of(NAMESPACE, "crafting_station")));
        event.register(SmelteryEntity.class, String.valueOf(Identifier.of(NAMESPACE, "smeltery_controller")));
        event.register(MultiServantEntity.class, String.valueOf(Identifier.of(NAMESPACE, "smeltery")));
        event.register(LavaTankEntity.class, String.valueOf(Identifier.of(NAMESPACE, "smeltery_tank")));
    }

    // Block Entity Renderers

    @EventListener
    public static void registerBlockEntityRenderers(BlockEntityRendererRegisterEvent event) {
        event.renderers.put(LavaTankEntity.class, new TankRenderer());
    }

    // Items

    public static Item searedBrick;
    public static Item woodenHelmet;
    public static Item woodenChestplate;
    public static Item woodenLeggings;
    public static Item woodenBoots;

    @EventListener
    public void registerItems(ItemRegistryEvent event) {
        searedBrick = new TemplateItem(NAMESPACE.id("seared_brick"))
                .setTranslationKey(NAMESPACE, "seared_brick");

        woodenHelmet = new WoodenArmorItem(NAMESPACE.id("wooden_helmet"), 0)
                .setTranslationKey(NAMESPACE, "wooden_helmet");
        woodenChestplate = new WoodenArmorItem(NAMESPACE.id("wooden_chestplate"), 1)
                .setTranslationKey(NAMESPACE, "wooden_chestplate");
        woodenLeggings = new WoodenArmorItem(NAMESPACE.id("wooden_leggings"), 2)
                .setTranslationKey(NAMESPACE, "wooden_leggings");
        woodenBoots = new WoodenArmorItem(NAMESPACE.id("wooden_boots"), 3)
                .setTranslationKey(NAMESPACE, "wooden_boots");
    }

    // GUIs

    @Environment(EnvType.CLIENT)
    @EventListener
    public void registerScreenHandlers(GuiHandlerRegistryEvent event) {
        GuiHandlerRegistry registry = event.registry;
        Registry.register(registry, Identifier.of(NAMESPACE, "openCraftingStation"), new GuiHandler((GuiHandler.ScreenFactoryNoMessage) this::openCraftingStation, CraftingStationEntity::new));
        Registry.register(registry, Identifier.of(NAMESPACE, "openSmeltery"), new GuiHandler((GuiHandler.ScreenFactoryNoMessage) this::openSmeltery, SmelteryEntity::new));
    }

    public static int TempGuiX;
    public static int TempGuiY;
    public static int TempGuiZ;

    @Environment(EnvType.CLIENT)
    public Screen openCraftingStation(PlayerEntity player, Inventory inventoryBase) {
        return new CraftingStationScreen(player.inventory, (CraftingStationEntity)inventoryBase, TempGuiX, TempGuiY, TempGuiZ);
    }

    @Environment(EnvType.CLIENT)
    public Screen openSmeltery(PlayerEntity player, Inventory inventoryBase) {
        return new SmelteryControllerScreen(player.inventory, (SmelteryEntity)inventoryBase, player.world, TempGuiX, TempGuiY, TempGuiZ);
    }

    // Textures

    public static int CraftingStationTop;
    public static int CraftingStationSlabSide;
    public static int TableBottom;
    public static int FurnaceSlabFront;
    public static int FurnaceSlabFrontActive;
    public static int FurnaceSlabSide;
    public static int SmelteryFront;
    public static int SmelteryFrontActive;
    public static int SmelterySide;
    public static int LavaTankTop;
    public static int LavaTankSide;
    public static int LavaTankBottom;

    @EventListener
    public void registerTextures(TextureRegisterEvent event) {
        ExpandableAtlas terrainAtlas = Atlases.getTerrain();

        CraftingStationTop = terrainAtlas.addTexture(NAMESPACE.id("block/table/crafting_station_top")).index;
        CraftingStationSlabSide = terrainAtlas.addTexture(NAMESPACE.id("block/table/crafting_station_slab_side")).index;
        TableBottom = terrainAtlas.addTexture(NAMESPACE.id("block/table/bottom")).index;
        FurnaceSlabFront = terrainAtlas.addTexture(NAMESPACE.id("block/furnace_slab_front")).index;
        FurnaceSlabFrontActive = terrainAtlas.addTexture(NAMESPACE.id("block/furnace_slab_front_active")).index;
        FurnaceSlabSide = terrainAtlas.addTexture(NAMESPACE.id("block/furnace_slab_side")).index;
        SmelteryFront = terrainAtlas.addTexture(NAMESPACE.id("block/smeltery/smeltery_inactive")).index;
        SmelteryFrontActive = terrainAtlas.addTexture(NAMESPACE.id("block/smeltery/smeltery_active")).index;
        SmelterySide = terrainAtlas.addTexture(NAMESPACE.id("block/seared_bricks")).index;
        LavaTankTop = terrainAtlas.addTexture(NAMESPACE.id("block/smeltery/lava_tank_top")).index;
        LavaTankSide = terrainAtlas.addTexture(NAMESPACE.id("block/smeltery/lava_tank_side")).index;
        LavaTankBottom = terrainAtlas.addTexture(NAMESPACE.id("block/smeltery/seared_gague_top")).index;
    }
}
