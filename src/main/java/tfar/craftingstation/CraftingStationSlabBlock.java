package tfar.craftingstation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CraftingStationSlabBlock extends SlabBlock implements EntityBlock {


  public CraftingStationSlabBlock(Properties properties) {
    super(properties);
    this.stateDefinition.any().setValue(CraftingStationBlock.FACING, Direction.NORTH);

  }

  @Override
  public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_225533_6_) {
    if (!world.isClientSide) {
      MenuProvider iNamedContainerProvider = getMenuProvider(state,world,pos);
      if (iNamedContainerProvider != null) {
        NetworkHooks.openScreen((ServerPlayer) player, iNamedContainerProvider, pos);
      }
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
    BlockEntity te = world.getBlockEntity(pos);
    return te instanceof CraftingStationBlockEntity ? (MenuProvider) te : null;
  }

  @org.jetbrains.annotations.Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return new CraftingStationBlockEntity(pPos,pState);
  }

  @Override
  public void onRemove(BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      BlockEntity tileentity = worldIn.getBlockEntity(pos);
      if (tileentity instanceof CraftingStationBlockEntity) {
        CraftingStationBlock.dropItems(((CraftingStationBlockEntity) tileentity).input, worldIn, pos);
        worldIn.updateNeighbourForOutputSignal(pos, this);
      }
      super.onRemove(state, worldIn, pos, newState, isMoving);
    }
  }

  @Nonnull
  public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
    return p_185499_1_.setValue(CraftingStationBlock.FACING, p_185499_2_.rotate(p_185499_1_.getValue(CraftingStationBlock.FACING)));
  }

  @Nonnull
  public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
    return p_185471_1_.rotate(p_185471_2_.getRotation(p_185471_1_.getValue(CraftingStationBlock.FACING)));
  }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_206840_1_) {
    super.createBlockStateDefinition(p_206840_1_);
    p_206840_1_.add(CraftingStationBlock.FACING);
  }

  @Nullable
  public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
    return super.getStateForPlacement(p_196258_1_).setValue(CraftingStationBlock.FACING, p_196258_1_.getHorizontalDirection());
  }
}
