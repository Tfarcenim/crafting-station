package com.tfar.craftingstation;

import com.tfar.craftingstation.slot.SlotFastCraft;
import com.tfar.craftingstation.slot.WrapperSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Optional;

public class CraftingStationContainer extends Container implements CraftingStationTile.Listener {
  public final CraftingInventory craftMatrix;
  public final CraftResultInventory craftResult = new CraftResultInventory();
  public final World world;
  private final BlockPos pos;
  private final PlayerEntity player;
  private final CraftingStationTile tileEntity;
  public IRecipe<CraftingInventory> lastRecipe;
  protected IRecipe<CraftingInventory> lastLastRecipe;

  public ITextComponent containerName;

  public int subContainerSlotStart = -1;
  public int subContainerSlotEnd = -1;


  public CraftingStationContainer(int id, PlayerInventory playerInventory, World world, BlockPos pos, PlayerEntity player) {
    super(CraftingStation.Objects.crafting_station_container,id);

    this.world = world;
    this.pos = pos;
    this.player = player;
    this.tileEntity = (CraftingStationTile) world.getTileEntity(pos);
    assert tileEntity != null;
    this.craftMatrix = new CraftingInventoryPersistant(this, tileEntity.input);


    addOwnSlots();

    // detect te
    TileEntity inventoryTE = null;
    Direction accessDir = null;
    for(Direction dir : Direction.values()) {
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
        LazyOptional<IItemHandler> maybe = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        IItemHandler handler = maybe.orElse(null);

        if (handler == null)continue;

        inventoryTE = te;
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

    if(inventoryTE != null) {
      addSideContainerSlots(inventoryTE, accessDir, -3 - 18 * 6, 17);
      containerName = inventoryTE instanceof INamedContainerProvider ? ((INamedContainerProvider) inventoryTE).getDisplayName() : playerInventory.getName();
    }
    addPlayerSlots(playerInventory);
    func_217066_a(this.windowId,world, player, craftMatrix, craftResult);

    tileEntity.addListener(this);
  }

  private void addSideContainerSlots(TileEntity te,Direction dir ,int xPos, int yPos){
    subContainerSlotStart = inventorySlots.size();
    IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
    int slotCount = handler.getSlots();
    for (int y = 0; y < (int)Math.ceil((double)slotCount / 6);y++)
      for(int x = 0; x < 6;x++) {
        int index = 6 * y + x;
        if (index >= slotCount)continue;
        int offset = y >= 9 ? -10000 : 0;
        WrapperSlot wrapper = new WrapperSlot(new SlotItemHandler(handler,index,18 * x +xPos,18 * y + yPos + offset));
        addSlot(wrapper);
      }
    subContainerSlotEnd = inventorySlots.size();
  }

  @Override
  public void onContainerClosed(PlayerEntity player) {
    tileEntity.removeListener(this);

    super.onContainerClosed(player);
  }

  private void addOwnSlots() {
    // crafting result
    addSlot(new SlotFastCraft(this,player, craftMatrix, craftResult, 0, 124, 35));

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

  @Override
  public ItemStack transferStackInSlot(PlayerEntity player, int index) {
    // shamelessly copied from ContainerWorkbench

    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      if (index == 0) {
        itemstack1.getItem().onCreated(itemstack1, world, player);

        if (!mergeItemStack(itemstack1, 10, 46, true)) {
          return ItemStack.EMPTY;
        }

        slot.onSlotChange(itemstack1, itemstack);
      } else if (index >= 10 && index < 37) {
        if (!mergeItemStack(itemstack1, 37, 46, false)) {
          return ItemStack.EMPTY;
        }
      } else if (index >= 37 && index < 46) {
        if (!mergeItemStack(itemstack1, 10, 37, false)) {
          return ItemStack.EMPTY;
        }
      } else if (!mergeItemStack(itemstack1, 10, 46, false)) {
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

  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != craftResult && super.canMergeSlot(stack, slot);
  }

  protected static void func_217066_a(int p_217066_0_, World p_217066_1_, PlayerEntity p_217066_2_, CraftingInventory p_217066_3_, CraftResultInventory p_217066_4_) {
    if (!p_217066_1_.isRemote) {
      ServerPlayerEntity lvt_5_1_ = (ServerPlayerEntity)p_217066_2_;
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
  public void updateSlotPositions(int offset)
  {
    int index = 0;
    for (int i = subContainerSlotStart; i < subContainerSlotEnd ; i++) {
      Slot slot = inventorySlots.get(i);
      int y = (index / 6) - offset;
      slot.yPos = (y >= 9 || y < 0) ? -2000 : 17 + 18 * y;
      index++;
    }
  }
  public int getRows(){
    return subContainerSlotStart == -1 ? 0 :(subContainerSlotEnd - subContainerSlotStart)/6;
  }
}