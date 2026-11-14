package com.script.comminutioncraft.common.block;

import com.script.comminutioncraft.ComminutionCraft;
import java.util.List;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every block of one wood, mirroring a vanilla wood (oak): log, wood, stripped log/wood, planks, stairs, slab, fence,
 * fence gate, door, trapdoor, pressure plate, button, sign, hanging sign, leaves, sapling and the potted sapling.
 * Ids follow vanilla naming, e.g. {@code stripped_purpleheart_log}, {@code purpleheart_wall_hanging_sign}.
 * <p>
 * The wood type is {@code comminutioncraft:<name>}, so the sign sheets are {@code textures/entity/signs/<name>.png}
 * and {@code .../signs/hanging/<name>.png}, and the hanging-sign edit screen uses {@code textures/gui/hanging_signs/<name>.png}.
 * Boats are entities + items (ModEntities / ModItems); datagen expands the planks family from {@code data/WoodFamily}.
 */
public final class WoodSet {
    public final String name;
    public final BlockSetType setType;
    public final WoodType woodType;

    public final DeferredBlock<RotatedPillarBlock> log;
    public final DeferredBlock<RotatedPillarBlock> wood;
    public final DeferredBlock<RotatedPillarBlock> strippedLog;
    public final DeferredBlock<RotatedPillarBlock> strippedWood;
    public final DeferredBlock<Block> planks;
    public final DeferredBlock<StairBlock> stairs;
    public final DeferredBlock<SlabBlock> slab;
    public final DeferredBlock<FenceBlock> fence;
    public final DeferredBlock<FenceGateBlock> fenceGate;
    public final DeferredBlock<DoorBlock> door;
    public final DeferredBlock<TrapDoorBlock> trapdoor;
    public final DeferredBlock<PressurePlateBlock> pressurePlate;
    public final DeferredBlock<ButtonBlock> button;
    public final DeferredBlock<StandingSignBlock> sign;
    public final DeferredBlock<WallSignBlock> wallSign;
    public final DeferredBlock<CeilingHangingSignBlock> hangingSign;
    public final DeferredBlock<WallHangingSignBlock> wallHangingSign;
    public final DeferredBlock<UntintedParticleLeavesBlock> leaves;
    public final DeferredBlock<SaplingBlock> sapling;
    public final DeferredBlock<FlowerPotBlock> pottedSapling;

