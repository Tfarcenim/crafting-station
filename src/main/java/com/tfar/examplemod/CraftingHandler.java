package com.tfar.examplemod;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class CraftingHandler extends ItemStackHandler {

  private final TileEntity craftiingStation;

  public CraftingHandler(CraftingStationTile craftingStation, int size){
    super(size);
    this.craftiingStation = craftingStation;
  }
  public List<ItemStack> getContents(){
    return stacks;
  }
}
