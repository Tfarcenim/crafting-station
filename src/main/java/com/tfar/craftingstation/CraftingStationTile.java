package com.tfar.craftingstation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CraftingStationTile extends TileEntity {

  public ItemStackHandler input;
  public ItemStackHandler output;

  public CraftingStationTile() {
    this.input = new ItemStackHandler(9);
    this.output = new ItemStackHandler();
  }

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound tag) {
    NBTTagCompound compound = this.input.serializeNBT();
    tag.setTag("inv", compound);
   // if (this.customName != null) {
   //   tag.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
  //  }
    return super.writeToNBT(tag);
  }

  @Override
  public void readFromNBT(NBTTagCompound tag) {
    NBTTagCompound invTag = tag.getCompoundTag("inv");
    input.deserializeNBT(invTag);
  //  if (tag.contains("CustomName", 8)) {
  //    this.customName = ITextComponent.Serializer.fromJson(tag.getString("CustomName"));
 //   }
    super.readFromNBT(tag);
  }

  @Nonnull
  @Override
  public ITextComponent getDisplayName() {
    return new TextComponentTranslation("title.crafting_station");
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

  @Override
  public NBTTagCompound getUpdateTag()
  {
    return this.writeToNBT(new NBTTagCompound());    // okay to send entire inventory on chunk load
  }

  @Override
  public SPacketUpdateTileEntity getUpdatePacket()
  {
    NBTTagCompound nbt = new NBTTagCompound();
    this.writeToNBT(nbt);

    return new SPacketUpdateTileEntity(getPos(), 1, nbt);
  }

  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
  {
    this.readFromNBT(packet.getNbtCompound());
  }

  public interface Listener {
    void tileEntityContentsChanged();
  }
}
