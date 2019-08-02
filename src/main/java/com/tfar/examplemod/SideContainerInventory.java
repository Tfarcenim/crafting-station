package com.tfar.examplemod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;


public class SideContainerInventory extends Container {

  public final int columns;
  public final int slotCount;
  public IItemHandler itemHandler;

  public SideContainerInventory(int id, TileEntity tile, Direction dir, int x, int y, int columns) {
    super(CraftingStation.Objects.side_container,id);
    this.columns = columns;
    tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(
            (iItemHandler) -> itemHandler = iItemHandler);

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
        this.addSlot(createSlot(itemHandler, index, x + c * 18, y + r * 18));
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
  public boolean canInteractWith(PlayerEntity playerIn) {
    return true;
  }

  protected Slot createSlot(IItemHandler itemHandler, int index, int x, int y) {
    return new SlotItemHandler(itemHandler, index, x, y);
  }

  public int getSlotCount() {
    return slotCount;
  }

}