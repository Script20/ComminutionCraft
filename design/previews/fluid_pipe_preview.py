"""Fluid Pipe restyle: the current pipe next to three new texture options, drawn on the real conduit model
(core 6x6x6, arms 4x4x5, connector plate 8x8x1 — see ModModelProvider) in an isometric scene, plus each 16x16 sheet.

Run: tools/.venv/Scripts/python design/previews/fluid_pipe_preview.py  -> design/previews/fluid_pipe.png
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
TOOLS = os.path.join(ROOT, 'tools')
sys.path.insert(0, TOOLS)
from mekstyle_lib import mix, shade, up  # noqa: E402
from dark_chassis_system import K, STEEL, COPPER  # noqa: E402

BLOCK = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures', 'block')
WATER = [(24, 60, 112), (40, 96, 168), (70, 140, 210), (150, 200, 240)]


def new_sheet():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))

    def px(x, y, c):
        img.putpixel((x, y), tuple(int(v) for v in c[:3]) + (255,))
    return img, px


def junction(px, body, rim_hi, rim_lo, bolt, centre=None):
    """Core 6x6 at (0,0): bevelled box, four bolts, optional centre detail (2x2)."""
    for y in range(6):
        for x in range(6):
            c = mix(body[0], body[1], y / 5)
            if x == 0 or y == 0:
                c = rim_hi
            if x == 5 or y == 5:
                c = rim_lo
            px(x, y, c)
    for (x, y) in ((1, 1), (4, 1), (1, 4), (4, 4)):
        px(x, y, bolt)
    if centre:
        for i, (x, y) in enumerate(((2, 2), (3, 2), (2, 3), (3, 3))):
            px(x, y, centre[i])


def flange(px, body, rim_hi, rim_lo, bolt, port, glint):
    """Connector plate 8x8 at (8,0): flange, four bolts, a dark port with a glint."""
    for y in range(8):
        for x in range(8):
            c = mix(body[0], body[1], y / 7)
            if x == 0 or y == 0:
                c = rim_hi
            if x == 7 or y == 7:
                c = rim_lo
            px(8 + x, y, c)
    for (x, y) in ((1, 1), (6, 1), (1, 6), (6, 6)):
        px(8 + x, y, bolt)
    for y in range(2, 6):
        for x in range(2, 6):
            ring = x in (2, 5) or y in (2, 5)
            px(8 + x, y, rim_lo if ring else port)
    px(8 + 3, 3, glint)


def tube(px, rows, collar_rows, glints=()):
    """Tube strip (16 x 4 at y 8): `rows` across the tube top to bottom; collars on every 4th texel so a 5-long arm
    reads collar-tube-tube-tube-collar either way round (two collars meet at each block border = a flange ring)."""
    for x in range(16):
        for y in range(4):
            c = collar_rows[y] if x % 4 == 0 else rows[y]
            if (x % 4, y) in glints:
                c = glints[(x % 4, y)]
            px(x, 8 + y, c)


# ---------------------------------------------------------------- options
def current():
    return Image.open(os.path.join(BLOCK, 'fluid_pipe.png')).convert('RGBA')


def option_a():
    """Steel plumbing: round brushed-steel tube, dark collars, steel junction and bolted flange."""
    img, px = new_sheet()
    junction(px, (STEEL[3], STEEL[1]), STEEL[4], STEEL[0], (60, 63, 70), centre=[STEEL[2], STEEL[1], STEEL[1], STEEL[0]])
    flange(px, (STEEL[3], STEEL[1]), STEEL[4], STEEL[0], (60, 63, 70), (18, 20, 24), WATER[2])
    tube(px, [(206, 210, 218), STEEL[3], STEEL[1], (78, 82, 90)],
         [STEEL[2], (84, 88, 96), (70, 74, 82), (46, 49, 56)])
    return img


def option_b():
    """Dark Chassis with a window: charcoal tube with a glass strip showing a cool blue glow, steel collars."""
    img, px = new_sheet()
    junction(px, (K['l'], K['m']), K['hi'], K['o'], STEEL[2], centre=[WATER[3], WATER[2], WATER[2], WATER[1]])
    flange(px, (K['l'], K['d']), K['hi'], K['o'], STEEL[2], (14, 18, 26), WATER[3])
    tube(px, [K['hi'], WATER[2], WATER[1], K['o']],
         [STEEL[4], STEEL[2], STEEL[1], STEEL[0]], glints={(2, 1): WATER[3]})
    return img


def option_b2():
    """B with the light steel collars and bolts swapped for the casing's own greys (owner, 2026-11-05)."""
    img, px = new_sheet()
    bolt = (96, 101, 112)
    junction(px, (K['l'], K['m']), K['hi'], K['o'], bolt, centre=[WATER[3], WATER[2], WATER[2], WATER[1]])
    flange(px, (K['l'], K['d']), K['hi'], K['o'], bolt, (14, 18, 26), WATER[3])
    tube(px, [K['l'], WATER[2], WATER[1], K['o']],
         [K['hi'], K['l'], K['m'], K['d']], glints={(2, 1): WATER[3]})
    return img


