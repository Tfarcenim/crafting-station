package com.tfar.craftingstation;

import com.tfar.craftingstation.network.LastRecipePacket;
import com.tfar.craftingstation.network.PacketHandler;
import com.tfar.craftingstation.slot.SlotFastCraft;
import com.tfar.craftingstation.slot.WrapperSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class CraftingStationContainer extends Container implements CraftingStationTile.Listener {
  public final CraftingInventoryPersistant craftMatrix;
  public final InventoryCraftResult craftResult = new InventoryCraftResult();
  private static final int SLOT_RESULT = 0;
  public final World world;
  private final BlockPos pos;
  private final EntityPlayer player;
  private final CraftingStationTile tileEntity;
  public IRecipe lastRecipe;
  protected IRecipe lastLastRecipe;
  public boolean hasSideContainer;

  public ITextComponent containerName;

  public int subContainerSlotStart = 10;
  public int subContainerSlotEnd = 46;


  public CraftingStationContainer(InventoryPlayer InventoryPlayer, World world, BlockPos pos, EntityPlayer player) {
    this.world = world;
    this.pos = pos;
    this.player = player;
    this.tileEntity = (CraftingStationTile) world.getTileEntity(pos);
    this.craftMatrix = new CraftingInventoryPersistant(this, tileEntity.input);
    this.hasSideContainer = false;

    addOwnSlots();

    // detect te
    TileEntity inventoryTE = null;
    EnumFacing accessDir = null;
    for(EnumFacing dir : EnumFacing.values()) {
      BlockPos neighbor = pos.offset(dir);

      TileEntity te = world.getTileEntity(neighbor);
      if(te != null && !(te instanceof CraftingStationTile)) {
        // if blacklisted, skip checks entirely
    //    if(blacklisted(te.getClass())) {
      //    continue;
    //    }
//        if(te instanceof IInventory && !((IInventory) te).isUsableByPlayer(player)) {
 //         continue;
  //      }

        // try internal access first
        if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,null)) {
          IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
          inventoryTE = te;
        }
        // try sided access else
        else if(te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite())) {
          if(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()) instanceof IItemHandlerModifiable) {
            inventoryTE = te;
            accessDir = dir.getOpposite();
            break;
          }
        }
      }
    }

    if(inventoryTE != null) {
      this.hasSideContainer = true;
      addSideContainerSlots(inventoryTE, accessDir, -3 - 18 * 6, 18);
      containerName = inventoryTE instanceof IInteractionObject ? ((IInteractionObject) inventoryTE).getDisplayName() : InventoryPlayer.getDisplayName();
    //  scrollTo(0);
    }
    addPlayerSlots(InventoryPlayer);
    slotChangedCraftingGrid(world, player, craftMatrix, craftResult);

    tileEntity.addListener(this);
  }

  private void addSideContainerSlots(TileEntity te, EnumFacing dir, int xPos, int yPos){
    subContainerSlotStart = 10;
    IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
    int slotCount = handler.getSlots();
    for (int y = 0; y < (int)Math.ceil((double)slotCount / 6);y++)
    for(int x = 0; x < 6;x++) {
      int index = 6 * y + x;
      if (index >= slotCount)continue;
      int offset = y >= 9 ? -10000 : 0;
      WrapperSlot wrapper = new WrapperSlot(new SlotItemHandler(handler,index,18 * x +xPos,18 * y + yPos + offset));
      addSlotToContainer(wrapper);
    }
    subContainerSlotEnd = inventorySlots.size();
  }

  @Override
  public void onContainerClosed(EntityPlayer player) {
    tileEntity.removeListener(this);

    super.onContainerClosed(player);
  }

  private void addOwnSlots() {
    // crafting result
    this.addSlotToContainer(new SlotFastCraft(this, player, this.craftMatrix, this.craftResult, SLOT_RESULT, 124, 35));

    // crafting grid
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        addSlotToContainer(new Slot(craftMatrix, x + 3 * y, 30 + 18 * x, 17 + 18 * y));
      }
    }
  }

  private void addPlayerSlots(InventoryPlayer inventoryPlayer) {
    // inventory
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 9; x++) {
        addSlotToContainer(new Slot(inventoryPlayer, 9 + x + 9 * y, 8 + 18 * x, 84 + 18 * y));
      }
    }

    // hotbar
    for (int x = 0; x < 9; x++) {
      addSlotToContainer(new Slot(inventoryPlayer, x, 8 + 18 * x, 142));
    }
  }


  public void updateSlotPositions(int offset)
  {
    int index = 0;
    for (int i = subContainerSlotStart; i < subContainerSlotEnd ; i++) {
      Slot slot = inventorySlots.get(i);
      int y = (index / 6) - offset;
      slot.yPos = (y >= 9 || y < 0) ? -2000 : 18 + 18 * y;
      index++;
    }
  }

  @Override
  public void tileEntityContentsChanged() {
    onCraftMatrixChanged(craftMatrix);
  }

  @Override
  public void onCraftMatrixChanged(IInventory inventory) {
    slotChangedCraftingGrid(world, player, craftMatrix, craftResult);
  }

  @Override
  public boolean canInteractWith(EntityPlayer player) {
    return true;
  }

  @Nonnull
  @Override
  public ItemStack transferStackInSlot(EntityPlayer player, int index) {

    //shift click sucks

    int hotBarEnd = this.inventorySlots.size() - 1;
    int hotbarStart = hotBarEnd - 8;
    int playerMainEnd = hotbarStart - 1;
    int playerMainStart = playerMainEnd - 26;

    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      if (index == 0) {
        itemstack1.getItem().onCreated(itemstack1, world, player);

        if (!mergeItemStack(itemstack1, playerMainStart, hotBarEnd, true)) {
          return ItemStack.EMPTY;
        }

        slot.onSlotChange(itemstack1, itemstack);
      } else if (index >= playerMainStart && index < playerMainEnd) {
        if (!mergeItemStack(itemstack1, hotbarStart, hotBarEnd, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index >= hotbarStart && index < hotBarEnd) {
        if (!mergeItemStack(itemstack1, playerMainStart, hotbarStart, false)) {
          return ItemStack.EMPTY;
        }
      } else if (!mergeItemStack(itemstack1, playerMainStart, hotBarEnd, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }

      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }

      ItemStack itemstack2 = slot.onTake(player, itemstack1);

      if (index == 0) {
        player.dropItem(itemstack2, false);
      }
    }

    return itemstack;
  }

  private void syncRecipeToAllOpenWindows(final IRecipe lastRecipe, List<EntityPlayerMP> players) {
    players.forEach(otherPlayer -> {
      // safe cast since hasSameContainerOpen does class checks
      ((CraftingStationContainer)otherPlayer.openContainer).lastRecipe = lastRecipe;
      PacketHandler.INSTANCE.sendTo(new LastRecipePacket(lastRecipe), otherPlayer);
    });
  }

  @Override
  protected void slotChangedCraftingGrid(World world, EntityPlayer player, InventoryCrafting inv, InventoryCraftResult result) {
    ItemStack itemstack = ItemStack.EMPTY;

    // if the recipe is no longer valid, update it
    if(lastRecipe == null || !lastRecipe.matches(inv, world)) {
      lastRecipe = CraftingManager.findMatchingRecipe(inv, world);
    }

    // if we have a recipe, fetch its result
    if(lastRecipe != null) {
      itemstack = lastRecipe.getCraftingResult(inv);
    }
    // set the slot on both sides, client is for display/so the client knows about the recipe
    result.setInventorySlotContents(SLOT_RESULT, itemstack);

    // update recipe on server
    if(!world.isRemote) {
      EntityPlayerMP entityplayermp = (EntityPlayerMP) player;

      // we need to sync to all players currently in the inventory
      List<EntityPlayerMP> relevantPlayers = getAllPlayersWithThisContainerOpen(this, entityplayermp.getServerWorld());

      // sync result to all serverside inventories to prevent duplications/recipes being blocked
      // need to do this every time as otherwise taking items of the result causes desync
      syncResultToAllOpenWindows(itemstack, relevantPlayers);

      // if the recipe changed, update clients last recipe
      // this also updates the client side display when the recipe is added
      if(lastLastRecipe != lastRecipe) {
        syncRecipeToAllOpenWindows(lastRecipe, relevantPlayers);
        lastLastRecipe = lastRecipe;
      }
    }
  }

  // server can be gotten from EntityPlayerMP
  private List<EntityPlayerMP> getAllPlayersWithThisContainerOpen(CraftingStationContainer container, WorldServer server) {
    return server.playerEntities.stream()
            .filter(player -> hasSameContainerOpen(container, player))
            .map(player -> (EntityPlayerMP)player)
            .collect(Collectors.toList());
  }

  private boolean hasSameContainerOpen(CraftingStationContainer container, EntityPlayer playerToCheck) {
    return playerToCheck instanceof EntityPlayerMP &&
            playerToCheck.openContainer.getClass().isAssignableFrom(container.getClass()) &&
            this.sameGui((CraftingStationContainer) playerToCheck.openContainer);
  }

  public boolean sameGui(CraftingStationContainer otherContainer) {
    return this.tileEntity == otherContainer.tileEntity;
  }


  private void syncResultToAllOpenWindows(final ItemStack stack, List<EntityPlayerMP> players) {
    players.forEach(otherPlayer -> {
      otherPlayer.openContainer.putStackInSlot(SLOT_RESULT, stack);
      otherPlayer.connection.sendPacket(new SPacketSetSlot(otherPlayer.openContainer.windowId, SLOT_RESULT, stack));
    });
  }

  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != craftResult && super.canMergeSlot(stack, slot);
  }

  public NonNullList<ItemStack> getRemainingItems() {
    if(lastRecipe != null && lastRecipe.matches(craftMatrix, world)) {
      return lastRecipe.getRemainingItems(craftMatrix);
    }
    return craftMatrix.stackList;
  }

  public void updateLastRecipeFromServer(IRecipe recipe) {
    lastRecipe = recipe;
    // if no recipe, set to empty to prevent ghost outputs when another player grabs the result
    this.craftResult.setInventorySlotContents(SLOT_RESULT, recipe != null ? recipe.getCraftingResult(craftMatrix) : ItemStack.EMPTY);
  }

  public int getRows(){
    return subContainerSlotStart == -1 ? 0 :(subContainerSlotEnd - subContainerSlotStart)/6;
  }
}