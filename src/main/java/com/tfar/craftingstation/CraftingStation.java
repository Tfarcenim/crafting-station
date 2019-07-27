package com.tfar.craftingstation;

import com.tfar.craftingstation.network.Network;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = CraftingStation.MODID, name = CraftingStation.NAME, version = CraftingStation.VERSION)
public class CraftingStation
{
    public static final String MODID = "craftingstation";
    public static final String NAME = "Example Mod";
    public static final String VERSION = "1.0";

    private static Logger logger;

    @Mod.Instance
    public static CraftingStation instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        Network.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new CraftingStationGuiHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
}
