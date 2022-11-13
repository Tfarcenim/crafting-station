package tfar.craftingstation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import tfar.craftingstation.CraftingStation;
import tfar.craftingstation.CraftingStationContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;

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
      RenderSystem.setShaderTexture(0,TAB);


      isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(770, 771, 1, 0);
      RenderSystem.blendFunc(770, 771);
      if (((CraftingStationContainer) ClientStuffs.mc.player.containerMenu).currentContainer == index)
      blit(matrices,x, y, 0, height, width, height,width,height * 2);

      else      blit(matrices,x, y, 0, 0, width, height,width,height * 2);
      if (!stack.isEmpty()) {
        // String s1 = s.getUnformattedComponentText();
        //String slot = String.valueOf(Utils.getSelectedSlot(bag));

        Integer color = stack.getItem().getRarity(stack).color.getColor();

        int c = color != null ? color : 0xFFFFFF;
        final int itemX = x + 3;
        final int itemY = y + 3;
        renderHotbarItem(matrices,itemX,itemY,partialTicks,minecraft.player,stack);

      }
    }
  }


  private static void renderHotbarItem(PoseStack matrices, int x, int y, float partialTicks, Player player, ItemStack stack) {
    ClientStuffs.mc.getItemRenderer().renderGuiItemDecorations(ClientStuffs.mc.font, stack, x, y);
  }
}

