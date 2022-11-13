package tfar.craftingstation.slot;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class BigSlot extends SlotItemHandler {
  private final int index;

  public BigSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
    super(itemHandler, index, xPosition, yPosition);
    this.index = index;
  }

  @Override
  public int getMaxStackSize(@Nonnull ItemStack stack) {
    return getItemHandler().getSlotLimit(index);
  }

  @Override
  public boolean isSameInventory(Slot other) {
    return other instanceof BigSlot && ((BigSlot) other).getItemHandler() == this.getItemHandler();
  }
}