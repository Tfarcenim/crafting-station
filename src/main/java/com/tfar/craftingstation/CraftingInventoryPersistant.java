package com.tfar.craftingstation;

import com.tfar.craftingstation.util.CraftingStationItemHandler;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

/** pretends to be an InventoryCrafting while actually just wrapping an IItemHandler */
public class CraftingInventoryPersistant extends CraftingInventory {

  private boolean doNotCallUpdates;
  protected final CraftingStationItemHandler inv;

  public CraftingInventoryPersistant(Container eventHandler, CraftingStationItemHandler itemHandler) {
    super(eventHandler, 3, 3);
    this.inv = itemHandler;
    doNotCallUpdates = false;
  }

  /**
   * Returns the size of this inventory.  Must be equivalent to {@link #getHeight()} * {@link #getWidth()}.
   */
  @Override
  public int getSizeInventory()
  {
    return inv.getSlots();
  }

  /**
   * Returns the stack in this slot.  This stack should be a modifiable reference, not a copy of a stack in your inventory.
   */
  @Override
  public ItemStack getStackInSlot(int slot)
  {
    return inv.getStackInSlot(slot);
  }

  /**
   * Attempts to remove n items from the specified slot.  Returns the split stack that was removed.  Modifies the inventory.
   */
  @Override
  public ItemStack decrStackSize(int slot, int count) {
    ItemStack stack = inv.extractItem(slot,count,false);
    if (!stack.isEmpty())
      onCraftMatrixChanged();
    return stack;
  }

  /**
   * Sets the contents of this slot to the provided stack.
   */
  @Override
  public void setInventorySlotContents(int slot, ItemStack stack) {
    inv.setStackInSlot(slot, stack);
    onCraftMatrixChanged();
  }

  /**
   * Removes the stack contained in this slot from the underlying handler, and returns it.
   */
  @Override
  public ItemStack removeStackFromSlot(int index) {
    ItemStack s = getStackInSlot(index);
    if(s.isEmpty()) return ItemStack.EMPTY;
    onCraftMatrixChanged();
    setInventorySlotContents(index, ItemStack.EMPTY);
    return s;
  }

  public NonNullList<ItemStack> getStackList(){
    return inv.getContents();
  }

  @Override
  public boolean isEmpty()
  {
    for(int i = 0; i < inv.getSlots(); i++)
    {
      if(!inv.getStackInSlot(i).isEmpty()) return false;
    }
    return true;
  }

  //dont
  @Override
  public void clear(){}

  /**
   * If set to true no eventhandler.onCraftMatrixChanged calls will be made.
   * This is used to prevent recipe check when changing the item slots when something is crafted
   * (since each slot with an item is reduced by 1, it changes -> callback)
   */
  public void setDoNotCallUpdates(boolean doNotCallUpdates) {
    this.doNotCallUpdates = doNotCallUpdates;
  }

  public void onCraftMatrixChanged() {
    if(!doNotCallUpdates) {
      this.field_70465_c.onCraftMatrixChanged(this);
    }
  }
}