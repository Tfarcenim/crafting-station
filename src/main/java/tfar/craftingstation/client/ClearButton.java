package tfar.craftingstation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ClearButton extends Button {


  public ClearButton(int x, int y, int widthIn, int heightIn, OnPress callback) {
    super(x, y, widthIn, heightIn, Component.empty(), callback,Button.DEFAULT_NARRATION);
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
    blit(stack,getX(), getY(), 0,
            46 + i * 20, halfwidth1, halfheight1);
    blit(stack,getX() + halfwidth1, getY(), 200 - halfwidth2,
            46 + i * 20, halfwidth2, halfheight1);

    blit(stack,getX(), getY() + halfheight1,
            0, 46 + i * 20 + 20 - halfheight2, halfwidth1, halfheight2);
    blit(stack,getX() + halfwidth1, getY() + halfheight1,
            200 - halfwidth2, 46 + i * 20 + 20 - halfheight2, halfwidth2, halfheight2);
  }
}