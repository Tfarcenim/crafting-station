
package com.tfar.craftingstation.slot;

import com.tfar.craftingstation.CraftingInventoryPersistant;
import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;


/**
 * SlotCraftingSucks from FastWorkbench adapted for the Crafting Station container (no change in functionality)
 * See: https://github.com/Shadows-of-Fire/FastWorkbench/blob/master/src/main/java/shadows/fastbench/gui/SlotCraftingSucks.java
 * <p>
 * Basically it makes crafting less laggy
 */
public class SlotFastCraft extends CraftingResultSlot {

  private final CraftingStationContainer container;
  protected CraftingInventoryPersistant craftingInventoryPersistant;

  public SlotFastCraft(CraftingStationContainer container, CraftingInventoryPersistant craftingInventoryPersistant, IInventory resultInventory, int slotIndex, int xPosition, int yPosition, PlayerEntity player) {
    super(player, craftingInventoryPersistant, resultInventory, slotIndex, xPosition, yPosition);
    this.container = container;
    this.craftingInventoryPersistant = craftingInventoryPersistant;
  }

  @Override
  public ItemStack remove(int amount) {
    if (this.hasItem()) {
      this.removeCount += Math.min(amount, getItem().getCount());
    }
    return getItem();
  }

  /**
   * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
   */
  @Override
  protected void checkTakeAchievements(ItemStack stack) {
    if (this.removeCount > 0) {
      stack.onCraftedBy(this.player.level, this.player, this.removeCount);
      BasicEventHooks.firePlayerCraftingEvent(this.player, stack, craftSlots);
    }

    this.removeCount = 0;
  }

  @Override
  public ItemStack onTake(PlayerEntity thePlayer, ItemStack craftingResult) {
    this.checkTakeAchievements(craftingResult);
    net.minecraftforge.common.ForgeHooks.setCraftingPlayer(thePlayer);
    /* CHANGE BEGINS HERE */
    NonNullList<ItemStack> nonnulllist = container.getRemainingItems();
    /* END OF CHANGE */
    net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

    // note: craftMatrixPersistent and this.craftSlots are the same object!
    craftingInventoryPersistant.setDoNotCallUpdates(true);

    for (int i = 0; i < nonnulllist.size(); ++i) {
      ItemStack stackInSlot = this.craftSlots.getItem(i);
      ItemStack stack1 = nonnulllist.get(i);

      if (!stackInSlot.isEmpty()) {
        this.craftSlots.removeItem(i, 1);
        stackInSlot = this.craftSlots.getItem(i);
      }

      if (!stack1.isEmpty()) {
        if (stackInSlot.isEmpty()) {
          this.craftSlots.setItem(i, stack1);
        } else if (ItemStack.isSame(stackInSlot, stack1) && ItemStack.tagMatches(stackInSlot, stack1)) {
          stack1.grow(stackInSlot.getCount());
          this.craftSlots.setItem(i, stack1);
        } else if (!this.player.inventory.add(stack1)) {
          this.player.drop(stack1, false);
        }
      }
    }

    craftingInventoryPersistant.setDoNotCallUpdates(false);
    container.slotsChanged(craftingInventoryPersistant);

    return craftingResult;
  }
}
