package com.tfar.craftingstation.jei;

import com.tfar.craftingstation.CraftingStationContainer;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.inventory.Slot;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CraftingStationTransferInfo implements IRecipeTransferInfo<CraftingStationContainer> {

  /**
   * Return the container class that this recipe transfer helper supports.
   */
  @Nonnull
  @Override
  public Class<CraftingStationContainer> getContainerClass() {
    return CraftingStationContainer.class;
  }

  /**
   * Return the recipe category that this container can handle.
   */
  @Nonnull
  @Override
  public String getRecipeCategoryUid() {
    return VanillaRecipeCategoryUid.CRAFTING;
  }

  /**
   * Return true if this recipe transfer info can handle the given container instance.
   *
   * @param container the container
   * @since JEI 4.0.2
   */
  @Override
  public boolean canHandle(@Nonnull CraftingStationContainer container) {
    return true;
  }

  /**
   * Return a list of slots for the recipe area.
   *
   * @param container the container
   */
  @Nonnull
  @Override
  public List<Slot> getRecipeSlots(@Nonnull CraftingStationContainer container) {
    return IntStream.range(1, 10).mapToObj(container::getSlot).collect(Collectors.toList());
  }

  /**
   * Return a list of slots that the transfer can use to get items for crafting, or place leftover items.
   *
   * @param container the container
   */
  @Nonnull
  @Override
  public List<Slot> getInventorySlots(@Nonnull CraftingStationContainer container) {
    return IntStream.range(10, container.inventorySlots.size()).mapToObj(container::getSlot).collect(Collectors.toList());
  }
}

