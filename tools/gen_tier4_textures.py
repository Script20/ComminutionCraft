"""Tier-4 (fusion) textures in the Dark Chassis style. Writes straight into the mod's texture folders.

Usage: python tools/gen_tier4_textures.py

Phase 4A (approved preview: design/previews/phase_4a.png, drawn from these same functions):
- Isotope Separator (centrifuge spinning heavy water out of water), Tritium Breeder (deuterium bombarded into green
  tritium), Pellet Press (pressing the two into pellets): fusion tier bottom (tier_fusion from six_tiers.py),
  shared tier-4 side and top.
- Items: Fusion Fuel Pellet, Reactor Plating, Fusion Circuit. Fluids: deuterium / tritium still + flow strips.

Phase 4B (approved preview: design/previews/phase_4b.png):
- Fusion Reactor blocks: Reactor Casing (+ _on: seams glow, animated), Reactor Glass (cutout window; the core is drawn
  by FusionReactorRenderer), Reactor Port (+ _on), Reactor Controller (front + animated _front_on; top/side = casing).
- Fusion Cable (same UV layout as the other cables), Fusion Energy Battery (battery faces in green).

Phase 4C (approved preview: design/previews/phase_4c.png): the Ore Purifier (washed ore between two oxygen jets,
oxygen inlet on the sides). Purified ore items are drawn by gen_granulated_textures.py.
"""
import math
import os
import sys

from PIL import Image

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TOOLS)
from mekstyle_lib import Tex, mix, shade, FRAMES, TAU, ORE  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import (K, GLASS, STEEL, COPPER, casing, seam, well, front, side, top,  # noqa: E402
                                 cylinder_row, circuit, battery_front, battery_side, battery_top, BATTERY_CELLS)
import gen_tier3_textures as t3  # noqa: E402

dcs.TIERS['fusion'] = dict(off=(32, 84, 44), on=(96, 255, 128), hi=(214, 255, 214))
F = dcs.TIERS['fusion']
WATER = t3.WATER
DEUTERIUM = [(70, 120, 150), (120, 186, 214), (180, 228, 244), (232, 250, 255)]  # pale cyan, a shade deeper than hydrogen
TRITIUM = [(30, 90, 50), (70, 190, 100), (140, 245, 160), (220, 255, 226)]      # the green glow of tritium lights
SILVER = [(110, 116, 130), (160, 166, 180), (204, 210, 222), (240, 244, 250)]

# tier_fusion (the approved tier-4 bottom) lives in six_tiers.py; pull just that function in.
_src = open(os.path.join(TOOLS, 'six_tiers.py')).read()
_fn = _src[_src.index('def tier_fusion'):_src.index('# ---------------------------------------------------------------- 5. ANTIMATTER')]
_ns = dict(dcs.__dict__)
_ns['tc'] = lambda tier, k: dcs.TIERS[tier][k]
exec(_fn, _ns)
tier_fusion = _ns['tier_fusion']


# ================================================================ Isotope Separator
def act_centrifuge(t, f, lit):
    """Top half: a centrifuge drum spinning water; the heavy water (deuterium) settles into a pale band at the rim."""
    ff = f if lit else 0
    for y in range(2, 7):
        for x in range(3, 13):
            c = cylinder_row(y, 2, 6, WATER[2], WATER[0])
            if y in (2, 6):
                c = DEUTERIUM[1] if lit else DEUTERIUM[0]  # heavy water flung to the rim
            if (x + ff * 2) % 5 == 0:
                c = shade(c, 0.6)  # drum ribs spinning past
            t.px(x, y, c)
    t.vline(2, 2, 6, STEEL[2]); t.vline(13, 2, 6, STEEL[0])  # axle bearings
    t.px(2, 4, STEEL[4]); t.px(13, 4, STEEL[3])
    if lit:
        t.px(3 + (ff * 3) % 10, 2, DEUTERIUM[3])


