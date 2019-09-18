package com.tfar.craftingstation.jei;

import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.client.CraftingStationScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JeiPlugin implements IModPlugin,IGuiScreenHandler<CraftingStationScreen> {
  @Override
  public void register(IModRegistry registry) {
    registry.getRecipeTransferRegistry().addRecipeTransferHandler(new CraftingStationTransferInfo());
    registry.addRecipeCatalyst(new ItemStack(CraftingStation.Objects.crafting_station),VanillaRecipeCategoryUid.CRAFTING);
    registry.addGuiScreenHandler(CraftingStationScreen.class, this);
  }

  @Override
  public IGuiProperties apply(CraftingStationScreen guiScreen) {
    return GuiProperties.create(guiScreen);
  }
}
