package com.tfar.craftingstation.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class CraftingStationScreen extends ContainerScreen<CraftingStationContainer> {
  public static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/crafting_table.png");

  public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(CraftingStation.MODID,"textures/gui/slot.png");
  public static final ResourceLocation SECONDARY_GUI_TEXTURE = new ResourceLocation(CraftingStation.MODID,"textures/gui/secondary.png");

  public int topRow;

  private int realRows;

  public CraftingStationScreen(CraftingStationContainer p_i51094_1_, PlayerInventory p_i51094_2_, ITextComponent p_i51094_3_) {
    super(p_i51094_1_, p_i51094_2_, p_i51094_3_);
    realRows = p_i51094_1_.getRows();
    topRow = 0;
  }

  public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
    this.renderBackground();
    super.render(p_render_1_, p_render_2_, p_render_3_);

    this.renderHoveredToolTip(p_render_1_, p_render_2_);
  }

  protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
    this.font.drawString(this.title.getFormattedText(), 28.0F, 6.0F, 4210752);
    this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, (float) (this.ySize - 96 + 2), 4210752);
    if (container.subContainerSlotStart != -1){
      this.font.drawString(container.containerName.getFormattedText(),-110,6,4210752);
    }
  }

  protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.minecraft.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
    int lvt_4_1_ = this.guiLeft;
    int lvt_5_1_ = (this.height - this.ySize) / 2;
    this.blit(lvt_4_1_, lvt_5_1_, 0, 0, this.xSize, this.ySize);
    if (container.subContainerSlotStart != -1){
      this.minecraft.getTextureManager().bindTexture(SECONDARY_GUI_TEXTURE);
      this.blit(lvt_4_1_ - 117, lvt_5_1_, 0, 0, this.xSize, this.ySize + 18);
      this.minecraft.getTextureManager().bindTexture(SLOT_TEXTURE);
      for (int i = 0; i < Math.min((this.container.subContainerSlotEnd - this.container.subContainerSlotStart), 54); i++){
        int j = i % 6;
        int k = i / 6;
        blit(lvt_4_1_ - 112 + j * 18, lvt_5_1_ + 18 * k + 16, 0, 0, 18, 18,18,18);
      }
    }
  }

  private boolean hasScrollbar() {
    return realRows > 9;
  }

  @Override
  public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double scrollDelta) {

    if (this.hasScrollbar()) {
      setTopRow(topRow - (int)scrollDelta);
      return true;
    }
    return false;
  }

  private void setTopRow(int offset) {
    topRow = offset;
    if (topRow < 0) topRow = 0;
    else if (topRow > realRows - 9) topRow = realRows - 9;
    container.updateSlotPositions(topRow);
  }
}

