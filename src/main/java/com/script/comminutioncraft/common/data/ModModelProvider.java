package com.script.comminutioncraft.common.data;

import com.mojang.math.Quadrant;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlock;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.item.WrenchMode;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import com.script.comminutioncraft.common.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.Block;
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

    private static final ModelTemplate CABLE_CORE = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(5, 5, 5).to(11, 11, 11)
                    .allFaces((dir, face) -> face.texture(CABLE).uvs(0, 0, 6, 6)))
            .build();

    /** One arm, pointing north; the blockstate rotates it to the other sides. */
    private static final ModelTemplate CABLE_ARM = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(6, 6, 0).to(10, 10, 5)
                    .face(Direction.NORTH, face -> face.texture(CABLE).uvs(1, 1, 5, 5).cullface(Direction.NORTH))
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(0, 8, 5, 12))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(0, 8, 5, 12))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(0, 8, 5, 12).rotation(Quadrant.R90))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(0, 8, 5, 12).rotation(Quadrant.R90)))
            .build();

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

    /** Item form: the core with a straight run of cable through it, east to west. */
    private static final ModelTemplate CABLE_INVENTORY = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(CABLE).requiredTextureSlot(TextureSlot.PARTICLE)
            .element(e -> e.from(5, 5, 5).to(11, 11, 11)
                    .allFaces((dir, face) -> face.texture(CABLE).uvs(0, 0, 6, 6)))
            .element(e -> e.from(0, 6, 6).to(16, 10, 10)
                    .face(Direction.NORTH, face -> face.texture(CABLE).uvs(0, 8, 16, 12))
                    .face(Direction.SOUTH, face -> face.texture(CABLE).uvs(0, 8, 16, 12))
                    .face(Direction.UP, face -> face.texture(CABLE).uvs(0, 8, 16, 12))
                    .face(Direction.DOWN, face -> face.texture(CABLE).uvs(0, 8, 16, 12))
                    .face(Direction.EAST, face -> face.texture(CABLE).uvs(1, 1, 5, 5))
                    .face(Direction.WEST, face -> face.texture(CABLE).uvs(1, 1, 5, 5)))
            .build();

    public ModModelProvider(PackOutput output) {
        super(output, ComminutionCraft.MODID);
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
        // Fusion Reactor: casing and ports glow while it burns (an _on texture); the glass is cutout; the controller is
        // a furnace-style front over casing sides.
        litCube(blockModels, ModBlocks.REACTOR_CASING.get());
        litCube(blockModels, ModBlocks.REACTOR_PORT.get());
        blockModels.createTrivialCube(ModBlocks.REACTOR_GLASS.get());
        blockModels.createFurnace(ModBlocks.REACTOR_CONTROLLER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        conduit(blockModels, itemModels, ModBlocks.FUSION_CABLE.get(), ModItems.FUSION_CABLE.get());
        energyBattery(blockModels, ModBlocks.FUSION_ENERGY_BATTERY.get());
        blockModels.createFurnace(ModBlocks.ANNIHILATION_GENERATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        conduit(blockModels, itemModels, ModBlocks.ANTIMATTER_CABLE.get(), ModItems.ANTIMATTER_CABLE.get());
        energyBattery(blockModels, ModBlocks.ANTIMATTER_ENERGY_BATTERY.get());
        itemModels.generateFlatItem(ModItems.ANTIMATTER_CIRCUIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FUSION_CIRCUIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.REACTOR_PLATING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FUSION_FUEL_PELLET.get(), ModelTemplates.FLAT_ITEM);
        conduit(blockModels, itemModels, ModBlocks.FLUID_PIPE.get(), ModItems.FLUID_PIPE.get());
        // fluid_tank_side has a transparent window, so the model renders cutout; the contents are a renderer
        blockModels.createTrivialBlock(ModBlocks.FLUID_TANK.get(), TexturedModel.COLUMN);
        blockModels.createTrivialBlock(ModBlocks.CONTAINMENT_CELL.get(), TexturedModel.COLUMN);
        conduit(blockModels, itemModels, ModBlocks.MAGNETIC_CONDUIT.get(), ModItems.MAGNETIC_CONDUIT.get());
        // Particle Accelerator: segments and ports light up while it runs (_on textures); the controller is a furnace
        litColumn(blockModels, ModBlocks.ACCELERATOR_SEGMENT.get());
        litColumn(blockModels, ModBlocks.ACCELERATOR_PORT.get());
        blockModels.createFurnace(ModBlocks.ACCELERATOR_CONTROLLER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        // Soaryn Chest: drawn by the vanilla ChestRenderer; the block model is particles only, the item a special model
        blockModels.createChest(ModBlocks.SOARYN_CHEST.get(), net.minecraft.world.level.block.Blocks.OAK_PLANKS,
                net.minecraft.client.renderer.special.ChestSpecialRenderer.REGULAR, true);
        itemModels.generateFlatItem(ModItems.AIR_FILTER.get(), ModelTemplates.FLAT_ITEM);
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

        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            itemModels.generateFlatItem(metal.granulated().get(), ModelTemplates.FLAT_ITEM);
            itemModels.generateFlatItem(metal.washed().get(), ModelTemplates.FLAT_ITEM);
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

    /** Cables and pipes: core + one arm per connected side + a plate where it meets a machine (same UV sheet layout). */
    private static void conduit(BlockModelGenerators blockModels, ItemModelGenerators itemModels, ConduitBlock cable, Item item) {
        TextureMapping textures = new TextureMapping()
                .put(CABLE, TextureMapping.getBlockTexture(cable))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(cable));
        MultiVariant core = BlockModelGenerators.plainVariant(CABLE_CORE.createWithSuffix(cable, "_core", textures, blockModels.modelOutput));
        MultiVariant arm = BlockModelGenerators.plainVariant(CABLE_ARM.createWithSuffix(cable, "_arm", textures, blockModels.modelOutput));
        MultiVariant plate = BlockModelGenerators.plainVariant(CABLE_PLATE.createWithSuffix(cable, "_plate", textures, blockModels.modelOutput));

        MultiPartGenerator generator = MultiPartGenerator.multiPart(cable).with(core);
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
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.CABLE, CableConnection.MACHINE), arm.with(rotation));
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.MACHINE), plate.with(rotation));
        }
        blockModels.blockStateOutput.accept(generator);

        Identifier inventory = CABLE_INVENTORY.createWithSuffix(cable, "_inventory", textures, blockModels.modelOutput);
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
