package io.github.yunivers.tconstruct.gui;

import io.github.yunivers.tconstruct.blocks.entity.CraftingStationEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import org.lwjgl.opengl.GL11;

public class CraftingStationScreen extends HandledScreen
{
    public CraftingStationScreen(PlayerInventory inventoryplayer, CraftingStationEntity logic, int i, int j, int k)
    {
        super(logic.getGuiContainer(inventoryplayer, inventoryplayer.player.world, i, j, k));
    }

    public void removed()
    {
        super.removed();
        container.onClosed(minecraft.player);
    }

    protected void drawForeground()
    {
        textRenderer.draw("Crafting Station", 8, 6, 0x404040);
        textRenderer.draw("Inventory", 8, backgroundHeight - 96 + 2, 0x404040);
    }

    protected void drawBackground(float f)
    {
        int i = minecraft.textureManager.getTextureId("/assets/tconstruct/stationapi/textures/gui/crafting.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.textureManager.bindTexture(i);
        int j = (width - backgroundWidth) / 2;
        int k = (height - backgroundHeight) / 2;
        drawTexture(j, k, 0, 0, backgroundWidth, backgroundHeight);
    }
}