def option_c():
    """Copper plumbing: warm copper tube with steel collars, steel junction and flange."""
    img, px = new_sheet()
    junction(px, (STEEL[3], STEEL[1]), STEEL[4], STEEL[0], COPPER[1], centre=[COPPER[3], COPPER[2], COPPER[2], COPPER[1]])
    flange(px, (STEEL[3], STEEL[1]), STEEL[4], STEEL[0], COPPER[1], (18, 20, 24), WATER[2])
    tube(px, [(246, 192, 150), COPPER[2], COPPER[1], COPPER[0]],
         [STEEL[3], STEEL[2], STEEL[1], STEEL[0]], glints={(2, 0): (255, 226, 196)})
    return img


def option_d():
    """A on a chunkier pipe: 8-wide steel junction, 6-wide tube (needs its own pipe model)."""
    img, px = new_sheet()
    for y in range(8):
        for x in range(8):
            c = mix(STEEL[3], STEEL[1], y / 7)
            if x == 0 or y == 0:
                c = STEEL[4]
            if x == 7 or y == 7:
                c = STEEL[0]
            px(x, y, c)
    for (x, y) in ((1, 1), (6, 1), (1, 6), (6, 6)):
        px(x, y, (60, 63, 70))
    for (x, y) in ((3, 3), (4, 3), (3, 4), (4, 4)):
        px(x, y, (STEEL[2] if (x, y) == (3, 3) else STEEL[1]))
    flange(px, (STEEL[3], STEEL[1]), STEEL[4], STEEL[0], (60, 63, 70), (18, 20, 24), WATER[2])
    rows = [(214, 218, 226), (184, 188, 196), STEEL[3], STEEL[2], STEEL[1], (78, 82, 90)]
    collar = [STEEL[2], (100, 104, 112), (84, 88, 96), (76, 80, 88), (64, 68, 76), (46, 49, 56)]
    for x in range(16):
        for y in range(6):
            px(x, 8 + y, collar[y] if x % 4 == 0 else rows[y])
    return img


# ---------------------------------------------------------------- tiny box renderer (viewer at +x, +y, +z)
C30 = math.cos(math.radians(30))


def crop(sheet, u0, v0, u1, v1):
    return sheet.crop((u0, v0, u1, v1))


