package com.tfar.craftingstation;

import com.tfar.craftingstation.slot.WrapperSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingStationContainer extends Container implements CraftingStationBlockEntity.Listener {
  public final CraftingInventory craftMatrix;
  public final CraftResultInventory craftResult = new CraftResultInventory();
  public final World world;
  private final BlockPos pos;
  private final PlayerEntity player;
  private final CraftingStationBlockEntity tileEntity;
  public IRecipe<CraftingInventory> lastRecipe;
  protected IRecipe<CraftingInventory> lastLastRecipe;
  public final List<Pair<Integer,Integer>> containerStarts = new ArrayList<>();
  public final List<ItemStack> blocks = new ArrayList<>();

  public final List<ITextComponent> containerNames = new ArrayList<>();

  public int subContainerSize = 0;
  public boolean hasSideContainers;
  public int currentContainer = 0;


  public CraftingStationContainer(int id, PlayerInventory playerInventory, World world, BlockPos pos, PlayerEntity player) {
    super(CraftingStation.Objects.crafting_station_container,id);

    this.world = world;
    this.pos = pos;
    this.player = player;
    this.tileEntity = (CraftingStationBlockEntity) world.getTileEntity(pos);
    assert tileEntity != null;
    this.craftMatrix = new CraftingInventoryPersistant(this, tileEntity.input);
    this.hasSideContainers = false;

    addOwnSlots();

    // detect te
    Direction accessDir = null;
    List<TileEntity> tileEntities = new ArrayList<>();
    for(Direction dir : Direction.values()) {
      BlockPos neighbor = pos.offset(dir);

      TileEntity te = world.getTileEntity(neighbor);
      if(te != null && !(te instanceof CraftingStationBlockEntity)) {
        // if blacklisted, skip checks entirely
    //    if(blacklisted(te.getClass())) {
      //    continue;
    //    }
//        if(te instanceof IInventory && !((IInventory) te).isUsableByPlayer(player)) {
 //         continue;
  //      }

        // try internal access first
        if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent()){
          tileEntities.add(te);
          blocks.add(new ItemStack(world.getBlockState(neighbor).getBlock()));
        }
        // try sided access else
  //      if(te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite())) {
  //        if(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()) instanceof IItemHandlerModifiable) {
  //          inventoryTE = te;
   //         accessDir = dir.getOpposite();
   //         break;
   //       }
     //   }
      }
    }

    if (!tileEntities.isEmpty()) {
      for (TileEntity tileEntity : tileEntities){
        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(this::accept);
      }
    }

    if(!tileEntities.isEmpty()) {
      addSideContainerSlots(tileEntities, accessDir, -125, 17);
    }
    addPlayerSlots(playerInventory);
    func_217066_a(this.windowId,world, player, craftMatrix, craftResult);

    tileEntity.addListener(this);
  }

  private void addSideContainerSlots(List<TileEntity> tes,Direction dir ,int xPos, int yPos){
    hasSideContainers = true;
    for (int i = 0; i < tes.size(); i++) {
      TileEntity te = tes.get(i);
      containerNames.add(te instanceof INamedContainerProvider ? ((INamedContainerProvider)te).getDisplayName() : new StringTextComponent("placeholder"));//inventoryTE instanceof INamedContainerProvider ? ((INamedContainerProvider) inventoryTE).getDisplayName() : playerInventory.getName();
      final int number = i;
      te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).ifPresent((h) -> {
        int size = h.getSlots();
        this.subContainerSize += size;
        int offsetx = (needsScroll()) ? 0 : 8;
        for (int y = 0; y < (int) Math.ceil((double)size / 6); y++)
          for (int x = 0; x < 6; x++) {
            int index = 6 * y + x;
            if (index >= size) continue;
            boolean hidden = y >= 9 || number != 0;
            WrapperSlot wrapper = new WrapperSlot(new SlotItemHandler(h, index, 18 * x + xPos + offsetx, 18 * y + yPos));
            if (hidden)hideSlot(wrapper);
            addSlot(wrapper);
          }
      });
    }
  }

  public void hideSlot(Slot slot){
    slot.yPos = Integer.MAX_VALUE;
  }

  @Override
  public void onContainerClosed(PlayerEntity player) {
    tileEntity.removeListener(this);
    super.onContainerClosed(player);
  }

  private void addOwnSlots() {
    // crafting result
    addSlot(new CraftingResultSlot(player, craftMatrix, craftResult, 0, 124, 35));

    // crafting grid
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        addSlot(new Slot(craftMatrix, x + 3 * y, 30 + 18 * x, 17 + 18 * y));
      }
    }
  }

  private void addPlayerSlots(PlayerInventory playerInventory) {
    // inventory
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 9; x++) {
        addSlot(new Slot(playerInventory, 9 + x + 9 * y, 8 + 18 * x, 84 + 18 * y));
      }
    }

    // hotbar
    for (int x = 0; x < 9; x++) {
      addSlot(new Slot(playerInventory, x, 8 + 18 * x, 142));
    }
  }

  @Override
  public void tileEntityContentsChanged() {
    onCraftMatrixChanged(craftMatrix);
  }

  @Override
  public void onCraftMatrixChanged(IInventory inventory) {
    func_217066_a(this.windowId, world, player, craftMatrix, craftResult);
  }

  @Override
  public boolean canInteractWith(PlayerEntity player) {
    return true;
  }

  @Nonnull
  @Override
  public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
    Slot slot = this.inventorySlots.get(index);

    if(slot == null || !slot.getHasStack()) {
      return ItemStack.EMPTY;
    }

    ItemStack ret = slot.getStack().copy();
    ItemStack stack = slot.getStack().copy();

    boolean nothingDone = true;

    //is this the crafting slot?
    if (index == 0){

      if (hasSideContainers)
        nothingDone = refillTileInventory(stack);
      // Try moving module -> player inventory
      nothingDone &= moveToPlayerInventory(stack);

      // Try moving module -> tile inventory
      if (hasSideContainers)
        nothingDone &= mergeItemStackMove(stack,10,10 + subContainerSize,false);
    }

    // Is the slot an input slot??
    else if(index < 10) {
      if (hasSideContainers)
        nothingDone = refillTileInventory(stack);
      // Try moving module -> player inventory
      nothingDone &= moveToPlayerInventory(stack);

      // Try moving module -> tile inventory
      if (hasSideContainers)
        nothingDone &= mergeItemStackMove(stack,10,10 + subContainerSize,false);
    }
    // Is the slot from the tile?
    else if(index < 10 + subContainerSize && hasSideContainers) {
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
      if (hasSideContainers)
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
  protected ItemStack notifySlotAfterTransfer(PlayerEntity player, @Nonnull ItemStack stack, @Nonnull ItemStack original, Slot slot) {
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


  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != craftResult && super.canMergeSlot(stack, slot);
  }

  protected static void func_217066_a(int p_217066_0_, World p_217066_1_, PlayerEntity player, CraftingInventory p_217066_3_, CraftResultInventory p_217066_4_) {
    if (!p_217066_1_.isRemote) {
      ServerPlayerEntity lvt_5_1_ = (ServerPlayerEntity)player;
      ItemStack lvt_6_1_ = ItemStack.EMPTY;
      Optional<ICraftingRecipe> lvt_7_1_ = p_217066_1_.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, p_217066_3_, p_217066_1_);
      if (lvt_7_1_.isPresent()) {
        ICraftingRecipe lvt_8_1_ = lvt_7_1_.get();
        if (p_217066_4_.canUseRecipe(p_217066_1_, lvt_5_1_, lvt_8_1_)) {
          lvt_6_1_ = lvt_8_1_.getCraftingResult(p_217066_3_);
        }
      }

      p_217066_4_.setInventorySlotContents(0, lvt_6_1_);
      lvt_5_1_.connection.sendPacket(new SSetSlotPacket(p_217066_0_, 0, lvt_6_1_));
    }
  }
  public void updateSlotPositions(int offset) {
    Pair<Integer,Integer> range = containerStarts.get(currentContainer);
    int start = range.getLeft();
    for (int i = start; i < range.getRight(); i++) {
      Slot slot = inventorySlots.get(i);
      int index = (i - start) / 6 - offset;
      slot.yPos = (index >= 9 || index < 0) ? -10000 : 17 + 18 * index;
    }
  }

  public void changeContainer(int newContainer){
    this.currentContainer = newContainer;
     Pair<Integer,Integer> range = containerStarts.get(currentContainer);
     int start = range.getLeft();
     int finish = range.getRight();
     for (int i = 10; i < subContainerSize + 10; i++){
       Slot slot = this.inventorySlots.get(i);
       if (slot instanceof WrapperSlot && (i < start || i >= finish))hideSlot(slot);
     }
     for (int i = start; i < finish;i++){
       Slot slot = inventorySlots.get(i);
       int index = (i - start) / 6;
       slot.yPos = (index >= 9 || index < 0) ? -10000 : 17 + 18 * index;
     }
  }

  public NonNullList<ItemStack> getRemainingItems() {
    if(lastRecipe != null && lastRecipe.matches(craftMatrix, world)) {
      return lastRecipe.getRemainingItems(craftMatrix);
    }
    return craftMatrix.stackList;
  }

  public void updateLastRecipeFromServer(IRecipe<CraftingInventory> recipe) {
    lastRecipe = recipe;
    // if no recipe, set to empty to prevent ghost outputs when another player grabs the result
    this.craftResult.setInventorySlotContents(0, recipe != null ? recipe.getCraftingResult(craftMatrix) : ItemStack.EMPTY);
  }

  public boolean needsScroll(){
    return getSlotCount() > 54;
  }

  public int getRows(){
    return (int)Math.ceil((double)getSlotCount()/6);
  }

  public int getSlotCount(){
    if (containerStarts.isEmpty())return 0;
    Pair<Integer,Integer> range = containerStarts.get(currentContainer);
    return range.getRight() - range.getLeft();
  }

  private void accept(IItemHandler handler) {
    if (containerStarts.size() == 0){
      int left = 10;
      int right = handler.getSlots() + left;
      containerStarts.add(Pair.of(left,right));
      return;
    }

    int left = containerStarts.get(containerStarts.size() - 1).getRight();
    int right = handler.getSlots() + left;
    containerStarts.add(Pair.of(left,right));
  }
}