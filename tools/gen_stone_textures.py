"""Marble and Volcanic Basalt block textures (vanilla-style pixel art, seamless 16x16 tiles).

Usage: python tools/gen_stone_textures.py [preview.png]
Writes into the mod's textures/block folder; with an argument, also writes a tiled preview sheet.

Per stone: <s>, cobbled_<s>, mossy_cobbled_<s>, polished_<s>, smooth_<s>, <s>_bricks,
cracked_<s>_bricks, mossy_<s>_bricks, chiseled_<s>, <s>_pillar (side) + <s>_pillar_top.

Marble: warm white with soft grey veins. Volcanic basalt: near-black, fine grain, pitted with small gas holes
(vesicles) so it reads differently from vanilla's grey, streaky basalt.
All noise is periodic over 16 px, so every texture tiles without seams.
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFont

OUT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources',
                   'assets', 'comminutioncraft', 'textures', 'block')
N = 16

MARBLE = dict(
    ramp=[(204, 202, 198), (215, 213, 208), (225, 223, 218), (234, 232, 227), (242, 240, 235)],
    vein=[(118, 123, 134), (146, 150, 159), (180, 182, 188)],  # core, edge, soft halo
    mortar=(146, 144, 140), shadow=(184, 182, 178), light=(250, 249, 245), grain=0.12,
)
BASALT = dict(
    ramp=[(40, 40, 46), (48, 48, 55), (57, 57, 65), (67, 67, 76), (79, 79, 89)],
    vein=[(20, 20, 24), (30, 30, 35), (104, 106, 118)],  # pit, pit rim, sparkle
    mortar=(17, 17, 20), shadow=(31, 31, 36), light=(94, 94, 105), grain=0.3,
)
MOSS = [(64, 86, 34), (78, 104, 40), (96, 124, 50), (112, 140, 60)]


def clamp(v):
    return max(0, min(255, int(round(v))))


def mix(a, b, t):
    return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(3))


def shade(c, f):
    return tuple(clamp(x * f) for x in c)


# ---------------------------------------------------------------- periodic noise
class Noise:
    """Value noise on a lattice that wraps every 16 px."""

    def __init__(self, seed):
        self.rng = random.Random(seed)
        self.grids = {}

    def _grid(self, cells):
        if cells not in self.grids:
            self.grids[cells] = [[self.rng.random() for _ in range(cells)] for _ in range(cells)]
        return self.grids[cells]

    def value(self, x, y, cells):
        g = self._grid(cells)
        fx, fy = x / N * cells, y / N * cells
        x0, y0 = int(math.floor(fx)), int(math.floor(fy))
        tx, ty = fx - x0, fy - y0
        tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
        a = g[y0 % cells][x0 % cells]
        b = g[y0 % cells][(x0 + 1) % cells]
        c = g[(y0 + 1) % cells][x0 % cells]
        d = g[(y0 + 1) % cells][(x0 + 1) % cells]
        return (a * (1 - tx) + b * tx) * (1 - ty) + (c * (1 - tx) + d * tx) * ty

    def fbm(self, x, y, octaves=((2, 0.5), (4, 0.3), (8, 0.2))):
        return sum(self.value(x, y, cells) * w for cells, w in octaves)


def pick(ramp, t):
    t = max(0.0, min(0.999, t))
    return ramp[int(t * len(ramp))]


def wrap_dist(ax, ay, bx, by):
    dx = min(abs(ax - bx), N - abs(ax - bx))
    dy = min(abs(ay - by), N - abs(ay - by))
    return math.hypot(dx, dy)


class Tile:
    def __init__(self):
        self.img = Image.new('RGBA', (N, N))

    def px(self, x, y, c):
        self.img.putpixel((x % N, y % N), tuple(c[:3]) + (255,))

    def get(self, x, y):
        return self.img.getpixel((x % N, y % N))[:3]


# ---------------------------------------------------------------- surfaces
def grain(t, p, seed, contrast=1.0, bias=0.0, box=(0, 0, N, N)):
    """Fill with the stone's own speckled grain (the base of every variant)."""
    nz = Noise(seed)
    rng = random.Random(seed + 1)
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1):
            v = 0.5 + (nz.fbm(x, y) - 0.5) * 1.6 * contrast + (rng.random() - 0.5) * p['grain'] * 2 * contrast + bias
            t.px(x, y, pick(p['ramp'], v))


