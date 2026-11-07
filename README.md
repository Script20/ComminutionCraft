<img width="1000" height="1000" alt="AI-W" src="https://github.com/user-attachments/assets/13f460a4-b998-4976-a12c-4d2526007a6a" />
<img width="1000" height="320" alt="Banner" src="https://github.com/user-attachments/assets/1cf815bc-254c-487b-a68b-bf64684e872b" />

# Comminution Craft

A tech mod about ore processing for Minecraft **26.1.2** (NeoForge **26.1.2.124**). Grind, wash, purify and
eventually crush ore in a black hole's gravity, across six power tiers, from a coal-burning granulator that doubles
your ore to an endgame machine that turns one raw ore into five ingots.

Mod id `comminutioncraft`. License MIT.

## The six tiers

| # | Tier | Power | Best ore output | Key machines |
|---|---|---|---|---|
| 1 | **Coal** | burns furnace fuel | 2x | Coal Granulator, Coal Smelter |
| 2 | **Electric** | FE from generators | 2x, faster | Electric Generator, Granulator, Smelter, Energy Battery |
| 3 | **Plasma** | FE; own power chain: water from the air, then hydrogen, then plasma, then FE | 3x | Ore Washer, Circuit Assembler, MHD Generator |
| 4 | **Fusion** | 3x3x3 fusion reactor burning fuel pellets | 3x + by-products | Fusion Reactor, Ore Purifier |
| 5 | **Antimatter** | antimatter from a particle accelerator ring | 4x | Particle Accelerator, Froth Flotation Cell, Annihilation Generator |
| 6 | **Singularity** | a contained black hole that eats any item | 5x + rare drops | Event Horizon Reactor, Singularity Processor |

Every powered machine takes FE, so generators from other mods work too. Higher tiers add their own resource on top
(plasma, fuel pellets, antimatter, a nearby black hole). Each tier's machines carry a glowing seam in the tier's
colour: orange, blue, magenta, green, icy white, gold.