def conduit_boxes(sheet, origin, arms, plates, thick=False):
    """The model's elements for one conduit block at `origin` (block coords) as boxes in pixel coords.
    thick: core 8x8x8 (4..12), arms 6x6 (5..11) — sheet: core 8x8 at (0,0), tube strip 16x6 at y 8, end = core (1..7)."""
    ox, oy, oz = (o * 16 for o in origin)
    if thick:
        core, strip, end = crop(sheet, 0, 0, 8, 8), crop(sheet, 0, 8, 4, 14), crop(sheet, 1, 1, 7, 7)
        c0, c1, a0, a1 = 4, 12, 5, 11
    else:
        core, strip, end = crop(sheet, 0, 0, 6, 6), crop(sheet, 0, 8, 5, 12), crop(sheet, 1, 1, 5, 5)
        c0, c1, a0, a1 = 5, 11, 6, 10
    big, edge_v, edge_h = crop(sheet, 8, 0, 16, 8), crop(sheet, 8, 0, 9, 8), crop(sheet, 8, 0, 16, 1)
    boxes = [((c0, c0, c0), (c1, c1, c1), {'x': core, 'y': core, 'z': core})]
    span = {'n': ((a0, a0, 0), (a1, a1, c0), 'z'), 's': ((a0, a0, c1), (a1, a1, 16), 'z'),
            'w': ((0, a0, a0), (c0, a1, a1), 'x'), 'e': ((c1, a0, a0), (16, a1, a1), 'x'),
            'd': ((a0, 0, a0), (a1, c0, a1), 'y'), 'u': ((a0, c1, a0), (a1, 16, a1), 'y')}
    for a in arms:
        lo, hi, axis = span[a]
        boxes.append((lo, hi, {k: (end if k == axis else strip) for k in 'xyz'}))
    p0, p1 = (3, 13) if thick else (4, 12)
    pspan = {'w': ((0, p0, p0), (1, p1, p1), 'x'), 'e': ((15, p0, p0), (16, p1, p1), 'x'),
             'n': ((p0, p0, 0), (p1, p1, 1), 'z'), 's': ((p0, p0, 15), (p1, p1, 16), 'z'),
             'd': ((p0, 0, p0), (p1, 1, p1), 'y'), 'u': ((p0, 15, p0), (p1, 16, p1), 'y')}
    for p in plates:
        lo, hi, axis = pspan[p]
        faces = {}
        for k in 'xyz':
            if k == axis:
                faces[k] = big
            else:
                faces[k] = edge_h if (k == 'y' or (k == 'z' and axis == 'y') or (k == 'x' and axis == 'y')) else edge_v
        boxes.append((lo, hi, faces))
    return [((lo[0] + ox, lo[1] + oy, lo[2] + oz), (hi[0] + ox, hi[1] + oy, hi[2] + oz), f) for lo, hi, f in boxes]


def cube_box(origin, top, front, side):
    ox, oy, oz = (o * 16 for o in origin)
    return ((ox, oy, oz), (ox + 16, oy + 16, oz + 16), {'y': top, 'z': front, 'x': side})


def render(boxes, S, W, H, ox, oy):
    """Draws the +x, +y and +z faces of every box, far boxes first. S = pixels per block."""
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    k = S / 16

    def proj(x, y, z):
        return (ox + (x - z) * C30 * k, oy + ((x + z) * 0.5 - y) * k)

    def face(tex, p0, du, dv, light):
        w, h = tex.size
        lu, lv = math.dist((0, 0, 0), du), math.dist((0, 0, 0), dv)
        if (w > h) != (lu > lv) and w != h:
            tex = tex.rotate(90, expand=True)
            w, h = tex.size
        for v in range(h):
            for u in range(w):
                c = tex.getpixel((u, v))
                if c[3] == 0:
                    continue
                col = shade(c, light)
                pts = []
                for a, b in ((u, v), (u + 1, v), (u + 1, v + 1), (u, v + 1)):
                    pts.append(proj(*(p0[i] + du[i] * a / w + dv[i] * b / h for i in range(3))))
                d.polygon(pts, fill=col, outline=col)

    for lo, hi, f in sorted(boxes, key=lambda b: sum(b[0]) + sum(b[1])):
        x0, y0, z0 = lo
        x1, y1, z1 = hi
        face(f['y'], (x0, y1, z0), (x1 - x0, 0, 0), (0, 0, z1 - z0), 1.0)      # top
        face(f['z'], (x0, y1, z1), (x1 - x0, 0, 0), (0, y0 - y1, 0), 0.84)     # south
        face(f['x'], (x1, y1, z1), (0, 0, z0 - z1), (0, y0 - y1, 0), 0.68)     # east
    return img


