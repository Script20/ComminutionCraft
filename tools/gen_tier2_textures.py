"""Tier-2 (electric) textures in the Dark Chassis style. Writes straight into the mod's texture folders.

Usage: python tools/gen_tier2_textures.py

- Electric Granulator / Smelter: same top-half action as the coal machines, blue battery meter below.
- Electric Generator: top half is a firebox (it burns fuel), bottom half the battery meter.
- Energy cable: one 16x16 sheet (core face u0-6 v0-6, connector plate u8-16 v0-8, arm strip u0-16 v8-12),
  matching the UVs in ModModelProvider.
- Wrench: Rotate mode (copper grip), Cable mode (blue grip + glowing jaw), Configure mode (green grip).
"""
import json
import math
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
from mekstyle_lib import mix, shade, fire, FRAMES, TAU  # noqa: E402
from dark_chassis_system import (K, TIERS, STEEL, COPPER, well, front, side, top,  # noqa: E402
                                 act_conveyor, act_conveyor_smelt, tier_elec_meter)

ROOT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures')
BLOCK = os.path.join(ROOT, 'block')
ITEM = os.path.join(ROOT, 'item')
ELEC = TIERS['electric']
CONFIGURE = (96, 214, 120)  # wrench grip in Configure mode


# ---------------------------------------------------------------- generator firebox (top half)
def act_firebox(t, f, lit):
    """Grate bars with flames licking between them, inside the top glass well."""
    for x in range(2, 14):
        if x % 3 == 2:
            for y in range(2, 7):
                t.px(x, y, K['l'] if y == 2 else K['d'])
        else:
            h = (2.6 + 1.3 * math.sin(f * TAU / FRAMES * 2 + x * 1.7)) if lit else 0
            for y in range(6, 1, -1):
                k = 6 - y
                if lit and k < h:
                    t.px(x, y, fire(k / max(h, 1) * 0.9))
                elif y == 6:
                    t.px(x, y, (56, 52, 52) if not lit else (255, 120, 40))


def save_machine(name, action, tier_fn):
    top('electric').img.save(os.path.join(BLOCK, f'{name}_top.png'))
    side('electric').img.save(os.path.join(BLOCK, f'{name}_side.png'))
    front(action, tier_fn, 'electric').img.save(os.path.join(BLOCK, f'{name}_front.png'))
    strip = Image.new('RGBA', (16, 16 * FRAMES))
    for k in range(FRAMES):
        strip.paste(front(action, tier_fn, 'electric', k).img, (0, 16 * k))
    path = os.path.join(BLOCK, f'{name}_front_on.png')
    strip.save(path)
    with open(path + '.mcmeta', 'w') as fh:
        json.dump({"animation": {"frametime": 2, "interpolate": False}}, fh, indent=2)
        fh.write('\n')


# ---------------------------------------------------------------- energy cable
def cable_sheet():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))

    def px(x, y, c):
        img.putpixel((x, y), tuple(c[:3]) + (255,))

    # core face 6x6: casing with a glowing centre
    for y in range(6):
        for x in range(6):
            edge = x in (0, 5) or y in (0, 5)
            px(x, y, K['o'] if edge else mix(K['l'], K['m'], y / 5))
    for (x, y) in ((2, 2), (3, 2), (2, 3), (3, 3)):
        px(x, y, ELEC['hi'] if (x, y) == (2, 2) else ELEC['on'])

    # connector plate 8x8: dark plate, bevelled, with a blue ring around the socket
    for y in range(8):
        for x in range(8):
            c = mix(K['l'], K['d'], y / 7)
            if y == 0 or x == 0:
                c = K['hi']
            if y == 7 or x == 7:
                c = K['o']
            px(8 + x, y, c)
    for y in range(2, 6):
        for x in range(2, 6):
            ring = x in (2, 5) or y in (2, 5)
            px(8 + x, y, shade(ELEC['on'], 0.8) if ring else (12, 14, 18))
    px(8 + 3, 3, ELEC['hi'])

    # arm strip 16x4, running along u: sheath edge, glowing core, darker core, shadow
    rows = [K['l'], ELEC['on'], shade(ELEC['on'], 0.62), K['o']]
    for x in range(16):
        for y, c in enumerate(rows):
            if y in (1, 2) and x % 4 == 3:
                c = shade(c, 0.75)  # faint segmenting so the arm reads as a cable
            px(x, 8 + y, c)
    return img


