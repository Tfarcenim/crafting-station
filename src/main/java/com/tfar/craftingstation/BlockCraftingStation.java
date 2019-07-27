package com.tfar.craftingstation;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCraftingStation extends Block {
  public BlockCraftingStation(Material materialIn) {
    super(materialIn);
    GameRegistry.registerTileEntity(TileCraftingStation.class, new ResourceLocation(CraftingStation.MODID,CraftingStation.MODID));
  }

  @Override
  public boolean hasTileEntity(IBlockState state) {
    return true;
  }


  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side,
                                  float clickX, float clickY, float clickZ) {
    if (player.isSneaking()) {return false;}

    TileEntity te = world.getTileEntity(pos);

    if(!world.isRemote && te instanceof TileCraftingStation) {
      player.openGui(CraftingStation.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());

    }
    return true;
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
                              ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

    // set custom name from named stack
    if(stack.hasDisplayName()) {
      TileEntity tileentity = worldIn.getTileEntity(pos);

      if(tileentity instanceof TileCraftingStation) {
        ((TileCraftingStation) tileentity).setCustomName(stack.getDisplayName());
      }
    }
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
    return new TileCraftingStation();
  }

  @Override
  public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
    TileEntity te = worldIn.getTileEntity(pos);
    if (te instanceof TileCraftingStation)
      InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) te);
  }

}
