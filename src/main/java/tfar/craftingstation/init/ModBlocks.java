package tfar.craftingstation.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import tfar.craftingstation.CraftingStationSlabBlock;

public class ModBlocks {
    static Block.Properties wood = Block.Properties.copy(Blocks.CRAFTING_TABLE);
    public static final Block crafting_station = new CraftingStationSlabBlock(wood);
    public static final Block crafting_station_slab = new CraftingStationSlabBlock(wood);
}
