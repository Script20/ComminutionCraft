package com.script.comminutioncraft.gametest;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.cable.FluidPipeBlock;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectrolyserBlockEntity;
import com.script.comminutioncraft.common.machine.MhdGeneratorBlockEntity;
import java.util.List;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.NeoForgeMod;
import com.script.comminutioncraft.common.machine.PlasmaMachineBlockEntity;
import com.script.comminutioncraft.common.machine.OreWasherBlockEntity;
import com.script.comminutioncraft.common.machine.IsotopeSeparatorBlockEntity;
import com.script.comminutioncraft.common.machine.PelletPressBlockEntity;
import com.script.comminutioncraft.common.machine.TritiumBreederBlockEntity;
import com.script.comminutioncraft.common.machine.FusionReactorBlockEntity;
import com.script.comminutioncraft.common.machine.ReactorPortBlockEntity;
import com.script.comminutioncraft.common.machine.OrePurifierBlockEntity;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.script.comminutioncraft.common.registry.ModFluids;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import com.script.comminutioncraft.common.data.ModWorldGen;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricMachineBlockEntity;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlock;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.machine.MachineBlock;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import com.script.comminutioncraft.common.registry.ModItems;
import com.script.comminutioncraft.common.data.ProcessedMetal;
import com.script.comminutioncraft.common.recipe.TagResult;
import com.script.comminutioncraft.common.recipe.TagSmeltingRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import com.script.comminutioncraft.common.item.WrenchMode;
import com.script.comminutioncraft.common.machine.FuelMachineBlockEntity;
import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import com.script.comminutioncraft.common.item.WrenchItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Server-side checks for the tier-2 power system. Run with {@code gradlew runGameTestServer}
 * (or {@code /test runall} in a dev client). Every test builds on a 5x3x5 stone floor
 * (data/comminutioncraft/structure/gametest/floor_5x3x5.nbt, made by tools/gen_test_structure.py);
 * blocks go at y = 1 along z = 2.
 */
