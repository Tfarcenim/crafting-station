package slimeknights.tconstruct.tools;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.tconstruct.mantle.pulsar.pulse.Pulse;
import slimeknights.tconstruct.common.CommonProxy;
import slimeknights.tconstruct.shared.tileentity.TileTable;
import slimeknights.tconstruct.tools.common.block.BlockToolTable;
import slimeknights.tconstruct.tools.common.item.ItemBlockTable;
import slimeknights.tconstruct.tools.common.tileentity.TileCraftingStation;

@Pulse(id = TinkerTools.PulseId, description = "All the tools and everything related to it.")
public class TinkerTools extends AbstractToolPulse {

  public static final String PulseId = "TinkerTools";

  @SidedProxy(serverSide = "slimeknights.tconstruct.common.CommonProxy",clientSide = "slimeknights.tconstruct.tools.ToolClientProxy")
  public static CommonProxy proxy;

  // Blocks
  public static BlockToolTable toolTables;

  @SubscribeEvent
  public void registerBlocks(Register<Block> event) {
    IForgeRegistry<Block> registry = event.getRegistry();

    // register blocks
    toolTables = registerBlock(registry, new BlockToolTable(), "tooltables");

    registerTE(TileTable.class, "table");
    registerTE(TileCraftingStation.class, "craftingstation");

  }

  @Override
  @SubscribeEvent
  public void registerItems(Register<Item> event) {
    IForgeRegistry<Item> registry = event.getRegistry();

    super.registerItems(event);

    // register blocks
    toolTables = registerItemBlockProp(registry, new ItemBlockTable(toolTables), BlockToolTable.TABLES);

  }

  @SubscribeEvent
  public void registerModels(ModelRegistryEvent event) {
    proxy.registerModels();
  }

  // PRE-INITIALIZATION
  @Subscribe
  public void preInit(FMLPreInitializationEvent event) {
    proxy.preInit();
  }

  // INITIALIZATION
  @Override
  @Subscribe
  public void init(FMLInitializationEvent event) {
    super.init(event);
    proxy.init();
  }

  // POST-INITIALIZATION
  @Override
  @Subscribe
  public void postInit(FMLPostInitializationEvent event) {
    super.postInit(event);

    proxy.postInit();
  }

  @Override
  protected void registerEventHandlers() {
    // prevents tools from despawning
  }
}
