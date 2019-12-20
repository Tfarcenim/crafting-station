package com.tfar.craftingstation.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationBlock;
import com.tfar.craftingstation.CraftingStationBlockEntity;
import com.tfar.craftingstation.CraftingStationSlabBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL13;

public class CraftingStationBlockEntityRenderer extends TileEntityRenderer<CraftingStationBlockEntity> {

  public CraftingStationBlockEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
    super(p_i226006_1_);
  }

  @Override
  public void func_225616_a_(CraftingStationBlockEntity blockEntity, float var2, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int var5, int var6) {
    if (this.field_228858_b_.renderInfo != null && blockEntity.getDistanceSq(this.field_228858_b_.renderInfo.getProjectedView().x, this.field_228858_b_.renderInfo.getProjectedView().y, this.field_228858_b_.renderInfo.getProjectedView().z) < 128d) {

      final double spacing = .189;
      final double offset = .31;
      //translate x,y,z
      matrixStack.func_227861_a_(0,1.0625, 0);
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          ItemStack item = blockEntity.input.getStackInSlot(j + 3 * i);
          if (item.isEmpty()) continue;
          //pushmatrix
          matrixStack.func_227860_a_();
          //translate x,y,z
          matrixStack.func_227861_a_(spacing * i +offset, 0, spacing * j +offset);
          matrixStack.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(0));
          //scale x,y,z
          matrixStack.func_227862_a_(0.25F, 0.25F, 0.25F);
          Minecraft.getInstance().getItemRenderer().func_229110_a_(item, ItemCameraTransforms.TransformType.FIXED, var5, OverlayTexture.field_229196_a_, matrixStack, iRenderTypeBuffer);
          //popmatrix
          matrixStack.func_227865_b_();
        }
      }
    }
  }
}
