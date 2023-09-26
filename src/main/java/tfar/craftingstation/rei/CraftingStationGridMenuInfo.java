package tfar.craftingstation.rei;

import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import tfar.craftingstation.CraftingStationMenu;

import java.util.stream.IntStream;

public class CraftingStationGridMenuInfo<D extends SimpleGridMenuDisplay> implements SimpleGridMenuInfo<CraftingStationMenu, D> {
        private final D display;

        public CraftingStationGridMenuInfo(D display) {
            this.display = display;
        }


        @Override
        public D getDisplay() {
            return display;
        }

    @Override
    public int getCraftingResultSlotIndex(CraftingStationMenu menu) {
        return 0;
    }

    @Override
    public int getCraftingWidth(CraftingStationMenu menu) {
        return 3;
    }

    @Override
    public int getCraftingHeight(CraftingStationMenu menu) {
        return 3;
    }

    public Iterable<SlotAccessor> getInventorySlots(MenuInfoContext<CraftingStationMenu, ?, D> context) {
        return IntStream.range(10, context.getMenu().slots.size())
                .mapToObj(index -> SlotAccessor.fromSlot(context.getMenu().slots.get(index))).toList();
    }
}
