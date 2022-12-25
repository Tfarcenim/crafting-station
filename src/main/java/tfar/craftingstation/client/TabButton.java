package tfar.craftingstation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import tfar.craftingstation.CraftingStation;
import tfar.craftingstation.CraftingStationMenu;

public class TabButton extends Button{

  public final int index;
  public final ItemStack stack;
  public TabButton(int x, int y, int widthIn, int heightIn, Button.OnPress callback, int index, ItemStack stack) {
    super(x, y, widthIn, heightIn, Component.empty(), callback,DEFAULT_NARRATION);
    this.index = index;
    this.stack = stack;
  }
  public static final ResourceLocation TAB = new ResourceLocation(CraftingStation.MODID,"textures/gui/tabs.png");


  @Override
  public void renderButton(PoseStack matrices,int mouseX, int mouseY, float partialTicks) {
      RenderSystem.setShaderTexture(0,TAB);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      if (((CraftingStationMenu) ClientStuffs.mc.player.containerMenu).getCurrentContainer() == index) {
        blit(matrices, getX(), getY(), 0, height, width, height,width,height * 2);
      } else {
        blit(matrices, getX(), getY(), 0, 0, width, height,width,height * 2);
      }
      if (!stack.isEmpty()) {
        ClientStuffs.mc.getItemRenderer().renderAndDecorateFakeItem(stack, getX() +3, getY() +3);
      }
  }
}

