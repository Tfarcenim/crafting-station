package com.tfar.craftingstation;

import com.tfar.craftingstation.gui.GuiCraftingStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CraftingStationGuiHandler implements IGuiHandler {
  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return ID == 0 ? new ContainerCraftingStation(player, world, (TileCraftingStation) world.getTileEntity(new BlockPos(x, y, z))) : null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return ID == 0 ? new GuiCraftingStation(player, world, (TileCraftingStation) world.getTileEntity(new BlockPos(x, y, z))) : null;
  }
}
