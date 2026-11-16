package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.recipe.AssemblingRecipe;
import com.script.comminutioncraft.common.recipe.DrillBitRecipe;
import com.script.comminutioncraft.common.recipe.FlotationRecipe;
import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
import com.script.comminutioncraft.common.recipe.PressingRecipe;
import com.script.comminutioncraft.common.recipe.PurifyingRecipe;
import com.script.comminutioncraft.common.recipe.SingularityRecipe;
import com.script.comminutioncraft.common.recipe.TagBlastingRecipe;
import com.script.comminutioncraft.common.recipe.TagResult;
import com.script.comminutioncraft.common.recipe.TagSmeltingRecipe;
import com.script.comminutioncraft.common.recipe.ToolUpgradeRecipe;
import com.script.comminutioncraft.common.recipe.WashingRecipe;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModRecipeProvider extends RecipeProvider {
    private static final int SMELTING_TIME = 200;
    private static final int BLASTING_TIME = 100;

    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    /** Chance per operation of the Ore Purifier's by-product. */
    private static final float BYPRODUCT_CHANCE = 0.2F;

    /**
     * Washed -> purified ore, sometimes with 1 granulated by-product metal. A by-product from another mod only counts
     * when that mod is installed; without it the recipe gives a vanilla nugget instead (a gold nugget with iron, an
     * iron nugget otherwise). Exactly one of the two versions loads.
     */
    private void purifying(RecipeOutput out, ProcessedMetal metal) {
        ProcessedMetal partner = ProcessedMetal.byName(metal.byproduct());
        ItemStackTemplate result = new ItemStackTemplate(metal.purified());
        Ingredient washed = Ingredient.of(metal.washed());
        ItemStackTemplate partnerByproduct = new ItemStackTemplate(partner.granulated());
        if (!partner.fromOtherMods()) {
            out.accept(key("purifying/" + metal.name()), new PurifyingRecipe(washed, result, partnerByproduct, BYPRODUCT_CHANCE), null);
            return;
        }
        ICondition partnerAvailable = new AndCondition(List.of(
                new NotCondition(new TagEmptyCondition<>(partner.rawTag())),
                new NotCondition(new TagEmptyCondition<>(partner.ingotTag()))));
        out.withConditions(partnerAvailable).accept(key("purifying/" + metal.name()),
                new PurifyingRecipe(washed, result, partnerByproduct, BYPRODUCT_CHANCE), null);
        ItemStackTemplate nugget = new ItemStackTemplate(metal.name().equals("iron") ? Items.GOLD_NUGGET : Items.IRON_NUGGET);
        out.withConditions(new NotCondition(partnerAvailable)).accept(key("purifying/" + metal.name() + "_without_" + partner.name()),
                new PurifyingRecipe(washed, result, nugget, BYPRODUCT_CHANCE), null);
    }

    /** The Singularity Processor's rare by-products (each rolled on its own chance per operation). */
    private static List<SingularityRecipe.Byproduct> singularityByproducts() {
        return List.of(
                new SingularityRecipe.Byproduct(new ItemStackTemplate(Items.DIAMOND), 0.03F),
                new SingularityRecipe.Byproduct(new ItemStackTemplate(Items.EMERALD), 0.02F),
                new SingularityRecipe.Byproduct(new ItemStackTemplate(ModItems.SINGULARITY_SHARD.get()), 0.02F),
                new SingularityRecipe.Byproduct(new ItemStackTemplate(Items.NETHERITE_SCRAP), 0.005F),
                new SingularityRecipe.Byproduct(new ItemStackTemplate(Items.NETHER_STAR), 0.0005F));
    }

    /** {@code PAP / DMD / PAP}: plating, Antimatter Circuits, diamonds round the machine it upgrades. */
    private void antimatterMachine(Item result, Item base) {
        shaped(RecipeCategory.MISC, result)
                .pattern("PAP")
                .pattern("DMD")
                .pattern("PAP")
                .define('P', ModItems.REACTOR_PLATING.get())
                .define('A', ModItems.ANTIMATTER_CIRCUIT.get())
                .define('D', tag(ProcessedMetal.commonItemTag("gems/diamond")))
                .define('M', base)
                .unlockedBy("has_antimatter_circuit", has(ModItems.ANTIMATTER_CIRCUIT.get()))
                .save(output, key(BuiltInRegistries.ITEM.getKey(result).getPath()));
    }

    @Override
    protected void buildRecipes() {
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            // Other mods' metals: only load when some mod provides both the raw ore and the ingot.
            RecipeOutput out = metal.fromOtherMods()
                    ? output.withConditions(
                            new NotCondition(new TagEmptyCondition<>(metal.rawTag())),
                            new NotCondition(new TagEmptyCondition<>(metal.ingotTag())))
                    : output;

            // raw ore -> 2 granulated ore
            out.accept(key("granulating/raw_" + metal.name()),
                    new GranulatingRecipe(tag(metal.rawTag()), new ItemStackTemplate(metal.granulated(), 2)),
                    null);

            // raw ore or ore block -> 3 washed ore (Ore Washer); ore blocks only count when some mod has them
            out.accept(key("washing/raw_" + metal.name()),
                    new WashingRecipe(tag(metal.rawTag()), new ItemStackTemplate(metal.washed(), 3)),
                    null);
            out.withConditions(new NotCondition(new TagEmptyCondition<>(metal.oreTag()))).accept(key("washing/ore_" + metal.name()),
                    new WashingRecipe(tag(metal.oreTag()), new ItemStackTemplate(metal.washed(), 3)),
                    null);
            // tier 6, the 5x step: raw ore or ore block -> 5 ingots in one go (Singularity Processor), with rare by-products
            out.accept(key("singularity/raw_" + metal.name()),
                    new SingularityRecipe(tag(metal.rawTag()), new TagResult(metal.ingotTag(), 5), singularityByproducts()),
                    null);
            out.withConditions(new NotCondition(new TagEmptyCondition<>(metal.oreTag()))).accept(key("singularity/ore_" + metal.name()),
                    new SingularityRecipe(tag(metal.oreTag()), new TagResult(metal.ingotTag(), 5), singularityByproducts()),
                    null);
            // tier 5, the 4x step: raw ore or ore block -> 4 Ore Concentrate (Froth Flotation Cell); 1 concentrate -> 1 washed (Ore Washer)
            out.accept(key("flotation/raw_" + metal.name()),
                    new FlotationRecipe(tag(metal.rawTag()), new ItemStackTemplate(metal.concentrate(), 4)),
                    null);
            out.withConditions(new NotCondition(new TagEmptyCondition<>(metal.oreTag()))).accept(key("flotation/ore_" + metal.name()),
                    new FlotationRecipe(tag(metal.oreTag()), new ItemStackTemplate(metal.concentrate(), 4)),
                    null);
            out.accept(key("washing/concentrate_" + metal.name()),
                    new WashingRecipe(Ingredient.of(metal.concentrate()), new ItemStackTemplate(metal.washed())),
                    null);
            // washed ore -> 1 granulated ore, Electric and Plasma Granulators only (tier 2+); purified ore the same
            out.accept(key("granulating/washed_" + metal.name()),
                    new GranulatingRecipe(Ingredient.of(metal.washed()), new ItemStackTemplate(metal.granulated()), 2),
                    null);
            out.accept(key("granulating/purified_" + metal.name()),
                    new GranulatingRecipe(Ingredient.of(metal.purified()), new ItemStackTemplate(metal.granulated()), 2),
                    null);
            purifying(out, metal);

            // granulated ore -> ingot (vanilla furnace, Coal/Electric Smelter, blast furnace)
            Ingredient granulated = Ingredient.of(metal.granulated());
            Recipe.CommonInfo info = RecipeBuilder.createCraftingCommonInfo(true);
            ResourceKey<Recipe<?>> smeltingKey = key("smelting/" + metal.name() + "_ingot_from_granulated");
            ResourceKey<Recipe<?>> blastingKey = key("blasting/" + metal.name() + "_ingot_from_granulated");
            if (metal.fromOtherMods()) {
                TagResult ingot = new TagResult(metal.ingotTag(), 1);
                out.accept(smeltingKey, new TagSmeltingRecipe(info, cookingInfo(), granulated, ingot, metal.experience(), SMELTING_TIME), null);
                out.accept(blastingKey, new TagBlastingRecipe(info, cookingInfo(), granulated, ingot, metal.experience(), BLASTING_TIME), null);
            } else {
                ItemStackTemplate ingot = new ItemStackTemplate(metal.ingot().builtInRegistryHolder(), 1);
                out.accept(smeltingKey, new SmeltingRecipe(info, cookingInfo(), granulated, ingot, metal.experience(), SMELTING_TIME), null);
                out.accept(blastingKey, new BlastingRecipe(info, cookingInfo(), granulated, ingot, metal.experience(), BLASTING_TIME), null);
            }
        }

        shaped(RecipeCategory.MISC, ModItems.COAL_GRANULATOR.get())
                .pattern("FIF")
                .pattern("CXC")
                .pattern("CCC")
                .define('F', Items.FLINT)
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('C', tag(ProcessedMetal.commonItemTag("cobblestones")))
                .define('X', Items.FURNACE)
                .unlockedBy("has_furnace", has(Items.FURNACE))
                .save(output, key("coal_granulator"));

        shaped(RecipeCategory.MISC, ModItems.COAL_SMELTER.get())
                .pattern("BBB")
                .pattern("BXB")
                .pattern("CCC")
                .define('B', Items.BRICK)
                .define('C', tag(ProcessedMetal.commonItemTag("cobblestones")))
                .define('X', Items.FURNACE)
                .unlockedBy("has_furnace", has(Items.FURNACE))
                .save(output, key("coal_smelter"));

        // ---- Tier 2 ----

        // The Electronic Circuit unlocks tier 2: every electric block except the cable needs one.
        shaped(RecipeCategory.MISC, ModItems.ELECTRONIC_CIRCUIT.get())
                .pattern(" R ")
                .pattern("CCC")
                .pattern(" R ")
                .define('R', tag(ProcessedMetal.commonItemTag("dusts/redstone")))
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output, key("electronic_circuit"));

        shaped(RecipeCategory.MISC, ModItems.ELECTRIC_GENERATOR.get())
                .pattern("IEI")
                .pattern("IFI")
                .pattern("CRC")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('F', Items.FURNACE)
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .define('R', tag(ProcessedMetal.commonItemTag("dusts/redstone")))
                .unlockedBy("has_electronic_circuit", has(ModItems.ELECTRONIC_CIRCUIT.get()))
                .save(output, key("electric_generator"));

        shaped(RecipeCategory.MISC, ModItems.ENERGY_BATTERY.get())
                .pattern("IRI")
                .pattern("CEC")
                .pattern("IRI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('R', tag(ProcessedMetal.commonItemTag("storage_blocks/redstone")))
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .unlockedBy("has_electronic_circuit", has(ModItems.ELECTRONIC_CIRCUIT.get()))
                .save(output, key("energy_battery"));

        shaped(RecipeCategory.MISC, ModItems.ENERGY_CABLE.get(), 8)
                .pattern("CCC")
                .pattern("RRR")
                .pattern("CCC")
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('R', tag(ProcessedMetal.commonItemTag("dusts/redstone")))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output, key("energy_cable"));

        // Electric machines are upgrades of the coal ones
        shaped(RecipeCategory.MISC, ModItems.ELECTRIC_GRANULATOR.get())
                .pattern("IEI")
                .pattern("CMC")
                .pattern("IRI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('M', ModItems.COAL_GRANULATOR.get())
                .define('R', tag(ProcessedMetal.commonItemTag("dusts/redstone")))
                .unlockedBy("has_coal_granulator", has(ModItems.COAL_GRANULATOR.get()))
                .save(output, key("electric_granulator"));

        shaped(RecipeCategory.MISC, ModItems.ELECTRIC_SMELTER.get())
                .pattern("IEI")
                .pattern("CMC")
                .pattern("IRI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('M', ModItems.COAL_SMELTER.get())
                .define('R', tag(ProcessedMetal.commonItemTag("dusts/redstone")))
                .unlockedBy("has_coal_smelter", has(ModItems.COAL_SMELTER.get()))
                .save(output, key("electric_smelter"));

        // ---- Tier 3 ----

        shaped(RecipeCategory.MISC, ModItems.PLASMA_GENERATOR.get())
                .pattern("IGI")
                .pattern("EXE")
                .pattern("ICI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .define('X', ModItems.ELECTRIC_GENERATOR.get())
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .unlockedBy("has_electric_generator", has(ModItems.ELECTRIC_GENERATOR.get()))
                .save(output, key("plasma_generator"));

        // The power chain. Everything up to the plasma is buildable with Electronic Circuits (an Electric Generator
        // powers it at first); the MHD Generator, which pays it back, needs Plasma Circuits.
        shaped(RecipeCategory.MISC, ModItems.ATMOSPHERIC_WATER_GENERATOR.get())
                .pattern("IGI")
                .pattern("EBE")
                .pattern("ICI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .define('B', Items.BUCKET)
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .unlockedBy("has_electronic_circuit", has(ModItems.ELECTRONIC_CIRCUIT.get()))
                .save(output, key("atmospheric_water_generator"));

        shaped(RecipeCategory.MISC, ModItems.ELECTROLYSER.get())
                .pattern("IEI")
                .pattern("CWC")
                .pattern("IGI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('W', Items.WATER_BUCKET)
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .unlockedBy("has_electronic_circuit", has(ModItems.ELECTRONIC_CIRCUIT.get()))
                .save(output, key("electrolyser"));

        shaped(RecipeCategory.MISC, ModItems.MHD_GENERATOR.get())
                .pattern("IPI")
                .pattern("CXC")
                .pattern("IPI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('P', ModItems.PLASMA_CIRCUIT.get())
                .define('C', tag(ProcessedMetal.commonItemTag("storage_blocks/copper")))
                .define('X', ModItems.ELECTRIC_GENERATOR.get())
                .unlockedBy("has_plasma_circuit", has(ModItems.PLASMA_CIRCUIT.get()))
                .save(output, key("mhd_generator"));

        // ---- Tier 4 ----

        // The "plasma-forged parts": the Circuit Assembler forges plating and fusion circuits with plasma.
        output.accept(key("assembling/reactor_plating"),
                new AssemblingRecipe(tag(ProcessedMetal.commonItemTag("storage_blocks/iron")), new ItemStackTemplate(ModItems.REACTOR_PLATING.get(), 2)),
                null);
        output.accept(key("assembling/fusion_circuit"),
                new AssemblingRecipe(Ingredient.of(ModItems.PLASMA_CIRCUIT.get()), new ItemStackTemplate(ModItems.FUSION_CIRCUIT.get())),
                null);
        // Tier 5: a Fusion Circuit + 10 mB antimatter -> Antimatter Circuit
        output.accept(key("assembling/antimatter_circuit"),
                new AssemblingRecipe(Ingredient.of(ModItems.FUSION_CIRCUIT.get()), new ItemStackTemplate(ModItems.ANTIMATTER_CIRCUIT.get()), 10),
                null);
        // Tier 6: an Antimatter Circuit + 50 mB antimatter -> Singularity Circuit
        output.accept(key("assembling/singularity_circuit"),
                new AssemblingRecipe(Ingredient.of(ModItems.ANTIMATTER_CIRCUIT.get()), new ItemStackTemplate(ModItems.SINGULARITY_CIRCUIT.get()), 50),
                null);
        // A fuel pellet is an iron-nugget casing filled with deuterium and tritium in the Pellet Press.
        output.accept(key("pressing/fusion_fuel_pellet"),
                new PressingRecipe(tag(ProcessedMetal.commonItemTag("nuggets/iron")), new ItemStackTemplate(ModItems.FUSION_FUEL_PELLET.get())),
                null);

        shaped(RecipeCategory.MISC, ModItems.ISOTOPE_SEPARATOR.get())
                .pattern("RPR")
                .pattern("GEG")
                .pattern("RPR")
                .define('R', ModItems.REACTOR_PLATING.get())
                .define('P', ModItems.PLASMA_CIRCUIT.get())
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .define('E', ModItems.ELECTROLYSER.get())
                .unlockedBy("has_reactor_plating", has(ModItems.REACTOR_PLATING.get()))
                .save(output, key("isotope_separator"));

        shaped(RecipeCategory.MISC, ModItems.TRITIUM_BREEDER.get())
                .pattern("RFR")
                .pattern("PXP")
                .pattern("RFR")
                .define('R', ModItems.REACTOR_PLATING.get())
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('P', ModItems.PLASMA_CIRCUIT.get())
                .define('X', ModItems.PLASMA_GENERATOR.get())
                .unlockedBy("has_fusion_circuit", has(ModItems.FUSION_CIRCUIT.get()))
                .save(output, key("tritium_breeder"));

        shaped(RecipeCategory.MISC, ModItems.PELLET_PRESS.get())
                .pattern("RFR")
                .pattern("PXP")
                .pattern("RFR")
                .define('R', ModItems.REACTOR_PLATING.get())
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('P', Items.PISTON)
                .define('X', ModItems.CIRCUIT_ASSEMBLER.get())
                .unlockedBy("has_fusion_circuit", has(ModItems.FUSION_CIRCUIT.get()))
                .save(output, key("pellet_press"));

        // The Fusion Reactor and the fast power blocks
        shaped(RecipeCategory.MISC, ModItems.ORE_PURIFIER.get())
                .pattern("RFR")
                .pattern("GWG")
                .pattern("RFR")
                .define('R', ModItems.REACTOR_PLATING.get())
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .define('W', ModItems.ORE_WASHER.get())
                .unlockedBy("has_fusion_circuit", has(ModItems.FUSION_CIRCUIT.get()))
                .save(output, key("ore_purifier"));

        shaped(RecipeCategory.MISC, ModItems.REACTOR_CASING.get(), 2)
                .pattern("PP")
                .pattern("PP")
                .define('P', ModItems.REACTOR_PLATING.get())
                .unlockedBy("has_reactor_plating", has(ModItems.REACTOR_PLATING.get()))
                .save(output, key("reactor_casing"));

        shaped(RecipeCategory.MISC, ModItems.REACTOR_GLASS.get(), 2)
                .pattern("GPG")
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .define('P', ModItems.REACTOR_PLATING.get())
                .unlockedBy("has_reactor_plating", has(ModItems.REACTOR_PLATING.get()))
                .save(output, key("reactor_glass"));

        shaped(RecipeCategory.MISC, ModItems.REACTOR_PORT.get())
                .pattern(" H ")
                .pattern("FCF")
                .pattern(" B ")
                .define('H', Items.HOPPER)
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('C', ModItems.REACTOR_CASING.get())
                .define('B', Items.BUCKET)
                .unlockedBy("has_reactor_casing", has(ModItems.REACTOR_CASING.get()))
                .save(output, key("reactor_port"));

        shaped(RecipeCategory.MISC, ModItems.REACTOR_CONTROLLER.get())
                .pattern("CFC")
                .pattern("FMF")
                .pattern("CFC")
                .define('C', ModItems.REACTOR_CASING.get())
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('M', ModItems.MHD_GENERATOR.get())
                .unlockedBy("has_reactor_casing", has(ModItems.REACTOR_CASING.get()))
                .save(output, key("reactor_controller"));

        shaped(RecipeCategory.MISC, ModItems.FUSION_CABLE.get(), 8)
                .pattern("CCC")
                .pattern("GFG")
                .pattern("CCC")
                .define('C', ModItems.ENERGY_CABLE.get())
                .define('G', tag(ProcessedMetal.commonItemTag("ingots/gold")))
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .unlockedBy("has_fusion_circuit", has(ModItems.FUSION_CIRCUIT.get()))
                .save(output, key("fusion_cable"));

        shaped(RecipeCategory.MISC, ModItems.FUSION_ENERGY_BATTERY.get())
                .pattern("BFB")
                .pattern("RXR")
                .pattern("BFB")
                .define('R', ModItems.PLASMA_ENERGY_BATTERY.get())
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('B', ModItems.REACTOR_PLATING.get())
                .define('X', tag(ProcessedMetal.commonItemTag("storage_blocks/redstone")))
                .unlockedBy("has_fusion_circuit", has(ModItems.FUSION_CIRCUIT.get()))
                .save(output, key("fusion_energy_battery"));

        // Tier 5 ore machines: each is its tier-3/4 machine wrapped in reactor plating with Antimatter Circuits
        antimatterMachine(ModItems.ANTIMATTER_GRANULATOR.get(), ModItems.PLASMA_GRANULATOR.get());
        antimatterMachine(ModItems.ANTIMATTER_SMELTER.get(), ModItems.PLASMA_SMELTER.get());
        antimatterMachine(ModItems.ANTIMATTER_WASHER.get(), ModItems.ORE_WASHER.get());
        antimatterMachine(ModItems.ANTIMATTER_PURIFIER.get(), ModItems.ORE_PURIFIER.get());
        shaped(RecipeCategory.MISC, ModItems.FROTH_FLOTATION_CELL.get())
                .pattern("PAP")
                .pattern("GWG")
                .pattern("PAP")
                .define('P', ModItems.REACTOR_PLATING.get())
                .define('A', ModItems.ANTIMATTER_CIRCUIT.get())
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .define('W', ModItems.ORE_WASHER.get())
                .unlockedBy("has_antimatter_circuit", has(ModItems.ANTIMATTER_CIRCUIT.get()))
                .save(output, key("froth_flotation_cell"));

        // Tier 6 phase B: the Singularity Processor, Cable and Energy Battery (the last two need Singularity Shards)
        shaped(RecipeCategory.MISC, ModItems.SINGULARITY_PROCESSOR.get())
                .pattern("FSF")
                .pattern("GXG")
                .pattern("FSF")
                .define('F', ModItems.CONTAINMENT_FRAME.get())
                .define('S', ModItems.SINGULARITY_CIRCUIT.get())
                .define('G', tag(ProcessedMetal.commonItemTag("storage_blocks/gold")))
                .define('X', ModItems.ANTIMATTER_GRANULATOR.get())
                .unlockedBy("has_singularity_circuit", has(ModItems.SINGULARITY_CIRCUIT.get()))
                .save(output, key("singularity_processor"));
        shaped(RecipeCategory.MISC, ModItems.SINGULARITY_CABLE.get(), 8)
                .pattern("CCC")
                .pattern("HSH")
                .pattern("CCC")
                .define('C', ModItems.ANTIMATTER_CABLE.get())
                .define('H', ModItems.SINGULARITY_SHARD.get())
                .define('S', ModItems.SINGULARITY_CIRCUIT.get())
                .unlockedBy("has_singularity_shard", has(ModItems.SINGULARITY_SHARD.get()))
                .save(output, key("singularity_cable"));
        shaped(RecipeCategory.MISC, ModItems.SINGULARITY_ENERGY_BATTERY.get())
                .pattern("BHB")
                .pattern("SFS")
                .pattern("BHB")
                .define('S', ModItems.ANTIMATTER_ENERGY_BATTERY.get())
                .define('H', ModItems.SINGULARITY_SHARD.get())
                .define('B', ModItems.SINGULARITY_CIRCUIT.get())
                .define('F', ModItems.CONTAINMENT_FRAME.get())
                .unlockedBy("has_singularity_shard", has(ModItems.SINGULARITY_SHARD.get()))
                .save(output, key("singularity_energy_battery"));

        // Tier 6: the Event Horizon Reactor
        shaped(RecipeCategory.MISC, ModItems.CONTAINMENT_FRAME.get(), 8)
                .pattern("PGP")
                .pattern("GAG")
                .pattern("PGP")
                .define('P', ModItems.REACTOR_PLATING.get())
                .define('G', tag(ProcessedMetal.commonItemTag("ingots/gold")))
                .define('A', ModItems.ANTIMATTER_CIRCUIT.get())
                .unlockedBy("has_antimatter_circuit", has(ModItems.ANTIMATTER_CIRCUIT.get()))
                .save(output, key("containment_frame"));
        shaped(RecipeCategory.MISC, ModItems.HORIZON_CONTROLLER.get())
                .pattern("FAF")
                .pattern("AXA")
                .pattern("FAF")
                .define('F', ModItems.CONTAINMENT_FRAME.get())
                .define('A', ModItems.ANTIMATTER_CIRCUIT.get())
                .define('X', ModItems.ANNIHILATION_GENERATOR.get())
                .unlockedBy("has_containment_frame", has(ModItems.CONTAINMENT_FRAME.get()))
                .save(output, key("horizon_controller"));
        shaped(RecipeCategory.MISC, ModItems.HORIZON_PORT.get())
                .pattern(" A ")
                .pattern("MFC")
                .pattern(" A ")
                .define('A', ModItems.ANTIMATTER_CIRCUIT.get())
                .define('M', ModItems.MAGNETIC_CONDUIT.get())
                .define('F', ModItems.CONTAINMENT_FRAME.get())
                .define('C', ModItems.ANTIMATTER_CABLE.get())
                .unlockedBy("has_containment_frame", has(ModItems.CONTAINMENT_FRAME.get()))
                .save(output, key("horizon_port"));

        // Tier 5 power: the Annihilation Generator, Antimatter Cable and Antimatter Energy Battery
        shaped(RecipeCategory.MISC, ModItems.ANNIHILATION_GENERATOR.get())
                .pattern("PAP")
                .pattern("CMC")
                .pattern("PAP")
                .define('P', ModItems.REACTOR_PLATING.get())
                .define('A', ModItems.ANTIMATTER_CIRCUIT.get())
                .define('C', ModItems.CONTAINMENT_CELL.get())
                .define('M', ModItems.MHD_GENERATOR.get())
                .unlockedBy("has_antimatter_circuit", has(ModItems.ANTIMATTER_CIRCUIT.get()))
                .save(output, key("annihilation_generator"));

        shaped(RecipeCategory.MISC, ModItems.ANTIMATTER_CABLE.get(), 8)
                .pattern("CCC")
                .pattern("MAM")
                .pattern("CCC")
                .define('C', ModItems.FUSION_CABLE.get())
                .define('M', ModItems.MAGNETIC_CONDUIT.get())
                .define('A', ModItems.ANTIMATTER_CIRCUIT.get())
                .unlockedBy("has_antimatter_circuit", has(ModItems.ANTIMATTER_CIRCUIT.get()))
                .save(output, key("antimatter_cable"));

        shaped(RecipeCategory.MISC, ModItems.ANTIMATTER_ENERGY_BATTERY.get())
                .pattern("BAB")
                .pattern("DCD")
                .pattern("BAB")
                .define('D', ModItems.FUSION_ENERGY_BATTERY.get())
                .define('A', ModItems.ANTIMATTER_CIRCUIT.get())
                .define('B', tag(ProcessedMetal.commonItemTag("storage_blocks/diamond")))
                .define('C', ModItems.CONTAINMENT_CELL.get())
                .unlockedBy("has_antimatter_circuit", has(ModItems.ANTIMATTER_CIRCUIT.get()))
                .save(output, key("antimatter_energy_battery"));

        // The Circuit Assembler makes Plasma Circuits, so it can't need one itself.
        output.accept(key("assembling/plasma_circuit"),
                new AssemblingRecipe(Ingredient.of(ModItems.ELECTRONIC_CIRCUIT.get()), new ItemStackTemplate(ModItems.PLASMA_CIRCUIT.get())),
                null);

        shaped(RecipeCategory.MISC, ModItems.CIRCUIT_ASSEMBLER.get())
                .pattern("IPI")
                .pattern("EXE")
                .pattern("IGI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('P', Items.PISTON)
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .define('X', Items.CRAFTING_TABLE)
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .unlockedBy("has_plasma_generator", has(ModItems.PLASMA_GENERATOR.get()))
                .save(output, key("circuit_assembler"));

        shaped(RecipeCategory.MISC, ModItems.ORE_WASHER.get())
                .pattern("IBI")
                .pattern("PMP")
                .pattern("III")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('B', Items.WATER_BUCKET)
                .define('P', ModItems.PLASMA_CIRCUIT.get())
                .define('M', ModItems.PLASMA_GRANULATOR.get())
                .unlockedBy("has_plasma_granulator", has(ModItems.PLASMA_GRANULATOR.get()))
                .save(output, key("ore_washer"));

        // Tier-3 blocks are upgrades of the tier-2 ones and need Plasma Circuits (made in the Circuit Assembler)
        shaped(RecipeCategory.MISC, ModItems.PLASMA_GRANULATOR.get())
                .pattern("IPI")
                .pattern("GMG")
                .pattern("IPI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('P', ModItems.PLASMA_CIRCUIT.get())
                .define('G', tag(ProcessedMetal.commonItemTag("ingots/gold")))
                .define('M', ModItems.ELECTRIC_GRANULATOR.get())
                .unlockedBy("has_plasma_circuit", has(ModItems.PLASMA_CIRCUIT.get()))
                .save(output, key("plasma_granulator"));

        shaped(RecipeCategory.MISC, ModItems.PLASMA_SMELTER.get())
                .pattern("IPI")
                .pattern("GMG")
                .pattern("IPI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('P', ModItems.PLASMA_CIRCUIT.get())
                .define('G', tag(ProcessedMetal.commonItemTag("ingots/gold")))
                .define('M', ModItems.ELECTRIC_SMELTER.get())
                .unlockedBy("has_plasma_circuit", has(ModItems.PLASMA_CIRCUIT.get()))
                .save(output, key("plasma_smelter"));

        shaped(RecipeCategory.MISC, ModItems.PLASMA_ENERGY_BATTERY.get())
                .pattern("BPB")
                .pattern("DXD")
                .pattern("BPB")
                .define('D', ModItems.ENERGY_BATTERY.get())
                .define('P', ModItems.PLASMA_CIRCUIT.get())
                .define('B', tag(ProcessedMetal.commonItemTag("gems/diamond")))
                .define('X', tag(ProcessedMetal.commonItemTag("storage_blocks/redstone")))
                .unlockedBy("has_plasma_circuit", has(ModItems.PLASMA_CIRCUIT.get()))
                .save(output, key("plasma_energy_battery"));

        shaped(RecipeCategory.MISC, ModItems.PLASMA_CABLE.get(), 8)
                .pattern("CCC")
                .pattern("GPG")
                .pattern("CCC")
                .define('C', ModItems.FLUID_PIPE.get())
                .define('G', tag(ProcessedMetal.commonItemTag("ingots/gold")))
                .define('P', ModItems.PLASMA_CIRCUIT.get())
                .unlockedBy("has_plasma_circuit", has(ModItems.PLASMA_CIRCUIT.get()))
                .save(output, key("plasma_cable"));

        // Tier 5 containment: copper magnet coils round fusion-tier parts
        shaped(RecipeCategory.MISC, ModItems.CONTAINMENT_CELL.get())
                .pattern("PCP")
                .pattern("FTF")
                .pattern("PCP")
                .define('P', ModItems.REACTOR_PLATING.get())
                .define('C', tag(ProcessedMetal.commonItemTag("storage_blocks/copper")))
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('T', ModItems.FLUID_TANK.get())
                .unlockedBy("has_fusion_circuit", has(ModItems.FUSION_CIRCUIT.get()))
                .save(output, key("containment_cell"));

        shaped(RecipeCategory.MISC, ModItems.MAGNETIC_CONDUIT.get(), 8)
                .pattern("PCP")
                .pattern("CFC")
                .pattern("PCP")
                .define('P', ModItems.FLUID_PIPE.get())
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .unlockedBy("has_fusion_circuit", has(ModItems.FUSION_CIRCUIT.get()))
                .save(output, key("magnetic_conduit"));

        // The Particle Accelerator: magnet coils (copper blocks) round a Magnetic Conduit beam tube
        shaped(RecipeCategory.MISC, ModItems.ACCELERATOR_SEGMENT.get(), 8)
                .pattern("PCP")
                .pattern("CMC")
                .pattern("PCP")
                .define('P', ModItems.REACTOR_PLATING.get())
                .define('C', tag(ProcessedMetal.commonItemTag("storage_blocks/copper")))
                .define('M', ModItems.MAGNETIC_CONDUIT.get())
                .unlockedBy("has_magnetic_conduit", has(ModItems.MAGNETIC_CONDUIT.get()))
                .save(output, key("accelerator_segment"));

        shaped(RecipeCategory.MISC, ModItems.ACCELERATOR_PORT.get())
                .pattern(" F ")
                .pattern("PSC")
                .pattern(" F ")
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('P', ModItems.FLUID_PIPE.get())
                .define('S', ModItems.ACCELERATOR_SEGMENT.get())
                .define('C', ModItems.FUSION_CABLE.get())
                .unlockedBy("has_accelerator_segment", has(ModItems.ACCELERATOR_SEGMENT.get()))
                .save(output, key("accelerator_port"));

        shaped(RecipeCategory.MISC, ModItems.ACCELERATOR_CONTROLLER.get())
                .pattern("FSF")
                .pattern("SXS")
                .pattern("FSF")
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('S', ModItems.ACCELERATOR_SEGMENT.get())
                .define('X', ModItems.CONTAINMENT_CELL.get())
                .unlockedBy("has_accelerator_segment", has(ModItems.ACCELERATOR_SEGMENT.get()))
                .save(output, key("accelerator_controller"));

        // Machine upgrades: an Electronic Circuit in redstone, with the upgrade's own ingredient
        upgrade(output, ModItems.SPEED_UPGRADE.get(), Items.SUGAR, "speed_upgrade");
        upgrade(output, ModItems.ENERGY_UPGRADE.get(), Items.GOLD_INGOT, "energy_upgrade");
        upgrade(output, ModItems.GAS_UPGRADE.get(), Items.GLASS_BOTTLE, "gas_upgrade");
        upgrade(output, ModItems.LIQUID_UPGRADE.get(), Items.LAPIS_LAZULI, "liquid_upgrade");

        shaped(RecipeCategory.MISC, ModItems.AIR_FILTER.get())
                .pattern(" P ")
                .pattern("PCP")
                .pattern(" P ")
                .define('P', Items.PAPER)
                .define('C', Items.CHARCOAL)
                .unlockedBy("has_charcoal", has(Items.CHARCOAL))
                .save(output, key("air_filter"));

        shaped(RecipeCategory.MISC, ModItems.FLUID_PIPE.get(), 8)
                .pattern("III")
                .pattern("GGG")
                .pattern("III")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .unlockedBy("has_plasma_generator", has(ModItems.PLASMA_GENERATOR.get()))
                .save(output, key("fluid_pipe"));

        shaped(RecipeCategory.MISC, ModItems.FLUID_TANK.get())
                .pattern("IGI")
                .pattern("GBG")
                .pattern("IGI")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('G', tag(ProcessedMetal.commonItemTag("glass_blocks")))
                .define('B', Items.BUCKET)
                .unlockedBy("has_bucket", has(Items.BUCKET))
                .save(output, key("fluid_tank"));

        shaped(RecipeCategory.TOOLS, ModItems.WRENCH.get())
                .pattern("I I")
                .pattern(" C ")
                .pattern(" I ")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output, key("wrench"));

        shapeless(RecipeCategory.MISC, ModItems.TINY_COAL.get(), 8)
                .requires(Items.COAL)
                .unlockedBy("has_coal", has(Items.COAL))
                .save(output, key("tiny_coal"));

        shapeless(RecipeCategory.MISC, ModItems.SOARYN_CHEST.get())
                .requires(Items.CHEST)
                .requires(Items.HOPPER)
                .unlockedBy("has_hopper", has(Items.HOPPER))
                .save(output, key("soaryn_chest"));

        ModBlocks.STONE_SETS.forEach(this::stoneRecipes);
        purpleheartRecipes();
        toolRecipes();
    }

    // ---- Tools (owner, 2026-11-16) ----

    private void toolRecipes() {
        Ingredient iron = tag(ProcessedMetal.commonItemTag("ingots/iron"));
        Ingredient stick = Ingredient.of(Items.STICK);
        shaped(RecipeCategory.TOOLS, ModItems.ORE_HAMMER.get())
                .pattern("IBI").pattern(" S ").pattern(" S ")
                .define('I', iron).define('B', tag(ProcessedMetal.commonItemTag("storage_blocks/iron"))).define('S', stick)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output, key("ore_hammer"));
        shaped(RecipeCategory.TOOLS, ModItems.DRILL_BIT.get())
                .pattern(" I ").pattern("IFI").pattern(" I ")
                .define('I', iron).define('F', Items.FLINT)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output, key("drill_bit"));
        shaped(RecipeCategory.TOOLS, ModItems.COAL_DRILL.get())
                .pattern(" IB").pattern("IPI").pattern("SI ")
                .define('I', iron).define('B', ModItems.DRILL_BIT.get()).define('P', Items.PISTON).define('S', stick)
                .unlockedBy("has_drill_bit", has(ModItems.DRILL_BIT.get()))
                .save(output, key("coal_drill"));
        output.accept(key("coal_drill_new_bit"), DrillBitRecipe.INSTANCE, null);
        shaped(RecipeCategory.TOOLS, ModItems.PLASMA_CHAINSAW.get())
                .pattern(" IP").pattern("IXI").pattern("GI ")
                .define('I', iron).define('P', ModItems.PLASMA_CIRCUIT.get()).define('X', Items.IRON_AXE)
                .define('G', tag(ProcessedMetal.commonItemTag("ingots/gold")))
                .unlockedBy("has_plasma_circuit", has(ModItems.PLASMA_CIRCUIT.get()))
                .save(output, key("plasma_chainsaw"));

        // each tier: the tool below in the middle, that tier's circuit and parts round it (FE carries over)
        Ingredient electronic = Ingredient.of(ModItems.ELECTRONIC_CIRCUIT.get());
        Ingredient plasma = Ingredient.of(ModItems.PLASMA_CIRCUIT.get());
        Ingredient fusion = Ingredient.of(ModItems.FUSION_CIRCUIT.get());
        Ingredient antimatter = Ingredient.of(ModItems.ANTIMATTER_CIRCUIT.get());
        Ingredient singularity = Ingredient.of(ModItems.SINGULARITY_CIRCUIT.get());
        Ingredient plating = Ingredient.of(ModItems.REACTOR_PLATING.get());
        Ingredient diamondBlock = tag(ProcessedMetal.commonItemTag("storage_blocks/diamond"));
        Ingredient shard = Ingredient.of(ModItems.SINGULARITY_SHARD.get());
        toolUpgrade(ModItems.ELECTRIC_DRILL.get(), ModItems.COAL_DRILL.get(), Map.of('E', electronic,
                'C', tag(ProcessedMetal.commonItemTag("ingots/copper")), 'R', tag(ProcessedMetal.commonItemTag("storage_blocks/redstone"))),
                " E ", "CDC", " R ");
        toolUpgrade(ModItems.PLASMA_DRILL.get(), ModItems.ELECTRIC_DRILL.get(), Map.of('P', plasma,
                'G', tag(ProcessedMetal.commonItemTag("ingots/gold")), 'X', tag(ProcessedMetal.commonItemTag("gems/diamond"))),
                " P ", "GDG", " X ");
        for (var pair : List.of(List.of(ModItems.FUSION_DRILL, ModItems.PLASMA_DRILL), List.of(ModItems.FUSION_CHAINSAW, ModItems.PLASMA_CHAINSAW))) {
            toolUpgrade(pair.get(0).get(), pair.get(1).get(), Map.of('R', plating, 'F', fusion), "RFR", "FDF", "RFR");
        }
        for (var pair : List.of(List.of(ModItems.ANTIMATTER_DRILL, ModItems.FUSION_DRILL), List.of(ModItems.ANTIMATTER_CHAINSAW, ModItems.FUSION_CHAINSAW))) {
            toolUpgrade(pair.get(0).get(), pair.get(1).get(), Map.of('X', diamondBlock, 'A', antimatter), "XAX", "ADA", "XAX");
        }
        for (var pair : List.of(List.of(ModItems.SINGULARITY_DRILL, ModItems.ANTIMATTER_DRILL),
                List.of(ModItems.SINGULARITY_CHAINSAW, ModItems.ANTIMATTER_CHAINSAW))) {
            toolUpgrade(pair.get(0).get(), pair.get(1).get(), Map.of('H', shard, 'S', singularity), "HSH", "SDS", "HSH");
        }

        // Reactor Plating tools: the vanilla shapes; the paxel from the four tools
        ModItems.ToolSet set = ModItems.REACTOR_PLATING_TOOLS;
        shaped(RecipeCategory.TOOLS, set.pickaxe().get()).pattern("XXX").pattern(" S ").pattern(" S ")
                .define('X', plating).define('S', stick).unlockedBy("has_reactor_plating", has(ModItems.REACTOR_PLATING.get()))
                .save(output, key("reactor_plating_pickaxe"));
        shaped(RecipeCategory.TOOLS, set.axe().get()).pattern("XX").pattern("XS").pattern(" S")
                .define('X', plating).define('S', stick).unlockedBy("has_reactor_plating", has(ModItems.REACTOR_PLATING.get()))
                .save(output, key("reactor_plating_axe"));
        shaped(RecipeCategory.TOOLS, set.shovel().get()).pattern("X").pattern("S").pattern("S")
                .define('X', plating).define('S', stick).unlockedBy("has_reactor_plating", has(ModItems.REACTOR_PLATING.get()))
                .save(output, key("reactor_plating_shovel"));
        shaped(RecipeCategory.TOOLS, set.hoe().get()).pattern("XX").pattern(" S").pattern(" S")
                .define('X', plating).define('S', stick).unlockedBy("has_reactor_plating", has(ModItems.REACTOR_PLATING.get()))
                .save(output, key("reactor_plating_hoe"));
        shaped(RecipeCategory.COMBAT, set.sword().get()).pattern("X").pattern("X").pattern("S")
                .define('X', plating).define('S', stick).unlockedBy("has_reactor_plating", has(ModItems.REACTOR_PLATING.get()))
                .save(output, key("reactor_plating_sword"));
        shapeless(RecipeCategory.TOOLS, set.paxel().get())
                .requires(set.pickaxe().get()).requires(set.axe().get()).requires(set.shovel().get()).requires(set.hoe().get())
                .unlockedBy("has_reactor_plating", has(ModItems.REACTOR_PLATING.get()))
                .save(output, key("reactor_plating_paxel"));

        // Singularity tools: a Reactor Plating tool + a Singularity Shard in a smithing table (keeps its enchantments)
        List<DeferredItem<? extends Item>> from = ModItems.REACTOR_PLATING_TOOLS.all();
        List<DeferredItem<? extends Item>> to = ModItems.SINGULARITY_TOOLS.all();
        for (int i = 0; i < from.size(); i++) {
            String name = to.get(i).getId().getPath();
            output.accept(key(name + "_smithing"), new SmithingTransformRecipe(new Recipe.CommonInfo(true), Optional.empty(),
                    Ingredient.of(from.get(i).get()), Optional.of(shard), new ItemStackTemplate(to.get(i).get())), null);
        }
    }

    /** A {@link ToolUpgradeRecipe}: shaped, 'D' = the tool below; the result keeps its FE. */
    private void toolUpgrade(Item result, Item previous, Map<Character, Ingredient> key, String... rows) {
        Map<Character, Ingredient> all = new HashMap<>(key);
        all.put('D', Ingredient.of(previous));
        output.accept(key(BuiltInRegistries.ITEM.getKey(result).getPath()), new ToolUpgradeRecipe(new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.EQUIPMENT, ""), ShapedRecipePattern.of(all, rows),
                new ItemStackTemplate(result)), null);
    }

    // ---- Purpleheart (vanilla's oak recipes) ----

    private void purpleheartRecipes() {
        var wood = ModBlocks.PURPLEHEART;
        planksFromLogs(wood.planks.get(), WoodFamily.logsItemTag(wood), 4);
        woodFromLogs(wood.wood.get(), wood.log.get());
        woodFromLogs(wood.strippedWood.get(), wood.strippedLog.get());
        hangingSign(ModItems.PURPLEHEART_HANGING_SIGN.get(), wood.strippedLog.get());
        woodenBoat(ModItems.PURPLEHEART_BOAT.get(), wood.planks.get());
        chestBoat(ModItems.PURPLEHEART_CHEST_BOAT.get(), ModItems.PURPLEHEART_BOAT.get());
        // stairs, slab, fence, fence gate, door, trapdoor, pressure plate, button, sign
        generateRecipes(WoodFamily.of(wood), FeatureFlags.VANILLA_SET);
    }

    // ---- Decorative stone (same recipes as vanilla stone / granite / deepslate) ----

    private void stoneRecipes(StoneSet set) {
        // Stairs, slabs, walls, button, polished (2x2 stone), chiseled (2 brick slabs),
        // cracked bricks (smelt bricks). Ids come from the result item, so they land in our namespace.
        StoneFamilies.of(set).all().forEach(family -> generateRecipes(family, FeatureFlags.VANILLA_SET));

        smeltingResultFromBase(set.stone.get(), set.cobbled.get());
        smeltingResultFromBase(set.smooth.get(), set.stone.get());

        shaped(RecipeCategory.BUILDING_BLOCKS, set.bricks.get(), 4)
                .define('#', set.polished.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy(getHasName(set.polished.get()), has(set.polished.get()))
                .save(output);
        shaped(RecipeCategory.BUILDING_BLOCKS, set.pillar.get(), 2)
                .define('#', set.polished.get())
                .pattern("#")
                .pattern("#")
                .unlockedBy(getHasName(set.polished.get()), has(set.polished.get()))
                .save(output);

        mossy(set.mossyCobbled.get(), set.cobbled.get());
        mossy(set.mossyBricks.get(), set.bricks.get());

        // Stonecutter: every worked form can be cut from the rougher forms above it.
        stonecutting(set.stone.get(), set.stairs.get(), set.slab.get(),
                set.polished.get(), set.polishedStairs.get(), set.polishedSlab.get(),
                set.bricks.get(), set.brickStairs.get(), set.brickSlab.get(), set.brickWall.get(),
                set.chiseled.get(), set.pillar.get());
        stonecutting(set.polished.get(), set.polishedStairs.get(), set.polishedSlab.get(),
                set.bricks.get(), set.brickStairs.get(), set.brickSlab.get(), set.brickWall.get(),
                set.chiseled.get(), set.pillar.get());
        stonecutting(set.bricks.get(), set.brickStairs.get(), set.brickSlab.get(), set.brickWall.get(), set.chiseled.get());
        stonecutting(set.cobbled.get(), set.cobbledStairs.get(), set.cobbledSlab.get(), set.cobbledWall.get());
        stonecutting(set.mossyCobbled.get(), set.mossyCobbledStairs.get(), set.mossyCobbledSlab.get(), set.mossyCobbledWall.get());
        stonecutting(set.mossyBricks.get(), set.mossyBrickStairs.get(), set.mossyBrickSlab.get(), set.mossyBrickWall.get());
        stonecutting(set.smooth.get(), set.smoothSlab.get());
    }

    /** Mossy variant from the plain block plus a vine or a moss block (like vanilla's mossy cobblestone). */
    private void mossy(Block result, Block base) {
        for (ItemLike moss : new ItemLike[] {Items.VINE, Items.MOSS_BLOCK}) {
            shapeless(RecipeCategory.BUILDING_BLOCKS, result)
                    .requires(base)
                    .requires(moss)
                    .group(getItemName(result))
                    .unlockedBy(getHasName(moss), has(moss))
                    .save(output, key(getItemName(result) + "_from_" + getItemName(moss)));
        }
    }

    /** One stonecutter recipe per result (2 for slabs). Vanilla's helper would put these in the minecraft namespace. */
    private void stonecutting(Block base, Block... results) {
        for (Block result : results) {
            SingleItemRecipeBuilder.stonecutting(Ingredient.of(base), RecipeCategory.BUILDING_BLOCKS, result,
                            result instanceof SlabBlock ? 2 : 1)
                    .unlockedBy(getHasName(base), has(base))
                    .save(output, key(getConversionRecipeName(result, base) + "_stonecutting"));
        }
    }

    private static AbstractCookingRecipe.CookingBookInfo cookingInfo() {
        return new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, "");
    }

    /** A machine upgrade: RXR / XCX / RXR (redstone, the upgrade's ingredient, an Electronic Circuit). */
    private void upgrade(RecipeOutput out, Item upgrade, ItemLike ingredient, String name) {
        shaped(RecipeCategory.MISC, upgrade)
                .pattern("RXR")
                .pattern("XCX")
                .pattern("RXR")
                .define('R', Items.REDSTONE)
                .define('X', ingredient)
                .define('C', ModItems.ELECTRONIC_CIRCUIT.get())
                .unlockedBy("has_electronic_circuit", has(ModItems.ELECTRONIC_CIRCUIT.get()))
                .save(out, key(name));
    }

    private static ResourceKey<Recipe<?>> key(String path) {
        return ResourceKey.create(Registries.RECIPE, ComminutionCraft.id(path));
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "ComminutionCraft Recipes";
        }
    }
}
