"""Tier 6 phase A previews: the Event Horizon Reactor. A 7x7x7 cage of Containment Frame blocks (the 12 edges; the
faces are open), a Controller and ports in the frame. Once ignited (antimatter) a field fills the inner 5x5x5 with a
black hole in the middle (drawn by a block entity renderer in game); walking into the field kills, even in creative.
It eats any item thrown or piped in, by mass, for FE.

Run: tools/.venv/Scripts/python design/previews/phase_6a_preview.py design/previews/phase_6a.png design/previews/phase_6a_running.gif
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
from dark_chassis_system import K, STEEL, casing, seam, well  # noqa: E402

dcs.TIERS['singularity'] = dict(off=(86, 64, 24), on=(255, 186, 56), hi=(255, 236, 168))
S = dcs.TIERS['singularity']
VOID = (6, 6, 10)
FE_RED = [(110, 16, 16), (200, 36, 34), (255, 96, 80)]
MATTER = [(70, 64, 58), (120, 110, 98), (170, 160, 146)]

_src = open(os.path.join(TOOLS, 'six_tiers.py')).read()
_fn = _src[_src.index('def tier_singularity'):_src.index('TIER_LIST = [')]
_ns = dict(dcs.__dict__)
_ns['tc'] = lambda tier, k: dcs.TIERS[tier][k]
_ns['VOID'] = (8, 8, 12)
exec(_fn, _ns)
tier_singularity = _ns['tier_singularity']


# ================================================================ blocks
def frame_side(f=None):
    """Containment Frame: black plating, two gold field-emitter rails and a node in the middle; running, the rails
    glow and pulses run along them."""
    lit = f is not None
    f = f or 0
    t = casing()
    well(t, 2, 2, 13, 13, (14, 14, 18))
    for x in (4, 11):  # emitter rails
        for y in range(3, 13):
            if lit:
                k = 0.5 + 0.5 * math.sin((y + f * (1 if x == 4 else -1)) * TAU / 6)
                c = mix(S['on'], S['hi'], k)
            else:
                c = S['off']
            t.px(x, y, c)
            t.px(x + (1 if x == 4 else -1), y, shade(c, 0.55))
    for y in (3, 12):  # caps
        for x in range(4, 12):
            t.px(x, y, shade(S['off'], 1.1) if not lit else shade(S['on'], 0.7))
    for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):  # node
        t.px(x, y, S['hi'] if lit else S['off'])
    for (x, y) in ((7, 6), (6, 7), (9, 8), (8, 9)):
        t.px(x, y, shade(S['on'], 0.6) if lit else (40, 34, 20))
    return t


def act_gravity_well(t, f, lit):
    """Top half of the controller: a gravity well seen from above - gold rings closing in on a black centre; running,
    a bright arc runs round each ring (inner ones faster)."""
    ff = f if lit else 0
    for y in range(2, 7):
        for x in range(2, 14):
            t.px(x, y, (10, 10, 14))
    cx, cy = 7.5, 4.0
    rings = [(5.4, 2.1, 0.35), (3.6, 1.4, 0.6), (1.9, 0.75, 0.9)]
    for k, (rx, ry, bright) in enumerate(rings):
        for i in range(48):
            a = i * TAU / 48
            x = int(round(cx + rx * math.cos(a) - 0.5))
            y = int(round(cy + ry * math.sin(a)))
            if not (2 <= x <= 13 and 2 <= y <= 6):
                continue
            base = mix((30, 26, 18), S['on'], bright) if lit else mix((24, 22, 18), S['off'], bright)
            if lit:
                hot = 0.5 + 0.5 * math.cos(a - ff * TAU / FRAMES * (k + 1))
                base = mix(base, S['hi'], hot * 0.8)
            t.px(x, y, base)
    for (x, y) in ((7, 4), (8, 4)):  # the hole
        t.px(x, y, (0, 0, 0))


def controller_front(f=None):
    return dcs.front(act_gravity_well, tier_singularity, 'singularity', f)


def port_side(output, lit=False, f=0):
    """A frame block with a round connector. Input (matter + antimatter in): grey ring, arrows in.
    Output (FE out): red ring, arrows out - the colour of FE in the JEI pages."""
    t = frame_side(f if lit else None)
    ring = (FE_RED[2] if lit else FE_RED[1]) if output else (MATTER[2] if lit else MATTER[1])
    for y in range(4, 12):
        for x in range(4, 12):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 3.9:
                t.px(x, y, ring if d > 2.9 else (STEEL[1] if d > 1.9 else (8, 8, 12)))
    t.px(7, 7, S['hi'] if lit else S['off'])
    arrow = FE_RED[2] if output else MATTER[2]
    for (cx, cy, dx, dy) in ((2, 2, 1, 1), (13, 2, -1, 1), (2, 13, 1, -1), (13, 13, -1, -1)):
        if output:
            t.px(cx, cy, arrow); t.px(cx + dx, cy, arrow); t.px(cx, cy + dy, arrow)
        else:
            t.px(cx + dx, cy + dy, arrow); t.px(cx, cy + dy, arrow); t.px(cx + dx, cy, arrow)
    return t


def frame_top():
    return frame_side()


# ================================================================ the reactor scene
N = 7


def cage(f=None):
    lit = f is not None
    side = frame_side(f).img
    blocks = {}
    for x in range(N):
        for y in range(N):
            for z in range(N):
                edges = (x in (0, N - 1)) + (y in (0, N - 1)) + (z in (0, N - 1))
                if edges >= 2:
                    blocks[(x, y, z)] = (side, side, side)
    mid = N // 2
    blocks[(mid, 0, N - 1)] = (side, controller_front(f).img, side)          # controller: bottom front edge
    blocks[(N - 1, mid, N - 1)] = (side, port_side(False, lit, f or 0).img, port_side(False, lit, f or 0).img)  # input
    blocks[(0, mid, N - 1)] = (side, port_side(True, lit, f or 0).img, side)  # output
    return blocks


def project(size, ox, oy):
    unit = size / 2.05
    c30 = math.cos(math.radians(30))
    return lambda x, y, z: (ox + (x - z) * c30 * unit, oy + 0.27 * size + unit + ((x + z) * 0.5 - y) * unit), unit


def iso_scene(blocks, size, W, H, ox, oy, only=None):
    unit = size / 2.05
    c30 = math.cos(math.radians(30))
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    for (gx, gy, gz) in sorted(blocks, key=lambda p: (p[0] + p[2] + p[1], p[1])):
        if only is not None and not only((gx, gy, gz)):
            continue
        tp, fr, sd = blocks[(gx, gy, gz)]
        cube = iso_block(tp, fr, sd, size)
        dx = (gx - gz) * c30 * unit
        dy = ((gx + gz) * 0.5 - gy) * unit
        img.alpha_composite(cube, (int(ox - size / 2 + dx), int(oy + dy)))
    return img


def field_and_hole(W, H, size, ox, oy, f):
    """The field (translucent gold box over the inner 5x5x5) and the black hole in its middle."""
    proj, unit = project(size, ox, oy)
    layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    a0, a1 = 1, N - 1
    shimmer = 0.5 + 0.5 * math.sin(f * TAU / FRAMES)
    faces = [  # the three faces we see: top, front (z = a1), side (x = a1)
        [(a0, a1, a0), (a1, a1, a0), (a1, a1, a1), (a0, a1, a1)],
        [(a0, a0, a1), (a1, a0, a1), (a1, a1, a1), (a0, a1, a1)],
        [(a1, a0, a0), (a1, a0, a1), (a1, a1, a1), (a1, a1, a0)],
    ]
    for face in faces:
        d.polygon([proj(*p) for p in face], fill=S['on'] + (int(26 + 18 * shimmer),))
    for face in faces:
        pts = [proj(*p) for p in face]
        d.line(pts + [pts[0]], fill=S['hi'] + (int(90 + 60 * shimmer),), width=1)
    cx, cy = proj(N / 2, N / 2, N / 2)
    r = 1.4 * unit
    # lensing glow
    for k in range(10, 0, -1):
        rr = r * (1 + k * 0.12)
        d.ellipse((cx - rr, cy - rr, cx + rr, cy + rr), fill=S['on'] + (int(10 + 3 * (10 - k)),))
    rot = f * TAU / FRAMES
    disc_w, disc_h = r * 2.6, r * 0.55

    def disc(front):
        for i in range(48):
            a = i * TAU / 48
            if (math.sin(a) > 0) != front:
                continue
            for ring in range(4):
                rr = 0.55 + ring * 0.15
                x = cx + math.cos(a) * disc_w * rr
                y = cy + math.sin(a) * disc_h * rr
                hot = 0.5 + 0.5 * math.cos(a - rot)
                col = mix(shade(S['on'], 0.6), S['hi'], hot)
                d.ellipse((x - 2.2, y - 1.6, x + 2.2, y + 1.6), fill=tuple(int(v) for v in col[:3]) + (230,))
    disc(False)
    d.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(0, 0, 0, 255))
    d.ellipse((cx - r - 1.5, cy - r - 1.5, cx + r + 1.5, cy + r + 1.5), outline=S['hi'] + (255,), width=2)
    disc(True)
    return layer


def reactor(f=None, size=44, W=420, H=420, ox=210, oy=160):
    blocks = cage(f)
    front = (lambda p: p[0] == N - 1 or p[2] == N - 1 or p[1] == N - 1)
    img = iso_scene(blocks, size, W, H, ox, oy, only=lambda p: not front(p))
    img.alpha_composite(iso_scene(blocks, size, W, H, ox, oy, only=front))
    if f is not None:  # drawn last so the near corner pillar doesn't hide it (in game you'd walk round it)
        img.alpha_composite(field_and_hole(W, H, size, ox, oy, f))
    return img


def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1320, 960), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)
    dr.text((20, 14), 'Tier 6 phase A: the Event Horizon Reactor (7x7x7 frame; field + black hole in the inner 5x5x5)', font=font, fill=head)
    sheet.alpha_composite(reactor(), (20, 50))
    sheet.alpha_composite(reactor(3), (460, 50))
    dr.text((170, 470), 'built, idle', font=small, fill=label)
    dr.text((520, 470), 'running: deadly field, black hole eating items', font=small, fill=label)

    y1 = 520
    dr.text((20, y1), 'Reactor blocks', font=font, fill=head)
    parts = [('Containment Frame', frame_side().img), ('Frame (running)', frame_side(3).img),
             ('Controller', controller_front().img), ('Controller (running)', controller_front(3).img),
             ('Input Port', port_side(False).img), ('Input (running)', port_side(False, True, 3).img),
             ('Output Port (FE)', port_side(True).img), ('Output (running)', port_side(True, True, 3).img)]
    for k, (name, im) in enumerate(parts):
        x = 20 + k * 160
        sheet.alpha_composite(up(im, 6), (x, y1 + 36))
        dr.text((x, y1 + 136), name, font=small, fill=label)
    for k, (name, im) in enumerate([('frame', frame_side().img), ('controller', controller_front().img),
                                    ('input port', port_side(False).img), ('output port', port_side(True).img)]):
        x = 20 + k * 200
        sheet.alpha_composite(iso_block(frame_top().img, im, frame_side().img, 150), (x, y1 + 190))
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (820, 440), BG)
        g.alpha_composite(reactor(k), (10, 10))
        g.alpha_composite(up(controller_front(k).img, 8), (450, 60))
        g.alpha_composite(up(frame_side(k).img, 8), (620, 60))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
