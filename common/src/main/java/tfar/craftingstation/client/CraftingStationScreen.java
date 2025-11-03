package tfar.craftingstation.client;

import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.Direction;
import tfar.craftingstation.CraftingStation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import tfar.craftingstation.menu.CraftingStationMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import tfar.craftingstation.network.C2SScrollPacket;
import tfar.craftingstation.platform.Services;

public class CraftingStationScreen extends AbstractContainerScreen<CraftingStationMenu> {
    private static final ResourceLocation SCROLLBAR_BACKGROUND_AND_TAB = ResourceLocation.parse("textures/gui/container/creative_inventory/tab_items.png");

    public static final ResourceLocation SECONDARY_GUI_TEXTURE = CraftingStation.id("textures/gui/secondary.png");

    /**
     * Amount scrolled in inventory (0 = top, 1 = bottom)
     */
    private double currentScroll;

    private boolean isScrolling = false;

    public CraftingStationScreen(CraftingStationMenu p_i51094_1_, Inventory p_i51094_2_, Component p_i51094_3_) {
        super(p_i51094_1_, p_i51094_2_, p_i51094_3_);
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.hasSideContainers()) {
            for (int i = 0; i < Direction.values().length; i++) {
                Direction direction = Direction.values()[i];
                if (menu.blockEntityMap.containsKey(direction)) {
                    addRenderableWidget(new TabButton(leftPos - 128 + 21 * i, topPos - 22, 22, 28,
                            button -> {
                        menu.setCurrentContainer(direction);
                                sendButtonToServer(CraftingStationMenu.ButtonAction.values()[direction.ordinal() + 1]);
                            }, direction, this.getMenu()));
                }
            }
        }
        if (!Services.PLATFORM.isModLoaded("craftingtweaks")) {

            Tooltip tooltipC = Tooltip.create(Component.translatable("text.crafting_station.clear"));

            ClearButton clear = new ClearButton(leftPos + 85, topPos + 16, 7, 7, b -> sendButtonToServer(CraftingStationMenu.ButtonAction.CLEAR));
            clear.setTooltip(tooltipC);
            this.addRenderableWidget(clear);
        }
    }

    private void sendButtonToServer(CraftingStationMenu.ButtonAction action) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, action.ordinal());
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        boolean b = super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton) &&
                (!menu.hasSideContainers() || !isHovering(-126, -16, 126, 32 + imageHeight, mouseX, mouseY));
        return b;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics stack, int mouseX, int mouseY) {
        super.renderLabels(stack, mouseX, mouseY);
        if (menu.hasSideContainers()) {
            stack.drawString(font, getTruncatedString(), -122, 6, 0x404040, false);
        }
    }

    Component getTruncatedString() {
        Component c = menu.containerNames.getOrDefault(menu.getSelectedContainer(), Component.empty());
      //  if (font.width(c) > 23) {
          //  return string.substring(0, 23) + "...";
    //    }
        return c;
    }

    @Override
    protected void renderBg(GuiGraphics stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        stack.blit(CraftingScreen.CRAFTING_TABLE_LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int i = this.leftPos;

        int i1 = i - 16;
        int i2 = i1 + 14;

        int j = (this.height - this.imageHeight) / 2;
        if (this.menu.hasSideContainers()) {
            //draw background
            stack.blit(SECONDARY_GUI_TEXTURE, i - 130, j, 0, 0, this.imageWidth, this.imageHeight + 18);

            int totalSlots = menu.getCurrentHandler().$getSlotCount();
            int slotsToDraw = Math.min(totalSlots,CraftingStationMenu.MAX_SLOTS);

            int offset = hasScrollbar() ? -126 : -118;

            for (int i3 = 0; i3 < slotsToDraw; i3++) {
                int j1 = i3 % 6;
                int k1 = i3 / 6;
                stack.blit(SCROLLBAR_BACKGROUND_AND_TAB, i + j1 * 18 + offset, 18 * k1 + j + 16, 8, 17, 18, 18);
            }

            if (this.hasScrollbar()) {
                stack.blit(SCROLLBAR_BACKGROUND_AND_TAB, i - 17, j + 16, 174, 17, 14, 100);
                stack.blit(SCROLLBAR_BACKGROUND_AND_TAB, i - 17, j + 67, 174, 18, 14, 111);
                int k = (int) (j + 17 + 145 * currentScroll);

                stack.blitSprite(CreativeModeInventoryScreen.SCROLLER_SPRITE, i - 16, k, 12, 15);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int scroll) {
        this.isScrolling = this.hasScrollbar();
        return super.mouseClicked(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if (this.isScrolling) {
            int j = this.topPos;
            int j1 = j + 24;
            int j2 = j1 + 145;
            int k = this.leftPos;
            int k1 = k - 16;
            int k2 = k1 + 14;

            if (mouseX <= k2 && mouseX >= k1) {
                this.currentScroll = (mouseY - j1) / (j2 - j1 - 0f);
                currentScroll = Mth.clamp(currentScroll, 0, 1);
                scrollDrag(currentScroll);
            }
        }
        return super.mouseDragged(mouseX, mouseY, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int scroll) {
        this.isScrolling = false;
        return super.mouseReleased(mouseX, mouseY, scroll);
    }

    private boolean hasScrollbar() {
        return menu.needsScroll();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double pScrollX, double pScrollY) {

        if (this.hasScrollbar() && mouseX < leftPos && mouseX > leftPos - 20) {
            scrollMouse(pScrollY);
            return true;
        }
        return false;
    }



    private void scrollDrag(double scroll) {
        int firstSlot = (int) (scroll * (menu.subContainerSize() - CraftingStationMenu.MAX_SLOTS));//30 -> 81
        menu.setFirstSlot(firstSlot);
        Services.PLATFORM.sendToServer(new C2SScrollPacket(firstSlot));
    }

    private void scrollMouse(double scrollDelta) {
        int firstSlot = (int) Mth.clamp(menu.getFirstSlot() - scrollDelta * 6,0,menu.subContainerSize() -CraftingStationMenu.MAX_SLOTS);//30 -> 81
        menu.setFirstSlot(firstSlot);
        setScrollPos();
        Services.PLATFORM.sendToServer(new C2SScrollPacket(firstSlot));
    }

    void setScrollPos() {
        double scroll = ((double)menu.getFirstSlot()) /(menu.subContainerSize() - CraftingStationMenu.MAX_SLOTS);
        currentScroll = Mth.clamp(scroll,0,1);
    }
}

