package com.script.comminutioncraft.gametest;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.cable.CableConnection;
import com.script.comminutioncraft.cable.EnergyCableBlock;
import com.script.comminutioncraft.machine.ElectricGeneratorBlockEntity;
import com.script.comminutioncraft.machine.ElectricMachineBlockEntity;
import com.script.comminutioncraft.machine.MachineBlock;
import com.script.comminutioncraft.registry.ModBlocks;
import com.script.comminutioncraft.registry.ModDataComponents;
import com.script.comminutioncraft.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
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
        wrench.set(ModDataComponents.WRENCH_CABLE_MODE.get(), Unit.INSTANCE);
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

    // ---- Helpers ----

    /** A generator at {@code from}, cables between, and an Electric Smelter at {@code to} (same row). */
    private static void buildLine(GameTestHelper helper, BlockPos from, BlockPos to) {
        helper.setBlock(from, ModBlocks.ELECTRIC_GENERATOR.get());
        helper.setBlock(to, ModBlocks.ELECTRIC_SMELTER.get());
        for (int x = from.getX() + 1; x < to.getX(); x++) {
            BlockPos pos = new BlockPos(x, from.getY(), from.getZ());
            BlockState cable = ModBlocks.ENERGY_CABLE.get().defaultBlockState();
            helper.setBlock(pos, cable);
            // setBlock doesn't run placement logic, so let the cable work out its connections like a placed one.
            BlockPos absolute = helper.absolutePos(pos);
            BlockState updated = cable;
            for (Direction direction : Direction.values()) {
                updated = updated.setValue(EnergyCableBlock.CONNECTIONS.get(direction),
                        EnergyCableBlock.connectionTo(helper.getLevel(), absolute, direction, CableConnection.NONE));
            }
            helper.setBlock(pos, updated);
        }
    }

    private static void useWrench(GameTestHelper helper, Player player, ItemStack wrench, BlockPos pos, Vec3 offsetFromCentre, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        BlockPos absolute = helper.absolutePos(pos);
        Vec3 hit = Vec3.atCenterOf(absolute).add(offsetFromCentre);
        wrench.useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, wrench,
                new BlockHitResult(hit, face, absolute, false)));
    }

    private static void assertConnection(GameTestHelper helper, BlockPos cable, Direction side, CableConnection expected) {
        CableConnection actual = helper.getLevel().getBlockState(helper.absolutePos(cable)).getValue(EnergyCableBlock.CONNECTIONS.get(side));
        helper.assertTrue(actual == expected, "Cable at " + cable + " " + side + " is " + actual + ", expected " + expected);
    }

    private ModGameTests() {}
}