def scene(sheet, thick=False):
    top = Image.open(os.path.join(BLOCK, 'ore_washer_top.png')).convert('RGBA')
    front = Image.open(os.path.join(BLOCK, 'ore_washer_front.png')).convert('RGBA')
    side = Image.open(os.path.join(BLOCK, 'ore_washer_side.png')).convert('RGBA')
    boxes = [cube_box((0, 0, 0), top, side, front)]
    pipes = {(1, 0, 0): ('we', 'w'), (2, 0, 0): ('wes', ''), (3, 0, 0): ('wu', ''), (3, 1, 0): ('d', ''),
             (2, 0, 1): ('ns', ''), (2, 0, 2): ('n', '')}
    for pos, (arms, plates) in pipes.items():
        boxes += conduit_boxes(sheet, pos, arms, plates, thick)
    return render(boxes, 84, 560, 400, 240, 100)


def item(sheet, thick=False):
    if thick:
        core, run, end, c, a = crop(sheet, 0, 0, 8, 8), crop(sheet, 0, 8, 16, 14), crop(sheet, 1, 1, 7, 7), (4, 12), (5, 11)
    else:
        core, run, end, c, a = crop(sheet, 0, 0, 6, 6), crop(sheet, 0, 8, 16, 12), crop(sheet, 1, 1, 5, 5), (5, 11), (6, 10)
    boxes = [((c[0],) * 3, (c[1],) * 3, {'x': core, 'y': core, 'z': core}),
             ((0, a[0], a[0]), (c[0], a[1], a[1]), {'x': end, 'y': run.crop((0, 0, c[0], run.height)), 'z': run.crop((0, 0, c[0], run.height))}),
             ((c[1], a[0], a[0]), (16, a[1], a[1]), {'x': end, 'y': run.crop((c[1], 0, 16, run.height)), 'z': run.crop((c[1], 0, 16, run.height))})]
    return render(boxes, 150, 240, 210, 115, 105)


def main():
    if '--b2' in sys.argv:
        options = [('Current', current()), ('B  as shown before', option_b()), ('B  grey collars and bolts', option_b2())]
    else:
        options = [('Current', current()), ('A  Steel plumbing', option_a()),
                   ('B  Dark Chassis + water window', option_b()), ('C  Copper plumbing', option_c()),
                   ('D  Steel plumbing, chunkier pipe (new model)', option_d())]
    W, row = 1180, 350
    out = Image.new('RGBA', (W, 60 + row * len(options)), (32, 34, 40, 255))
    d = ImageDraw.Draw(out)
    try:
        font = ImageFont.truetype('arial.ttf', 26)
        small = ImageFont.truetype('arial.ttf', 18)
    except OSError:
        font = small = ImageFont.load_default()
    d.text((20, 16), 'Fluid Pipe restyle — same model, new textures (scene · item · 16x16 sheet)', fill=(230, 230, 235), font=font)
    for i, (name, sheet) in enumerate(options):
        y = 60 + i * row
        d.rectangle((10, y, W - 10, y + row - 12), fill=(44, 47, 54, 255))
        d.text((24, y + 10), name, fill=(240, 240, 245), font=font)
        thick = i == 4 and len(options) == 5
        out.alpha_composite(scene(sheet, thick), (10, y + 20))
        out.alpha_composite(item(sheet, thick), (580, y + 100))
        out.alpha_composite(up(sheet, 14), (836, y + 50))
        d.text((836, y + 280), 'sheet: core · plate / tube strip', fill=(170, 170, 180), font=small)
        if len(options) == 5:
            sheet.save(os.path.join(HERE, f'fluid_pipe_option_{"cur" if i == 0 else "abcd"[i - 1]}.png'))
    if '--b2' in sys.argv:
        option_b2().save(os.path.join(HERE, 'fluid_pipe_option_b2.png'))
    out.save(os.path.join(HERE, 'fluid_pipe_b2.png' if '--b2' in sys.argv else 'fluid_pipe.png'))
    print('ok')


if __name__ == '__main__':
    main()
