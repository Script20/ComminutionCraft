package com.script.comminutioncraft.common.machine.control;

import com.script.comminutioncraft.common.registry.ModDataComponents;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A machine's player settings: its {@link RedstoneMode} and a {@link SideMode} per {@link RelativeSide}.
 * Saved with the block entity, synced to open GUIs through two menu data slots ({@link #packSides()} fits 6 sides
 * x 2 bits in one 16-bit slot), and changed from the GUI with menu button clicks ({@link #handleButton}).
 */
public final class MachineControls {
    /** Menu button ids: 0 cycles the redstone mode, 1..6 cycle {@code RelativeSide.values()[id - 1]}. */
    public static final int BUTTON_REDSTONE = 0;
    public static final int BUTTON_FIRST_SIDE = 1;

    private final EnumMap<RelativeSide, SideMode> defaults;
    private final EnumMap<RelativeSide, SideMode> sides;
    private final Runnable onChanged;
    private final boolean byproduct;
    private RedstoneMode redstone = RedstoneMode.IGNORE;

    /** @param onChanged called after any change (save the block entity, refresh hopper/pipe capabilities) */
    public MachineControls(Map<RelativeSide, SideMode> defaults, Runnable onChanged) {
        this(defaults, onChanged, false);
    }

    /** @param byproduct whether faces can also be set to {@link SideMode#BYPRODUCT} */
    public MachineControls(Map<RelativeSide, SideMode> defaults, Runnable onChanged, boolean byproduct) {
        this.defaults = new EnumMap<>(defaults);
        this.sides = new EnumMap<>(defaults);
        this.onChanged = onChanged;
        this.byproduct = byproduct;
    }

    public boolean hasByproduct() {
        return byproduct;
    }

    /** The usual layout: output at the bottom, input everywhere else. */
    public static Map<RelativeSide, SideMode> outputBelow() {
        EnumMap<RelativeSide, SideMode> map = new EnumMap<>(RelativeSide.class);
        for (RelativeSide side : RelativeSide.values()) {
            map.put(side, side == RelativeSide.BOTTOM ? SideMode.OUTPUT : SideMode.INPUT);
        }
        return map;
    }

    // ---- Redstone ----

    public RedstoneMode redstone() {
        return redstone;
    }

    public void setRedstone(RedstoneMode mode) {
        redstone = mode;
        onChanged.run();
    }

    public boolean canRun(Level level, BlockPos pos) {
        return redstone.allows(level.hasNeighborSignal(pos));
    }

    // ---- Sides ----

    public SideMode side(RelativeSide side) {
        return sides.get(side);
    }

    /** The mode of a world-facing side of a machine whose front points {@code facing}. */
    public SideMode side(Direction facing, Direction worldSide) {
        return sides.get(RelativeSide.of(facing, worldSide));
    }

    public void setSide(RelativeSide side, SideMode mode) {
        sides.put(side, mode);
        onChanged.run();
    }

    public SideMode cycleSide(RelativeSide side) {
        SideMode next = sides.get(side).next(byproduct);
        setSide(side, next);
        return next;
    }

    public int packSides() {
        int packed = 0;
        for (RelativeSide side : RelativeSide.values()) {
            packed |= sides.get(side).ordinal() << (side.ordinal() * 2);
        }
        return packed;
    }

    public static SideMode unpackSide(int packed, RelativeSide side) {
        return SideMode.byOrdinal((packed >> (side.ordinal() * 2)) & 0b11);
    }

    private void unpackSides(int packed) {
        for (RelativeSide side : RelativeSide.values()) {
            sides.put(side, unpackSide(packed, side));
        }
    }

    // ---- GUI buttons ----

    /** Applies a menu button click. Returns false for ids that aren't ours. */
    public boolean handleButton(int id) {
        if (id == BUTTON_REDSTONE) {
            setRedstone(redstone.next());
            return true;
        }
        int sideIndex = id - BUTTON_FIRST_SIDE;
        if (sideIndex >= 0 && sideIndex < RelativeSide.values().length) {
            cycleSide(RelativeSide.values()[sideIndex]);
            return true;
        }
        return false;
    }

    /** For menus' {@code clickMenuButton}: on the server the menu's container is the machine itself. */
    public static boolean handleMenuButton(Container container, int id) {
        return container instanceof ControllableMachine machine && machine.getControls().handleButton(id);
    }

    // ---- Kept on the item when picked up with the wrench ----

    public MachineSettings settings() {
        return new MachineSettings(redstone, packSides());
    }

    public void saveTo(DataComponentMap.Builder components) {
        components.set(ModDataComponents.MACHINE_SETTINGS.get(), settings());
    }

    /** Restores the settings from the item the machine was placed from, if it has any. */
    public void loadFrom(DataComponentGetter components) {
        MachineSettings saved = components.get(ModDataComponents.MACHINE_SETTINGS.get());
        if (saved != null) {
            redstone = saved.redstone();
            unpackSides(saved.sides());
            onChanged.run();
        }
    }

    // ---- Saving ----

    public void save(ValueOutput output) {
        output.putInt("redstone_mode", redstone.ordinal());
        output.putInt("side_config", packSides());
    }

    public void load(ValueInput input) {
        redstone = RedstoneMode.byOrdinal(input.getIntOr("redstone_mode", 0));
        sides.putAll(defaults);
        input.getInt("side_config").ifPresent(this::unpackSides);
    }
}
