/* Code for ctl and shift down from TicTooltips by squeek502
 * https://github.com/squeek502/TiC-Tooltips/blob/1.7.10/java/squeek/tictooltips/helpers/KeyHelper.java
 */

package slimeknights.tconstruct.library;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.translation.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.mantle.util.RecipeMatchRegistry;

import java.util.Locale;

@SuppressWarnings("deprecation")
public class Util {

  public static final String MODID = "craftingstation";
  public static final String RESOURCE = MODID.toLowerCase(Locale.US);

  public static Logger getLogger(String type) {
    String log = MODID;

    return LogManager.getLogger(log + "-" + type);
  }


  /**
   * Returns the given Resource prefixed with tinkers resource location. Use this function instead of hardcoding
   * resource locations.
   */
  public static String resource(String res) {
    return String.format("%s:%s", RESOURCE, res);
  }

  public static ResourceLocation getResource(String res) {
    return new ResourceLocation(RESOURCE, res);
  }


  /**
   * Prefixes the given unlocalized name with tinkers prefix. Use this when passing unlocalized names for a uniform
   * namespace.
   */
  public static String prefix(String name) {
    return String.format("%s.%s", RESOURCE, name.toLowerCase(Locale.US));
  }

  /**
   * Translate the string, insert parameters into the result of the translation
   */
  public static String translateFormatted(String key, Object... pars) {
    // translates twice to allow rerouting/alias
    return I18n.translateToLocal(I18n.translateToLocalFormatted(key, pars).trim()).trim();
  }

  /** Returns a fixed size DEEP copy of the list */
  public static NonNullList<ItemStack> deepCopyFixedNonNullList(NonNullList<ItemStack> in) {
    return RecipeMatchRegistry.copyItemStackArray(in);
  }

  /** @deprecated use deepCopyFixedNonNullList */
  @Deprecated
  public static NonNullList<ItemStack> copyItemStackArray(NonNullList<ItemStack> in) {
    return deepCopyFixedNonNullList(in);
  }

  static {
    ImmutableMap.Builder<Vec3i, EnumFacing> builder = ImmutableMap.builder();
    for(EnumFacing facing : EnumFacing.VALUES) builder.put(facing.getDirectionVec(), facing);
  }
}
