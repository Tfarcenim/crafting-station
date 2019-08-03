package com.tfar.examplemod;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tfar.examplemod.slot.SlotFastCraft;
import com.tfar.examplemod.slot.WrapperSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Map;
import java.util.Set;


public class CraftingStationContainer extends Container implements CraftingStationTile.Listener {
  public final InventoryCrafting craftMatrix;
  public final InventoryCraftResult craftResult = new InventoryCraftResult();
  public final World world;
  private final BlockPos pos;
  private final EntityPlayer player;
  private final CraftingStationTile tileEntity;
  public IRecipe lastRecipe;
  protected IRecipe lastLastRecipe;

  public Container subContainer;
  public ITextComponent containerName;

  protected Set<Container> shiftClickContainers = Sets.newHashSet();
  protected Map<Integer, Container> slotContainerMap = Maps.newHashMap();
  public int subContainerSlotStart = -1;
  public int[] range = new int[2];


  public CraftingStationContainer(InventoryPlayer InventoryPlayer, World world, BlockPos pos, EntityPlayer player) {
    this.world = world;
    this.pos = pos;
    this.player = player;
    this.tileEntity = (CraftingStationTile) world.getTileEntity(pos);
    assert tileEntity != null;
    this.craftMatrix = new CraftingInventoryPersistant(this, tileEntity.input);


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
          if (handler == null) continue;
          inventoryTE = te;
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

    if(inventoryTE != null) {
      addSubContainer(new SideContainerInventory(inventoryTE, accessDir, -3 - 18 * 6, 17, 6), false);
      containerName = inventoryTE instanceof IInteractionObject ? ((IInteractionObject) inventoryTE).getDisplayName() : InventoryPlayer.getDisplayName();
    }
    addPlayerSlots(InventoryPlayer);
    slotChangedCraftingGrid(world, player, craftMatrix, craftResult);

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
      addSlotToContainer(wrapper);
      slotContainerMap.put(wrapper.slotNumber, subcontainer);
    }
    int end = inventorySlots.size();
    range[0]=begin;
    range[1]=end;
  }

  @Override
  public void onContainerClosed(EntityPlayer player) {
    tileEntity.removeListener(this);

    super.onContainerClosed(player);
  }

  private void addOwnSlots() {
    // crafting result
    addSlotToContainer(new SlotFastCraft(this,player, craftMatrix, craftResult, 0, 124, 35));

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

  @Override
  public ItemStack transferStackInSlot(EntityPlayer player, int index) {
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