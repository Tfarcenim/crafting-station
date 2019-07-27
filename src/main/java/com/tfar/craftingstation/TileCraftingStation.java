package com.tfar.craftingstation;

import com.tfar.craftingstation.gui.GuiCraftingStation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;



public class TileCraftingStation extends TileEntity implements IInventory {

  public TileCraftingStation() {
    super("gui.craftingstation.name", 9);
    this.itemHandler = new CraftingStationItemHandler(this, true, false);
  }

  @Override
  public Container createContainer(InventoryPlayer inventoryplayer, World world, BlockPos pos) {
    return new ContainerCraftingStation(inventoryplayer, this);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public GuiContainer createGui(InventoryPlayer inventoryplayer, World world, BlockPos pos) {
    return new GuiCraftingStation(inventoryplayer, world, pos, this);
  }

  @Override
  protected IExtendedBlockState setInventoryDisplay(IExtendedBlockState state) {
    PropertyTableItem.TableItems toDisplay = new PropertyTableItem.TableItems();

    float s = 0.125f;
    float o = 3f / 16f; // we want to move it 3 pixel in a 16 width texture
    for(int i = 0; i < 9; i++) {
      ItemStack itemStack = getStackInSlot(i);
      PropertyTableItem.TableItem item = getTableItem(itemStack, this.getWorld(), null);
      if(item != null) {
        item.x = +o - (i % 3) * o;
        item.z = +o - (i / 3) * o;
        item.y = -0.5f + s / 32f; // half itemmodel height + move it down to the bottom from the center
        //item.s *= 0.46875f;
        item.s = s;

        // correct itemblock because scaling
        if(itemStack.getItem() instanceof ItemBlock && !(Block.getBlockFromItem(itemStack.getItem())  instanceof BlockPane)) {
          item.y = -(1f - item.s) / 2f;
        }

        //item.s *= 2/5f;
        toDisplay.items.add(item);
      }
    }

    // add inventory if needed
    return state.withProperty(BlockTable.INVENTORY, toDisplay);
  }

  /**
   * Returns the number of slots in the inventory.
   */
  @Override
  public int getSizeInventory() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  /**
   * Returns the stack in the given slot.
   *
   * @param index
   */
  @Override
  public ItemStack getStackInSlot(int index) {
    return null;
  }

  /**
   * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
   *
   * @param index
   * @param count
   */
  @Override
  public ItemStack decrStackSize(int index, int count) {
    return null;
  }

  /**
   * Removes a stack from the given slot and returns it.
   *
   * @param index
   */
  @Override
  public ItemStack removeStackFromSlot(int index) {
    return null;
  }

  /**
   * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
   *
   * @param index
   * @param stack
   */
  @Override
  public void setInventorySlotContents(int index, ItemStack stack) {

  }

  /**
   * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
   */
  @Override
  public int getInventoryStackLimit() {
    return 0;
  }

  /**
   * Don't rename this method to canInteractWith due to conflicts with Container
   *
   * @param player
   */
  @Override
  public boolean isUsableByPlayer(EntityPlayer player) {
    return false;
  }

  @Override
  public void openInventory(EntityPlayer player) {

  }

  @Override
  public void closeInventory(EntityPlayer player) {

  }

  /**
   * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
   * guis use Slot.isItemValid
   *
   * @param index
   * @param stack
   */
  @Override
  public boolean isItemValidForSlot(int index, ItemStack stack) {
    return false;
  }

  @Override
  public int getField(int id) {
    return 0;
  }

  @Override
  public void setField(int id, int value) {

  }

  @Override
  public int getFieldCount() {
    return 0;
  }

  @Override
  public void clear() {

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
}
