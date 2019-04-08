package slimeknights.tconstruct.library.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public final class TagUtil {

  public static int TAG_TYPE_STRING = (new NBTTagString()).getId();

  private TagUtil() {
  }

  /* Generic Tag Operations */
  public static NBTTagCompound getTagSafe(ItemStack stack) {
    // yes, the null checks aren't needed anymore, but they don't hurt either.
    // After all the whole purpose of this function is safety/processing possibly invalid input ;)
    if(stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
      return new NBTTagCompound();
    }

    return stack.getTagCompound();
  }

  public static NBTTagCompound getTagSafe(NBTTagCompound tag, String key) {
    if(tag == null) {
      return new NBTTagCompound();
    }

    return tag.getCompoundTag(key);
  }

  public static NBTTagList getTagListSafe(NBTTagCompound tag, String key, int type) {
    if(tag == null) {
      return new NBTTagList();
    }

    return tag.getTagList(key, type);
  }


  /* Operations concerning the base-data of the tool */
  public static NBTTagCompound getBaseTag(ItemStack stack) {
    return getBaseTag(getTagSafe(stack));
  }

  public static NBTTagCompound getBaseTag(NBTTagCompound root) {
    return getTagSafe(root, Tags.BASE_DATA);
  }


  public static NBTTagList getBaseModifiersTagList(ItemStack stack) {
    return getBaseModifiersTagList(getTagSafe(stack));
  }

  public static NBTTagList getBaseModifiersTagList(NBTTagCompound root) {
    return getTagListSafe(getBaseTag(root), Tags.BASE_MODIFIERS, TAG_TYPE_STRING);
  }

  public static NBTTagList getBaseMaterialsTagList(ItemStack stack) {
    return getBaseMaterialsTagList(getTagSafe(stack));
  }

  public static NBTTagList getBaseMaterialsTagList(NBTTagCompound root) {
    return getTagListSafe(getBaseTag(root), Tags.BASE_MATERIALS, TAG_TYPE_STRING);
  }


  /* Helper functions */

}
