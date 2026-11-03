"""All six ComminutionCraft tiers on the Dark Chassis, shown as granulators (top half = conveyor roller)."""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)

# Reuse the Dark Chassis building blocks (everything before the VARIANTS table).
_src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'dark_chassis_system.py')).read()
_src = _src[:_src.index('VARIANTS = [')]
_argv = sys.argv[:]
sys.argv = [sys.argv[0], os.path.join(OUT, '_scratch')]
exec(_src, globals())
sys.argv = _argv
OUT = _argv[1]  # the exec'd code reassigns OUT

TIERS.update({
    'plasma': dict(off=(92, 34, 84), on=(255, 80, 210), hi=(255, 196, 244)),
    'fusion': dict(off=(32, 84, 44), on=(96, 255, 128), hi=(214, 255, 214)),
    'antimatter': dict(off=(86, 90, 104), on=(214, 228, 255), hi=(255, 255, 255)),
    'singularity': dict(off=(86, 64, 24), on=(255, 186, 56), hi=(255, 236, 168)),
})
VOID = (8, 8, 11)


def tc(tier, k):
    return TIERS[tier][k]


# ---------------------------------------------------------------- 3. PLASMA: swirling arc in a containment tube
def tier_plasma(t, f, lit):
    well(t, 2, 9, 13, 14, (12, 10, 16))
    # glass tube walls
    for x in range(4, 12):
        t.px(x, 10, (52, 46, 62))
        t.px(x, 13, (40, 36, 48))
    # electrodes at each end
    for y in range(10, 14):
        t.px(3, y, STEEL[3] if y in (11, 12) else STEEL[1])
        t.px(12, y, STEEL[3] if y in (11, 12) else STEEL[1])
    phase = f * TAU / FRAMES
    for x in range(4, 12):
        for k, off in enumerate((0.0, math.pi)):
            yf = 11.5 + 1.0 * math.sin(x * 0.95 + phase * (1 if k == 0 else -1) + off)
            y = int(round(yf))
            if lit:
                t.px(x, y, tc('plasma', 'hi') if k == 0 else tc('plasma', 'on'))
                for yy in (y - 1, y + 1):
                    if 11 <= yy <= 12 and t.img.getpixel((x, yy))[:3] in ((12, 10, 16), (52, 46, 62), (40, 36, 48)):
                        t.px(x, yy, shade(tc('plasma', 'on'), 0.45))
            elif k == 0:
                t.px(x, max(11, min(12, y)), shade(tc('plasma', 'off'), 0.8))


# ---------------------------------------------------------------- 4. FUSION: glowing core with orbiting particles
def tier_fusion(t, f, lit):
    well(t, 2, 9, 13, 14, (8, 14, 10))
    cx, cy = 7.5, 11.5
    # faint torus track
    for a in range(0, 360, 12):
        r = math.radians(a)
        x, y = int(round(cx + 4.6 * math.cos(r))), int(round(cy + 1.7 * math.sin(r)))
        t.px(x, y, (24, 46, 30) if lit else (20, 30, 24))
    # magnetic coils either side
    for y in (10, 11, 12, 13):
        t.px(3, y, COPPER[2] if y % 2 else COPPER[1])
        t.px(12, y, COPPER[2] if y % 2 else COPPER[1])
    # core
    for y in range(10, 14):
        for x in range(5, 11):
            d = math.hypot(x - cx, (y - cy) * 1.2)
            if d <= 1.7:
                if lit:
                    pulse = 0.85 + 0.15 * math.sin(f * TAU / FRAMES * 2)
                    t.px(x, y, mix(tc('fusion', 'on'), tc('fusion', 'hi'), max(0.0, 1 - d / 1.7) * pulse))
                else:
                    t.px(x, y, (40, 60, 46) if d < 1 else (30, 44, 34))
    if lit:  # three particles orbiting
        for k in range(3):
            a = f * TAU / FRAMES + k * TAU / 3
            x, y = int(round(cx + 4.6 * math.cos(a))), int(round(cy + 1.7 * math.sin(a)))
            t.px(x, y, tc('fusion', 'hi'))


