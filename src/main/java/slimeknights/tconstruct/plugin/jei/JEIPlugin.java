package slimeknights.tconstruct.plugin.jei;

import mezz.jei.api.*;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.CraftingStation;
import slimeknights.tconstruct.plugin.jei.interpreter.TableSubtypeInterpreter;
import slimeknights.tconstruct.plugin.jei.table.TableRecipeHandler;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.common.TableRecipeFactory.TableRecipe;
import slimeknights.tconstruct.tools.common.block.BlockToolTable;

import javax.annotation.Nonnull;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {
  public static IJeiHelpers jeiHelpers;
  // crafting grid slots, integer constants from the default crafting grid implementation
  private static final int craftOutputSlot = 0;
  private static final int craftInputSlot1 = 1;

  public static ICraftingGridHelper craftingGridHelper;
  public static IRecipeRegistry recipeRegistry;


  @Override
  public void registerItemSubtypes(ISubtypeRegistry registry) {
    TableSubtypeInterpreter tableInterpreter = new TableSubtypeInterpreter();

    // tools
    if(CraftingStation.pulseManager.isPulseLoaded(TinkerTools.PulseId)) {
      // tool tables
      registry.registerSubtypeInterpreter(Item.getItemFromBlock(TinkerTools.toolTables), tableInterpreter);

    }
  }

  @Override
  public void register(@Nonnull IModRegistry registry) {
    jeiHelpers = registry.getJeiHelpers();
    IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

    // crafting helper used by the shaped table wrapper
    craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1, craftOutputSlot);

    if(CraftingStation.pulseManager.isPulseLoaded(TinkerTools.PulseId)) {
      registry.handleRecipes(TableRecipe.class, new TableRecipeHandler(), VanillaRecipeCategoryUid.CRAFTING);

      // crafting table shiftclicking
      registry.getRecipeTransferRegistry().addRecipeTransferHandler(new CraftingStationRecipeTransferInfo());

      // add our crafting table to the list with the vanilla crafting table
      registry.addRecipeCatalyst(new ItemStack(TinkerTools.toolTables, 1, BlockToolTable.TableTypes.CraftingStation.meta), VanillaRecipeCategoryUid.CRAFTING);
    }

  }

  @Override
  public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
    recipeRegistry = jeiRuntime.getRecipeRegistry();
  }

}
