package com.tfar.craftingstation.jei;

import com.tfar.craftingstation.client.CraftingStationScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.ArrayList;
import java.util.List;

public class ContainerScreenRegistration implements IGuiHandlerRegistration {

  @Override
  public <T extends ContainerScreen<?>> void addGuiContainerHandler(Class<? extends T> aClass, IGuiContainerHandler<T> iGuiContainerHandler) {

  }

  @Override
  public <T extends Screen> void addGuiScreenHandler(Class<T> aClass, IScreenHandler<T> iScreenHandler) {

  }

  @Override
  public void addGlobalGuiHandler(IGlobalGuiHandler iGlobalGuiHandler) {

  }

  @Override
  public <T extends Screen> void addGhostIngredientHandler(Class<T> aClass, IGhostIngredientHandler<T> iGhostIngredientHandler) {

  }
}
