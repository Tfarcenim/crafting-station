package com.tfar.examplemod.jei;

import com.tfar.examplemod.CraftingStation;
import com.tfar.examplemod.CraftingStationContainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.util.ResourceLocation;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(CraftingStationContainer.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
  }

  @Override
  public ResourceLocation getPluginUid() {
    return new ResourceLocation(CraftingStation.MODID, CraftingStation.MODID);
  }
}
