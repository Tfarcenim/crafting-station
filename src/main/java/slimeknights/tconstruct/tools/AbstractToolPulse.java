package slimeknights.tconstruct.tools;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import slimeknights.tconstruct.common.TinkerPulse;

/**
 * Parent pulse for all the pulses that add tinker tools.
 * So you don't forget anything and we can simplify a few tasks
 *
 * Attention Addon-Developers: If you're looking at this.. you can't use it.
 * All your stuff will run after TiC has already registered everything.
 */
public abstract class AbstractToolPulse extends TinkerPulse {


  public void registerItems(Register<Item> event) {
  }

  // INITIALIZATION
  public void init(FMLInitializationEvent event) {
    registerToolBuilding();
  }

  protected void registerToolBuilding() {
  }

  // POST-INITIALIZATION
  public void postInit(FMLPostInitializationEvent event) {
    registerEventHandlers();
  }

  protected void registerEventHandlers() {
  }
}
