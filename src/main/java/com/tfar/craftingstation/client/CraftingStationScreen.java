package com.tfar.craftingstation.client;

import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
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

  private final int realRows;
  private int topRow;

  private static final ResourceLocation backgroundTexture = new ResourceLocation("textures/gui/container/crafting_table.png");

  public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(CraftingStation.MODID, "textures/gui/slot.png");
  public static final ResourceLocation SECONDARY_GUI_TEXTURE = new ResourceLocation(CraftingStation.MODID, "textures/gui/secondary.png");

  public CraftingStationScreen(CraftingStationContainer inv) {
    super(inv);
    realRows = inv.getRows();
    topRow = 0;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);
    renderHoveredToolTip(mouseX, mouseY);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    fontRenderer.drawString(I18n.format("title.crafting_station"), 28, 6, 0x404040);
    fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, 0x404040);
    if (((CraftingStationContainer)this.inventorySlots).hasSideContainer)
      this.fontRenderer.drawString(((CraftingStationContainer) inventorySlots)
              .containerName.getFormattedText(), -110, 6, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    mc.getTextureManager().bindTexture(backgroundTexture);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    int i = this.guiLeft;
    int j = (this.height - this.ySize) / 2;
    if (((CraftingStationContainer)this.inventorySlots).hasSideContainer){
      this.mc.getTextureManager().bindTexture(SECONDARY_GUI_TEXTURE);
      this.drawTexturedModalRect(i - 117, j, 0, 0, this.xSize, this.ySize + 18);

      this.mc.getTextureManager().bindTexture(SLOT_TEXTURE);
      for (int i1 = 0; i1 < Math.min((((CraftingStationContainer) this.inventorySlots).subContainerSlotEnd - ((CraftingStationContainer) this.inventorySlots).subContainerSlotStart), 54); i1++) {
        int j1 = i1 % 6;
        int k1 = i1 / 6;
        drawModalRectWithCustomSizedTexture(i + j1 * 18 - 112, 18 * k1 + j + 17, 0, 0, 18, 18, 18, 18);
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
    int scrollDelta = Mouse.getEventDWheel();

    if (scrollDelta != 0 && this.hasScrollbar()) {

      if (scrollDelta > 0) {
        scrollDelta = 1;
      }

      if (scrollDelta < 0) {
        scrollDelta = -1;
      }
      setTopRow(topRow + scrollDelta);
    }
  }

  private void setTopRow(int value) {
    topRow = value;
    if (topRow < 0) topRow = 0;
    else if (topRow > realRows - 9) topRow = realRows - 9;
    ((CraftingStationContainer) inventorySlots).updateSlotPositions(topRow);
  }
}