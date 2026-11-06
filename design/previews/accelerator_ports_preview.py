"""Accelerator Port split into two modes (owner, 2026-11-06): the wrench's Configure mode switches a port between
Input (FE + hydrogen in on every face) and Output (antimatter out of every face). Each mode gets its own face:
Input = copper ring with arrows pointing in; Output = icy ring with arrows pointing out and an antimatter speck.

Run: tools/.venv/Scripts/python design/previews/accelerator_ports_preview.py design/previews/accelerator_ports.png
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.join(HERE, '..', '..', 'tools')
sys.path.insert(0, TOOLS)
sys.path.insert(0, HERE)
from mekstyle_lib import mix, iso_block, up  # noqa: E402
from dark_chassis_system import STEEL, COPPER  # noqa: E402
import phase_5a_preview as p5  # noqa: E402

A = p5.A
CORE = (12, 14, 22)


def port_side(output, lit=False, f=0):
    """A segment with a round connector. Input: copper ring, arrows pointing in. Output: icy ring, arrows out."""
    t = p5.segment_side(f if lit else None)
    ring = (A['on'] if lit else A['off']) if output else (COPPER[3] if lit else COPPER[2])
    for y in range(4, 12):
        for x in range(4, 12):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 3.9:
                t.px(x, y, ring if d > 2.9 else (STEEL[1] if d > 1.9 else CORE))
    if output:  # an antimatter speck in the middle
        t.px(7, 7, A['hi'] if lit else A['on'])
        t.px(8, 8, A['on'] if lit else A['off'])
    else:
        t.px(7, 7, COPPER[1]); t.px(8, 8, COPPER[1])
    # arrows in the corners: in = pointing at the centre, out = pointing away
    arrow = A['on'] if output else COPPER[3]
    for (cx, cy, dx, dy) in ((2, 2, 1, 1), (13, 2, -1, 1), (2, 13, 1, -1), (13, 13, -1, -1)):
        if output:
            tip = (cx, cy)
            t.px(*tip, arrow); t.px(cx + dx, cy, arrow); t.px(cx, cy + dy, arrow)
        else:
            tip = (cx + dx, cy + dy)
            t.px(*tip, arrow); t.px(cx, cy + dy, arrow); t.px(cx + dx, cy, arrow)
    return t


def main(out_png):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1240, 560), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)
    top = p5.segment_top().img
    top_on = p5.segment_top(True, 3).img
    dr.text((20, 14), 'Accelerator Port: wrench (Configure mode) switches Input <-> Output', font=font, fill=head)
    cubes = [('Input', port_side(False).img, top), ('Input (running)', port_side(False, True, 3).img, top_on),
             ('Output', port_side(True).img, top), ('Output (running)', port_side(True, True, 3).img, top_on),
             ('(old port)', p5.port_side().img, top)]
    for k, (name, side, tp) in enumerate(cubes):
        x = 20 + k * 240
        sheet.alpha_composite(iso_block(tp, side, side, 190), (x, 50))
        sheet.alpha_composite(up(side, 6), (x + 50, 270))
        dr.text((x + 50, 376), name, font=small, fill=label)
    dr.text((20, 420), 'Input: every face takes FE and hydrogen.  Output: every face gives antimatter and pushes it into', font=small, fill=label)
    dr.text((20, 442), 'Containment Cells / Magnetic Conduits next to it (never into the ring itself). A new port starts as Input.', font=small, fill=label)
    ring = p5.ring_blocks()
    side7, mid = 7, 3
    ring[(side7 - 1, 0, mid)] = (top, p5.segment_side().img, port_side(False).img)
    ring[(0, 0, mid)] = (top, port_side(True).img, p5.segment_side().img)
    sheet.alpha_composite(p5.iso_scene(ring, 40, 420, 250, 210, 10).resize((300, 178)), (920, 380))
    sheet.save(out_png)
    print('wrote', out_png)


if __name__ == '__main__':
    main(sys.argv[1])
