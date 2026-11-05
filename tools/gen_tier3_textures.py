"""Tier-3 (plasma) textures in the Dark Chassis style. Writes straight into the mod's texture folders.

Usage: python tools/gen_tier3_textures.py

- Plasma Generator: air intake + electrodes on top of the front, the tier-3 plasma tube below (tier_plasma from
  six_tiers.py, the approved design), intake louvres on the sides, a fan grille on top.
- Air Filter item; Fluid Pipe sheet (same UV layout as the energy cable, see ModModelProvider);
  Fluid Tank (the side has a fully transparent window: the block renders cutout and FluidTankRenderer draws the
  contents); plasma_still / plasma_flow (8-frame strips, used by the plasma fluid model).
The phase B preview sheet (design/previews/phase_b.png) was drawn from these same functions.

Phase C1 (approved preview: design/previews/phase_c.png):
- Plasma Granulator / Smelter: the electric machines' top halves over the plasma tube; tier-3 top and side.
- Plasma Cable (same UV layout as the energy cable: magenta core, steel bands, double-ringed plate).
- Plasma Circuit item (the circuit board with gold traces and a magenta chip).
- Plasma Energy Battery (battery faces in magenta, _front_0.._front_6 + _front).

Phase C2 (same approved preview):
- Circuit Assembler: a press stamping Electronic Circuits into Plasma Circuits over the plasma tube.
- Ore Washer: a slatted drum under spray nozzles; its sides have a blue-ringed water inlet.

Phase D, the tier-3 power chain (approved preview: design/previews/phase_d.png):
- Atmospheric Water Generator: a frosty condenser coil dripping into a tray; it takes over the old Plasma Generator's
  intake louvres (sides) and fan (top).
- Electrolyser: electrodes in a water bath, hydrogen and oxygen bubbling off (2:1); gas outlet ports on the sides.
- Plasma Generator (reworked): hydrogen nozzle in, plasma out; a hydrogen inlet port on the sides, the tier-3 top.
- MHD Generator: plasma through a channel between magnet poles; copper coil sides, bus-bar top.
- Hydrogen and Oxygen fluids (still / flowing strips).
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
from dark_chassis_system import (front, side, top, act_conveyor, act_conveyor_smelt, mix as _mix,  # noqa: E402,F401
                                 circuit, battery_front, battery_side, battery_top, BATTERY_CELLS, cylinder_row)
from mekstyle_lib import ORE  # noqa: E402

dcs.TIERS['plasma'] = dict(off=(92, 34, 84), on=(255, 80, 210), hi=(255, 196, 244))
P = dcs.TIERS['plasma']
AIR = (196, 218, 236)
GOLD = [(120, 84, 20), (180, 132, 36), (232, 186, 64), (255, 230, 130)]
WATER = [(24, 52, 120), (40, 92, 186), (86, 150, 228), (190, 226, 250)]
E = dcs.TIERS['electric']
HYDROGEN = [(150, 150, 176), (196, 198, 224), (228, 230, 248), (250, 250, 255)]  # pale, almost white
OXYGEN = [(60, 120, 190), (110, 170, 230), (160, 206, 248), (214, 236, 255)]     # liquid-oxygen blue
FROST = [(120, 150, 170), (170, 200, 220), (220, 240, 250)]
MAG_N = (196, 56, 56)
MAG_S = (56, 96, 196)



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
    """Dark Chassis pipe with a blue glass window along the tube, casing-grey collars and bolts (design/previews/
    fluid_pipe_preview.py option_b2, owner-approved 2026-11-05). UVs: core 6x6 at (0,0), plate 8x8 at (8,0), tube
    strip 16x4 at y 8 with a collar every 4th texel (a 5-long arm reads collar-tube-tube-tube-collar either way round)."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    water = [(24, 60, 112), (40, 96, 168), (70, 140, 210), (150, 200, 240)]
    bolt = (96, 101, 112)

    def px(x, y, c):
        img.putpixel((x, y), tuple(int(v) for v in c[:3]) + (255,))

    for y in range(6):  # core: casing box, four bolts, a blue glass eye
        for x in range(6):
            c = mix(K['l'], K['m'], y / 5)
            if x == 0 or y == 0:
                c = K['hi']
            if x == 5 or y == 5:
                c = K['o']
            px(x, y, c)
    for (x, y) in ((1, 1), (4, 1), (1, 4), (4, 4)):
        px(x, y, bolt)
    for (x, y), c in zip(((2, 2), (3, 2), (2, 3), (3, 3)), (water[3], water[2], water[2], water[1])):
        px(x, y, c)
    for y in range(8):  # connector plate: casing flange, four bolts, a dark port with a glint
        for x in range(8):
            c = mix(K['l'], K['d'], y / 7)
            if x == 0 or y == 0:
                c = K['hi']
            if x == 7 or y == 7:
                c = K['o']
            px(8 + x, y, c)
    for (x, y) in ((1, 1), (6, 1), (1, 6), (6, 6)):
        px(8 + x, y, bolt)
    for y in range(2, 6):
        for x in range(2, 6):
            px(8 + x, y, K['o'] if x in (2, 5) or y in (2, 5) else (14, 18, 26))
    px(8 + 3, 3, water[3])
    rows = [K['l'], water[2], water[1], K['o']]  # tube: casing rim, blue glass (two rows), shadow
    collar = [K['hi'], K['l'], K['m'], K['d']]
    for x in range(16):
        for y in range(4):
            c = collar[y] if x % 4 == 0 else rows[y]
            if x % 4 == 2 and y == 1:
                c = water[3]  # glint
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


