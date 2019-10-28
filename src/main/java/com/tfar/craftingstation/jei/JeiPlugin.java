package com.tfar.craftingstation.jei;

import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.client.CraftingStationScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin, IGuiContainerHandler<CraftingStationScreen> {
  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(new CraftingStationTransferInfo());
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(new ItemStack(CraftingStation.Objects.crafting_station),VanillaRecipeCategoryUid.CRAFTING);
  }

  @Nonnull
  @Override
  public ResourceLocation getPluginUid() {
    return new ResourceLocation(CraftingStation.MODID, CraftingStation.MODID);
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGuiContainerHandler(CraftingStationScreen.class,this);
  }

  @Nonnull
  @Override
  public List<Rectangle2d> getGuiExtraAreas(CraftingStationScreen containerScreen) {
    List<Rectangle2d> areas = new ArrayList<>();
    if (containerScreen.getContainer().hasSideContainers){
      int x = (containerScreen.width - 140) / 2 - 140;
      int y = (containerScreen.height - 180) / 2 - 16;
      areas.add(new Rectangle2d(x, y, 140, 196));    }
    return areas;
  }
}
