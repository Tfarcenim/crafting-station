package com.tfar.craftingstation;

import com.google.common.collect.Lists;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Configs {

  public static final ServerConfig SERVER;
  public static final ForgeConfigSpec SERVER_SPEC;

  static {
    final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
    SERVER_SPEC = specPair.getRight();
    SERVER = specPair.getLeft();
  }

  public static class ClientConfig {

    public static ForgeConfigSpec.BooleanValue showItemsInTable;

    ClientConfig(ForgeConfigSpec.Builder builder) {
      builder.push("client");
      showItemsInTable = builder
              .comment("Always show armor bar even if empty?")
              .translation("text.overloadedarmorbar.config.alwaysshowarmorbar")
              .define("Always show bar", false);
      builder.pop();
    }
  }
  public static class ServerConfig {
    static ForgeConfigSpec.ConfigValue<List<? extends String>> blockEntityTypeStrings;
    public static final Set<TileEntityType<?>> blockentitytypes = new HashSet<>();

    ServerConfig(ForgeConfigSpec.Builder builder) {

      builder.push("server");
      blockEntityTypeStrings = builder
              .comment("Blacklisted Block Entities")
              .translation("text.craftingstation.config.blacklistedblockentities")
              .defineList("blacklisted block entities", Lists.newArrayList(), String.class::isInstance);
    }
  }
  public static void onConfigChanged(ModConfig.ModConfigEvent e){
    if (e.getConfig().getModId().equals(CraftingStation.MODID)){
      ServerConfig.blockentitytypes.clear();
      ServerConfig.blockEntityTypeStrings.get().forEach((blockEntityType) -> ServerConfig.blockentitytypes.add(ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(blockEntityType))));
    }
  }
}
