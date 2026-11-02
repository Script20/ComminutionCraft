"""Dark Chassis machine system: top half of the front = what the machine does, bottom half = tier.
Three variants (A/B/C). Each shows: coal granulator, coal smelter, tier-2 (electric) granulator."""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

from mekstyle_lib import Tex, mix, shade, clamp, fire, iso_block, up, FRAMES, TAU, ORE


K = dict(o=(16, 17, 20), d=(28, 30, 35), m=(42, 45, 52), l=(60, 64, 73), hi=(84, 89, 100))
GLASS = (13, 14, 17)
TIERS = {
    'coal': dict(off=(86, 56, 34), on=(255, 150, 40), hi=(255, 214, 120)),
    'electric': dict(off=(30, 66, 86), on=(64, 196, 255), hi=(190, 240, 255)),
}
STEEL = [(70, 74, 82), (100, 104, 112), (130, 134, 142), (160, 164, 172), (196, 200, 208)]
MOLTEN = [(150, 50, 16), (226, 104, 28), (255, 168, 56), (255, 226, 140)]
COPPER = [(96, 50, 30), (150, 82, 48), (198, 118, 72), (232, 160, 112)]


def grad(t, top, bot, box=(0, 0, 15, 15)):
    x0, y0, x1, y1 = box
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            t.px(x, y, mix(top, bot, (y - y0) / max(1, y1 - y0)))


def casing():
    t = Tex()
    grad(t, K['l'], K['m'])
    t.hline(1, 14, 0, K['hi'])
    t.vline(0, 1, 14, K['l'])
    t.hline(1, 14, 15, K['o'])
    t.vline(15, 1, 14, K['d'])
    for (x, y) in ((0, 0), (15, 0), (0, 15), (15, 15)):
        t.px(x, y, K['o'])
    return t


def seam(t, pts, tier, lit, f):
    T = TIERS[tier]
    for (x, y) in pts:
        if lit:
            t.px(x, y, mix(T['on'], T['hi'], 0.2 + 0.3 * math.sin((f + x + y) * TAU / FRAMES)))
        else:
            t.px(x, y, T['off'])


def well(t, x0, y0, x1, y1, fill=GLASS):
    t.rect(x0, y0, x1, y1, fill)
    t.hline(x0, x1, y0, K['o'])
    t.vline(x0, y0, y1, K['o'])
    t.hline(x0, x1, y1, K['hi'])
    t.vline(x1, y0, y1, K['hi'])


def glass_glint(t, pts):
    for (x, y) in pts:
        t.px(x, y, (150, 160, 172))


def cylinder_row(y, top, bot, c_hi, c_lo):
    """Shade for a horizontal cylinder spanning rows top..bot."""
    mid = (top + bot) / 2
    k = 1 - abs(y - mid) / max(0.5, (bot - top) / 2 + 0.5)
    return mix(c_lo, c_hi, k)


