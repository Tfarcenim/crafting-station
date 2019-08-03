package com.tfar.examplemod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;


public class SideContainerInventory extends Container {

  public final int columns;
  public final int slotCount;
  public IItemHandler itemHandler;

  public SideContainerInventory(TileEntity tile, EnumFacing dir, int x, int y, int columns) {
    this.columns = columns;
    if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,dir))
      itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
    this.slotCount = itemHandler.getSlots();
    int rows = slotCount / columns;
    if(slotCount % columns != 0) {
      rows++;
    }

    int index = 0;
    for(int r = 0; r < rows; r++) {
      for(int c = 0; c < columns; c++) {
        if(index >= slotCount) {
          break;
        }
        this.addSlotToContainer(createSlot(itemHandler, index, x + c * 18, y + r * 18));
        index++;
      }
    }
  }
  /**
   * Determines whether supplied player can use this container
   *
   * @param playerIn
   */
  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    return true;
  }

  protected Slot createSlot(IItemHandler itemHandler, int index, int x, int y) {
    return new SlotItemHandler(itemHandler, index, x, y);
  }

  public int getSlotCount() {
    return slotCount;
  }

}