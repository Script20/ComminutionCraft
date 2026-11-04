"""Granulated ore textures for other mods' metals, recoloured from the hand-made granulated_iron.png, and the
Washed ore textures for every metal.

Usage: python tools/gen_granulated_textures.py [preview.png]

Each colour of the iron pile is mapped, by brightness, onto the same step of a metal's own ramp, so every metal
keeps the iron pile's shape and shading. Iron, copper and gold are hand-drawn and are never overwritten.
Washed ore (approved preview: design/previews/washed_ore.png) is drawn: clean rounded chunks shaded with the 5 tones
of the metal's granulated texture, plus blue water droplets, so a washed ore matches its granulated form.
"""
import math
import os
import sys

from PIL import Image

ITEM = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources',
                    'assets', 'comminutioncraft', 'textures', 'item')

# Mid-tone of each metal; the ramp is built around it. Keys must match ProcessedMetal names.
METALS = {
    'tin': (158, 170, 186),
    'lead': (104, 110, 140),
    'silver': (222, 230, 242),
    'nickel': (204, 198, 160),
    'zinc': (168, 186, 192),
    'aluminum': (206, 196, 190),
    'osmium': (132, 156, 196),
    'uranium': (112, 172, 84),
}


def luminance(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


ALL_METALS = ['iron', 'copper', 'gold'] + list(METALS)
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




def recolour(template, mid):
    colours = sorted({p[:3] for p in template.getdata() if p[3] > 0}, key=luminance)
    reference = luminance(colours[len(colours) // 2])
    out = Image.new('RGBA', template.size)
    mapping = {}
    for c in colours:
        k = luminance(c) / reference
        mapping[c] = tuple(max(0, min(255, round(v * k))) for v in mid)
    for y in range(template.height):
        for x in range(template.width):
            p = template.getpixel((x, y))
            out.putpixel((x, y), mapping[p[:3]] + (p[3],) if p[3] > 0 else (0, 0, 0, 0))
    return out


if __name__ == '__main__':
    iron = Image.open(os.path.join(ITEM, 'granulated_iron.png')).convert('RGBA')
    made = {}
    for name, mid in METALS.items():
        made[name] = recolour(iron, mid)
        made[name].save(os.path.join(ITEM, f'granulated_{name}.png'))
    for name in ALL_METALS:  # after the granulated ones: washed ore takes their colours
        washed(name).save(os.path.join(ITEM, f'washed_{name}.png'))
    if len(sys.argv) > 1:
        sheet = Image.new('RGBA', (8 + len(made) * 72, 80), (60, 62, 70, 255))
        for i, img in enumerate(made.values()):
            sheet.alpha_composite(img.resize((64, 64), Image.NEAREST), (8 + i * 72, 8))
        sheet.save(sys.argv[1])
    print('ok')
