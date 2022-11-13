package tfar.craftingstation.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import tfar.craftingstation.CraftingStationBlockEntity;

public class ModBlockEntityTypes {
    public static final BlockEntityType<CraftingStationBlockEntity> crafting_station = BlockEntityType.Builder.of(CraftingStationBlockEntity::new, ModBlocks.crafting_station, ModBlocks.crafting_station_slab).build(null);
}
