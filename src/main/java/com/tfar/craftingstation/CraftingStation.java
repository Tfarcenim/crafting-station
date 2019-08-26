package com.tfar.craftingstation;

import com.tfar.craftingstation.client.CraftingStationScreen;
import com.tfar.craftingstation.client.CraftingStationTileSpecialRenderer;
import com.tfar.craftingstation.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CraftingStation.MODID)
public class CraftingStation {
  // Directly reference a log4j logger.

  public static final String MODID = "craftingstation";

  private static final Logger LOGGER = LogManager.getLogger();

  public CraftingStation() {
    // Register the setup method for modloading
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
  }

  private void setup(final FMLCommonSetupEvent event) {
    PacketHandler.registerMessages(MODID);
  }

  // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
  // Event bus for receiving Registry Events)
  @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class RegistryEvents {
    @SubscribeEvent
    public static void block(final RegistryEvent.Register<Block> event) {
      // register a new block here
      event.getRegistry().register(new CraftingStationBlock(Block.Properties
              .create(Material.WOOD)
              .hardnessAndResistance(2,2)).setRegistryName("crafting_station"));
    }

    @SubscribeEvent
    public static void item(final RegistryEvent.Register<Item> event) {
      // register a new block here
      event.getRegistry().register(new BlockItem(Objects.crafting_station, new Item.Properties()
              .group(ItemGroup.DECORATIONS)).setRegistryName("crafting_station"));
    }

    @SubscribeEvent
    public static void container(final RegistryEvent.Register<ContainerType<?>> event){
      event.getRegistry().register(IForgeContainerType.create((windowId, inv, data) ->
              new CraftingStationContainer(windowId, inv, inv.player.world, data.readBlockPos(), inv.player)).setRegistryName("crafting_station_container"));

    }

    @SubscribeEvent
    public static void tile(final RegistryEvent.Register<TileEntityType<?>> event){
      event.getRegistry().register(TileEntityType.Builder.create(CraftingStationTile::new, Objects.crafting_station).build(null).setRegistryName("crafting_station_tile"));
    }
  }
  @ObjectHolder(MODID)
  public static class Objects {
    public static final Block crafting_station = null;
    public static final ContainerType<CraftingStationContainer> crafting_station_container = null;
    public static final TileEntityType<CraftingStationTile> crafting_station_tile = null;
  }
}
