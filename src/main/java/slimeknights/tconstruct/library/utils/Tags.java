package slimeknights.tconstruct.library.utils;

/**
 * Holds all the NBT Tag keys used by the standard tinkers stuff.
 */
public final class Tags {

  /** The base data of the tinker item. What it is built from. */
  public static final String BASE_DATA = "TinkerData";
  /** Contains the materials of the parts the tool was built from */
  public static final String BASE_MATERIALS = "Materials";
  /** Contains all the applied modifiers */
  public static final String BASE_MODIFIERS = "Modifiers";

  /**
   * Extra-data that is specific to this Itemstack and is used to build the item. An example would be if a special
   * pickaxe had 100 more durability, it'd be stored in here.
   */
  private Tags() {
  }
}
