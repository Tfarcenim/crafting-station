package com.tfar.craftingstation.network;

import com.tfar.craftingstation.CraftingStation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;


public class PacketHandler {

  public static SimpleChannel INSTANCE;

  public static void registerMessages(String channelName) {
    INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(CraftingStation.MODID, channelName), () -> "1.0", s -> true, s -> true);
    INSTANCE.registerMessage(0, S2CLastRecipePacket.class,
            S2CLastRecipePacket::encode,
            S2CLastRecipePacket::new,
            S2CLastRecipePacket::handle);

    INSTANCE.registerMessage(1, C2SClearPacket.class,
            (cMessagePickBlock, buffer) -> {},
            buffer -> new C2SClearPacket(),
            C2SClearPacket::handle);

    INSTANCE.registerMessage(2, C2SChangeContainerPacket.class,
            C2SChangeContainerPacket::encode,
            C2SChangeContainerPacket::new,
            C2SChangeContainerPacket::handle);

    INSTANCE.registerMessage(3, S2CChangeContainerPacket.class,
            S2CChangeContainerPacket::encode,
            S2CChangeContainerPacket::new,
            S2CChangeContainerPacket::handle);
  }
}