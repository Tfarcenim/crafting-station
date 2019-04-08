package slimeknights.tconstruct.mantle.client;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexTransformer;
import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.vecmath.Vector3f;

public class ModelHelper {

  public static final IModelState DEFAULT_ITEM_STATE;
  public static final IModelState DEFAULT_TOOL_STATE;
  public static final TRSRTransformation BLOCK_THIRD_PERSON_RIGHT;
  public static final TRSRTransformation BLOCK_THIRD_PERSON_LEFT;

  public static TextureAtlasSprite getTextureFromBlock(Block block, int meta) {
    IBlockState state = block.getStateFromMeta(meta);
    return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
  }

  private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s) {
    return TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
            new Vector3f(tx / 16, ty / 16, tz / 16),
            TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
            new Vector3f(s, s, s),
            null));
  }

  static {
    {
      // equals forge:default-item
      ImmutableMap.Builder<IModelPart, TRSRTransformation> builder = ImmutableMap.builder();
      builder.put(ItemCameraTransforms.TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.5f));
      builder.put(ItemCameraTransforms.TransformType.HEAD, get(0, 13, 7, 0, 180, 0, 1));
      builder.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 3, 1, 0, 0, 0, 0.55f));
      builder.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, get(0, 3, 1, 0, 0, 0, 0.55f));
      builder.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, get(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f));
      builder.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, get(1.13f, 3.2f, 1.13f, 0, 90, -25, 0.68f));
      DEFAULT_ITEM_STATE = new SimpleModelState(builder.build());
    }
    {
      // equals forge:default-tool
      ImmutableMap.Builder<IModelPart, TRSRTransformation> builder = ImmutableMap.builder();
      builder.put(ItemCameraTransforms.TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.5f));
      builder.put(ItemCameraTransforms.TransformType.HEAD, get(0, 13, 7, 0, 180, 0, 1));
      builder.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 4, 0.5f, 0, -90, 55, 0.85f));
      builder.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, get(0, 4, 0.5f, 0, 90, -55, 0.85f));
      builder.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, get(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f));
      builder.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, get(1.13f, 3.2f, 1.13f, 0, 90, -25, 0.68f));
      DEFAULT_TOOL_STATE = new SimpleModelState(builder.build());

    }
    {
      BLOCK_THIRD_PERSON_RIGHT = get(0, 2.5f, 0, 75, 45, 0, 0.375f);
      BLOCK_THIRD_PERSON_LEFT = get(0, 0, 0, 0, 255, 0, 0.4f);
    }
  }


}
