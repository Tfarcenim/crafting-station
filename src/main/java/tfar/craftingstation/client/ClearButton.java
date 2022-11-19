package tfar.craftingstation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ClearButton extends Button {


  public ClearButton(int x, int y, int widthIn, int heightIn, OnPress callback,OnTooltip tooltip) {
    super(x, y, widthIn, heightIn, Component.empty(), callback,tooltip);
  }


  @Override
  public void renderButton(PoseStack stack,int mouseX, int mouseY, float partialTicks) {
    RenderSystem.setShaderTexture(0,WIDGETS_LOCATION);

    RenderSystem.setShaderColor(1, 0, 0,1);

    int i = getYImage(isHoveredOrFocused());

    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(770, 771, 1, 0);
    RenderSystem.blendFunc(770, 771);

    int halfwidth1 = this.width / 2;
    int halfwidth2 = this.width - halfwidth1;
    int halfheight1 = this.height / 2;
    int halfheight2 = this.height - halfheight1;
    blit(stack,x, y, 0,
            46 + i * 20, halfwidth1, halfheight1);
    blit(stack,x + halfwidth1, y, 200 - halfwidth2,
            46 + i * 20, halfwidth2, halfheight1);

    blit(stack,x, y + halfheight1,
            0, 46 + i * 20 + 20 - halfheight2, halfwidth1, halfheight2);
    blit(stack,x + halfwidth1, y + halfheight1,
            200 - halfwidth2, 46 + i * 20 + 20 - halfheight2, halfwidth2, halfheight2);

    if (this.isHoveredOrFocused()) {
      this.renderToolTip(stack,mouseX,mouseY);
    }
  }
}