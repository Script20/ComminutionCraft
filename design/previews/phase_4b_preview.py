"""Tier 4 phase B preview (fusion power): the 3x3x3 Fusion Reactor's blocks (Reactor Casing, Reactor Glass, Reactor
Port, Reactor Controller), the whole reactor unformed / running, the Fusion Cable and the Fusion Energy Battery.

Usage: tools/.venv/Scripts/python.exe design/previews/phase_4b_preview.py <out.png> <out.gif>
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

TOOLS = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'tools')
sys.path.insert(0, TOOLS)
from mekstyle_lib import Tex, mix, shade, iso_block, up, FRAMES, TAU  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import (K, GLASS, STEEL, COPPER, casing, seam, well, battery_front, battery_side,  # noqa: E402
                                 battery_top, BATTERY_CELLS)
import gen_tier3_textures as t3  # noqa: E402
import gen_tier4_textures as t4  # noqa: E402

F = dcs.TIERS['fusion']


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
    """Same UV layout as the other cables: green core, heavy steel bands, a triple-ringed plate (the fast power cable)."""
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
    for x in range(16):  # an extra steel band on the arm, at every 4th pixel
        if x % 8 == 4:
            px[x, 9] = tuple(STEEL[3][:3]) + (255,)
            px[x, 10] = tuple(STEEL[2][:3]) + (255,)
    for i in range(8, 16):  # the plate gets an outer green ring
        px[i, 0] = tuple(shade(F['on'], 0.7)[:3]) + (255,)
    return img


def cable_run(sheet, length=3):
    W = 16 * length
    img = Image.new('RGBA', (W, 8), (0, 0, 0, 0))
    arm = sheet.crop((0, 8, 16, 12))
    for k in range(length):
        img.paste(arm, (16 * k, 2))
    plate = sheet.crop((8, 0, 9, 8))
    img.paste(plate, (0, 0)); img.paste(plate, (W - 1, 0))
    return img


# ================================================================ multiblock view
def iso_structure(blocks, size):
    """Draws blocks {(gx, gy, gz): (top, front, side)} as one isometric picture (viewer at +x, +y, +z)."""
    unit = size / 2.05
    c30 = math.cos(math.radians(30))
    W, H = int(size * 2.6), int(size * 3.4)
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    for (gx, gy, gz) in sorted(blocks, key=lambda p: (p[0] + p[2] + p[1], p[1])):
        tp, fr, sd = blocks[(gx, gy, gz)]
        cube = iso_block(tp, fr, sd, size)
        dx = (gx - gz) * c30 * unit
        dy = ((gx + gz) * 0.5 - gy) * unit
        img.alpha_composite(cube, (int(W / 2 - size / 2 + dx), int(size * 1.05 + dy)))
    return img


def reactor_blocks(f=None):
    """The 3x3x3 reactor: casing frame, glass in the face centres, controller in the front centre, a port on the right
    face's lower middle, the core (hollow) in the middle."""
    lit = f is not None
    cas = (reactor_casing_lit(f) if lit else reactor_casing()).img
    glass = (reactor_glass(core_glow(f)) if lit else reactor_glass(lambda x, y: (16, 20, 18))).img
    blocks = {}
    for gx in range(3):
        for gy in range(3):
            for gz in range(3):
                if (gx, gy, gz) == (1, 1, 1):
                    continue
                centres = [gx == 1, gy == 1, gz == 1].count(True)
                face = cas
                fr = sd = tp = cas
                if centres == 2:  # face centre: glass
                    tp = fr = sd = glass
                blocks[(gx, gy, gz)] = (tp, fr, sd)
    ctrl = reactor_controller(f).img
    blocks[(1, 1, 2)] = (cas, ctrl, cas)            # front centre: controller
    port = reactor_port(lit).img
    blocks[(2, 0, 1)] = (cas, cas, port)             # right face, bottom middle: a port
    return blocks


def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1320, 1000), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)

    dr.text((20, 14), 'Tier 4 phase B: the 3x3x3 Fusion Reactor (unformed / running)', font=font, fill=head)
    sheet.alpha_composite(iso_structure(reactor_blocks(), 110), (0, 40))
    sheet.alpha_composite(iso_structure(reactor_blocks(3), 110), (320, 40))
    dr.text((60, 360), 'built, not running', font=small, fill=label)
    dr.text((380, 360), 'running: seams glow, core visible through the glass', font=small, fill=label)

    x0 = 700
    dr.text((x0, 50), 'Reactor blocks', font=font, fill=head)
    parts = [('Reactor Casing', reactor_casing().img), ('Reactor Glass', reactor_glass().img),
             ('Reactor Port', reactor_port().img), ('Reactor Controller', reactor_controller().img),
             ('Controller (running)', reactor_controller(3).img), ('Glass (running)', reactor_glass(core_glow(3)).img)]
    for k, (name, im) in enumerate(parts):
        x = x0 + (k % 3) * 200
        y = 90 + (k // 3) * 140
        sheet.alpha_composite(up(im, 6), (x, y))
        dr.text((x, y + 100), name, font=small, fill=label)
    dr.text((x0, 380), 'Frame and edges: casing (or ports). Face centres: glass, ports,', font=small, fill=label)
    dr.text((x0, 400), 'or the one controller. Middle: hollow (the core).', font=small, fill=label)

    y1 = 450
    dr.text((20, y1), 'Fusion Cable (fast power cable, same shape as the others)', font=font, fill=head)
    fc = fusion_cable_sheet()
    sheet.alpha_composite(up(fc, 8), (20, y1 + 40))
    dr.text((20, y1 + 172), 'texture sheet', font=small, fill=label)
    ec = Image.open(os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures',
                                 'block', 'energy_cable.png')).convert('RGBA')
    sheet.alpha_composite(up(cable_run(ec, 3), 6), (180, y1 + 50))
    sheet.alpha_composite(up(cable_run(fc, 3), 6), (180, y1 + 110))
    dr.text((480, y1 + 60), 'Energy Cable', font=small, fill=label)
    dr.text((480, y1 + 120), 'Fusion Cable', font=small, fill=label)

    dr.text((700, y1), 'Fusion Energy Battery', font=font, fill=head)
    sheet.alpha_composite(iso_block(battery_top('plasma').img, battery_front('plasma', 6).img, battery_side('plasma').img, 150), (690, y1 + 34))
    sheet.alpha_composite(iso_block(battery_top('fusion').img, battery_front('fusion', 4).img, battery_side('fusion').img, 150), (850, y1 + 34))
    dr.text((710, y1 + 190), 'Plasma Battery', font=small, fill=label)
    dr.text((870, y1 + 190), 'Fusion (4/6 charged)', font=small, fill=label)
    for lvl in range(BATTERY_CELLS + 1):
        sheet.alpha_composite(up(battery_front('fusion', lvl).img, 2), (1020 + (lvl % 4) * 36, y1 + 50 + (lvl // 4) * 40))
    dr.text((1020, y1 + 140), 'charge 0..6', font=small, fill=label)

    y2 = 700
    dr.text((20, y2), 'Controller GUI (comes with the code): status, core temperature, energy, plasma (to ignite),', font=small, fill=label)
    dr.text((20, y2 + 20), 'tritium and pellet count. Ports are set with the wrench / side config like any machine face.', font=small, fill=label)
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (640, 330), BG)
        g.alpha_composite(iso_structure(reactor_blocks(k), 110), (0, 0))
        g.alpha_composite(up(reactor_controller(k).img, 8), (370, 100))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
