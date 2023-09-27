package tfar.craftingstation.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginCommon;
import net.minecraft.client.renderer.Rect2i;
import tfar.craftingstation.CraftingStationMenu;
import tfar.craftingstation.client.CraftingStationScreen;
import tfar.craftingstation.init.ModBlocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static me.shedaniel.rei.plugin.common.BuiltinPlugin.CRAFTING;

@REIPluginCommon
public class ReiClientPlugin implements REIClientPlugin {
 // @Override
 // public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
 //   registration.addRecipeTransferHandler(new CraftingStationTransferInfo());
  //}

  @Override
  public void registerCategories(CategoryRegistry registry) {
    registry.addWorkstations(CRAFTING, EntryStacks.of(ModBlocks.crafting_station),EntryStacks.of(ModBlocks.crafting_station_slab));
  }

  @Override
  public void registerTransferHandlers(TransferHandlerRegistry registry) {
    registry.register(new CraftingStationTransferHandler(CraftingStationMenu.class,CRAFTING));
  }

  @Override
  public void registerScreens(ScreenRegistry registry) {
    registry.registerContainerClickArea(new Rectangle(88, 32, 28, 23), CraftingStationScreen.class, CRAFTING);
  }

  @Nonnull
  //@Override
  public List<Rect2i> getGuiExtraAreas(CraftingStationScreen containerScreen) {
    List<Rect2i> areas = new ArrayList<>();
    if (containerScreen.getMenu().hasSideContainers){
      int x = (containerScreen.width - 140) / 2 - 140;
      int y = (containerScreen.height - 180) / 2 - 16;
      areas.add(new Rect2i(x, y, 140, 196));    }
    return areas;
  }
}