# ---------------------------------------------------------------- 5. ANTIMATTER: speck suspended in a magnetic trap
def tier_antimatter(t, f, lit):
    well(t, 2, 9, 13, 14, (14, 18, 28) if lit else (10, 11, 16))
    # trap electrodes (top and bottom plates) + side posts
    for x in range(5, 11):
        t.px(x, 10, STEEL[4] if lit else STEEL[2])
        t.px(x, 13, STEEL[2] if lit else STEEL[1])
    for y in (11, 12):
        t.px(4, y, STEEL[1]); t.px(11, y, STEEL[1])
    if lit:
        # shimmering field lines sweeping across
        for x in range(5, 11):
            if (x + f) % 4 == 0:
                t.px(x, 11, shade(tc('antimatter', 'on'), 0.35))
            if (x - f) % 4 == 0:
                t.px(x, 12, shade(tc('antimatter', 'on'), 0.35))
        # the speck, flickering between two pixels with a halo
        sx = 7 + (1 if f % 4 in (1, 2) else 0)
        sy = 11 + (1 if f % 4 in (2, 3) else 0)
        for (hx, hy) in ((sx - 1, sy), (sx + 1, sy), (sx, sy - 1), (sx, sy + 1)):
            if 5 <= hx <= 10 and 11 <= hy <= 12:
                t.px(hx, hy, mix((120, 170, 255), tc('antimatter', 'on'), 0.5))
        t.px(sx, sy, tc('antimatter', 'hi'))
        # corner warning lights
        t.px(3, 10, tc('antimatter', 'hi') if f % 2 == 0 else tc('antimatter', 'off'))
        t.px(12, 10, tc('antimatter', 'hi') if f % 2 == 1 else tc('antimatter', 'off'))
    else:
        t.px(3, 10, tc('antimatter', 'off')); t.px(12, 10, tc('antimatter', 'off'))


# ---------------------------------------------------------------- 6. SINGULARITY: black hole with a gold accretion disc
def tier_singularity(t, f, lit):
    """Frameless 6-row black hole: round event horizon, photon ring, disc crossing in front."""
    ON, HI, OFF = tc('singularity', 'on'), tc('singularity', 'hi'), tc('singularity', 'off')
    rot = f * TAU / FRAMES
    t.rect(2, 9, 13, 14, VOID)
    for (x, y) in ((3, 9), (12, 14), (12, 10), (2, 13), (4, 14)):
        t.px(x, y, (150, 150, 170) if lit and (f + x + y) % 3 == 0 else (54, 54, 66))
    ring = shade(ON, 0.75) if lit else OFF
    ring_hi = HI if lit else shade(OFF, 1.2)
    # photon ring around a 4x4 black core (x6..9, y10..13), with arcs top (y9) and bottom (y14)
    for x in range(6, 10):
        t.px(x, 9, ring_hi if x in (7, 8) else ring)
        t.px(x, 14, ring)
    for y in (10, 13):
        t.px(5, y, ring); t.px(10, y, ring)
    for y in range(10, 14):
        for x in range(6, 10):
            t.px(x, y, (0, 0, 0))
    # far side of the accretion disc (hidden behind the hole in the middle)
    for x in list(range(2, 6)) + list(range(10, 14)):
        hot = 0.5 + 0.5 * math.cos((x - 7.5) / 6 * math.pi - rot)
        t.px(x, 11, mix(shade(ON, 0.35), shade(ON, 0.8), hot) if lit else shade(OFF, 0.55))
    # near side, crossing in front of the hole: the brightest band
    for x in range(2, 14):
        hot = 0.5 + 0.5 * math.cos((x - 7.5) / 6 * math.pi + rot)
        t.px(x, 12, mix(ON, HI, hot) if lit else shade(OFF, 0.75 + 0.3 * hot))
    if lit:  # matter spiralling in along the far side
        p = (f * 3) % 8
        x = 2 + p // 2 if f % 2 == 0 else 13 - p // 2
        t.px(x, 10, ORE[f % 4])


