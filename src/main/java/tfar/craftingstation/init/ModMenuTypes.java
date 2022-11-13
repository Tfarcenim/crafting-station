package tfar.craftingstation.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import tfar.craftingstation.CraftingStationContainer;

public class ModMenuTypes {
    public static final MenuType<CraftingStationContainer> crafting_station = IForgeMenuType.create((windowId, inv, data) ->
            new CraftingStationContainer(windowId, inv, data.readBlockPos()));
}
