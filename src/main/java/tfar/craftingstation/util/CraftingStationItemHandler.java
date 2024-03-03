package tfar.craftingstation.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tfar.craftingstation.CraftingStationBlockEntity;

public class CraftingStationItemHandler extends ItemStackHandler {

  CraftingStationBlockEntity craftingStationBlockEntity;
  
  public CraftingStationItemHandler(int size, CraftingStationBlockEntity craftingStationBlockEntity){
    super(size);
    this.craftingStationBlockEntity = craftingStationBlockEntity;
  }

  public NonNullList<ItemStack> getContents(){
    return stacks;
  }

  public boolean isEmpty(){
    return getContents().stream().allMatch(ItemStack::isEmpty);
  }

  @Override
  protected void onContentsChanged(int slot) {
    this.craftingStationBlockEntity.setChanged();
    super.onContentsChanged(slot);
  }
}
