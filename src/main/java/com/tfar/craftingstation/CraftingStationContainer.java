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
  private final EntityPlayer player;
  private final CraftingStationTile tileEntity;
  public IRecipe lastRecipe;
  protected IRecipe lastLastRecipe;
  public boolean hasSideContainer;

  public ITextComponent containerName;

  public int subContainerSize = 0;


  public CraftingStationContainer(InventoryPlayer InventoryPlayer, World world, BlockPos pos, EntityPlayer player) {
    this.world = world;
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
        if(isBlacklisted(te) || te instanceof IInventory && !((IInventory)te).isUsableByPlayer(player)) {
          continue;
        }
//        if(te instanceof IInventory && !((IInventory) te).isUsableByPlayer(player)) {
 //         continue;
  //      }

        // try internal access first
        if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,null)) {
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
      addSideContainerSlots(inventoryTE, accessDir, -125, 18);
      containerName = inventoryTE instanceof IInteractionObject ? ((IInteractionObject) inventoryTE).getDisplayName() : InventoryPlayer.getDisplayName();
    //  scrollTo(0);
    }
    addPlayerSlots(InventoryPlayer);
    slotChangedCraftingGrid(world, player, craftMatrix, craftResult);

    tileEntity.addListener(this);
  }

  protected boolean isBlacklisted(TileEntity te){
    return Configs.tileentityblacklistresourcelocations.contains(TileEntity.getKey(te.getClass()));
  }

  private void addSideContainerSlots(TileEntity te, EnumFacing dir, int xPos, int yPos){
    IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
    this.subContainerSize = handler.getSlots();
    if (!needsScroll())xPos += 8;
    for (int y = 0; y < (int)Math.ceil((double)subContainerSize / 6);y++)
    for(int x = 0; x < 6;x++) {
      int index = 6 * y + x;
      if (index >= subContainerSize)continue;
      int offset = y >= 9 ? -10000 : 0;
      WrapperSlot wrapper = new WrapperSlot(new SlotItemHandler(handler,index,18 * x +xPos,18 * y + yPos + offset));
      addSlotToContainer(wrapper);
    }
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


  public void updateSlotPositions(int offset) {

    for (int i = 10; i < subContainerSize + 10; i++) {
      Slot slot = inventorySlots.get(i);
      int index = ((i-10) / 6) - offset;
      slot.yPos = (index >= 9 || index < 0) ? -2000 : 18 + 18 * index;
    }
  }

  public int getSubContainerSize(){
    return this.subContainerSize;
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
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
    Slot slot = this.inventorySlots.get(index);

    if(slot == null || !slot.getHasStack()) {
      return ItemStack.EMPTY;
    }

    ItemStack ret = slot.getStack().copy();
    ItemStack stack = slot.getStack().copy();

    boolean nothingDone = true;

    //is this the crafting slot?
    if (index == 0){

      if (hasSideContainer)
        nothingDone = refillTileInventory(stack);
      // Try moving module -> player inventory
      nothingDone &= moveToPlayerInventory(stack);

      // Try moving module -> tile inventory
      if (hasSideContainer)
        nothingDone &= mergeItemStackMove(stack,10,10 + subContainerSize,false);
    }

    // Is the slot an input slot??
    else if(index < 10) {
      if (hasSideContainer)
        nothingDone = refillTileInventory(stack);
      // Try moving module -> player inventory
      nothingDone &= moveToPlayerInventory(stack);

      // Try moving module -> tile inventory
      if (hasSideContainer)
        nothingDone &= mergeItemStackMove(stack,10,10 + subContainerSize,false);
    }
    // Is the slot from the tile?
    else if(index < 10 + subContainerSize && hasSideContainer) {
      // Try moving tile -> preferred modules
      nothingDone = moveToCraftingStation(stack);

      // Try moving module -> player inventory
      nothingDone &= moveToPlayerInventory(stack);
    }
    // Slot is from the player inventory
    else if(index >= 10 + subContainerSize) {
      // try moving player -> modules
      nothingDone = moveToCraftingStation(stack);

      // Try moving player -> tile inventory
      if (hasSideContainer)
      nothingDone &= moveToTileInventory(stack);
    }
    // you violated some assumption or something. Shame on you.
    else {
      return ItemStack.EMPTY;
    }

    if(nothingDone) {
      return ItemStack.EMPTY;
    }
    return notifySlotAfterTransfer(playerIn, stack, ret, slot);
  }

  @Nonnull
  protected ItemStack notifySlotAfterTransfer(EntityPlayer player, @Nonnull ItemStack stack, @Nonnull ItemStack original, Slot slot) {
    // notify slot
    slot.onSlotChange(stack, original);

    if(stack.getCount() == original.getCount()) {
      return ItemStack.EMPTY;
    }

    // update slot we pulled from
    slot.putStack(stack);
    slot.onTake(player, stack);

    if(slot.getHasStack() && slot.getStack().isEmpty()) {
      slot.putStack(ItemStack.EMPTY);
    }

    return original;
  }

  protected boolean moveToTileInventory(@Nonnull ItemStack itemstack) {
    return !this.mergeItemStack(itemstack, 10, 10 + subContainerSize, false);
  }

  protected boolean moveToPlayerInventory(@Nonnull ItemStack itemstack) {
    return !this.mergeItemStack(itemstack, 10 + subContainerSize, this.inventorySlots.size(), true);
  }

  protected boolean refillTileInventory(@Nonnull ItemStack itemStack){
    return this.mergeItemStackRefill(itemStack,10,10 + subContainerSize,false);
  }

  protected boolean moveToCraftingStation(@Nonnull ItemStack itemstack) {
    return !this.mergeItemStack(itemstack, 1, 10, false);
  }

  // Fix for a vanilla bug: doesn't take Slot.getMaxStackSize into account
  @Override
  protected boolean mergeItemStack(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
    boolean ret = mergeItemStackRefill(stack, startIndex, endIndex, useEndIndex);
    if(!stack.isEmpty()) ret |= mergeItemStackMove(stack, startIndex, endIndex, useEndIndex);
    return ret;
  }

  // only refills items that are already present
  protected boolean mergeItemStackRefill(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
    if(stack.isEmpty()) return false;

    boolean flag1 = false;
    int k = startIndex;

    if(useEndIndex) {
      k = endIndex - 1;
    }

    Slot slot;
    ItemStack stack1;

    if(stack.isStackable()) {
      while(!stack.isEmpty() && ((!useEndIndex && (k < endIndex)) || (useEndIndex && (k >= startIndex)))) {
        slot = this.inventorySlots.get(k);
        stack1 = slot.getStack();

        if(!stack1.isEmpty()
                && stack1.getItem() == stack.getItem()
                && (!stack.getHasSubtypes() || stack.getMetadata() == stack1.getMetadata())
                && ItemStack.areItemStackTagsEqual(stack, stack1)
                && this.canMergeSlot(stack, slot)) {
          int l = stack1.getCount() + stack.getCount();
          int limit = Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack));

          if(l <= limit) {
            stack.setCount(0);
            stack1.setCount(l);
            slot.onSlotChanged();
            flag1 = true;
          }
          else if(stack1.getCount() < limit) {
            stack.shrink(limit - stack1.getCount());
            stack1.setCount(limit);
            slot.onSlotChanged();
            flag1 = true;
          }
        }

        if(useEndIndex) {
          --k;
        }
        else {
          ++k;
        }
      }
    }
    return flag1;
  }

  // only moves items into empty slots
  protected boolean mergeItemStackMove(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
    if(stack.isEmpty()) return false;

    boolean flag1 = false;
    int k;

    if(useEndIndex) {
      k = endIndex - 1;
    }
    else {
      k = startIndex;
    }

    while(!useEndIndex && k < endIndex || useEndIndex && k >= startIndex) {
      Slot slot = this.inventorySlots.get(k);
      ItemStack itemstack1 = slot.getStack();

      if(itemstack1.isEmpty() && slot.isItemValid(stack) && this.canMergeSlot(stack, slot)) // Forge: Make sure to respect isItemValid in the slot.
      {
        int limit = slot.getItemStackLimit(stack);
        ItemStack stack2 = stack.copy();
        if(stack2.getCount() > limit) {
          stack2.setCount(limit);
          stack.shrink(limit);
        }
        else {
          stack.setCount(0);
        }
        slot.putStack(stack2);
        slot.onSlotChanged();
        flag1 = true;

        if(stack.isEmpty()) {
          break;
        }
      }

      if(useEndIndex) {
        --k;
      }
      else {
        ++k;
      }
    }


    return flag1;
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

  public boolean needsScroll(){
    return this.hasSideContainer && this.subContainerSize > 54;
  }

  public void updateLastRecipeFromServer(IRecipe recipe) {
    lastRecipe = recipe;
    // if no recipe, set to empty to prevent ghost outputs when another player grabs the result
    this.craftResult.setInventorySlotContents(SLOT_RESULT, recipe != null ? recipe.getCraftingResult(craftMatrix) : ItemStack.EMPTY);
  }

  public int getRows(){
    return (int)Math.ceil((double)subContainerSize/6);
  }
}