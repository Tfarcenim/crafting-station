package tfar.craftingstation.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.security.Provider;
import java.util.concurrent.CompletableFuture;

public class ModDatagen {

    public static void gather(GatherDataEvent e) {
        DataGenerator dataGenerator = e.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();
        ExistingFileHelper existingFileHelper = e.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = e.getLookupProvider();
        dataGenerator.addProvider(e.includeServer(),new ModBlockTagsProvider(packOutput,lookup,existingFileHelper));
    }
}
