package slimeknights.tconstruct.tools;

import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.tconstruct.common.ClientProxy;


public class ToolClientProxy extends ClientProxy {

  @Override
  public void preInit() {
    super.preInit();

    MinecraftForge.EVENT_BUS.register(new ToolClientEvents());
  }

  @Override
  public void postInit() {
  }

  @Override
  public void registerModels() {
    // blocks
    Item tableItem = Item.getItemFromBlock(TinkerTools.toolTables);
    ModelLoader.setCustomModelResourceLocation(tableItem, 0, ToolClientEvents.locCraftingStation);
  }

}
