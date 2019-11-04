package com.tfar.craftingstation.client;

import com.tfar.craftingstation.CraftingStationTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class CraftingStationTileSpecialRenderer extends TileEntitySpecialRenderer<CraftingStationTile> {

  @Override
  public void render(CraftingStationTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    if (te.getDistanceSq(this.rendererDispatcher.entityX, this.rendererDispatcher.entityY, this.rendererDispatcher.entityZ) < 128d){

      double shiftX;
      double shiftY;
      double shiftZ;

      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3;j++) {
          ItemStack item = te.input.getStackInSlot(j + 3 * i);
          if (item.isEmpty())continue;
          boolean isBlock = (item.getItem() instanceof ItemBlock);
          double blockScale = isBlock ? .5 : .31;
          shiftX = (isBlock ? 0.311 : .306) + .189 * j;
          shiftY = isBlock ? 1.0625 : 1.005;
          shiftZ = (isBlock ? 0.217 : .277) + .189 * i;
          GlStateManager.pushMatrix();
          GlStateManager.enableBlend();
          GlStateManager.translate(x + shiftX, y + shiftY, z + shiftZ);
          //if(!isBlock)
          GlStateManager.rotate(90, 1, 0, 0);

          GlStateManager.scale(blockScale, blockScale, blockScale);

          Minecraft.getMinecraft().getItemRenderer().renderItem(Minecraft.getMinecraft().player,item, ItemCameraTransforms.TransformType.GROUND);
          GlStateManager.popMatrix();
        }
      }
    }
  }
}
