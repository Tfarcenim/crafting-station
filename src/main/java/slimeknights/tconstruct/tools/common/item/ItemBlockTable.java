package slimeknights.tconstruct.tools.common.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import slimeknights.tconstruct.mantle.item.ItemBlockMeta;
import slimeknights.tconstruct.mantle.util.LocUtils;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.shared.tileentity.TileTable;
import slimeknights.tconstruct.tools.common.block.BlockToolTable;

public class ItemBlockTable extends ItemBlockMeta {

  public ItemBlockTable(Block block) {
    super(block);
  }

  @Override
  public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
    super.addInformation(stack, worldIn, tooltip, flagIn);
    if(!stack.hasTagCompound()) {
      return;
    }

    ItemStack legs = getLegStack(stack);
    if(!legs.isEmpty()) {
      tooltip.add(legs.getDisplayName());
    }

    if(stack.getTagCompound().hasKey("inventory")) {
      this.addInventoryInformation(stack, worldIn, tooltip, flagIn);
    }
  }
  /**
   * Gets the itemstack that determines the leg's texture from the table
   * @param table  Input table
   * @return  The itemstack determining the leg's texture, or null if none exists
   */
  public static ItemStack getLegStack(ItemStack table) {
    NBTTagCompound tag = TagUtil.getTagSafe(table).getCompoundTag(TileTable.FEET_TAG);
    return new ItemStack(tag);
  }

  protected void addInventoryInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
    // get the inventory, so we can inspect its items
    NBTTagCompound inventory = stack.getTagCompound().getCompoundTag("inventory");

    if(!inventory.hasKey("Items")) {
      return;
    }

    NBTTagList items = inventory.getTagList("Items", 10);

    // generic chest, only show count
    tooltip.addAll(LocUtils.getTooltips(Util.translateFormatted("tooltip.chest.has_items", items.tagCount())));
  }
}
