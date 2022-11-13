package com.tfar.craftingstation.network;

import com.tfar.craftingstation.client.CraftingStationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CLastRecipePacket {

  public static final ResourceLocation NULL = new ResourceLocation("null", "null");

  ResourceLocation rec;

  public S2CLastRecipePacket() {
  }

  public S2CLastRecipePacket(Recipe<CraftingContainer> toSend) {
    rec = toSend == null ? NULL : toSend.getId();
  }

  public S2CLastRecipePacket(ResourceLocation toSend) {
    rec = toSend;
  }


  public S2CLastRecipePacket(FriendlyByteBuf buf) {
    rec = new ResourceLocation(buf.readUtf());
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeUtf(rec.toString());
  }

  @SuppressWarnings("unchecked")
  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      if (Minecraft.getInstance().screen instanceof CraftingStationScreen) {
        Recipe<?> r = Minecraft.getInstance().level.getRecipeManager().byKey(rec).orElse(null);
        ((CraftingStationScreen) Minecraft.getInstance().screen).getMenu().updateLastRecipeFromServer((Recipe<CraftingContainer>) r);
      }
    });
    ctx.get().setPacketHandled(true);
  }

}