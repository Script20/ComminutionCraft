"""Generates all ComminutionCraft textures as 16x16 pixel art (plus GUI sheets)."""
import json
import math
import os
import random
import sys

from PIL import Image

OUT = sys.argv[1]
BLOCK = os.path.join(OUT, 'block')
ITEM = os.path.join(OUT, 'item')
GUI = os.path.join(OUT, 'gui', 'container')
for d in (BLOCK, ITEM, GUI):
    os.makedirs(d, exist_ok=True)

FRAMES = 8


def clamp(v):
    return max(0, min(255, int(round(v))))


def shade(c, f):
    return tuple(clamp(x * f) for x in c[:3]) + (255,)


def mix(a, b, t):
    return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def new(w=16, h=16, c=(0, 0, 0, 0)):
    return Image.new('RGBA', (w, h), c)


# ---------------------------------------------------------------- materials

STONE = [(84, 84, 88), (97, 97, 101), (110, 110, 114), (122, 122, 126)]
IRON_HI, IRON, IRON_MID, IRON_LO, IRON_DARK = (196, 198, 204), (160, 162, 168), (128, 130, 136), (92, 94, 100), (52, 53, 58)


def stone_fill(img, seed, box=(0, 0, 16, 16)):
    rng = random.Random(seed)
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1):
            r = rng.random()
            c = STONE[0] if r < 0.15 else STONE[1] if r < 0.5 else STONE[2] if r < 0.85 else STONE[3]
            img.putpixel((x, y), c + (255,))
    # a few darker "cracks" for a cobbled look
    for _ in range((x1 - x0) * (y1 - y0) // 20):
        x, y = rng.randrange(x0, x1), rng.randrange(y0, y1)
        img.putpixel((x, y), (70, 70, 74, 255))


def iron_frame(img):
    """1px bevelled iron border with corner rivets."""
    for i in range(16):
        img.putpixel((i, 0), IRON_HI + (255,))
        img.putpixel((0, i), IRON_HI + (255,))
        img.putpixel((i, 15), IRON_LO + (255,))
        img.putpixel((15, i), IRON_LO + (255,))
    img.putpixel((15, 0), IRON + (255,))
    img.putpixel((0, 15), IRON + (255,))
    for (x, y) in ((1, 1), (14, 1), (1, 14), (14, 14)):
        img.putpixel((x, y), IRON + (255,))
        img.putpixel((x + (1 if x == 1 else -1), y + (1 if y == 1 else -1)), IRON_DARK + (255,))


def iron_band(img, y, rivets=True):
    for x in range(1, 15):
        img.putpixel((x, y), IRON + (255,))
        img.putpixel((x, y + 1), IRON_LO + (255,))
    if rivets:
        for x in (3, 12):
            img.putpixel((x, y), IRON_HI + (255,))


def bevel_box(img, x0, y0, x1, y1, hi, lo):
    """Inset frame: dark top/left, light bottom/right (looks recessed)."""
    for x in range(x0, x1 + 1):
        img.putpixel((x, y0), lo + (255,))
        img.putpixel((x, y1), hi + (255,))
    for y in range(y0, y1 + 1):
        img.putpixel((x0, y), lo + (255,))
        img.putpixel((x1, y), hi + (255,))


# ---------------------------------------------------------------- granulator

RAW_ORE = [(216, 175, 147), (175, 130, 105), (225, 140, 95), (235, 200, 90), (150, 150, 150)]


def granulator_base(seed):
    img = new()
    stone_fill(img, seed)
    iron_frame(img)
    return img


def granulator_side():
    img = granulator_base(11)
    iron_band(img, 7)
    return img


def granulator_top():
    img = granulator_base(12)
    # feed hopper: iron rim, dark throat, a few ore chunks waiting
    for y in range(4, 12):
        for x in range(4, 12):
            img.putpixel((x, y), IRON_MID + (255,))
    bevel_box(img, 4, 4, 11, 11, IRON_HI, IRON_LO)
    for y in range(5, 11):
        for x in range(5, 11):
            d = max(abs(x - 7.5), abs(y - 7.5))
            v = 18 + d * 9
            img.putpixel((x, y), (clamp(v), clamp(v), clamp(v + 2), 255))
    for (x, y, c) in ((6, 6, 0), (9, 7, 2), (7, 9, 4), (8, 6, 1)):
        img.putpixel((x, y), RAW_ORE[c] + (255,))
    return img


ROLL_L = (4.5, 5.0)
ROLL_R = (11.5, 5.0)
ROLL_RADIUS = 2.6


def draw_roller(img, centre, phase, spin):
    """Toothed crushing roller: dark notches on the rim rotate each frame."""
    cx, cy = centre
    sectors = 8
    for y in range(2, 9):
        for x in range(2, 14):
            dx, dy = x - cx, y - cy
            dist = math.hypot(dx, dy)
            if dist > ROLL_RADIUS + 0.35:
                continue
            ang = (math.atan2(dy, dx) * spin + phase) % (2 * math.pi)
            tooth = int(ang / (2 * math.pi / sectors)) % 2 == 0
            if dist > ROLL_RADIUS - 0.75:
                c = (IRON_HI if dy < 0 else IRON) if tooth else IRON_DARK
            elif dist < 0.75:
                c = IRON_DARK
            else:
                c = IRON if (dx + dy) < 0 else IRON_MID
            img.putpixel((x, y), c + (255,))


def granulator_front(frame=None):
    lit = frame is not None
    img = granulator_base(13)
    # crushing chamber window
    for y in range(2, 9):
        for x in range(2, 14):
            img.putpixel((x, y), (26, 26, 29, 255))
    bevel_box(img, 1, 1, 14, 9, IRON_HI, IRON_DARK)
    phase = (frame or 0) * (2 * math.pi / 4) / FRAMES
    draw_roller(img, ROLL_L, phase, 1)
    draw_roller(img, ROLL_R, phase, -1)

    # granules: ore chunks feed in at the top of the nip, fine grit falls out below it
    if lit:
        img.putpixel((8, 2 + frame % 2), RAW_ORE[frame % 4] + (255,))
        img.putpixel((7, 2 + (frame + 1) % 2), RAW_ORE[(frame + 2) % 4] + (255,))
        for k in range(3):
            y = 5 + (frame + k * 3) % 4
            x = 7 + (k + frame // 2) % 2
            img.putpixel((x, y), RAW_ORE[(frame + k) % 5] + (255,))
    else:
        img.putpixel((8, 8), (70, 60, 52, 255))
        img.putpixel((7, 8), (58, 52, 46, 255))

    # iron band separating chamber and firebox
    iron_band(img, 10, rivets=False)
    # firebox grate
    flicker = [1.0, 0.85, 0.95, 0.75, 1.0, 0.9, 0.8, 0.95]
    for y in range(12, 15):
        for x in range(3, 13):
            if x % 2 == 0:
                img.putpixel((x, y), IRON_DARK + (255,))
            elif lit:
                f = flicker[(frame + x) % FRAMES]
                base = (255, 200, 70) if y == 14 else (240, 130, 40) if y == 13 else (190, 70, 25)
                img.putpixel((x, y), shade(base, f))
            else:
                img.putpixel((x, y), (16, 16, 18, 255))
    for x in range(2, 14):
        img.putpixel((x, 11), IRON_DARK + (255,))
    return img


# ---------------------------------------------------------------- smelter

BRICK = [(140, 64, 48), (152, 72, 54), (163, 82, 61), (128, 58, 44)]
MORTAR = (160, 150, 140)


def brick_fill(img, seed, box=(0, 0, 16, 16)):
    rng = random.Random(seed)
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        row = y // 4
        for x in range(x0, x1):
            off = 4 if row % 2 else 0
            if y % 4 == 3 or (x + off) % 8 == 7:
                c = MORTAR
                if rng.random() < 0.2:
                    c = shade(MORTAR, 0.9)[:3]
            else:
                brick_id = ((x + off) // 8) * 7 + row * 3
                base = BRICK[(brick_id + seed) % len(BRICK)]
                c = shade(base, 0.93 + 0.12 * rng.random())[:3]
                if y % 4 == 0:
                    c = shade(c, 1.08)[:3]
            img.putpixel((x, y), c + (255,))


def smelter_bands(img):
    for x in range(16):
        img.putpixel((x, 0), IRON_HI + (255,))
        img.putpixel((x, 1), IRON_LO + (255,))
        img.putpixel((x, 14), IRON + (255,))
        img.putpixel((x, 15), IRON_LO + (255,))
    for x in (2, 13):
        img.putpixel((x, 0), (230, 232, 236, 255))
        img.putpixel((x, 14), IRON_HI + (255,))


def smelter_side():
    img = new()
    brick_fill(img, 3)
    smelter_bands(img)
    return img


def smelter_top():
    img = new()
    brick_fill(img, 5)
    smelter_bands(img)
    # chimney vent: round iron collar with a dark grated hole
    for y in range(3, 13):
        for x in range(3, 13):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 4.8:
                if d > 3.6:
                    c = IRON_HI if (x + y) < 15 else IRON_LO
                elif d > 2.9:
                    c = IRON_DARK
                else:
                    c = (22, 20, 20) if (x % 2) else (48, 44, 42)
                img.putpixel((x, y), c + (255,))
    return img


def flame_colour(t):
    """t: 0 = base of flame (hot), 1 = tip."""
    stops = [(255, 245, 170), (255, 205, 80), (245, 140, 40), (200, 70, 25)]
    t = max(0.0, min(0.999, t)) * (len(stops) - 1)
    i = int(t)
    return mix(stops[i], stops[i + 1], t - i)


def smelter_front(frame=None):
    lit = frame is not None
    img = new()
    brick_fill(img, 7)
    smelter_bands(img)
    # iron-framed mouth
    x0, y0, x1, y1 = 3, 4, 12, 13
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            img.putpixel((x, y), IRON_MID + (255,))
    bevel_box(img, x0, y0, x1, y1, IRON_HI, IRON_DARK)
    ix0, iy0, ix1, iy1 = x0 + 1, y0 + 1, x1 - 1, y1 - 1
    for y in range(iy0, iy1 + 1):
        for x in range(ix0, ix1 + 1):
            img.putpixel((x, y), (20, 16, 15, 255))

    # crucible sitting on the coals
    cru_top = 9
    for y in range(cru_top, iy1 - 1):
        inset = (y - cru_top) // 2
        for x in range(ix0 + 1 + inset, ix1 - inset):
            img.putpixel((x, y), (88, 80, 76, 255) if not lit else (120, 90, 70, 255))
    # crucible rim / molten metal surface
    for x in range(ix0 + 1, ix1):
        if lit:
            glow = 0.9 + 0.1 * math.sin(frame * math.pi / 4 + x)
            img.putpixel((x, cru_top), shade((255, 170, 60), glow))
        else:
            img.putpixel((x, cru_top), (110, 102, 98, 255))
    # coal bed
    for x in range(ix0, ix1 + 1):
        if lit:
            img.putpixel((x, iy1), shade((230, 90, 30), 0.75 + 0.25 * ((x + frame) % 3 == 0)))
            img.putpixel((x, iy1 - 1), (60, 30, 20, 255) if x % 3 else (255, 150, 50, 255))
        else:
            img.putpixel((x, iy1), (40, 38, 38, 255) if x % 2 else (28, 27, 27, 255))
            img.putpixel((x, iy1 - 1), (24, 22, 22, 255))

    if lit:
        # flames licking up around the crucible (columns on each side + behind)
        for x in range(ix0, ix1 + 1):
            side = x in (ix0, ix1)
            h = 3.2 + 1.8 * math.sin(frame * math.pi / 4 * 2 + x * 1.7) + (1.5 if side else 0)
            for k in range(int(h)):
                y = iy1 - 2 - k
                if y < iy0:
                    break
                if not side and y >= cru_top:
                    continue  # crucible is in front
                img.putpixel((x, y), flame_colour(k / max(h, 1)))
        # warm glow on the iron frame
        for x in range(x0 + 1, x1):
            img.putpixel((x, y1), shade((200, 140, 100), 1.0))
    return img


# ---------------------------------------------------------------- items

def granulated(palette, seed):
    """palette: (darkest, dark, mid, light, highlight)"""
    darkest, dark, mid, light, hi = palette
    rng = random.Random(seed)
    img = new()
    cx, base, top = 7.5, 13.5, 5.0
    mask = set()
    for y in range(16):
        for x in range(16):
            nx = (x - cx) / 6.6
            ny = (y - base) / (base - top)
            wobble = 0.08 * math.sin(x * 2.1 + seed)
            if y <= 13 and nx * nx + ny * ny <= 1.0 + wobble:
                mask.add((x, y))
    for (x, y) in mask:
        light_amt = (-(x - cx) * 0.6 - (y - 9) * 0.8) / 8 + rng.uniform(-0.45, 0.45)
        c = hi if light_amt > 0.55 else light if light_amt > 0.15 else mid if light_amt > -0.3 else dark
        img.putpixel((x, y), c + (255,))
    # outline
    for (x, y) in mask:
        if any((x + dx, y + dy) not in mask for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            img.putpixel((x, y), (darkest if y > 9 or x > cx else dark) + (255,))
    # loose grains
    for (x, y) in ((2, 13), (13, 12), (3, 10), (12, 14), (14, 14)):
        if (x, y) not in mask:
            img.putpixel((x, y), (mid if rng.random() < 0.5 else light) + (255,))
    return img


ITEM_PALETTES = {
    'iron': ((92, 78, 72), (150, 132, 122), (196, 180, 170), (222, 210, 202), (244, 238, 232)),
    'copper': ((100, 46, 28), (158, 78, 48), (214, 118, 76), (238, 158, 110), (252, 196, 156)),
    'gold': ((128, 84, 10), (196, 140, 24), (238, 196, 50), (252, 228, 110), (255, 248, 190)),
}

# ---------------------------------------------------------------- GUI

GUI_BG = (198, 198, 198)
GUI_HI = (255, 255, 255)
GUI_LO = (85, 85, 85)
SLOT_IN = (139, 139, 139)
SLOT_LO = (55, 55, 55)

FLAME = [
    "......#.......",
    ".....##.......",
    ".....###......",
    "....####...#..",
    "...#####..##..",
    "...######.##..",
    "..##########..",
    "..###########.",
    ".############.",
    ".############.",
    ".############.",
    "..##########..",
    "...########...",
    "....######....",
]


def arrow_mask(x, y):
    if 6 <= y <= 10 and x <= 14:
        return True
    if 15 <= x <= 23:
        h = 8 - (x - 15)
        return 8 - h <= y <= 8 + h
    return False


def slot(img, x, y, size=18):
    for i in range(size):
        for j in range(size):
            img.putpixel((x + i, y + j), SLOT_IN + (255,))
    for i in range(size):
        img.putpixel((x + i, y), SLOT_LO + (255,))
        img.putpixel((x, y + i), SLOT_LO + (255,))
        img.putpixel((x + i, y + size - 1), GUI_HI + (255,))
        img.putpixel((x + size - 1, y + i), GUI_HI + (255,))
    img.putpixel((x + size - 1, y), SLOT_IN + (255,))
    img.putpixel((x, y + size - 1), SLOT_IN + (255,))


def gui_sheet(arrow_fill, accent):
    img = new(256, 256)
    W, H = 176, 166
    for y in range(H):
        for x in range(W):
            img.putpixel((x, y), GUI_BG + (255,))
    # rounded outer border + bevel, vanilla style
    for x in range(3, W - 3):
        img.putpixel((x, 0), (0, 0, 0, 255))
        img.putpixel((x, H - 1), (0, 0, 0, 255))
    for y in range(3, H - 3):
        img.putpixel((0, y), (0, 0, 0, 255))
        img.putpixel((W - 1, y), (0, 0, 0, 255))
    for (x, y) in ((1, 1), (2, 1), (1, 2), (W - 2, H - 2), (W - 3, H - 2), (W - 2, H - 3),
                   (W - 3, 1), (W - 2, 2), (1, H - 3), (2, H - 2)):
        img.putpixel((x, y), (0, 0, 0, 255))
    for (x, y) in ((0, 0), (1, 0), (2, 0), (0, 1), (0, 2), (W - 1, H - 1), (W - 2, H - 1), (W - 3, H - 1),
                   (W - 1, H - 2), (W - 1, H - 3), (W - 1, 0), (W - 2, 0), (W - 1, 1), (W - 1, 2), (W - 2, 1),
                   (0, H - 1), (1, H - 1), (0, H - 2), (0, H - 3), (1, H - 2)):
        img.putpixel((x, y), (0, 0, 0, 0))
    for x in range(3, W - 3):
        img.putpixel((x, 1), GUI_HI + (255,))
        img.putpixel((x, 2), GUI_HI + (255,))
        img.putpixel((x, H - 2), GUI_LO + (255,))
        img.putpixel((x, H - 3), GUI_LO + (255,))
    for y in range(3, H - 3):
        img.putpixel((1, y), GUI_HI + (255,))
        img.putpixel((2, y), GUI_HI + (255,))
        img.putpixel((W - 2, y), GUI_LO + (255,))
        img.putpixel((W - 3, y), GUI_LO + (255,))
    img.putpixel((3, 3), GUI_HI + (255,))
    img.putpixel((W - 4, H - 4), GUI_LO + (255,))

    # machine slots (same positions as the vanilla furnace)
    slot(img, 55, 16)          # input
    slot(img, 55, 52)          # fuel
    slot(img, 111, 30, 26)     # output (big)
    # player inventory
    for row in range(3):
        for col in range(9):
            slot(img, 7 + col * 18, 83 + row * 18)
    for col in range(9):
        slot(img, 7 + col * 18, 141)

    # accent strip under the title, tinted per machine
    for x in range(56, 120):
        img.putpixel((x, 14), accent + (255,))

    # empty flame + arrow in the background
    for y, row in enumerate(FLAME):
        for x, ch in enumerate(row):
            if ch == '#':
                img.putpixel((56 + x, 36 + y), SLOT_IN + (255,))
    for y in range(17):
        for x in range(24):
            if arrow_mask(x, y):
                img.putpixel((79 + x, 34 + y), SLOT_IN + (255,))

    # filled flame sprite at (176, 0)
    for y, row in enumerate(FLAME):
        for x, ch in enumerate(row):
            if ch == '#':
                centre = 1 - min(1, abs(x - 6.5) / 6)
                img.putpixel((176 + x, y), flame_colour(1 - (y / 13) * 0.8 - centre * 0.25))
    # filled arrow sprite at (176, 14)
    for y in range(17):
        for x in range(24):
            if arrow_mask(x, y):
                edge = not (arrow_mask(x, y - 1) and arrow_mask(x, y + 1))
                c = shade(arrow_fill, 0.8) if edge else arrow_fill + (255,)
                img.putpixel((176 + x, 14 + y), c)
    return img


# ---------------------------------------------------------------- write everything

def save_anim(name, frame_fn):
    strip = new(16, 16 * FRAMES)
    for f in range(FRAMES):
        strip.paste(frame_fn(f), (0, 16 * f))
    path = os.path.join(BLOCK, name + '.png')
    strip.save(path)
    with open(path + '.mcmeta', 'w') as fh:
        json.dump({"animation": {"frametime": 2, "interpolate": False}}, fh, indent=2)
        fh.write('\n')


granulator_side().save(os.path.join(BLOCK, 'coal_granulator_side.png'))
granulator_top().save(os.path.join(BLOCK, 'coal_granulator_top.png'))
granulator_front().save(os.path.join(BLOCK, 'coal_granulator_front.png'))
save_anim('coal_granulator_front_on', granulator_front)

smelter_side().save(os.path.join(BLOCK, 'coal_smelter_side.png'))
smelter_top().save(os.path.join(BLOCK, 'coal_smelter_top.png'))
smelter_front().save(os.path.join(BLOCK, 'coal_smelter_front.png'))
save_anim('coal_smelter_front_on', smelter_front)

for i, (metal, pal) in enumerate(ITEM_PALETTES.items()):
    granulated(pal, 40 + i).save(os.path.join(ITEM, f'granulated_{metal}.png'))

gui_sheet((214, 200, 186), (150, 150, 156)).save(os.path.join(GUI, 'coal_granulator.png'))
gui_sheet((255, 170, 70), (168, 84, 60)).save(os.path.join(GUI, 'coal_smelter.png'))
print('ok')