def marble_veins(t, seed, strength=1.0, box=(0, 0, N, N)):
    """One wandering diagonal vein per tile, running on into the next tile.

    |sin(pi * u)| repeats every 1 in u, so with u = (x + y) / 16 the vein wraps cleanly across tiles.
    strength < 1 makes the veins fainter (for worked stone).
    """
    nz = Noise(seed)
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1):
            warp = (nz.fbm(x, y) - 0.5) * 3.0
            main = abs(math.sin(math.pi * (x + y) / N + warp))
            if strength >= 0.8:  # natural stone: thin grey core with a soft halo
                if main < 0.07:
                    c, k = MARBLE['vein'][1], 1.0
                elif main < 0.18:
                    c, k = MARBLE['vein'][2], 0.9
                elif main < 0.28:
                    c, k = MARBLE['vein'][2], 0.45
                else:
                    continue
            else:  # worked stone: only a faint ghost of the vein
                if main < 0.16:
                    c, k = MARBLE['vein'][2], 0.25 + 0.5 * strength
                else:
                    continue
            t.px(x, y, mix(t.get(x, y), c, k))


def basalt_pits(t, seed, count=7, box=(0, 0, N, N)):
    """Small gas holes: a dark pixel with a lit lower-right rim, plus a few glassy sparkles."""
    rng = random.Random(seed)
    x0, y0, x1, y1 = box
    for _ in range(count):
        x, y = rng.randrange(x0, x1), rng.randrange(y0, y1)
        t.px(x, y, BASALT['vein'][0])
        if rng.random() < 0.5 and x + 1 < x1:
            t.px(x + 1, y, BASALT['vein'][0])
        if y - 1 >= y0:
            t.px(x, y - 1, BASALT['vein'][1])
        if y + 1 < y1:
            t.px(x, y + 1, BASALT['light'])
    for _ in range(3):
        t.px(rng.randrange(x0, x1), rng.randrange(y0, y1), BASALT['vein'][2])


def natural(p, kind, seed):
    t = Tile()
    grain(t, p, seed)
    if kind == 'marble':
        marble_veins(t, seed + 7)
    else:
        basalt_pits(t, seed + 7, count=10)
    return t


def cobbled(p, kind, seed, mossy=False):
    """Rounded stones (periodic Voronoi) with dark gaps, lit from the top left."""
    rng = random.Random(seed)
    pts = [(rng.uniform(0, N), rng.uniform(0, N)) for _ in range(10)]
    tones = [rng.uniform(-0.25, 0.25) for _ in pts]
    nz = Noise(seed + 3)
    t = Tile()
    for y in range(N):
        for x in range(N):
            cx, cy = x + 0.5, y + 0.5
            d = sorted((wrap_dist(cx, cy, px_, py_), i) for i, (px_, py_) in enumerate(pts))
            (d1, i1), (d2, _) = d[0], d[1]
            if d2 - d1 < 0.8:
                t.px(x, y, p['mortar'])
                continue
            # offset from the stone's centre (wrapped) -> light top-left, shade bottom-right
            ox = ((cx - pts[i1][0] + N / 2) % N) - N / 2
            oy = ((cy - pts[i1][1] + N / 2) % N) - N / 2
            light = -(ox + oy) / 5.0
            v = 0.55 + tones[i1] + light * 0.32 + (nz.value(x, y, 8) - 0.5) * 0.3
            t.px(x, y, pick(p['ramp'], v))
            if d2 - d1 < 1.6:
                t.px(x, y, p['shadow'] if (ox + oy) > 0 else mix(t.get(x, y), p['light'], 0.5))
    if kind == 'marble':
        marble_veins(t, seed + 9, strength=0.6)
    else:
        basalt_pits(t, seed + 9, count=5)
    if mossy:
        moss(t, seed + 11, amount=0.2)
    return t


def moss(t, seed, amount=0.3, mortar=None):
    """Moss tufts covering about `amount` of the tile, favouring gaps (mortar) so it creeps out of the cracks."""
    nz = Noise(seed)
    rng = random.Random(seed)
    scores = {}
    for y in range(N):
        for x in range(N):
            scores[(x, y)] = (nz.fbm(x, y, ((4, 0.5), (8, 0.5))) + rng.random() * 0.35
                              + (0.25 if mortar and mortar(x, y) else 0))
    cutoff = sorted(scores.values(), reverse=True)[int(N * N * amount)]
    for (x, y), v in scores.items():
        if v > cutoff:
            k = min(0.999, (v - cutoff) / 0.35)
            t.px(x, y, MOSS[int(k * len(MOSS))])


