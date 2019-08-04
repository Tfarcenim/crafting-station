package com.tfar.craftingstation.jei;

import com.tfar.craftingstation.CraftingStation;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JeiPlugin implements IModPlugin {
  @Override
  public void register(IModRegistry registry) {
    registry.getRecipeTransferRegistry().addRecipeTransferHandler(new CraftingStationTransferInfo());
    registry.addRecipeCatalyst(new ItemStack(CraftingStation.Objects.crafting_station),VanillaRecipeCategoryUid.CRAFTING);
  }
}
