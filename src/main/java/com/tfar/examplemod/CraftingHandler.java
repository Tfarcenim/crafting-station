package com.tfar.examplemod;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

public class CraftingHandler extends ItemStackHandler {

  private final TileEntity craftiingStation;

  public CraftingHandler(CraftingStationTile craftingStation, int size){
    super(size);
    this.craftiingStation = craftingStation;
  }
}
