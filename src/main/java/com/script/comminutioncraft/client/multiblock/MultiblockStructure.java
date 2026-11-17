package com.script.comminutioncraft.client.multiblock;

import com.script.comminutioncraft.common.machine.AcceleratorPortBlock;
import com.script.comminutioncraft.common.machine.ParticleAcceleratorBlockEntity;
import com.script.comminutioncraft.common.registry.ModBlocks;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

/**
 * An example build of one of our multiblocks, shared by the JEI pages and the guide book: the blocks (positions from 0,
 * the front = south), the spots that must stay empty, and what each kind of block may be used for.
 */
public record MultiblockStructure(String id, int sizeX, int sizeY, int sizeZ, Map<BlockPos, BlockState> blocks,
                                  Set<BlockPos> mustBeEmpty, Map<Block, Component> rules) {
    public static final Supplier<MultiblockStructure> FUSION_REACTOR = memo(MultiblockStructure::fusionReactor);
    public static final Supplier<MultiblockStructure> PARTICLE_ACCELERATOR = memo(MultiblockStructure::particleAccelerator);
    public static final Supplier<MultiblockStructure> EVENT_HORIZON = memo(MultiblockStructure::eventHorizon);

    private static Supplier<MultiblockStructure> memo(Supplier<MultiblockStructure> factory) {
        return new Supplier<>() {
            private @Nullable MultiblockStructure value;

            @Override
            public MultiblockStructure get() {
                if (value == null) {
                    value = factory.get();
                }
                return value;
            }
        };
    }

    public static @Nullable MultiblockStructure byId(String id) {
        return switch (id) {
            case "fusion_reactor" -> FUSION_REACTOR.get();
            case "particle_accelerator" -> PARTICLE_ACCELERATOR.get();
            case "event_horizon" -> EVENT_HORIZON.get();
            default -> null;
        };
    }

    public static List<String> ids() {
        return List.of("fusion_reactor", "particle_accelerator", "event_horizon");
    }

    /** The rule for this kind of block (where it may go), if any. */
    public @Nullable Component rule(Block block) {
        return rules.get(block);
    }

    private static BlockState facingSouth(Block block) {
        return block.defaultBlockState().trySetValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
    }

    private static BlockState port(Block block, boolean output) {
        return block.defaultBlockState().setValue(AcceleratorPortBlock.OUTPUT, output);
    }

    /** 3x3x3: casing frame, glass in the face centres, a port under the core, the controller in front. */
    private static MultiblockStructure fusionReactor() {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    int centred = (x == 1 ? 1 : 0) + (y == 1 ? 1 : 0) + (z == 1 ? 1 : 0);
                    if (centred == 3) {
                        continue; // the core stays empty
                    }
                    BlockState state;
                    if (x == 1 && y == 1 && z == 2) {
                        state = facingSouth(ModBlocks.REACTOR_CONTROLLER.get());
                    } else if (x == 1 && y == 0 && z == 1) {
                        state = ModBlocks.REACTOR_PORT.get().defaultBlockState();
                    } else if (centred == 2) {
                        state = ModBlocks.REACTOR_GLASS.get().defaultBlockState();
                    } else {
                        state = ModBlocks.REACTOR_CASING.get().defaultBlockState();
                    }
                    blocks.put(new BlockPos(x, y, z), state);
                }
            }
        }
        Map<Block, Component> rules = new LinkedHashMap<>();
        rules.put(ModBlocks.REACTOR_CASING.get(), Component.translatable("jei.comminutioncraft.fusion_reactor.frame"));
        rules.put(ModBlocks.REACTOR_GLASS.get(), Component.translatable("jei.comminutioncraft.fusion_reactor.face_centre"));
        rules.put(ModBlocks.REACTOR_PORT.get(), Component.translatable("jei.comminutioncraft.fusion_reactor.port"));
        rules.put(ModBlocks.REACTOR_CONTROLLER.get(), Component.translatable("jei.comminutioncraft.fusion_reactor.controller"));
        return new MultiblockStructure("fusion_reactor", 3, 3, 3, blocks, Set.of(new BlockPos(1, 1, 1)), rules);
    }

    /** A 7x7 ring: the controller in the middle of the front, an Input port east, an Output port west. */
    private static MultiblockStructure particleAccelerator() {
        int side = 7;
        int mid = side / 2;
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (int z = 0; z < side; z++) {
            for (int x = 0; x < side; x++) {
                if (x != 0 && x != side - 1 && z != 0 && z != side - 1) {
                    continue; // inside the ring
                }
                BlockState state = x == mid && z == side - 1 ? facingSouth(ModBlocks.ACCELERATOR_CONTROLLER.get())
                        : x == side - 1 && z == mid ? port(ModBlocks.ACCELERATOR_PORT.get(), false)
                        : x == 0 && z == mid ? port(ModBlocks.ACCELERATOR_PORT.get(), true)
                        : ModBlocks.ACCELERATOR_SEGMENT.get().defaultBlockState();
                blocks.put(new BlockPos(x, 0, z), state);
            }
        }
        Map<Block, Component> rules = new LinkedHashMap<>();
        rules.put(ModBlocks.ACCELERATOR_SEGMENT.get(), Component.translatable("jei.comminutioncraft.particle_accelerator.sizes",
                ParticleAcceleratorBlockEntity.MIN_RING_SIDE.get(), ParticleAcceleratorBlockEntity.MAX_RING_SIDE.get()));
        rules.put(ModBlocks.ACCELERATOR_PORT.get(), Component.translatable("jei.comminutioncraft.multiblock.port_rule"));
        rules.put(ModBlocks.ACCELERATOR_CONTROLLER.get(), Component.translatable("jei.comminutioncraft.particle_accelerator.controller"));
        return new MultiblockStructure("particle_accelerator", side, 1, side, blocks, Set.of(), rules);
    }

    /** The 7x7x7 cage: frame on every edge, the controller in the middle of the front bottom edge, two ports. */
    private static MultiblockStructure eventHorizon() {
        int n = 7;
        int mid = n / 2;
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        Set<BlockPos> empty = new LinkedHashSet<>();
        for (int y = 0; y < n; y++) {
            for (int z = 0; z < n; z++) {
                for (int x = 0; x < n; x++) {
                    int outer = (x == 0 || x == n - 1 ? 1 : 0) + (y == 0 || y == n - 1 ? 1 : 0) + (z == 0 || z == n - 1 ? 1 : 0);
                    if (outer == 0) {
                        empty.add(new BlockPos(x, y, z)); // the inner 5x5x5
                    }
                    if (outer < 2) {
                        continue; // the inside and the faces' openings
                    }
                    BlockState state = x == mid && y == 0 && z == n - 1 ? facingSouth(ModBlocks.HORIZON_CONTROLLER.get())
                            : x == n - 1 && y == 0 && z == mid ? port(ModBlocks.HORIZON_PORT.get(), false)
                            : x == 0 && y == 0 && z == mid ? port(ModBlocks.HORIZON_PORT.get(), true)
                            : ModBlocks.CONTAINMENT_FRAME.get().defaultBlockState();
                    blocks.put(new BlockPos(x, y, z), state);
                }
            }
        }
        Map<Block, Component> rules = new LinkedHashMap<>();
        rules.put(ModBlocks.CONTAINMENT_FRAME.get(), Component.translatable("jei.comminutioncraft.event_horizon.frame"));
        rules.put(ModBlocks.HORIZON_PORT.get(), Component.translatable("jei.comminutioncraft.multiblock.port_rule"));
        rules.put(ModBlocks.HORIZON_CONTROLLER.get(), Component.translatable("jei.comminutioncraft.event_horizon.controller"));
        return new MultiblockStructure("event_horizon", n, n, n, blocks, empty, rules);
    }
}
