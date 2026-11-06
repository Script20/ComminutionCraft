"""Tier 5 phase B previews: the Annihilation Generator (antimatter -> FE), the Antimatter Circuit, the Antimatter Cable
and the Antimatter Energy Battery. Builds on phase_5a_preview (antimatter tier colours, the tier-5 bottom).

Run: tools/.venv/Scripts/python design/previews/phase_5b_preview.py design/previews/phase_5b.png design/previews/phase_5b_running.gif
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.join(HERE, '..', '..', 'tools')
sys.path.insert(0, TOOLS)
sys.path.insert(0, HERE)
from mekstyle_lib import mix, shade, iso_block, up, FRAMES, TAU  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import K, STEEL, COPPER  # noqa: E402
import phase_5a_preview as p5  # noqa: E402  (defines dcs.TIERS['antimatter'])
import fluid_pipe_preview as fp  # noqa: E402
import gen_tier3_textures as t3  # noqa: E402
import gen_tier4_textures as t4  # noqa: E402

A = p5.A
VOID = p5.VOID
MATTER = [(70, 60, 52), (130, 110, 92), (196, 170, 140), (240, 222, 196)]  # ordinary matter: warm dust
PLATINUM = [(112, 118, 140), (162, 170, 196), (208, 216, 238), (244, 247, 255)]


# ================================================================ Annihilation Generator
def act_annihilation(t, f, lit):
    """Top half: a vacuum chamber between two magnet coils. An icy antimatter injector on the left and a warm matter
    injector on the right; running, both streams meet in the middle in a white flash that throws gamma sparks."""
    ff = f if lit else 0
    for y in range(2, 7):  # the chamber (deep void)
        for x in range(2, 14):
            t.px(x, y, mix(VOID[0], VOID[1], 0.35 + 0.25 * math.sin(x * 0.7 + y)) if lit else (12, 13, 20))
    for x in range(2, 14):  # magnet coils top and bottom: dark copper windings, a glint running along when on
        c = shade(COPPER[1], 0.75) if x % 2 else shade(COPPER[0], 0.9)
        t.px(x, 2, A['on'] if lit and (x + ff) % 6 == 0 else c)
        t.px(x, 6, A['on'] if lit and (x - ff) % 6 == 0 else c)
    for y in (3, 4, 5):  # injectors
        t.px(2, y, A['off'] if not lit else (A['on'] if y == 4 else A['off']))
        t.px(13, y, MATTER[1] if not lit else (MATTER[3] if y == 4 else MATTER[1]))
    t.px(3, 4, STEEL[2]); t.px(12, 4, STEEL[2])
    if not lit:
        t.px(7, 4, (30, 34, 46)); t.px(8, 4, (30, 34, 46))
        return
    for k in range(2):  # the two streams racing in
        lx = 4 + (ff + k * 2) % 4
        rx = 11 - (ff + k * 2) % 4
        t.px(lx, 4, A['hi'] if k == 0 else A['on'])
        t.px(rx, 4, MATTER[3] if k == 0 else MATTER[2])
    pulse = 0.5 + 0.5 * math.sin(ff * TAU / FRAMES * 2)
    for y in range(3, 6):  # the flash
        for x in range(6, 10):
            d = math.hypot(x - 7.5, y - 4)
            if d < 1.2 + pulse:
                t.px(x, y, mix(A['on'], (255, 255, 255), 1 - d / 2.4))
    for k in range(3):  # gamma sparks flung out of the flash
        ang = (ff * 0.9 + k * TAU / 3)
        r = 1.5 + (ff + k) % 3
        sx, sy = int(round(7.5 + math.cos(ang) * r * 1.4)), int(round(4 + math.sin(ang) * r * 0.6))
        if 3 <= sx <= 12 and 3 <= sy <= 5:
            t.px(sx, sy, (255, 236, 170))


def generator_front(f=None):
    return dcs.front(act_annihilation, p5.tier_antimatter, 'antimatter', f)


def generator_side():
    return dcs.side('antimatter')


def generator_top():
    return dcs.top('antimatter')


# ================================================================ items, cable, battery
def antimatter_circuit():
    """The usual circuit, but on a void-black board (its traces platinum), so it reads apart from the Fusion Circuit."""
    t = dcs.circuit('antimatter', PLATINUM)
    board = {K['l'], K['m'], K['hi'], K['o']}
    for y in range(16):
        for x in range(16):
            c = t.img.getpixel((x, y))
            if c[3] and c[:3] in board or (c[3] and abs(c[0] - c[1]) < 6 and abs(c[1] - c[2]) < 10 and 30 < c[0] < 90):
                k = (c[0] - 16) / 84
                t.img.putpixel((x, y), tuple(int(v) for v in mix(VOID[0], VOID[2], max(0.0, min(1.0, k)) * 0.7)[:3]) + (255,))
    return t


def antimatter_cable_sheet():
    """Same UV layout as the other cables: the fusion cable recoloured icy white on a void-dark core, plus copper coil
    dots on the bands (the magnets that keep it contained) - the fastest power cable."""
    img = t4.fusion_cable_sheet().copy()
    px = img.load()
    F = t4.F
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            if g > 150 and r < 200 and b < 200 and g > r + 40:  # fusion green -> icy blue-white
                k = g / 255
                px[x, y] = tuple(int(v) for v in mix(VOID[2], A['on'], k * 0.8)[:3]) + (a,)
            elif g > r + 25 and g > b + 25:  # dark greens -> void blue
                px[x, y] = tuple(int(v) for v in mix(VOID[1], VOID[2], g / 255)[:3]) + (a,)
            elif r > 200 and g > 240 and b > 200:  # green highlight -> white
                px[x, y] = (255, 255, 255, a)
    for x in range(16):  # copper coil dots on the bands
        if x % 8 == 1:
            px[x, 8] = tuple(COPPER[2][:3]) + (255,)
            px[x, 11] = tuple(COPPER[1][:3]) + (255,)
    return img


def battery(level):
    return dcs.battery_front('antimatter', level)


# ================================================================ sheet + gif
def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1320, 900), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)

    dr.text((20, 14), 'Annihilation Generator (antimatter in -> massive FE out)', font=font, fill=head)
    sheet.alpha_composite(iso_block(generator_top().img, generator_front().img, generator_side().img, 190), (20, 50))
    sheet.alpha_composite(iso_block(generator_top().img, generator_front(3).img, generator_side().img, 190), (230, 50))
    dr.text((70, 262), 'idle', font=small, fill=label)
    dr.text((280, 262), 'running', font=small, fill=label)
    for k, (name, im) in enumerate([('Front', generator_front().img), ('Front (running)', generator_front(3).img),
                                    ('Side', generator_side().img), ('Top', generator_top().img)]):
        x = 460 + k * 160
        sheet.alpha_composite(up(im, 8), (x, 50))
        dr.text((x, 186), name, font=small, fill=label)

    y1 = 310
    dr.text((20, y1), 'Antimatter Circuit (Plasma and Fusion beside it)', font=font, fill=head)
    circuits = [('Plasma', dcs.circuit('plasma', t3.GOLD) if hasattr(t3, 'GOLD') else None),
                ('Fusion', t4.fusion_circuit()), ('Antimatter', antimatter_circuit())]
    for k, (name, t) in enumerate(circuits):
        if t is None:
            continue
        x = 20 + k * 150
        sheet.alpha_composite(up(t.img, 8), (x, y1 + 36))
        dr.text((x, y1 + 172), name, font=small, fill=label)

    dr.text((520, y1), 'Antimatter Cable (fastest power cable)', font=font, fill=head)
    cable = antimatter_cable_sheet()
    scene = fp.scene(cable)
    sheet.alpha_composite(scene.crop((60, 0, 480, 330)).resize((300, 236)), (520, y1 + 34))
    sheet.alpha_composite(fp.item(cable), (830, y1 + 30))
    sheet.alpha_composite(up(cable, 8), (1150, y1 + 40))
    sheet.alpha_composite(up(t4.fusion_cable_sheet(), 4), (1170, y1 + 190))
    dr.text((1150, y1 + 176), 'sheet / (fusion)', font=small, fill=label)

    y2 = 610
    dr.text((20, y2), 'Antimatter Energy Battery', font=font, fill=head)
    sheet.alpha_composite(iso_block(dcs.battery_top('antimatter').img, battery(dcs.BATTERY_CELLS).img,
                                    dcs.battery_side('antimatter').img, 190), (20, y2 + 36))
    for lvl in range(dcs.BATTERY_CELLS + 1):
        x = 250 + lvl * 110
        sheet.alpha_composite(up(battery(lvl).img, 6), (x, y2 + 50))
        dr.text((x + 30, y2 + 152), f'{lvl}/6', font=small, fill=label)
    sheet.alpha_composite(up(dcs.battery_side('antimatter').img, 6), (1040, y2 + 50))
    sheet.alpha_composite(up(dcs.battery_top('antimatter').img, 6), (1150, y2 + 50))
    dr.text((1040, y2 + 152), 'side', font=small, fill=label)
    dr.text((1150, y2 + 152), 'top', font=small, fill=label)
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (560, 230), BG)
        g.alpha_composite(iso_block(generator_top().img, generator_front(k).img, generator_side().img, 190), (10, 20))
        g.alpha_composite(up(generator_front(k).img, 12), (360, 20))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
