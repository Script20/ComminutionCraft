"""Tier 5 phase A previews: the Particle Accelerator ring (segment, controller, port), the Antimatter fluid, the
Containment Cell (antimatter tank, no power needed) and the Magnetic Conduit (antimatter-only pipe).

Run: tools/.venv/Scripts/python design/previews/phase_5a_preview.py design/previews/phase_5a.png design/previews/phase_5a_running.gif
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.join(HERE, '..', '..', 'tools')
sys.path.insert(0, TOOLS)
sys.path.insert(0, HERE)
from mekstyle_lib import Tex, mix, shade, iso_block, up, FRAMES, TAU  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import K, STEEL, COPPER, casing, seam, well  # noqa: E402
import fluid_pipe_preview as fp  # noqa: E402

dcs.TIERS['antimatter'] = dict(off=(86, 90, 104), on=(214, 228, 255), hi=(255, 255, 255))
A = dcs.TIERS['antimatter']
VOID = [(6, 8, 18), (22, 28, 58), (70, 92, 160), (170, 196, 245), (246, 250, 255)]

# the approved tier-5 bottom (speck in a magnetic trap) lives in six_tiers.py
_src = open(os.path.join(TOOLS, 'six_tiers.py')).read()
_fn = _src[_src.index('def tier_antimatter'):_src.index('# ---------------------------------------------------------------- 6. SINGULARITY')]
_ns = dict(dcs.__dict__)
_ns['tc'] = lambda tier, k: dcs.TIERS[tier][k]
exec(_fn, _ns)
tier_antimatter = _ns['tier_antimatter']


# ================================================================ Antimatter fluid
def antimatter_still(f):
    """Near-black void with thin icy filaments drifting through it and a few sparks (looks like nothing else)."""
    t = Tex()
    w = TAU / 16
    ph = f * TAU / FRAMES
    for y in range(16):
        for x in range(16):
            a = math.sin(x * w * 2 + ph + math.sin(y * w + ph) * 1.5)
            b = math.sin(y * w * 2 - ph + math.sin(x * w * 2 - ph))
            line = max(0.0, 1 - abs(a - b) * 2.4)  # where the two waves cross: a thin bright filament
            base = mix(VOID[0], VOID[1], 0.5 + 0.5 * math.sin((x + y) * w + ph))
            c = mix(base, VOID[2], line) if line < 0.7 else mix(VOID[2], VOID[3], (line - 0.7) / 0.3)
            t.px(x, y, c)
    for k in range(3):  # sparks that blink on and off
        sx, sy = (3 + 5 * k + f) % 16, (2 + 7 * k + 3 * f // 2) % 16
        if (f + k) % 3 != 2:
            t.px(sx, sy, VOID[4])
    return t


# ================================================================ Accelerator Segment
def segment_side(f=None):
    """Two magnet coil housings (copper windings) with the beam tube between them; running: the beam glows and
    particles streak along it."""
    lit = f is not None
    f = f or 0
    t = casing()
    for y0 in (2, 11):  # coil housings
        well(t, 2, y0, 13, y0 + 2, K['d'])
        for x in range(3, 13):
            c = COPPER[2] if x % 2 else COPPER[1]
            t.px(x, y0 + 1, c)
        t.hline(3, 12, y0 + 1 - 1 + 0, shade(COPPER[1], 0.8)) if False else None
    well(t, 1, 6, 14, 9, (10, 12, 20))  # beam tube window
    for x in range(2, 14):
        if lit:
            glow = mix(A['off'], A['on'], 0.55 + 0.25 * math.sin((x - f * 2) * TAU / 8))
            t.px(x, 7, glow); t.px(x, 8, shade(glow, 0.75))
        else:
            t.px(x, 7, (26, 30, 42)); t.px(x, 8, (20, 22, 32))
    if lit:
        for k in range(2):  # particles streaking along the beam
            px = 2 + (f * 3 + k * 6) % 12
            t.px(px, 7, A['hi']); t.px(max(2, px - 1), 7, A['on'])
    seam(t, [(x, 5) for x in range(1, 15)] + [(x, 10) for x in range(1, 15)], 'antimatter', lit, f)
    return t


def segment_top(lit=False, f=0):
    """Looking down the magnet: a quadrupole, four copper coil lobes round the beam spot, an icy ring seam."""
    t = casing()
    ring = []
    for y in range(1, 15):
        for x in range(1, 15):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 6.6:
                t.px(x, y, K['d'] if d > 2.2 else (14, 16, 26))
            if 5.6 <= d < 6.6:
                ring.append((x, y))
    for (cx, cy) in ((7.5, 3.6), (7.5, 11.4), (3.6, 7.5), (11.4, 7.5)):  # coil lobes
        for y in range(16):
            for x in range(16):
                if abs(x - cx) <= 1.6 and abs(y - cy) <= 1.6:
                    t.px(x, y, COPPER[2] if (x + y) % 2 else COPPER[1])
    seam(t, ring, 'antimatter', lit, f)
    t.px(7, 7, A['hi'] if lit else A['off']); t.px(8, 8, A['on'] if lit else (30, 34, 46))
    t.px(8, 7, A['on'] if lit else (30, 34, 46)); t.px(7, 8, A['on'] if lit else (30, 34, 46))
    return t


# ================================================================ Controller and Port
def act_ring_map(t, f, lit):
    """Top half of the controller front: a map of the ring with the beam running round it, and a speed readout."""
    for x in range(3, 10):
        for y in (2, 6):
            t.px(x, y, A['off'])
    for y in range(2, 7):
        for x in (3, 9):
            t.px(x, y, A['off'])
    loop = [(x, 2) for x in range(3, 10)] + [(9, y) for y in range(3, 7)] + \
           [(x, 6) for x in range(8, 2, -1)] + [(3, y) for y in range(5, 2, -1)]
    if lit:
        for k in range(3):
            x, y = loop[(f * 2 - k) % len(loop)]
            t.px(x, y, A['hi'] if k == 0 else mix(A['off'], A['on'], 0.6 - 0.2 * k))
    for i in range(3):  # speed bars
        h = (1 + (f + i) % 4) if lit else 1
        for k in range(h):
            t.px(11 + i, 6 - k, mix(A['on'], A['hi'], k / 4) if lit else A['off'])


def controller_front(f=None):
    return dcs.front(act_ring_map, tier_antimatter, 'antimatter', f)


def port_side(lit=False, f=0):
    """A segment with a round connector in the middle: FE and hydrogen in, antimatter out."""
    t = segment_side(f if lit else None)
    for y in range(4, 12):
        for x in range(4, 12):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 3.9:
                t.px(x, y, (A['on'] if lit else A['off']) if d > 2.9 else (STEEL[1] if d > 1.9 else (12, 14, 22)))
    for (x, y) in ((7, 4), (8, 11), (4, 8), (11, 7)):
        t.px(x, y, STEEL[4])
    if lit:
        t.px(7, 7, A['hi'])
    return t


# ================================================================ Containment Cell
def cell_side(fill=None, frame=0, preview=True):
    """A heavy frame with copper magnet bands top and bottom round a glass core. In game the window is cutout and a
    renderer draws the antimatter inside (like the Fluid Tank); `fill` paints that here."""
    t = casing()
    for y0 in (1, 12):
        t.rect(1, y0, 14, y0 + 2, COPPER[1])
        for x in range(1, 15):
            t.px(x, y0, COPPER[3] if x % 2 else COPPER[2])
            t.px(x, y0 + 2, COPPER[0])
        t.hline(1, 14, y0 + 1, COPPER[2])
    for y in range(4, 12):
        for x in range(3, 13):
            t.img.putpixel((x, y), (0, 0, 0, 0))
    if fill:
        top = 11 - int(round(fill * 7))
        fl = antimatter_still(frame).img
        for y in range(max(4, top), 12):
            for x in range(3, 13):
                t.px(x, y, fl.getpixel((x, y))[:3])
    for y in range(4, 12):
        for x in range(3, 13):
            base = t.img.getpixel((x, y))
            if base[3] == 0:  # in game: see-through (cutout) onto the cell's dark inner walls
                t.img.putpixel((x, y), (16, 18, 28, 255) if preview else (0, 0, 0, 0))
    t.vline(2, 4, 11, K['o']); t.vline(13, 4, 11, K['hi'])
    seam(t, [(2, 3), (13, 3), (2, 12), (13, 12)], 'antimatter', bool(fill), frame)
    for (x, y) in ((4, 5), (5, 5), (4, 6)):
        b = t.img.getpixel((x, y))
        t.img.putpixel((x, y), (230, 238, 255, 150 if b[3] < 255 else 255))
    return t


def cell_top():
    t = casing()
    ring = [(x, 3) for x in range(3, 13)] + [(x, 12) for x in range(3, 13)] + \
           [(3, y) for y in range(4, 12)] + [(12, y) for y in range(4, 12)]
    seam(t, ring, 'antimatter', False, 0)
    t.rect(4, 4, 11, 11, COPPER[1])
    for y in range(4, 12):
        for x in range(4, 12):
            if (x + y) % 2:
                t.px(x, y, COPPER[2])
    t.rect(6, 6, 9, 9, STEEL[1])
    t.rect(7, 7, 8, 8, (12, 14, 22))
    t.hline(6, 9, 6, STEEL[3]); t.vline(6, 6, 9, STEEL[2])
    return t


# ================================================================ Magnetic Conduit (same UVs as the other conduits)
def conduit_sheet():
    """Dark tube with an icy beam of contained antimatter, copper coil collars (the magnets), grey bolts."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))

    def px(x, y, c):
        img.putpixel((x, y), tuple(int(v) for v in c[:3]) + (255,))

    bolt = (96, 101, 112)
    for y in range(6):
        for x in range(6):
            c = mix(K['l'], K['m'], y / 5)
            if x == 0 or y == 0:
                c = K['hi']
            if x == 5 or y == 5:
                c = K['o']
            px(x, y, c)
    for (x, y) in ((1, 1), (4, 1), (1, 4), (4, 4)):
        px(x, y, bolt)
    for (x, y), c in zip(((2, 2), (3, 2), (2, 3), (3, 3)), (A['hi'], A['on'], A['on'], A['off'])):
        px(x, y, c)
    for y in range(8):
        for x in range(8):
            c = mix(K['l'], K['d'], y / 7)
            if x == 0 or y == 0:
                c = K['hi']
            if x == 7 or y == 7:
                c = K['o']
            px(8 + x, y, c)
    for y in range(1, 7):
        for x in range(1, 7):
            if x in (1, 6) or y in (1, 6):
                px(8 + x, y, COPPER[2] if (x + y) % 2 else COPPER[1])
    for y in range(2, 6):
        for x in range(2, 6):
            px(8 + x, y, K['o'] if x in (2, 5) or y in (2, 5) else (12, 14, 24))
    px(8 + 3, 3, A['hi'])
    rows = [K['l'], A['on'], shade(A['on'], 0.62), K['o']]
    collar = [COPPER[3], COPPER[2], COPPER[1], COPPER[0]]
    for x in range(16):
        for y in range(4):
            c = collar[y] if x % 4 == 0 else rows[y]
            if x % 4 == 2 and y == 1:
                c = A['hi']
            px(x, 8 + y, c)
    return img


