package com.tfar.craftingstation;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.IWorld;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.IntStream;

import static com.tfar.craftingstation.CraftingStationBlock.FACING;
import static com.tfar.craftingstation.CraftingStationBlock.dropItems;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public class CraftingStationSlabBlock extends SlabBlock {


  public CraftingStationSlabBlock(Properties properties) {
    super(properties);
    this.stateDefinition.any().setValue(FACING, Direction.NORTH);

  }

  @Override
  public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_225533_6_) {
    if (!world.isClientSide) {
      MenuProvider iNamedContainerProvider = getMenuProvider(state,world,pos);
      if (iNamedContainerProvider != null) {
        NetworkHooks.openGui((ServerPlayer) player, iNamedContainerProvider, pos);
      }
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
    BlockEntity te = world.getBlockEntity(pos);
    return te instanceof CraftingStationBlockEntity ? (MenuProvider) te : null;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
    return new CraftingStationBlockEntity();
  }

  @Override
  public void onRemove(BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      BlockEntity tileentity = worldIn.getBlockEntity(pos);
      if (tileentity instanceof CraftingStationBlockEntity) {
        dropItems(((CraftingStationBlockEntity) tileentity).input, worldIn, pos);
        worldIn.updateNeighbourForOutputSignal(pos, this);
      }
      super.onRemove(state, worldIn, pos, newState, isMoving);
    }
  }

  @Nonnull
  public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
    return p_185499_1_.setValue(FACING, p_185499_2_.rotate(p_185499_1_.getValue(FACING)));
  }

  @Nonnull
  public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
    return p_185471_1_.rotate(p_185471_2_.getRotation(p_185471_1_.getValue(FACING)));
  }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_206840_1_) {
    super.createBlockStateDefinition(p_206840_1_);
    p_206840_1_.add(FACING);
  }

  @Nullable
  public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
    return super.getStateForPlacement(p_196258_1_).setValue(FACING, p_196258_1_.getHorizontalDirection());
  }
}
