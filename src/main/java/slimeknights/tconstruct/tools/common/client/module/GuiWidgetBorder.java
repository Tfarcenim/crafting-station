package slimeknights.tconstruct.tools.common.client.module;

import slimeknights.tconstruct.mantle.client.gui.GuiElement;
import slimeknights.tconstruct.mantle.client.gui.GuiElementScalable;
import slimeknights.tconstruct.mantle.client.gui.GuiModule;
import slimeknights.tconstruct.mantle.client.gui.GuiWidget;

public class GuiWidgetBorder extends GuiWidget {

  // all elements based on generic gui
  public GuiElement cornerTopLeft = GuiGeneric.cornerTopLeft;
  public GuiElement cornerTopRight = GuiGeneric.cornerTopRight;
  public GuiElement cornerBottomLeft = GuiGeneric.cornerBottomLeft;
  public GuiElement cornerBottomRight = GuiGeneric.cornerBottomRight;

  public GuiElementScalable borderTop = GuiGeneric.borderTop;
  public GuiElementScalable borderBottom = GuiGeneric.borderBottom;
  public GuiElementScalable borderLeft = GuiGeneric.borderLeft;
  public GuiElementScalable borderRight = GuiGeneric.borderRight;

  public int w = borderLeft.w;
  public int h = borderTop.h;

  public int getHeightWithBorder(int height) {
    return height + borderTop.h + borderBottom.h;
  }

  @Override
  public void draw() {
    int x = xPos;
    int y = yPos;
    int midW = width - borderLeft.w - borderRight.w;
    int midH = height - borderTop.h - borderBottom.h;

    // top row
    x += cornerTopLeft.draw(x, y);
    x += borderTop.drawScaledX(x, y, midW);
    cornerTopRight.draw(x, y);

    // center row
    x = xPos;
    y += borderTop.h;
    x += borderLeft.drawScaledY(x, y, midH);
    x += midW;
    borderRight.drawScaledY(x, y, midH);

    // bottom row
    x = xPos;
    y += midH;
    x += cornerBottomLeft.draw(x, y);
    x += borderBottom.drawScaledX(x, y, midW);
    cornerBottomRight.draw(x, y);
  }
}
