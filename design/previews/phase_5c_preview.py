"""Tier 5 phase C previews: the Froth Flotation Cell (the 4x step), Ore Concentrate (one per metal) and the antimatter
machine suite (Antimatter Granulator, Smelter, Washer, Purifier) - the tier-3/4 machines' tops over the approved
tier-5 bottom, with icy seams.

Run: tools/.venv/Scripts/python design/previews/phase_5c_preview.py design/previews/phase_5c.png design/previews/phase_5c_running.gif
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.join(HERE, '..', '..', 'tools')
sys.path.insert(0, TOOLS)
sys.path.insert(0, HERE)
from mekstyle_lib import Tex, mix, shade, iso_block, up, FRAMES, TAU, ORE  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import K, STEEL, casing, seam, well  # noqa: E402
import phase_5a_preview as p5  # noqa: E402  (antimatter tier colours + bottom)
import gen_tier3_textures as t3  # noqa: E402
import gen_tier4_textures as t4  # noqa: E402
import gen_granulated_textures as gg  # noqa: E402

A = p5.A
WATER = t3.WATER
OXYGEN = t3.OXYGEN
FROTH = [(150, 160, 170), (196, 204, 212), (230, 236, 240), (252, 252, 255)]


# ================================================================ Froth Flotation Cell
def act_flotation(t, f, lit):
    """Top half: a tank of water. Oxygen bubbles rise from a sparger along the bottom, carrying ore grains up into a
    grey froth layer at the top, which a paddle skims off to the right (where the concentrate leaves)."""
    ff = f if lit else 0
    for y in range(3, 7):  # water
        for x in range(2, 14):
            t.px(x, y, mix(WATER[0], WATER[1], (y - 3) / 4 * 0.6 + 0.2))
    for x in range(2, 14):  # froth on top, rippling when running
        k = 0.5 + 0.5 * math.sin((x + ff) * 1.3)
        t.px(x, 2, mix(FROTH[1], FROTH[3], k) if lit else FROTH[0])
    t.hline(2, 13, 6, STEEL[1])  # the sparger pipe
    for x in range(3, 13, 3):
        t.px(x, 6, OXYGEN[2] if lit else OXYGEN[0])
    if lit:
        for k in range(4):  # bubbles rising, some with an ore grain
            x = 3 + k * 3
            y = 5 - (ff + k * 2) % 3
            t.px(x, y, OXYGEN[3])
            if k % 2 == 0 and y > 3:
                t.px(x + 1, y, ORE[(k + ff) % 3])
        for k in range(2):  # grains riding the froth
            t.px(4 + (ff * 2 + k * 5) % 9, 2, ORE[k])
    px = 12 - (ff % 4)  # skimmer paddle
    t.vline(px, 1 if lit else 2, 2, STEEL[3])
    t.px(13, 3, ORE[1] if lit else K['d'])  # concentrate out


def flotation_side():
    """Two inlets above the seam: water (blue ring) and oxygen (pale ring); a grille below."""
    t = casing()
    well(t, 2, 2, 13, 6, K['d'])
    t3.port(t, 5, 4, WATER[2], (14, 16, 20))
    t3.port(t, 10, 4, OXYGEN[3], (14, 16, 20))
    seam(t, [(x, 8) for x in range(1, 15)], 'antimatter', False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


def front5(action):
    return lambda f=None: dcs.front(action, p5.tier_antimatter, 'antimatter', f)


flotation_front = front5(act_flotation)


# ================================================================ Ore Concentrate
def concentrate(metal):
    """A low heap of fine powder in the metal's tones (finer and flatter than granulated ore), flecked with the
    white froth it was skimmed from."""
    tones = gg.ramp(metal)
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    cx, base = 7.5, 13.0
    for y in range(16):
        for x in range(16):
            px, py = x + 0.5, y + 0.5
            h = 6.2 * (1 - ((px - cx) / 7.0) ** 2)  # a wide, low dome
            if h <= 0 or py < base - h or py > base:
                continue
            depth = (py - (base - h)) / max(1.0, h)
            edge = py < base - h + 1.0 or abs(px - cx) > 6.2 or py > base - 0.6
            n = ((x * 7 + y * 13) % 5)  # fixed speckle, so it reads as fine grains
            if edge:
                tone = 0 if py > base - 0.6 or abs(px - cx) > 6.2 else 3
            else:
                tone = 2 + (1 if n == 0 else 0) - (1 if depth > 0.6 else 0) - (1 if n == 3 else 0)
                tone = max(0, min(4, tone + (1 if px < cx - 1 and depth < 0.4 else 0)))
            img.putpixel((x, y), tones[tone] + (255,))
    for (x, y) in ((5, 9), (9, 8), (11, 11), (6, 12), (8, 10)):  # froth flecks
        if img.getpixel((x, y))[3]:
            img.putpixel((x, y), FROTH[3] + (255,))
    img.putpixel((9, 7), FROTH[2] + (255,)) if img.getpixel((9, 7))[3] else None
    return img


# ================================================================ the suite
def suite_side(port_ring=None):
    if port_ring is None:
        return dcs.side('antimatter')
    t = casing()
    well(t, 4, 2, 11, 6, K['d'])
    t3.port(t, 7, 4, port_ring, (14, 16, 20))
    t.px(8, 4, (14, 16, 20))
    seam(t, [(x, 8) for x in range(1, 15)], 'antimatter', False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


SUITE = [
    ('Antimatter Granulator', front5(dcs.act_conveyor), lambda: suite_side()),
    ('Antimatter Smelter', front5(dcs.act_conveyor_smelt), lambda: suite_side()),
    ('Antimatter Washer', front5(t3.act_washer), lambda: suite_side(WATER[2])),
    ('Antimatter Purifier', front5(t4.act_purifier), lambda: suite_side(OXYGEN[1])),
]


# ================================================================ sheet + gif
def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1320, 1000), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)
    top = dcs.top('antimatter').img

    dr.text((20, 14), 'Froth Flotation Cell (raw ore + water + oxygen -> 4 Ore Concentrate)', font=font, fill=head)
    sheet.alpha_composite(iso_block(top, flotation_front().img, flotation_side().img, 190), (20, 50))
    sheet.alpha_composite(iso_block(top, flotation_front(3).img, flotation_side().img, 190), (230, 50))
    dr.text((70, 262), 'idle', font=small, fill=label)
    dr.text((280, 262), 'running', font=small, fill=label)
    for k, (name, im) in enumerate([('Front', flotation_front().img), ('Front (running)', flotation_front(3).img),
                                    ('Side (water + oxygen in)', flotation_side().img), ('Top', top)]):
        x = 460 + k * 200
        sheet.alpha_composite(up(im, 8), (x, 50))
        dr.text((x, 186), name, font=small, fill=label)

    y1 = 300
    dr.text((20, y1), 'Ore Concentrate (one per metal; other mods\' metals only when present)', font=font, fill=head)
    for k, metal in enumerate(gg.ALL_METALS):
        x = 20 + k * 116
        sheet.alpha_composite(up(concentrate(metal), 6), (x, y1 + 36))
        dr.text((x, y1 + 136), metal, font=small, fill=label)
    dr.text((20, y1 + 166), 'the chain for iron:', font=small, fill=label)
    chain = [concentrate('iron'), gg.washed('iron'), gg.purified('iron'),
             Image.open(os.path.join(gg.ITEM, 'granulated_iron.png')).convert('RGBA')]
    names = ['1 raw -> 4 concentrate', 'washed', 'purified', 'granulated']
    for k, im in enumerate(chain):
        x = 200 + k * 130
        if im is not None:
            sheet.alpha_composite(up(im, 4), (x, y1 + 186))
        dr.text((x - 4, y1 + 256), names[k], font=small, fill=label)
        if k:
            dr.text((x - 40, y1 + 208), '->', font=font, fill=head)

    y2 = 600
    dr.text((20, y2), 'Antimatter machine suite (FE only, fastest; washer takes water, purifier oxygen)', font=font, fill=head)
    for k, (name, front_fn, side_fn) in enumerate(SUITE):
        x = 20 + k * 320
        sheet.alpha_composite(iso_block(top, front_fn().img, side_fn().img, 150), (x, y2 + 40))
        sheet.alpha_composite(iso_block(top, front_fn(3).img, side_fn().img, 150), (x + 150, y2 + 40))
        dr.text((x + 10, y2 + 210), name, font=small, fill=label)
        sheet.alpha_composite(up(front_fn(3).img, 5), (x + 20, y2 + 240))
        sheet.alpha_composite(up(side_fn().img, 5), (x + 120, y2 + 240))
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (1160, 230), BG)
        g.alpha_composite(iso_block(top, flotation_front(k).img, flotation_side().img, 190), (10, 20))
        for i, (_, front_fn, side_fn) in enumerate(SUITE):
            g.alpha_composite(iso_block(top, front_fn(k).img, side_fn().img, 190), (230 + i * 230, 20))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
