package tfar.craftingstation.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginCommon;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.client.renderer.Rect2i;
import tfar.craftingstation.client.CraftingStationScreen;
import tfar.craftingstation.init.ModBlocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@REIPluginCommon
public class ReiClientPlugin implements REIClientPlugin {
 // @Override
 // public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
 //   registration.addRecipeTransferHandler(new CraftingStationTransferInfo());
  //}

  @Override
  public void registerCategories(CategoryRegistry registry) {
    registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(ModBlocks.crafting_station),EntryStacks.of(ModBlocks.crafting_station_slab));
  }
  

  @Nonnull
 // @Override
  public List<Rect2i> getGuiExtraAreas(CraftingStationScreen containerScreen) {
    List<Rect2i> areas = new ArrayList<>();
    if (containerScreen.getMenu().hasSideContainers){
      int x = (containerScreen.width - 140) / 2 - 140;
      int y = (containerScreen.height - 180) / 2 - 16;
      areas.add(new Rect2i(x, y, 140, 196));    }
    return areas;
  }
}
