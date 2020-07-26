package com.tfar.craftingstation;

import com.tfar.craftingstation.util.CraftingStationItemHandler;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.IntStream;

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
   * Returns the stack in this slot.  This stack should be a modifiable reference, not a copy of a stack in your inventory.
   */
  @Nonnull
  @Override
  public ItemStack getStackInSlot(int slot) {
    validate(slot);
    return inv.getStackInSlot(slot);
  }

  public void validate(int slot){
    if (isValid(slot))return;
    throw new IndexOutOfBoundsException("Someone attempted to poll an outofbounds stack at slot " +
            slot+" report to them, NOT Crafting Station");
  }

  public boolean isValid(int slot){
    return slot >= 0 && slot < getSizeInventory();
  }

  /**
   * Attempts to remove n items from the specified slot.  Returns the split stack that was removed.  Modifies the inventory.
   */
  @Nonnull
  @Override
  public ItemStack decrStackSize(int slot, int count) {
    validate(slot);
    ItemStack stack = inv.extractItem(slot,count,false);
    if (!stack.isEmpty())
      onCraftMatrixChanged();
    return stack;
  }

  /**
   * Sets the contents of this slot to the provided stack.
   */
  @Override
  public void setInventorySlotContents(int slot,@Nonnull ItemStack stack) {
    validate(slot);
    inv.setStackInSlot(slot, stack);
    onCraftMatrixChanged();
  }

  /**
   * Removes the stack contained in this slot from the underlying handler, and returns it.
   */
  @Nonnull
  @Override
  public ItemStack removeStackFromSlot(int index) {
    validate(index);
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
  public boolean isEmpty() {
    return IntStream.range(0, inv.getSlots()).allMatch(i -> inv.getStackInSlot(i).isEmpty());
  }

  @Override
  public void clear(){
    //dont
  }

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
      this.eventHandler.onCraftMatrixChanged(this);
    }
  }
}