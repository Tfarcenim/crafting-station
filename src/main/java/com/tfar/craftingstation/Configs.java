package com.tfar.craftingstation;

import net.minecraftforge.common.ForgeConfigSpec;

public class Configs {

	public static class Client {

      public static ForgeConfigSpec.BooleanValue showItemsInTable;

    Client(ForgeConfigSpec.Builder builder) {
      builder.push("general");
      showItemsInTable = builder
              .comment("Display Items in Table?")
              .translation("text.craftingstation.config.displayitemsintable")
              .define("display items in table", true);
      builder.pop();
    }
  }

  public static class Server {

      public static ForgeConfigSpec.BooleanValue sideInventories;


      Server(ForgeConfigSpec.Builder builder) {
          builder.push("general");
          sideInventories = builder
                  .comment("Are side inventories displayed in the crafting grid?")
                  .translation("text.craftingstation.config.enable_side_inventories")
                  .define("display side inventories in crafting grid", true);
          builder.pop();
      }
  }

}
