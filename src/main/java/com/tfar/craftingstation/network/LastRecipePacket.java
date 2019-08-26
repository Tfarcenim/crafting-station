package com.tfar.craftingstation.network;

import java.util.function.Supplier;

import com.tfar.craftingstation.client.CraftingStationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class LastRecipePacket  {

  public static final ResourceLocation NULL = new ResourceLocation("null", "null");

  ResourceLocation rec;

  public LastRecipePacket() {
  }

  public LastRecipePacket(IRecipe<CraftingInventory> toSend) {
    rec = toSend == null ? NULL : toSend.getId();
  }

  public LastRecipePacket(ResourceLocation toSend) {
    rec = toSend;
  }


  public LastRecipePacket(PacketBuffer buf) {
    rec = new ResourceLocation(buf.readString());
  }

  public void encode(PacketBuffer buf) {
    buf.writeString(rec.toString());
  }

  @SuppressWarnings("unchecked")
  public void handle(Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      if (Minecraft.getInstance().currentScreen instanceof CraftingStationScreen) {
        IRecipe<?> r = Minecraft.getInstance().world.getRecipeManager().getRecipe(rec).orElse(null);
        ((CraftingStationScreen) Minecraft.getInstance().currentScreen).getContainer().updateLastRecipeFromServer((IRecipe<CraftingInventory>) r);
      }
    });
  }

}