package com.script.comminutioncraft.common.item.tool;

import com.script.comminutioncraft.common.registry.ModDataComponents;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * A drill or chainsaw of one {@link ToolTier}. Tier 1 wears its bit (the item's durability, which stops one short of
 * breaking: craft the drill with a new Drill Bit to replace it); tiers 2-6 use FE, held in the
 * {@code comminutioncraft:stored_energy} component and charged in an Energy Battery's charge slot. Without power
 * (a worn bit, or less FE than one block costs) it mines like a bare hand and gets no drops.
 */
public abstract class PoweredTool extends Item {
    protected final ToolTier tier;

    protected PoweredTool(ToolTier tier, TagKey<Block> mineable, float attackDamage, float attackSpeed, Properties properties) {
        super(toolProperties(properties, tier, mineable, attackDamage, attackSpeed));
        this.tier = tier;
    }

    private static Properties toolProperties(Properties properties, ToolTier tier, TagKey<Block> mineable, float damage, float speed) {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        Tool tool = new Tool(List.of(Tool.Rule.deniesDrops(blocks.getOrThrow(tier.incorrectForDrops)),
                Tool.Rule.minesAndDrops(blocks.getOrThrow(mineable), tier.speed)), 1.0F, tier.powered() ? 0 : 1, true);
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
        properties = properties.component(DataComponents.TOOL, tool).attributes(attributes);
        return tier.powered() ? properties.stacksTo(1) : properties.durability(ToolTier.BIT_USES);
    }

    public ToolTier tier() {
        return tier;
    }

    public static int energy(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORED_ENERGY.get(), 0);
    }

    /** Enough left for one more block: FE for one block, or a bit that isn't worn out. */
    public boolean hasPower(ItemStack stack) {
        return tier.powered() ? energy(stack) >= tier.energyPerBlock() : stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    /** Whether the bit is worn out (tier 1). */
    public boolean bitWorn(ItemStack stack) {
        return !tier.powered() && !hasPower(stack);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return hasPower(stack) ? super.getDestroySpeed(stack, state) : 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return hasPower(stack) && super.isCorrectToolForDrops(stack, state);
    }

    /** Tier 1: the bit wears down to its last use but never breaks (the drill stays; it needs a new bit). */
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return Math.max(0, Math.min(amount, stack.getMaxDamage() - 1 - stack.getDamageValue()));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        boolean result = super.mineBlock(stack, level, state, pos, owner); // tier 1: wears the bit
        if (level.isClientSide() || state.getDestroySpeed(level, pos) == 0.0F) {
            return result;
        }
        if (tier.powered()) {
            stack.set(ModDataComponents.STORED_ENERGY.get(), Math.max(0, energy(stack) - tier.energyPerBlock()));
        }
        if (owner instanceof ServerPlayer player && !AreaBreaker.busy()) {
            afterMining(stack, (ServerLevel) level, state, pos, player);
        }
        return result;
    }

    /** After the player broke a block with it (not one of the extra blocks it breaks itself): the area abilities. */
    protected abstract void afterMining(ItemStack stack, ServerLevel level, BlockState state, BlockPos pos,
                                        ServerPlayer player);

    /** Breaks one more block as the player if there's power left for it; false once the power ran out. */
    protected boolean breakExtra(ItemStack stack, ServerPlayer player, BlockPos pos) {
        if (!hasPower(stack)) {
            return false;
        }
        AreaBreaker.breakAs(player, pos);
        return true;
    }

    // ---- the charge bar (FE) on powered tiers; tier 1 shows its bit's durability as usual ----

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return tier.powered() || super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (!tier.powered()) {
            return super.getBarWidth(stack);
        }
        return Math.round(13.0F * Math.min(1.0F, (float) energy(stack) / Math.max(1, tier.capacity())));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return tier.powered() ? tier.colour : super.getBarColor(stack);
    }
}
