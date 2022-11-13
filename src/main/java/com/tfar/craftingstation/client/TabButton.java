package com.tfar.craftingstation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;

import static com.tfar.craftingstation.client.ClientStuffs.mc;

public class TabButton extends Button{

  public final int index;
  public final ItemStack stack;
  public TabButton(int x, int y, int widthIn, int heightIn, Button.OnPress callback, int index, ItemStack stack) {
    super(x, y, widthIn, heightIn,new TextComponent(""), callback);
    this.index = index;
    this.stack = stack;
  }
  public static final ResourceLocation TAB = new ResourceLocation(CraftingStation.MODID,"textures/gui/tabs.png");


  @Override
  public void render(PoseStack matrices,int mouseX, int mouseY, float partialTicks) {
    if (visible) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.getTextureManager().bind(TAB);


      isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(770, 771, 1, 0);
      RenderSystem.blendFunc(770, 771);
      if (((CraftingStationContainer)mc.player.containerMenu).currentContainer == index)
      blit(matrices,x, y, 0, height, width, height,width,height * 2);

      else      blit(matrices,x, y, 0, 0, width, height,width,height * 2);
      if (!stack.isEmpty()) {
        // String s1 = s.getUnformattedComponentText();
        //String slot = String.valueOf(Utils.getSelectedSlot(bag));

        Integer color = stack.getItem().getRarity(stack).color.getColor();

        int c = color != null ? color : 0xFFFFFF;
        final int itemX = x + 3;
        final int itemY = y + 3;
        renderHotbarItem(itemX,itemY,minecraft.getFrameTime(),minecraft.player,stack);

      }
    }
  }


  private static void renderHotbarItem(int x, int y, float partialTicks, Player player, ItemStack stack) {
    float f = (float) stack.getPopTime() - partialTicks;
    if (f > 0.0F) {
      RenderSystem.pushMatrix();
      float f1 = 1.0F + f / 5.0F;
      RenderSystem.translatef((float) (x + 8), (float) (y + 12), 0.0F);
      RenderSystem.scalef(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
      RenderSystem.translatef((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
    }

    mc.getItemRenderer().renderAndDecorateItem(player, stack, x, y);
    if (f > 0.0F) {
      RenderSystem.popMatrix();
    }
    mc.getItemRenderer().renderGuiItemDecorations(mc.font, stack, x, y);
  }


  /**
   * Draws an ItemStack.
   *
   * The z index is increased by 32 (and not decreased afterwards), and the item is then rendered at z=200.
   */
  private void drawItemStack(ItemStack stack, int x, int y) {

    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    RenderSystem.translatef(0.0F, 0.0F, 32.0F);
    this.setBlitOffset(200);
    itemRenderer.blitOffset = 200.0F;
    net.minecraft.client.gui.Font font = stack.getItem().getFontRenderer(stack);
    //if (font == null) font = this.font;
    itemRenderer.renderAndDecorateItem(stack, x, y);
    //itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y - (this.draggedStack.isEmpty() ? 0 : 8), altText);
    this.setBlitOffset(0);
    itemRenderer.blitOffset = 0.0F;
  }

}

