package com.tfar.craftingstation.jei;

import com.tfar.craftingstation.client.CraftingStationScreen;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ContainerScreenHandler implements IGuiContainerHandler<CraftingStationScreen> {
  @Override
  public List<Rectangle2d> getGuiExtraAreas(CraftingStationScreen containerScreen) {
    List<Rectangle2d> areas = new ArrayList<>();
    if (containerScreen.getContainer().hasSideContainer){
      areas.add(new Rectangle2d(-100,60,300,190));
    }
    return areas;
  }

  @Override
  public Collection<IGuiClickableArea> getGuiClickableAreas(CraftingStationScreen containerScreen) {
    //IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(-120, 0, 1200, 900, VanillaRecipeCategoryUid.CRAFTING);
    return Collections.emptyList();
  }
}
