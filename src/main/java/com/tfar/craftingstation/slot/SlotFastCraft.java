package com.tfar.craftingstation.slot;

import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;

public class SlotFastCraft extends SlotCrafting {

  protected final CraftingStationContainer container;

  public SlotFastCraft(CraftingStationContainer container, EntityPlayer player, InventoryCrafting inv, IInventory holder, int slotIndex, int xPosition, int yPosition) {
    super(player, inv, holder, slotIndex, xPosition, yPosition);
    this.container = container;
  }

  @Nonnull
  @Override
  public ItemStack decrStackSize(int amount) {
    if (this.getHasStack()) {
      this.amountCrafted += Math.min(amount, getStack().getCount());
    }
    return getStack();
  }

  /**
   * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
   */
  @Override
  protected void onCrafting(@Nonnull ItemStack stack) {
    if (this.amountCrafted > 0) {
      stack.onCrafting(this.player.world, this.player, this.amountCrafted);
      FMLCommonHandler.instance().firePlayerCraftingEvent(this.player, stack, craftMatrix);
    }

    this.amountCrafted = 0;
  }

  @Nonnull
  @Override
  public ItemStack onTake(EntityPlayer player,@Nonnull ItemStack stack) {
    this.onCrafting(stack);
    ForgeHooks.setCraftingPlayer(player);
    NonNullList<ItemStack> list;
    if (container.lastRecipe != null && container.lastRecipe.matches(craftMatrix, container.world)) list = container.lastRecipe.getRemainingItems(craftMatrix);
    else list = craftMatrix.stackList;
    ForgeHooks.setCraftingPlayer(null);

    for (int i = 0; i < list.size(); ++i) {
      ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
      ItemStack itemstack1 = list.get(i);

      if (!itemstack.isEmpty()) {
        this.craftMatrix.decrStackSize(i, 1);
        itemstack = this.craftMatrix.getStackInSlot(i);
      }

      if (!itemstack1.isEmpty()) {
        if (itemstack.isEmpty()) {
          this.craftMatrix.setInventorySlotContents(i, itemstack1);
        } else if (ItemStack.areItemsEqual(itemstack, itemstack1) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1)) {
          itemstack1.grow(itemstack.getCount());
          this.craftMatrix.setInventorySlotContents(i, itemstack1);
        } else if (!this.player.inventory.addItemStackToInventory(itemstack1)) {
          this.player.dropItem(itemstack1, false);
        }
      }
    }

    return stack;
  }
}