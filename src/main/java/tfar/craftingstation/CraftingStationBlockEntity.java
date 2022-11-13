package tfar.craftingstation;

import tfar.craftingstation.init.ModBlockEntityTypes;
import tfar.craftingstation.util.CraftingStationItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CraftingStationBlockEntity extends BlockEntity implements MenuProvider {

  public CraftingStationItemHandler input;

  public int currentContainer = 0;

  public CraftingStationBlockEntity(BlockPos pPos, BlockState pState) {
    super(ModBlockEntityTypes.crafting_station,pPos,pState);
    this.input = new CraftingStationItemHandler(9);
  }

  @Nonnull
  @Override
  public void saveAdditional(CompoundTag tag) {
    CompoundTag compound = this.input.serializeNBT();
    tag.put("inv", compound);
    // if (this.customName != null) {
    //   tag.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
    //  }
  }

  @Override
  public void load(CompoundTag tag) {
    CompoundTag invTag = tag.getCompound("inv");
    input.deserializeNBT(invTag);
    //  if (tag.contains("CustomName", 8)) {
    //    this.customName = ITextComponent.Serializer.fromJson(tag.getString("CustomName"));
    //   }
    super.load(tag);
  }

  @Nonnull
  @Override
  public Component getDisplayName() {
    return new TranslatableComponent("title.crafting_station");
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
    return new CraftingStationContainer(id, playerInventory, worldPosition);
  }

  @Nonnull
  @Override
  public CompoundTag getUpdateTag() {
    return saveWithoutMetadata();    // okay to send entire inventory on chunk load
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
}

