package slimeknights.tconstruct.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import slimeknights.tconstruct.mantle.network.AbstractPacket;
import slimeknights.tconstruct.mantle.network.NetworkWrapper;
import slimeknights.tconstruct.CraftingStation;
import slimeknights.tconstruct.common.config.ConfigSyncPacket;
import slimeknights.tconstruct.tools.common.network.*;

public class TinkerNetwork extends NetworkWrapper {

  public static TinkerNetwork instance = new TinkerNetwork();

  public TinkerNetwork() {
    super(CraftingStation.modID);
  }

  public void setup() {
    // register all the packets
    registerPacketClient(ConfigSyncPacket.class);

    // TOOLS
    registerPacketServer(TinkerStationTabPacket.class);
    registerPacketServer(InventoryCraftingSyncPacket.class);
    registerPacketClient(InventorySlotSyncPacket.class);
    registerPacketClient(EntityMovementChangePacket.class);

    // OTHER STUFF
    registerPacketServer(BouncedPacket.class);
    registerPacketClient(LastRecipeMessage.class);
  }

  public static void sendTo(AbstractPacket packet, EntityPlayerMP player) {
    instance.network.sendTo(packet, player);
  }


  public static void sendToServer(AbstractPacket packet) {
    instance.network.sendToServer(packet);
  }

  public static void sendToClients(WorldServer world, BlockPos pos, AbstractPacket packet) {
    Chunk chunk = world.getChunkFromBlockCoords(pos);
    for(EntityPlayer player : world.playerEntities) {
      // only send to relevant players
      if(!(player instanceof EntityPlayerMP)) {
        continue;
      }
      EntityPlayerMP playerMP = (EntityPlayerMP) player;
      if(world.getPlayerChunkMap().isPlayerWatchingChunk(playerMP, chunk.x, chunk.z)) {
        TinkerNetwork.sendTo(packet, playerMP);
      }
    }
  }
}
