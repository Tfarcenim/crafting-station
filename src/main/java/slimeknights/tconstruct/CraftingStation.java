package slimeknights.tconstruct;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.common.ClientProxy;
import slimeknights.tconstruct.common.TinkerNetwork;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.config.ConfigSync;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.mantle.common.GuiHandler;
import slimeknights.tconstruct.mantle.pulsar.control.PulseManager;
import slimeknights.tconstruct.plugin.CraftingTweaks;
import slimeknights.tconstruct.plugin.theoneprobe.TheOneProbe;
import slimeknights.tconstruct.plugin.waila.Waila;
import slimeknights.tconstruct.tools.TinkerTools;

/**
 * TConstruct, the tool mod. Craft your tools with style, then modify until the original is gone!
 *
 * @author mDiyo
 */

@Mod(modid = CraftingStation.modID,
     name = CraftingStation.modName,
     version = CraftingStation.modVersion,
     guiFactory = "slimeknights.craftingstation.common.config.ConfigGui$ConfigGuiFactory",
     dependencies = "required-after:forge@[14.23.1.2577,);"
                    + "after:jei@[4.8,);"
                    + "before:taiga@(1.3.0,);"
                    + "after:chisel",
     acceptedMinecraftVersions = "[1.12, 1.13)")
public class CraftingStation {

  public static final String modID = Util.MODID;
  public static final String modVersion = "0.0.0";
  public static final String modName = "Crafting Station";

  public static final Logger log = LogManager.getLogger(modID);

  @Mod.Instance(modID)
  public static CraftingStation instance;

  public static PulseManager pulseManager = new PulseManager(Config.pulseConfig);
  public static GuiHandler guiHandler = new GuiHandler();

  // Tinker pulses
  static {
    pulseManager.registerPulse(new TinkerTools());

    // Plugins/Integration
    pulseManager.registerPulse(new CraftingTweaks());
    pulseManager.registerPulse(new Waila());
    pulseManager.registerPulse(new TheOneProbe());
  }

  public CraftingStation() {}

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    Config.load(event);

    NetworkRegistry.INSTANCE.registerGuiHandler(instance, guiHandler);

    if(event.getSide().isClient()) {
      ClientProxy.initClient();
      ClientProxy.initRenderMaterials();
    }

    TinkerNetwork.instance.setup();
    MinecraftForge.EVENT_BUS.register(this);
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    if (!event.getSide().isClient()) {
      // config syncing
      MinecraftForge.EVENT_BUS.register(new ConfigSync());
    }
  }
}
