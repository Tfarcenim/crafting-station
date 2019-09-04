package com.tfar.craftingstation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

import static net.minecraft.inventory.InventoryHelper.spawnItemStack;

public class CraftingStationBlock extends Block {


  public CraftingStationBlock(Material materialIn) {
    super(materialIn);
  }

  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (world.isRemote) return true;
    player.addStat(StatList.CRAFTING_TABLE_INTERACTION);
    player.openGui(CraftingStation.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
    return true;
  }

  private static ImmutableList<AxisAlignedBB> BOUNDS_Table = ImmutableList.of(
          new AxisAlignedBB(0, 0.75, 0, 1, 1, 1),
          new AxisAlignedBB(0, 0, 0, 0.25, 0.75, 0.25),
          new AxisAlignedBB(0.75, 0, 0, 1, 0.75, 0.25),
          new AxisAlignedBB(0.75, 0, 0.75, 1, 0.75, 1),
          new AxisAlignedBB(0, 0, 0.75, 0.25, 0.75, 1)
  );

  @Override
  public RayTraceResult collisionRayTrace(IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
    // basically the same BlockStairs does
    // Raytrace through all AABBs (plate, legs) and return the nearest one
    return raytraceMultiAABB(BOUNDS_Table, pos, start, end);
  }

  public static RayTraceResult raytraceMultiAABB(List<AxisAlignedBB> aabbs, BlockPos pos, Vec3d start, Vec3d end) {
    List<RayTraceResult> list = Lists.newArrayList();

    for(AxisAlignedBB axisalignedbb : aabbs) {
      list.add(rayTrace2(pos, start, end, axisalignedbb));
    }

    RayTraceResult raytraceresult1 = null;
    double d1 = 0.0D;

    for(RayTraceResult raytraceresult : list) {
      if(raytraceresult != null) {
        double d0 = raytraceresult.hitVec.squareDistanceTo(end);

        if(d0 > d1) {
          raytraceresult1 = raytraceresult;
          d1 = d0;
        }
      }
    }

    return raytraceresult1;
  }

  // Block.raytrace
  private static RayTraceResult rayTrace2(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox) {
    Vec3d vec3d = start.subtract((double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
    Vec3d vec3d1 = end.subtract((double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
    RayTraceResult raytraceresult = boundingBox.calculateIntercept(vec3d, vec3d1);
    return raytraceresult == null ? null : new RayTraceResult(raytraceresult.hitVec.add((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()), raytraceresult.sideHit, pos);
  }

  @Override
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  @Override
  public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
    return false;
  }

  @Override
  public boolean hasTileEntity(IBlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(World world, IBlockState state) {
    return new CraftingStationTile();
  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof CraftingStationTile) {
        dropItems((CraftingStationTile)tileentity,worldIn, pos);
        worldIn.updateComparatorOutputLevel(pos, this);
      }
      super.breakBlock(worldIn, pos, state);
    }

  public static void dropItems(CraftingStationTile table, World world, BlockPos pos) {
    IntStream.range(0, table.input.getSlots()).mapToObj(i -> table.input.getStackInSlot(i)).filter(stack -> !stack.isEmpty()).forEach(stack -> spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack));
  }
}

