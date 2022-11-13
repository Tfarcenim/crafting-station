package com.tfar.craftingstation.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.gui.widget.button.Button.ITooltip;

public class ClearButton extends Button {


  public ClearButton(int x, int y, int widthIn, int heightIn, IPressable callback,ITooltip tooltip) {
    super(x, y, widthIn, heightIn,new StringTextComponent(""), callback,tooltip);
  }


  @Override
  public void renderButton(MatrixStack stack,int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.getTextureManager().bind(WIDGETS_LOCATION);

    RenderSystem.color3f(1, 0, 0);

    int i = getYImage(isHovered());

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

    if (this.isHovered()) {
      this.renderToolTip(stack,mouseX,mouseY);
    }
  }
}