def polished(p, kind, seed):
    """Flat, lightly grained slab with a crisp bevel (like polished andesite)."""
    t = Tile()
    grain(t, p, seed, contrast=0.45, bias=0.1)
    if kind == 'marble':
        marble_veins(t, seed + 5, strength=0.55, box=(1, 1, N - 1, N - 1))
    else:
        basalt_pits(t, seed + 5, count=2, box=(2, 2, N - 2, N - 2))
    for i in range(N):
        t.px(i, 0, p['light']); t.px(0, i, p['light'])
        t.px(i, N - 1, p['shadow']); t.px(N - 1, i, p['shadow'])
    t.px(0, N - 1, p['ramp'][2]); t.px(N - 1, 0, p['ramp'][2])
    return t


def smooth(p, kind, seed):
    """Almost flat face with a thin darker edge (like smooth stone)."""
    t = Tile()
    grain(t, p, seed, contrast=0.25, bias=0.05)
    for i in range(N):
        for (x, y) in ((i, 0), (0, i), (i, N - 1), (N - 1, i)):
            t.px(x, y, mix(t.get(x, y), p['shadow'], 0.7))
    return t


def bricks(p, kind, seed, cracked=False, mossy=False):
    """Vanilla stone-brick layout: two 8 px courses of 16 px bricks, the lower one offset by half a brick."""
    t = Tile()
    grain(t, p, seed, contrast=0.6, bias=0.05)
    if kind == 'marble':
        marble_veins(t, seed + 3, strength=0.5)
    else:
        basalt_pits(t, seed + 3, count=4)

    def mortar_at(x, y):
        x, y = x % N, y % N
        return y % 8 == 7 or (x + (0 if y < 8 else 8)) % 16 == 15

    for y in range(N):
        for x in range(N):
            row = y // 8
            bx = (x + (0 if row == 0 else 8)) % 16  # x within the brick
            by = y % 8
            if by == 7 or bx == 15:
                t.px(x, y, p['mortar'])
            elif by == 0 or bx == 0:
                t.px(x, y, mix(t.get(x, y), p['light'], 0.6))
            elif by == 6 or bx == 14:
                t.px(x, y, mix(t.get(x, y), p['shadow'], 0.7))
    if cracked:
        rng = random.Random(seed + 13)
        # (start x, start y, step x, step y, length): each crack runs off a mortar line into a brick
        for (x, y, sx, sy, length) in ((3, 0, 1, 1, 6), (13, 6, -1, -1, 4), (6, 8, 1, 1, 5), (12, 14, 1, -1, 3), (1, 9, 1, 0, 3)):
            for _ in range(length):
                t.px(x, y, p['mortar'])
                t.px(x + 1, y, mix(t.get(x + 1, y), p['shadow'], 0.35))
                if rng.random() < 0.6:
                    x += sx
                if rng.random() < 0.7:
                    y += sy
    if mossy:
        moss(t, seed + 17, amount=0.28, mortar=mortar_at)
    return t


def chiseled(p, kind, seed):
    """Bevelled panel with a carved centre: a rosette for marble, a stepped diamond for basalt."""
    t = polished(p, kind, seed)
    # inner frame, engraved
    for i in range(2, N - 2):
        t.px(i, 2, p['shadow']); t.px(2, i, p['shadow'])
        t.px(i, N - 3, p['light']); t.px(N - 3, i, p['light'])
    c = (N - 1) / 2
    for y in range(3, N - 3):
        for x in range(3, N - 3):
            dx, dy = x - c, y - c
            if kind == 'marble':
                r = math.hypot(dx, dy)
                petal = math.cos(4 * math.atan2(dy, dx))
                if abs(r - 4.2) < 0.55:
                    t.px(x, y, p['shadow'] if dy < 0 else p['light'])  # ring, carved
                elif r < 3.4 and r > 0.8 and petal > 0.55:
                    t.px(x, y, mix(t.get(x, y), MARBLE['vein'][1], 0.55))
                elif r <= 0.8:
                    t.px(x, y, MARBLE['vein'][1])
            else:
                d = abs(dx) + abs(dy)
                if abs(d - 4.5) < 0.5:
                    t.px(x, y, p['mortar'] if dy < 0 else p['light'])
                elif abs(d - 2) < 0.5:
                    t.px(x, y, p['light'] if dy < 0 else p['mortar'])
    return t


