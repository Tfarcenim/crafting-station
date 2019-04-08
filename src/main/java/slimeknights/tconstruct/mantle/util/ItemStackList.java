package slimeknights.tconstruct.mantle.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ItemStackList extends NonNullList<ItemStack> {

  protected ItemStackList() {
    this(new ArrayList<>());
  }

  protected ItemStackList(List<ItemStack> delegate) {
    super(delegate, ItemStack.EMPTY);
  }

  public static ItemStackList create() {
    return new ItemStackList();
  }

  /**
   * Create an empty ItemStackList with the given size
   */
  public static ItemStackList withSize(int size) {
    ItemStack[] aobject = new ItemStack[size];
    Arrays.fill(aobject, ItemStack.EMPTY);
    return new ItemStackList(Arrays.asList(aobject));
  }

  /**
   * Create an ItemStackList from the given elements.
   */
  public static ItemStackList of(ItemStack... element) {
    ItemStackList itemStackList = create();
    itemStackList.addAll(Arrays.asList(element));
    return itemStackList;
  }

  /**
   * Create an ItemStackList from the given elements.
   */
  public static ItemStackList of(Collection<ItemStack> boringList) {
    ItemStackList itemStackList = create();
    itemStackList.addAll(boringList);
    return itemStackList;
  }

  /**
   * Create an ItemStackList from the given elements.
   */
  public static ItemStackList of(IInventory inventory) {
    ItemStackList itemStackList = withSize(inventory.getSizeInventory());
    for(int i = 0; i < inventory.getSizeInventory(); i++) {
      itemStackList.add(inventory.getStackInSlot(i));
    }
    return itemStackList;
  }

  /**
   * Sets the itemstack at the given index to Itemstack.EMPTY.
   * Does nothing if the index is out of bounds.
   *
   * @param index The index to set empty
   */
  public void setEmpty(int index) {
    if(index >= 0 && index < size()) {
      set(index, ItemStack.EMPTY);
    }
  }

}
