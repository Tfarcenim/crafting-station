package com.tfar.craftingstation;

import com.tfar.craftingstation.util.Helpers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class CraftingStationBlock extends Block {

  protected static final VoxelShape[] shapes;
  protected static final VoxelShape shape;

  public CraftingStationBlock(Properties p_i48440_1_) {
    super(p_i48440_1_);
  }

static   {
    shapes = new VoxelShape[5];

    shapes[0] = Helpers.createVoxelShape(0,12,0,16,16,16);
    shapes[1] = Helpers.createVoxelShape(0, 0, 0, 4, 12, 4);
    shapes[2] = Helpers.createVoxelShape(12, 0, 0, 16, 12, 4);
    shapes[3] = Helpers.createVoxelShape(0, 0, 12, 4, 12, 16);
    shapes[4] = Helpers.createVoxelShape(12, 0, 12, 16, 12, 16);

    shape = VoxelShapes.or(shapes[0],shapes[1],shapes[2],shapes[3],shapes[4]);
  }

  @Override
  public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
    if (!world.isRemote) {
      TileEntity tileEntity = world.getTileEntity(pos);
      if (tileEntity instanceof INamedContainerProvider) {
        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getPos());
      } else {
        throw new IllegalStateException("Our named container provider is missing!");
      }
    }
    return true;
  }

  @Override
  public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos) {
    TileEntity te = world.getTileEntity(pos);
    return te instanceof CraftingStationTile ? (INamedContainerProvider) te : null;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new CraftingStationTile();
  }

  @Override
  public void onReplaced(BlockState p_196243_1_, World p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_, boolean p_196243_5_) {
    super.onReplaced(p_196243_1_, p_196243_2_, p_196243_3_, p_196243_4_, p_196243_5_);
  }

  @Override
  public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos)
  {
    return state.getShape(worldIn, pos);
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
  {
    return this.getShape(state, worldIn, pos, context);
  }

  @Override
  public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
  {
    return shape;
  }
}
