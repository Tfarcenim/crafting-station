package com.tfar.examplemod;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tfar.examplemod.slot.SlotFastCraft;
import com.tfar.examplemod.slot.WrapperSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.*;

public class CraftingStationContainer extends RecipeBookContainer<CraftingInventory> implements CraftingStationTile.Listener {
  public final CraftingInventory craftMatrix;
  public final CraftResultInventory craftResult = new CraftResultInventory();
  public final World world;
  private final BlockPos pos;
  private final PlayerEntity player;
  private final CraftingStationTile tileEntity;
  public IRecipe<CraftingInventory> lastRecipe;
  protected IRecipe<CraftingInventory> lastLastRecipe;

  public Container subContainer;
  public ITextComponent containerName;

  protected Set<Container> shiftClickContainers = Sets.newHashSet();
  protected Map<Integer, Container> slotContainerMap = Maps.newHashMap();
  public int subContainerSlotStart = -1;
  public int[] range = new int[2];


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
        IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                .filter(IItemHandlerModifiable.class::isInstance).orElse(null);
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
      addSubContainer(new SideContainerInventory(id,inventoryTE, accessDir, -3 - 18 * 6, 17, 6), false);
      containerName = inventoryTE instanceof INamedContainerProvider ? ((INamedContainerProvider) inventoryTE).getDisplayName() : playerInventory.getName();
    }
    addPlayerSlots(playerInventory);
    func_217066_a(this.windowId,world, player, craftMatrix, craftResult);

    tileEntity.addListener(this);
  }

  //side container
  private void addSubContainer(Container subcontainer, boolean prefershift) {
    if(subContainer == null) {
      subContainerSlotStart = inventorySlots.size();
    }
    this.subContainer = subcontainer;

    if(prefershift) {
      shiftClickContainers.add(subcontainer);
    }

    int begin = inventorySlots.size();
    for(Slot slot : subcontainer.inventorySlots) {
      WrapperSlot wrapper = new WrapperSlot(slot);
      addSlot(wrapper);
      slotContainerMap.put(wrapper.slotNumber, subcontainer);
    }
    int end = inventorySlots.size();
    range[0]=begin;
    range[1]=end;
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

  @Override
  public void func_201771_a(RecipeItemHelper p_201771_1_) {

  }

  @Override
  public void clear() {

  }

  @Override
  public boolean matches(IRecipe<? super CraftingInventory> recipeIn) {
    return false;
  }


  public int getOutputSlot() {
    return 0;
  }

  public int getWidth() {
    return this.craftMatrix.getWidth();
  }

  public int getHeight() {
    return this.craftMatrix.getHeight();
  }

  public int getSize() {
    return 10;
  }
}