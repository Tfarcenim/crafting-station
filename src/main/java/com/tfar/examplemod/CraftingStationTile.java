package com.tfar.examplemod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CraftingStationTile extends TileEntity implements INamedContainerProvider {

  public ItemStackHandler input;
  public ItemStackHandler output;

  public CraftingStationTile() {
    super(CraftingStation.Objects.crafting_station_tile);
    this.input = new CraftingHandler(this,9);
    this.output = new ItemStackHandler();
  }

  @Nonnull
  @Override
  public CompoundNBT write(CompoundNBT tag) {
    CompoundNBT compound = this.input.serializeNBT();
    tag.put("inv", compound);
   // if (this.customName != null) {
   //   tag.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
  //  }
    return super.write(tag);
  }

  @Override
  public void read(CompoundNBT tag) {
    CompoundNBT invTag = tag.getCompound("inv");
    input.deserializeNBT(invTag);
  //  if (tag.contains("CustomName", 8)) {
  //    this.customName = ITextComponent.Serializer.fromJson(tag.getString("CustomName"));
 //   }
    super.read(tag);
  }

  @Nonnull
  @Override
  public ITextComponent getDisplayName() {
    return new TranslationTextComponent("title.craftingstation");
  }

  @Nullable
  @Override
  public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) {
    return new CraftingStationContainer(id, playerInventory, world, pos,player);
  }
  private List<Listener> listeners = new ArrayList<>();

  protected ItemStackHandler newItemHandler() {
    return new ItemStackHandler(9) {
      @Override
      protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);

        contentsChanged();
      }

      @Override
      protected void onLoad() {
        super.onLoad();

        contentsChanged();
      }
    };
  }

  public void dropContents() {
    for (int i = 0; i < input.getSlots(); i++) {
      InventoryHelper.spawnItemStack(
              world,
              (double) pos.getX(),
              (double) pos.getY(),
              (double) pos.getZ(),
              input.getStackInSlot(i)
      );
    }
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  public void contentsChanged() {
    if (world == null)
      return; // not loaded yet
    markDirty();
    listeners.forEach(Listener::tileEntityContentsChanged);
  }

  public interface Listener {
    void tileEntityContentsChanged();
  }
}
