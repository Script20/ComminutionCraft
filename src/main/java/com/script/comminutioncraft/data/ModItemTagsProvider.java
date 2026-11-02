package com.script.comminutioncraft.data;

import com.script.comminutioncraft.ComminutionCraft;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ComminutionCraft.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            tag(metal.dustTag()).add(metal.granulated().get());
            tag(ProcessedMetal.commonItemTag("dusts")).addTag(metal.dustTag());
        }
    }
}