TIER_LIST = [
    ('1', 'Coal', 'coal', tier_coal_door),
    ('2', 'Electric', 'electric', tier_elec_meter),
    ('3', 'Plasma', 'plasma', tier_plasma),
    ('4', 'Fusion', 'fusion', tier_fusion),
    ('5', 'Antimatter', 'antimatter', tier_antimatter),
    ('6', 'Singularity', 'singularity', tier_singularity),
]

# ---------------------------------------------------------------- save textures
for num, name, key, fn in TIER_LIST:
    d = os.path.join(OUT, f'tier{num}_{key}')
    os.makedirs(d, exist_ok=True)
    top(key).img.save(os.path.join(d, 'granulator_top.png'))
    side(key).img.save(os.path.join(d, 'granulator_side.png'))
    front(act_conveyor, fn, key).img.save(os.path.join(d, 'granulator_front.png'))
    strip = Image.new('RGBA', (16, 16 * FRAMES))
    for k in range(FRAMES):
        strip.paste(front(act_conveyor, fn, key, k).img, (0, 16 * k))
    strip.save(os.path.join(d, 'granulator_front_on.png'))

# ---------------------------------------------------------------- preview sheet
font = ImageFont.load_default(size=20)
small = ImageFont.load_default(size=13)
BG = (38, 40, 48, 255)
CW = 165
W = CW * 6 + 20
sheet = Image.new('RGBA', (W, 470), BG)
dr = ImageDraw.Draw(sheet)
for i, (num, name, key, fn) in enumerate(TIER_LIST):
    x0 = 10 + i * CW
    on = TIERS[key]['on']
    dr.rectangle((x0 + 6, 12, x0 + 18, 24), fill=on)
    dr.text((x0 + 24, 8), f'{num}. {name}', font=font, fill=(235, 235, 240))
    sheet.alpha_composite(iso_block(top(key).img, front(act_conveyor, fn, key, 2).img, side(key).img, 150), (x0, 36))
    dr.text((x0 + 50, 186), 'running', font=small, fill=(170, 170, 180))
    sheet.alpha_composite(up(front(act_conveyor, fn, key).img, 4), (x0 + 10, 210))
    sheet.alpha_composite(up(front(act_conveyor, fn, key, 2).img, 4), (x0 + 84, 210))
    dr.text((x0 + 28, 276), 'off', font=small, fill=(170, 170, 180))
    dr.text((x0 + 104, 276), 'on', font=small, fill=(170, 170, 180))
    # bottom-half animation strip, zoomed
    for k in range(4):
        crop = front(act_conveyor, fn, key, k * 2).img.crop((0, 8, 16, 16))
        sheet.alpha_composite(up(crop, 4), (x0 + 10 + (k % 2) * 74, 300 + (k // 2) * 38))
    dr.text((x0 + 10, 378), 'tier half, 4 frames', font=small, fill=(170, 170, 180))
    sheet.alpha_composite(up(side(key).img, 3), (x0 + 10, 400 - 4))
    sheet.alpha_composite(up(top(key).img, 3), (x0 + 70, 400 - 4))
sheet.save(os.path.join(OUT, 'six_tiers.png'))

# ---------------------------------------------------------------- animated GIF: row of six running machines
gif = []
for k in range(FRAMES):
    g = Image.new('RGBA', (W, 300), BG)
    gd = ImageDraw.Draw(g)
    for i, (num, name, key, fn) in enumerate(TIER_LIST):
        x0 = 10 + i * CW
        gd.rectangle((x0 + 6, 12, x0 + 18, 24), fill=TIERS[key]['on'])
        gd.text((x0 + 24, 8), f'{num}. {name}', font=font, fill=(235, 235, 240))
        g.alpha_composite(iso_block(top(key).img, front(act_conveyor, fn, key, k).img, side(key).img, 150), (x0, 36))
        g.alpha_composite(up(front(act_conveyor, fn, key, k).img, 5), (x0 + 42, 196))
    gif.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
gif[0].save(os.path.join(OUT, 'six_tiers_running.gif'), save_all=True, append_images=gif[1:], duration=110, loop=0)
print('ok')
