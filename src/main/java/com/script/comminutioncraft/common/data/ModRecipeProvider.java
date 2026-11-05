package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.recipe.AssemblingRecipe;
import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
import com.script.comminutioncraft.common.recipe.PressingRecipe;
import com.script.comminutioncraft.common.recipe.PurifyingRecipe;
import java.util.List;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import com.script.comminutioncraft.common.recipe.WashingRecipe;
import com.script.comminutioncraft.common.recipe.TagBlastingRecipe;
import com.script.comminutioncraft.common.recipe.TagResult;
import com.script.comminutioncraft.common.recipe.TagSmeltingRecipe;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

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
                .pattern("III")
                .pattern("IFI")
                .pattern("CEC")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('F', Items.FURNACE)
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
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
                .pattern("III")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('M', ModItems.COAL_GRANULATOR.get())
                .unlockedBy("has_coal_granulator", has(ModItems.COAL_GRANULATOR.get()))
                .save(output, key("electric_granulator"));

        shaped(RecipeCategory.MISC, ModItems.ELECTRIC_SMELTER.get())
                .pattern("IEI")
                .pattern("CMC")
                .pattern("III")
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('E', ModItems.ELECTRONIC_CIRCUIT.get())
                .define('C', tag(ProcessedMetal.commonItemTag("ingots/copper")))
                .define('M', ModItems.COAL_SMELTER.get())
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
                new AssemblingRecipe(tag(ProcessedMetal.commonItemTag("storage_blocks/iron")), new ItemStackTemplate(ModItems.REACTOR_PLATING.get())),
                null);
        output.accept(key("assembling/fusion_circuit"),
                new AssemblingRecipe(Ingredient.of(ModItems.PLASMA_CIRCUIT.get()), new ItemStackTemplate(ModItems.FUSION_CIRCUIT.get())),
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
                .define('B', ModItems.PLASMA_ENERGY_BATTERY.get())
                .define('F', ModItems.FUSION_CIRCUIT.get())
                .define('R', ModItems.REACTOR_PLATING.get())
                .define('X', tag(ProcessedMetal.commonItemTag("storage_blocks/redstone")))
                .unlockedBy("has_fusion_circuit", has(ModItems.FUSION_CIRCUIT.get()))
                .save(output, key("fusion_energy_battery"));

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
                .define('B', ModItems.ENERGY_BATTERY.get())
                .define('P', ModItems.PLASMA_CIRCUIT.get())
                .define('D', tag(ProcessedMetal.commonItemTag("gems/diamond")))
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

        shapeless(RecipeCategory.MISC, ModItems.SOARYN_CHEST.get())
                .requires(Items.CHEST)
                .requires(Items.HOPPER)
                .unlockedBy("has_hopper", has(Items.HOPPER))
                .save(output, key("soaryn_chest"));

        ModBlocks.STONE_SETS.forEach(this::stoneRecipes);
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
