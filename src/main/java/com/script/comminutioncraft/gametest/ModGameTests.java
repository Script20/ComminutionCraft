package com.script.comminutioncraft.gametest;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlockEntity;
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
        TEST_FUNCTIONS.register("plasma_generator_breathes", () -> ModGameTests::plasmaGeneratorBreathes);
        TEST_FUNCTIONS.register("walled_in_generator_makes_nothing", () -> ModGameTests::walledInGeneratorMakesNothing);
        TEST_FUNCTIONS.register("no_filter_half_speed", () -> ModGameTests::noFilterHalfSpeed);
        TEST_FUNCTIONS.register("pipe_carries_plasma_to_tank", () -> ModGameTests::pipeCarriesPlasmaToTank);
        TEST_FUNCTIONS.register("wrench_pickup_keeps_tank_fluid", () -> ModGameTests::wrenchPickupKeepsTankFluid);
        TEST_FUNCTIONS.register("pipes_and_cables_dont_join", () -> ModGameTests::pipesAndCablesDontJoin);
        TEST_FUNCTIONS.register("side_change_reconnects_cable", () -> ModGameTests::sideChangeReconnectsCable);
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

    /** On the floor with nothing else around, 5 faces breathe: 5 mB/t, and the filter wears 1 per mB made. */
    private static void plasmaGeneratorBreathes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        PlasmaGeneratorBlockEntity generator = placePlasmaGenerator(helper, pos, true);

        helper.runAfterDelay(20, () -> {
            helper.assertTrue(generator.getIntake() == 5, "On the floor in open air the intake should be 5/6, but is " + generator.getIntake());
            int made = generator.getTank().getAmount();
            helper.assertTrue(made >= 5 * 15, "Expected about 5 mB/t of plasma, only " + made + " mB after 20 ticks");
            int wear = generator.getItem(PlasmaGeneratorBlockEntity.SLOT_FILTER).getDamageValue();
            helper.assertTrue(wear == made, "The filter should wear 1 per mB (" + made + "), but wore " + wear);
            helper.succeed();
        });
    }

    /** Stone on every side: no intake, no plasma, even with power and a filter. */
    private static void walledInGeneratorMakesNothing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        PlasmaGeneratorBlockEntity generator = placePlasmaGenerator(helper, pos, true);
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

    /** Two generators in the same open air: the one without a filter makes half as much plasma. */
    private static void noFilterHalfSpeed(GameTestHelper helper) {
        PlasmaGeneratorBlockEntity filtered = placePlasmaGenerator(helper, new BlockPos(1, 1, 1), true);
        PlasmaGeneratorBlockEntity bare = placePlasmaGenerator(helper, new BlockPos(3, 1, 3), false);
        helper.runAfterDelay(40, () -> {
            int full = filtered.getTank().getAmount();
            int half = bare.getTank().getAmount();
            helper.assertTrue(full > 0 && Math.abs(half * 2 - full) <= 10,
                    "Without a filter it should make half as much: " + half + " mB vs " + full + " mB with one");
            helper.succeed();
        });
    }

    /** Generator (output to the east) -> 2 fluid pipes -> Fluid Tank: plasma ends up in the tank. */
    private static void pipeCarriesPlasmaToTank(GameTestHelper helper) {
        BlockPos generatorPos = new BlockPos(0, 1, 2);
        BlockPos tankPos = new BlockPos(3, 1, 2);
        PlasmaGeneratorBlockEntity generator = placePlasmaGenerator(helper, generatorPos, true);
        // Facing north, the generator's left is east (towards the pipes).
        generator.getControls().setSide(RelativeSide.LEFT, SideMode.OUTPUT);
        helper.setBlock(tankPos, ModBlocks.FLUID_TANK.get());
        placeConduit(helper, new BlockPos(1, 1, 2), ModBlocks.FLUID_PIPE.get());
        placeConduit(helper, new BlockPos(2, 1, 2), ModBlocks.FLUID_PIPE.get());
        assertConnection(helper, new BlockPos(1, 1, 2), Direction.WEST, CableConnection.MACHINE);
        assertConnection(helper, new BlockPos(2, 1, 2), Direction.EAST, CableConnection.MACHINE);

        FluidTankBlockEntity tank = helper.getBlockEntity(tankPos, FluidTankBlockEntity.class);
        helper.succeedWhen(() -> helper.assertTrue(tank.getTank().getAmount() >= 100 && tank.getTank().getFluid().is(ModFluids.PLASMA_TYPE.get()),
                "The tank should be filling with plasma, it has " + tank.getTank().getAmount() + " mB"));
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

    // ---- Helpers ----

    /** A Plasma Generator facing north with a full FE buffer and, optionally, an Air Filter. */
    private static PlasmaGeneratorBlockEntity placePlasmaGenerator(GameTestHelper helper, BlockPos pos, boolean filter) {
        helper.setBlock(pos, ModBlocks.PLASMA_GENERATOR.get().defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        PlasmaGeneratorBlockEntity generator = helper.getBlockEntity(pos, PlasmaGeneratorBlockEntity.class);
        generator.setEnergy(PlasmaGeneratorBlockEntity.ENERGY_CAPACITY);
        if (filter) {
            generator.setItem(PlasmaGeneratorBlockEntity.SLOT_FILTER, new ItemStack(ModItems.AIR_FILTER.get()));
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
