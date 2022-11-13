package com.tfar.craftingstation;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.IntStream;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public class CraftingStationBlock extends Block implements SimpleWaterloggedBlock {

  public static final VoxelShape shape;

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;


  public CraftingStationBlock(Properties p_i48440_1_) {
    super(p_i48440_1_);
    this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false);
  }

  static {
    VoxelShape[] shapes = new VoxelShape[5];

    shapes[0] = Block.box(0, 12, 0, 16, 16, 16);
    shapes[1] = Block.box(0, 0, 0, 4, 12, 4);
    shapes[2] = Block.box(12, 0, 0, 16, 12, 4);
    shapes[3] = Block.box(0, 0, 12, 4, 12, 16);
    shapes[4] = Block.box(12, 0, 12, 16, 12, 16);

    shape = Shapes.or(shapes[0], shapes[1], shapes[2], shapes[3], shapes[4]);
  }

  @Override
  public InteractionResult use(BlockState p_225533_1_, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_225533_6_) {
    if (!world.isClientSide) {
      BlockEntity tileEntity = world.getBlockEntity(pos);
      if (tileEntity instanceof MenuProvider) {
        NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) tileEntity, tileEntity.getBlockPos());
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

  public static void dropItems(IItemHandler inv, Level world, BlockPos pos) {
    IntStream.range(0, inv.getSlots()).mapToObj(inv::getStackInSlot).filter(s -> !s.isEmpty()).forEach(stack -> Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack));
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return shape;
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
    p_206840_1_.add(WATERLOGGED,FACING);
  }

  @Nonnull
  public FluidState getFluidState(BlockState p_204507_1_) {
    return p_204507_1_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_204507_1_);
  }

  @Nullable
  public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
    LevelAccessor lvt_2_1_ = p_196258_1_.getLevel();
    BlockPos lvt_3_1_ = p_196258_1_.getClickedPos();
    boolean lvt_4_1_ = lvt_2_1_.getFluidState(lvt_3_1_).getType() == Fluids.WATER;
    return this.defaultBlockState().setValue(FACING, p_196258_1_.getHorizontalDirection()).setValue(WATERLOGGED, lvt_4_1_);
  }
}
