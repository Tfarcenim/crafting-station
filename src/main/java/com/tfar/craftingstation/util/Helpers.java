package com.tfar.craftingstation.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.shapes.VoxelShape;

public class Helpers {
  public static VoxelShape createVoxelShape(double x1,double y1,double z1,double x2,double y2,double z2){
    return Block.makeCuboidShape(x1, y1, z1, x2, y2, z2);
  }
}
