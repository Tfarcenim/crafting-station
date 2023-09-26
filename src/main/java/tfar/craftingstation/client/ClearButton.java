package tfar.craftingstation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ClearButton extends Button {


  public ClearButton(int x, int y, int widthIn, int heightIn, OnPress callback) {
    super(x, y, widthIn, heightIn, Component.empty(), callback,Button.DEFAULT_NARRATION);
  }


  @Override
  public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    guiGraphics.setColor(1,0,0,this.alpha);
    RenderSystem.enableBlend();
    RenderSystem.enableDepthTest();
    guiGraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    int i = getFGColor();
    this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
  }

  private int getTextureY() {
    int i = 1;
    if (!this.active) {
      i = 0;
    } else if (this.isHoveredOrFocused()) {
      i = 2;
    }

    return 46 + i * 20;
  }

}