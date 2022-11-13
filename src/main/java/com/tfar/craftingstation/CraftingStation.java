package com.tfar.craftingstation;

import com.tfar.craftingstation.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.tfar.craftingstation.CraftingStation.Objects.crafting_station;
import static com.tfar.craftingstation.CraftingStation.Objects.crafting_station_slab;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CraftingStation.MODID)
public class CraftingStation {
  // Directly reference a log4j logger.

  public static final String MODID = "craftingstation";
	public static final ITag<TileEntityType<?>> blacklisted
					= ForgeTagHandler.makeWrapperTag(ForgeRegistries.TILE_ENTITIES,new ResourceLocation(MODID,"blacklisted"));

	public static final Logger LOGGER = LogManager.getLogger();

  public CraftingStation() {
    // Register the setup method for modloading
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    IEventBus iEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    iEventBus.addListener(this::setup);
    iEventBus.addListener(this::enqueueIMC);
    //ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Configs.SERVER_SPEC);
  }

  public static final Configs.Server SERVER;
  public static final ForgeConfigSpec SERVER_SPEC;

  static {
    //final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
    //CLIENT_SPEC = specPair.getRight();
    //CLIENT = specPair.getLeft();
    final Pair<Configs.Server, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder().configure(Configs.Server::new);
    SERVER_SPEC = specPair2.getRight();
    SERVER = specPair2.getLeft();
  }


  private void setup(final FMLCommonSetupEvent event) {
    PacketHandler.registerMessages(MODID);
  }

  private void enqueueIMC(final InterModEnqueueEvent event) {
    InterModComms.sendTo("craftingtweaks", "RegisterProvider", () -> {
      CompoundNBT tagCompound = new CompoundNBT();
      tagCompound.putString("ContainerClass", CraftingStationContainer.class.getName());
      tagCompound.putString("AlignToGrid", "left");
      return tagCompound;
    });
  }

  // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
  // Event bus for receiving Registry Events)
  @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class RegistryEvents {
    @SubscribeEvent
    public static void block(final RegistryEvent.Register<Block> event) {
      // register a new block here
      Block.Properties wood = Block.Properties.copy(Blocks.CRAFTING_TABLE);
      register(new CraftingStationBlock(wood),"crafting_station",event.getRegistry());
      register(new CraftingStationSlabBlock(wood),"crafting_station_slab",event.getRegistry());

    }

    @SubscribeEvent
    public static void item(final RegistryEvent.Register<Item> event) {
      // register a new item here
      Item.Properties properties = new Item.Properties().tab(ItemGroup.TAB_DECORATIONS);
      register(new BlockItem(crafting_station,properties),"crafting_station",event.getRegistry());
      register(new BlockItem(crafting_station_slab,properties),"crafting_station_slab",event.getRegistry());
    }

    @SubscribeEvent
    public static void container(final RegistryEvent.Register<ContainerType<?>> event){
      register(IForgeContainerType.create((windowId, inv, data) ->
              new CraftingStationContainer(windowId, inv, inv.player.level, data.readBlockPos())),"crafting_station_container",event.getRegistry());

    }

    @SubscribeEvent
    public static void tile(final RegistryEvent.Register<TileEntityType<?>> event){
      register(TileEntityType.Builder.of(CraftingStationBlockEntity::new,crafting_station,crafting_station_slab).build(null),"crafting_station_tile",event.getRegistry());
    }

    private static <T extends IForgeRegistryEntry<T>> void register(T obj, String name, IForgeRegistry<T> registry) {
      registry.register(obj.setRegistryName(new ResourceLocation(MODID, name)));
    }
  }
  @ObjectHolder(MODID)
  public static class Objects {
    public static final Block crafting_station = null;
    public static final Block crafting_station_slab = null;
    public static final ContainerType<CraftingStationContainer> crafting_station_container = null;
    public static final TileEntityType<CraftingStationBlockEntity> crafting_station_tile = null;
  }
}
