"""Tier 5 (antimatter) textures, drawn by the functions in the owner-approved preview
design/previews/phase_5a_preview.py (approved 2026-11-05).

Part 1 of phase 5A: the Antimatter fluid, the Containment Cell (cutout window; the renderer draws the contents) and
the Magnetic Conduit sheet (same UVs as the other conduits). Part 2: the Particle Accelerator ring - Accelerator Segment
(top / side, + animated _on strips), Accelerator Port (+ _on) and Accelerator Controller (front + animated _front_on;
top / side = segment).
Phase 5B (approved preview: design/previews/phase_5b.png, from phase_5b_preview.py): the Annihilation Generator
(front + animated _front_on, tier-5 side and top), the Antimatter Circuit item, the Antimatter Cable sheet and the
Antimatter Energy Battery faces.
Phase 5C (approved preview: design/previews/phase_5c.png, from phase_5c_preview.py): the Froth Flotation Cell, Ore
Concentrate for every metal (run gen_granulated_textures.py first: concentrate takes the granulated tones) and the
antimatter suite (granulator, smelter, washer, purifier: the tier-3/4 tops over the tier-5 bottom).

Run: tools/.venv/Scripts/python tools/gen_tier5_textures.py
"""
import os
import sys

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TOOLS)
sys.path.insert(0, os.path.join(TOOLS, '..', 'design', 'previews'))
import gen_tier3_textures as t3  # noqa: E402
import phase_5a_preview as p5  # noqa: E402
import phase_5b_preview as p5b  # noqa: E402
import phase_5c_preview as p5c  # noqa: E402
import accelerator_ports_preview as ports  # noqa: E402  (Input / Output port faces, approved 2026-11-06)
import dark_chassis_system as dcs  # noqa: E402

BLOCK = os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures', 'block')
ITEM = os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures', 'item')

if __name__ == '__main__':
    os.makedirs(BLOCK, exist_ok=True)
    t3.save_strip(os.path.join(BLOCK, 'antimatter_still.png'), p5.antimatter_still)
    t3.save_strip(os.path.join(BLOCK, 'antimatter_flow.png'), p5.antimatter_still)
    p5.cell_side(preview=False).img.save(os.path.join(BLOCK, 'containment_cell_side.png'))
    p5.cell_top().img.save(os.path.join(BLOCK, 'containment_cell_top.png'))
    p5.conduit_sheet().save(os.path.join(BLOCK, 'magnetic_conduit.png'))
    # Part 2: the accelerator ring
    p5.segment_side().img.save(os.path.join(BLOCK, 'accelerator_segment_side.png'))
    t3.save_strip(os.path.join(BLOCK, 'accelerator_segment_side_on.png'), p5.segment_side)
    p5.segment_top().img.save(os.path.join(BLOCK, 'accelerator_segment_top.png'))
    t3.save_strip(os.path.join(BLOCK, 'accelerator_segment_top_on.png'), lambda f: p5.segment_top(True, f))
    ports.port_side(False).img.save(os.path.join(BLOCK, 'accelerator_port_side.png'))
    t3.save_strip(os.path.join(BLOCK, 'accelerator_port_side_on.png'), lambda f: ports.port_side(False, True, f))
    ports.port_side(True).img.save(os.path.join(BLOCK, 'accelerator_port_output_side.png'))
    t3.save_strip(os.path.join(BLOCK, 'accelerator_port_output_side_on.png'), lambda f: ports.port_side(True, True, f))
    p5.segment_top().img.save(os.path.join(BLOCK, 'accelerator_port_top.png'))
    t3.save_strip(os.path.join(BLOCK, 'accelerator_port_top_on.png'), lambda f: p5.segment_top(True, f))
    p5.segment_top().img.save(os.path.join(BLOCK, 'accelerator_controller_top.png'))
    p5.segment_side().img.save(os.path.join(BLOCK, 'accelerator_controller_side.png'))
    p5.controller_front().img.save(os.path.join(BLOCK, 'accelerator_controller_front.png'))
    t3.save_strip(os.path.join(BLOCK, 'accelerator_controller_front_on.png'), p5.controller_front)
    # Phase 5B
    p5b.generator_top().img.save(os.path.join(BLOCK, 'annihilation_generator_top.png'))
    p5b.generator_side().img.save(os.path.join(BLOCK, 'annihilation_generator_side.png'))
    p5b.generator_front().img.save(os.path.join(BLOCK, 'annihilation_generator_front.png'))
    t3.save_strip(os.path.join(BLOCK, 'annihilation_generator_front_on.png'), p5b.generator_front)
    p5b.antimatter_circuit().img.save(os.path.join(ITEM, 'antimatter_circuit.png'))
    p5b.antimatter_cable_sheet().save(os.path.join(BLOCK, 'antimatter_cable.png'))
    dcs.battery_top('antimatter').img.save(os.path.join(BLOCK, 'antimatter_energy_battery_top.png'))
    dcs.battery_side('antimatter').img.save(os.path.join(BLOCK, 'antimatter_energy_battery_side.png'))
    dcs.battery_front('antimatter', dcs.BATTERY_CELLS).img.save(os.path.join(BLOCK, 'antimatter_energy_battery_front.png'))
    for level in range(dcs.BATTERY_CELLS + 1):
        dcs.battery_front('antimatter', level).img.save(os.path.join(BLOCK, f'antimatter_energy_battery_front_{level}.png'))
    # Phase 5C
    top5 = dcs.top('antimatter').img
    machines = [('froth_flotation_cell', p5c.flotation_front, p5c.flotation_side)]
    machines += [('antimatter_' + name.split()[1].lower(), front_fn, side_fn) for name, front_fn, side_fn in p5c.SUITE]
    for name, front_fn, side_fn in machines:
        top5.save(os.path.join(BLOCK, f'{name}_top.png'))
        side_fn().img.save(os.path.join(BLOCK, f'{name}_side.png'))
        front_fn().img.save(os.path.join(BLOCK, f'{name}_front.png'))
        t3.save_strip(os.path.join(BLOCK, f'{name}_front_on.png'), front_fn)
    for metal in p5c.gg.ALL_METALS:
        p5c.concentrate(metal).save(os.path.join(ITEM, f'concentrate_{metal}.png'))
    print('ok')
