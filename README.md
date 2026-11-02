# ComminutionCraft

Ore processing and doubling for Minecraft 26.1.2 (NeoForge 26.1.2.124).

## Building

Needs a Java 25 JDK (Gradle will fetch one automatically if it can). Gradle itself must be started
with Java 17 or newer, so point `JAVA_HOME` at a modern JDK.

```
./gradlew build          # jar ends up in build/libs/
./gradlew runClient      # launch a dev client with the mod
./gradlew runData        # regenerate recipes, tags, models, loot tables and lang
./gradlew runGameTestServer   # run the automated in-game tests, then exit
```

On Windows use `gradlew.bat`. In IntelliJ, open the folder as a Gradle project.

## Tier 1 (fuel-burning) machines

| Machine | Does | Speed | Items per coal |
|---|---|---|---|
| Vanilla furnace | smelting | 200 ticks | 8 |
| Coal Granulator | raw ore → 2 granulated ore | 150 ticks (33% faster) | 10.67 (33% more) |
| Coal Smelter | any smelting recipe | 100 ticks (2× faster) | 10 |

Both burn any furnace fuel (coal, charcoal, lava buckets, etc.) and scale its burn time the same way.

Tuning lives in the block entity classes:
- `CoalGranulatorBlockEntity.PROCESS_TIME` / `BURN_TIME_MULTIPLIER`
- `CoalSmelterBlockEntity.PROCESS_TIME` / `BURN_TIME_MULTIPLIER`

Items per coal = 1600 × multiplier ÷ process time.

Hoppers and pipes: by default the bottom is the output and every other side is an input
(input sides take both ores and fuel). Change it per side, see "Machine settings" below.

Like a vanilla furnace, the machines store the XP from smelting recipes. It drops when you
take items out of the output slot, or when the machine is broken.

## Tier 2 (electric) machines

| Block | Does | Speed | Power |
|---|---|---|---|
| Electric Generator | burns any furnace fuel into FE | — | 40 FE/t, 32,000 FE per coal |
| Electric Granulator | raw ore → 2 granulated ore | 75 ticks (2× the Coal Granulator) | 27 FE/t (~15.8 ores per coal) |
| Electric Smelter | any smelting recipe | 50 ticks (2× the Coal Smelter) | 40 FE/t (16 items per coal) |

One generator runs one Electric Smelter, or an Electric Granulator with power to spare.
Machines hold 20,000 FE and pause (keeping their progress) when they run out.

The generator pushes power out of every side, either straight into an adjacent machine or into
**Energy Cables**. Cables join up automatically and share power evenly between every machine
on the network (up to 1,000 FE per transfer).

### Wrench

Sneak + right-click to switch between three modes:
- **Rotate** (copper grip): right-click a block to turn it (machines, logs, observers, stairs...).
- **Cable** (blue grip): right-click one arm of a cable to cut that connection; right-click the same
  spot (or the cable on the other side) to restore it.
- **Configure** (green grip): right-click a machine face to cycle it between Input, Output and None.

## Machine settings

Every machine has two tabs on the right of its GUI:

- **Redstone** (gunpowder / redstone dust / redstone torch): **Ignore** always runs, **High** runs only
  with a redstone signal, **Low** runs only without one. A machine switched off by redstone pauses:
  fuel stops burning, progress is kept.
- **Side configuration** (wrench icon): shows the machine as a cube. Left-click a face to cycle it
  between **Input**, **Output** and **None** (for hoppers and pipes); right-drag to turn the cube,
  or right-click to turn it a quarter. Sides are relative to the front, so they turn with the machine.

The wrench's **Configure** mode does the same in the world: right-click a machine face to cycle it.

## Mod support

- **JEI** (optional): a Granulating category for both granulators; the smelters show under Smelting
  and everything that burns fuel under Fuel; click a GUI's progress arrow or flame to open the recipes.
- **Jade** (optional): look at a machine to see its input, progress and output (and stored FE);
  the generator shows its fuel and how long it will keep generating.
- **Other mods' metals**: tin, lead, silver, nickel, zinc, aluminum, osmium and uranium are granulated
  too, as soon as any installed mod adds that metal (its `c:raw_materials/<metal>` and `c:ingots/<metal>`
  tags). Granulated forms smelt into whichever ingot is first in `c:ingots/<metal>`. Metals no mod
  provides stay hidden: no recipes, and no item in the creative tab or JEI.

## Decorative stone

Two new stones, each with the full vanilla-stone set: natural, cobbled, mossy cobbled, polished,
smooth, bricks (plus cracked, mossy and chiseled), a pillar, and stairs, slabs, walls, a button and a
pressure plate. Everything can be made in a stonecutter.

- **Marble** — white, softly veined. Generates underground in big blobs, just like granite and diorite.
- **Volcanic Basalt** — near-black and pitted. Forms a crust around underground lava pools,
  including the lava lakes deep below.

Natural stone drops its cobbled form (use silk touch to keep it); smelt cobbled back into natural,
and natural into smooth.

## Recipes

- All recipes, tags, models, loot tables and translations are generated by datagen
  (`src/main/java/.../data`) into `src/generated/resources`. Don't edit the generated JSON:
  change the providers and run `runData`.
- Granulating recipes take the common tags `#c:raw_materials/<metal>`, so raw ores from other
  mods that use those tags work too.
- Granulated ores are tagged `#c:dusts/<metal>` for compatibility, and smelt in a vanilla
  furnace (normal speed), a blast furnace, or the Coal/Electric Smelter.
- Coal Granulator: flint, iron ingot, flint / cobblestone, furnace, cobblestone / 3 cobblestone.
- Coal Smelter: 3 bricks / brick, furnace, brick / 3 cobblestone.
- Electric Generator: 3 iron / redstone, furnace, redstone / 3 copper.
- Electric Granulator / Smelter: iron, redstone, iron / copper, Coal Granulator (or Smelter), copper / iron, redstone, iron.
- Energy Cable ×8: 3 copper / 3 redstone / 3 copper.
- Wrench: iron, _, iron / _, copper, _ / _, iron, _.

## Textures

Machine and GUI textures use the "Dark Chassis" style and are generated by scripts in `tools/`
(`dark_chassis_system.py` and `gen_tier2_textures.py` for blocks and items, `gui_dark_chassis.py` for GUIs,
`gen_stone_textures.py` for marble and volcanic basalt, `gen_granulated_textures.py` for other
mods' granulated metals; all need Pillow).
The lit machine fronts are 8-frame animations (`*_front_on.png` + `.mcmeta`).

## License

MIT, see [LICENSE](LICENSE).
