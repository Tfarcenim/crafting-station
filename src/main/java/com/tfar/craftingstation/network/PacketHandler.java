package com.tfar.craftingstation.network;

import com.tfar.craftingstation.CraftingStation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;


public class PacketHandler {

  public static SimpleChannel INSTANCE;

  public static void registerMessages(String channelName) {
    INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(CraftingStation.MODID, channelName), () -> "1.0", s -> true, s -> true);
    INSTANCE.registerMessage(0, LastRecipePacket.class,
            LastRecipePacket::encode,
            LastRecipePacket::new,
            LastRecipePacket::handle);
  }
}