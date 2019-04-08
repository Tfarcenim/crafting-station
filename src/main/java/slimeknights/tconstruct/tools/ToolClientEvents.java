package slimeknights.tconstruct.tools;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.shared.client.BakedTableModel;
import slimeknights.tconstruct.tools.common.block.BlockToolTable;

@SideOnly(Side.CLIENT)
public class ToolClientEvents {

  // tool tables
  private static final String LOCATION_ToolTable = Util.resource("tooltables");

  // the actual locations where the models are located
  public static final ModelResourceLocation locCraftingStation = getTableLoc();


  private static ModelResourceLocation getTableLoc() {
    return new ModelResourceLocation(LOCATION_ToolTable, String.format("%s=%s",
                                                                       BlockToolTable.TABLES.getName(),
                                                                       BlockToolTable.TABLES.getName(BlockToolTable.TableTypes.CraftingStation)));
  }

  @SubscribeEvent
  public void onModelBake(ModelBakeEvent event) {
    // tool tables
    replaceTableModel(locCraftingStation, event);
  }

  public static void replaceTableModel(ModelResourceLocation location, ModelBakeEvent event) {
    try {
      IModel model = ModelLoaderRegistry.getModel(location);
      IBakedModel standard = event.getModelRegistry().getObject(location);
      IBakedModel finalModel = new BakedTableModel(standard, model, DefaultVertexFormats.BLOCK);
      event.getModelRegistry().putObject(location, finalModel);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }



}
