package slimeknights.tconstruct.mantle.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Comparator;

/**
 * Contains a set of matches. Allows you to easily find if a set of itemstacks matches one of them.
 */
public class RecipeMatchRegistry {


  public static NonNullList<ItemStack> copyItemStackArray(NonNullList<ItemStack> in) {
    NonNullList<ItemStack> stacksCopy = NonNullList.withSize(in.size(), ItemStack.EMPTY);
    for(int i = 0; i < in.size(); i++) {
      if(!in.get(i).isEmpty()) {
        stacksCopy.set(i, in.get(i).copy());
      }
    }

    return stacksCopy;
  }

}
