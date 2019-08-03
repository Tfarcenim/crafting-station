package com.tfar.examplemod.jei;

import com.tfar.examplemod.CraftingStation;
import com.tfar.examplemod.CraftingStationContainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

@JEIPlugin
class JeiPlugin implements IModPlugin {

  @Override
  public void register(IModRegistry registry) {
    registry.getRecipeTransferRegistry().addRecipeTransferHandler(CraftingStationContainer.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
  }

}