public final class ModGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, ComminutionCraft.MODID);

    private static final Identifier FLOOR = ComminutionCraft.id("gametest/floor_5x3x5");

    static {
        TEST_FUNCTIONS.register("generator_powers_machine_through_cable", () -> ModGameTests::generatorPowersMachineThroughCable);
        TEST_FUNCTIONS.register("generator_powers_adjacent_granulator", () -> ModGameTests::generatorPowersAdjacentGranulator);
        TEST_FUNCTIONS.register("wrench_cuts_and_restores_cable", () -> ModGameTests::wrenchCutsAndRestoresCable);
        TEST_FUNCTIONS.register("wrench_rotates_machine", () -> ModGameTests::wrenchRotatesMachine);
        TEST_FUNCTIONS.register("lava_gets_volcanic_basalt_crust", () -> ModGameTests::lavaGetsVolcanicBasaltCrust);
        TEST_FUNCTIONS.register("no_crust_without_lava", () -> ModGameTests::noCrustWithoutLava);
        TEST_FUNCTIONS.register("tag_smelting_gives_tag_item", () -> ModGameTests::tagSmeltingGivesTagItem);
        TEST_FUNCTIONS.register("redstone_high_waits_for_signal", () -> ModGameTests::redstoneHighWaitsForSignal);
        TEST_FUNCTIONS.register("side_config_follows_rotation", () -> ModGameTests::sideConfigFollowsRotation);
        TEST_FUNCTIONS.register("wrench_configure_cycles_face", () -> ModGameTests::wrenchConfigureCyclesFace);
        TEST_FUNCTIONS.register("battery_passes_power_on", () -> ModGameTests::batteryPassesPowerOn);
        TEST_FUNCTIONS.register("pickaxe_drop_forgets_settings", () -> ModGameTests::pickaxeDropForgetsSettings);
        TEST_FUNCTIONS.register("wrench_pickup_keeps_settings", () -> ModGameTests::wrenchPickupKeepsSettings);
        TEST_FUNCTIONS.register("wrench_acts_before_machine_gui", () -> ModGameTests::wrenchActsBeforeMachineGui);
        TEST_FUNCTIONS.register("water_generator_breathes", () -> ModGameTests::waterGeneratorBreathes);
        TEST_FUNCTIONS.register("walled_in_generator_makes_nothing", () -> ModGameTests::walledInGeneratorMakesNothing);
        TEST_FUNCTIONS.register("filter_doubles_water", () -> ModGameTests::filterDoublesWater);
        TEST_FUNCTIONS.register("pipe_carries_water_to_tank", () -> ModGameTests::pipeCarriesWaterToTank);
        TEST_FUNCTIONS.register("humidity_follows_biome", () -> ModGameTests::humidityFollowsBiome);
        TEST_FUNCTIONS.register("power_chain_makes_energy", () -> ModGameTests::powerChainMakesEnergy);
        TEST_FUNCTIONS.register("wrench_pickup_keeps_tank_fluid", () -> ModGameTests::wrenchPickupKeepsTankFluid);
        TEST_FUNCTIONS.register("pipes_and_cables_dont_join", () -> ModGameTests::pipesAndCablesDontJoin);
        TEST_FUNCTIONS.register("side_change_reconnects_cable", () -> ModGameTests::sideChangeReconnectsCable);
        TEST_FUNCTIONS.register("plasma_granulator_runs_on_fe", () -> ModGameTests::plasmaGranulatorRunsOnFe);
        TEST_FUNCTIONS.register("oxygen_leaves_by_byproduct_face", () -> ModGameTests::oxygenLeavesByByproductFace);
        TEST_FUNCTIONS.register("energy_cable_caps_per_tick", () -> ModGameTests::energyCableCapsPerTick);
        TEST_FUNCTIONS.register("plasma_cable_carries_only_plasma", () -> ModGameTests::plasmaCableCarriesOnlyPlasma);
        TEST_FUNCTIONS.register("cable_tiers_dont_join", () -> ModGameTests::cableTiersDontJoin);
        TEST_FUNCTIONS.register("circuit_assembler_makes_plasma_circuit", () -> ModGameTests::circuitAssemblerMakesPlasmaCircuit);
        TEST_FUNCTIONS.register("ore_washer_triples_with_water", () -> ModGameTests::oreWasherTriplesWithWater);
        TEST_FUNCTIONS.register("electric_granulator_takes_washed_ore", () -> ModGameTests::electricGranulatorTakesWashedOre);
        TEST_FUNCTIONS.register("coal_granulator_refuses_washed_ore", () -> ModGameTests::coalGranulatorRefusesWashedOre);
        TEST_FUNCTIONS.register("machines_push_into_the_next", () -> ModGameTests::machinesPushIntoTheNext);
        TEST_FUNCTIONS.register("separator_and_breeder_make_fusion_fuel", () -> ModGameTests::separatorAndBreederMakeFusionFuel);
        TEST_FUNCTIONS.register("pellet_press_makes_a_pellet", () -> ModGameTests::pelletPressMakesAPellet);
        TEST_FUNCTIONS.register("assembler_forges_reactor_parts", () -> ModGameTests::assemblerForgesReactorParts);
        TEST_FUNCTIONS.register("reactor_forms_and_unforms", () -> ModGameTests::reactorFormsAndUnforms);
        TEST_FUNCTIONS.register("reactor_burns_pellets_for_fe", () -> ModGameTests::reactorBurnsPelletsForFe);
        TEST_FUNCTIONS.register("ore_purifier_purifies_with_oxygen", () -> ModGameTests::orePurifierPurifiesWithOxygen);
        TEST_FUNCTIONS.register("granulator_takes_purified_ore", () -> ModGameTests::granulatorTakesPurifiedOre);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(ComminutionCraft.id("default"));
        TEST_FUNCTIONS.getEntries().forEach(function -> event.registerTest(function.getId(),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, function.getId()),
                        new TestData<>(environment, FLOOR, 400, 0, true))));
    }

    // ---- Tests ----

    /** Generator -> 3 cables -> Electric Smelter: raw iron comes out as an ingot, and the cable arms show the right connections. */
    private static void generatorPowersMachineThroughCable(GameTestHelper helper) {
        BlockPos generator = new BlockPos(0, 1, 2);
        BlockPos smelter = new BlockPos(4, 1, 2);
        buildLine(helper, generator, smelter);

        assertConnection(helper, new BlockPos(1, 1, 2), Direction.WEST, CableConnection.MACHINE);
        assertConnection(helper, new BlockPos(1, 1, 2), Direction.EAST, CableConnection.CABLE);
        assertConnection(helper, new BlockPos(3, 1, 2), Direction.EAST, CableConnection.MACHINE);
        assertConnection(helper, new BlockPos(2, 1, 2), Direction.UP, CableConnection.NONE);

        helper.getBlockEntity(generator, ElectricGeneratorBlockEntity.class).setItem(0, new ItemStack(Items.COAL, 2));
        helper.getBlockEntity(smelter, ElectricMachineBlockEntity.class).setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON));

        helper.succeedWhen(() -> {
            ItemStack output = helper.getBlockEntity(smelter, ElectricMachineBlockEntity.class).getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(Items.IRON_INGOT), "Smelter hasn't produced an iron ingot yet");
        });
    }

    /** Generator right next to an Electric Granulator (no cable): raw copper -> 2 granulated copper. */
    private static void generatorPowersAdjacentGranulator(GameTestHelper helper) {
        BlockPos generator = new BlockPos(1, 1, 2);
        BlockPos granulator = new BlockPos(2, 1, 2);
        helper.setBlock(generator, ModBlocks.ELECTRIC_GENERATOR.get());
        helper.setBlock(granulator, ModBlocks.ELECTRIC_GRANULATOR.get());
        helper.getBlockEntity(generator, ElectricGeneratorBlockEntity.class).setItem(0, new ItemStack(Items.COAL));
        helper.getBlockEntity(granulator, ElectricMachineBlockEntity.class).setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_COPPER));

        helper.succeedWhen(() -> {
            ItemStack output = helper.getBlockEntity(granulator, ElectricMachineBlockEntity.class).getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(ModItems.GRANULATED_COPPER.get()) && output.getCount() == 2,
                    "Granulator hasn't produced 2 granulated copper yet (has " + output + ")");
        });
    }

    /**
     * Cutting the middle cable's east arm with the wrench (Cable mode, clicking that arm) stops power reaching
     * the smelter; clicking the far side of the cut restores it and the smelter starts working.
     */
    private static void wrenchCutsAndRestoresCable(GameTestHelper helper) {
        BlockPos generator = new BlockPos(0, 1, 2);
        BlockPos middle = new BlockPos(2, 1, 2);
        BlockPos next = new BlockPos(3, 1, 2);
        BlockPos smelter = new BlockPos(4, 1, 2);
        buildLine(helper, generator, smelter);

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wrench = new ItemStack(ModItems.WRENCH.get());
        wrench.set(ModDataComponents.WRENCH_MODE.get(), WrenchMode.CABLE);
        useWrench(helper, player, wrench, middle, new Vec3(0.4, 0, 0), Direction.EAST);

        assertConnection(helper, middle, Direction.EAST, CableConnection.DISABLED);
        assertConnection(helper, next, Direction.WEST, CableConnection.NONE);

        helper.getBlockEntity(generator, ElectricGeneratorBlockEntity.class).setItem(0, new ItemStack(Items.COAL, 2));
        helper.getBlockEntity(smelter, ElectricMachineBlockEntity.class).setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON));

        helper.runAfterDelay(60, () -> {
            int energy = helper.getBlockEntity(smelter, ElectricMachineBlockEntity.class).getEnergyHandler().getAmountAsInt();
            helper.assertTrue(energy == 0, "Power got through a cut cable (smelter has " + energy + " FE)");

            // Click the other cable's west arm, i.e. the far end of the cut: that restores it too.
            useWrench(helper, player, wrench, next, new Vec3(-0.4, 0, 0), Direction.WEST);
            assertConnection(helper, middle, Direction.EAST, CableConnection.CABLE);
            assertConnection(helper, next, Direction.WEST, CableConnection.CABLE);

            helper.succeedWhen(() -> {
                ItemStack output = helper.getBlockEntity(smelter, ElectricMachineBlockEntity.class).getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(output.is(Items.IRON_INGOT), "Smelter hasn't produced an iron ingot after restoring the cable");
            });
        });
    }

    /** Rotate mode turns a machine clockwise. */
    private static void wrenchRotatesMachine(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ELECTRIC_GRANULATOR.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        useWrench(helper, player, new ItemStack(ModItems.WRENCH.get()), pos, new Vec3(0, 0.5, 0), Direction.UP);

        Direction facing = helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(MachineBlock.FACING);
        helper.assertTrue(facing == Direction.EAST, "Expected the machine to face east after one turn, but it faces " + facing);
        helper.succeed();
    }

    /** The world-gen crust feature turns stone next to lava into volcanic basalt, but not stone 3+ blocks away. */
    private static void lavaGetsVolcanicBasaltCrust(GameTestHelper helper) {
        fillWithStone(helper);
        BlockPos lava = new BlockPos(2, 1, 2);
        helper.setBlock(lava, Blocks.LAVA);
        helper.assertTrue(placeCrust(helper, lava), "Crust feature reported nothing placed next to lava");

        BlockState basalt = ModBlocks.VOLCANIC_BASALT.stone.get().defaultBlockState();
        for (BlockPos touching : new BlockPos[] {new BlockPos(1, 1, 2), new BlockPos(3, 1, 2), new BlockPos(2, 1, 1),
                new BlockPos(2, 1, 3), new BlockPos(2, 2, 2), new BlockPos(2, 0, 2)}) {
            helper.assertBlockState(touching, state -> state == basalt, state -> Component.literal(
                    "Stone touching the lava at " + touching + " should be volcanic basalt, but is " + state));
        }
        // (4, 2, 4) is exactly 3 blocks from the lava; the crust is always thinner than that.
        helper.assertBlockState(new BlockPos(4, 2, 4), state -> state.is(Blocks.STONE),
                state -> Component.literal("Stone 3 blocks from the lava should be untouched, but is " + state));
        helper.assertBlockState(lava, state -> state.is(Blocks.LAVA), state -> Component.literal("The lava itself was replaced"));
        helper.succeed();
    }

    /** With no lava within range the feature changes nothing. */
    private static void noCrustWithoutLava(GameTestHelper helper) {
        fillWithStone(helper);
        BlockPos centre = new BlockPos(2, 1, 2);
        helper.assertFalse(placeCrust(helper, centre), "Crust feature placed blocks with no lava nearby");
        helper.assertBlockState(centre, state -> state.is(Blocks.STONE), state -> Component.literal("Stone changed without lava: " + state));
        helper.succeed();
    }

    /**
     * Tag-result smelting (used for other mods' metals) gives the tag's item. Tested with c:ingots/iron, since
     * modded metals don't exist in the test world. Also checks those metals correctly report themselves unavailable.
     */
    private static void tagSmeltingGivesTagItem(GameTestHelper helper) {
        TagSmeltingRecipe recipe = new TagSmeltingRecipe(RecipeBuilder.createCraftingCommonInfo(true),
                new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, ""),
                Ingredient.of(ModItems.GRANULATED_IRON.get()), new TagResult(ProcessedMetal.commonItemTag("ingots/iron"), 2), 0.7F, 200);
        ItemStack result = recipe.assemble(new SingleRecipeInput(new ItemStack(ModItems.GRANULATED_IRON.get())));
        helper.assertTrue(result.is(Items.IRON_INGOT) && result.getCount() == 2, "Expected 2 iron ingots from c:ingots/iron, got " + result);
        helper.assertTrue(ProcessedMetal.ALL.stream().filter(ProcessedMetal::fromOtherMods).noneMatch(ProcessedMetal::isAvailable),
                "A modded metal reports itself available in a world without that mod");
        helper.succeed();
    }

    /** Redstone mode High: a fuelled Coal Smelter does nothing until it gets a signal, then smelts. */
    private static void redstoneHighWaitsForSignal(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.COAL_SMELTER.get());
        FuelMachineBlockEntity smelter = helper.getBlockEntity(pos, FuelMachineBlockEntity.class);
        smelter.getControls().setRedstone(RedstoneMode.HIGH);
        smelter.setItem(FuelMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON));
        smelter.setItem(FuelMachineBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL));

        helper.runAfterDelay(120, () -> {
            helper.assertTrue(smelter.getItem(FuelMachineBlockEntity.SLOT_OUTPUT).isEmpty(), "Smelted without a redstone signal on High");
            helper.assertTrue(smelter.getItem(FuelMachineBlockEntity.SLOT_FUEL).is(Items.COAL), "Burned fuel without a redstone signal on High");
            helper.setBlock(new BlockPos(3, 1, 2), Blocks.REDSTONE_BLOCK);
            helper.succeedWhen(() -> helper.assertTrue(smelter.getItem(FuelMachineBlockEntity.SLOT_OUTPUT).is(Items.IRON_INGOT),
                    "Powered smelter on High hasn't smelted yet"));
        });
    }

    /** Side settings are relative to the front: a face set to None stays None after the machine is turned. */
    private static void sideConfigFollowsRotation(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ELECTRIC_SMELTER.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        ElectricMachineBlockEntity smelter = helper.getBlockEntity(pos, ElectricMachineBlockEntity.class);
        // Defaults: output below, input elsewhere.
        helper.assertTrue(java.util.Arrays.equals(smelter.getSlotsForFace(Direction.DOWN), new int[] {ElectricMachineBlockEntity.SLOT_OUTPUT}),
                "Bottom should give the output slot by default");

        // Facing north, the machine's left (as you look at its front) is east.
        smelter.getControls().setSide(RelativeSide.LEFT, SideMode.NONE);
        helper.assertTrue(smelter.getSlotsForFace(Direction.EAST).length == 0, "Left side set to None still exposes slots");
        helper.assertFalse(smelter.canPlaceItemThroughFace(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON), Direction.EAST),
                "Items can go in through a None side");

        // Turn it to face east: its left is now south, and east (now its front) is an input again.
        helper.setBlock(pos, helper.getLevel().getBlockState(helper.absolutePos(pos)).setValue(MachineBlock.FACING, Direction.EAST));
        helper.assertTrue(smelter.getSlotsForFace(Direction.SOUTH).length == 0, "None side didn't turn with the machine");
        helper.assertTrue(smelter.canPlaceItemThroughFace(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON), Direction.EAST),
                "The new front should accept input");
        helper.succeed();
    }

    /** Configure mode: right-clicking a machine's top cycles it Input -> Output. */
    private static void wrenchConfigureCyclesFace(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ELECTRIC_GRANULATOR.get());
        ElectricMachineBlockEntity granulator = helper.getBlockEntity(pos, ElectricMachineBlockEntity.class);
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wrench = new ItemStack(ModItems.WRENCH.get());
        wrench.set(ModDataComponents.WRENCH_MODE.get(), WrenchMode.CONFIGURE);
        useWrench(helper, player, wrench, pos, new Vec3(0, 0.5, 0), Direction.UP);

        SideMode top = granulator.getControls().side(RelativeSide.TOP);
        helper.assertTrue(top == SideMode.OUTPUT, "Top should be Output after one Configure click, but is " + top);
        helper.succeed();
    }

    /**
     * Generator -> Energy Battery -> Electric Granulator, all touching: the generator charges the battery through
     * an input face and the battery runs the granulator through the face set to Output. (The granulator doesn't
     * touch the generator, so its power can only have come through the battery.)
     */
    private static void batteryPassesPowerOn(GameTestHelper helper) {
        BlockPos generator = new BlockPos(1, 1, 2);
        BlockPos battery = new BlockPos(2, 1, 2);
        BlockPos granulator = new BlockPos(3, 1, 2);
        helper.setBlock(generator, ModBlocks.ELECTRIC_GENERATOR.get());
        helper.setBlock(battery, ModBlocks.ENERGY_BATTERY.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        helper.setBlock(granulator, ModBlocks.ELECTRIC_GRANULATOR.get());
        EnergyBatteryBlockEntity cell = helper.getBlockEntity(battery, EnergyBatteryBlockEntity.class);
        // Facing north, the battery's left is east (towards the granulator).
        cell.getControls().setSide(RelativeSide.LEFT, SideMode.OUTPUT);
        helper.assertTrue(cell.getEnergyHandler(Direction.WEST) != null, "The battery's input face has no FE capability");

        helper.getBlockEntity(generator, ElectricGeneratorBlockEntity.class).setItem(0, new ItemStack(Items.COAL));
        helper.getBlockEntity(granulator, ElectricMachineBlockEntity.class).setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_COPPER));

        helper.succeedWhen(() -> {
            ItemStack output = helper.getBlockEntity(granulator, ElectricMachineBlockEntity.class).getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(ModItems.GRANULATED_COPPER.get()) && output.getCount() == 2,
                    "Granulator hasn't produced 2 granulated copper through the battery yet (has " + output + ")");
        });
    }

    /** A half-charged, configured battery shows 3 cells; broken normally (not with the wrench) it drops a plain battery. */
    private static void pickaxeDropForgetsSettings(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ENERGY_BATTERY.get());
        EnergyBatteryBlockEntity battery = helper.getBlockEntity(pos, EnergyBatteryBlockEntity.class);
        battery.setEnergy(EnergyBatteryBlockEntity.CAPACITY / 2);
        battery.getControls().setSide(RelativeSide.TOP, SideMode.NONE);

        helper.runAfterDelay(2, () -> {
            int charge = helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(EnergyBatteryBlock.CHARGE);
            helper.assertTrue(charge == 3, "A half-full battery should light 3 cells, but lights " + charge);

            BlockPos absolute = helper.absolutePos(pos);
            helper.getLevel().destroyBlock(absolute, true);
            ItemStack drop = droppedItem(helper, pos, ModItems.ENERGY_BATTERY.get().asItem());
            helper.assertFalse(drop.isEmpty(), "Breaking the battery dropped nothing");
            helper.assertTrue(!drop.has(ModDataComponents.STORED_ENERGY.get()) && !drop.has(ModDataComponents.MACHINE_SETTINGS.get()),
                    "A battery broken without the wrench should forget its charge and settings, but drops " + drop.getComponentsPatch());
            helper.succeed();
        });
    }

    /**
     * Wrench in Configure mode + sneak + right-click: the battery goes into the player's inventory with its FE, side
     * and redstone settings, and a battery placed from that item has them all back. A granulator picked up the same
     * way drops the ore that was in it.
     */
    private static void wrenchPickupKeepsSettings(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 2);
        BlockPos granulatorPos = new BlockPos(3, 1, 2);
        int stored = 123_456;
        helper.setBlock(pos, ModBlocks.ENERGY_BATTERY.get());
        EnergyBatteryBlockEntity battery = helper.getBlockEntity(pos, EnergyBatteryBlockEntity.class);
        battery.setEnergy(stored);
        battery.getControls().setSide(RelativeSide.LEFT, SideMode.NONE);
        battery.getControls().setRedstone(RedstoneMode.HIGH);
        helper.setBlock(granulatorPos, ModBlocks.ELECTRIC_GRANULATOR.get());
        helper.getBlockEntity(granulatorPos, ElectricMachineBlockEntity.class).setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON, 5));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        ItemStack wrench = WrenchItem.withMode(WrenchMode.CONFIGURE);
        useWrench(helper, player, wrench, pos, new Vec3(0, 0.5, 0), Direction.UP);
        useWrench(helper, player, wrench, granulatorPos, new Vec3(0, 0.5, 0), Direction.UP);

        helper.assertBlockNotPresent(ModBlocks.ENERGY_BATTERY.get(), pos);
        helper.assertBlockNotPresent(ModBlocks.ELECTRIC_GRANULATOR.get(), granulatorPos);
        ItemStack item = ItemStack.EMPTY;
        for (ItemStack stack : player.getInventory()) {
            if (stack.is(ModItems.ENERGY_BATTERY.get())) {
                item = stack;
            }
        }
        helper.assertFalse(item.isEmpty(), "The picked-up battery isn't in the player's inventory");
        ItemStack ore = droppedItem(helper, granulatorPos, Items.RAW_IRON);
        helper.assertTrue(ore.getCount() == 5, "The granulator's 5 raw iron should drop when it's picked up, found " + ore);

        helper.setBlock(pos, ModBlocks.ENERGY_BATTERY.get());
        EnergyBatteryBlockEntity placed = helper.getBlockEntity(pos, EnergyBatteryBlockEntity.class);
        placed.applyComponentsFromItemStack(item);
        helper.assertTrue(placed.getEnergy() == stored, "The placed battery has " + placed.getEnergy() + " FE, expected " + stored);
        helper.assertTrue(placed.getControls().side(RelativeSide.LEFT) == SideMode.NONE, "The placed battery lost its side config");
        helper.assertTrue(placed.getControls().redstone() == RedstoneMode.HIGH, "The placed battery lost its redstone mode");
        helper.succeed();
    }

    /**
     * Through the real right-click path (as a player clicking in game): the wrench configures the machine's face
     * instead of the machine opening its GUI.
     */
    @SuppressWarnings("removal") // makeMockServerPlayerInLevel: vanilla has no replacement yet
    private static void wrenchActsBeforeMachineGui(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ELECTRIC_GRANULATOR.get());
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack wrench = WrenchItem.withMode(WrenchMode.CONFIGURE);
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        BlockPos absolute = helper.absolutePos(pos);
        player.gameMode.useItemOn(player, helper.getLevel(), wrench, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute).add(0, 0.5, 0), Direction.UP, absolute, false));

        SideMode top = helper.getBlockEntity(pos, ElectricMachineBlockEntity.class).getControls().side(RelativeSide.TOP);
        helper.assertTrue(top == SideMode.OUTPUT, "Top should be Output after one Configure click, but is " + top);
        helper.assertTrue(player.containerMenu == player.inventoryMenu, "Right-clicking with the wrench opened the machine's GUI");
        helper.succeed();
    }

    /**
     * On the floor with nothing else around, 5 faces breathe: with a filter that's 5 x humidity x 2 mB/t of water
     * (whatever the test world's biome is), and the filter wears 1 per 100 mB made.
     */
    private static void waterGeneratorBreathes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        AtmosphericWaterGeneratorBlockEntity generator = placeWaterGenerator(helper, pos, true);
        float humidity = AtmosphericWaterGeneratorBlockEntity.humidity(helper.getLevel(), helper.absolutePos(pos));
        helper.assertTrue(humidity > 0, "The test world should have some humidity");

        helper.runAfterDelay(40, () -> {
            helper.assertTrue(generator.getIntake() == 5, "On the floor in open air the intake should be 5/6, but is " + generator.getIntake());
            int made = generator.getTank().getAmount();
            float expected = 5 * humidity * 2 * 40;
            helper.assertTrue(Math.abs(made - expected) <= 5 * humidity * 2 * 3 + 1,
                    "Expected about " + expected + " mB of water after 40 ticks, got " + made);
            int wear = generator.getItem(AtmosphericWaterGeneratorBlockEntity.SLOT_FILTER).getDamageValue();
            helper.assertTrue(wear == made / AtmosphericWaterGeneratorBlockEntity.MB_PER_FILTER_WEAR,
                    "The filter should wear 1 per 100 mB (" + made + " mB), but wore " + wear);
            helper.succeed();
        });
    }

    /** Stone on every side: no intake, no water, even with a filter. */
    private static void walledInGeneratorMakesNothing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        AtmosphericWaterGeneratorBlockEntity generator = placeWaterGenerator(helper, pos, true);
        for (Direction direction : Direction.values()) {
            if (direction != Direction.DOWN) {
                helper.setBlock(pos.relative(direction), Blocks.STONE);
            }
        }
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(generator.getIntake() == 0, "A walled-in generator should have no intake, but has " + generator.getIntake());
            helper.assertTrue(generator.getTank().getAmount() == 0, "A walled-in generator made " + generator.getTank().getAmount() + " mB");
            helper.succeed();
        });
    }

    /** Two water generators in the same open air: the one with a filter makes twice as much. */
    private static void filterDoublesWater(GameTestHelper helper) {
        AtmosphericWaterGeneratorBlockEntity filtered = placeWaterGenerator(helper, new BlockPos(1, 1, 1), true);
        AtmosphericWaterGeneratorBlockEntity bare = placeWaterGenerator(helper, new BlockPos(3, 1, 3), false);
        helper.runAfterDelay(60, () -> {
            int full = filtered.getTank().getAmount();
            int half = bare.getTank().getAmount();
            helper.assertTrue(full > 0 && Math.abs(half * 2 - full) <= 4,
                    "With a filter it should make twice as much: " + full + " mB vs " + half + " mB without");
            helper.succeed();
        });
    }

    /** Water generator (output to the east) -> 2 fluid pipes -> Fluid Tank: water ends up in the tank. */
    private static void pipeCarriesWaterToTank(GameTestHelper helper) {
        BlockPos generatorPos = new BlockPos(0, 1, 2);
        BlockPos tankPos = new BlockPos(3, 1, 2);
        AtmosphericWaterGeneratorBlockEntity generator = placeWaterGenerator(helper, generatorPos, true);
        // Facing north, the generator's left is east (towards the pipes).
        generator.getControls().setSide(RelativeSide.LEFT, SideMode.OUTPUT);
        helper.setBlock(tankPos, ModBlocks.FLUID_TANK.get());
        placeConduit(helper, new BlockPos(1, 1, 2), ModBlocks.FLUID_PIPE.get());
        placeConduit(helper, new BlockPos(2, 1, 2), ModBlocks.FLUID_PIPE.get());
        assertConnection(helper, new BlockPos(1, 1, 2), Direction.WEST, CableConnection.MACHINE);
        assertConnection(helper, new BlockPos(2, 1, 2), Direction.EAST, CableConnection.MACHINE);

        FluidTankBlockEntity tank = helper.getBlockEntity(tankPos, FluidTankBlockEntity.class);
        helper.succeedWhen(() -> helper.assertTrue(tank.getTank().getAmount() >= 10 && tank.getTank().getFluid().is(NeoForgeMod.WATER_TYPE.value()),
                "The tank should be filling with water, it has " + tank.getTank().getAmount() + " mB"));
    }

    /** Humidity follows the biome: a desert is nearly dry, a river wetter than plains, a jungle wetter still. */
    private static void humidityFollowsBiome(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        float desert = AtmosphericWaterGeneratorBlockEntity.humidity(biomes.getOrThrow(Biomes.DESERT));
        float plains = AtmosphericWaterGeneratorBlockEntity.humidity(biomes.getOrThrow(Biomes.PLAINS));
        float river = AtmosphericWaterGeneratorBlockEntity.humidity(biomes.getOrThrow(Biomes.RIVER));
        float jungle = AtmosphericWaterGeneratorBlockEntity.humidity(biomes.getOrThrow(Biomes.JUNGLE));
        helper.assertTrue(desert > 0 && desert < 0.1F, "A desert should be nearly dry, is " + desert);
        helper.assertTrue(river > plains, "A river (" + river + ") should be wetter than plains (" + plains + ")");
        helper.assertTrue(jungle > plains, "A jungle (" + jungle + ") should be wetter than plains (" + plains + ")");
        helper.succeed();
    }

    /**
     * Electrolyser -> Plasma Generator -> MHD Generator side by side (each pushing east): water becomes hydrogen (and
     * half as much oxygen, kept in the electrolyser), then plasma, then FE. The MHD Generator also powers the
     * Plasma Generator next to it.
     */
    private static void powerChainMakesEnergy(GameTestHelper helper) {
        BlockPos electrolyserPos = new BlockPos(1, 1, 2);
        BlockPos plasmaPos = new BlockPos(2, 1, 2);
        BlockPos mhdPos = new BlockPos(3, 1, 2);
        for (BlockPos pos : List.of(electrolyserPos, plasmaPos, mhdPos)) {
            helper.setBlock(pos, (pos == electrolyserPos ? ModBlocks.ELECTROLYSER.get() : pos == plasmaPos
                    ? ModBlocks.PLASMA_GENERATOR.get() : ModBlocks.MHD_GENERATOR.get()).defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        }
        ElectrolyserBlockEntity electrolyser = helper.getBlockEntity(electrolyserPos, ElectrolyserBlockEntity.class);
        PlasmaGeneratorBlockEntity plasma = helper.getBlockEntity(plasmaPos, PlasmaGeneratorBlockEntity.class);
        MhdGeneratorBlockEntity mhd = helper.getBlockEntity(mhdPos, MhdGeneratorBlockEntity.class);
        // Facing north, left is east: each one's east face is its Output.
        electrolyser.getControls().setSide(RelativeSide.LEFT, SideMode.OUTPUT);
        plasma.getControls().setSide(RelativeSide.LEFT, SideMode.OUTPUT);
        electrolyser.setEnergy(ElectrolyserBlockEntity.ENERGY_CAPACITY);
        plasma.setEnergy(PlasmaGeneratorBlockEntity.ENERGY_CAPACITY);
        electrolyser.getTanks().get(ElectrolyserBlockEntity.TANK_WATER).fill(FluidResource.of(Fluids.WATER), 2_000);

        helper.succeedWhen(() -> {
            helper.assertTrue(mhd.getRate() > 0, "The MHD Generator isn't making FE yet");
            int oxygen = electrolyser.getTanks().get(ElectrolyserBlockEntity.TANK_OXYGEN).getAmount();
            int waterUsed = 2_000 - electrolyser.getTanks().get(ElectrolyserBlockEntity.TANK_WATER).getAmount();
            helper.assertTrue(oxygen > 0 && Math.abs(oxygen - waterUsed / 4) <= 1,
                    "Oxygen should be half the hydrogen (a quarter of the water used, " + waterUsed + " mB), has " + oxygen);
        });
    }

    /** A tank picked up with the wrench keeps its fluid; one placed from that item has it back. */
    private static void wrenchPickupKeepsTankFluid(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.FLUID_TANK.get());
        helper.getBlockEntity(pos, FluidTankBlockEntity.class).getTank().fill(FluidResource.of(Fluids.WATER), 5_000);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        useWrench(helper, player, WrenchItem.withMode(WrenchMode.CONFIGURE), pos, new Vec3(0, 0.5, 0), Direction.UP);
        helper.assertBlockNotPresent(ModBlocks.FLUID_TANK.get(), pos);
        ItemStack item = ItemStack.EMPTY;
        for (ItemStack stack : player.getInventory()) {
            if (stack.is(ModItems.FLUID_TANK.get())) {
                item = stack;
            }
        }
        helper.assertFalse(item.isEmpty(), "The picked-up tank isn't in the player's inventory");

        helper.setBlock(pos, ModBlocks.FLUID_TANK.get());
        FluidTankBlockEntity placed = helper.getBlockEntity(pos, FluidTankBlockEntity.class);
        placed.applyComponentsFromItemStack(item);
        helper.assertTrue(placed.getTank().getAmount() == 5_000 && placed.getTank().getFluid().is(Fluids.WATER.getFluidType()),
                "The placed tank should hold 5,000 mB of water, it has " + placed.getTank().getStack());
        helper.succeed();
    }

    /** A pipe next to a cable doesn't connect to it (and vice versa). */
    private static void pipesAndCablesDontJoin(GameTestHelper helper) {
        BlockPos cable = new BlockPos(1, 1, 2);
        BlockPos pipe = new BlockPos(2, 1, 2);
        placeConduit(helper, cable, ModBlocks.ENERGY_CABLE.get());
        placeConduit(helper, pipe, ModBlocks.FLUID_PIPE.get());
        assertConnection(helper, cable, Direction.EAST, CableConnection.NONE);
        assertConnection(helper, pipe, Direction.WEST, CableConnection.NONE);
        helper.succeed();
    }

    /** Setting a battery face to None drops the cable's arm there; setting it back reconnects it. */
    private static void sideChangeReconnectsCable(GameTestHelper helper) {
        BlockPos battery = new BlockPos(2, 1, 2);
        BlockPos cable = new BlockPos(3, 1, 2);
        helper.setBlock(battery, ModBlocks.ENERGY_BATTERY.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        placeConduit(helper, cable, ModBlocks.ENERGY_CABLE.get());
        assertConnection(helper, cable, Direction.WEST, CableConnection.MACHINE);

        EnergyBatteryBlockEntity cell = helper.getBlockEntity(battery, EnergyBatteryBlockEntity.class);
        // Facing north, the battery's left is east (towards the cable).
        cell.getControls().setSide(RelativeSide.LEFT, SideMode.NONE);
        assertConnection(helper, cable, Direction.WEST, CableConnection.NONE);
        cell.getControls().setSide(RelativeSide.LEFT, SideMode.INPUT);
        assertConnection(helper, cable, Direction.WEST, CableConnection.MACHINE);
        helper.succeed();
    }

    /** A powered Plasma Granulator needs nothing but FE: 2 raw iron become 4 granulated iron, and it takes no fluid. */
    private static void plasmaGranulatorRunsOnFe(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.PLASMA_GRANULATOR.get());
        ElectricMachineBlockEntity machine = helper.getBlockEntity(pos, ElectricMachineBlockEntity.class);
        ((MachineEnergyStorage) machine.getEnergyHandler()).set(ElectricMachineBlockEntity.TIER3_ENERGY_CAPACITY);
        machine.setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON, 2));
        helper.assertTrue(helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(pos), Direction.NORTH) == null,
                "The Plasma Granulator shouldn't take fluids any more");
        helper.succeedWhen(() -> {
            ItemStack output = machine.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(ModItems.GRANULATED_IRON.get()) && output.getCount() == 4,
                    "Expected 4 granulated iron, got " + output);
        });
    }

    /**
     * An Electrolyser's oxygen leaves through its By-product face (the back, by default) and its hydrogen through its
     * Output face, each into its own Fluid Tank.
     */
    private static void oxygenLeavesByByproductFace(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ELECTROLYSER.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        ElectrolyserBlockEntity electrolyser = helper.getBlockEntity(pos, ElectrolyserBlockEntity.class);
        electrolyser.setEnergy(ElectrolyserBlockEntity.ENERGY_CAPACITY);
        electrolyser.getTanks().get(ElectrolyserBlockEntity.TANK_WATER).fill(FluidResource.of(Fluids.WATER), 2_000);
        // Facing north: the back is south; make east an Output face (left side).
        electrolyser.getControls().setSide(RelativeSide.LEFT, SideMode.OUTPUT);
        BlockPos oxygenTank = new BlockPos(2, 1, 3);
        BlockPos hydrogenTank = new BlockPos(3, 1, 2);
        helper.setBlock(oxygenTank, ModBlocks.FLUID_TANK.get());
        helper.setBlock(hydrogenTank, ModBlocks.FLUID_TANK.get());
        FluidTankBlockEntity oxygen = helper.getBlockEntity(oxygenTank, FluidTankBlockEntity.class);
        FluidTankBlockEntity hydrogen = helper.getBlockEntity(hydrogenTank, FluidTankBlockEntity.class);
        helper.succeedWhen(() -> {
            helper.assertTrue(oxygen.getTank().getAmount() > 0 && oxygen.getTank().getFluid().is(ModFluids.OXYGEN_TYPE.get()),
                    "The back tank should fill with oxygen, it has " + oxygen.getTank().getStack());
            helper.assertTrue(hydrogen.getTank().getAmount() > 0 && hydrogen.getTank().getFluid().is(ModFluids.HYDROGEN_TYPE.get()),
                    "The east tank should fill with hydrogen, it has " + hydrogen.getTank().getStack());
        });
    }

    /**
     * An Energy Cable network moves at most 1,000 FE per tick however many inserts there are, a cancelled
     * (simulated) insert doesn't use any of that, and the next tick the full amount is available again.
     */
    private static void energyCableCapsPerTick(GameTestHelper helper) {
        BlockPos battery = new BlockPos(1, 1, 2);
        BlockPos cable = new BlockPos(2, 1, 2);
        // The plasma battery takes up to 8,000 FE per insert, so only the cable limits what arrives.
        helper.setBlock(battery, ModBlocks.PLASMA_ENERGY_BATTERY.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        placeConduit(helper, cable, ModBlocks.ENERGY_CABLE.get());
        assertConnection(helper, cable, Direction.WEST, CableConnection.MACHINE);
        EnergyHandler handler = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(cable), Direction.EAST);
        helper.assertTrue(handler != null, "The energy cable has no FE capability");
        EnergyBatteryBlockEntity cell = helper.getBlockEntity(battery, EnergyBatteryBlockEntity.class);

        try (Transaction simulation = Transaction.openRoot()) {
            handler.insert(600, simulation); // not committed
        }
        int first = insertCommitted(handler, 600);
        int second = insertCommitted(handler, 600);
        helper.assertTrue(first == 600 && second == 400,
                "Expected 600 then 400 FE this tick, got " + first + " then " + second);
        helper.assertTrue(cell.getEnergy() == EnergyCableBlock.ENERGY_CABLE_RATE, "The battery should hold 1,000 FE, has " + cell.getEnergy());
        helper.runAfterDelay(1, () -> {
            int next = insertCommitted(handler, 600);
            helper.assertTrue(next == 600, "Next tick the cable should carry 600 FE again, carried " + next);
            helper.succeed();
        });
    }

    /**
     * A Plasma Cable takes plasma (up to 4,000 mB at a time) into a tank but refuses water, and doesn't join a
     * Fluid Pipe next to it.
     */
    private static void plasmaCableCarriesOnlyPlasma(GameTestHelper helper) {
        BlockPos pipe = new BlockPos(1, 1, 2);
        BlockPos cable = new BlockPos(2, 1, 2);
        BlockPos tankPos = new BlockPos(3, 1, 2);
        helper.setBlock(tankPos, ModBlocks.FLUID_TANK.get());
        placeConduit(helper, pipe, ModBlocks.FLUID_PIPE.get());
        placeConduit(helper, cable, ModBlocks.PLASMA_CABLE.get());
        assertConnection(helper, cable, Direction.EAST, CableConnection.MACHINE);
        assertConnection(helper, cable, Direction.WEST, CableConnection.NONE);
        assertConnection(helper, pipe, Direction.EAST, CableConnection.NONE);

        var handler = helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(cable), Direction.UP);
        helper.assertTrue(handler != null, "The plasma cable has no fluid capability");
        int water;
        int plasma;
        try (Transaction transaction = Transaction.openRoot()) {
            water = handler.insert(FluidResource.of(Fluids.WATER), 1_000, transaction);
            plasma = handler.insert(FluidResource.of(ModFluids.PLASMA.get()), 5_000, transaction);
            transaction.commit();
        }
        FluidTankBlockEntity tank = helper.getBlockEntity(tankPos, FluidTankBlockEntity.class);
        helper.assertTrue(water == 0, "The plasma cable carried " + water + " mB of water");
        helper.assertTrue(plasma == FluidPipeBlock.PLASMA_CABLE_RATE && tank.getTank().getAmount() == plasma,
                "Expected 4,000 mB of plasma in the tank, carried " + plasma + ", tank has " + tank.getTank().getAmount());
        helper.succeed();
    }

    /** An Energy Cable next to a Plasma Cable doesn't connect to it either way (conduits only join their own kind). */
    private static void cableTiersDontJoin(GameTestHelper helper) {
        BlockPos energy = new BlockPos(1, 1, 2);
        BlockPos plasma = new BlockPos(2, 1, 2);
        placeConduit(helper, energy, ModBlocks.ENERGY_CABLE.get());
        placeConduit(helper, plasma, ModBlocks.PLASMA_CABLE.get());
        assertConnection(helper, energy, Direction.EAST, CableConnection.NONE);
        assertConnection(helper, plasma, Direction.WEST, CableConnection.NONE);
        helper.succeed();
    }

    /** Electronic Circuit + 100 mB plasma -> Plasma Circuit. */
    private static void circuitAssemblerMakesPlasmaCircuit(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.CIRCUIT_ASSEMBLER.get());
        PlasmaMachineBlockEntity machine = helper.getBlockEntity(pos, PlasmaMachineBlockEntity.class);
        ((MachineEnergyStorage) machine.getEnergyHandler()).set(ElectricMachineBlockEntity.TIER3_ENERGY_CAPACITY);
        machine.getTank().fill(FluidResource.of(ModFluids.PLASMA.get()), 150);
        machine.setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(ModItems.ELECTRONIC_CIRCUIT.get()));
        helper.succeedWhen(() -> {
            ItemStack output = machine.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(ModItems.PLASMA_CIRCUIT.get()), "No Plasma Circuit yet (has " + output + ")");
            helper.assertTrue(machine.getTank().getAmount() == 50, "Expected 50 mB plasma left, has " + machine.getTank().getAmount());
        });
    }

    /**
     * Without water the Ore Washer waits; with a water source block next to it, it fills its water tank by itself and
     * washes raw iron into 3 Washed Iron.
     */
    private static void oreWasherTriplesWithWater(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ORE_WASHER.get());
        OreWasherBlockEntity washer = helper.getBlockEntity(pos, OreWasherBlockEntity.class);
        ((MachineEnergyStorage) washer.getEnergyHandler()).set(ElectricMachineBlockEntity.TIER3_ENERGY_CAPACITY);
        washer.setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON));

        helper.runAfterDelay(OreWasherBlockEntity.PROCESS_TIME + 10, () -> {
            helper.assertTrue(washer.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT).isEmpty(), "The washer ran without water");
            helper.setBlock(new BlockPos(2, 1, 3), Blocks.WATER);
            helper.succeedWhen(() -> {
                ItemStack output = washer.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(output.is(ModItems.WASHED_IRON.get()) && output.getCount() == 3,
                        "Expected 3 Washed Iron, got " + output);
            });
        });
    }

    /** A powered Electric Granulator turns 2 Washed Iron into 2 granulated iron (1 each). */
    private static void electricGranulatorTakesWashedOre(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ELECTRIC_GRANULATOR.get());
        ElectricMachineBlockEntity machine = helper.getBlockEntity(pos, ElectricMachineBlockEntity.class);
        ((MachineEnergyStorage) machine.getEnergyHandler()).set(ElectricMachineBlockEntity.ENERGY_CAPACITY);
        machine.setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(ModItems.WASHED_IRON.get(), 2));
        helper.succeedWhen(() -> {
            ItemStack output = machine.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(ModItems.GRANULATED_IRON.get()) && output.getCount() == 2,
                    "Expected 2 granulated iron, got " + output);
        });
    }

    /** The Coal Granulator won't take Washed ore (not even from a hopper), and won't run it if it's put in anyway. */
    private static void coalGranulatorRefusesWashedOre(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.COAL_GRANULATOR.get());
        FuelMachineBlockEntity granulator = helper.getBlockEntity(pos, FuelMachineBlockEntity.class);
        ItemStack washed = new ItemStack(ModItems.WASHED_IRON.get());
        helper.assertTrue(!granulator.canPlaceItem(FuelMachineBlockEntity.SLOT_INPUT, washed), "The Coal Granulator accepted Washed Iron");
        granulator.setItem(FuelMachineBlockEntity.SLOT_INPUT, washed);
        granulator.setItem(FuelMachineBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL));
        helper.runAfterDelay(200, () -> {
            helper.assertTrue(granulator.getItem(FuelMachineBlockEntity.SLOT_OUTPUT).isEmpty(), "The Coal Granulator granulated Washed Iron");
            helper.succeed();
        });
    }

    /**
     * Ore Washer -> Electric Granulator -> Electric Smelter side by side, each with its east face set to Output and no
     * hoppers: raw iron ends up as iron ingots in the smelter (3 of them, one per Washed Iron).
     */
    private static void machinesPushIntoTheNext(GameTestHelper helper) {
        BlockPos washerPos = new BlockPos(1, 1, 2);
        BlockPos granulatorPos = new BlockPos(2, 1, 2);
        BlockPos smelterPos = new BlockPos(3, 1, 2);
        helper.setBlock(washerPos, ModBlocks.ORE_WASHER.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        helper.setBlock(granulatorPos, ModBlocks.ELECTRIC_GRANULATOR.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        helper.setBlock(smelterPos, ModBlocks.ELECTRIC_SMELTER.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        OreWasherBlockEntity washer = helper.getBlockEntity(washerPos, OreWasherBlockEntity.class);
        ElectricMachineBlockEntity granulator = helper.getBlockEntity(granulatorPos, ElectricMachineBlockEntity.class);
        ElectricMachineBlockEntity smelter = helper.getBlockEntity(smelterPos, ElectricMachineBlockEntity.class);
        // Facing north, the left side is east: the face towards the next machine.
        washer.getControls().setSide(RelativeSide.LEFT, SideMode.OUTPUT);
        granulator.getControls().setSide(RelativeSide.LEFT, SideMode.OUTPUT);
        ((MachineEnergyStorage) washer.getEnergyHandler()).set(ElectricMachineBlockEntity.TIER3_ENERGY_CAPACITY);
        ((MachineEnergyStorage) granulator.getEnergyHandler()).set(ElectricMachineBlockEntity.ENERGY_CAPACITY);
        ((MachineEnergyStorage) smelter.getEnergyHandler()).set(ElectricMachineBlockEntity.ENERGY_CAPACITY);
        washer.getWater().fill(FluidResource.of(Fluids.WATER), 1_000);
        washer.setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.RAW_IRON));

        helper.succeedWhen(() -> {
            ItemStack ingots = smelter.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(ingots.is(Items.IRON_INGOT) && ingots.getCount() == 3,
                    "Expected 3 iron ingots at the end of the line, got " + ingots);
        });
    }

    /**
     * The Isotope Separator spins 20 mB of water into each mB of deuterium (1 mB/t); the Tritium Breeder turns
     * deuterium into tritium 1:1.
     */
    private static void separatorAndBreederMakeFusionFuel(GameTestHelper helper) {
        BlockPos separatorPos = new BlockPos(1, 1, 2);
        BlockPos breederPos = new BlockPos(3, 1, 2);
        helper.setBlock(separatorPos, ModBlocks.ISOTOPE_SEPARATOR.get());
        helper.setBlock(breederPos, ModBlocks.TRITIUM_BREEDER.get());
        IsotopeSeparatorBlockEntity separator = helper.getBlockEntity(separatorPos, IsotopeSeparatorBlockEntity.class);
        TritiumBreederBlockEntity breeder = helper.getBlockEntity(breederPos, TritiumBreederBlockEntity.class);
        separator.setEnergy(IsotopeSeparatorBlockEntity.ENERGY_CAPACITY);
        breeder.setEnergy(TritiumBreederBlockEntity.ENERGY_CAPACITY);
        separator.getTanks().get(IsotopeSeparatorBlockEntity.TANK_WATER).fill(FluidResource.of(Fluids.WATER), 1_000);
        breeder.getTanks().get(TritiumBreederBlockEntity.TANK_DEUTERIUM).fill(FluidResource.of(ModFluids.DEUTERIUM.get()), 50);
        helper.runAfterDelay(30, () -> {
            int deuterium = separator.getTanks().get(IsotopeSeparatorBlockEntity.TANK_DEUTERIUM).getAmount();
            int waterUsed = 1_000 - separator.getTanks().get(IsotopeSeparatorBlockEntity.TANK_WATER).getAmount();
            helper.assertTrue(deuterium > 0 && waterUsed == deuterium * IsotopeSeparatorBlockEntity.WATER_PER_MB,
                    "Expected 20 mB water per mB deuterium, used " + waterUsed + " for " + deuterium);
            int tritium = breeder.getTanks().get(TritiumBreederBlockEntity.TANK_TRITIUM).getAmount();
            int deuteriumLeft = breeder.getTanks().get(TritiumBreederBlockEntity.TANK_DEUTERIUM).getAmount();
            helper.assertTrue(tritium > 0 && tritium + deuteriumLeft == 50,
                    "Expected deuterium -> tritium 1:1, got " + tritium + " tritium with " + deuteriumLeft + " deuterium left");
            helper.succeed();
        });
    }

    /** 100 mB deuterium + 100 mB tritium + an iron nugget -> a Fusion Fuel Pellet. */
    private static void pelletPressMakesAPellet(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.PELLET_PRESS.get());
        PelletPressBlockEntity press = helper.getBlockEntity(pos, PelletPressBlockEntity.class);
        ((MachineEnergyStorage) press.getEnergyHandler()).set(ElectricMachineBlockEntity.TIER3_ENERGY_CAPACITY);
        press.getDeuterium().fill(FluidResource.of(ModFluids.DEUTERIUM.get()), 150);
        press.getTritium().fill(FluidResource.of(ModFluids.TRITIUM.get()), 150);
        press.setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.IRON_NUGGET, 2));
        helper.succeedWhen(() -> {
            ItemStack output = press.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(ModItems.FUSION_FUEL_PELLET.get()) && output.getCount() == 1,
                    "Expected one pellet (only enough fuel for one), got " + output);
            helper.assertTrue(press.getDeuterium().getAmount() == 50 && press.getTritium().getAmount() == 50,
                    "Expected 50 mB of each left, have " + press.getDeuterium().getAmount() + " / " + press.getTritium().getAmount());
        });
    }

    /** The Circuit Assembler forges an iron block into Reactor Plating and a Plasma Circuit into a Fusion Circuit. */
    private static void assemblerForgesReactorParts(GameTestHelper helper) {
        BlockPos platingPos = new BlockPos(1, 1, 2);
        BlockPos circuitPos = new BlockPos(3, 1, 2);
        PlasmaMachineBlockEntity plating = placeAssembler(helper, platingPos, new ItemStack(Items.IRON_BLOCK));
        PlasmaMachineBlockEntity circuit = placeAssembler(helper, circuitPos, new ItemStack(ModItems.PLASMA_CIRCUIT.get()));
        helper.succeedWhen(() -> {
            helper.assertTrue(plating.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT).is(ModItems.REACTOR_PLATING.get()), "No Reactor Plating yet");
            helper.assertTrue(circuit.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT).is(ModItems.FUSION_CIRCUIT.get()), "No Fusion Circuit yet");
        });
    }

    /** A complete 3x3x3 forms at once; taking a corner casing away unforms it at the next check. */
    private static void reactorFormsAndUnforms(GameTestHelper helper) {
        FusionReactorBlockEntity reactor = buildReactor(helper);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(reactor.isFormed(), "The complete reactor didn't form");
            helper.setBlock(new BlockPos(1, 0, 1), Blocks.AIR);
            helper.runAfterDelay(FusionReactorBlockEntity.FORM_CHECK_INTERVAL + 2, () -> {
                helper.assertTrue(!reactor.isFormed(), "The reactor stayed formed with a corner missing");
                helper.succeed();
            });
        });
    }

    /**
     * Given plasma and two pellets, the reactor heats up, burns a pellet for FE (pushed out of the port's Output face
     * into a battery) and gets tritium back when the pellet is spent.
     */
    private static void reactorBurnsPelletsForFe(GameTestHelper helper) {
        FusionReactorBlockEntity reactor = buildReactor(helper);
        reactor.getPlasma().fill(FluidResource.of(ModFluids.PLASMA.get()), 1_500);
        reactor.setItem(FusionReactorBlockEntity.SLOT_PELLETS, new ItemStack(ModItems.FUSION_FUEL_PELLET.get(), 2));
        BlockPos batteryPos = new BlockPos(4, 1, 2);
        helper.setBlock(batteryPos, ModBlocks.ENERGY_BATTERY.get());
        EnergyBatteryBlockEntity battery = helper.getBlockEntity(batteryPos, EnergyBatteryBlockEntity.class);
        helper.succeedWhen(() -> {
            helper.assertTrue(battery.getEnergy() > 0, "No FE has reached the battery yet (core heat " + reactor.getHeat() + "%)");
            helper.assertTrue(reactor.getTritium().getAmount() >= FusionReactorBlockEntity.TRITIUM_PER_PELLET,
                    "No tritium back yet (" + reactor.getTritium().getAmount() + " mB)");
        });
    }

    /**
     * 5 Washed Iron + 250 mB oxygen -> 5 Purified Iron, using 50 mB oxygen each. Any by-product is a gold nugget (no
     * mod adds nickel in the test world, so the fallback recipe is the one that loaded).
     */
    private static void orePurifierPurifiesWithOxygen(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ORE_PURIFIER.get());
        OrePurifierBlockEntity purifier = helper.getBlockEntity(pos, OrePurifierBlockEntity.class);
        ((MachineEnergyStorage) purifier.getEnergyHandler()).set(ElectricMachineBlockEntity.TIER3_ENERGY_CAPACITY);
        purifier.getOxygen().fill(FluidResource.of(ModFluids.OXYGEN.get()), 300);
        purifier.setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(ModItems.WASHED_IRON.get(), 5));
        helper.succeedWhen(() -> {
            ItemStack output = purifier.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(ModItems.PURIFIED_IRON.get()) && output.getCount() == 5, "Expected 5 Purified Iron, got " + output);
            helper.assertTrue(purifier.getOxygen().getAmount() == 50, "Expected 50 mB oxygen left, has " + purifier.getOxygen().getAmount());
            ItemStack byproduct = purifier.getItem(OrePurifierBlockEntity.SLOT_BYPRODUCT);
            helper.assertTrue(byproduct.isEmpty() || byproduct.is(Items.GOLD_NUGGET), "Unexpected by-product " + byproduct);
        });
    }

    /** An Electric Granulator turns Purified ore into granulated ore, 1 for 1. */
    private static void granulatorTakesPurifiedOre(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.ELECTRIC_GRANULATOR.get());
        ElectricMachineBlockEntity machine = helper.getBlockEntity(pos, ElectricMachineBlockEntity.class);
        ((MachineEnergyStorage) machine.getEnergyHandler()).set(ElectricMachineBlockEntity.ENERGY_CAPACITY);
        machine.setItem(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(ModItems.PURIFIED_COPPER.get(), 2));
        helper.succeedWhen(() -> {
            ItemStack output = machine.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(ModItems.GRANULATED_COPPER.get()) && output.getCount() == 2, "Expected 2 granulated copper, got " + output);
        });
    }

    // ---- Helpers ----

    /**
     * Builds a 3x3x3 Fusion Reactor at x 1-3, y 0-2, z 1-3 (it replaces part of the floor): casing frame, glass in
     * the face centres, the controller in the south face's centre (facing south) and a port in the east face's centre
     * with its east face set to Output.
     */
    private static FusionReactorBlockEntity buildReactor(GameTestHelper helper) {
        BlockPos core = new BlockPos(2, 1, 2);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = core.offset(dx, dy, dz);
                    int centred = (dx == 0 ? 1 : 0) + (dy == 0 ? 1 : 0) + (dz == 0 ? 1 : 0);
                    if (centred == 3) {
                        helper.setBlock(p, Blocks.AIR);
                    } else {
                        helper.setBlock(p, centred == 2 ? ModBlocks.REACTOR_GLASS.get() : ModBlocks.REACTOR_CASING.get());
                    }
                }
            }
        }
        BlockPos controllerPos = core.south();
        helper.setBlock(controllerPos, ModBlocks.REACTOR_CONTROLLER.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH));
        BlockPos portPos = core.east();
        helper.setBlock(portPos, ModBlocks.REACTOR_PORT.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        // Facing north, the port's left is east (away from the reactor).
        helper.getBlockEntity(portPos, ReactorPortBlockEntity.class).getControls().setSide(RelativeSide.LEFT, SideMode.OUTPUT);
        return helper.getBlockEntity(controllerPos, FusionReactorBlockEntity.class);
    }

    private static PlasmaMachineBlockEntity placeAssembler(GameTestHelper helper, BlockPos pos, ItemStack input) {
        helper.setBlock(pos, ModBlocks.CIRCUIT_ASSEMBLER.get());
        PlasmaMachineBlockEntity machine = helper.getBlockEntity(pos, PlasmaMachineBlockEntity.class);
        ((MachineEnergyStorage) machine.getEnergyHandler()).set(ElectricMachineBlockEntity.TIER3_ENERGY_CAPACITY);
        machine.getTank().fill(FluidResource.of(ModFluids.PLASMA.get()), 200);
        machine.setItem(ElectricMachineBlockEntity.SLOT_INPUT, input);
        return machine;
    }

    private static int insertCommitted(EnergyHandler handler, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    /** An Atmospheric Water Generator facing north, optionally with an Air Filter. */
    private static AtmosphericWaterGeneratorBlockEntity placeWaterGenerator(GameTestHelper helper, BlockPos pos, boolean filter) {
        helper.setBlock(pos, ModBlocks.ATMOSPHERIC_WATER_GENERATOR.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        AtmosphericWaterGeneratorBlockEntity generator = helper.getBlockEntity(pos, AtmosphericWaterGeneratorBlockEntity.class);
        if (filter) {
            generator.setItem(AtmosphericWaterGeneratorBlockEntity.SLOT_FILTER, new ItemStack(ModItems.AIR_FILTER.get()));
        }
        return generator;
    }

    /** Places a cable or pipe and lets it work out its connections like a placed one (setBlock skips placement logic). */
    private static void placeConduit(GameTestHelper helper, BlockPos pos, ConduitBlock conduit) {
        helper.setBlock(pos, conduit.defaultBlockState());
        BlockPos absolute = helper.absolutePos(pos);
        BlockState updated = conduit.defaultBlockState();
        for (Direction direction : Direction.values()) {
            updated = updated.setValue(ConduitBlock.CONNECTIONS.get(direction),
                    conduit.connectionTo(helper.getLevel(), absolute, direction, CableConnection.NONE));
        }
        helper.setBlock(pos, updated);
    }

    private static void fillWithStone(GameTestHelper helper) {
        for (int x = 0; x < 5; x++) {
            for (int y = 1; y < 3; y++) {
                for (int z = 0; z < 5; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        }
    }

    /** Runs the real, datagen'd volcanic basalt crust feature at a position in the test. */
    private static boolean placeCrust(GameTestHelper helper, BlockPos pos) {
        ConfiguredFeature<?, ?> crust = helper.getLevel().registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(ModWorldGen.VOLCANIC_BASALT_CRUST).value();
        return crust.place(helper.getLevel(), helper.getLevel().getChunkSource().getGenerator(),
                helper.getLevel().getRandom(), helper.absolutePos(pos));
    }

    /** A generator at {@code from}, cables between, and an Electric Smelter at {@code to} (same row). */
    private static void buildLine(GameTestHelper helper, BlockPos from, BlockPos to) {
        helper.setBlock(from, ModBlocks.ELECTRIC_GENERATOR.get());
        helper.setBlock(to, ModBlocks.ELECTRIC_SMELTER.get());
        for (int x = from.getX() + 1; x < to.getX(); x++) {
            placeConduit(helper, new BlockPos(x, from.getY(), from.getZ()), ModBlocks.ENERGY_CABLE.get());
        }
    }

    private static void useWrench(GameTestHelper helper, Player player, ItemStack wrench, BlockPos pos, Vec3 offsetFromCentre, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        BlockPos absolute = helper.absolutePos(pos);
        Vec3 hit = Vec3.atCenterOf(absolute).add(offsetFromCentre);
        wrench.onItemUseFirst(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, wrench,
                new BlockHitResult(hit, face, absolute, false)));
    }

    /** An item entity of this item lying around the given test position, or EMPTY. */
    private static ItemStack droppedItem(GameTestHelper helper, BlockPos pos, Item item) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(pos)).inflate(1)).stream()
                .map(ItemEntity::getItem)
                .filter(stack -> stack.is(item))
                .findFirst().orElse(ItemStack.EMPTY);
    }

    private static void assertConnection(GameTestHelper helper, BlockPos cable, Direction side, CableConnection expected) {
        CableConnection actual = helper.getLevel().getBlockState(helper.absolutePos(cable)).getValue(EnergyCableBlock.CONNECTIONS.get(side));
        helper.assertTrue(actual == expected, "Cable at " + cable + " " + side + " is " + actual + ", expected " + expected);
    }

    private ModGameTests() {}
}
