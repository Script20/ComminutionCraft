"""Tier 6 (singularity) textures, drawn by the functions in the owner-approved preview
design/previews/phase_6a_preview.py (2026-11-06).

Phase 6A part 1: the Event Horizon Reactor's blocks - Containment Frame (+ animated _on), Event Horizon Controller
(front + animated _front_on; top / side = frame), Horizon Port in its Input and Output modes (+ _on; top = frame).
The black hole itself is part 2 (a renderer, from design/previews/singularity_mockup.py).

Run: tools/.venv/Scripts/python tools/gen_tier6_textures.py
"""
import os
import sys

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TOOLS)
sys.path.insert(0, os.path.join(TOOLS, '..', 'design', 'previews'))
import gen_tier3_textures as t3  # noqa: E402
import phase_6a_preview as p6  # noqa: E402

BLOCK = os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures', 'block')

if __name__ == '__main__':
    os.makedirs(BLOCK, exist_ok=True)
    p6.frame_side().img.save(os.path.join(BLOCK, 'containment_frame.png'))
    t3.save_strip(os.path.join(BLOCK, 'containment_frame_on.png'), p6.frame_side)
    p6.frame_side().img.save(os.path.join(BLOCK, 'horizon_controller_top.png'))
    p6.frame_side().img.save(os.path.join(BLOCK, 'horizon_controller_side.png'))
    p6.controller_front().img.save(os.path.join(BLOCK, 'horizon_controller_front.png'))
    t3.save_strip(os.path.join(BLOCK, 'horizon_controller_front_on.png'), p6.controller_front)
    p6.port_side(False).img.save(os.path.join(BLOCK, 'horizon_port_side.png'))
    t3.save_strip(os.path.join(BLOCK, 'horizon_port_side_on.png'), lambda f: p6.port_side(False, True, f))
    p6.port_side(True).img.save(os.path.join(BLOCK, 'horizon_port_output_side.png'))
    t3.save_strip(os.path.join(BLOCK, 'horizon_port_output_side_on.png'), lambda f: p6.port_side(True, True, f))
    p6.frame_side().img.save(os.path.join(BLOCK, 'horizon_port_top.png'))
    t3.save_strip(os.path.join(BLOCK, 'horizon_port_top_on.png'), p6.frame_side)
    print('ok')
