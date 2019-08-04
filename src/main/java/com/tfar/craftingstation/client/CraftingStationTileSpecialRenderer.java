package com.tfar.craftingstation.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tfar.craftingstation.CraftingStationTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class CraftingStationTileSpecialRenderer extends TileEntityRenderer<CraftingStationTile> {
  private ItemRenderer itemRenderer;

  private static double level = 1.015;

  private static double[][] shifts = {
          {0.25F, level, 0.5F},
      //    {0.75F, level, 0.5F},
    //      {0.5F, level, 0.25F},
  //        {0.5F, level, 0.75F}
  };


  @Override
  public void render(CraftingStationTile tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
    if (this.rendererDispatcher.renderInfo != null && tileEntity.getDistanceSq(this.rendererDispatcher.renderInfo.getProjectedView().x, this.rendererDispatcher.renderInfo.getProjectedView().y, this.rendererDispatcher.renderInfo.getProjectedView().z) < 128d) {

      double shiftX;
      double shiftY;
      double shiftZ;

      if (this.itemRenderer == null) {
        this.itemRenderer = new ItemRenderer(Minecraft.getInstance().textureManager, Minecraft.getInstance().getModelManager(), Minecraft.getInstance().getItemColors());
      }
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3;j++) {
          ItemStack item = tileEntity.input.getStackInSlot(j + 3 * i);
          if (item.isEmpty())continue;
          boolean isBlock = (item.getItem() instanceof BlockItem);
          double blockScale = isBlock ? .5 : .31;
          shiftX = (isBlock ? 0.311 : .306) + .189 * j;
          shiftY = isBlock ? 1.0625 : 1.005;
          shiftZ = (isBlock ? 0.217 : .277) + .189 * i;
          GlStateManager.pushMatrix();
          GlStateManager.enableBlend();
          GlStateManager.translated(x + shiftX, y + shiftY, z + shiftZ);
          //if(!isBlock)
          GlStateManager.rotated(90, 1, 0, 0);

          GlStateManager.scaled(blockScale, blockScale, blockScale);

          this.itemRenderer.renderItem(item, ItemCameraTransforms.TransformType.GROUND);
          GlStateManager.popMatrix();
        }
      }
    }
  }
}
