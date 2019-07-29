package com.tfar.examplemod.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tfar.examplemod.CraftingStation;
import com.tfar.examplemod.CraftingStationContainer;
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
  private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
  public RecipeBookGui recipeBookGui = new RecipeBookGui();

  public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(CraftingStation.MODID,"textures/gui/slot.png");
  public static final ResourceLocation SECONDARY_GUI_TEXTURE = new ResourceLocation(CraftingStation.MODID,"textures/gui/secondary.png");


  private boolean widthTooNarrow;


  public CraftingStationScreen(CraftingStationContainer p_i51094_1_, PlayerInventory p_i51094_2_, ITextComponent p_i51094_3_) {
    super(p_i51094_1_, p_i51094_2_, p_i51094_3_);
  }

  protected void init() {
    super.init();
    this.widthTooNarrow = this.width < 379;
    this.recipeBookGui.func_201520_a(this.width, this.height, this.minecraft, this.widthTooNarrow, this.container);
    this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
    this.children.add(this.recipeBookGui);
    this.func_212928_a(this.recipeBookGui);
    this.addButton(new ImageButton(this.guiLeft + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (p_214076_1_) -> {
      this.recipeBookGui.func_201518_a(this.widthTooNarrow);
      this.recipeBookGui.toggleVisibility();
      this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
      ((ImageButton) p_214076_1_).setPosition(this.guiLeft + 5, this.height / 2 - 49);
    }));
  }

  public void tick() {
    super.tick();
    this.recipeBookGui.tick();
  }

  public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
    this.renderBackground();
    if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
      this.drawGuiContainerBackgroundLayer(p_render_3_, p_render_1_, p_render_2_);
      this.recipeBookGui.render(p_render_1_, p_render_2_, p_render_3_);
    } else {
      this.recipeBookGui.render(p_render_1_, p_render_2_, p_render_3_);
      super.render(p_render_1_, p_render_2_, p_render_3_);
      this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, true, p_render_3_);
    }

    this.renderHoveredToolTip(p_render_1_, p_render_2_);
    this.recipeBookGui.renderTooltip(this.guiLeft, this.guiTop, p_render_1_, p_render_2_);
    this.func_212932_b(this.recipeBookGui);
  }

  protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
    this.font.drawString(this.title.getFormattedText(), 28.0F, 6.0F, 4210752);
    this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, (float) (this.ySize - 96 + 2), 4210752);
    if (container.subContainer != null){
      this.font.drawString(container.containerName.getFormattedText(),-110,6,4210752);
    }
  }

  protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.minecraft.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
    int lvt_4_1_ = this.guiLeft;
    int lvt_5_1_ = (this.height - this.ySize) / 2;
    this.blit(lvt_4_1_, lvt_5_1_, 0, 0, this.xSize, this.ySize);
    if (container.subContainer != null){
      this.minecraft.getTextureManager().bindTexture(SECONDARY_GUI_TEXTURE);
      this.blit(lvt_4_1_ - 117, lvt_5_1_, 0, 0, this.xSize, this.ySize + 18);
      this.minecraft.getTextureManager().bindTexture(SLOT_TEXTURE);
      for (int i = 0;i < (container.range[1] - container.range[0]);i++){
        int j = i % 6;
        int k = i / 6;
        blit(lvt_4_1_ - 112 + j * 18, lvt_5_1_ + 18 * k + 16, 0, 0, 18, 18,18,18);
      }
    }
  }

  protected boolean isPointInRegion(int p_195359_1_, int p_195359_2_, int p_195359_3_, int p_195359_4_, double p_195359_5_, double p_195359_7_) {
    return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isPointInRegion(p_195359_1_, p_195359_2_, p_195359_3_, p_195359_4_, p_195359_5_, p_195359_7_);
  }

  public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
    if (this.recipeBookGui.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
      return true;
    } else {
      return this.widthTooNarrow && this.recipeBookGui.isVisible() || super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
    }
  }

  protected boolean hasClickedOutside(double p_195361_1_, double p_195361_3_, int p_195361_5_, int p_195361_6_, int p_195361_7_) {
    boolean lvt_8_1_ = p_195361_1_ < (double) p_195361_5_ || p_195361_3_ < (double) p_195361_6_ || p_195361_1_ >= (double) (p_195361_5_ + this.xSize) || p_195361_3_ >= (double) (p_195361_6_ + this.ySize);
    return this.recipeBookGui.func_195604_a(p_195361_1_, p_195361_3_, this.guiLeft, this.guiTop, this.xSize, this.ySize, p_195361_7_) && lvt_8_1_;
  }

  protected void handleMouseClick(Slot p_184098_1_, int p_184098_2_, int p_184098_3_, ClickType p_184098_4_) {
    super.handleMouseClick(p_184098_1_, p_184098_2_, p_184098_3_, p_184098_4_);
    this.recipeBookGui.slotClicked(p_184098_1_);
  }

  public void recipesUpdated() {
    this.recipeBookGui.recipesUpdated();
  }

  public void removed() {
    this.recipeBookGui.removed();
    super.removed();
  }

  public RecipeBookGui func_194310_f() {
    return this.recipeBookGui;
  }
}
