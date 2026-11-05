"""Tier 5 (antimatter) textures, drawn by the functions in the owner-approved preview
design/previews/phase_5a_preview.py (approved 2026-11-05).

Part 1 of phase 5A: the Antimatter fluid, the Containment Cell (cutout window; the renderer draws the contents) and
the Magnetic Conduit sheet (same UVs as the other conduits). The accelerator blocks follow in part 2.

Run: tools/.venv/Scripts/python tools/gen_tier5_textures.py
"""
import os
import sys

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TOOLS)
sys.path.insert(0, os.path.join(TOOLS, '..', 'design', 'previews'))
import gen_tier3_textures as t3  # noqa: E402
import phase_5a_preview as p5  # noqa: E402

BLOCK = os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures', 'block')

if __name__ == '__main__':
    os.makedirs(BLOCK, exist_ok=True)
    t3.save_strip(os.path.join(BLOCK, 'antimatter_still.png'), p5.antimatter_still)
    t3.save_strip(os.path.join(BLOCK, 'antimatter_flow.png'), p5.antimatter_still)
    p5.cell_side(preview=False).img.save(os.path.join(BLOCK, 'containment_cell_side.png'))
    p5.cell_top().img.save(os.path.join(BLOCK, 'containment_cell_top.png'))
    p5.conduit_sheet().save(os.path.join(BLOCK, 'magnetic_conduit.png'))
    print('ok')
