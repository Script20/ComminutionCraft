"""Granulated ore textures for other mods' metals, recoloured from the hand-made granulated_iron.png.

Usage: python tools/gen_granulated_textures.py [preview.png]

Each colour of the iron pile is mapped, by brightness, onto the same step of a metal's own ramp, so every metal
keeps the iron pile's shape and shading. Iron, copper and gold are hand-drawn and are never overwritten.
"""
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
    if len(sys.argv) > 1:
        sheet = Image.new('RGBA', (8 + len(made) * 72, 80), (60, 62, 70, 255))
        for i, img in enumerate(made.values()):
            sheet.alpha_composite(img.resize((64, 64), Image.NEAREST), (8 + i * 72, 8))
        sheet.save(sys.argv[1])
    print('ok')
