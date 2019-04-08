package slimeknights.tconstruct.common;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import slimeknights.tconstruct.CraftingStation;

/**
 * This class contains all the base functions for server and clientside proxy that should be called. Can be used when no
 * specific handling is needed. Can be replaced with a specific implementation at any time.
 *
 * Also doubles as documentation when what should happen.
 */
public class CommonProxy {

  public void preInit() {
    if(!Loader.instance().isInState(LoaderState.PREINITIALIZATION)) {
      CraftingStation.log.error(
          "Proxy.preInit has to be called during Pre-Initialisation.");
    }
  }

  public void init() {
    if(!Loader.instance().isInState(LoaderState.INITIALIZATION)) {
      CraftingStation.log.error(
          "Proxy.init has to be called during Initialisation.");
    }
  }

  public void postInit() {
    if(!Loader.instance().isInState(LoaderState.POSTINITIALIZATION)) {
      CraftingStation.log.error(
          "Proxy.postInit has to be called during Post-Initialisation.");
    }
  }

  public void registerModels() {
    if(Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
      CraftingStation.log.error(
          "Proxy.registerModels has to be called during preInit. Otherwise the models wont be found on first load.");
    }
  }
}
