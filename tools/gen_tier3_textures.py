"""Tier-3 (plasma) textures in the Dark Chassis style. Writes straight into the mod's texture folders.

Usage: python tools/gen_tier3_textures.py

- Plasma Generator: air intake + electrodes on top of the front, the tier-3 plasma tube below (tier_plasma from
  six_tiers.py, the approved design), intake louvres on the sides, a fan grille on top.
- Air Filter item; Fluid Pipe sheet (same UV layout as the energy cable, see ModModelProvider);
  Fluid Tank (the side has a fully transparent window: the block renders cutout and FluidTankRenderer draws the
  contents); plasma_still / plasma_flow (8-frame strips, used by the plasma fluid model).
The phase B preview sheet (design/previews/phase_b.png) was drawn from these same functions.
"""
import math
import os
import sys

import json

from PIL import Image

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TOOLS)
from mekstyle_lib import Tex, mix, shade, FRAMES, TAU  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import K, GLASS, STEEL, COPPER, casing, seam, well  # noqa: E402

dcs.TIERS['plasma'] = dict(off=(92, 34, 84), on=(255, 80, 210), hi=(255, 196, 244))
P = dcs.TIERS['plasma']
AIR = (196, 218, 236)

# tier_plasma (the approved tier-3 bottom) lives in six_tiers.py; pull just that function in.
_src = open(os.path.join(TOOLS, 'six_tiers.py')).read()
_fn = _src[_src.index('def tier_plasma'):_src.index('# ---------------------------------------------------------------- 4. FUSION')]
_ns = dict(dcs.__dict__)
_ns['tc'] = lambda tier, k: dcs.TIERS[tier][k]
exec(_fn, _ns)
tier_plasma = _ns['tier_plasma']


# ================================================================ Plasma Generator
def act_ionizer(t, f, lit):
    """Top half: air drawn in through a grille (left), superheated between two electrodes, plasma out (right)."""
    for y in range(2, 7):  # inlet grille
        t.px(2, y, STEEL[2] if y % 2 == 0 else K['o'])
        t.px(3, y, STEEL[1] if y % 2 == 0 else K['d'])
    for y in range(2, 7):  # electrodes
        t.px(6, y, COPPER[2] if y in (2, 6) else COPPER[1])
        t.px(9, y, COPPER[2] if y in (2, 6) else COPPER[1])
    for y in range(3, 6):  # the hot zone between them
        for x in (7, 8):
            if lit:
                k = 0.5 + 0.5 * math.sin(f * TAU / FRAMES * 2 + y + x)
                t.px(x, y, mix(P['on'], P['hi'], k * (0.9 if y == 4 else 0.5)))
            else:
                t.px(x, y, shade(P['off'], 0.7 if y == 4 else 0.5))
    t.rect(12, 3, 13, 5, STEEL[1])  # outlet nozzle
    t.px(12, 3, STEEL[3]); t.px(13, 5, STEEL[0])
    t.px(13, 4, P['on'] if lit else P['off'])
    if lit:  # air specks drifting in, turning to plasma as they pass the electrodes
        for lane, y in enumerate((3, 4, 5)):
            x = 4 + (f + lane * 3) % 8
            if x in (6, 9):
                continue
            c = AIR if x < 6 else (P['hi'] if x < 9 else P['on'])
            if t.img.getpixel((x, y))[:3] == GLASS or x > 9 or x < 6:
                t.px(x, y, c)


def generator_front(f=None):
    lit = f is not None
    f = f or 0
    t = casing()
    well(t, 1, 1, 14, 7)
    act_ionizer(t, f, lit)
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', lit, f)
    tier_plasma(t, f, lit)
    return t


def generator_side():
    """Intake louvres above and below the tier seam: it breathes through every face."""
    t = casing()
    for (y0, y1) in ((2, 6), (10, 13)):
        well(t, 2, y0, 13, y1, K['d'])
        for y in range(y0 + 1, y1):
            for x in range(3, 13):
                t.px(x, y, K['l'] if (y - y0) % 2 == 1 else K['o'])
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', False, 0)
    return t


