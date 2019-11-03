package com.tfar.craftingstation.network;

import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SClearPacket {

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    PlayerEntity player = ctx.get().getSender();

    if (player == null) return;

    ctx.get().enqueueWork(() -> {
      Container container = player.openContainer;
      if (container instanceof CraftingStationContainer) {
        CraftingStationContainer craftingStationContainer = (CraftingStationContainer)container;
        for (int i = 1; i < 10;i++)
        craftingStationContainer.transferStackInSlot(player,i);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
