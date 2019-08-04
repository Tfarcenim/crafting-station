package com.tfar.craftingstation.client;

import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationTile;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT)
public class ClientStuff {
  @SubscribeEvent
  public static void doClientStuff(final ModelRegistryEvent event) {
    ClientRegistry.bindTileEntitySpecialRenderer(CraftingStationTile.class, new CraftingStationTileSpecialRenderer());
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CraftingStation.Objects.crafting_station),0,new ModelResourceLocation(Item.getItemFromBlock(CraftingStation.Objects.crafting_station).getRegistryName(), "inventory"));
  }
}
