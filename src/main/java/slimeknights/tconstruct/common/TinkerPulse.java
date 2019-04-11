package slimeknights.tconstruct.common;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.mantle.item.ItemBlockMeta;

import java.util.Locale;

/**
 * Just a small helper class that provides some function for cleaner Pulses.
 *
 * Items should be registered during PreInit
 *
 * Models should be registered during Init
 */
// MANTLE
public abstract class TinkerPulse {

  protected static <T extends Block> T registerBlock(IForgeRegistry<Block> registry, T block, String name) {
    if(!name.equals(name.toLowerCase(Locale.US))) {
      throw new IllegalArgumentException(String.format("Unlocalized names need to be all lowercase! Block: %s", name));
    }

    String prefixedName = Util.prefix(name);
    block.setTranslationKey(prefixedName);

    register(registry, block, name);
    return block;
  }

  @SuppressWarnings("unchecked")
  protected static <T extends Block> T registerItemBlockProp(IForgeRegistry<Item> registry, ItemBlock itemBlock, IProperty<?> property) {
    itemBlock.setTranslationKey(itemBlock.getBlock().getTranslationKey());

    register(registry, itemBlock, itemBlock.getBlock().getRegistryName());
    ItemBlockMeta.setMappingProperty(itemBlock.getBlock(), property);
    return (T) itemBlock.getBlock();
  }

  protected static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, T thing, String name) {
    thing.setRegistryName(Util.getResource(name));
    registry.register(thing);
    return thing;
  }

  protected static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, T thing, ResourceLocation name) {
    thing.setRegistryName(name);
    registry.register(thing);
    return thing;
  }

  protected static void registerTE(Class<? extends TileEntity> teClazz, String name) {
    if(!name.equals(name.toLowerCase(Locale.US))) {
      throw new IllegalArgumentException(String.format("Unlocalized names need to be all lowercase! TE: %s", name));
    }

    GameRegistry.registerTileEntity(teClazz, Util.prefix(name));
  }
}
