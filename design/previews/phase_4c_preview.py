"""Tier 4 phase C preview: the Ore Purifier (Washed ore + oxygen -> Purified ore, sometimes a by-product) and the
Purified ore items for every metal, next to their washed and granulated forms.

Usage: tools/.venv/Scripts/python.exe design/previews/phase_4c_preview.py <out.png> <out.gif>
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

TOOLS = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'tools')
sys.path.insert(0, TOOLS)
from mekstyle_lib import Tex, mix, shade, iso_block, up, FRAMES, TAU, ORE  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import K, GLASS, STEEL, casing, seam, well, front, side, top  # noqa: E402
import gen_tier3_textures as t3  # noqa: E402
import gen_tier4_textures as t4  # noqa: E402
import gen_granulated_textures as gg  # noqa: E402

F = dcs.TIERS['fusion']
OXYGEN = t3.OXYGEN
ITEM = gg.ITEM
METALS = gg.ALL_METALS


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
    return front(act_purifier, t4.tier_fusion, 'fusion', f)


# ================================================================ Purified ore
# One faceted nugget: (x, y) -> facet number; facets get lighter towards the top left.
def purified(metal):
    """A clean, faceted nugget in the metal's colours, with a white sparkle (purer = shinier than washed ore)."""
    tones = gg.ramp(metal)
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    cx, cy = 7.5, 8.6
    for y in range(16):
        for x in range(16):
            px, py = x + 0.5 - cx, y + 0.5 - cy
            # an octagon-ish gem, wider than tall
            if abs(px) / 5.6 + abs(py) / 4.6 > 1.0 or abs(px) > 5.2 or abs(py) > 4.4:
                continue
            edge = abs(px) / 5.6 + abs(py) / 4.6 > 0.84 or abs(px) > 4.6 or abs(py) > 3.9
            if edge:
                tone = 0
            elif py < -1.2:  # top facets
                tone = 4 if px < 0.5 else 3
            elif px < -1.5:
                tone = 3
            elif px > 1.5:
                tone = 1
            else:
                tone = 2
            img.putpixel((x, y), tones[tone] + (255,))
    for (x, y) in ((5, 6), (6, 5), (5, 4), (4, 5), (5, 5)):  # sparkle
        img.putpixel((x, y), (255, 255, 255, 255))
    img.putpixel((10, 10), tones[4] + (255,))
    return img


# ================================================================ sheet
BYPRODUCTS = {'iron': 'nickel', 'copper': 'gold', 'gold': 'silver', 'lead': 'silver', 'tin': 'zinc', 'zinc': 'lead',
              'nickel': 'osmium', 'silver': 'lead', 'aluminum': 'iron', 'osmium': 'nickel', 'uranium': 'lead'}


def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    tiny = ImageFont.load_default(size=13)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1320, 840), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)

    dr.text((20, 14), 'Ore Purifier: Washed ore + oxygen + FE -> Purified ore, sometimes a by-product (off / running)', font=font, fill=head)
    tp, sd = t4.machine_top().img, purifier_side().img
    off, on = purifier_front().img, purifier_front(3).img
    sheet.alpha_composite(iso_block(tp, off, sd, 190), (10, 50))
    sheet.alpha_composite(iso_block(tp, on, sd, 190), (210, 50))
    for k, im in enumerate((off, on, sd, tp)):
        sheet.alpha_composite(up(im, 5), (440 + k * 95, 70))
    dr.text((440, 160), 'front         on           side          top', font=small, fill=label)
    for k in range(FRAMES):
        sheet.alpha_composite(up(purifier_front(k).img, 3), (440 + k * 54, 190))
    dr.text((440, 245), 'animation frames', font=small, fill=label)

    y0 = 300
    dr.text((20, y0), 'Purified ore (new), with each metal\'s washed and granulated forms, and its by-product', font=font, fill=head)
    for i, metal in enumerate(METALS):
        x = 20 + i * 117
        pur = purified(metal)
        sheet.alpha_composite(pur.resize((96, 96), Image.NEAREST), (x, y0 + 40))
        dr.text((x, y0 + 140), f'purified {metal}', font=tiny, fill=(200, 200, 210))
        for k, kind in enumerate(('washed', 'granulated')):
            im = Image.open(os.path.join(ITEM, f'{kind}_{metal}.png')).convert('RGBA')
            sheet.alpha_composite(im.resize((48, 48), Image.NEAREST), (x + k * 50, y0 + 165))
        dr.text((x, y0 + 218), f'-> + {BYPRODUCTS[metal]}', font=tiny, fill=label)
        dr.rectangle((x + 20, y0 + 245, x + 73, y0 + 298), fill=(139, 139, 139), outline=(55, 55, 55))
        sheet.alpha_composite(pur.resize((48, 48), Image.NEAREST), (x + 23, y0 + 248))
    dr.text((20, y0 + 320), 'Chain: Ore Washer (1 raw -> 3 washed) -> Ore Purifier (1 washed -> 1 purified, ~20% chance of 1 granulated', font=small, fill=label)
    dr.text((20, y0 + 340), 'by-product metal) -> Electric/Plasma Granulator (1 purified -> 1 granulated) -> smelter. A by-product metal that', font=small, fill=label)
    dr.text((20, y0 + 360), 'isn\'t in the pack is skipped (or a vanilla nugget instead, e.g. iron -> nickel falls back to a gold nugget).', font=small, fill=label)
    dr.text((20, y0 + 390), 'GUI: the electric machine layout with an oxygen gauge and a second output slot for the by-product.', font=small, fill=label)
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (300, 300), BG)
        g.alpha_composite(iso_block(tp, purifier_front(k).img, sd, 280), (10, 10))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
