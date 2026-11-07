"""Mock-up: a new JEI page for the multiblocks (owner's idea, 2026-11-07) - a 3D model of the structure, a button to
step UP through the layers (after the top layer it shows the whole thing again) and one to step DOWN, and under it a
list of the blocks needed. Shown with the Fusion Reactor (3x3x3).

Run: tools/.venv/Scripts/python design/previews/jei_multiblock_preview.py design/previews/jei_multiblock.png
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.join(HERE, '..', '..', 'tools')
sys.path.insert(0, TOOLS)
from mekstyle_lib import iso_block  # noqa: E402

TEX = os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures', 'block')
S = 3  # everything drawn 3x, like a big GUI scale


def tex(name):
    return Image.open(os.path.join(TEX, name + '.png')).convert('RGBA').crop((0, 0, 16, 16))


CASING, GLASS, PORT = tex('reactor_casing'), tex('reactor_glass'), tex('reactor_port')
CTRL_FRONT, CTRL_SIDE = tex('reactor_controller_front'), tex('reactor_controller_side')

# the example reactor: (x, y, z) -> (name, top, front, side); z = 2 is the front
BLOCKS = {}
for y in range(3):
    for z in range(3):
        for x in range(3):
            centred = (x == 1) + (y == 1) + (z == 1)
            if centred == 3:
                continue  # the core
            if (x, y, z) == (1, 1, 2):
                BLOCKS[(x, y, z)] = ('Fusion Reactor Controller', CTRL_SIDE, CTRL_FRONT, CTRL_SIDE)
            elif (x, y, z) == (1, 0, 1):
                BLOCKS[(x, y, z)] = ('Reactor Port', PORT, PORT, PORT)
            elif centred == 2:
                BLOCKS[(x, y, z)] = ('Reactor Glass', GLASS, GLASS, GLASS)
            else:
                BLOCKS[(x, y, z)] = ('Reactor Casing', CASING, CASING, CASING)

BG = (198, 198, 198, 255)
DARK, LIGHT = (85, 85, 85, 255), (255, 255, 255, 255)
TEXT = (64, 64, 64, 255)


def model(layer, size, W, H):
    """The structure as an iso model; layer None = all, else only that layer (others not drawn)."""
    unit = size / 2.05
    c30 = math.cos(math.radians(30))
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ox, oy = W / 2, H / 2 - 1.45 * unit
    for (gx, gy, gz) in sorted(BLOCKS, key=lambda p: (p[0] + p[2] + p[1], p[1])):
        if layer is not None and gy != layer:
            continue
        _, top, front, side = BLOCKS[(gx, gy, gz)]
        cube = iso_block(top, front, side, size)
        dx = (gx - gz) * c30 * unit
        dy = ((gx + gz) * 0.5 - gy) * unit
        img.alpha_composite(cube, (int(ox - size / 2 + dx), int(oy + dy)))
    return img


def bevel(d, box, pressed=False):
    x0, y0, x1, y1 = box
    d.rectangle(box, fill=(139, 139, 139, 255))
    d.line([(x0, y0), (x1, y0)], fill=DARK if pressed else LIGHT, width=S)
    d.line([(x0, y0), (x0, y1)], fill=DARK if pressed else LIGHT, width=S)
    d.line([(x0, y1), (x1, y1)], fill=LIGHT if pressed else DARK, width=S)
    d.line([(x1, y0), (x1, y1)], fill=LIGHT if pressed else DARK, width=S)


def page(layer, title):
    W, H = 170 * S, 200 * S
    img = Image.new('RGBA', (W, H), BG)
    d = ImageDraw.Draw(img)
    font = ImageFont.load_default(size=8 * S)
    small = ImageFont.load_default(size=7 * S)
    # model area
    d.rectangle((2 * S, 2 * S, 142 * S, 108 * S), fill=(178, 178, 178, 255))
    img.alpha_composite(model(layer, 34 * S, 140 * S, 106 * S), (2 * S, 2 * S))
    # layer buttons on the right
    bevel(d, (147 * S, 6 * S, 166 * S, 25 * S))
    d.polygon([(151 * S, 21 * S), (162 * S, 21 * S), (156.5 * S, 10 * S)], fill=(40, 40, 40, 255))
    bevel(d, (147 * S, 85 * S, 166 * S, 104 * S))
    d.polygon([(151 * S, 89 * S), (162 * S, 89 * S), (156.5 * S, 100 * S)], fill=(40, 40, 40, 255))
    label = 'All' if layer is None else f'{layer + 1}/3'
    d.text((156.5 * S, 52 * S), 'Layer', font=small, fill=TEXT, anchor='mm')
    d.text((156.5 * S, 62 * S), label, font=font, fill=TEXT, anchor='mm')
    # blocks needed (for the layer shown, or the whole thing)
    counts = {}
    for (gx, gy, gz), (name, top, front, side) in BLOCKS.items():
        if layer is None or gy == layer:
            counts.setdefault(name, [0, top, front, side])[0] += 1
    d.text((4 * S, 113 * S), 'Blocks needed' + ('' if layer is None else f' (layer {layer + 1})'), font=font, fill=TEXT)
    y = 124 * S
    for name, (n, top, front, side) in sorted(counts.items(), key=lambda kv: -kv[1][0]):
        d.rectangle((4 * S, y, 21 * S, y + 17 * S), fill=(139, 139, 139, 255))
        img.alpha_composite(iso_block(top, front, side, 16 * S), (int(4.5 * S), int(y + 0.5 * S)))
        d.text((26 * S, y + 9 * S), f'{n} x {name}', font=font, fill=TEXT, anchor='lm')
        y += 18 * S
    return img, title


def main(out):
    pages = [page(None, 'Opens on the whole structure'), page(1, 'Up / down: one layer at a time (here: the middle)'),
             page(2, 'Layer 3 (the top); one more up shows it all again')]
    W = sum(p.width for p, _ in pages) + 40 * (len(pages) + 1)
    sheet = Image.new('RGBA', (W, 200 * S + 90), (38, 40, 48, 255))
    d = ImageDraw.Draw(sheet)
    head = ImageFont.load_default(size=24)
    d.text((40, 16), 'JEI multiblock page (mock-up): 3D model, layer buttons, blocks needed', font=head, fill=(235, 235, 240))
    x = 40
    for img, title in pages:
        sheet.alpha_composite(img, (x, 60))
        d.text((x, 60 + img.height + 8), title, font=ImageFont.load_default(size=16), fill=(170, 170, 180))
        x += img.width + 40
    sheet.save(out)
    print('wrote', out)


if __name__ == '__main__':
    main(sys.argv[1])
