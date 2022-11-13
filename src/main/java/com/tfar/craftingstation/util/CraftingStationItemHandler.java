package com.tfar.craftingstation.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

public class CraftingStationItemHandler extends ItemStackHandler {
  public CraftingStationItemHandler(int size){
    super(size);
  }

  public NonNullList<ItemStack> getContents(){
    return stacks;
  }

  public boolean isEmpty(){
    return getContents().stream().allMatch(ItemStack::isEmpty);
  }
}