def generator_top():
    """Intake fan under a round grille, ringed in the tier colour."""
    t = casing()
    ring = []
    for y in range(1, 15):
        for x in range(1, 15):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 6.2:
                a = math.atan2(y - 7.5, x - 7.5)
                blade = (math.sin(a * 4 + d * 0.6) > 0.2) and d > 1.6
                t.px(x, y, K['l'] if blade else K['o'])
                if 5.4 < d <= 6.2:
                    ring.append((x, y))
    seam(t, ring, 'plasma', False, 0)
    t.rect(7, 7, 8, 8, STEEL[2]); t.px(7, 7, STEEL[4])
    return t


# ================================================================ Air Filter item
def air_filter():
    t = Tex()
    x0, y0, x1, y1 = 2, 3, 13, 12
    t.rect(x0, y0, x1, y1, K['m'])
    t.hline(x0, x1, y0, K['hi']); t.vline(x0, y0, y1, K['l'])
    t.hline(x0, x1, y1, K['o']); t.vline(x1, y0, y1, K['o'])
    paper = [(236, 230, 212), (204, 196, 174)]
    for x in range(x0 + 1, x1):
        for y in range(y0 + 1, y1):
            c = paper[(x - x0) % 2]
            t.px(x, y, shade(c, 1.0 - 0.06 * (y - y0)))
    for (x, y) in ((4, 6), (7, 9), (10, 5), (11, 10), (5, 10), (8, 5)):  # charcoal specks
        t.px(x, y, (70, 66, 62))
    for (x, y) in ((x0, y0), (x1, y0), (x0, y1), (x1, y1)):
        t.px(x, y, STEEL[3])
    return t


# ================================================================ Fluid Pipe (same sheet layout as the cable)
def pipe_sheet():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))

    def px(x, y, c):
        img.putpixel((x, y), tuple(c[:3]) + (255,))

    for y in range(6):  # core: steel junction box with bolts
        for x in range(6):
            edge = x in (0, 5) or y in (0, 5)
            px(x, y, STEEL[0] if edge else mix(STEEL[3], STEEL[1], y / 5))
    for (x, y) in ((1, 1), (4, 1), (1, 4), (4, 4)):
        px(x, y, STEEL[4])
    for y in range(8):  # flange plate with four bolts and a dark port
        for x in range(8):
            c = mix(STEEL[3], STEEL[1], y / 7)
            if y == 0 or x == 0:
                c = STEEL[4]
            if y == 7 or x == 7:
                c = STEEL[0]
            px(8 + x, y, c)
    for (x, y) in ((1, 1), (6, 1), (1, 6), (6, 6)):
        px(8 + x, y, K['o'])
    for y in range(2, 6):
        for x in range(2, 6):
            px(8 + x, y, (18, 20, 24) if x not in (2, 5) and y not in (2, 5) else STEEL[0])
    rows = [STEEL[3], (26, 28, 34), (20, 22, 26), STEEL[0]]  # tube: steel rim, dark glass, shadow
    for x in range(16):
        for y, c in enumerate(rows):
            if x % 8 in (0, 1):
                c = STEEL[4] if y == 0 else (STEEL[2] if y < 3 else STEEL[0])  # clamp rings
            elif y == 1 and x % 8 == 4:
                c = (120, 128, 140)  # glint
            px(x, 8 + y, c)
    return img


# ================================================================ Fluid Tank
def tank_side(fill=None, fluid_frame=0):
    """Steel frame, tall glass window. `fill` (0..1) paints plasma behind the glass, as the renderer would."""
    t = casing()
    for y in range(1, 15):
        for x in range(3, 13):
            t.img.putpixel((x, y), (0, 0, 0, 0))
    if fill:
        top = 14 - int(round(fill * 13))
        fl = plasma_still(fluid_frame).img
        for y in range(max(1, top), 15):
            for x in range(3, 13):
                t.px(x, y, fl.getpixel((x, y))[:3])
    for y in range(1, 15):  # glass tint + glints over whatever is behind
        for x in range(3, 13):
            base = t.img.getpixel((x, y))
            if base[3] == 0:
                t.img.putpixel((x, y), (150, 170, 190, 40))
            else:
                t.px(x, y, mix(base, (180, 200, 220), 0.08))
    for (x, y) in ((4, 2), (4, 3), (5, 2), (11, 12), (10, 13)):
        b = t.img.getpixel((x, y))
        t.img.putpixel((x, y), (210, 225, 240, 140 if b[3] < 255 else 255))
    t.vline(2, 1, 14, K['o']); t.vline(13, 1, 14, K['hi'])
    return t


