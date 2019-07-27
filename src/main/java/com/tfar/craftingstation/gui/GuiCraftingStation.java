package com.tfar.craftingstation.gui;

import com.tfar.craftingstation.ContainerCraftingStation;
import com.tfar.craftingstation.ContainerSideInventory;
import com.tfar.craftingstation.TileCraftingStation;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCraftingStation extends GuiContainer {

  private static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/container/crafting_table.png");
  protected final TileCraftingStation tile;

  public GuiCraftingStation(InventoryPlayer playerInv, World world, BlockPos pos, TileCraftingStation tile) {
    super(tile.createContainer(playerInv, world, pos));

    this.tile = tile;

    if(inventorySlots instanceof ContainerCraftingStation) {
      ContainerCraftingStation container = (ContainerCraftingStation) inventorySlots;
      ContainerSideInventory chestContainer = container.getSubContainer(ContainerSideInventory.class);
      if(chestContainer != null) {
        if(chestContainer.getTile() instanceof TileEntityChest) {
          // Fix: chests don't update their single/double chest status clientside once accessed
          ((TileEntityChest) chestContainer.getTile()).doubleChestHandler = null;
        }
        this.addModule(new GuiSideInventory(this, chestContainer, chestContainer.getSlotCount(), chestContainer.columns));
      }
    }
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    drawBackground(BACKGROUND);

    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
  }
}
