package com.tfar.craftingstation.network;

import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SClearPacket {

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    Player player = ctx.get().getSender();

    if (player == null) return;

    ctx.get().enqueueWork(() -> {
      AbstractContainerMenu container = player.containerMenu;
      if (container instanceof CraftingStationContainer) {
        CraftingStationContainer craftingStationContainer = (CraftingStationContainer)container;
        for (int i = 1; i < 10;i++)
        craftingStationContainer.quickMoveStack(player,i);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