# ================================================================ Tritium Breeder
def act_breeder(t, f, lit):
    """Top half: a chamber where deuterium (pale cyan) is bombarded into tritium, which glows green."""
    t.rect(3, 2, 12, 6, (12, 18, 14))
    t.hline(3, 12, 2, STEEL[1]); t.hline(3, 12, 6, STEEL[0])
    t.vline(2, 3, 5, STEEL[2]); t.vline(13, 3, 5, STEEL[2])  # emitters
    t.px(2, 4, F['on'] if lit else F['off']); t.px(13, 4, F['on'] if lit else F['off'])
    for y in (3, 4, 5):
        for x in range(4, 12):
            k = (x - 4) / 7  # deuterium on the left turns to tritium on the right
            if lit:
                w = 0.5 + 0.5 * math.sin(f * TAU / FRAMES * 2 + x * 0.9 + y)
                c = mix(mix(DEUTERIUM[2], TRITIUM[2], k), mix(DEUTERIUM[3], TRITIUM[3], k), w * (0.6 if y == 4 else 0.3))
            else:
                c = shade(mix(DEUTERIUM[0], TRITIUM[0], k), 0.7)
            t.px(x, y, c)


# ================================================================ Pellet Press
def act_pellet_press(t, f, lit):
    """Top half: a press squeezing deuterium and tritium into a pellet; pellets roll out on the right."""
    ff = f if lit else 0
    for x in range(2, 14):  # belt
        t.px(x, 6, (50, 52, 58) if (x - ff) % 3 else (30, 32, 36))
    t.hline(4, 10, 2, STEEL[1])  # gantry
    t.px(5, 2, DEUTERIUM[2] if lit else DEUTERIUM[0]); t.px(9, 2, TRITIUM[2] if lit else TRITIUM[0])  # the two feeds
    stroke = (0, 0, 1, 1, 1, 0, 0, 0)[ff % FRAMES] if lit else 0
    for y in range(3, 4 + stroke):
        t.px(7, y, STEEL[3]); t.px(8, y, STEEL[2])
    hy = 4 + stroke
    t.hline(6, 9, hy, STEEL[3]); t.px(6, hy, STEEL[4]); t.px(9, hy, STEEL[1])
    t.vline(4, 3, 5, K['d']); t.vline(11, 3, 5, K['d'])
    if lit and stroke:
        t.px(7, hy + 1 if hy < 5 else hy, F['hi'])
    px_ = 12 + (ff // 2) % 2  # a finished pellet
    if px_ < 14:
        t.px(px_, 5, F['on'] if lit else F['off'])


def machine_side():
    return side('fusion')


def machine_top():
    return top('fusion')


def machine_front(action):
    return lambda f=None: front(action, tier_fusion, 'fusion', f)


# ================================================================ items
def fuel_pellet():
    """A small capsule, deuterium half (pale cyan) and tritium half (green), with steel caps."""
    t = Tex()
    for y in range(16):
        for x in range(16):
            # capsule along the diagonal from (4, 11) to (11, 4), radius ~2.6
            ax, ay, bx, by = 4.5, 11.5, 11.5, 4.5
            vx, vy = bx - ax, by - ay
            u = max(0.0, min(1.0, ((x + 0.5 - ax) * vx + (y + 0.5 - ay) * vy) / (vx * vx + vy * vy)))
            d = math.hypot(x + 0.5 - (ax + vx * u), y + 0.5 - (ay + vy * u))
            if d > 2.7:
                continue
            light = 0.5 + 0.5 * (-(x - 7.5) - (y - 7.5)) / 12 - d / 6
            if d > 2.1:
                c = STEEL[0]
            elif u < 0.12 or u > 0.88:
                c = STEEL[3] if light > 0.45 else STEEL[1]
            else:
                ramp = DEUTERIUM if u < 0.5 else TRITIUM
                c = ramp[3] if light > 0.62 else ramp[2] if light > 0.45 else ramp[1]
            t.px(x, y, c)
    t.px(6, 8, (255, 255, 255))
    return t


def reactor_plating():
    """A thick plate: steel with rivets and a green tier seam across it (the reactor's shell blocks are made of it)."""
    t = Tex()
    x0, y0, x1, y1 = 2, 3, 13, 12
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            t.px(x, y, mix(STEEL[3], STEEL[1], (y - y0) / (y1 - y0)))
    t.hline(x0, x1, y0, STEEL[4]); t.vline(x0, y0, y1, STEEL[3])
    t.hline(x0, x1, y1, STEEL[0]); t.vline(x1, y0, y1, STEEL[0])
    t.hline(x0 + 1, x1 - 1, 7, F['off']); t.hline(x0 + 1, x1 - 1, 8, shade(F['on'], 0.8))
    for (x, y) in ((4, 5), (11, 5), (4, 10), (11, 10)):
        t.px(x, y, STEEL[0]); t.px(x - 1, y - 1, STEEL[4])
    return t


def fusion_circuit():
    return circuit('fusion', SILVER)


# ================================================================ fluids
def deuterium_still(f):
    return t3.gas_still(DEUTERIUM, f)


def tritium_still(f):
    return t3.gas_still(TRITIUM, f)



# ================================================================ Reactor Casing
def reactor_casing():
    """Heavy plated block: four riveted plates split by green seams (it's what the reactor's frame is built from)."""
    t = casing()
    for y in range(1, 15):
        for x in range(1, 15):
            t.px(x, y, mix(STEEL[2], STEEL[0], (y - 1) / 13) if (x - 1) % 7 and (y - 1) % 7 else K['d'])
    seam(t, [(x, 7) for x in range(1, 15)] + [(x, 8) for x in range(1, 15)], 'fusion', False, 0)
    for x in (7, 8):
        for y in range(1, 15):
            t.px(x, y, F['off'])
    for (x, y) in ((3, 3), (12, 3), (3, 12), (12, 12)):
        t.px(x, y, STEEL[0]); t.px(x - 1, y - 1, STEEL[4])
    t.hline(1, 14, 0, STEEL[3]); t.vline(0, 1, 14, STEEL[2])
    return t


def reactor_casing_lit(f):
    """Formed and running: the seams glow."""
    t = reactor_casing()
    for x in (7, 8):
        for y in range(1, 15):
            t.px(x, y, mix(F['on'], F['hi'], 0.2 + 0.3 * math.sin((f + y) * TAU / FRAMES)))
    for x in range(1, 15):
        for y in (7, 8):
            t.px(x, y, mix(F['on'], F['hi'], 0.2 + 0.3 * math.sin((f + x) * TAU / FRAMES)))
    return t


# ================================================================ Reactor Glass
def reactor_glass(core=None):
    """Steel frame, a wide window (see-through in game: cutout), green-tinted corners. `core` paints the glow behind it."""
    t = Tex()
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            inner = x in (1, 14) or y in (1, 14)
            if edge:
                t.px(x, y, STEEL[1] if x == 0 or y == 0 else STEEL[0])
            elif inner:
                t.px(x, y, STEEL[2] if x == 1 or y == 1 else K['d'])
            elif core is not None:
                t.px(x, y, core(x, y))
            else:
                t.img.putpixel((x, y), (0, 0, 0, 0))
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        t.px(x, y, F['off'])
    for (x, y) in ((3, 4), (4, 3), (11, 12), (12, 11)):  # glints
        b = t.img.getpixel((x, y))
        t.img.putpixel((x, y), (210, 240, 220, 255 if b[3] else 120))
    return t


def core_glow(f):
    """What a window shows of the running core: a green plasma torus, brightest in the middle."""
    def colour(x, y):
        d = math.hypot((x - 7.5) / 6, (y - 7.5) / 3.2)
        ring = abs(d - 0.75)
        pulse = 0.85 + 0.15 * math.sin(f * TAU / FRAMES * 2 + x * 0.4)
        if ring < 0.22:
            return mix(F['on'], F['hi'], (1 - ring / 0.22) * pulse)
        if d < 0.5:
            return mix((20, 60, 30), F['on'], 0.4 * pulse)
        return mix((10, 24, 14), (24, 60, 32), max(0.0, 1 - ring))
    return colour


# ================================================================ Reactor Port
def reactor_port(lit=False):
    """Where the reactor connects to the world: pellets in, plasma in, tritium out, FE out (one port does all four)."""
    t = reactor_casing()
    for y in range(3, 13):
        for x in range(3, 13):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 4.6:
                t.px(x, y, (F['on'] if lit else F['off']) if d > 3.6 else (STEEL[1] if d > 2.6 else (14, 18, 16)))
    for (x, y) in ((7, 3), (8, 12), (3, 8), (12, 7)):  # four small arrows: in / out
        t.px(x, y, STEEL[4])
    if lit:
        t.px(7, 7, F['hi']); t.px(8, 8, F['on'])
    return t


# ================================================================ Reactor Controller
def reactor_controller(f=None):
    """The reactor's brain and GUI: a status screen (temperature bars, a core readout) over the fusion tier panel."""
    lit = f is not None
    f = f or 0
    t = reactor_casing() if not lit else reactor_casing_lit(f)
    well(t, 2, 2, 13, 13)
    for i in range(5):  # temperature bars rising
        h = (2 + int(3 + 2 * math.sin(f * TAU / FRAMES + i))) if lit else 1
        for k in range(h):
            y = 12 - k
            t.px(3 + i * 2, y, mix(F['on'], F['hi'], k / 7) if lit else F['off'])
    for y in range(3, 7):  # core readout (a little torus)
        for x in range(9, 13):
            d = math.hypot(x - 10.5, (y - 4.5) * 1.6)
            if 0.8 < d < 1.9:
                t.px(x, y, F['hi'] if lit and (x + y + f) % 3 else (F['on'] if lit else F['off']))
    t.px(12, 12, F['on'] if lit else (120, 40, 40))  # status lamp: green when running
    return t


# ================================================================ Fusion Cable and Battery
def fusion_cable_sheet():
    """Same UV layout as the other cables: green core, heavy grey bands, a triple-ringed plate (the fast power cable)."""
    img = t3.plasma_cable_sheet().copy()
    px = img.load()
    for y in range(16):
        for x in range(16):
            c = px[x, y]
            if c[3] == 0:
                continue
            r, g, b, a = c
            if r > 150 and b > 120 and g < 150:  # plasma magenta -> fusion green
                k = (r + b) / 2 / 255
                px[x, y] = tuple(int(v) for v in mix(F['off'], F['on'], k)[:3]) + (a,)
            elif r > 200 and g > 150 and b > 200:  # magenta highlight -> green highlight
                px[x, y] = tuple(F['hi'][:3]) + (a,)
    for x in range(16):  # an extra band on the arm, at every 4th pixel
        if x % 8 == 4:
            px[x, 9] = tuple(t3.CONDUIT_COLLAR[1][:3]) + (255,)
            px[x, 10] = tuple(t3.CONDUIT_COLLAR[2][:3]) + (255,)
    for i in range(8, 16):  # the plate gets an outer green ring
        px[i, 0] = tuple(shade(F['on'], 0.7)[:3]) + (255,)
    return img


OXYGEN = t3.OXYGEN


# ================================================================ Ore Purifier
def act_purifier(t, f, lit):
    """Top half: a chunk of washed ore held between two oxygen jets (blue), burning off its impurities in sparks."""
    ff = f if lit else 0
    t.vline(2, 3, 5, STEEL[2]); t.vline(13, 3, 5, STEEL[2])  # nozzles
    t.px(2, 4, OXYGEN[2] if lit else OXYGEN[0]); t.px(13, 4, OXYGEN[2] if lit else OXYGEN[0])
    t.rect(6, 3, 9, 5, ORE[3])  # the ore chunk on its stand
    t.px(6, 3, ORE[0]); t.px(9, 5, shade(ORE[3], 0.6)); t.px(7, 4, ORE[1])
    t.hline(5, 10, 6, STEEL[1])
    if lit:
        for x in range(3, 6):  # oxygen jets, flickering
            if (x + ff) % 2 == 0 or x == 5:
                t.px(x, 4, OXYGEN[3] if x == 5 else OXYGEN[1])
        for x in range(10, 13):
            if (x + ff) % 2 == 0 or x == 10:
                t.px(x, 4, OXYGEN[3] if x == 10 else OXYGEN[1])
        for k in range(3):  # sparks flying off the chunk
            a = (ff + k * 3) * TAU / FRAMES
            sx, sy = int(round(7.5 + 2.6 * math.cos(a))), int(round(4 - 1.6 * abs(math.sin(a))))
            if 2 <= sy <= 6 and 3 <= sx <= 12:
                t.px(sx, sy, (255, 236, 160) if k else F['hi'])
        t.px(7, 4, mix(ORE[1], (255, 230, 170), 0.6))  # the chunk glows where the jets meet


def purifier_side():
    """An oxygen inlet port above the seam (blue ring, like the Electrolyser's oxygen outlet), a grille below."""
    t = casing()
    well(t, 4, 2, 11, 6, K['d'])
    t3.port(t, 7, 4, OXYGEN[1], (14, 16, 20))
    t.px(8, 4, (14, 16, 20))
    seam(t, [(x, 8) for x in range(1, 15)], 'fusion', False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


def purifier_front(f=None):
    return front(act_purifier, tier_fusion, 'fusion', f)


ROOT = os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures')
BLOCK = os.path.join(ROOT, 'block')
ITEM = os.path.join(ROOT, 'item')

if __name__ == '__main__':
    os.makedirs(BLOCK, exist_ok=True)
    os.makedirs(ITEM, exist_ok=True)
    for name, act in (('isotope_separator', act_centrifuge), ('tritium_breeder', act_breeder), ('pellet_press', act_pellet_press)):
        machine_top().img.save(os.path.join(BLOCK, f'{name}_top.png'))
        machine_side().img.save(os.path.join(BLOCK, f'{name}_side.png'))
        machine_front(act)().img.save(os.path.join(BLOCK, f'{name}_front.png'))
        t3.save_strip(os.path.join(BLOCK, f'{name}_front_on.png'), machine_front(act))
    fuel_pellet().img.save(os.path.join(ITEM, 'fusion_fuel_pellet.png'))
    reactor_plating().img.save(os.path.join(ITEM, 'reactor_plating.png'))
    fusion_circuit().img.save(os.path.join(ITEM, 'fusion_circuit.png'))
    for name, still in (('deuterium', deuterium_still), ('tritium', tritium_still)):
        t3.save_strip(os.path.join(BLOCK, f'{name}_still.png'), still)
        t3.save_strip(os.path.join(BLOCK, f'{name}_flow.png'), still)
    # phase 4B
    reactor_casing().img.save(os.path.join(BLOCK, 'reactor_casing.png'))
    t3.save_strip(os.path.join(BLOCK, 'reactor_casing_on.png'), reactor_casing_lit)
    reactor_glass().img.save(os.path.join(BLOCK, 'reactor_glass.png'))
    reactor_port().img.save(os.path.join(BLOCK, 'reactor_port.png'))
    reactor_port(True).img.save(os.path.join(BLOCK, 'reactor_port_on.png'))
    reactor_casing().img.save(os.path.join(BLOCK, 'reactor_controller_top.png'))
    reactor_casing().img.save(os.path.join(BLOCK, 'reactor_controller_side.png'))
    reactor_controller().img.save(os.path.join(BLOCK, 'reactor_controller_front.png'))
    t3.save_strip(os.path.join(BLOCK, 'reactor_controller_front_on.png'), reactor_controller)
    fusion_cable_sheet().save(os.path.join(BLOCK, 'fusion_cable.png'))
    battery_top('fusion').img.save(os.path.join(BLOCK, 'fusion_energy_battery_top.png'))
    battery_side('fusion').img.save(os.path.join(BLOCK, 'fusion_energy_battery_side.png'))
    battery_front('fusion', BATTERY_CELLS).img.save(os.path.join(BLOCK, 'fusion_energy_battery_front.png'))
    for level in range(BATTERY_CELLS + 1):
        battery_front('fusion', level).img.save(os.path.join(BLOCK, f'fusion_energy_battery_front_{level}.png'))
    # phase 4C
    machine_top().img.save(os.path.join(BLOCK, 'ore_purifier_top.png'))
    purifier_side().img.save(os.path.join(BLOCK, 'ore_purifier_side.png'))
    purifier_front().img.save(os.path.join(BLOCK, 'ore_purifier_front.png'))
    t3.save_strip(os.path.join(BLOCK, 'ore_purifier_front_on.png'), purifier_front)
    print('ok')
