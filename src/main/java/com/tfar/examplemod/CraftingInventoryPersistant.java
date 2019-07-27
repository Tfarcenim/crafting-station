package com.tfar.examplemod;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/** pretends to be an InventoryCrafting while actually just wrapping an IItemHandler */
public class CraftingInventoryPersistant extends CraftingInventory {
  ItemStackHandler itemHandler;
  Container eventHandler;

  CraftingInventoryPersistant(Container eventHandler, ItemStackHandler itemHandler) {
    super(eventHandler, 3, 3);

    assert itemHandler.getSlots() == 3 * 3;

    this.eventHandler = eventHandler;
    this.itemHandler = itemHandler;
  }

  @Override
  public boolean isEmpty() {
    for (int i = 0; i < itemHandler.getSlots(); i++) {
      if (!itemHandler.getStackInSlot(i).isEmpty())
        return false;
    }
    return true;
  }

  public ItemStack getStackInSlot(int index) {
    return index < itemHandler.getSlots() ? itemHandler.getStackInSlot(index) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    return itemHandler.extractItem(index, 64, false);
  }

  @Override
  public ItemStack decrStackSize(int index, int count) {
    ItemStack removed = itemHandler.extractItem(index, count, false);

    if (!removed.isEmpty()) {
      eventHandler.onCraftMatrixChanged(this);
    }

    return removed;
  }

  @Override
  public void setInventorySlotContents(int index, ItemStack stack) {
    itemHandler.setStackInSlot(index, stack);
    eventHandler.onCraftMatrixChanged(this);
  }

  @Override
  public void clear() {
    for (int i = 0; i < itemHandler.getSlots(); i++) {
      itemHandler.setStackInSlot(i, ItemStack.EMPTY);
    }
  }
}