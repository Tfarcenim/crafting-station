package tfar.craftingstation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import tfar.craftingstation.CraftingStation;
import tfar.craftingstation.CraftingStationMenu;
import tfar.craftingstation.network.C2SClearPacket;
import tfar.craftingstation.network.PacketHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

public class CraftingStationScreen extends AbstractContainerScreen<CraftingStationMenu> {
  public static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/crafting_table.png");

  public static final ResourceLocation SCROLLBAR_AND_TAB = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

  private static final ResourceLocation SCROLLBAR_BACKGROUND_AND_TAB = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");

  public static final ResourceLocation SECONDARY_GUI_TEXTURE = new ResourceLocation(CraftingStation.MODID, "textures/gui/secondary.png");

  /**
   * Amount scrolled in inventory (0 = top, 1 = bottom)
   */
  private double currentScroll;

  private boolean isScrolling = false;

  private int topRow;

  public CraftingStationScreen(CraftingStationMenu p_i51094_1_, Inventory p_i51094_2_, Component p_i51094_3_) {
    super(p_i51094_1_, p_i51094_2_, p_i51094_3_);
    topRow = 0;
  }

  @Override
  protected void init() {
    super.init();
    if (this.menu.hasSideContainers) {
      for (int i = 0; i < menu.containerStarts.size(); i++) {

        Tooltip tab = null;//Tooltip.create((TabButton)button).stack, x, y);

        addRenderableWidget(new TabButton(leftPos - 128 + 21 * i, topPos - 22, 22, 28, button -> changeContainer(((TabButton)button).index),i,menu.blocks.get(i),this));
      }
    }
    if (!ModList.get().isLoaded("craftingtweaks")) {

      Tooltip tooltipC =  Tooltip.create(Component.translatable("text.crafting_station.clear"));

      ClearButton clear =new ClearButton(leftPos + 85, topPos + 16,7,7, b -> PacketHandler.INSTANCE.sendToServer(new C2SClearPacket()));
      clear.setTooltip(tooltipC);
      this.addRenderableWidget(clear);
    }
  }

  @Override
  protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
    boolean b = super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton) &&
            (!menu.hasSideContainers || !isHovering(-126, -16, 126, 32 + imageHeight, mouseX, mouseY));
    return b;
  }

  public void changeContainer(int container){
    this.menu.changeContainer(container);
  }

  @Override
  public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
    renderBackground(stack);
    super.render(stack,mouseX, mouseY, partialTicks);
    renderTooltip(stack,mouseX, mouseY);
  }

  protected void renderLabels(GuiGraphics stack,int p_146979_1_, int p_146979_2_) {
    super.renderLabels(stack, p_146979_1_, p_146979_2_);
    if (menu.hasSideContainers){
      stack.drawString(font, getTruncatedString(),-122,6, 0x404040,false);
    }
  }

  String getTruncatedString() {
    String string = menu.containerNames.get(menu.getCurrentContainer()).getString();
    if (string.length() > 23) {
      return string.substring(0, 23) + "...";
    }
    return string;
  }

  @Override
  protected void renderBg(GuiGraphics stack,float partialTicks, int mouseX, int mouseY) {
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    stack.blit(CRAFTING_TABLE_GUI_TEXTURES,leftPos, topPos, 0, 0, imageWidth, imageHeight);
    int i = this.leftPos;

    int i1 = i - 16;
    int i2 = i1 + 14;

    int j = (this.height - this.imageHeight) / 2;
    if (this.menu.hasSideContainers) {
      //draw background
      //bind(SECONDARY_GUI_TEXTURE);
      stack.blit(SECONDARY_GUI_TEXTURE,i - 130, j, 0, 0, this.imageWidth, this.imageHeight + 18);

      bind(SCROLLBAR_BACKGROUND_AND_TAB);
      int totalSlots = this.menu.getSlotCount();
      int slotsToDraw = 54;
      if (totalSlots < slotsToDraw) slotsToDraw = totalSlots;
      else if (hasScrollbar() && topRow == this.menu.getRows() - 9 && totalSlots % 6 != 0)
        slotsToDraw = 54 - 6 + totalSlots % 6;

      int offset = hasScrollbar() ? -126 : -118;

      for (int i3 = 0; i3 < slotsToDraw; i3++) {
        int j1 = i3 % 6;
        int k1 = i3 / 6;
        stack.blit(SCROLLBAR_BACKGROUND_AND_TAB,i + j1 * 18 + offset, 18 * k1 + j + 16, 8, 17, 18, 18);
      }

      if (this.hasScrollbar()) {
        stack.blit(SCROLLBAR_BACKGROUND_AND_TAB,i - 17, j + 16, 174, 17, 14, 100);
        stack.blit(SCROLLBAR_BACKGROUND_AND_TAB,i - 17, j + 67, 174, 18, 14, 111);
        bind(SCROLLBAR_AND_TAB);
        int k = (int) (j + 17 + 145 * currentScroll);

        if (isScrolling && mouseX <= i2 && mouseX >= i1)
          stack.blit(SCROLLBAR_AND_TAB,i - 16, k, 244, 0, 12, 15);
        else stack.blit(SCROLLBAR_AND_TAB,i - 16, k, 244 - 12, 0, 12, 15);
      }
    }
  }

  private static void bind(ResourceLocation tex) {
    RenderSystem.setShaderTexture(0,tex);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int scroll) {
    this.isScrolling = this.hasScrollbar();
    return super.mouseClicked(mouseX,mouseY,scroll);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
    if (this.isScrolling){
      int j = this.topPos;
      int j1 = j + 24;
      int j2 = j1 + 145;
      int k = this.leftPos;
      int k1 = k - 16;
      int k2 = k1 + 14;

      if (mouseX <= k2 && mouseX >= k1) {
        this.currentScroll = (mouseY - j1) / (j2 - j1 - 0f);
        currentScroll = Mth.clamp(currentScroll, 0, 1);
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
    return menu.getRows() > 9;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {

    if (this.hasScrollbar() && mouseX < leftPos && mouseX > leftPos - 20) {
      setTopRow((int) (topRow - scrollDelta), false);
      return true;
    }
    return false;
  }

  private void scrollTo(double scroll) {
    setTopRow((int) Math.round((menu.getRows() - 9) * scroll), true);
  }

  private void setTopRow(int offset, boolean smooth) {
    topRow = offset;
    if (topRow < 0) topRow = 0;
    else if (topRow > menu.getRows() - 9) topRow = menu.getRows() - 9;
    menu.updateSlotPositions(topRow);
    if (!smooth) this.currentScroll = (double) topRow / (this.menu.getRows() - 9);
  }
}

