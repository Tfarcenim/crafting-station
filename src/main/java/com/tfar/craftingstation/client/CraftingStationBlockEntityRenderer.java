package com.tfar.craftingstation.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tfar.craftingstation.CraftingStationBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.math.vector.Vector3f;

public class CraftingStationBlockEntityRenderer extends TileEntityRenderer<CraftingStationBlockEntity> {

  public CraftingStationBlockEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
    super(p_i226006_1_);
  }

  @Override
  public void render(CraftingStationBlockEntity blockEntity, float var2, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int light, int var6) {
    if (this.renderDispatcher.renderInfo != null) {

      if (blockEntity.input.isEmpty())return;

      BlockState state = blockEntity.getBlockState();

      double height = state.hasProperty(SlabBlock.TYPE) ? state.get(SlabBlock.TYPE) == SlabType.BOTTOM ?.5:1:1;

      final double spacing = .189;
      final double offset = .31;
      //translate x,y,z
      matrixStack.translate(0,.0625 + height, 0);
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          ItemStack item = blockEntity.input.getStackInSlot(j + 3 * i);
          if (item.isEmpty()) continue;

          //pushmatrix
          matrixStack.push();
          //translate x,y,z
          matrixStack.translate(spacing * i +offset, 0, spacing * j +offset);
          matrixStack.rotate(Vector3f.YP.rotation(0));
          //scale x,y,z
          matrixStack.scale(0.25F, 0.25F, 0.25F);

          int lightAbove = WorldRenderer.getCombinedLight(blockEntity.getWorld(), blockEntity.getPos().up());
          Minecraft.getInstance().getItemRenderer().renderItem(item, ItemCameraTransforms.TransformType.FIXED,
                  lightAbove, OverlayTexture.NO_OVERLAY, matrixStack, iRenderTypeBuffer);
          //popmatrix
          matrixStack.pop();
        }
      }
    }
  }
}