def tank_top():
    t = casing()
    ring = [(x, 3) for x in range(3, 13)] + [(x, 12) for x in range(3, 13)] + \
           [(3, y) for y in range(4, 12)] + [(12, y) for y in range(4, 12)]
    seam(t, ring, 'plasma', False, 0)
    t.rect(4, 4, 11, 11, K['d'])
    t.rect(6, 6, 9, 9, STEEL[1])
    t.rect(7, 7, 8, 8, (18, 20, 24))
    t.hline(6, 9, 6, STEEL[3]); t.vline(6, 6, 9, STEEL[2])
    return t


# ================================================================ plasma fluid
def plasma_still(f, flow=False):
    """Seamless 16x16 swirl, FRAMES-frame loop. The flowing version drifts downwards."""
    t = Tex()
    w = TAU / 16
    ph = f * TAU / FRAMES
    deep, mid = (96, 14, 92), (196, 40, 172)
    for y in range(16):
        for x in range(16):
            yy = y - (f * 2 if flow else 0)
            v = (math.sin(x * w * 2 + ph) + math.sin(yy * w * 2 - ph + x * w)
                 + 0.7 * math.sin((x + yy) * w * 3 + 2 * ph)) / 2.7
            v = 0.5 + 0.5 * v
            if v < 0.55:
                c = mix(deep, mid, v / 0.55)
            elif v < 0.85:
                c = mix(mid, P['on'], (v - 0.55) / 0.3)
            else:
                c = mix(P['on'], P['hi'], (v - 0.85) / 0.15)
            t.px(x, y, c)
    return t


def tank_side_cutout():
    """In game the window is fully see-through (cutout); only the frame and a few glints are drawn."""
    t = casing()
    for y in range(1, 15):
        for x in range(3, 13):
            t.img.putpixel((x, y), (0, 0, 0, 0))
    for (x, y) in ((4, 2), (4, 3), (5, 2), (11, 12), (10, 13)):
        t.px(x, y, (210, 225, 240))
    t.vline(2, 1, 14, K['o']); t.vline(13, 1, 14, K['hi'])
    return t


ROOT = os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures')
BLOCK = os.path.join(ROOT, 'block')
ITEM = os.path.join(ROOT, 'item')


def save_strip(path, frame_fn):
    strip = Image.new('RGBA', (16, 16 * FRAMES))
    for k in range(FRAMES):
        strip.paste(frame_fn(k).img, (0, 16 * k))
    strip.save(path)
    with open(path + '.mcmeta', 'w') as fh:
        json.dump({"animation": {"frametime": 2, "interpolate": False}}, fh, indent=2)
        fh.write('\n')


if __name__ == '__main__':
    os.makedirs(BLOCK, exist_ok=True)
    os.makedirs(ITEM, exist_ok=True)
    generator_top().img.save(os.path.join(BLOCK, 'plasma_generator_top.png'))
    generator_side().img.save(os.path.join(BLOCK, 'plasma_generator_side.png'))
    generator_front().img.save(os.path.join(BLOCK, 'plasma_generator_front.png'))
    save_strip(os.path.join(BLOCK, 'plasma_generator_front_on.png'), generator_front)
    air_filter().img.save(os.path.join(ITEM, 'air_filter.png'))
    pipe_sheet().save(os.path.join(BLOCK, 'fluid_pipe.png'))
    tank_side_cutout().img.save(os.path.join(BLOCK, 'fluid_tank_side.png'))
    tank_top().img.save(os.path.join(BLOCK, 'fluid_tank_top.png'))
    save_strip(os.path.join(BLOCK, 'plasma_still.png'), plasma_still)
    save_strip(os.path.join(BLOCK, 'plasma_flow.png'), lambda k: plasma_still(k, flow=True))
    print('ok')
