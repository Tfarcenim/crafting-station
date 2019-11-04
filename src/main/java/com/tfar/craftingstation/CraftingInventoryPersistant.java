package com.tfar.craftingstation;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.stream.IntStream;

/** pretends to be an InventoryCrafting while actually just wrapping an IItemHandler */
public class CraftingInventoryPersistant extends InventoryCrafting {

  private ItemStackHandler itemHandler;
  private Container eventHandler;
  private boolean doNotCallUpdates;

  CraftingInventoryPersistant(Container eventHandler, ItemStackHandler itemHandler) {
    super(eventHandler, 3, 3);

    this.eventHandler = eventHandler;
    this.itemHandler = itemHandler;
  }

  @Override
  public boolean isEmpty() {
    return IntStream.range(0, itemHandler.getSlots()).allMatch(i -> itemHandler.getStackInSlot(i).isEmpty());
  }

  public ItemStack getStackInSlot(int index) {
    return itemHandler.getStackInSlot(index);
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
    if (!doNotCallUpdates)eventHandler.onCraftMatrixChanged(this);
  }

  @Override
  public void clear() {
    for (int i = 0; i < itemHandler.getSlots(); i++) {
      itemHandler.setStackInSlot(i, ItemStack.EMPTY);
    }
  }

  /**
   * If set to true no eventhandler.onCraftMatrixChanged calls will be made.
   * This is used to prevent recipe check when changing the item slots when something is crafted
   * (since each slot with an item is reduced by 1, it changes -> callback)
   */
  public void setDoNotCallUpdates(boolean doNotCallUpdates) {
    this.doNotCallUpdates = doNotCallUpdates;
  }
}