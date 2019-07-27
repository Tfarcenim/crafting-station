package com.tfar.craftingstation.network;

import com.tfar.craftingstation.CraftingStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class Network extends NetworkWrapper {

  public static Network instance = new Network();

  public Network() {
    super(CraftingStation.MODID);
  }

  public void setup() {
    // register all the packets

    // TOOLS
  //  registerPacketServer(InventoryCraftingSyncPacket.class);
  //  registerPacketClient(InventorySlotSyncPacket.class);

    // OTHER STUFF
    registerPacketClient(LastRecipeMessage.class);
  }

  public static void sendTo(AbstractPacket packet, EntityPlayerMP player) {
    instance.network.sendTo(packet, player);
  }


  public static void sendToServer(AbstractPacket packet) {
    instance.network.sendToServer(packet);
  }

  public static void sendToClients(WorldServer world, BlockPos pos, AbstractPacket packet) {
    Chunk chunk = world.getChunk(pos);
    for(EntityPlayer player : world.playerEntities) {
      // only send to relevant players
      if(!(player instanceof EntityPlayerMP)) {
        continue;
      }
      EntityPlayerMP playerMP = (EntityPlayerMP) player;
      if(world.getPlayerChunkMap().isPlayerWatchingChunk(playerMP, chunk.x, chunk.z)) {
        Network.sendTo(packet, playerMP);
      }
    }
  }
}
