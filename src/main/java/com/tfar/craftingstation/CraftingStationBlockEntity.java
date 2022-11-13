package com.tfar.craftingstation;

import com.tfar.craftingstation.util.CraftingStationItemHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CraftingStationBlockEntity extends BlockEntity implements MenuProvider {

  public CraftingStationItemHandler input;

  public int currentContainer = 0;

  public CraftingStationBlockEntity() {
    super(CraftingStation.Objects.crafting_station_tile);
    this.input = new CraftingStationItemHandler(9);
  }

  @Nonnull
  @Override
  public CompoundTag save(CompoundTag tag) {
    CompoundTag compound = this.input.serializeNBT();
    tag.put("inv", compound);
    // if (this.customName != null) {
    //   tag.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
    //  }
    return super.save(tag);
  }

  @Override
  public void load(BlockState state,CompoundTag tag) {
    CompoundTag invTag = tag.getCompound("inv");
    input.deserializeNBT(invTag);
    //  if (tag.contains("CustomName", 8)) {
    //    this.customName = ITextComponent.Serializer.fromJson(tag.getString("CustomName"));
    //   }
    super.load(state,tag);
  }

  @Nonnull
  @Override
  public Component getDisplayName() {
    return new TranslatableComponent("title.crafting_station");
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
    return new CraftingStationContainer(id, playerInventory, level, worldPosition);
  }

  @Nonnull
  @Override
  public CompoundTag getUpdateTag() {
    return save(new CompoundTag());    // okay to send entire inventory on chunk load
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return new ClientboundBlockEntityDataPacket(getBlockPos(), 1, getUpdateTag());
  }

  @Override
  public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
    this.load(null,packet.getTag());
  }
}

