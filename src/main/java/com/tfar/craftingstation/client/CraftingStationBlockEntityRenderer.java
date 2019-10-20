package com.tfar.craftingstation.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tfar.craftingstation.CraftingStationBlock;
import com.tfar.craftingstation.CraftingStationBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public class CraftingStationBlockEntityRenderer extends TileEntityRenderer<CraftingStationBlockEntity> {

  private ItemRenderer itemRenderer;

  private double[] north = new double[]{0.311, 1.0625,0.217};//.05 + +

  @Override
  public void render(CraftingStationBlockEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
    if (this.rendererDispatcher.renderInfo != null && tileEntity.getDistanceSq(this.rendererDispatcher.renderInfo.getProjectedView().x, this.rendererDispatcher.renderInfo.getProjectedView().y, this.rendererDispatcher.renderInfo.getProjectedView().z) < 128d) {

      double shiftX;
      double shiftY;
      double shiftZ;

      double blockshift = 0.6;

      Direction facing = tileEntity.getBlockState().get(CraftingStationBlock.FACING);

      if (this.itemRenderer == null) {
        this.itemRenderer = new ItemRenderer(Minecraft.getInstance().textureManager, Minecraft.getInstance().getModelManager(), Minecraft.getInstance().getItemColors());
      }
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3;j++) {
          ItemStack item = tileEntity.input.getStackInSlot(j + 3 * i);
          if (item.isEmpty())continue;
          boolean isBlock = (item.getItem() instanceof BlockItem);
          double blockScale = isBlock ? .5 : .31;
          switch (facing) {
            case NORTH: {
              shiftX = (isBlock ? 0.311 : .306) + .189 * j;//.05
              shiftY = isBlock ? 1.0625 : 1.005;//.062
              shiftZ = (isBlock ? 0.217 : .277) + .189 * i;//.06
              break;
            }
            case EAST: {
              shiftX = (isBlock ? 0.311 : .306) - .189 * i + .378;//.05
              shiftY = isBlock ? 1.0625 : 1.005;//.062
              shiftZ = (isBlock ? 0.217 : .277) + .189 * j;//.06
              break;
            }
            case SOUTH: {
              shiftX = (isBlock ? 0.311 : .306) - .189 * j + 0.378;//.05
              shiftY = isBlock ? 1.0625 : 1.005;//.062
              shiftZ = (isBlock ? 0.217 : .277) - .189 * i + 0.378;//.06
              break;
            }
            case WEST: {
              shiftX = (isBlock ? 0.311 : .306) + .189 * i;//.05
              shiftY = isBlock ? 1.0625 : 1.005;//.062
              shiftZ = (isBlock ? 0.217 : .277) - .189 * j + .378;//.06
              break;
            }
            default:throw new RuntimeException();
          }
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
