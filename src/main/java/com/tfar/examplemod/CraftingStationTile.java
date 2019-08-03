package com.tfar.examplemod;

import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CraftingStationTile extends TileEntity implements IInteractionObject {

  public ItemStackHandler input;
  public ItemStackHandler output;

  public CraftingStationTile() {
    this.input = new CraftingHandler(this,9);
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

  /**
   * Gets the name of this thing. This method has slightly different behavior depending on the interface (for <a
   * href="https://github.com/ModCoderPack/MCPBot-Issues/issues/14">technical reasons</a> the same method is used for
   * both IWorldNameable and ICommandSender):
   *
   * <dl>
   * <dt>{@link net.minecraft.util.INameable#getName() INameable.getName()}</dt>
   * <dd>Returns the name of this inventory. If this {@linkplain net.minecraft.inventory#hasCustomName() has a custom
   * name} then this <em>should</em> be a direct string; otherwise it <em>should</em> be a valid translation
   * string.</dd>
   * <dd>However, note that <strong>the translation string may be invalid</strong>, as is the case for {@link
   * TileEntityBanner TileEntityBanner} (always returns nonexistent translation code
   * <code>banner</code> without a custom name), {@link BlockAnvil.Anvil BlockAnvil$Anvil} (always
   * returns <code>anvil</code>), {@link BlockWorkbench.InterfaceCraftingTable
   * BlockWorkbench$InterfaceCraftingTable} (always returns <code>crafting_table</code>), {@link
   * InventoryCraftResult InventoryCraftResult} (always returns <code>Result</code>) and the
   * {@link EntityMinecart EntityMinecart} family (uses the entity definition). This is not
   * an exaustive list.</dd>
   * <dd>In general, this method should be safe to use on tile entities that implement IInventory.</dd>
   * <dt>{@link ICommandSender#getName() ICommandSender.getName()} and {@link
   * Entity#getName() Entity.getName()}</dt>
   * <dd>Returns a valid, displayable name (which may be localized). For most entities, this is the translated version
   * of its translation string (obtained via {@link EntityList#getEntityString
   * EntityList.getEntityString}).</dd>
   * <dd>If this entity has a custom name set, this will return that name.</dd>
   * <dd>For some entities, this will attempt to translate a nonexistent translation string; see <a
   * href="https://bugs.mojang.com/browse/MC-68446">MC-68446</a>. For {@linkplain
   * EntityPlayer#getName() players} this returns the player's name. For {@linkplain
   * EntityOcelot ocelots} this may return the translation of
   * <code>entity.Cat.name</code> if it is tamed. For {@linkplain EntityItem#getName() item
   * entities}, this will attempt to return the name of the item in that item entity. In all cases other than players,
   * the custom name will overrule this.</dd>
   * <dd>For non-entity command senders, this will return some arbitrary name, such as "Rcon" or "Server".</dd>
   * </dl>
   */
  @Override
  public String getName() {
    return null;
  }

  /**
   * Checks if this thing has a custom name. This method has slightly different behavior depending on the interface
   * (for <a href="https://github.com/ModCoderPack/MCPBot-Issues/issues/14">technical reasons</a> the same method is
   * used for both IWorldNameable and Entity):
   *
   * <dl>
   * <dt>{@link net.minecraft.util.INameable#hasCustomName() INameable.hasCustomName()}</dt>
   * <dd>If true, then {@link #getName()} probably returns a preformatted name; otherwise, it probably returns a
   * translation string. However, exact behavior varies.</dd>
   * <dt>{@link Entity#hasCustomName() Entity.hasCustomName()}</dt>
   * <dd>If true, then {@link Entity#getCustomNameTag() Entity.getCustomNameTag()} will return a
   * non-empty string, which will be used by {@link #getName()}.</dd>
   * </dl>
   */
  @Override
  public boolean hasCustomName() {
    return false;
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
  public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
    return new CraftingStationContainer(playerInventory, world, pos,player);
  }

  @Override
  public String getGuiID() {
    return null;
  }

  public interface Listener {
    void tileEntityContentsChanged();
  }
}
