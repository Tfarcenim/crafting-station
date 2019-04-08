package slimeknights.tconstruct.mantle.util;

import com.google.common.collect.Lists;

import net.minecraft.util.text.translation.I18n;

import java.util.List;
import java.util.Locale;

// localization utils
public abstract class LocUtils {

  private LocUtils() {
  }

  public static String translateRecursive(String key, Object... params) {
    return I18n.translateToLocal(I18n.translateToLocalFormatted(key, params));
  }

  public static List<String> getTooltips(String text) {
    List<String> list = Lists.newLinkedList();
    if(text == null)
      return list;
    int j = 0;
    int k;
    while((k = text.indexOf("\\n", j)) >= 0)
    {
      list.add(text.substring(j, k));
      j = k+2;
    }

    list.add(text.substring(j));

    return list;
  }


}
