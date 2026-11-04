"""Phase D preview (tier-3 power chain): Atmospheric Water Generator -> Electrolyser -> Plasma Generator (hydrogen)
-> MHD Generator, plus the Hydrogen and Oxygen fluids. Functions here move into tools/gen_tier3_textures.py once approved.

Usage: tools/.venv/Scripts/python.exe design/previews/phase_d_preview.py <out.png> <out.gif>
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

TOOLS = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'tools')
sys.path.insert(0, TOOLS)
from mekstyle_lib import Tex, mix, shade, iso_block, up, FRAMES, TAU, ORE  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import K, GLASS, STEEL, COPPER, casing, seam, well, front, side, top, cylinder_row  # noqa: E402
import gen_tier3_textures as t3  # noqa: E402

P = dcs.TIERS['plasma']
tier_plasma = t3.tier_plasma
WATER = t3.WATER
HYDROGEN = [(150, 150, 176), (196, 198, 224), (228, 230, 248), (250, 250, 255)]  # pale, almost white
OXYGEN = [(60, 120, 190), (110, 170, 230), (160, 206, 248), (214, 236, 255)]     # liquid-oxygen blue
FROST = [(120, 150, 170), (170, 200, 220), (220, 240, 250)]
MAG_N = (196, 56, 56)
MAG_S = (56, 96, 196)


# ================================================================ Atmospheric Water Generator
def act_condenser(t, f, lit):
    """Top half: a frosty cooling coil; moisture condenses on it and drips into a water tray."""
    ff = f if lit else 0
    for y in (2, 4):  # two coil runs, joined at alternating ends
        for x in range(2, 14):
            c = FROST[1] if (x + y) % 3 else FROST[2]
            t.px(x, y, c if lit else shade(c, 0.7))
    t.px(13, 3, FROST[1] if lit else shade(FROST[1], 0.7))
    t.px(2, 5, FROST[0])
    for x in range(3, 13):  # tray
        t.px(x, 6, WATER[1] if lit else WATER[0])
    t.px(2, 6, STEEL[1]); t.px(13, 6, STEEL[1])
    if lit:  # drops forming under the coils and falling into the tray
        for k, x in enumerate((4, 7, 10, 12)):
            y = 3 + ((ff + k * 3) % 4) // 2 * 2  # 3 or 5, below a coil run
            if t.img.getpixel((x, y))[:3] == GLASS:
                t.px(x, y, WATER[3])
        t.px(3 + (ff * 3) % 10, 6, WATER[3])  # a ripple running along the tray


def awg_front(f=None):
    return front(act_condenser, tier_plasma, 'plasma', f)


# The Plasma Generator's intake louvres and fan suit the water generator (it is the one that breathes now).
awg_side = t3.generator_side
awg_top = t3.generator_top


# ================================================================ Electrolyser
def act_electrolysis(t, f, lit):
    """Top half: two electrodes in a water bath; hydrogen bubbles off one, half as much oxygen off the other."""
    ff = f if lit else 0
    for y in range(3, 7):  # bath
        for x in range(2, 14):
            t.px(x, y, WATER[2] if y == 3 else (WATER[1] if y < 6 else WATER[0]))
    for y in range(2, 7):  # electrodes: copper cathode (hydrogen), steel anode (oxygen)
        t.px(5, y, COPPER[2] if y == 2 else COPPER[1])
        t.px(10, y, STEEL[3] if y == 2 else STEEL[2])
    t.hline(4, 6, 2, COPPER[3] if lit else COPPER[1])
    t.hline(9, 11, 2, STEEL[4] if lit else STEEL[2])
    if lit:
        for k in range(4):  # hydrogen: twice the bubbles
            x = 4 if k % 2 == 0 else 6
            y = 6 - (ff + k * 2) % 4
            t.px(x, y, HYDROGEN[3])
        for k in range(2):  # oxygen
            x = 9 if k == 0 else 11
            y = 6 - (ff + k * 3) % 4
            t.px(x, y, OXYGEN[3])
        t.px(7, 3, WATER[3]); t.px(8, 3, WATER[3])


def electrolyser_front(f=None):
    return front(act_electrolysis, tier_plasma, 'plasma', f)


def port(t, cx, cy, ring, core):
    """A round pipe port (5x5) with a coloured ring."""
    for y in range(cy - 2, cy + 3):
        for x in range(cx - 2, cx + 3):
            d = math.hypot(x - cx, y - cy)
            if d <= 2.3:
                t.px(x, y, ring if d > 1.2 else core)
    t.px(cx - 1, cy - 1, shade(ring, 1.3))


def electrolyser_side():
    """Two gas outlets above the tier seam (hydrogen white, oxygen blue), a grille below."""
    t = casing()
    well(t, 2, 2, 13, 6, K['d'])
    port(t, 5, 4, HYDROGEN[2], (14, 16, 20))
    port(t, 10, 4, OXYGEN[1], (14, 16, 20))
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


def electrolyser_top():
    return top('plasma')


# ================================================================ Plasma Generator (now fed hydrogen)
def act_ionizer_h2(t, f, lit):
    """Top half: hydrogen in through a nozzle (left), superheated between two electrodes, plasma out (right)."""
    t.rect(2, 3, 3, 5, STEEL[1])  # inlet nozzle
    t.px(2, 3, STEEL[3]); t.px(3, 5, STEEL[0])
    t.px(2, 4, HYDROGEN[2] if lit else HYDROGEN[0])
    for y in range(2, 7):  # electrodes
        t.px(6, y, COPPER[2] if y in (2, 6) else COPPER[1])
        t.px(9, y, COPPER[2] if y in (2, 6) else COPPER[1])
    for y in range(3, 6):  # the hot zone
        for x in (7, 8):
            if lit:
                k = 0.5 + 0.5 * math.sin(f * TAU / FRAMES * 2 + y + x)
                t.px(x, y, mix(P['on'], P['hi'], k * (0.9 if y == 4 else 0.5)))
            else:
                t.px(x, y, shade(P['off'], 0.7 if y == 4 else 0.5))
    t.rect(12, 3, 13, 5, STEEL[1])  # outlet nozzle
    t.px(12, 3, STEEL[3]); t.px(13, 5, STEEL[0])
    t.px(13, 4, P['on'] if lit else P['off'])
    if lit:  # hydrogen specks drifting in, turning to plasma between the electrodes
        for lane, y in enumerate((3, 4, 5)):
            x = 4 + (f + lane * 3) % 8
            if x in (6, 9):
                continue
            c = HYDROGEN[3] if x < 6 else (P['hi'] if x < 9 else P['on'])
            if t.img.getpixel((x, y))[:3] == GLASS or x > 9 or x < 6:
                t.px(x, y, c)


def plasma_generator_front(f=None):
    return front(act_ionizer_h2, tier_plasma, 'plasma', f)


def plasma_generator_side():
    """A hydrogen inlet port above the tier seam, a grille below (no more intake louvres)."""
    t = casing()
    well(t, 4, 2, 11, 6, K['d'])
    port(t, 7, 4, HYDROGEN[2], (14, 16, 20))
    t.px(8, 4, (14, 16, 20))
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


def plasma_generator_top():
    return top('plasma')


# ================================================================ MHD Generator
def act_mhd(t, f, lit):
    """Top half: plasma rushing through a channel between two magnet poles; current sparks off the side electrodes."""
    ff = f if lit else 0
    for x in range(3, 13):  # magnet poles (north above, south below)
        t.px(x, 2, MAG_N if lit else shade(MAG_N, 0.6))
        t.px(x, 6, MAG_S if lit else shade(MAG_S, 0.6))
    t.px(3, 2, shade(MAG_N, 1.25)); t.px(3, 6, shade(MAG_S, 1.25))
    for x in range(2, 14):  # channel
        for y in (3, 4, 5):
            if lit:
                k = 0.5 + 0.5 * math.sin((x - ff * 2) * 0.9 + y * 1.3)
                c = mix(P['off'], P['on'], k) if y != 4 else mix(P['on'], P['hi'], k)
            else:
                c = shade(P['off'], 0.55 if y != 4 else 0.8)
            t.px(x, y, c)
    for y in (3, 5):  # electrodes at the ends
        t.px(2, y, COPPER[2]); t.px(13, y, COPPER[2])
    if lit:
        sx = 3 + (ff * 5) % 10
        t.px(sx, 3 if ff % 2 else 5, (255, 246, 190))


def mhd_front(f=None):
    return front(act_mhd, tier_plasma, 'plasma', f)


def mhd_side():
    """Two big magnet coils (copper windings round a steel core), the tier seam between them."""
    t = casing()
    for (y0, y1) in ((2, 6), (10, 13)):
        for y in range(y0, y1 + 1):
            for x in range(2, 14):
                c = cylinder_row(y, y0, y1, COPPER[3], COPPER[0])
                if x % 2 == 0:
                    c = shade(c, 0.82)
                t.px(x, y, c)
        t.vline(2, y0, y1, STEEL[2]); t.vline(13, y0, y1, STEEL[0])
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', False, 0)
    return t


def mhd_top():
    """FE terminals: copper bus bars inside the tier ring."""
    t = top('plasma')
    for x in range(5, 11):
        t.px(x, 6, COPPER[2]); t.px(x, 9, COPPER[2])
    t.px(5, 6, COPPER[3]); t.px(5, 9, COPPER[3])
    return t


# ================================================================ fluids
def gas_still(palette, f):
    """Soft drifting wisps (gases are thinner than plasma: low contrast, slow)."""
    t = Tex()
    w = TAU / 16
    ph = f * TAU / FRAMES
    for y in range(16):
        for x in range(16):
            v = (math.sin(x * w + ph) + math.sin((x + y) * w * 2 - ph) + 0.6 * math.sin(y * w * 3 + ph)) / 2.6
            v = 0.5 + 0.5 * v
            if v < 0.6:
                c = mix(palette[0], palette[1], v / 0.6)
            elif v < 0.88:
                c = mix(palette[1], palette[2], (v - 0.6) / 0.28)
            else:
                c = mix(palette[2], palette[3], (v - 0.88) / 0.12)
            t.px(x, y, c)
    return t


def hydrogen_still(f):
    return gas_still(HYDROGEN, f)


def oxygen_still(f):
    return gas_still(OXYGEN, f)


def tank_with(fluid_fn, fill, f):
    """The Fluid Tank as the renderer shows it, holding this fluid."""
    t = t3.tank_side_cutout()
    top_y = 14 - int(round(fill * 13))
    fl = fluid_fn(f).img
    for y in range(max(1, top_y), 15):
        for x in range(3, 13):
            t.px(x, y, fl.getpixel((x, y))[:3])
    return t


# ================================================================ sheet
BLOCKS = [
    ('Atmospheric Water Generator', 'air on every open face -> water', awg_top, awg_front, awg_side),
    ('Electrolyser', '2 water + FE -> 1 hydrogen (+ oxygen)', electrolyser_top, electrolyser_front, electrolyser_side),
    ('Plasma Generator (reworked)', 'hydrogen + FE -> plasma', plasma_generator_top, plasma_generator_front, plasma_generator_side),
    ('MHD Generator', 'plasma -> FE', mhd_top, mhd_front, mhd_side),
]


def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    W, H = 1320, 1010
    sheet = Image.new('RGBA', (W, H), BG)
    dr = ImageDraw.Draw(sheet)
    label = (170, 170, 180)
    head = (235, 235, 240)

    dr.text((20, 14), 'Tier-3 power chain: water from the air -> hydrogen -> plasma -> FE (off / running)', font=font, fill=head)
    for i, (name, note, top_fn, front_fn, side_fn) in enumerate(BLOCKS):
        x = 20 + i * 325
        tp, sd = top_fn().img, side_fn().img
        off, on = front_fn().img, front_fn(3).img
        sheet.alpha_composite(iso_block(tp, off, sd, 150), (x - 5, 50))
        sheet.alpha_composite(iso_block(tp, on, sd, 150), (x + 150, 50))
        dr.text((x, 205), name, font=small, fill=head)
        dr.text((x, 224), note, font=small, fill=label)
        sheet.alpha_composite(up(off, 4), (x, 252))
        sheet.alpha_composite(up(on, 4), (x + 72, 252))
        sheet.alpha_composite(up(sd, 4), (x + 144, 252))
        sheet.alpha_composite(up(tp, 4), (x + 216, 252))
        dr.text((x, 320), 'front      on       side       top', font=small, fill=label)
        for k in range(FRAMES):
            sheet.alpha_composite(up(front_fn(k).img, 2), (x + (k % 4) * 36, 346 + (k // 4) * 36))

    # old vs new Plasma Generator
    y0 = 450
    dr.text((20, y0), 'Plasma Generator: before -> after', font=font, fill=head)
    old = iso_block(t3.generator_top().img, t3.generator_front(3).img, t3.generator_side().img, 150)
    new = iso_block(plasma_generator_top().img, plasma_generator_front(3).img, plasma_generator_side().img, 150)
    sheet.alpha_composite(old, (20, y0 + 34))
    sheet.alpha_composite(new, (200, y0 + 34))
    dr.text((45, y0 + 190), 'breathes air (now)', font=small, fill=label)
    dr.text((215, y0 + 190), 'hydrogen inlet', font=small, fill=label)
    dr.text((20, y0 + 215), 'Its louvres and fan move to the water generator.', font=small, fill=label)

    # fluids
    dr.text((420, y0), 'Hydrogen and Oxygen (still, 8 frames) and in a Fluid Tank', font=font, fill=head)
    for k in range(FRAMES):
        sheet.alpha_composite(up(hydrogen_still(k).img, 3), (420 + k * 52, y0 + 40))
        sheet.alpha_composite(up(oxygen_still(k).img, 3), (420 + k * 52, y0 + 100))
    dr.text((420, y0 + 152), 'hydrogen (top), oxygen (bottom); water uses the vanilla texture', font=small, fill=label)
    ttop = t3.tank_top().img
    for k, (fn, nm) in enumerate(((hydrogen_still, 'hydrogen'), (oxygen_still, 'oxygen'), (t3.plasma_still, 'plasma'))):
        sd = tank_with(fn, 0.65, 2).img
        sheet.alpha_composite(iso_block(ttop, sd, sd, 120), (850 + k * 150, y0 + 30))
        dr.text((880 + k * 150, y0 + 155), nm, font=small, fill=label)

    # the chain
    y1 = 720
    dr.text((20, y1), 'The chain (pipes between each block)', font=font, fill=head)
    chain = [iso_block(awg_top().img, awg_front(3).img, awg_side().img, 140),
             iso_block(electrolyser_top().img, electrolyser_front(3).img, electrolyser_side().img, 140),
             iso_block(plasma_generator_top().img, plasma_generator_front(3).img, plasma_generator_side().img, 140),
             iso_block(mhd_top().img, mhd_front(3).img, mhd_side().img, 140)]
    pipe = t3.pipe_sheet().crop((0, 8, 16, 12))
    for k, im in enumerate(chain):
        x = 20 + k * 210
        sheet.alpha_composite(im, (x, y1 + 40))
        if k < 3:
            sheet.alpha_composite(up(pipe, 4), (x + 140, y1 + 110))
    for k, txt in enumerate(('water', 'hydrogen', 'plasma')):
        dr.text((20 + k * 210 + 150, y1 + 135), txt, font=small, fill=label)
    dr.text((900, y1 + 60), 'Oxygen leaves the Electrolyser through', font=small, fill=label)
    dr.text((900, y1 + 80), 'the same Output faces into its own tank;', font=small, fill=label)
    dr.text((900, y1 + 100), 'with nowhere to go it is vented.', font=small, fill=label)
    dr.text((900, y1 + 130), 'GUI mock-ups come with the code.', font=small, fill=label)
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (1000, 230), BG)
        for i, (name, note, top_fn, front_fn, side_fn) in enumerate(BLOCKS):
            g.alpha_composite(iso_block(top_fn().img, front_fn(k).img, side_fn().img, 230), (i * 245, 0))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
