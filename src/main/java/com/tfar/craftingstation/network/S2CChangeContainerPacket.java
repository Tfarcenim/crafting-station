package com.tfar.craftingstation.network;

import com.tfar.craftingstation.CraftingStationContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CChangeContainerPacket {
    private int newContainer;

    public S2CChangeContainerPacket() {}

    public S2CChangeContainerPacket(int newContainer) {
      this.newContainer = newContainer;
    }

    public S2CChangeContainerPacket(PacketBuffer buf) {
      newContainer = buf.readInt();
    }

    public void encode(PacketBuffer buf) {
      buf.writeInt(newContainer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
      ctx.get().enqueueWork(() -> {
        PlayerEntity player = DistExecutor.callWhenOn(Dist.CLIENT,()->()->Minecraft.getInstance().player);
        Container station = player.openContainer;
        if (station instanceof CraftingStationContainer){
          ((CraftingStationContainer)station).changeContainer(newContainer);
        }
      });
      ctx.get().setPacketHandled(true);
    }
}
