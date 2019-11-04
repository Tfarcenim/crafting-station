package com.tfar.craftingstation;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.tfar.craftingstation.CraftingStation.MODID;

@Config(modid = MODID)
@Mod.EventBusSubscriber(modid = MODID)
public class Configs {

  @Config.Name("Tile entity blacklist")
  @Config.Comment("Blacklisted tile entities should be specified in modid:name format")
  public static String[] tileentityblacklist = new String[]{};

  @Config.Ignore
  public static final Set<ResourceLocation> tileentityblacklistresourcelocations = new HashSet<>();

  @SubscribeEvent
  public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
    //Only process events for this mod
    if (event.getModID().equals(MODID)) {
      ConfigManager.sync(CraftingStation.MODID, Config.Type.INSTANCE);
      tileentityblacklistresourcelocations.clear();
      Arrays.stream(tileentityblacklist).map(ResourceLocation::new).forEach(tileentityblacklistresourcelocations::add);
    }
  }
}