    /**
     * @param planksColor map colour of planks, stripped wood and log ends
     * @param barkColor   map colour of the bark sides
     * @param leafColor   tint of the falling leaf particles (the leaves texture itself is not tinted)
     */
    public WoodSet(DeferredRegister.Blocks blocks, String name, MapColor planksColor, MapColor barkColor, int leafColor, TreeGrower grower) {
        this.name = name;
        this.setType = BlockSetType.register(new BlockSetType(ComminutionCraft.MODID + ":" + name));
        this.woodType = WoodType.register(new WoodType(ComminutionCraft.MODID + ":" + name, setType));

        log = blocks.registerBlock(name + "_log", RotatedPillarBlock::new, p -> log(p, planksColor, barkColor));
        wood = blocks.registerBlock(name + "_wood", RotatedPillarBlock::new, p -> log(p, barkColor, barkColor));
        strippedLog = blocks.registerBlock("stripped_" + name + "_log", RotatedPillarBlock::new, p -> log(p, planksColor, planksColor));
        strippedWood = blocks.registerBlock("stripped_" + name + "_wood", RotatedPillarBlock::new, p -> log(p, planksColor, planksColor));
        planks = blocks.registerBlock(name + "_planks", Block::new, p -> wooden(p, planksColor).strength(2.0F, 3.0F));
        stairs = blocks.registerBlock(name + "_stairs", p -> new StairBlock(planks.get().defaultBlockState(), p),
                p -> wooden(p, planksColor).strength(2.0F, 3.0F));
        slab = blocks.registerBlock(name + "_slab", SlabBlock::new, p -> wooden(p, planksColor).strength(2.0F, 3.0F));
        fence = blocks.registerBlock(name + "_fence", FenceBlock::new, p -> wooden(p, planksColor).forceSolidOn().strength(2.0F, 3.0F));
        fenceGate = blocks.registerBlock(name + "_fence_gate", p -> new FenceGateBlock(woodType, p),
                p -> wooden(p, planksColor).forceSolidOn().strength(2.0F, 3.0F));
        door = blocks.registerBlock(name + "_door", p -> new DoorBlock(setType, p),
                p -> wooden(p, planksColor).strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY));
        trapdoor = blocks.registerBlock(name + "_trapdoor", p -> new TrapDoorBlock(setType, p),
                p -> wooden(p, planksColor).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never));
        pressurePlate = blocks.registerBlock(name + "_pressure_plate", p -> new PressurePlateBlock(setType, p),
                p -> wooden(p, planksColor).forceSolidOn().noCollision().strength(0.5F).pushReaction(PushReaction.DESTROY));
        button = blocks.registerBlock(name + "_button", p -> new ButtonBlock(setType, 30, p),
                p -> p.noCollision().strength(0.5F).pushReaction(PushReaction.DESTROY));

        sign = blocks.registerBlock(name + "_sign", p -> new StandingSignBlock(woodType, p),
                p -> wooden(p, planksColor).forceSolidOn().noCollision().strength(1.0F));
        wallSign = blocks.registerBlock(name + "_wall_sign", p -> new WallSignBlock(woodType, p),
                p -> wooden(wallOf(p, sign.get()), planksColor).forceSolidOn().noCollision().strength(1.0F));
        hangingSign = blocks.registerBlock(name + "_hanging_sign", p -> new CeilingHangingSignBlock(woodType, p),
                p -> wooden(p, planksColor).forceSolidOn().noCollision().strength(1.0F));
        wallHangingSign = blocks.registerBlock(name + "_wall_hanging_sign", p -> new WallHangingSignBlock(woodType, p),
                p -> wooden(wallOf(p, hangingSign.get()), planksColor).forceSolidOn().noCollision().strength(1.0F));

        leaves = blocks.registerBlock(name + "_leaves",
                p -> new UntintedParticleLeavesBlock(0.01F, ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, leafColor), p),
                p -> p.mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
                        .isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false).ignitedByLava().pushReaction(PushReaction.DESTROY)
                        .isRedstoneConductor((state, level, pos) -> false));
        sapling = blocks.registerBlock(name + "_sapling", p -> new SaplingBlock(grower, p),
                p -> p.mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));
        pottedSapling = blocks.registerBlock("potted_" + name + "_sapling",
                p -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, sapling, p),
                p -> p.instabreak().noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    private static BlockBehaviour.Properties log(BlockBehaviour.Properties p, MapColor end, MapColor side) {
        return p.mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == net.minecraft.core.Direction.Axis.Y ? end : side)
                .instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava();
    }

    private static BlockBehaviour.Properties wooden(BlockBehaviour.Properties p, MapColor color) {
        return p.mapColor(color).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).ignitedByLava();
    }

    /** Like vanilla's wallVariant: the wall sign drops and is named like the standing one. */
    private static BlockBehaviour.Properties wallOf(BlockBehaviour.Properties p, Block standing) {
        return p.overrideLootTable(standing.getLootTable()).overrideDescription(standing.getDescriptionId());
    }

    /** Every block that has an item, in creative-tab order (vanilla's building-blocks order, then the natural ones). */
    public List<DeferredBlock<? extends Block>> withItems() {
        return List.of(log, wood, strippedLog, strippedWood, planks, stairs, slab, fence, fenceGate, door, trapdoor,
                pressurePlate, button, leaves, sapling);
    }

    /** Logs and wood, stripped or not (the {@code <name>_logs} tag). */
    public List<DeferredBlock<RotatedPillarBlock>> logs() {
        return List.of(log, wood, strippedLog, strippedWood);
    }
}
