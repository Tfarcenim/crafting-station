package slimeknights.tconstruct.common.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import slimeknights.tconstruct.mantle.pulsar.config.ForgeCFG;
import slimeknights.tconstruct.CraftingStation;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.utils.RecipeUtil;

public final class Config {

  public static ForgeCFG pulseConfig = new ForgeCFG("TinkerModules", "Modules");
  public static Config instance = new Config();
  public static Logger log = Util.getLogger("Config");

  private Config() {
  }

  // Tools and general
  private static String[] craftingStationBlacklistArray = new String[] {
      "de.ellpeck.actuallyadditions.mod.tile.TileEntityItemViewer"
  };
  private static String[] orePreference = {
      "minecraft",
      "craftingstation",
      "thermalfoundation",
      "forestry",
      "immersiveengineering",
      "embers",
      "ic2"
  };
  public static Set<String> craftingStationBlacklist = Collections.emptySet();

  // Clientside configs
  public static boolean renderTableItems = true;
  public static boolean renderInventoryNullLayer = true;

  /* Config File */

  static Configuration configFile;

  static ConfigCategory Modules;
  static ConfigCategory Gameplay;
  static ConfigCategory ClientSide;

  public static void load(FMLPreInitializationEvent event) {
    configFile = new Configuration(event.getSuggestedConfigurationFile(), "0.1", false);

    MinecraftForge.EVENT_BUS.register(instance);

    syncConfig();
  }

  @SubscribeEvent
  public void update(ConfigChangedEvent.OnConfigChangedEvent event) {
    if(event.getModID().equals(CraftingStation.modID)) {
      syncConfig();
    }
  }


  public static boolean syncConfig() {
    Property prop;

    // Modules
    Modules = pulseConfig.getCategory();
    // Gameplay
    {
      String cat = "gameplay";
      List<String> propOrder = Lists.newArrayList();
      Gameplay = configFile.getCategory(cat);


      prop = configFile.get(cat, "craftingStationBlacklist", craftingStationBlacklistArray);
      prop.setComment("Blacklist of registry names or TE classnames for the crafting station to connect to. Mainly for compatibility.");
      craftingStationBlacklistArray = prop.getStringList();
      craftingStationBlacklist = Sets.newHashSet(craftingStationBlacklistArray);
      propOrder.add(prop.getName());

      prop = configFile.get(cat, "orePreference", orePreference);
      prop.setComment("Preferred mod ID for oredictionary outputs. Top most mod ID will be the preferred output ID, and if none is found the first output stack is used.");
      orePreference = prop.getStringList();
      RecipeUtil.setOrePreferences();
      propOrder.add(prop.getName());
    }
     // Clientside
    {
      String cat = "clientside";
      List<String> propOrder = Lists.newArrayList();
      ClientSide = configFile.getCategory(cat);

      // rename renderTableItems to renderInventoryInWorld
      configFile.renameProperty(cat, "renderTableItems", "renderInventoryInWorld");

      prop = configFile.get(cat, "renderInventoryInWorld", renderTableItems);
      prop.setComment("If true all of Tinkers' blocks with contents (tables, basin, drying racks,...) will render their contents in the world");
      renderTableItems = prop.getBoolean();
      propOrder.add(prop.getName());

      prop = configFile.get(cat, "renderInventoryNullLayer", renderInventoryNullLayer);
      prop.setComment("If true use a null render layer when building the models to render tables. Fixes an issue with chisel, but the config is provide in case it breaks something.");
      renderInventoryNullLayer = prop.getBoolean();
      propOrder.add(prop.getName());

      ClientSide.setPropertyOrder(propOrder);
    }

    // save changes if any
    boolean changed = false;
    if(configFile.hasChanged()) {
      configFile.save();
      changed = true;
    }
    if(pulseConfig.getConfig().hasChanged()) {
      pulseConfig.flush();
      changed = true;
    }
    return changed;
  }
}
