package com.tfar.craftingstation.client;

import com.tfar.craftingstation.CraftingStation;
import com.tfar.craftingstation.CraftingStationBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus= Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ClientStuffs {

  public static final Minecraft mc = Minecraft.getInstance();

  @SubscribeEvent
  public static void doClientStuff(final FMLClientSetupEvent event) {
    ScreenManager.registerFactory(CraftingStation.Objects.crafting_station_container, CraftingStationScreen::new);
    ClientRegistry.bindTileEntitySpecialRenderer(CraftingStationBlockEntity.class, new CraftingStationBlockEntityRenderer());
  }
}
