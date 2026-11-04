"""Tier 4 phase A preview (fusion fuel): Isotope Separator, Tritium Breeder, Pellet Press, Fusion Fuel Pellet,
Reactor Plating, Fusion Circuit, and the Deuterium / Tritium fluids. Functions move into a tools script once approved.

Usage: tools/.venv/Scripts/python.exe design/previews/phase_4a_preview.py <out.png> <out.gif>
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

TOOLS = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'tools')
sys.path.insert(0, TOOLS)
from mekstyle_lib import Tex, mix, shade, iso_block, up, FRAMES, TAU  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import (K, GLASS, STEEL, COPPER, casing, seam, well, front, side, top,  # noqa: E402
                                 cylinder_row, circuit)
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


# ================================================================ sheet
MACHINES = [
    ('Isotope Separator', 'water -> deuterium (heavy water spun out)', act_centrifuge),
    ('Tritium Breeder', 'deuterium + lots of FE -> tritium', act_breeder),
    ('Pellet Press', 'deuterium + tritium -> fuel pellet', act_pellet_press),
]


def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1320, 940), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)
    tp, sd = machine_top().img, machine_side().img

    dr.text((20, 14), 'Tier 4 phase A: fusion fuel (off / running), green fusion tier bottom', font=font, fill=head)
    for i, (name, note, act) in enumerate(MACHINES):
        x = 20 + i * 430
        fr = machine_front(act)
        off, on = fr().img, fr(3).img
        sheet.alpha_composite(iso_block(tp, off, sd, 170), (x - 5, 50))
        sheet.alpha_composite(iso_block(tp, on, sd, 170), (x + 175, 50))
        dr.text((x, 225), name, font=small, fill=head)
        dr.text((x, 244), note, font=small, fill=label)
        for k, (lab, im) in enumerate((('front', off), ('on', on), ('side', sd), ('top', tp))):
            sheet.alpha_composite(up(im, 4), (x + k * 72, 272))
        dr.text((x, 340), 'front      on       side       top', font=small, fill=label)
        for k in range(FRAMES):
            sheet.alpha_composite(up(fr(k).img, 2), (x + k * 36, 366))

    y0 = 430
    dr.text((20, y0), 'Items', font=font, fill=head)
    items = [('Fusion Fuel Pellet', fuel_pellet()), ('Reactor Plating', reactor_plating()),
             ('Fusion Circuit', fusion_circuit()), ('Plasma Circuit (for comparison)', circuit('plasma', t3.GOLD))]
    for k, (name, it) in enumerate(items):
        x = 20 + k * 250
        sheet.alpha_composite(up(it.img, 8), (x, y0 + 36))
        dr.rectangle((x + 140, y0 + 70, x + 193, y0 + 123), fill=(139, 139, 139), outline=(55, 55, 55))
        sheet.alpha_composite(up(it.img, 3), (x + 143, y0 + 73))
        dr.text((x, y0 + 170), name, font=small, fill=label)

    y1 = 650
    dr.text((20, y1), 'Deuterium and Tritium (still, 8 frames) and in a Fluid Tank', font=font, fill=head)
    for k in range(FRAMES):
        sheet.alpha_composite(up(deuterium_still(k).img, 3), (20 + k * 52, y1 + 40))
        sheet.alpha_composite(up(tritium_still(k).img, 3), (20 + k * 52, y1 + 100))
    dr.text((20, y1 + 152), 'deuterium (top), tritium (bottom)', font=small, fill=label)
    ttop = t3.tank_top().img
    for k, (fn, nm) in enumerate(((deuterium_still, 'deuterium'), (tritium_still, 'tritium'), (t3.hydrogen_still, 'hydrogen'))):
        tank = t3.tank_side_cutout()
        fl = fn(2).img
        for y in range(5, 15):
            for x in range(3, 13):
                tank.px(x, y, fl.getpixel((x, y))[:3])
        sheet.alpha_composite(iso_block(ttop, tank.img, tank.img, 130), (480 + k * 160, y1 + 30))
        dr.text((510 + k * 160, y1 + 165), nm, font=small, fill=label)
    dr.text((20, y1 + 200), 'Machine GUIs follow the fluid-machine layout (energy left, fluid gauges, arrow); mock-ups come with the code.',
            font=small, fill=label)
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (780, 250), BG)
        for i, (_, _, act) in enumerate(MACHINES):
            g.alpha_composite(iso_block(tp, machine_front(act)(k).img, sd, 250), (i * 260, 0))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
