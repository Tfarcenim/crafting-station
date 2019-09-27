package com.tfar.craftingstation.network;

import com.tfar.craftingstation.CraftingStation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;


public class PacketHandler {

  public static SimpleChannel INSTANCE;

  public static void registerMessages(String channelName) {
    INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(CraftingStation.MODID, channelName), () -> "1.0", s -> true, s -> true);
    INSTANCE.registerMessage(0, SLastRecipePacket.class,
            SLastRecipePacket::encode,
            SLastRecipePacket::new,
            SLastRecipePacket::handle);

    INSTANCE.registerMessage(4, CClearPacket.class,
            (cMessagePickBlock, buffer) -> {},
            buffer -> new CClearPacket(),
            CClearPacket::handle);
  }
}