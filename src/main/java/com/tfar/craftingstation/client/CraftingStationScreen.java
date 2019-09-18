package com.tfar.craftingstation.client;

import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@SideOnly(CLIENT)
public class CraftingStationScreen extends GuiContainer {

  /**
   * Amount scrolled in inventory (0 = top, 1 = bottom)
   */
  private double currentScroll;

  boolean isScrolling = false;
  /**
   * True if the left mouse button was held down last time drawScreen was called.
   */
  private boolean wasClicking;
  private final int realRows;
  private int topRow;

  private static final ResourceLocation backgroundTexture = new ResourceLocation("textures/gui/container/crafting_table.png");
  private static final ResourceLocation SCROLLBAR = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
  private static final ResourceLocation SCROLLBAR_BACKGROUND_AND_TAB = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");


  public static final ResourceLocation SECONDARY_GUI_TEXTURE = new ResourceLocation(CraftingStation.MODID, "textures/gui/secondary.png");

  public CraftingStationScreen(CraftingStationContainer inv) {
    super(inv);
    realRows = inv.getRows();
    topRow = 0;
    currentScroll = 0;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();

    boolean isLeftClicking = Mouse.isButtonDown(0);

    int j = this.guiTop;
    int j1 = j + 24;
    int j2 = j1 + 145;
    int k = this.guiLeft;
    int k1 = k - 16;
    int k2 = k1 + 14;

    if (!this.wasClicking && isLeftClicking) {
      isScrolling = hasScrollbar();
    }

    if (!isLeftClicking) this.isScrolling = false;

    this.wasClicking = isLeftClicking;

    if (this.isScrolling && mouseX <= k2 && mouseX >= k1) {
      this.currentScroll = (mouseY - j1) / (j2 - j1 - 0f);
      currentScroll = MathHelper.clamp(currentScroll, 0, 1);
      scrollTo(currentScroll);
    }

    super.drawScreen(mouseX, mouseY, partialTicks);
    renderHoveredToolTip(mouseX, mouseY);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    fontRenderer.drawString(I18n.format("title.crafting_station"), 28, 6, 0x404040);
    fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, 0x404040);
    if (((CraftingStationContainer) this.inventorySlots).hasSideContainer)
      this.fontRenderer.drawString(((CraftingStationContainer) inventorySlots)
              .containerName.getFormattedText(), -125, 6, 0x404040);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    //if (Minecraft.getSystemTime() % 25 == 0)System.out.println(currentScroll);
    mc.getTextureManager().bindTexture(backgroundTexture);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    int i = this.guiLeft;

    int i1 = i - 16;
    int i2 = i1 + 14;

    int j = (this.height - this.ySize) / 2;
    if (((CraftingStationContainer) this.inventorySlots).hasSideContainer) {
      this.mc.getTextureManager().bindTexture(SECONDARY_GUI_TEXTURE);
      this.drawTexturedModalRect(i - 130, j, 0, 0, this.xSize, this.ySize + 18);

      this.mc.getTextureManager().bindTexture(SCROLLBAR_BACKGROUND_AND_TAB);
      int totalSlots = ((CraftingStationContainer) this.inventorySlots).getSubContainerSize();
      int slotsToDraw = 54;
      if (totalSlots < slotsToDraw) slotsToDraw = totalSlots;
      else if (hasScrollbar() && topRow == this.realRows - 9 && totalSlots % 6 != 0)
        slotsToDraw = 54 - 6 + totalSlots % 6;

      int offset = hasScrollbar() ? -126 : -118;

      for (int i3 = 0; i3 < slotsToDraw; i3++) {
        int j1 = i3 % 6;
        int k1 = i3 / 6;
        drawTexturedModalRect(i + j1 * 18 + offset, 18 * k1 + j + 17, 8, 17, 18, 18);
      }

      if (this.hasScrollbar()) {
        drawTexturedModalRect(i - 17, j + 17, 174, 17, 14, 100);
        drawTexturedModalRect(i - 17, j + 68, 174, 18, 14, 111);
        this.mc.getTextureManager().bindTexture(SCROLLBAR);
        int k = (int) (j + 18 + 145 * currentScroll);

        if (wasClicking && mouseX <= i2 && mouseX >= i1)
          drawTexturedModalRect(i - 16, k, 244, 0, 12, 15);
        else drawTexturedModalRect(i - 16, k, 244 - 12, 0, 12, 15);
      }

    }
  }

  private boolean hasScrollbar() {
    return realRows > 9;
  }


  /**
   * Handles mouse input.
   */
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int scrollDelta = -Mouse.getEventDWheel();

    if (scrollDelta != 0 && this.hasScrollbar()) {

      if (scrollDelta > 0) {
        scrollDelta = 1;
      }

      if (scrollDelta < 0) {
        scrollDelta = -1;
      }
      setTopRow(topRow + scrollDelta, false);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  protected void mouseReleased(int mouseX, int mouseY, int state) {
    super.mouseReleased(mouseX, mouseY, state);
  }

  private void scrollTo(double scroll) {
    setTopRow((int) Math.round((realRows - 9) * scroll), true);
  }

  private void setTopRow(int value, boolean smooth) {
    topRow = value;
    if (topRow < 0) topRow = 0;
    else if (topRow > realRows - 9) topRow = realRows - 9;
    ((CraftingStationContainer) inventorySlots).updateSlotPositions(topRow);
    if (!smooth) this.currentScroll = (double) topRow / (this.realRows - 9);
  }
}