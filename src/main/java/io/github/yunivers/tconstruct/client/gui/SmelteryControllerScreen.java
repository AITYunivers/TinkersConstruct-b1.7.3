package io.github.yunivers.tconstruct.client.gui;

import io.github.yunivers.stationfluidapi.api.FluidStack;
import io.github.yunivers.tconstruct.blocks.entity.SmelteryEntity;
import io.github.yunivers.tconstruct.inventory.SmelteryHandler;
import io.github.yunivers.tconstruct.mixin.MinecraftAccessor;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SmelteryControllerScreen extends HandledScreen
{
    public SmelteryEntity smeltery;
    boolean isScrolling = false;
    boolean wasClicking;
    float currentScroll = 0.0F;
    int slotPos = 0;
    int prevSlotPos = 0;

    int mouseX;
    int mouseY;

    int guiLeft;
    int guiTop;

    public SmelteryControllerScreen(PlayerInventory playerInventory, SmelteryEntity smeltery, World world, int x, int y, int z) {
        super(smeltery.getGuiContainer(playerInventory, world, x, y, z));
        this.smeltery = smeltery;
        backgroundWidth = 248;
        smeltery.updateFuelDisplay();
    }

    @Override
    public void init()
    {
        super.init();
        guiLeft = (this.width - this.backgroundWidth) / 2;
        guiTop = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public void removed()
    {
        super.removed();
    }

    @Override
    public void render(int mouseX, int mouseY, float delta)
    {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.render(mouseX, mouseY, delta);
        updateScrollbar(mouseX, mouseY, delta);
    }

    protected void updateScrollbar (int mouseX, int mouseY, float delta)
    {
        if (smeltery.getLayers() > 2)
        {
            boolean mouseDown = Mouse.isButtonDown(0);
            int lefto = this.guiLeft;
            int topo = this.guiTop;
            int xScroll = lefto + 67;
            int yScroll = topo + 8;
            int scrollWidth = xScroll + 14;
            int scrollHeight = yScroll + 144;

            if (!this.wasClicking && mouseDown && mouseX >= xScroll && mouseY >= yScroll && mouseX < scrollWidth && mouseY < scrollHeight)
            {
                this.isScrolling = true;
            }

            if (!mouseDown)
            {
                this.isScrolling = false;
            }

            if (wasClicking && !isScrolling && slotPos != prevSlotPos)
            {
                prevSlotPos = slotPos;
            }

            this.wasClicking = mouseDown;

            if (this.isScrolling)
            {
                this.currentScroll = (mouseY - yScroll - 7.5F) / (scrollHeight - yScroll - 15.0F);

                if (this.currentScroll < 0.0F)
                {
                    this.currentScroll = 0.0F;
                }

                if (this.currentScroll > 1.0F)
                {
                    this.currentScroll = 1.0F;
                }

                int s = ((SmelteryHandler)this.container).scrollTo(this.currentScroll);
                if (s != -1)
                    slotPos = s;
            }
        }
    }

    @Override
    protected void drawForeground()
    {
        textRenderer.draw("Smeltery", 86, 5, 0x404040);
        textRenderer.draw("Inventory", 90, (backgroundHeight - 96) + 2, 0x404040);

        int base = 0;
        int cornerX = (width - backgroundWidth) / 2 + 36;
        int cornerY = (height - backgroundHeight) / 2;

        for (FluidStack liquid : smeltery.moltenMetal)
        {
            int basePos = 54;
            int initialLiquidSize = 0;
            int liquidSize = 0;//liquid.amount * 52 / liquidLayers;
            if (smeltery.getCapacity() > 0)
            {
                int total = smeltery.getTotalLiquid();
                int liquidLayers = (total / 20000 + 1) * 20000;
                if (liquidLayers > 0)
                {
                    liquidSize = liquid.amount * 52 / liquidLayers;
                    if (liquidSize == 0)
                        liquidSize = 1;
                    base += liquidSize;
                }
            }

            int leftX = cornerX + basePos;
            int topY = (cornerY + 68) - base;
            int sizeX = 52;
            int sizeY = liquidSize;
            if (mouseX >= leftX && mouseX <= leftX + sizeX && mouseY >= topY && mouseY < topY + sizeY)
            {
                drawFluidStackTooltip(liquid, mouseX - cornerX + 36, mouseY - cornerY);
            }
        }
        if (smeltery.fuelGague > 0)
        {
            int leftX = cornerX + 117;
            int topY = (cornerY + 68-52);
            int sizeX = 12;
            int sizeY = 52;
            if (mouseX >= leftX && mouseX <= leftX + sizeX && mouseY >= topY && mouseY < topY + sizeY)
            {
                //drawFluidStackTooltip(new FluidStack(-37, smeltery.fuelAmount), mouseX - cornerX + 36, mouseY - cornerY);
            }
        }
    }

    private static final int background = MinecraftAccessor.getInstance().textureManager.getTextureId("/assets/tconstruct/stationapi/textures/gui/smeltery.png");
    private static final int backgroundSide = MinecraftAccessor.getInstance().textureManager.getTextureId("/assets/tconstruct/stationapi/textures/gui/smelteryside.png");

    @Override
    protected void drawBackground(float tickDelta)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.textureManager.bindTexture(background);
        int cornerX = (width - backgroundWidth) / 2 + 36;
        int cornerY = (height - backgroundHeight) / 2;
        drawTexture(cornerX + 46, cornerY, 0, 0, 176, backgroundHeight);

        /*//Fuel - Lava
        minecraft.textureManager.bindTexture(TextureMap.locationBlocksTexture);
        if (logic.fuelGague > 0)
        {
            Icon lavaIcon = Block.lavaStill.getIcon(0, 0);
            int fuel = logic.getScaledFuelGague(52);
            int count = 0;
            while (fuel > 0)
            {
                int size = fuel >= 16 ? 16 : fuel;
                fuel -= size;
                drawLiquidRect(cornerX + 117, (cornerY + 68) - size - 16 * count, lavaIcon, 12, size);
                count++;
            }
        }

        //Liquids - molten metal
        int base = 0;
        for (FluidStack liquid : logic.moltenMetal)
        {
            Icon renderIndex = liquid.getFluid().getStillIcon();
            int basePos = 54;
            if (logic.getCapacity() > 0)
            {
                int total = logic.getTotalLiquid();
                int liquidLayers = (total / 20000 + 1) * 20000;
                if (liquidLayers > 0)
                {
                    int liquidSize = liquid.amount * 52 / liquidLayers;
                    if (liquidSize == 0)
                        liquidSize = 1;
                    while (liquidSize > 0)
                    {
                        int size = liquidSize >= 16 ? 16 : liquidSize;
                        if (renderIndex != null)
                        {
                            drawLiquidRect(cornerX + basePos, (cornerY + 68) - size - base, renderIndex, 16, size);
                            drawLiquidRect(cornerX + basePos + 16, (cornerY + 68) - size - base, renderIndex, 16, size);
                            drawLiquidRect(cornerX + basePos + 32, (cornerY + 68) - size - base, renderIndex, 16, size);
                            drawLiquidRect(cornerX + basePos + 48, (cornerY + 68) - size - base, renderIndex, 4, size);
                        }
                        liquidSize -= size;
                        base += size;
                    }
                }
            }
        }*/

        //Liquid gague
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        minecraft.textureManager.bindTexture(background);
        drawTexture(cornerX + 54, cornerY + 16, 176, 76, 52, 52);

        //Side inventory
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.textureManager.bindTexture(backgroundSide);
        if (smeltery.getLayers() > 0)
        {
            if (smeltery.getLayers() == 1)
            {
                drawTexture(cornerX - 46, cornerY, 0, 0, 98, 43);
                drawTexture(cornerX - 46, cornerY + 43, 0, 133, 98, 25);
            }
            else if (smeltery.getLayers() == 2)
            {
                drawTexture(cornerX - 46, cornerY, 0, 0, 98, 61);
                drawTexture(cornerX - 46, cornerY + 61, 0, 97, 98, 61);
            }
            else
            {
                drawTexture(cornerX - 46, cornerY, 0, 0, 98, backgroundHeight - 8);
            }
            drawTexture(cornerX + 32, (int) (cornerY + 8 + 127 * currentScroll), 98, 0, 12, 15);
        }

        //Temperature
        int slotSize = smeltery.getLayers() * 9;
        if (slotSize > 24)
            slotSize = 24;
        for (int iter = 0; iter < slotSize; iter++)
        {
            int slotTemp = smeltery.getTempForSlot(iter + slotPos * 3) - 20;
            int maxTemp = smeltery.getMeltingPointForSlot(iter + slotPos * 3) - 20;
            if (slotTemp > 0 && maxTemp > 0)
            {
                int size = 16 * slotTemp / maxTemp + 1;
                drawTexture(cornerX - 38 + (iter % 3 * 22), cornerY + 8 + (iter / 3 * 18) + 16 - size, 98, 15 + 16 - size, 5, size);
            }
        }
    }

    protected void drawFluidStackTooltip (FluidStack par1ItemStack, int par2, int par3)
    {
        this.zOffset = 100;
        List list = getLiquidTooltip(par1ItemStack);

        this.drawToolTip(list, par2, par3);
        this.zOffset = 0;
    }

    public List getLiquidTooltip (FluidStack liquid)
    {
        ArrayList list = new ArrayList();
        /*if (liquid.fluidID == -37)
        {
            list.add("\u00A7f" + StatCollector.translateToLocal("gui.smeltery1"));
            list.add("mB: " + liquid.amount);
        }
        else*/
        {
            String name = liquid.getFluidName();
            list.add("\u00A7f" + name);
            /*if (name.equals("Liquified Emerald"))
            {
                list.add("Emeralds: " + liquid.amount / 640f);
            }
            else if (name.equals("Molten Glass"))
            {
                int blocks = liquid.amount / 1000;
                if (blocks > 0)
                    list.add("Blocks: " + blocks);
                int panels = (liquid.amount % 1000) / 250;
                if (panels > 0)
                    list.add("Panels: " + panels);
                int mB = (liquid.amount % 1000) % 250;
                if (mB > 0)
                    list.add("mB: " + mB);
            }
            else if (name.contains("Molten"))
            {
                int ingots = liquid.amount / TConstruct.ingotLiquidValue;
                if (ingots > 0)
                    list.add("Ingots: " + ingots);
                int mB = liquid.amount % TConstruct.ingotLiquidValue;
                if (mB > 0)
                {
                    int nuggets = mB / TConstruct.nuggetLiquidValue;
                    int junk = (mB % TConstruct.nuggetLiquidValue);
                    if (nuggets > 0)
                        list.add("Nuggets: " + nuggets);
                    if (junk > 0)
                        list.add("mB: " + junk);
                }
            }
            else if (name.equals("Seared Stone"))
            {
                int ingots = liquid.amount / TConstruct.ingotLiquidValue;
                if (ingots > 0)
                    list.add("Blocks: " + ingots);
                int mB = liquid.amount % TConstruct.ingotLiquidValue;
                if (mB > 0)
                    list.add("mB: " + mB);
            }
            else*/
            {
                list.add("mB: " + liquid.amount);
            }
        }
        return list;
    }

    protected void drawToolTip (List par1List, int par2, int par3)
    {
        if (!par1List.isEmpty())
        {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            //RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;
            Iterator iterator = par1List.iterator();

            while (iterator.hasNext())
            {
                String s = (String) iterator.next();
                int l = this.textRenderer.getWidth(s);

                if (l > k)
                {
                    k = l;
                }
            }

            int i1 = par2 + 12;
            int j1 = par3 - 12;
            int k1 = 8;

            if (par1List.size() > 1)
            {
                k1 += 2 + (par1List.size() - 1) * 10;
            }

            if (i1 + k > this.width)
            {
                i1 -= 28 + k;
            }

            if (j1 + k1 + 6 > this.height)
            {
                j1 = this.height - k1 - 6;
            }

            this.zOffset = 300.0F;
            //itemRenderer.zLevel = 300.0F;
            int l1 = 0xF0100010;
            this.fillGradient(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
            this.fillGradient(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
            this.fillGradient(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
            this.fillGradient(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
            this.fillGradient(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
            int i2 = 0x505000FF;
            int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
            this.fillGradient(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
            this.fillGradient(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
            this.fillGradient(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
            this.fillGradient(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

            for (int k2 = 0; k2 < par1List.size(); ++k2)
            {
                String s1 = (String) par1List.get(k2);
                this.textRenderer.drawWithShadow(s1, i1, j1, -1);

                if (k2 == 0)
                {
                    j1 += 2;
                }

                j1 += 10;
            }

            this.zOffset = 0.0F;
            //itemRenderer.zLevel = 0.0F;
        }
    }

    public void drawLiquidRect (int startU, int startV, Atlas.Sprite sprite, int endU, int endV)
    {
        Tessellator tessellator = Tessellator.INSTANCE;
        tessellator.startQuads();
        tessellator.vertex(startU + 0, startV + endV, this.zOffset, sprite.getStartU(), sprite.getEndV());//Bottom left
        tessellator.vertex(startU + endU, startV + endV, this.zOffset, sprite.getEndU(), sprite.getEndV());//Bottom right
        tessellator.vertex(startU + endU, startV + 0, this.zOffset, sprite.getEndU(), sprite.getStartV());//Top right
        tessellator.vertex(startU + 0, startV + 0, this.zOffset, sprite.getStartU(), sprite.getStartV()); //Top left
        tessellator.draw();
    }

    @Override
    public void mouseClicked (int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int base = 0;
        int cornerX = (width - backgroundWidth) / 2 + 36;
        int cornerY = (height - backgroundHeight) / 2;
        int fluidToBeBroughtUp = -1;

        for (FluidStack liquid : smeltery.moltenMetal)
        {
            int basePos = 54;
            int initialLiquidSize = 0;
            int liquidSize = 0;//liquid.amount * 52 / liquidLayers;
            if (smeltery.getCapacity() > 0)
            {
                int total = smeltery.getTotalLiquid();
                int liquidLayers = (total / 20000 + 1) * 20000;
                if (liquidLayers > 0)
                {
                    liquidSize = liquid.amount * 52 / liquidLayers;
                    if (liquidSize == 0)
                        liquidSize = 1;
                    base += liquidSize;
                }
            }

            int leftX = cornerX + basePos;
            int topY = (cornerY + 68) - base;
            int sizeX = 52;
            int sizeY = liquidSize;
            if (mouseX >= leftX && mouseX <= leftX + sizeX && mouseY >= topY && mouseY < topY + sizeY)
            {
                /*fluidToBeBroughtUp = liquid.fluidID;

                Packet250CustomPayload packet = new Packet250CustomPayload();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);

                try
                {
                    dos.write(11);

                    dos.writeInt(logic.worldObj.provider.dimensionId);
                    dos.writeInt(logic.xCoord);
                    dos.writeInt(logic.yCoord);
                    dos.writeInt(logic.zCoord);

                    dos.writeBoolean(this.isShiftKeyDown());

                    dos.writeInt(fluidToBeBroughtUp);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                packet.channel = "TConstruct";
                packet.data = bos.toByteArray();
                packet.length = bos.size();

                PacketDispatcher.sendPacketToServer(packet);*/
            }
        }
    }
}
