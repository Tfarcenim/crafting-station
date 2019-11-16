package com.tfar.craftingstation.client;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationBlock;
import com.tfar.craftingstation.CraftingStationBlockEntity;
import com.tfar.craftingstation.CraftingStationSlabBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;

public class CraftingStationBlockEntityRenderer extends TileEntityRenderer<CraftingStationBlockEntity> {

  @Override
  public void render(CraftingStationBlockEntity blockEntity, double x, double y, double z, float partialTicks, int destroyStage) {
    if (this.rendererDispatcher.renderInfo != null && blockEntity.getDistanceSq(this.rendererDispatcher.renderInfo.getProjectedView().x, this.rendererDispatcher.renderInfo.getProjectedView().y, this.rendererDispatcher.renderInfo.getProjectedView().z) < 128d) {

      double shiftX;
      double shiftY;
      double shiftZ;

      final double sixteenth = .0625;

      final double shift = .189;

      BlockState storedState = blockEntity.getBlockState();

      Direction facing = storedState.get(CraftingStationBlock.FACING);

      double height = storedState.getBlock() == CraftingStation.Objects.crafting_station_slab ? storedState.get(SlabBlock.TYPE) == SlabType.BOTTOM ? .5 : 1 : 1;

      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3;j++) {
          ItemStack item = blockEntity.input.getStackInSlot(j + 3 * i);
          if (item.isEmpty())continue;
          boolean isBlock = (item.getItem() instanceof BlockItem);
          double blockScale = isBlock ? .25 : .125;
          double offset = isBlock ? sixteenth : 0;
          double offset2 = isBlock ? .125 : 0;
          final double offset3 = .3109;
          shiftY = height + offset;
          double x1 = offset2 / 2 - offset - shift * i + 0.689;
          double z1 = offset3 - offset2 / 2 + offset + shift * i;
          switch (facing) {
            case NORTH: {
              shiftX = offset3 + shift * j;
              shiftZ = z1;
              break;
            }
            case EAST: {
              shiftX = x1;
              shiftZ = offset3 + shift * j;
              break;
            }
            case SOUTH: {
              shiftX = 0.689 - shift * j;
              shiftZ = x1;
              break;
            }
            case WEST: {
              shiftX = z1;
              shiftZ =  0.689 - shift * j;
              break;
            }
            default:throw new IllegalStateException(facing.toString());
          }
          GlStateManager.pushMatrix();
          GlStateManager.enableBlend();
          GlStateManager.translated(x + shiftX, y + shiftY, z + shiftZ);
          //if(!isBlock)
        GlStateManager.rotated(90, 1, 0, 0);
          switch (facing){
            case WEST: GlStateManager.rotated(90, 0, 0, 1);
            break;
            case SOUTH: break;
            case EAST: GlStateManager.rotated(270, 0, 0, 1);
              break;
            case NORTH: GlStateManager.rotated(180, 0, 0, 1);
            default://no
          }
          GlStateManager.scaled(blockScale, blockScale, blockScale);
          int light = blockEntity.getWorld().getCombinedLight(blockEntity.getPos().up(), 0);
          GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, light % 65536, light / 65536);
          ClientStuffs.mc.getItemRenderer().renderItem(item, ItemCameraTransforms.TransformType.FIXED);
          GlStateManager.popMatrix();
        }
      }
    }
  }
}
