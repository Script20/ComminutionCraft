package com.script.comminutioncraft.data;

import com.mojang.math.Quadrant;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.block.StoneSet;
import com.script.comminutioncraft.cable.CableConnection;
import com.script.comminutioncraft.cable.EnergyCableBlock;
import com.script.comminutioncraft.registry.ModBlocks;
import com.script.comminutioncraft.registry.ModDataComponents;
import com.script.comminutioncraft.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Blockstates, block models and item models. Machines use vanilla's furnace layout, which expects
 * textures named <block>_top, _side, _front and _front_on (the lit, animated front).
 */
public class ModModelProvider extends ModelProvider {
    /**
     * Energy cable texture layout (16x16): core face at u0-6 v0-6, connector plate at u8-16 v0-8,
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
        energyCable(blockModels, itemModels);
        for (StoneSet set : ModBlocks.STONE_SETS) {
            StoneFamilies.of(set).all().forEach(family -> blockModels.family(family.getBaseBlock()).generateFor(family));
            // Pillar: <name>_pillar on the sides, <name>_pillar_top on the ends (like quartz pillars)
            blockModels.createRotatedPillarWithHorizontalVariant(set.pillar.get(), TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        }

        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            itemModels.generateFlatItem(metal.granulated().get(), ModelTemplates.FLAT_ITEM);
        }
        wrench(itemModels);
    }

    private static void energyCable(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        EnergyCableBlock cable = ModBlocks.ENERGY_CABLE.get();
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
            var property = EnergyCableBlock.CONNECTIONS.get(direction);
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.CABLE, CableConnection.MACHINE), arm.with(rotation));
            generator.with(BlockModelGenerators.condition().term(property, CableConnection.MACHINE), plate.with(rotation));
        }
        blockModels.blockStateOutput.accept(generator);

        Identifier inventory = CABLE_INVENTORY.createWithSuffix(cable, "_inventory", textures, blockModels.modelOutput);
        itemModels.itemModelOutput.accept(ModItems.ENERGY_CABLE.get(), ItemModelUtils.plainModel(inventory));
    }

    /** Two textures: the plain wrench (Rotate mode) and one with blue tips (Cable mode). */
    private static void wrench(ItemModelGenerators itemModels) {
        var wrench = ModItems.WRENCH.get();
        Identifier rotateModel = itemModels.createFlatItemModel(wrench, ModelTemplates.FLAT_HANDHELD_ITEM);
        Identifier cableModel = itemModels.createFlatItemModel(wrench, "_cable", ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.itemModelOutput.accept(wrench, ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(ModDataComponents.WRENCH_CABLE_MODE.get()),
                ItemModelUtils.plainModel(cableModel),
                ItemModelUtils.plainModel(rotateModel)));
    }
}
