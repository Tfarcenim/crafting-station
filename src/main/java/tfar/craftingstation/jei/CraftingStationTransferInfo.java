package tfar.craftingstation.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import tfar.craftingstation.CraftingStationContainer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CraftingStationTransferInfo implements IRecipeTransferInfo<CraftingStationContainer, CraftingRecipe> {

  /**
   * Return the container class that this recipe transfer helper supports.
   */
  @Nonnull
  @Override
  public Class<CraftingStationContainer> getContainerClass() {
    return CraftingStationContainer.class;
  }

  @Override
  public List<Slot> getInventorySlots(CraftingStationContainer container,CraftingRecipe recipe) {
    return IntStream.range(1, 10).mapToObj(container::getSlot).collect(Collectors.toList());
  }

  @Override
  public Optional<MenuType<CraftingStationContainer>> getMenuType() {
    return Optional.empty();
  }

  @Override
  public RecipeType<CraftingRecipe> getRecipeType() {
    return RecipeTypes.CRAFTING;
  }

  @Override
  public List<Slot> getRecipeSlots(CraftingStationContainer container,CraftingRecipe recipe) {
    return IntStream.range(10, container.slots.size()).mapToObj(container::getSlot).collect(Collectors.toList());
  }

  @Override
  public boolean canHandle(CraftingStationContainer container,CraftingRecipe recipe) {
    return true;
  }
}