# ---------------------------------------------------------------- wrench
def wrench(cable_mode, configure_mode=False):
    """Open-end wrench lying diagonally: handle bottom-left, head top-right with its slot opening up-right."""
    head = (10.8, 5.2)
    handle_a, handle_b = (2.6, 13.4), (9.0, 7.0)
    slot_dir = (0.7071, -0.7071)

    def in_head(x, y):
        dx, dy = x - head[0], y - head[1]
        if math.hypot(dx, dy) > 3.7:
            return False
        along = dx * slot_dir[0] + dy * slot_dir[1]
        across = abs(dx * slot_dir[1] - dy * slot_dir[0])
        return not (along > -0.6 and across < 1.15)  # the slot

    def handle_t(x, y):
        """Position along the handle (0..1), or None if (x, y) isn't on it."""
        ax, ay = handle_a
        bx, by = handle_b
        vx, vy = bx - ax, by - ay
        t = max(0.0, min(1.0, ((x - ax) * vx + (y - ay) * vy) / (vx * vx + vy * vy)))
        px_, py_ = ax + vx * t, ay + vy * t
        return t if math.hypot(x - px_, y - py_) <= 1.25 else None

    def solid(x, y):
        return 0 <= x < 16 and 0 <= y < 16 and (in_head(x + 0.5, y + 0.5) or handle_t(x + 0.5, y + 0.5) is not None)

    if configure_mode:
        grip_hi, grip_lo = CONFIGURE, shade(CONFIGURE, 0.6)
    elif cable_mode:
        grip_hi, grip_lo = ELEC['on'], shade(ELEC['on'], 0.6)
    else:
        grip_hi, grip_lo = COPPER[3], COPPER[1]
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if not solid(x, y):
                continue
            lit_edge = not solid(x - 1, y) or not solid(x, y - 1)    # light from the top left
            dark_edge = not solid(x + 1, y) or not solid(x, y + 1)
            t = handle_t(x + 0.5, y + 0.5)
            on_grip = t is not None and 0.05 <= t <= 0.55 and not in_head(x + 0.5, y + 0.5)
            if on_grip:
                c = grip_lo if dark_edge else grip_hi
            elif dark_edge:
                c = STEEL[0]
            elif lit_edge:
                c = STEEL[4]
            else:
                c = STEEL[2]
            img.putpixel((x, y), tuple(c[:3]) + (255,))
    if cable_mode:
        # the inside of the jaw glows blue
        for y in range(16):
            for x in range(16):
                if solid(x, y) and in_head(x + 0.5, y + 0.5):
                    near_slot = any(not in_head(x + 0.5 + ox, y + 0.5 + oy) and math.hypot(
                        x + 0.5 + ox - head[0], y + 0.5 + oy - head[1]) < 3.0 for ox, oy in ((1, 0), (0, -1), (-1, 0), (0, 1)))
                    if near_slot:
                        img.putpixel((x, y), tuple(ELEC['hi'][:3]) + (255,))
    return img


os.makedirs(BLOCK, exist_ok=True)
os.makedirs(ITEM, exist_ok=True)
save_machine('electric_granulator', act_conveyor, tier_elec_meter)
save_machine('electric_smelter', act_conveyor_smelt, tier_elec_meter)
save_machine('electric_generator', act_firebox, tier_elec_meter)
cable_sheet().save(os.path.join(BLOCK, 'energy_cable.png'))
wrench(False).save(os.path.join(ITEM, 'wrench.png'))
wrench(True).save(os.path.join(ITEM, 'wrench_cable.png'))
wrench(False, configure_mode=True).save(os.path.join(ITEM, 'wrench_configure.png'))
print('ok')
