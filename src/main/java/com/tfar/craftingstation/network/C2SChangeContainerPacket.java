package com.tfar.craftingstation.network;

import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SChangeContainerPacket {
  private int newContainer;

  public C2SChangeContainerPacket() {}

  public C2SChangeContainerPacket(int newContainer) {
    this.newContainer = newContainer;
  }

  public C2SChangeContainerPacket(PacketBuffer buf) {
    newContainer = buf.readInt();
  }

  public void encode(PacketBuffer buf) {
    buf.writeInt(newContainer);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      if (ctx.get() == null || ctx.get().getSender() == null)return;
      ServerPlayerEntity player = ctx.get().getSender();
      Container station = ctx.get().getSender().openContainer;
      if (station instanceof CraftingStationContainer){
        ((CraftingStationContainer)station).changeContainer(newContainer);
      }
      PacketHandler.INSTANCE.sendTo(new S2CChangeContainerPacket(newContainer), player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    });
    ctx.get().setPacketHandled(true);
  }
}

