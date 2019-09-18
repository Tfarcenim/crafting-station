package com.tfar.craftingstation.jei;

import com.tfar.craftingstation.CraftingStationContainer;
import com.tfar.craftingstation.client.CraftingStationScreen;
import mezz.jei.api.gui.IGuiProperties;
import net.minecraft.client.gui.GuiScreen;

public class GuiProperties implements IGuiProperties {

  private final Class<? extends GuiScreen> guiClass;
  private final int guiLeft;
  private final int guiTop;
  private final int guiXSize;
  private final int guiYSize;
  private final int screenWidth;
  private final int screenHeight;
  private final boolean hasSideContainer;

  public static GuiProperties create(CraftingStationScreen craftingStationScreen) {
    return new GuiProperties(
            craftingStationScreen.getClass(),
            craftingStationScreen.getGuiLeft(),
            craftingStationScreen.getGuiTop(),
            craftingStationScreen.getXSize(),
            craftingStationScreen.getYSize(),
            craftingStationScreen.width,
            craftingStationScreen.height,
            ((CraftingStationContainer)craftingStationScreen.inventorySlots).hasSideContainer
    );
  }

   GuiProperties(Class<? extends GuiScreen> guiClass, int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight,boolean hasSideContainer) {
    this.guiClass = guiClass;
    this.guiLeft = guiLeft;
    this.guiTop = guiTop;
    this.guiXSize = guiXSize;
    this.guiYSize = guiYSize;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.hasSideContainer = hasSideContainer;
  }

  public Class<? extends GuiScreen> getGuiClass() {
    return guiClass;
  }

  public int getGuiLeft() {
    return hasSideContainer ? guiLeft - 70 : guiLeft;
  }

  public int getGuiTop() {
    return guiTop;
  }

  public int getGuiXSize() {
    return hasSideContainer ? guiXSize + 70: guiXSize;
  }

  public int getGuiYSize() {
    return guiYSize;
  }

  public int getScreenWidth() {
    return screenWidth;
  }

  public int getScreenHeight() {
    return screenHeight;
  }
}
