package com.script.comminutioncraft.common.machine.control;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * A machine face named relative to its front, so side settings stay put when the machine is rotated.
 * Left and right are as seen by a player looking at the machine's front.
 */
public enum RelativeSide {
    FRONT("front"),
    BACK("back"),
    LEFT("left"),
    RIGHT("right"),
    TOP("top"),
    BOTTOM("bottom");

    private final String name;

    RelativeSide(String name) {
        this.name = name;
    }

    /** Which relative side a world direction is, for a machine whose front points {@code facing}. */
    public static RelativeSide of(Direction facing, Direction side) {
        if (side == Direction.UP) {
            return TOP;
        }
        if (side == Direction.DOWN) {
            return BOTTOM;
        }
        if (side == facing) {
            return FRONT;
        }
        if (side == facing.getOpposite()) {
            return BACK;
        }
        // Standing in front you look towards facing.getOpposite(); your left is clockwise of the front.
        return side == facing.getClockWise() ? LEFT : RIGHT;
    }

    public Direction toDirection(Direction facing) {
        return switch (this) {
            case FRONT -> facing;
            case BACK -> facing.getOpposite();
            case LEFT -> facing.getClockWise();
            case RIGHT -> facing.getCounterClockWise();
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
        };
    }

    public Component displayName() {
        return Component.translatable("gui.comminutioncraft.side." + name);
    }
}
