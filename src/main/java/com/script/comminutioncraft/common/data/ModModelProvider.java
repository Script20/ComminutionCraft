package com.script.comminutioncraft.common.data;

import com.mojang.math.Quadrant;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.client.render.TankItemRenderer;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.pipe.ItemPipeBlock;
import com.script.comminutioncraft.common.item.WrenchMode;
import com.script.comminutioncraft.common.machine.AcceleratorPortBlock;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlock;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Blockstates, block models and item models. Machines use vanilla's furnace layout, which expects
 * textures named <block>_top, _side, _front and _front_on (the lit, animated front).
 */
public class ModModelProvider extends ModelProvider {
    /**
     * Cable / pipe texture layout (16x16): core face at u0-6 v0-6, connector plate at u8-16 v0-8,
     * arm side strip (along the arm) at u0-16 v8-12.
     */
    private static final TextureSlot CABLE = TextureSlot.create("cable");



    /** Connector plate where a north arm meets a machine. */
    private static final ModelTemplate CABLE_PLATE = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(4, 4, 0).to(12, 12, 1)
                    .face(Direction.NORTH, face -> face.texture(CABLE).uvs(8, 0, 16, 8).cullface(Direction.NORTH))
                    .face(Direction.SOUTH, face -> face.texture(CABLE).uvs(8, 0, 16, 8))
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(8, 0, 9, 8))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(8, 0, 9, 8))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(8, 0, 16, 1))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(8, 0, 16, 1)))
            .build();

    // Cables and pipes (energy cables: look B "Light strip"; pipes: look A "Glass channel"): an even 6x6 run; arms reach the middle so two opposite ones make a straight
    // run, and the 8x8 node only shows where it isn't straight. Sheet: node 8x8 at (0,0), plate 8x8 at (8,0), run 16x6 at (0,8).
    private static final ModelTemplate CONDUIT_NODE = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(4, 4, 4).to(12, 12, 12)
                    .allFaces((dir, face) -> face.texture(CABLE).uvs(0, 0, 8, 8)))
            .build();

    /** The north half. No end face: an arm only ever ends at another conduit or under a plate (and see-through pipes would show it). */
    private static final ModelTemplate CONDUIT_ARM = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(5, 5, 0).to(11, 11, 8)
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(0, 8, 8, 14))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(0, 8, 8, 14))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(0, 8, 8, 14).rotation(Quadrant.R90))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(0, 8, 8, 14).rotation(Quadrant.R90)))
            .build();

    /** The south half: used (with the same turn as the north half) for the arm opposite it. */
    private static final ModelTemplate CONDUIT_ARM_FAR = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(5, 5, 8).to(11, 11, 16)
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(8, 8, 16, 14))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(8, 8, 16, 14))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(8, 8, 16, 14).rotation(Quadrant.R90))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(8, 8, 16, 14).rotation(Quadrant.R90)))
            .build();

    private static final ModelTemplate CONDUIT_INVENTORY = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(0, 5, 5).to(16, 11, 11)
                    .face(Direction.NORTH, face -> face.texture(CABLE).uvs(0, 8, 16, 14))
                    .face(Direction.SOUTH, face -> face.texture(CABLE).uvs(0, 8, 16, 14))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(0, 8, 16, 14))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(0, 8, 16, 14))
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(1, 1, 7, 7))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(1, 1, 7, 7)))
            .build();

    // Item Pipe: an even 6x6 see-through run, the 8x8 core only where it isn't straight, 10x10 plates. Textures: item_pipe (core 8x8 at (0,0),
    // its middle 6x6 = the arm's end, tube strip 16x6 at (0,8)) and item_pipe_plate (the Insert plate 10x10 at (0,0)).
    private static final TextureSlot PIPE_PLATE = TextureSlot.create("plate");

    private static final ModelTemplate ITEM_PIPE_CORE = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(4, 4, 4).to(12, 12, 12)
                    .allFaces((dir, face) -> face.texture(CABLE).uvs(0, 0, 8, 8)))
            .build();

    private static final ModelTemplate ITEM_PIPE_ARM = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(5, 5, 0).to(11, 11, 8)
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(0, 8, 8, 14))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(0, 8, 8, 14))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(0, 8, 8, 14).rotation(Quadrant.R90))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(0, 8, 8, 14).rotation(Quadrant.R90)))
            .build();

    private static final ModelTemplate ITEM_PIPE_ARM_FAR = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(5, 5, 8).to(11, 11, 16)
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(8, 8, 16, 14))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(8, 8, 16, 14))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(8, 8, 16, 14).rotation(Quadrant.R90))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(8, 8, 16, 14).rotation(Quadrant.R90)))
            .build();

    /** Up to the core's edge only (the core is see-through, so an arm running on into it would show inside). */
    private static final ModelTemplate ITEM_PIPE_STUB = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(5, 5, 0).to(11, 11, 4)
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(0, 8, 4, 14))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(0, 8, 4, 14))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(0, 8, 4, 14).rotation(Quadrant.R90))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(0, 8, 4, 14).rotation(Quadrant.R90)))
            .build();

    private static final ModelTemplate ITEM_PIPE_STUB_FAR = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(5, 5, 12).to(11, 11, 16)
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(12, 8, 16, 14))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(12, 8, 16, 14))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(12, 8, 16, 14).rotation(Quadrant.R90))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(12, 8, 16, 14).rotation(Quadrant.R90)))
            .build();

    private static final ModelTemplate ITEM_PIPE_PLATE = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(PIPE_PLATE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(3, 3, 0).to(13, 13, 1)
                    .face(Direction.NORTH, face -> face.texture(PIPE_PLATE).uvs(0, 0, 10, 10).cullface(Direction.NORTH))
                    .face(Direction.SOUTH, face -> face.texture(PIPE_PLATE).uvs(0, 0, 10, 10))
                    .face(Direction.EAST, face -> face.texture(PIPE_PLATE).uvs(0, 0, 1, 10))
                    .face(Direction.WEST, face -> face.texture(PIPE_PLATE).uvs(0, 0, 1, 10))
                    .face(Direction.UP, face -> face.texture(PIPE_PLATE).uvs(0, 0, 10, 1))
                    .face(Direction.DOWN, face -> face.texture(PIPE_PLATE).uvs(0, 0, 10, 1)))
            .build();

    private static final ModelTemplate ITEM_PIPE_INVENTORY = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(0, 5, 5).to(16, 11, 11)
                    .face(Direction.NORTH, face -> face.texture(CABLE).uvs(0, 8, 16, 14))
                    .face(Direction.SOUTH, face -> face.texture(CABLE).uvs(0, 8, 16, 14))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(0, 8, 16, 14))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(0, 8, 16, 14))
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(1, 1, 7, 7))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(1, 1, 7, 7)))
            .build();


    public ModModelProvider(PackOutput output) {
        super(output, ComminutionCraft.MODID);
    }

    /** A tank item = its block model plus the inner walls and the fluid it holds (client/render/TankItemRenderer). */
    private static void tankItem(ItemModelGenerators itemModels, Block tank) {
        Identifier model = ModelLocationUtils.getModelLocation(tank);
        itemModels.itemModelOutput.accept(tank.asItem(), ItemModelUtils.composite(
                ItemModelUtils.plainModel(model),
                ItemModelUtils.specialModel(model, new TankItemRenderer.Unbaked(
                        BuiltInRegistries.BLOCK.getKey(tank)))));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createFurnace(ModBlocks.COAL_GRANULATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.COAL_SMELTER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ELECTRIC_GRANULATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ELECTRIC_SMELTER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ELECTRIC_GENERATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        energyBattery(blockModels, ModBlocks.ENERGY_BATTERY.get());
        conduit(blockModels, itemModels, ModBlocks.ENERGY_CABLE.get(), ModItems.ENERGY_CABLE.get());
        blockModels.createFurnace(ModBlocks.PLASMA_GENERATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ATMOSPHERIC_WATER_GENERATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ELECTROLYSER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.MHD_GENERATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ISOTOPE_SEPARATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.TRITIUM_BREEDER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.PELLET_PRESS.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ORE_PURIFIER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.CREATIVE_ENERGY_BATTERY.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createTrivialBlock(ModBlocks.CREATIVE_FLUID_TANK.get(), TexturedModel.COLUMN);
        blockModels.createFurnace(ModBlocks.CREATIVE_CHEST.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        // Fusion Reactor: casing and ports glow while it burns (an _on texture); the glass is cutout; the controller is
        // a furnace-style front over casing sides.
        litCube(blockModels, ModBlocks.REACTOR_CASING.get());
        litCube(blockModels, ModBlocks.REACTOR_PORT.get());
        blockModels.createTrivialCube(ModBlocks.REACTOR_GLASS.get());
        blockModels.createFurnace(ModBlocks.REACTOR_CONTROLLER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        conduit(blockModels, itemModels, ModBlocks.FUSION_CABLE.get(), ModItems.FUSION_CABLE.get());
        energyBattery(blockModels, ModBlocks.FUSION_ENERGY_BATTERY.get());
        blockModels.createFurnace(ModBlocks.ANTIMATTER_GRANULATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ANTIMATTER_SMELTER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.FROTH_FLOTATION_CELL.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ANTIMATTER_WASHER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ANTIMATTER_PURIFIER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ANNIHILATION_GENERATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        conduit(blockModels, itemModels, ModBlocks.ANTIMATTER_CABLE.get(), ModItems.ANTIMATTER_CABLE.get());
        energyBattery(blockModels, ModBlocks.ANTIMATTER_ENERGY_BATTERY.get());
        itemModels.generateFlatItem(ModItems.ANTIMATTER_CIRCUIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FUSION_CIRCUIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.REACTOR_PLATING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FUSION_FUEL_PELLET.get(), ModelTemplates.FLAT_ITEM);
        conduit(blockModels, itemModels, ModBlocks.FLUID_PIPE.get(), ModItems.FLUID_PIPE.get());
        itemPipe(blockModels, itemModels, ModBlocks.ITEM_PIPE.get(), ModItems.ITEM_PIPE.get());
        itemPipe(blockModels, itemModels, ModBlocks.FUSION_ITEM_PIPE.get(), ModItems.FUSION_ITEM_PIPE.get());
        // fluid_tank_side has a transparent window, so the model renders cutout; the contents are a renderer
        blockModels.createTrivialBlock(ModBlocks.FLUID_TANK.get(), TexturedModel.COLUMN);
        blockModels.createTrivialBlock(ModBlocks.CONTAINMENT_CELL.get(), TexturedModel.COLUMN);
        tankItem(itemModels, ModBlocks.FLUID_TANK.get());
        tankItem(itemModels, ModBlocks.CONTAINMENT_CELL.get());
        conduit(blockModels, itemModels, ModBlocks.MAGNETIC_CONDUIT.get(), ModItems.MAGNETIC_CONDUIT.get());
        // Particle Accelerator: segments and ports light up while it runs (_on textures); the controller is a furnace
        litColumn(blockModels, ModBlocks.ACCELERATOR_SEGMENT.get());
        acceleratorPort(blockModels, ModBlocks.ACCELERATOR_PORT.get());
        // Event Horizon Reactor: frame lights up while the black hole lives; ports like the accelerator's
        litCube(blockModels, ModBlocks.CONTAINMENT_FRAME.get());
        blockModels.createFurnace(ModBlocks.SINGULARITY_PROCESSOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        conduit(blockModels, itemModels, ModBlocks.SINGULARITY_CABLE.get(), ModItems.SINGULARITY_CABLE.get());
        energyBattery(blockModels, ModBlocks.SINGULARITY_ENERGY_BATTERY.get());
        itemModels.generateFlatItem(ModItems.SINGULARITY_CIRCUIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SINGULARITY_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SPEED_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ENERGY_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GAS_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LIQUID_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        acceleratorPort(blockModels, ModBlocks.HORIZON_PORT.get());
        blockModels.createFurnace(ModBlocks.HORIZON_CONTROLLER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ACCELERATOR_CONTROLLER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        // Soaryn Chest: drawn by the vanilla ChestRenderer; the block model is particles only, the item a special model
        blockModels.createChest(ModBlocks.SOARYN_CHEST.get(), Blocks.OAK_PLANKS,
                ChestSpecialRenderer.REGULAR, true);
        itemModels.generateFlatItem(ModItems.AIR_FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.TINY_COAL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.DRILL_BIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ORE_HAMMER.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        Stream.concat(ModItems.DRILLS.stream(), ModItems.CHAINSAWS.stream())
                .forEach(tool -> itemModels.generateFlatItem(tool.get(), ModelTemplates.FLAT_HANDHELD_ITEM));
        Stream.concat(ModItems.REACTOR_PLATING_TOOLS.all().stream(), ModItems.SINGULARITY_TOOLS.all().stream())
                .forEach(tool -> itemModels.generateFlatItem(tool.get(), ModelTemplates.FLAT_HANDHELD_ITEM));
        itemModels.generateFlatItem(ModItems.ELECTRONIC_CIRCUIT.get(), ModelTemplates.FLAT_ITEM);
        blockModels.createFurnace(ModBlocks.PLASMA_GRANULATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.PLASMA_SMELTER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.CIRCUIT_ASSEMBLER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ORE_WASHER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        energyBattery(blockModels, ModBlocks.PLASMA_ENERGY_BATTERY.get());
        conduit(blockModels, itemModels, ModBlocks.PLASMA_CABLE.get(), ModItems.PLASMA_CABLE.get());
        itemModels.generateFlatItem(ModItems.PLASMA_CIRCUIT.get(), ModelTemplates.FLAT_ITEM);
        for (StoneSet set : ModBlocks.STONE_SETS) {
            StoneFamilies.of(set).all().forEach(family -> blockModels.family(family.getBaseBlock()).generateFor(family));
            // Pillar: <name>_pillar on the sides, <name>_pillar_top on the ends (like quartz pillars)
            blockModels.createRotatedPillarWithHorizontalVariant(set.pillar.get(), TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        }

        // Purpleheart, like vanilla's oak models (leaves keep their own colours: no tint)
        var wood = ModBlocks.PURPLEHEART;
        blockModels.family(wood.planks.get()).generateFor(WoodFamily.of(wood));
        blockModels.woodProvider(wood.log.get()).logWithHorizontal(wood.log.get()).wood(wood.wood.get());
        blockModels.woodProvider(wood.strippedLog.get()).logWithHorizontal(wood.strippedLog.get()).wood(wood.strippedWood.get());
        blockModels.createHangingSign(wood.strippedLog.get(), wood.hangingSign.get(), wood.wallHangingSign.get());
        blockModels.createPlantWithDefaultItem(wood.sapling.get(), wood.pottedSapling.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        blockModels.createTrivialBlock(wood.leaves.get(), TexturedModel.LEAVES);
        itemModels.generateFlatItem(ModItems.PURPLEHEART_BOAT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.PURPLEHEART_CHEST_BOAT.get(), ModelTemplates.FLAT_ITEM);

        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            itemModels.generateFlatItem(metal.granulated().get(), ModelTemplates.FLAT_ITEM);
            itemModels.generateFlatItem(metal.washed().get(), ModelTemplates.FLAT_ITEM);
            itemModels.generateFlatItem(metal.concentrate().get(), ModelTemplates.FLAT_ITEM);
            itemModels.generateFlatItem(metal.purified().get(), ModelTemplates.FLAT_ITEM);
        }
        wrench(itemModels);
    }

    /**
     * One model per charge level, each with its own front (<block>_front_0 .. _front_6). The plain model, with the
     * full _front, is what the item (and the side config screen) shows.
     */
    private static void energyBattery(BlockModelGenerators blockModels, EnergyBatteryBlock battery) {
        TexturedModel.ORIENTABLE_ONLY_TOP.create(battery, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(battery)
                .with(PropertyDispatch.initial(EnergyBatteryBlock.CHARGE).generate(charge -> BlockModelGenerators.plainVariant(
                        TexturedModel.ORIENTABLE_ONLY_TOP.get(battery)
                                .updateTextures(t -> t.put(TextureSlot.FRONT, TextureMapping.getBlockTexture(battery, "_front_" + charge)))
                                .createWithSuffix(battery, "_charge_" + charge, blockModels.modelOutput))))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    /** A cube with one texture on every face, swapped for {@code <block>_on} while {@code lit}. */
    private static void litCube(BlockModelGenerators blockModels, Block block) {
        Identifier off = TexturedModel.CUBE.create(block, blockModels.modelOutput);
        Identifier on = TexturedModel.CUBE.get(block)
                .updateTextures(t -> t.put(TextureSlot.ALL, TextureMapping.getBlockTexture(block, "_on")))
                .createWithSuffix(block, "_on", blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.LIT,
                        BlockModelGenerators.plainVariant(on), BlockModelGenerators.plainVariant(off))));
    }

    /** A column block (<block>_side / <block>_top) with lit variants (<block>_side_on / <block>_top_on). */
    private static void litColumn(BlockModelGenerators blockModels, Block block) {
        Identifier off = TexturedModel.COLUMN.create(block, blockModels.modelOutput);
        Identifier on = TexturedModel.COLUMN.get(block)
                .updateTextures(t -> t
                        .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side_on"))
                        .put(TextureSlot.END, TextureMapping.getBlockTexture(block, "_top_on")))
                .createWithSuffix(block, "_on", blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.LIT,
                        BlockModelGenerators.plainVariant(on), BlockModelGenerators.plainVariant(off))));
    }

    /** The Accelerator Port: Input / Output faces (output = <block>_output_side), each with a lit variant. */
    private static void acceleratorPort(BlockModelGenerators blockModels, Block block) {
        Identifier[] models = new Identifier[4];
        String[] suffixes = {"", "_on", "_output", "_output_on"};
        for (int i = 0; i < 4; i++) {
            String side = (i >= 2 ? "_output_side" : "_side") + (i % 2 == 1 ? "_on" : "");
            String end = i % 2 == 1 ? "_top_on" : "_top";
            String suffix = suffixes[i];
            TextureMapping textures = new TextureMapping()
                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, side))
                    .put(TextureSlot.END, TextureMapping.getBlockTexture(block, end))
                    .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, side));
            models[i] = suffix.isEmpty()
                    ? ModelTemplates.CUBE_COLUMN.create(block, textures, blockModels.modelOutput)
                    : ModelTemplates.CUBE_COLUMN.createWithSuffix(block, suffix, textures, blockModels.modelOutput);
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.LIT,
                                AcceleratorPortBlock.OUTPUT)
                        .select(false, false, BlockModelGenerators.plainVariant(models[0]))
                        .select(true, false, BlockModelGenerators.plainVariant(models[1]))
                        .select(false, true, BlockModelGenerators.plainVariant(models[2]))
                        .select(true, true, BlockModelGenerators.plainVariant(models[3]))));
    }

    /**
     * The arm on one side: the north-half model or the south-half one, turned so that opposite arms share the same
     * turn (a 180 degree turn would flip the stripe on their top faces every half block).
     */
    private static MultiVariant arm(Direction side, MultiVariant near, MultiVariant far) {
        return switch (side) {
            case NORTH -> near;
            case SOUTH -> far;
            case EAST -> near.with(BlockModelGenerators.Y_ROT_90);
            case WEST -> far.with(BlockModelGenerators.Y_ROT_90);
            case UP -> near.with(BlockModelGenerators.X_ROT_270);
            case DOWN -> far.with(BlockModelGenerators.X_ROT_270);
        };
    }

    /** Cables and pipes: arms to the middle, the node only where it isn't straight ({@link ConduitBlock#STRAIGHT}), plates on blocks. */
    private static void conduit(BlockModelGenerators blockModels, ItemModelGenerators itemModels, ConduitBlock cable, Item item) {
        TextureMapping textures = new TextureMapping()
                .put(CABLE, TextureMapping.getBlockTexture(cable))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(cable));
        MultiVariant node = BlockModelGenerators.plainVariant(CONDUIT_NODE.createWithSuffix(cable, "_core", textures, blockModels.modelOutput));
        MultiVariant arm = BlockModelGenerators.plainVariant(CONDUIT_ARM.createWithSuffix(cable, "_arm", textures, blockModels.modelOutput));
        MultiVariant armFar = BlockModelGenerators.plainVariant(CONDUIT_ARM_FAR.createWithSuffix(cable, "_arm_far", textures, blockModels.modelOutput));
        MultiVariant plate = BlockModelGenerators.plainVariant(CABLE_PLATE.createWithSuffix(cable, "_plate", textures, blockModels.modelOutput));
        MultiPartGenerator generator = MultiPartGenerator.multiPart(cable)
                .with(BlockModelGenerators.condition().term(ConduitBlock.STRAIGHT, false), node);
        for (Direction direction : Direction.values()) {
            VariantMutator rotation = switch (direction) {
                case NORTH -> BlockModelGenerators.NOP;
                case EAST -> BlockModelGenerators.Y_ROT_90;
                case SOUTH -> BlockModelGenerators.Y_ROT_180;
                case WEST -> BlockModelGenerators.Y_ROT_270;
                case UP -> BlockModelGenerators.X_ROT_270;
                case DOWN -> BlockModelGenerators.X_ROT_90;
            };
            var property = ConduitBlock.CONNECTIONS.get(direction);
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.CABLE, CableConnection.MACHINE), arm(direction, arm, armFar));
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.MACHINE), plate.with(rotation));
        }
        blockModels.blockStateOutput.accept(generator);
        Identifier inventory = CONDUIT_INVENTORY.createWithSuffix(cable, "_inventory", textures, blockModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(inventory));
    }

    /** Like {@link #conduit} with the Item Pipe's bigger core and plates and its own plate texture (Extract plates: ItemPipeRenderer). */
    private static void itemPipe(BlockModelGenerators blockModels, ItemModelGenerators itemModels, ItemPipeBlock pipe, Item item) {
        // both tiers share the Item Pipe's Insert plate texture
        TextureMapping textures = new TextureMapping()
                .put(CABLE, TextureMapping.getBlockTexture(pipe))
                .put(PIPE_PLATE, TextureMapping.getBlockTexture(ModBlocks.ITEM_PIPE.get(), "_plate"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(pipe));
        MultiVariant core = BlockModelGenerators.plainVariant(ITEM_PIPE_CORE.createWithSuffix(pipe, "_core", textures, blockModels.modelOutput));
        // stuffed: the same core with the red-ringed texture
        TextureMapping stuffedTextures = new TextureMapping()
                .put(CABLE, TextureMapping.getBlockTexture(pipe, "_stuffed"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(pipe));
        MultiVariant stuffedCore = BlockModelGenerators.plainVariant(ITEM_PIPE_CORE.createWithSuffix(pipe, "_core_stuffed", stuffedTextures, blockModels.modelOutput));
        MultiVariant arm = BlockModelGenerators.plainVariant(ITEM_PIPE_ARM.createWithSuffix(pipe, "_arm", textures, blockModels.modelOutput));
        MultiVariant armFar = BlockModelGenerators.plainVariant(ITEM_PIPE_ARM_FAR.createWithSuffix(pipe, "_arm_far", textures, blockModels.modelOutput));
        MultiVariant stub = BlockModelGenerators.plainVariant(ITEM_PIPE_STUB.createWithSuffix(pipe, "_stub", textures, blockModels.modelOutput));
        MultiVariant stubFar = BlockModelGenerators.plainVariant(ITEM_PIPE_STUB_FAR.createWithSuffix(pipe, "_stub_far", textures, blockModels.modelOutput));
        MultiVariant plate = BlockModelGenerators.plainVariant(ITEM_PIPE_PLATE.createWithSuffix(pipe, "_plate", textures, blockModels.modelOutput));
        // the core shows where it isn't straight, and always while stuffed (then red); arms stop at it whenever it shows
        MultiPartGenerator generator = MultiPartGenerator.multiPart(pipe)
                .with(BlockModelGenerators.condition().term(ConduitBlock.STRAIGHT, false).term(ItemPipeBlock.STUFFED, false), core)
                .with(BlockModelGenerators.condition().term(ItemPipeBlock.STUFFED, true), stuffedCore);
        for (Direction direction : Direction.values()) {
            VariantMutator rotation = switch (direction) {
                case NORTH -> BlockModelGenerators.NOP;
                case EAST -> BlockModelGenerators.Y_ROT_90;
                case SOUTH -> BlockModelGenerators.Y_ROT_180;
                case WEST -> BlockModelGenerators.Y_ROT_270;
                case UP -> BlockModelGenerators.X_ROT_270;
                case DOWN -> BlockModelGenerators.X_ROT_90;
            };
            var property = ConduitBlock.CONNECTIONS.get(direction);
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.CABLE, CableConnection.MACHINE)
                    .term(ConduitBlock.STRAIGHT, true).term(ItemPipeBlock.STUFFED, false), arm(direction, arm, armFar));
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.CABLE, CableConnection.MACHINE)
                    .term(ConduitBlock.STRAIGHT, false), arm(direction, stub, stubFar));
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.CABLE, CableConnection.MACHINE)
                    .term(ConduitBlock.STRAIGHT, true).term(ItemPipeBlock.STUFFED, true), arm(direction, stub, stubFar));
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.MACHINE), plate.with(rotation));
        }
        blockModels.blockStateOutput.accept(generator);
        Identifier inventory = ITEM_PIPE_INVENTORY.createWithSuffix(pipe, "_inventory", textures, blockModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(inventory));
    }

    /** One texture per mode: copper grip (Rotate), blue (Cable), green (Configure); no component = Rotate. */
    private static void wrench(ItemModelGenerators itemModels) {
        var wrench = ModItems.WRENCH.get();
        Identifier rotateModel = itemModels.createFlatItemModel(wrench, ModelTemplates.FLAT_HANDHELD_ITEM);
        Identifier cableModel = itemModels.createFlatItemModel(wrench, "_cable", ModelTemplates.FLAT_HANDHELD_ITEM);
        Identifier configureModel = itemModels.createFlatItemModel(wrench, "_configure", ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.itemModelOutput.accept(wrench, ItemModelUtils.select(
                new ComponentContents<>(ModDataComponents.WRENCH_MODE.get()),
                ItemModelUtils.plainModel(rotateModel),
                ItemModelUtils.when(WrenchMode.CABLE, ItemModelUtils.plainModel(cableModel)),
                ItemModelUtils.when(WrenchMode.CONFIGURE, ItemModelUtils.plainModel(configureModel))));
    }
}
