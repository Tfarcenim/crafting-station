package com.tfar.craftingstation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.IntStream;

import static com.tfar.craftingstation.CraftingStationBlock.FACING;
import static com.tfar.craftingstation.CraftingStationBlock.dropItems;

public class CraftingStationSlabBlock extends SlabBlock {


  public CraftingStationSlabBlock(Properties properties) {
    super(properties);
    this.stateContainer.getBaseState().with(FACING, Direction.NORTH);

  }

  @Override
  public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult p_225533_6_) {
    if (!world.isRemote) {
      INamedContainerProvider iNamedContainerProvider = getContainer(state,world,pos);
      if (iNamedContainerProvider != null) {
        NetworkHooks.openGui((ServerPlayerEntity) player, iNamedContainerProvider, pos);
      } else {
        throw new IllegalStateException("Our named container provider is missing!");
      }
    }
    return ActionResultType.SUCCESS;
  }

  @Override
  public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos) {
    TileEntity te = world.getTileEntity(pos);
    return te instanceof CraftingStationBlockEntity ? (INamedContainerProvider) te : null;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new CraftingStationBlockEntity();
  }

  @Override
  public void onReplaced(BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof CraftingStationBlockEntity) {
        dropItems(((CraftingStationBlockEntity) tileentity).input, worldIn, pos);
        worldIn.updateComparatorOutputLevel(pos, this);
      }
      super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
  }

  @Nonnull
  public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
    return p_185499_1_.with(FACING, p_185499_2_.rotate(p_185499_1_.get(FACING)));
  }

  @Nonnull
  public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
    return p_185471_1_.rotate(p_185471_2_.toRotation(p_185471_1_.get(FACING)));
  }

  protected void fillStateContainer(StateContainer.Builder<Block, BlockState> p_206840_1_) {
    super.fillStateContainer(p_206840_1_);
    p_206840_1_.add(FACING);
  }

  @Nullable
  public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
    return super.getStateForPlacement(p_196258_1_).with(FACING, p_196258_1_.getPlacementHorizontalFacing());
  }
}