# ================================================================ Circuit Assembler (top half)
def act_assembler(t, f, lit):
    """A press head over the belt: Electronic Circuit (blue chip) in, a plasma-fed stamp, Plasma Circuit (magenta) out."""
    ff = f if lit else 0
    for x in range(2, 14):  # belt
        t.px(x, 6, (50, 52, 58) if (x - ff) % 3 else (30, 32, 36))
    t.rect(5, 2, 10, 2, STEEL[1])  # gantry + plasma feed line
    t.hline(5, 10, 2, STEEL[2])
    t.px(7, 2, P['on'] if lit else P['off']); t.px(8, 2, P['on'] if lit else P['off'])
    stroke = (0, 0, 1, 1, 1, 0, 0, 0)[ff % FRAMES] if lit else 0  # press comes down, holds, lifts
    for y in range(3, 4 + stroke):  # piston
        t.px(7, y, STEEL[3]); t.px(8, y, STEEL[2])
    hy = 4 + stroke
    t.hline(6, 9, hy, STEEL[3]); t.px(6, hy, STEEL[4]); t.px(9, hy, STEEL[1])  # stamp
    if lit and stroke:
        t.px(7, hy + 1 if hy < 5 else hy, P['hi'])  # plasma flash under the stamp
        t.px(5, 5, shade(P['on'], 0.6)); t.px(10, 5, shade(P['on'], 0.6))
    t.vline(4, 3, 5, K['d']); t.vline(11, 3, 5, K['d'])  # gantry legs
    # boards on the belt: blue chips in from the left, magenta chips out on the right
    bx = 2 + (ff // 2) % 2
    t.hline(bx, bx + 1, 5, K['hi']); t.px(bx, 5, E['on'])
    ox = 12 + (ff // 2) % 2
    if ox < 14:
        t.hline(ox, min(13, ox + 1), 5, K['hi']); t.px(ox, 5, P['on'] if lit else P['off'])


# ================================================================ Ore Washer (top half)
def act_washer(t, f, lit):
    """A slotted drum turning under spray nozzles; water falls through, clean grit drops out the bottom right."""
    ff = f if lit else 0
    for x in range(3, 13):  # spray bar
        t.px(x, 2, STEEL[1] if x % 3 else STEEL[3])
    for y in range(3, 7):  # drum
        for x in range(4, 12):
            c = cylinder_row(y, 3, 6, STEEL[3], STEEL[0])
            if (x + ff) % 3 == 0:
                c = shade(c, 0.55)  # slats turning
            t.px(x, y, c)
    for (x, y) in ((5, 4), (8, 5), (10, 4)):  # ore inside the drum
        xx = 4 + (x - 4 + ff) % 8
        t.px(xx, y, ORE[(x + y) % 3])
    if lit:  # water drops from every nozzle, splashing on the drum
        for k, x in enumerate((3, 6, 9, 12)):
            y = 3 + (ff + k * 2) % 4
            if t.img.getpixel((x, y))[:3] == GLASS or x in (3, 12):
                t.px(x, y, WATER[2])
            t.px(x, 3, WATER[3])
        t.px(4 + ff % 8, 6, WATER[3])  # spray off the bottom of the drum
    else:
        for x in (3, 6, 9, 12):
            t.px(x, 3, WATER[0])
    t.vline(2, 3, 6, WATER[1] if lit else WATER[0])  # water in (left), level glows
    t.px(13, 6, ORE[(ff // 2) % 3])  # clean grit out


def washer_side():
    """Water inlet port above the seam (blue-ringed), grille below: shows where pipes and tanks connect."""
    t = casing()
    well(t, 4, 2, 11, 6, K['d'])
    for x in range(5, 11):
        t.px(x, 3, WATER[0]); t.px(x, 5, WATER[0])
    t.px(5, 4, WATER[0]); t.px(10, 4, WATER[0])
    t.rect(6, 4, 9, 4, (14, 16, 20))
    t.px(7, 4, WATER[2]); t.px(8, 4, WATER[1])
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


# ================================================================ Atmospheric Water Generator
def act_condenser(t, f, lit):
    """Top half: a frosty cooling coil; moisture condenses on it and drips into a water tray."""
    ff = f if lit else 0
    for y in (2, 4):  # two coil runs, joined at alternating ends
        for x in range(2, 14):
            c = FROST[1] if (x + y) % 3 else FROST[2]
            t.px(x, y, c if lit else shade(c, 0.7))
    t.px(13, 3, FROST[1] if lit else shade(FROST[1], 0.7))
    t.px(2, 5, FROST[0])
    for x in range(3, 13):  # tray
        t.px(x, 6, WATER[1] if lit else WATER[0])
    t.px(2, 6, STEEL[1]); t.px(13, 6, STEEL[1])
    if lit:  # drops forming under the coils and falling into the tray
        for k, x in enumerate((4, 7, 10, 12)):
            y = 3 + ((ff + k * 3) % 4) // 2 * 2  # 3 or 5, below a coil run
            if t.img.getpixel((x, y))[:3] == GLASS:
                t.px(x, y, WATER[3])
        t.px(3 + (ff * 3) % 10, 6, WATER[3])  # a ripple running along the tray


def awg_front(f=None):
    return front(act_condenser, tier_plasma, 'plasma', f)


# The Plasma Generator's intake louvres and fan suit the water generator (it is the one that breathes now).
awg_side = generator_side
awg_top = generator_top


# ================================================================ Electrolyser
def act_electrolysis(t, f, lit):
    """Top half: two electrodes in a water bath; hydrogen bubbles off one, half as much oxygen off the other."""
    ff = f if lit else 0
    for y in range(3, 7):  # bath
        for x in range(2, 14):
            t.px(x, y, WATER[2] if y == 3 else (WATER[1] if y < 6 else WATER[0]))
    for y in range(2, 7):  # electrodes: copper cathode (hydrogen), steel anode (oxygen)
        t.px(5, y, COPPER[2] if y == 2 else COPPER[1])
        t.px(10, y, STEEL[3] if y == 2 else STEEL[2])
    t.hline(4, 6, 2, COPPER[3] if lit else COPPER[1])
    t.hline(9, 11, 2, STEEL[4] if lit else STEEL[2])
    if lit:
        for k in range(4):  # hydrogen: twice the bubbles
            x = 4 if k % 2 == 0 else 6
            y = 6 - (ff + k * 2) % 4
            t.px(x, y, HYDROGEN[3])
        for k in range(2):  # oxygen
            x = 9 if k == 0 else 11
            y = 6 - (ff + k * 3) % 4
            t.px(x, y, OXYGEN[3])
        t.px(7, 3, WATER[3]); t.px(8, 3, WATER[3])


def electrolyser_front(f=None):
    return front(act_electrolysis, tier_plasma, 'plasma', f)


def port(t, cx, cy, ring, core):
    """A round pipe port (5x5) with a coloured ring."""
    for y in range(cy - 2, cy + 3):
        for x in range(cx - 2, cx + 3):
            d = math.hypot(x - cx, y - cy)
            if d <= 2.3:
                t.px(x, y, ring if d > 1.2 else core)
    t.px(cx - 1, cy - 1, shade(ring, 1.3))


def electrolyser_side():
    """Two gas outlets above the tier seam (hydrogen white, oxygen blue), a grille below."""
    t = casing()
    well(t, 2, 2, 13, 6, K['d'])
    port(t, 5, 4, HYDROGEN[2], (14, 16, 20))
    port(t, 10, 4, OXYGEN[1], (14, 16, 20))
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


def electrolyser_top():
    return top('plasma')


# ================================================================ Plasma Generator (now fed hydrogen)
def act_ionizer_h2(t, f, lit):
    """Top half: hydrogen in through a nozzle (left), superheated between two electrodes, plasma out (right)."""
    t.rect(2, 3, 3, 5, STEEL[1])  # inlet nozzle
    t.px(2, 3, STEEL[3]); t.px(3, 5, STEEL[0])
    t.px(2, 4, HYDROGEN[2] if lit else HYDROGEN[0])
    for y in range(2, 7):  # electrodes
        t.px(6, y, COPPER[2] if y in (2, 6) else COPPER[1])
        t.px(9, y, COPPER[2] if y in (2, 6) else COPPER[1])
    for y in range(3, 6):  # the hot zone
        for x in (7, 8):
            if lit:
                k = 0.5 + 0.5 * math.sin(f * TAU / FRAMES * 2 + y + x)
                t.px(x, y, mix(P['on'], P['hi'], k * (0.9 if y == 4 else 0.5)))
            else:
                t.px(x, y, shade(P['off'], 0.7 if y == 4 else 0.5))
    t.rect(12, 3, 13, 5, STEEL[1])  # outlet nozzle
    t.px(12, 3, STEEL[3]); t.px(13, 5, STEEL[0])
    t.px(13, 4, P['on'] if lit else P['off'])
    if lit:  # hydrogen specks drifting in, turning to plasma between the electrodes
        for lane, y in enumerate((3, 4, 5)):
            x = 4 + (f + lane * 3) % 8
            if x in (6, 9):
                continue
            c = HYDROGEN[3] if x < 6 else (P['hi'] if x < 9 else P['on'])
            if t.img.getpixel((x, y))[:3] == GLASS or x > 9 or x < 6:
                t.px(x, y, c)


def plasma_generator_front(f=None):
    return front(act_ionizer_h2, tier_plasma, 'plasma', f)


def plasma_generator_side():
    """A hydrogen inlet port above the tier seam, a grille below (no more intake louvres)."""
    t = casing()
    well(t, 4, 2, 11, 6, K['d'])
    port(t, 7, 4, HYDROGEN[2], (14, 16, 20))
    t.px(8, 4, (14, 16, 20))
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


def plasma_generator_top():
    return top('plasma')


# ================================================================ MHD Generator
def act_mhd(t, f, lit):
    """Top half: plasma rushing through a channel between two magnet poles; current sparks off the side electrodes."""
    ff = f if lit else 0
    for x in range(3, 13):  # magnet poles (north above, south below)
        t.px(x, 2, MAG_N if lit else shade(MAG_N, 0.6))
        t.px(x, 6, MAG_S if lit else shade(MAG_S, 0.6))
    t.px(3, 2, shade(MAG_N, 1.25)); t.px(3, 6, shade(MAG_S, 1.25))
    for x in range(2, 14):  # channel
        for y in (3, 4, 5):
            if lit:
                k = 0.5 + 0.5 * math.sin((x - ff * 2) * 0.9 + y * 1.3)
                c = mix(P['off'], P['on'], k) if y != 4 else mix(P['on'], P['hi'], k)
            else:
                c = shade(P['off'], 0.55 if y != 4 else 0.8)
            t.px(x, y, c)
    for y in (3, 5):  # electrodes at the ends
        t.px(2, y, COPPER[2]); t.px(13, y, COPPER[2])
    if lit:
        sx = 3 + (ff * 5) % 10
        t.px(sx, 3 if ff % 2 else 5, (255, 246, 190))


def mhd_front(f=None):
    return front(act_mhd, tier_plasma, 'plasma', f)


def mhd_side():
    """Two big magnet coils (copper windings round a steel core), the tier seam between them."""
    t = casing()
    for (y0, y1) in ((2, 6), (10, 13)):
        for y in range(y0, y1 + 1):
            for x in range(2, 14):
                c = cylinder_row(y, y0, y1, COPPER[3], COPPER[0])
                if x % 2 == 0:
                    c = shade(c, 0.82)
                t.px(x, y, c)
        t.vline(2, y0, y1, STEEL[2]); t.vline(13, y0, y1, STEEL[0])
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', False, 0)
    return t


def mhd_top():
    """FE terminals: copper bus bars inside the tier ring."""
    t = top('plasma')
    for x in range(5, 11):
        t.px(x, 6, COPPER[2]); t.px(x, 9, COPPER[2])
    t.px(5, 6, COPPER[3]); t.px(5, 9, COPPER[3])
    return t


# ================================================================ fluids
def gas_still(palette, f):
    """Soft drifting wisps (gases are thinner than plasma: low contrast, slow)."""
    t = Tex()
    w = TAU / 16
    ph = f * TAU / FRAMES
    for y in range(16):
        for x in range(16):
            v = (math.sin(x * w + ph) + math.sin((x + y) * w * 2 - ph) + 0.6 * math.sin(y * w * 3 + ph)) / 2.6
            v = 0.5 + 0.5 * v
            if v < 0.6:
                c = mix(palette[0], palette[1], v / 0.6)
            elif v < 0.88:
                c = mix(palette[1], palette[2], (v - 0.6) / 0.28)
            else:
                c = mix(palette[2], palette[3], (v - 0.88) / 0.12)
            t.px(x, y, c)
    return t


def hydrogen_still(f):
    return gas_still(HYDROGEN, f)


def oxygen_still(f):
    return gas_still(OXYGEN, f)


# ================================================================ Plasma Cable (same UVs as the energy cable)
def plasma_cable_sheet():
    """Like the energy cable but magenta, with steel bands on the arm and a double ring on the plate (heavier duty)."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))

    def px(x, y, c):
        img.putpixel((x, y), tuple(c[:3]) + (255,))

    for y in range(6):
        for x in range(6):
            edge = x in (0, 5) or y in (0, 5)
            px(x, y, K['o'] if edge else mix(K['l'], K['m'], y / 5))
    for (x, y) in ((2, 2), (3, 2), (2, 3), (3, 3)):
        px(x, y, P['hi'] if (x, y) == (2, 2) else P['on'])
    for (x, y) in ((1, 1), (4, 1), (1, 4), (4, 4)):
        px(x, y, STEEL[2])
    for y in range(8):
        for x in range(8):
            c = mix(K['l'], K['d'], y / 7)
            if y == 0 or x == 0:
                c = K['hi']
            if y == 7 or x == 7:
                c = K['o']
            px(8 + x, y, c)
    for y in range(1, 7):
        for x in range(1, 7):
            outer = x in (1, 6) or y in (1, 6)
            inner = x in (2, 5) or y in (2, 5)
            if outer:
                px(8 + x, y, STEEL[1])
            elif inner:
                px(8 + x, y, shade(P['on'], 0.8))
            else:
                px(8 + x, y, (14, 10, 16))
    px(8 + 3, 3, P['hi'])
    rows = [K['l'], P['on'], shade(P['on'], 0.62), K['o']]
    for x in range(16):
        for y, c in enumerate(rows):
            if x % 8 in (0, 1):
                c = STEEL[3] if y == 0 else (STEEL[2] if y < 3 else STEEL[0])  # steel bands
            elif y == 1 and x % 8 == 4:
                c = P['hi']
            px(x, 8 + y, c)
    return img


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
    # phase D: the Plasma Generator runs on hydrogen; the water generator breathes instead
    plasma_generator_top().img.save(os.path.join(BLOCK, 'plasma_generator_top.png'))
    plasma_generator_side().img.save(os.path.join(BLOCK, 'plasma_generator_side.png'))
    plasma_generator_front().img.save(os.path.join(BLOCK, 'plasma_generator_front.png'))
    save_strip(os.path.join(BLOCK, 'plasma_generator_front_on.png'), plasma_generator_front)
    for name, top_fn, front_fn, side_fn in (('atmospheric_water_generator', awg_top, awg_front, awg_side),
                                            ('electrolyser', electrolyser_top, electrolyser_front, electrolyser_side),
                                            ('mhd_generator', mhd_top, mhd_front, mhd_side)):
        top_fn().img.save(os.path.join(BLOCK, f'{name}_top.png'))
        side_fn().img.save(os.path.join(BLOCK, f'{name}_side.png'))
        front_fn().img.save(os.path.join(BLOCK, f'{name}_front.png'))
        save_strip(os.path.join(BLOCK, f'{name}_front_on.png'), front_fn)
    for name, still in (('hydrogen', hydrogen_still), ('oxygen', oxygen_still)):
        save_strip(os.path.join(BLOCK, f'{name}_still.png'), still)
        save_strip(os.path.join(BLOCK, f'{name}_flow.png'), still)
    air_filter().img.save(os.path.join(ITEM, 'air_filter.png'))
    pipe_sheet().save(os.path.join(BLOCK, 'fluid_pipe.png'))
    tank_side_cutout().img.save(os.path.join(BLOCK, 'fluid_tank_side.png'))
    tank_top().img.save(os.path.join(BLOCK, 'fluid_tank_top.png'))
    save_strip(os.path.join(BLOCK, 'plasma_still.png'), plasma_still)
    save_strip(os.path.join(BLOCK, 'plasma_flow.png'), lambda k: plasma_still(k, flow=True))
    # phase C1
    for name, act in (('plasma_granulator', act_conveyor), ('plasma_smelter', act_conveyor_smelt)):
        top('plasma').img.save(os.path.join(BLOCK, f'{name}_top.png'))
        side('plasma').img.save(os.path.join(BLOCK, f'{name}_side.png'))
        front(act, tier_plasma, 'plasma').img.save(os.path.join(BLOCK, f'{name}_front.png'))
        save_strip(os.path.join(BLOCK, f'{name}_front_on.png'), lambda k, act=act: front(act, tier_plasma, 'plasma', k))
    plasma_cable_sheet().save(os.path.join(BLOCK, 'plasma_cable.png'))
    # phase C2
    for name, act, side_fn in (('circuit_assembler', act_assembler, lambda: side('plasma')),
                               ('ore_washer', act_washer, washer_side)):
        top('plasma').img.save(os.path.join(BLOCK, f'{name}_top.png'))
        side_fn().img.save(os.path.join(BLOCK, f'{name}_side.png'))
        front(act, tier_plasma, 'plasma').img.save(os.path.join(BLOCK, f'{name}_front.png'))
        save_strip(os.path.join(BLOCK, f'{name}_front_on.png'), lambda k, act=act: front(act, tier_plasma, 'plasma', k))
    circuit('plasma', GOLD).img.save(os.path.join(ITEM, 'plasma_circuit.png'))
    battery_top('plasma').img.save(os.path.join(BLOCK, 'plasma_energy_battery_top.png'))
    battery_side('plasma').img.save(os.path.join(BLOCK, 'plasma_energy_battery_side.png'))
    battery_front('plasma', BATTERY_CELLS).img.save(os.path.join(BLOCK, 'plasma_energy_battery_front.png'))
    for level in range(BATTERY_CELLS + 1):
        battery_front('plasma', level).img.save(os.path.join(BLOCK, f'plasma_energy_battery_front_{level}.png'))
    print('ok')
