"""Washed ore items: clean, rounded ore chunks with water droplets, in each metal's own colours (the 5-tone ramp of
its granulated texture, so a washed ore and its granulated form match). Once approved this moves into
tools/gen_granulated_textures.py.

Usage: tools/.venv/Scripts/python.exe design/previews/washed_ore_preview.py <out.png>
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

ITEM = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'src', 'main', 'resources',
                    'assets', 'comminutioncraft', 'textures', 'item')
METALS = ['iron', 'copper', 'gold', 'tin', 'lead', 'silver', 'nickel', 'zinc', 'aluminum', 'osmium', 'uranium']
DROP = [(70, 120, 190), (130, 180, 235), (210, 236, 255)]  # water: shadow, body, glint

# Chunks (centre x, centre y, radius x, radius y), back to front.
CHUNKS = [(11.2, 10.4, 3.0, 2.6), (4.6, 11.0, 2.9, 2.5), (7.6, 8.6, 4.3, 3.8)]


def luminance(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def ramp(metal):
    """The 5 colours of the metal's granulated texture, dark to light."""
    img = Image.open(os.path.join(ITEM, f'granulated_{metal}.png')).convert('RGBA')
    colours = sorted({img.getpixel((x, y))[:3] for x in range(16) for y in range(16) if img.getpixel((x, y))[3] > 0},
                     key=luminance)
    if len(colours) > 5:  # hand-drawn ones may have more: pick 5 spread across the range
        colours = [colours[round(i * (len(colours) - 1) / 4)] for i in range(5)]
    return colours


def washed(metal):
    tones = ramp(metal)
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            px, py = x + 0.5, y + 0.5
            hit = None
            for (cx, cy, rx, ry) in CHUNKS:  # last chunk that covers the pixel is in front
                d = math.hypot((px - cx) / rx, (py - cy) / ry)
                if d <= 1.0:
                    hit = (cx, cy, rx, ry, d)
            if hit is None:
                continue
            cx, cy, rx, ry, d = hit
            nx, ny = (px - cx) / rx, (py - cy) / ry
            light = 0.55 - 0.5 * (nx + ny) / math.sqrt(2)  # lit from the top left
            if d > 0.82:
                tone = 0
            elif light > 0.92:
                tone = 4
            elif light > 0.68:
                tone = 3
            elif light > 0.42:
                tone = 2
            else:
                tone = 1
            img.putpixel((x, y), tones[tone] + (255,))
    # water: droplets beading on the chunks, and a wet line underneath
    for (x, y) in ((9, 7), (12, 9), (4, 10)):
        img.putpixel((x, y), DROP[1] + (255,))
        img.putpixel((x, y + 1), DROP[0] + (255,))
    img.putpixel((9, 6), DROP[2] + (255,))
    for x in (3, 4, 6, 10, 12):
        if img.getpixel((x, 13))[3] > 0:
            img.putpixel((x, 13), DROP[1] + (255,))
    return img


def main(out_png):
    font = ImageFont.load_default(size=15)
    head = ImageFont.load_default(size=22)
    W = 20 + len(METALS) * 110
    sheet = Image.new('RGBA', (W, 470), (38, 40, 48, 255))
    dr = ImageDraw.Draw(sheet)
    dr.text((20, 14), 'Washed ore (Ore Washer: 1 raw ore -> 3 washed) next to granulated (Electric/Plasma Granulator: 1 -> 1)',
            font=head, fill=(235, 235, 240))
    for i, metal in enumerate(METALS):
        x = 20 + i * 110
        w = washed(metal)
        g = Image.open(os.path.join(ITEM, f'granulated_{metal}.png')).convert('RGBA')
        sheet.alpha_composite(w.resize((96, 96), Image.NEAREST), (x, 60))
        sheet.alpha_composite(g.resize((96, 96), Image.NEAREST), (x, 190))
        dr.text((x, 160), f'washed {metal}', font=font, fill=(170, 170, 180))
        dr.text((x, 290), f'granulated', font=font, fill=(120, 120, 130))
        # in an inventory slot, at real size x3
        dr.rectangle((x + 20, 330, x + 73, 383), fill=(139, 139, 139), outline=(55, 55, 55))
        sheet.alpha_composite(w.resize((48, 48), Image.NEAREST), (x + 23, 333))
    dr.text((20, 400), 'Iron, copper and gold use the ramps of their hand-made granulated textures; the other metals', font=font,
            fill=(170, 170, 180))
    dr.text((20, 420), 'use their generated ones, so each washed ore matches its granulated form. Water droplets stay blue.',
            font=font, fill=(170, 170, 180))
    sheet.save(out_png)
    print('wrote', out_png)


if __name__ == '__main__':
    main(sys.argv[1])
