package com.tfar.craftingstation.network;

import com.tfar.craftingstation.CraftingStationContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// not threadsafe!
public class LastRecipePacket implements IMessage {

  private IRecipe recipe;

  public LastRecipePacket() {
  }

  public LastRecipePacket(IRecipe recipe) {
    this.recipe = recipe;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    recipe = CraftingManager.REGISTRY.getObjectById(buf.readInt());
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(CraftingManager.REGISTRY.getIDForObject(recipe));
  }

  public static class Handler implements IMessageHandler<LastRecipePacket, IMessage> {
    @Override
    public IMessage onMessage(LastRecipePacket message, MessageContext ctx) {
      // Always use a construct like this to actually handle your message. This ensures that
      // youre 'handle' code is run on the main Minecraft thread. 'onMessage' itself
      // is called on the networking thread so it is not safe to do a lot of things
      // here.
      FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
      return null;
    }

    private void handle(LastRecipePacket message, MessageContext ctx) {
      // This code is run on the server side. So you can do server-side calculations here
      Minecraft.getMinecraft().addScheduledTask(() -> {
        Container container = Minecraft.getMinecraft().player.openContainer;
        if(container instanceof CraftingStationContainer) {
          ((CraftingStationContainer) container).updateLastRecipeFromServer(message.recipe);
        }
      });
    }
  }
}