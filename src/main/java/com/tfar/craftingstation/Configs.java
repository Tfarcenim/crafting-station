package com.tfar.craftingstation;

import net.minecraftforge.common.ForgeConfigSpec;

public class Configs {

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
