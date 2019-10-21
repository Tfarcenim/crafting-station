package com.tfar.craftingstation.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static com.tfar.craftingstation.client.ClientStuffs.mc;

public class TabButton extends Button{

  public final int index;
  public final ItemStack stack;
  public TabButton(int x, int y, int widthIn, int heightIn, Button.IPressable callback, int index, ItemStack stack) {
    super(x, y, widthIn, heightIn,"", callback);
    this.index = index;
    this.stack = stack;
  }
  public static final ResourceLocation TAB = new ResourceLocation(CraftingStation.MODID,"textures/gui/tabs.png");


  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    if (visible) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.getTextureManager().bindTexture(TAB);


      isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

      int i = getYImage(isHovered);

      GlStateManager.enableBlend();
      GlStateManager.blendFuncSeparate(770, 771, 1, 0);
      GlStateManager.blendFunc(770, 771);
      if (((CraftingStationContainer)mc.player.openContainer).currentContainer == index)
      blit(x, y, 0, height, width, height,width,height * 2);

      else      blit(x, y, 0, 0, width, height,width,height * 2);
      if (!stack.isEmpty()) {
        // String s1 = s.getUnformattedComponentText();
        //String slot = String.valueOf(Utils.getSelectedSlot(bag));

        Integer color = stack.getItem().getRarity(stack).color.getColor();

        int c = color != null ? color : 0xFFFFFF;
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        final int itemX = x + 3;
        final int itemY = y + 3;

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, itemX, itemY);

        mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, stack, itemX, itemY);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
      }
    }

    }
  }