All the numbers below are defaults. Every one of them can be changed in the config (see [Configuration](#configuration)).
JEI shows every recipe and every machine's numbers in game.

## Ore processing

| Tier | Chain | Ingots per raw ore |
|---|---|---|
| 1-2 | raw ore → **Granulator** → 2 granulated ore → smelt | 2 |
| 3 | raw ore (or ore block) → **Ore Washer** → 3 washed ore → Electric/Plasma Granulator → granulated → smelt | 3 |
| 4 | washed ore → **Ore Purifier** (+ oxygen) → purified ore (+ 20% by-product) → granulator → smelt | 3 + by-products |
| 5 | raw ore (or ore block) → **Froth Flotation Cell** → 4 concentrate → washer → purifier → granulator → smelt | 4 |
| 6 | raw ore (or ore block) → **Singularity Processor** → 5 ingots (+ rare drops) | 5 |

- Granulated ores are tagged `c:dusts/<metal>` and smelt in a furnace, a blast furnace or any of the smelters.
- Purifier by-products follow real ores: iron gives nickel, copper gives gold, gold and lead give silver. When the
  partner metal isn't in the pack, you get a vanilla nugget instead.
- Singularity Processor drops: diamond 3%, emerald 2%, Singularity Shard 2%, netherite scrap 0.5%, nether star 0.05%.
- **Other mods' metals** are processed too: tin, lead, silver, nickel, zinc, aluminum, osmium and uranium, as soon as
  an installed mod adds that metal (its `c:raw_materials/<metal>` and `c:ingots/<metal>` tags). They smelt into the
  first item of `c:ingots/<metal>`. Metals no mod provides stay hidden: no recipes, nothing in the creative tab or JEI.

## Tier 1 - Coal

| Machine | Does | Speed | Fuel |
|---|---|---|---|
| Coal Granulator | raw ore → 2 granulated ore | 150 ticks | ~10.7 ores per coal |
| Coal Smelter | any smelting recipe | 100 ticks | 10 items per coal |

Both burn any furnace fuel. Like a furnace, they store smelting XP, which drops when you take the output or break the machine.

## Tier 2 - Electric

| Block | Numbers |
|---|---|
| Electric Generator | burns furnace fuel into 40 FE/t (32,000 FE per coal); pushes up to 400 FE/t out of every side |
| Electric Granulator | 75 ticks at 27 FE/t |
| Electric Smelter | 50 ticks at 40 FE/t |
| Energy Battery | 1,000,000 FE, 1,000 FE/t in and out; its front shows the charge, comparators read it |
| Energy Cable | 1,000 FE/t per network, shared evenly between the machines on it |

The **Electronic Circuit** (redstone + copper) goes into most tier-2 recipes. Machines pause without power and keep their progress.

## Tier 3 - Plasma

**Machines** (FE only):

| Machine | Does | Numbers |
|---|---|---|
| Plasma Granulator | like the electric one, faster | 40 ticks at 60 FE/t |
| Plasma Smelter | like the electric one, faster | 25 ticks at 80 FE/t |
| Ore Washer | raw ore or ore block → 3 washed ore | 60 ticks at 80 FE/t + 250 mB water |
| Circuit Assembler | Electronic Circuit + 100 mB plasma → Plasma Circuit (later also the fusion, antimatter and singularity parts) | 100 ticks at 20 FE/t |

The Ore Washer takes water from pipes, or pulls it from water source blocks touching it.

**Power chain:**

1. **Atmospheric Water Generator**: water from the air. 1 mB/t per side open to the air, times the biome's humidity
   (deserts ~5%, plains ~65%, jungles and rivers 120-140%). An **Air Filter** doubles it. Needs no FE.
2. **Electrolyser**: 2 water → 1 hydrogen + ½ oxygen, 10 FE per mB. Hydrogen leaves by Output faces, oxygen by
   By-product faces (keep it: tiers 4 and 5 use it).
3. **Plasma Generator**: hydrogen → plasma 1:1, 16 FE per mB.
4. **MHD Generator**: plasma → 60 FE per mB, up to 600 FE/t. That's about 34 FE net per mB of plasma.

**Logistics:** Fluid Pipe (any fluid, 1,000 mB per transfer), Plasma Cable (plasma only, 8,000 mB), Fluid Tank
(32,000 mB of any fluid; shows its contents), Plasma Energy Battery (8,000,000 FE, 8,000 FE/t).

## Tier 4 - Fusion

**Fuel:**
- **Isotope Separator**: 20 mB water → 1 mB deuterium (50 FE per mB).
- **Tritium Breeder**: deuterium → tritium 1:1 (400 FE per mB).
- **Pellet Press**: iron nugget + 100 mB deuterium + 100 mB tritium → Fusion Fuel Pellet.
- The Circuit Assembler forges **Reactor Plating** (from an iron block) and the **Fusion Circuit** (from a Plasma Circuit).

**Fusion Reactor**, a 3x3x3 multiblock:
- Reactor Casing (or a Reactor Port) on the edges and corners; Reactor Casing, Reactor Glass or a Reactor Port on
  the face centres; the **Fusion Reactor Controller** in one face centre; the middle block empty.
- Ports take pellets and plasma and give FE and tritium.
- To ignite, it heats up on plasma (10 mB/t, about 1,000 mB from cold). Then it burns a pellet every 10 seconds
  for 4,000 FE/t and breeds 60 mB of tritium back per pellet. That isn't enough to sustain itself: top it up from
  the breeder.

**Ore Purifier**: washed ore + 50 mB oxygen → purified ore, with a 20% chance of the by-product.

**Logistics:** Fusion Cable (32,000 FE/t per network), Fusion Energy Battery (64,000,000 FE, 32,000 FE/t).

## Tier 5 - Antimatter

**Particle Accelerator**, a ring multiblock:
- A hollow square ring of Accelerator Segments, 1 block high, side 5 to 15.
- One **Particle Accelerator Controller** in the ring, plus Accelerator Ports.
- A port is Input (takes FE and hydrogen) or Output (gives antimatter). Switch it with the wrench's Configure mode.
- It runs on 333 FE/t per ring block and 100 mB hydrogen per mB of antimatter. A 7x7 ring makes 1 mB per second
  for about 8,000 FE/t; bigger rings are faster.

**Antimatter** only goes into **Containment Cells** (4,000 mB) and **Magnetic Conduits**; ordinary tanks and pipes
refuse it. It's used for power and circuits:
- **Annihilation Generator**: 1 mB antimatter = 2,000,000 FE, burned at up to 100,000 FE/t.
- **Antimatter Circuit**: Circuit Assembler, Fusion Circuit + 10 mB antimatter.

**Machines** (FE only, 200,000 FE buffers):

| Machine | Does | Numbers |
|---|---|---|
| Froth Flotation Cell | raw ore or ore block → 4 concentrate | 80 ticks at 200 FE/t + 500 mB water + 100 mB oxygen |
| Antimatter Granulator | fastest granulator | 20 ticks at 200 FE/t |
| Antimatter Smelter | fastest smelter | 12 ticks at 250 FE/t |
| Antimatter Washer | raw ore or ore block → 3 washed, concentrate → 1 washed | 30 ticks at 250 FE/t + 125 mB water |
| Antimatter Purifier | washed → purified (+ by-product) | 30 ticks at 300 FE/t + 25 mB oxygen |

**Logistics:** Antimatter Cable (128,000 FE/t per network), Antimatter Energy Battery (512,000,000 FE, 128,000 FE/t).

## Tier 6 - Singularity

**Event Horizon Reactor**, a 7x7x7 cage:
- Containment Frame on all 12 edges, the **Event Horizon Controller** in the middle of a bottom edge facing out,
  Horizon Ports anywhere in the frame. The inner 5x5x5 must be empty; the faces' openings can hold anything
  (glass keeps people out).
- 1,000 mB of antimatter ignites a black hole in the middle. It eats any item, by mass, for 50,000 FE per mass
  (up to 250,000 FE/t). Items come in through Input ports, the controller's slot, or by throwing them in.
- Starved of mass for 60 seconds, it evaporates and needs igniting again.
- **While it runs, the inner 5x5x5 is a deadly field: anything that walks in dies, even in creative mode.**
  Switch the reactor off with redstone before going in: the black hole goes dormant until you switch it back on.
- Ports are Input (items and antimatter) or Output (FE); switch them with the wrench.
- Item mass is the `comminutioncraft:matter_mass` data map: 1 by default, 9 for storage blocks, more for rare
  things (nether star 200, dragon egg 1,000).
- Want to just look at one? `/summon comminutioncraft:black_hole` spawns a harmless one.

**Singularity Processor**: raw ore or ore block → 5 ingots plus the rare drops, 40 ticks at 5,000 FE/t. It only
works within 16 blocks of a running black hole.

**Parts and logistics:**
- Singularity Circuit: Circuit Assembler, Antimatter Circuit + 50 mB antimatter.
- Singularity Shard: drops only from the processor; used in the endgame recipes.
- Singularity Cable: 512,000 FE/t per network.
- Singularity Energy Battery: 2,000,000,000 FE, 512,000 FE/t.

## Using machines

- **Side configuration** (the wrench tab in every GUI): the machine is drawn as a cube. Left-click a face to cycle
  **Input / Output / By-product / None**; drag with the right mouse button to turn the cube. Sides are relative to
  the front, so they turn with the machine. By default the bottom is Output and the rest Input.
- **Machines push their results out of Output faces** into whatever is there (another machine, a chest, a pipe),
  so you can chain them without hoppers.
- **Redstone** tab: **Ignore** (always runs), **High** (runs with a signal), **Low** (runs without one). Switched
  off, a machine pauses and keeps its progress.
- **Wrench**: press **V** while holding it to switch mode (rebindable under Controls > Comminution Craft):
  - **Rotate**: right-click a block to turn it.
  - **Cable**: right-click one arm of a cable or pipe to cut it, again to restore it.
  - **Configure**: right-click a machine face to cycle its side mode, or a multiblock port to switch Input/Output.
    Sneak + right-click picks a machine up.
- **Pickaxe vs wrench**: mining a machine drops a plain block. Picking it up with the wrench keeps its stored FE
  and settings (Fluid Tanks, the Ore Washer and the water generator keep their fluid too).
- All tooltips are behind **Shift**.

## Other blocks

- **Soaryn Chest**: a chest (crafted from a chest and a hopper) that empties your inventory into itself when you
  place it. Your hotbar stays, unless the common config says otherwise.
- **Creative Energy Battery** and **Creative Fluid Tank** (creative only): endless FE into every side, and an
  endless supply of any fluid you pick.
- **Decorative stone: Marble and Volcanic Basalt**, each with natural, cobbled, mossy cobbled, polished, smooth,
  bricks (plus cracked, mossy and chiseled), a pillar, stairs, slabs, walls and a button, all stonecutter-friendly.
  Marble generates underground in big blobs like granite. Volcanic Basalt forms a crust around underground lava.

## Mod support

- **JEI** (optional): a page for every machine and recipe type, fluid costs as fluid slots, and 3D multiblock pages
  for the Fusion Reactor, Particle Accelerator and Event Horizon Reactor: step through the layers and see the
  blocks needed. Press U on any structure block to find its page. Click a GUI's progress arrow to open its recipes.
- **Jade** (optional): machine progress, stored FE, battery in/out, generator status and the water generator's intake.

## Configuration

- `config/comminutioncraft-server.toml`: about 100 values, one section per machine: process times, FE costs,
  buffers, tank sizes, fluid costs, generator outputs, cable and pipe rates, battery sizes, reactor and
  accelerator numbers, the water generator's humidity formula. Synced to players, so GUIs, tooltips and JEI always
  match the server. Also editable from the Mods menu in game.
- `config/comminutioncraft-common.toml`: whether the Soaryn Chest also takes your hotbar.
- **Datapacks**: every recipe is a normal datapack recipe, so outputs, chances and by-products can be changed.
  Types: `granulating`, `washing`, `purifying`, `flotation`, `assembling`, `pressing`, `singularity_processing`,
  `tag_smelting` and `tag_blasting`. Item masses for the black hole come from the `comminutioncraft:matter_mass`
  item data map.

## Building from source

The mod needs **Java 25**. Gradle itself has to start on Java 17 or newer, so point `JAVA_HOME` at a modern JDK
(on Windows, use `gradlew.bat`).

```
./gradlew build               # the jar ends up in build/libs/
./gradlew runClient           # a dev client with the mod (plus JEI and Jade)
./gradlew runData             # regenerate recipes, tags, models, loot tables and translations
./gradlew runGameTestServer   # run the automated in-game tests, then exit
```

- Everything under `src/generated/resources` comes from datagen (`common/data/`). Don't edit it by hand: change
  the providers and run `runData`.

## License

MIT, see [LICENSE](LICENSE).