def pillar_side(p, kind, seed):
    """Vertical fluting: shallow grooves, lit on their left edge."""
    t = Tile()
    grain(t, p, seed, contrast=0.4, bias=0.08)
    if kind == 'marble':
        marble_veins(t, seed + 2, strength=0.35)
    for y in range(N):
        for x in range(N):
            k = x % 4
            if k == 0:
                t.px(x, y, mix(t.get(x, y), p['shadow'], 0.75))
            elif k == 1:
                t.px(x, y, mix(t.get(x, y), p['light'], 0.45))
    return t


def pillar_top(p, kind, seed):
    """End grain: concentric rounded rings, bevelled border."""
    t = polished(p, kind, seed)
    c = (N - 1) / 2
    for y in range(1, N - 1):
        for x in range(1, N - 1):
            r = max(abs(x - c), abs(y - c)) * 0.6 + math.hypot(x - c, y - c) * 0.4
            ring = r % 3.0
            if ring < 0.7:
                t.px(x, y, mix(t.get(x, y), p['shadow'], 0.55))
            elif ring < 1.3:
                t.px(x, y, mix(t.get(x, y), p['light'], 0.35))
    return t


# ---------------------------------------------------------------- build
def build(s, kind, p, base_seed):
    """s = block name stem (e.g. volcanic_basalt), kind = 'marble' or 'basalt' (which look to draw)."""
    return {
        s: natural(p, kind, base_seed + 1),
        f'cobbled_{s}': cobbled(p, kind, base_seed + 2),
        f'mossy_cobbled_{s}': cobbled(p, kind, base_seed + 2, mossy=True),
        f'polished_{s}': polished(p, kind, base_seed + 3),
        f'smooth_{s}': smooth(p, kind, base_seed + 4),
        f'{s}_bricks': bricks(p, kind, base_seed + 5),
        f'cracked_{s}_bricks': bricks(p, kind, base_seed + 5, cracked=True),
        f'mossy_{s}_bricks': bricks(p, kind, base_seed + 5, mossy=True),
        f'chiseled_{s}': chiseled(p, kind, base_seed + 6),
        f'{s}_pillar': pillar_side(p, kind, base_seed + 7),
        f'{s}_pillar_top': pillar_top(p, kind, base_seed + 8),
    }


def preview(sets, path):
    """Each texture tiled 3x3 at 3x scale, so seams and repetition are visible."""
    scale, reps = 3, 3
    cell = N * scale * reps
    cols = max(len(v) for v in sets.values())
    pad, label_h = 10, 18
    font = ImageFont.load_default(size=12)
    sheet = Image.new('RGBA', (pad + cols * (cell + pad), pad + len(sets) * (cell + label_h + pad)), (34, 36, 42, 255))
    draw = ImageDraw.Draw(sheet)
    for r, (kind, textures) in enumerate(sets.items()):
        for c, (name, tile) in enumerate(textures.items()):
            tiled = Image.new('RGBA', (N * reps, N * reps))
            for i in range(reps):
                for j in range(reps):
                    tiled.paste(tile.img, (i * N, j * N))
            x, y = pad + c * (cell + pad), pad + r * (cell + label_h + pad)
            sheet.paste(tiled.resize((cell, cell), Image.NEAREST), (x, y))
            draw.text((x, y + cell + 3), name, font=font, fill=(220, 222, 228))
    sheet.save(path)


if __name__ == '__main__':
    sets = {'marble': build('marble', 'marble', MARBLE, 100),
            'volcanic_basalt': build('volcanic_basalt', 'basalt', BASALT, 200)}
    os.makedirs(OUT, exist_ok=True)
    for textures in sets.values():
        for name, tile in textures.items():
            tile.img.save(os.path.join(OUT, name + '.png'))
    if len(sys.argv) > 1:
        preview(sets, sys.argv[1])
    print('ok')
