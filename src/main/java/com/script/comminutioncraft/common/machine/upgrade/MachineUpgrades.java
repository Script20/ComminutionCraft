package com.script.comminutioncraft.common.machine.upgrade;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A machine's upgrade slots: one per {@link UpgradeType}, up to {@link UpgradeType#MAX} each, and what they do.
 * <ul>
 *   <li>Speed: x(1 + 0.5 n) speed (8 = 5x), but each one also costs 25% more FE per operation.</li>
 *   <li>Energy: x0.84^n FE per operation (8 = about a quarter) and x(1 + 0.5 n) FE buffer.</li>
 *   <li>Gas / Liquid: x0.84^n of that kind of fluid per operation. Gases are {@link #GASES}
 *       ({@code #comminutioncraft:gases}: hydrogen, oxygen, plasma, deuterium, tritium); every other fluid is a liquid.</li>
 * </ul>
 * FE per tick = base x speed x FE-per-operation multiplier, and the machine's FE input limit grows the same way so it
 * can keep up.
 * <p>
 * Speed and Energy fit every machine; Gas only one that uses a gas, Liquid only one that uses a liquid (from the
 * fluids the machine consumes, checked when needed since the gas tag is datapack data). The client's copy gets the
 * answer as a synced bit mask ({@link #allowedMask()}).
 */
public class MachineUpgrades extends SimpleContainer {
    public static final TagKey<Fluid> GASES = TagKey.create(Registries.FLUID, ComminutionCraft.id("gases"));
    private static final double SPEED_STEP = 0.5;
    private static final double SPEED_COST_STEP = 0.25;
    private static final double SAVING = 0.84;
    private static final double BUFFER_STEP = 0.5;

    private final Runnable onChanged;
    private final IntSupplier allowedMask;

    /** A machine's upgrades; {@code consumed} = the fluids it uses up (decides Gas / Liquid). */
    public MachineUpgrades(Runnable onChanged, Supplier<Collection<Fluid>> consumed) {
        super(UpgradeType.values().length);
        this.onChanged = onChanged;
        this.allowedMask = () -> maskFor(consumed.get());
    }

    /** The client's stand-in in a machine menu: what fits comes from the synced mask. */
    public static MachineUpgrades client(IntSupplier mask) {
        return new MachineUpgrades(mask);
    }

    private MachineUpgrades(IntSupplier mask) {
        super(UpgradeType.values().length);
        this.onChanged = () -> {};
        this.allowedMask = mask;
    }

    private static int maskFor(Collection<Fluid> consumed) {
        int mask = (1 << UpgradeType.SPEED.ordinal()) | (1 << UpgradeType.ENERGY.ordinal());
        for (Fluid fluid : consumed) {
            mask |= 1 << (isGas(fluid) ? UpgradeType.GAS : UpgradeType.LIQUID).ordinal();
        }
        return mask;
    }

    /** Bit per {@link UpgradeType} ordinal: which upgrades this machine takes. */
    public int allowedMask() {
        return allowedMask.getAsInt();
    }

    public boolean allows(UpgradeType type) {
        return (allowedMask() & (1 << type.ordinal())) != 0;
    }

    public int count(UpgradeType type) {
        ItemStack stack = getItem(type.ordinal());
        return stack.is(type.item()) && allows(type) ? Math.min(stack.getCount(), UpgradeType.MAX) : 0;
    }

    /** The speed multiplier N Speed upgrades give. */
    public static double speedFor(int n) {
        return 1 + SPEED_STEP * n;
    }

    /** The share of FE or fluid N Energy / Gas / Liquid upgrades save (0..1). */
    public static double savingFor(int n) {
        return 1 - remaining(n);
    }

    /** What's left to pay after N Energy / Gas / Liquid upgrades (1 = full cost). */
    private static double remaining(int n) {
        return Math.pow(SAVING, n);
    }

    public double speed() {
        return speedFor(count(UpgradeType.SPEED));
    }

    /** FE per operation, relative to no upgrades. */
    public double energyPerOperation() {
        return (1 + SPEED_COST_STEP * count(UpgradeType.SPEED)) * remaining(count(UpgradeType.ENERGY));
    }

    /** FE per tick, relative to no upgrades. */
    public double energyPerTick() {
        return speed() * energyPerOperation();
    }

    public double bufferSize() {
        return 1 + BUFFER_STEP * count(UpgradeType.ENERGY);
    }

    public static boolean isGas(Fluid fluid) {
        return fluid.defaultFluidState().is(GASES);
    }

    /** How much of this fluid an operation uses, relative to no upgrades. */
    public double fluidUse(Fluid fluid) {
        return remaining(count(isGas(fluid) ? UpgradeType.GAS : UpgradeType.LIQUID));
    }

    /** A per-operation fluid cost after the Gas / Liquid upgrades (never below 1 mB if it was more than 0). */
    public int fluidCost(Fluid fluid, int amount) {
        return amount <= 0 ? amount : Math.max(1, (int) Math.ceil(amount * fluidUse(fluid) - 1e-9));
    }

    public int processTime(int base) {
        return Math.max(1, (int) Math.round(base / speed()));
    }

    public int energyPerTick(int base) {
        return base <= 0 ? base : Math.max(1, (int) Math.round(base * energyPerTick()));
    }

    // ---- Container: one type per slot, up to 8 ----

    @Override
    public int getMaxStackSize() {
        return UpgradeType.MAX;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        UpgradeType type = UpgradeType.values()[slot];
        return UpgradeType.of(stack) == type && allows(type);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onChanged.run();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // ---- Saving, and the item a machine becomes when picked up with the wrench ----

    public void save(ValueOutput output) {
        ContainerHelper.saveAllItems(output.child("upgrades"), getItems());
    }

    public void load(ValueInput input) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input.childOrEmpty("upgrades"), stacks);
        for (int i = 0; i < stacks.size(); i++) {
            super.setItem(i, stacks.get(i));
        }
        onChanged.run();
    }

    /** Counts in slot order, on the picked-up item ({@code comminutioncraft:upgrades}). */
    public void saveTo(DataComponentMap.Builder components) {
        if (!isEmpty()) {
            List<Integer> counts = new ArrayList<>();
            for (UpgradeType type : UpgradeType.values()) {
                counts.add(count(type));
            }
            components.set(ModDataComponents.UPGRADES.get(), counts);
        }
    }

    public void loadFrom(DataComponentGetter components) {
        List<Integer> counts = components.get(ModDataComponents.UPGRADES.get());
        if (counts == null) {
            return;
        }
        for (UpgradeType type : UpgradeType.values()) {
            int n = type.ordinal() < counts.size() ? Math.clamp(counts.get(type.ordinal()), 0, UpgradeType.MAX) : 0;
            super.setItem(type.ordinal(), n > 0 ? new ItemStack(type.item(), n) : ItemStack.EMPTY);
        }
        onChanged.run();
    }
}
