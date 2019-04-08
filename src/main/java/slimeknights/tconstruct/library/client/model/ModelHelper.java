package slimeknights.tconstruct.library.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.shared.client.BakedColoredItemModel;

@SideOnly(Side.CLIENT)
public class ModelHelper extends slimeknights.tconstruct.mantle.client.ModelHelper {

  public static final EnumFacing[] MODEL_SIDES = new EnumFacing[] { null, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };

  public static IBakedModel getBakedModelForItem(ItemStack stack, World world, EntityLivingBase entity) {
    IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, world, entity);
    if(model.isBuiltInRenderer()) {
      // missing model so people don't go paranoid when their chests go missing
      model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getMissingModel();
    }
    else {
      // take color into account
      model = new BakedColoredItemModel(stack, model);
    }
    return model;
  }

}
