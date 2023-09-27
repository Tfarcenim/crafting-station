package tfar.craftingstation.rei;

import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.transfer.info.stack.VanillaSlotAccessor;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.client.player.LocalPlayer;
import tfar.craftingstation.CraftingStationMenu;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CraftingStationTransferHandler implements SimpleTransferHandler {

    private final Class<CraftingStationMenu> containerClass;
    private final CategoryIdentifier<DefaultCraftingDisplay<?>> categoryIdentifier;

    public CraftingStationTransferHandler(Class<CraftingStationMenu> containerClass,
                                          CategoryIdentifier<DefaultCraftingDisplay<?>> categoryIdentifier) {

        this.containerClass = containerClass;
        this.categoryIdentifier = categoryIdentifier;
    }

    @Override
    public ApplicabilityResult checkApplicable(Context context) {
        if (!containerClass.isInstance(context.getMenu())
                || !categoryIdentifier.equals(context.getDisplay().getCategoryIdentifier())
                || context.getContainerScreen() == null) {
            return ApplicabilityResult.createNotApplicable();
        } else {
            return ApplicabilityResult.createApplicable();
        }
    }

    @Override
    public Iterable<SlotAccessor> getInputSlots(Context context) {
        return IntStream.range(1, 10)
                .mapToObj(id -> SlotAccessor.fromSlot(context.getMenu().getSlot(id)))
                .toList();
    }

    @Override
    public Iterable<SlotAccessor> getInventorySlots(Context context) {
        LocalPlayer player = context.getMinecraft().player;

        int sideContainerOffset = 10 + ((CraftingStationMenu) context.getMenu()).subContainerSize;

        return IntStream.range(10, context.getMenu().slots.size())
                .mapToObj(index -> {
                    if (index < sideContainerOffset) {
                        return new VanillaSlotAccessor(context.getMenu().getSlot(index));
                    } else {
                        return SlotAccessor.fromPlayerInventory(player, index - sideContainerOffset);
                    }
                })
                .collect(Collectors.toList());
    }
}
