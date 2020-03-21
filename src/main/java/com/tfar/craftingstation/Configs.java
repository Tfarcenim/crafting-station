package com.tfar.craftingstation;

import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import tfar.extratags.api.tagtypes.BlockEntityTypeTags;

public class Configs {

	public static final Tag<TileEntityType<?>>  blacklisted
					= BlockEntityTypeTags.makeWrapperTag(new ResourceLocation(CraftingStation.MODID,"blacklisted"));

  public static class ClientConfig {

    public static ForgeConfigSpec.BooleanValue showItemsInTable;

    ClientConfig(ForgeConfigSpec.Builder builder) {
      builder.push("client");
      showItemsInTable = builder
              .comment("Display Items in Table?")
              .translation("text.craftingstation.config.displayitemsintable")
              .define("display items in table", true);
      builder.pop();
    }
  }
}
