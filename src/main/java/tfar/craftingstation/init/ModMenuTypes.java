package tfar.craftingstation.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import tfar.craftingstation.CraftingStationMenu;

public class ModMenuTypes {
    public static final MenuType<CraftingStationMenu> crafting_station = IForgeMenuType.create((windowId, inv, data) ->
            new CraftingStationMenu(windowId, inv, data.readBlockPos()));
}
