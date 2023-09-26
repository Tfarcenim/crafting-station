package tfar.craftingstation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import tfar.craftingstation.CraftingStation;
import tfar.craftingstation.CraftingStationContainer;

public class TabButton extends Button {

    public final int index;
    public final ItemStack stack;
    private final CraftingStationScreen craftingStationScreen;

    public TabButton(int x, int y, int widthIn, int heightIn, Button.OnPress callback, OnTooltip tooltip, int index, ItemStack stack, CraftingStationScreen craftingStationScreen) {
        super(x, y, widthIn, heightIn, Component.empty(), callback, tooltip);
        this.index = index;
        this.stack = stack;
        this.craftingStationScreen = craftingStationScreen;
    }

    public static final ResourceLocation TAB = new ResourceLocation(CraftingStation.MODID, "textures/gui/tabs.png");


    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, TAB);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        if (craftingStationScreen.getMenu().getCurrentContainer() == index) {
            blit(matrices, x, y, 0, height, width, height, width, height * 2);
        } else {
            blit(matrices, x, y, 0, 0, width, height, width, height * 2);
        }
        if (!stack.isEmpty()) {
            ClientStuffs.mc.getItemRenderer().renderAndDecorateFakeItem(stack, x + 3, y + 3);
        }
        if (isHoveredOrFocused()) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }
}