# ================================================================ ring view
def iso_scene(blocks, size, W, H, ox, oy):
    unit = size / 2.05
    c30 = math.cos(math.radians(30))
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    for (gx, gy, gz) in sorted(blocks, key=lambda p: (p[0] + p[2] + p[1], p[1])):
        tp, fr, sd = blocks[(gx, gy, gz)]
        cube = iso_block(tp, fr, sd, size)
        dx = (gx - gz) * c30 * unit
        dy = ((gx + gz) * 0.5 - gy) * unit
        img.alpha_composite(cube, (int(ox - size / 2 + dx), int(oy + dy)))
    return img


def ring_blocks(f=None, side=7):
    lit = f is not None
    seg = segment_side(f).img
    top = segment_top(lit, f or 0).img
    blocks = {}
    for i in range(side):
        for j in range(side):
            if i in (0, side - 1) or j in (0, side - 1):
                blocks[(i, 0, j)] = (top, seg, seg)
    mid = side // 2
    blocks[(mid, 0, side - 1)] = (top, controller_front(f).img, seg)   # front edge: controller
    blocks[(side - 1, 0, mid)] = (top, seg, port_side(lit, f or 0).img)  # right edge: a port
    return blocks


def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1320, 1180), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)

    dr.text((20, 14), 'Tier 5 phase A: the Particle Accelerator ring (7x7 shown; sides 5 to 15 work)', font=font, fill=head)
    sheet.alpha_composite(iso_scene(ring_blocks(), 64, 640, 400, 320, 40), (0, 40))
    sheet.alpha_composite(iso_scene(ring_blocks(3), 64, 640, 400, 320, 40), (660, 40))
    dr.text((240, 430), 'built, idle', font=small, fill=label)
    dr.text((870, 430), 'running: beam lit, particles streak round', font=small, fill=label)

    y1 = 470
    dr.text((20, y1), 'Accelerator blocks', font=font, fill=head)
    parts = [('Segment side', segment_side().img), ('Segment side (running)', segment_side(3).img),
             ('Segment top', segment_top().img), ('Segment top (running)', segment_top(True, 3).img),
             ('Controller', controller_front().img), ('Controller (running)', controller_front(3).img),
             ('Port', port_side().img), ('Port (running)', port_side(True, 3).img)]
    for k, (name, im) in enumerate(parts):
        x = 20 + k * 160
        sheet.alpha_composite(up(im, 6), (x, y1 + 36))
        dr.text((x, y1 + 136), name, font=small, fill=label)

    y2 = 680
    dr.text((20, y2), 'Antimatter (fluid)', font=font, fill=head)
    for k in range(4):
        sheet.alpha_composite(up(antimatter_still(k * 2).img, 5), (20 + k * 92, y2 + 36))
    dr.text((20, y2 + 124), 'frames 0, 2, 4, 6 of 8 (glows)', font=small, fill=label)

    dr.text((420, y2), 'Containment Cell (no power needed)', font=font, fill=head)
    sheet.alpha_composite(iso_block(cell_top().img, cell_side().img, cell_side().img, 150), (420, y2 + 34))
    sheet.alpha_composite(iso_block(cell_top().img, cell_side(0.6, 2).img, cell_side(0.6, 2).img, 150), (590, y2 + 34))
    dr.text((440, y2 + 190), 'empty', font=small, fill=label)
    dr.text((610, y2 + 190), '60% full', font=small, fill=label)
    sheet.alpha_composite(up(cell_side(0.6, 2).img, 5), (770, y2 + 40))
    sheet.alpha_composite(up(cell_top().img, 5), (870, y2 + 40))

    y3 = 920
    dr.text((20, y3 - 20), 'Magnetic Conduit (antimatter only; normal pipes and tanks refuse antimatter)', font=font, fill=head)
    mc = conduit_sheet()
    scene = fp.scene(mc)
    sheet.alpha_composite(scene.crop((60, 0, 480, 330)).resize((300, 236)), (20, y3 + 20))
    sheet.alpha_composite(fp.item(mc), (340, y3 - 6))
    sheet.alpha_composite(up(mc, 10), (600, y3 + 20))
    dr.text((600, y3 + 186), 'texture sheet', font=small, fill=label)
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (980, 400), BG)
        g.alpha_composite(iso_scene(ring_blocks(k), 64, 640, 400, 320, 40), (0, 0))
        g.alpha_composite(up(controller_front(k).img, 6), (640, 40))
        g.alpha_composite(up(antimatter_still(k).img, 6), (640, 200))
        g.alpha_composite(iso_block(cell_top().img, cell_side(0.6, k).img, cell_side(0.6, k).img, 150), (790, 160))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
