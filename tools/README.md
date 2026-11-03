# Texture & asset tools

Python scripts that generate every texture in the mod (nothing is hand-painted except the
`granulated_iron/copper/gold.png` item piles). Scripts that write into the mod overwrite the files
in `src/main/resources/...` directly; re-running them on unchanged code reproduces the textures
byte for byte, so `git status` showing no change means nothing moved.

## Setup (once)

```bash
cd tools
python -m venv .venv
.venv/Scripts/python.exe -m pip install -r requirements.txt
```

`.venv/` is git-ignored. Run scripts with `.venv/Scripts/python.exe <script>` from inside `tools/`.

## Scripts

| Script | Writes | Notes |
|---|---|---|
| `mekstyle_lib.py` | – | Shared helpers: 16x16 `Tex`, colour `mix`/`shade`, `fire()` ramp, `iso_block` previews. |
| `dark_chassis_system.py` | – (or a preview with an argument) | **Dark Chassis** building blocks: casing palette `K`, `TIERS` colours, `casing/side/top/front`, machine tops `act_*`, tier bottoms `tier_*`, `circuit(tier, metal)` items and `battery_front/side/top(tier)`. Importing it only defines functions; `dark_chassis_system.py <out_dir>` renders the original A/B/C comparison sheet (variant C was chosen). |
| `six_tiers.py <out_dir>` | `<out_dir>` only | All six tiers as granulators (+ plasma/fusion/antimatter/singularity bottoms and tier colours) and a preview sheet/GIF. Its output for tiers 1-6 is what `design/tier_textures/` holds. |
| `gen_tier2_textures.py` | `textures/block/electric_*`, `energy_cable.png`, `energy_battery_*`, `textures/item/wrench*.png`, `electronic_circuit.png` | Electric Granulator/Smelter/Generator, cable sheet (UVs match `ModModelProvider`), wrench in 3 modes, Energy Battery (one front per charge level `_front_0..6`, `_front` = full), Electronic Circuit. |
| `gen_tier3_textures.py` | `textures/block/plasma_generator_*`, `fluid_pipe.png`, `fluid_tank_*`, `plasma_still/flow.png`, `textures/item/air_filter.png`; phase C1: `plasma_granulator_*`, `plasma_smelter_*`, `plasma_cable.png`, `plasma_energy_battery_*`, `textures/item/plasma_circuit.png`; phase C2: `circuit_assembler_*`, `ore_washer_*` | Tier 3 (plasma). Uses `tier_plasma` from `six_tiers.py` (approved design). Tank side window is fully transparent (cutout); fluid strips are 8 frames. The phase B preview sheet came from the same functions. |
| `gui_dark_chassis.py` | `textures/gui/container/*.png` | All machine GUI sheets (layout documented at the top; matches `MachineScreen`). |
| `gen_stone_textures.py [preview.png]` | `textures/block/*marble*`, `*volcanic_basalt*` | Seamless 16x16 stone sets (periodic noise). Not Dark Chassis: vanilla-like pixel noise. |
| `gen_granulated_textures.py [preview.png]` | `textures/item/granulated_<other metal>.png` | Recolours the hand-made `granulated_iron.png` for other mods' metals. Never touches iron/copper/gold. |
| `gen_test_structure.py` | `data/comminutioncraft/structure/gametest/floor_5x3x5.nbt` | GameTest floor (no Pillow needed). Bump `DATA_VERSION` when Minecraft updates. |
| `legacy/gen_textures.py` | – | The original light/stone textures. **Don't run against the mod** — it would overwrite Dark Chassis textures. Kept for reference. |

## Adding things

- **New machine:** write an `act_<machine>(t, f, lit)` (top half, rows 1-7) in `dark_chassis_system.py`,
  then `front(act_<machine>, tier_<tier>, '<tier>')` + `top(tier)` + `side(tier)`; save as
  `<block>_top/_side/_front/_front_on` (8-frame strip + `.mcmeta` `{"animation": {"frametime": 2, "interpolate": false}}`).
- **New tier:** add its colours to `TIERS` and a `tier_<name>(t, f, lit)` bottom (rows 9-14); see `six_tiers.py`.
- **New stone:** add a palette + `build('<name>', ...)` in `gen_stone_textures.py`, and one `StoneSet` line in `ModBlocks`.
- **New metal:** add it to `ProcessedMetal.ALL` + `ModItems`, and its mid-tone to `METALS` in `gen_granulated_textures.py`.
