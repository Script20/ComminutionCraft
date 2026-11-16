package com.script.comminutioncraft.common.item.tool;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * The drill and chainsaw tiers (owner, 2026-11-16). Tier 1 runs on a Drill Bit that wears out; tiers 2-6 run on FE and
 * hold half of their tier's Energy Battery. What each tier adds:
 * <ul>
 *   <li>1 Coal: drill only, iron-level, wears its bit</li>
 *   <li>2 Electric: drill, diamond-level</li>
 *   <li>3 Plasma: drill and chainsaw</li>
 *   <li>4 Fusion: the drill mines 3x3, the chainsaw fells every log of a tree in one swing</li>
 *   <li>5 Antimatter: as 4, faster, the drill has Fortune III</li>
 *   <li>6 Singularity: as 5, faster; right-click an ore to vein-mine it, a tree to clear it (logs, leaves) and replant</li>
 * </ul>
 */
public enum ToolTier {
    COAL(1, "coal", 6.0F, BlockTags.INCORRECT_FOR_IRON_TOOL, 0xFF9628, null),
    ELECTRIC(2, "electric", 8.0F, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 0x40C4FF, EnergyBatteryBlockEntity.CAPACITY),
    PLASMA(3, "plasma", 10.0F, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 0xFF50D2, EnergyBatteryBlockEntity.PLASMA_CAPACITY),
    FUSION(4, "fusion", 12.0F, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 0x60FF80, EnergyBatteryBlockEntity.FUSION_CAPACITY),
    ANTIMATTER(5, "antimatter", 16.0F, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 0xD6E4FF, EnergyBatteryBlockEntity.ANTIMATTER_CAPACITY),
    SINGULARITY(6, "singularity", 24.0F, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 0xFFBA38, EnergyBatteryBlockEntity.SINGULARITY_CAPACITY);

    /** Blocks the tier-1 drill's bit lasts. */
    public static final int BIT_USES = 250;

    public final int level;
    public final String id;
    public final float speed;
    public final TagKey<Block> incorrectForDrops;
    public final int colour;
    private final @Nullable ConfigInt batteryCapacity;

    ToolTier(int level, String id, float speed, TagKey<Block> incorrectForDrops, int colour, @Nullable ConfigInt batteryCapacity) {
        this.level = level;
        this.id = id;
        this.speed = speed;
        this.incorrectForDrops = incorrectForDrops;
        this.colour = colour;
        this.batteryCapacity = batteryCapacity;
    }

    /** Runs on FE (tiers 2-6); tier 1 wears a bit instead. */
    public boolean powered() {
        return batteryCapacity != null;
    }

    /** FE it holds: half of its tier's Energy Battery. */
    public int capacity() {
        return batteryCapacity == null ? 0 : batteryCapacity.get() / 2;
    }

    public int energyPerBlock() {
        return powered() ? MachineConfig.TOOL_ENERGY_PER_BLOCK[level].get() : 0;
    }
}
