package com.tfar.craftingstation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.IntStream;

import net.minecraft.block.AbstractBlock.Properties;

public class CraftingStationBlock extends Block implements IWaterLoggable {

  public static final VoxelShape shape;

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  public static final DirectionProperty FACING = HorizontalBlock.FACING;


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

    shape = VoxelShapes.or(shapes[0], shapes[1], shapes[2], shapes[3], shapes[4]);
  }

  @Override
  public ActionResultType use(BlockState p_225533_1_, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult p_225533_6_) {
    if (!world.isClientSide) {
      TileEntity tileEntity = world.getBlockEntity(pos);
      if (tileEntity instanceof INamedContainerProvider) {
        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getBlockPos());
      }
    }
    return ActionResultType.SUCCESS;
  }

  @Override
  public INamedContainerProvider getMenuProvider(BlockState state, World world, BlockPos pos) {
    TileEntity te = world.getBlockEntity(pos);
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
  public void onRemove(BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      TileEntity tileentity = worldIn.getBlockEntity(pos);
      if (tileentity instanceof CraftingStationBlockEntity) {
        dropItems(((CraftingStationBlockEntity) tileentity).input, worldIn, pos);
        worldIn.updateNeighbourForOutputSignal(pos, this);
      }
      super.onRemove(state, worldIn, pos, newState, isMoving);
    }
  }

  public static void dropItems(IItemHandler inv, World world, BlockPos pos) {
    IntStream.range(0, inv.getSlots()).mapToObj(inv::getStackInSlot).filter(s -> !s.isEmpty()).forEach(stack -> InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack));
  }

  @Override
  public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
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

  protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> p_206840_1_) {
    p_206840_1_.add(WATERLOGGED,FACING);
  }

  @Nonnull
  public FluidState getFluidState(BlockState p_204507_1_) {
    return p_204507_1_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_204507_1_);
  }

  @Nullable
  public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
    IWorld lvt_2_1_ = p_196258_1_.getLevel();
    BlockPos lvt_3_1_ = p_196258_1_.getClickedPos();
    boolean lvt_4_1_ = lvt_2_1_.getFluidState(lvt_3_1_).getType() == Fluids.WATER;
    return this.defaultBlockState().setValue(FACING, p_196258_1_.getHorizontalDirection()).setValue(WATERLOGGED, lvt_4_1_);
  }
}