# ---------------------------------------------------------------- shared faces
def side(tier):
    t = casing()
    well(t, 2, 2, 13, 6, K['m'])
    t.hline(4, 11, 4, K['d'])
    seam(t, [(x, 8) for x in range(1, 15)], tier, False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


def top(tier):
    t = casing()
    ring = []
    for y in range(2, 14):
        for x in range(2, 14):
            d = abs(x - 7.5) + abs(y - 7.5)
            if d <= 6:
                t.px(x, y, K['o'] if d < 4 else K['d'])
                if 5 <= d < 6:
                    ring.append((x, y))
    seam(t, ring, tier, False, 0)
    t.px(7, 7, ORE[1]); t.px(8, 8, ORE[0])
    return t


def front(action, tier_fn, tier, f=None):
    lit = f is not None
    f = f or 0
    t = casing()
    well(t, 1, 1, 14, 7)
    action(t, f, lit)
    seam(t, [(x, 8) for x in range(1, 15)], tier, lit, f)
    tier_fn(t, f, lit)
    return t


# ================================================================ ACTIONS (top half)
# --- A: screw roller in a horizontal glass tube
def act_screw(t, f, lit):
    for y in range(2, 7):
        for x in range(2, 14):
            base = cylinder_row(y, 2, 6, STEEL[3], STEEL[0])
            if (x + (y - 2) - (f if lit else 0)) % 4 < 2:
                base = shade(base, 0.62)
            t.px(x, y, base)
    if lit:
        for k in range(3):
            p = 2 + (f * 3 // 2 + k * 4) % 12
            if p < 8:
                t.px(p, 4, ORE[k % 3]); t.px(p, 3, ORE[k % 3])
            else:
                t.px(p, 5 - (k % 2), ORE[(k + 1) % 4])
    glass_glint(t, [(3, 2), (4, 2), (9, 2)])


def act_screw_smelt(t, f, lit):
    for y in range(2, 7):
        for x in range(2, 14):
            if y >= 4:
                c = MOLTEN[1] if y == 6 else MOLTEN[2]
                if lit and (x - f) % 5 == 0 and y == 4:
                    c = MOLTEN[3]
                if not lit:
                    c = (70, 52, 44) if y > 4 else (90, 66, 54)
            else:
                c = cylinder_row(y, 2, 6, (60, 50, 46), (30, 26, 24))
                if lit and y == 3 and (x + f) % 6 == 0:
                    c = (120, 70, 40)
            t.px(x, y, c)
    if lit:
        t.px(2 + f % 3, 3, ORE[0])
    glass_glint(t, [(3, 2), (4, 2), (9, 2)])


# --- B: big horizontal toothed roller, ore fed on top, grit spills below
def act_bigroller(t, f, lit):
    ff = f if lit else 0
    for y in range(3, 6):
        for x in range(2, 14):
            c = cylinder_row(y, 3, 5, STEEL[4], STEEL[1])
            if (x * 2 + y + ff) % 4 == 0:
                c = STEEL[0]
            t.px(x, y, c)
    for k in range(4):  # ore chunks on top travelling into the roller
        x = 2 + ((ff + k * 3) % 12)
        t.px(x, 2, ORE[k % 4] if lit or k < 2 else ORE[3])
    if lit:
        for k in range(4):  # grit falling out underneath
            x = 13 - ((f * 2 + k * 3) % 12)
            t.px(x, 6, shade(ORE[(k + 1) % 4], 0.9))
    glass_glint(t, [(12, 2)])


def act_crucible(t, f, lit):
    for y in range(2, 7):
        for x in range(2, 14):
            t.px(x, y, (22, 20, 20))
    # crucible bowl
    for y in range(3, 7):
        inset = max(0, y - 4)
        for x in range(3 + inset, 13 - inset):
            t.px(x, y, (92, 86, 82) if x in (3 + inset, 12 - inset) else (70, 64, 60))
    for x in range(4, 12):
        if lit:
            c = MOLTEN[2] if (x + f) % 4 else MOLTEN[3]
        else:
            c = (96, 74, 62)
        t.px(x, 4, c)
    if lit:
        bx = 5 + (f * 3) % 6  # bubble
        t.px(bx, 3, MOLTEN[3])
        t.px(4 + (f * 5) % 7, 2, shade(MOLTEN[1], 0.7))  # heat haze speck
    glass_glint(t, [(12, 2)])


# --- C: conveyor belt under a roller; chunks in, grit out
def act_conveyor(t, f, lit):
    ff = f if lit else 0
    for x in range(2, 14):
        t.px(x, 6, (50, 52, 58) if (x - ff) % 3 else (30, 32, 36))
    for y in range(2, 5):
        for x in range(5, 11):
            c = cylinder_row(y, 2, 4, STEEL[4], STEEL[1])
            if (x + ff) % 3 == 0:
                c = shade(c, 0.7)
            t.px(x, y, c)
    for y in range(2, 6):
        t.px(4, y, K['d']); t.px(11, y, K['d'])
    t.px(4, 3, STEEL[2]); t.px(11, 3, STEEL[2])
    for k in range(2):
        x = 2 + (ff // 2 + k * 2) % 3
        t.px(x, 5, ORE[k]); t.px(x, 4, ORE[k])
    for k in range(3):
        x = 11 + (ff // 2 + k) % 3
        t.px(x, 5, ORE[(k + 2) % 4])


def act_conveyor_smelt(t, f, lit):
    ff = f if lit else 0
    for x in range(2, 14):
        t.px(x, 6, (50, 52, 58) if (x - ff) % 3 else (30, 32, 36))
    for x in range(5, 11):  # heating element
        for y in (2, 3):
            if lit:
                t.px(x, y, fire(0.05 + 0.3 * (y - 2) + 0.15 * ((x + f) % 2)))
            else:
                t.px(x, y, (70, 46, 38))
    for y in range(2, 6):
        t.px(4, y, K['d']); t.px(11, y, K['d'])
    if lit:
        for x in range(5, 11):
            t.px(x, 4, (90, 44, 24) if (x + f) % 2 else (60, 30, 20))
    for k in range(2):
        t.px(2 + (ff // 2 + k * 2) % 3, 5, ORE[k])
    for k in range(2):  # ingots out
        x = 11 + ((ff // 2 + k * 2) % 3)
        t.px(x, 5, (236, 236, 240) if k == 0 else (246, 208, 80))


# ================================================================ TIERS (bottom half)
def tier_coal_grate(t, f, lit):
    well(t, 2, 9, 13, 14, (14, 12, 12))
    for x in range(3, 13):
        if x % 2 == 0:
            for y in range(10, 14):
                t.px(x, y, K['l'] if y == 10 else K['d'])
        else:
            h = (2.4 + 1.2 * math.sin(f * TAU / FRAMES * 2 + x * 1.9)) if lit else 0
            for y in range(13, 9, -1):
                k = 13 - y
                if lit and k < h:
                    t.px(x, y, fire(k / max(h, 1) * 0.9))
                elif y == 13:
                    t.px(x, y, (56, 52, 52) if not lit else (255, 120, 40))


def tier_coal_hearth(t, f, lit):
    well(t, 2, 9, 13, 14, (14, 12, 12))
    for x in range(3, 13):
        lump = (x * 7) % 3
        t.px(x, 13, (40, 38, 38) if lump else (28, 26, 26))
        t.px(x, 12, (34, 32, 32) if (x * 5) % 4 == 0 else (14, 12, 12))
        if lit:
            if (x + f) % 3 == 0:
                t.px(x, 13, (255, 140, 40))
            if (x * 5) % 4 == 0:
                t.px(x, 12, (220, 90, 30))
            h = 1.6 + 1.4 * math.sin(f * TAU / FRAMES * 2 + x * 2.3)
            for k in range(int(h) + 1):
                y = 11 - k
                if y >= 10:
                    t.px(x, y, fire(0.25 + k * 0.3))


def tier_coal_door(t, f, lit):
    t.rect(3, 9, 12, 14, K['l'])
    t.hline(3, 12, 9, K['hi']); t.vline(3, 9, 14, K['hi'])
    t.hline(3, 12, 14, K['o']); t.vline(12, 9, 14, K['o'])
    for (x, y) in ((4, 10), (11, 10), (4, 13), (11, 13)):
        t.px(x, y, K['d'])
    for y in (11, 12):
        for x in range(6, 10):
            if lit:
                t.px(x, y, fire(0.1 + 0.35 * (y - 11) + 0.2 * ((x + f) % 3 == 0)))
            else:
                t.px(x, y, (18, 16, 16))
    t.hline(6, 9, 10, K['o'])
    if lit:  # embers drifting up past the door
        t.px(13, 14 - (f % 5), (255, 170, 60))
        t.px(2, 14 - ((f + 3) % 5), (255, 120, 40))


def tier_elec_arc(t, f, lit):
    well(t, 2, 9, 13, 14, (12, 14, 18))
    for y in range(10, 14):
        t.px(3, y, STEEL[2] if y > 10 else STEEL[4])
        t.px(12, y, STEEL[2] if y > 10 else STEEL[4])
    if lit:
        y = 11
        pts = []
        for x in range(4, 12):
            y = max(10, min(13, y + ((x * 7 + f * 3) % 3) - 1))
            pts.append((x, y))
        for (x, yy) in pts:
            t.px(x, yy, TIERS['electric']['hi'])
            if yy + 1 <= 13:
                t.px(x, yy + 1, shade(TIERS['electric']['on'], 0.6))


def tier_elec_coil(t, f, lit):
    well(t, 2, 9, 13, 14, (12, 14, 18))
    t.hline(3, 12, 12, STEEL[1])  # core rod
    for x in range(4, 12):
        for y in range(10, 14):
            wind = x % 2 == 0
            c = [COPPER[3], COPPER[2], COPPER[1], COPPER[0]][y - 10] if wind else (30, 24, 22)
            if lit and wind and abs(x - (4 + (f * 8 // FRAMES) % 8)) <= 1:
                c = mix(c, TIERS['electric']['hi'], 0.75 if y < 12 else 0.5)
            t.px(x, y, c)
    for y in range(10, 14):
        t.px(3, y, STEEL[3] if y == 10 else STEEL[1])
        t.px(12, y, STEEL[3] if y == 10 else STEEL[1])
    if lit:
        t.px(3, 9 + (f % 2), TIERS['electric']['hi'])
        t.px(12, 9 + ((f + 1) % 2), TIERS['electric']['hi'])


def tier_elec_meter(t, f, lit):
    well(t, 2, 9, 13, 14, (12, 14, 18))
    level = (f % FRAMES) + 1 if lit else 0
    for i in range(5):
        x = 3 + i * 2
        on = lit and i < (level * 5 // FRAMES + 1)
        for y in range(10, 14):
            t.px(x, y, TIERS['electric']['on'] if on else TIERS['electric']['off'])
            t.px(x + 1, y, shade(TIERS['electric']['on'], 0.7) if on else shade(TIERS['electric']['off'], 0.7))
        if on:
            t.px(x, 10, TIERS['electric']['hi'])


VARIANTS = [
    ('A', 'Screw Roller / Grate', act_screw, act_screw_smelt, tier_coal_grate, tier_elec_arc),
    ('B', 'Big Roller / Hearth', act_bigroller, act_crucible, tier_coal_hearth, tier_elec_coil),
    ('C', 'Conveyor / Fire Door', act_conveyor, act_conveyor_smelt, tier_coal_door, tier_elec_meter),
]

# ================================================================ render
def render_preview(OUT):
    """Preview sheet + gif of all three variants (python tools/dark_chassis_system.py <out_dir>)."""
    os.makedirs(OUT, exist_ok=True)
    font = ImageFont.load_default(size=21)
    small = ImageFont.load_default(size=14)
    BG = (38, 40, 48, 255)
    COL_W = 310
    sheet = Image.new('RGBA', (COL_W * 3 + 20, 600), BG)
    dr = ImageDraw.Draw(sheet)
    gif = [Image.new('RGBA', (COL_W * 3 + 20, 360), BG) for _ in range(FRAMES)]

    for i, (key, name, act_g, act_s, tc, te) in enumerate(VARIANTS):
        x0 = 10 + i * COL_W
        builds = {
            'granulator': (top('coal'), side('coal'), lambda k=None: front(act_g, tc, 'coal', k)),
            'smelter': (top('coal'), side('coal'), lambda k=None: front(act_s, tc, 'coal', k)),
            'granulator T2': (top('electric'), side('electric'), lambda k=None: front(act_g, te, 'electric', k)),
        }
        folder = os.path.join(OUT, f'{key}')
        os.makedirs(folder, exist_ok=True)
        for bname, (tp, sd, fr) in builds.items():
            stem = bname.replace(' ', '_').lower()
            tp.img.save(os.path.join(folder, f'{stem}_top.png'))
            sd.img.save(os.path.join(folder, f'{stem}_side.png'))
            fr().img.save(os.path.join(folder, f'{stem}_front.png'))
            strip = Image.new('RGBA', (16, 16 * FRAMES))
            for k in range(FRAMES):
                strip.paste(fr(k).img, (0, 16 * k))
            strip.save(os.path.join(folder, f'{stem}_front_on.png'))

        dr.text((x0 + 8, 10), f'{key}. {name}', font=font, fill=(235, 235, 240))
        # three running blocks
        for j, (bname, (tp, sd, fr)) in enumerate(builds.items()):
            sheet.alpha_composite(iso_block(tp.img, fr(2).img, sd.img, 100), (x0 + j * 100, 42))
            dr.text((x0 + 8 + j * 100, 145), bname, font=small, fill=(170, 170, 180))
        # flat fronts
        tp, sd, fr = builds['granulator']
        flats = [('off', fr().img), ('on', fr(2).img), ('smelter', builds['smelter'][2](3).img),
                 ('T2 on', builds['granulator T2'][2](2).img)]
        for k, (lab, im) in enumerate(flats):
            sheet.alpha_composite(up(im, 4), (x0 + 6 + k * 74, 172))
            dr.text((x0 + 8 + k * 74, 240), lab, font=small, fill=(170, 170, 180))
        for k in range(FRAMES):
            sheet.alpha_composite(up(fr(k).img, 2), (x0 + 6 + (k % 4) * 74, 266 + (k // 4) * 38))
        dr.text((x0 + 8, 342), 'granulator frames', font=small, fill=(170, 170, 180))
        # wall: casing row + machines in place
        for gx in range(3):
            sheet.alpha_composite(up(sd.img, 4), (x0 + 30 + gx * 70, 370))
        sheet.alpha_composite(up(fr(2).img, 4), (x0 + 30, 440))
        sheet.alpha_composite(up(builds['smelter'][2](4).img, 4), (x0 + 100, 440))
        sheet.alpha_composite(up(sd.img, 4), (x0 + 170, 440))
        dr.text((x0 + 8, 512), 'in a wall of casing', font=small, fill=(170, 170, 180))

        for k in range(FRAMES):
            g = gif[k]
            ImageDraw.Draw(g).text((x0 + 8, 10), f'{key}. {name}', font=font, fill=(235, 235, 240))
            g.alpha_composite(iso_block(tp.img, fr(k).img, sd.img, 190), (x0 + 55, 36))
            g.alpha_composite(iso_block(top('coal').img, builds['smelter'][2](k).img, side('coal').img, 120), (x0 + 20, 228))
            g.alpha_composite(iso_block(top('electric').img, builds['granulator T2'][2](k).img, side('electric').img, 120), (x0 + 160, 228))
            ImageDraw.Draw(g).text((x0 + 45, 340), 'smelter', font=small, fill=(170, 170, 180))
            ImageDraw.Draw(g).text((x0 + 175, 340), 'tier 2 granulator', font=small, fill=(170, 170, 180))

    sheet = sheet.crop((0, 0, sheet.width, 540))
    sheet.save(os.path.join(OUT, 'dark_chassis_system.png'))
    pal = [g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255) for g in gif]
    pal[0].save(os.path.join(OUT, 'dark_chassis_system_running.gif'), save_all=True, append_images=pal[1:], duration=110, loop=0)
    print('ok')


if __name__ == '__main__':
    render_preview(sys.argv[1])
