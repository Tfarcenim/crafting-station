package com.tfar.craftingstation.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationContainer;
import com.tfar.craftingstation.network.CClearPacket;
import com.tfar.craftingstation.network.PacketHandler;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.ModList;

public class CraftingStationScreen extends ContainerScreen<CraftingStationContainer> {
  public static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/crafting_table.png");

  private static final ResourceLocation SCROLLBAR = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
  private static final ResourceLocation SCROLLBAR_BACKGROUND_AND_TAB = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");

  public static final ResourceLocation SECONDARY_GUI_TEXTURE = new ResourceLocation(CraftingStation.MODID, "textures/gui/secondary.png");
  /**
   * Amount scrolled in inventory (0 = top, 1 = bottom)
   */
  private double currentScroll;

  private boolean isScrolling = false;

  private final int realRows;
  private int topRow;

  public CraftingStationScreen(CraftingStationContainer p_i51094_1_, PlayerInventory p_i51094_2_, ITextComponent p_i51094_3_) {
    super(p_i51094_1_, p_i51094_2_, p_i51094_3_);
    realRows = p_i51094_1_.getRows();
    topRow = 0;
  }

  @Override
  protected void init() {
    super.init();

    if (!ModList.get().isLoaded("craftingtweaks")) {
      this.addButton(new ClearButton(guiLeft + 85, guiTop + 16,7,7, b -> PacketHandler.INSTANCE.sendToServer(new CClearPacket())));
    }
  }

  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    renderBackground();
    super.render(mouseX, mouseY, partialTicks);
    renderHoveredToolTip(mouseX, mouseY);
  }

  protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
    this.font.drawString(this.title.getFormattedText(), 28.0F, 6.0F, 4210752);
    this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, (float) (this.ySize - 96 + 2), 4210752);
    if (container.hasSideContainer){
      this.font.drawString(container.containerName.getFormattedText(),-120,6,4210752);
    }
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    minecraft.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
    blit(guiLeft, guiTop, 0, 0, xSize, ySize);
    int i = this.guiLeft;

    int i1 = i - 16;
    int i2 = i1 + 14;

    int j = (this.height - this.ySize) / 2;
    if (this.container.hasSideContainer) {
      this.minecraft.getTextureManager().bindTexture(SECONDARY_GUI_TEXTURE);
      this.blit(i - 130, j, 0, 0, this.xSize, this.ySize + 18);

      this.minecraft.getTextureManager().bindTexture(SCROLLBAR_BACKGROUND_AND_TAB);
      int totalSlots = this.container.subContainerSize;
      int slotsToDraw = 54;
      if (totalSlots < slotsToDraw) slotsToDraw = totalSlots;
      else if (hasScrollbar() && topRow == this.realRows - 9 && totalSlots % 6 != 0)
        slotsToDraw = 54 - 6 + totalSlots % 6;

      int offset = hasScrollbar() ? -126 : -118;

      for (int i3 = 0; i3 < slotsToDraw; i3++) {
        int j1 = i3 % 6;
        int k1 = i3 / 6;
        blit(i + j1 * 18 + offset, 18 * k1 + j + 16, 8, 17, 18, 18);
      }

      if (this.hasScrollbar()) {
        blit(i - 17, j + 16, 174, 17, 14, 100);
        blit(i - 17, j + 67, 174, 18, 14, 111);
        this.minecraft.getTextureManager().bindTexture(SCROLLBAR);
        int k = (int) (j + 17 + 145 * currentScroll);

        if (isScrolling && mouseX <= i2 && mouseX >= i1)
          blit(i - 16, k, 244, 0, 12, 15);
        else blit(i - 16, k, 244 - 12, 0, 12, 15);
      }

    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int scroll) {
    this.isScrolling = this.hasScrollbar();
    return super.mouseClicked(mouseX,mouseY,scroll);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
    if (this.isScrolling){
      int j = this.guiTop;
      int j1 = j + 24;
      int j2 = j1 + 145;
      int k = this.guiLeft;
      int k1 = k - 16;
      int k2 = k1 + 14;

      if (mouseX <= k2 && mouseX >= k1) {
        this.currentScroll = (mouseY - j1) / (j2 - j1 - 0f);
        currentScroll = MathHelper.clamp(currentScroll, 0, 1);
        scrollTo(currentScroll);
      }
    }
    return super.mouseDragged(mouseX, mouseY, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int scroll) {
    this.isScrolling = false;
    return super.mouseReleased(mouseX, mouseY, scroll);
  }

  private boolean hasScrollbar() {
    return realRows > 9;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {

    if (this.hasScrollbar() && mouseX < guiLeft && mouseX > guiLeft - 20) {
      setTopRow((int) (topRow - scrollDelta), false);
      return true;
    }
    return false;
  }

  private void scrollTo(double scroll) {
    setTopRow((int) Math.round((realRows - 9) * scroll), true);
  }

  private void setTopRow(int offset, boolean smooth) {
    topRow = offset;
    if (topRow < 0) topRow = 0;
    else if (topRow > realRows - 9) topRow = realRows - 9;
    container.updateSlotPositions(topRow);
    if (!smooth) this.currentScroll = (double) topRow / (this.realRows - 9);
  }
}

