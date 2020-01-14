package com.tfar.craftingstation.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GLX;
import com.tfar.craftingstation.CraftingStationBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.SlabType;

public class CraftingStationBlockEntityRenderer extends TileEntityRenderer<CraftingStationBlockEntity> {

  public CraftingStationBlockEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
    super(p_i226006_1_);
  }

  @Override
  public void render(CraftingStationBlockEntity blockEntity, float var2, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int light, int var6) {
    if (this.dispatcher.renderInfo != null && blockEntity.getDistanceSq(this.dispatcher.renderInfo.getProjectedView().x,
            this.dispatcher.renderInfo.getProjectedView().y, this.dispatcher.renderInfo.getProjectedView().z) < 128d) {

      if (blockEntity.input.isEmpty())return;

      BlockState state = blockEntity.getBlockState();

      double height = state.has(SlabBlock.TYPE) ? state.get(SlabBlock.TYPE) == SlabType.BOTTOM ?.5:1:1;

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
          matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(0));
          //scale x,y,z
          matrixStack.scale(0.25F, 0.25F, 0.25F);

          int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
          Minecraft.getInstance().getItemRenderer().renderItem(item, ItemCameraTransforms.TransformType.FIXED,
                  lightAbove, OverlayTexture.DEFAULT_UV, matrixStack, iRenderTypeBuffer);
          //popmatrix
          matrixStack.pop();
        }
      }
    }
  }
}
