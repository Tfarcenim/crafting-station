package tfar.craftingstation;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
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
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfar.craftingstation.init.ModBlockEntityTypes;
import tfar.craftingstation.init.ModBlocks;
import tfar.craftingstation.init.ModMenuTypes;
import tfar.craftingstation.network.PacketHandler;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CraftingStation.MODID)
public class CraftingStation {
  // Directly reference a log4j logger.

  public static final String MODID = "craftingstation";
	public static final TagKey<BlockEntityType<?>> blacklisted
					= TagKey.create(Registry.BLOCK_ENTITY_TYPE_REGISTRY,new ResourceLocation(MODID,"blacklisted"));

	public static final Logger LOGGER = LogManager.getLogger();

  public CraftingStation() {
    // Register the setup method for modloading
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    IEventBus iEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    iEventBus.addListener(this::setup);
    iEventBus.addListener(this::enqueueIMC);
    iEventBus.addListener(RegistryEvents::block);
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
      CompoundTag tagCompound = new CompoundTag();
      tagCompound.putString("ContainerClass", CraftingStationContainer.class.getName());
      tagCompound.putString("AlignToGrid", "left");
      return tagCompound;
    });
  }
  public static class RegistryEvents {
    @SubscribeEvent
    public static void block(final RegisterEvent event) {
      // register a new block here
      register(ModBlocks.crafting_station,"crafting_station",event.getRegistry());
      register(ModBlocks.crafting_station_slab,"crafting_station_slab",event.getRegistry());

    }

    @SubscribeEvent
    public static void item(final RegistryEvent.Register<Item> event) {
      // register a new item here
      Item.Properties properties = new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS);
      register(new BlockItem(ModBlocks.crafting_station,properties),"crafting_station",event.getRegistry());
      register(new BlockItem(ModBlocks.crafting_station_slab,properties),"crafting_station_slab",event.getRegistry());
    }

    @SubscribeEvent
    public static void container(final RegistryEvent.Register<MenuType<?>> event){
      register(ModMenuTypes.crafting_station,"crafting_station",event.getRegistry());

    }

    @SubscribeEvent
    public static void tile(final RegistryEvent.Register<BlockEntityType<?>> event){
      register(ModBlockEntityTypes.crafting_station,"crafting_station",event.getRegistry());
    }

    private static <T extends IForgeRegistryEntry<T>> void register(T obj, String name, IForgeRegistry<T> registry) {
      registry.register(obj.setRegistryName(new ResourceLocation(MODID, name)));
    }
  }
}
