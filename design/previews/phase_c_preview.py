"""Phase C preview: Plasma Granulator / Smelter, Circuit Assembler, Ore Washer, Plasma Circuit, Plasma Cable,
Plasma Energy Battery. Draws from the tool helpers so the approved functions can move into gen_tier3_textures.py."""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

TOOLS = r'C:\Users\Script\Desktop\comminutioncraft\tools'
sys.path.insert(0, TOOLS)
from mekstyle_lib import Tex, mix, shade, iso_block, up, FRAMES, TAU, ORE  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import (K, GLASS, STEEL, COPPER, casing, seam, well, front, side, top,  # noqa: E402
                                 act_conveyor, act_conveyor_smelt, tier_elec_meter, cylinder_row,
                                 circuit, battery_front, battery_side, battery_top, BATTERY_CELLS)
import gen_tier3_textures as t3  # noqa: E402  (registers the plasma tier, gives tier_plasma)

P = dcs.TIERS['plasma']
E = dcs.TIERS['electric']
tier_plasma = t3.tier_plasma
GOLD = [(120, 84, 20), (180, 132, 36), (232, 186, 64), (255, 230, 130)]
WATER = [(24, 52, 120), (40, 92, 186), (86, 150, 228), (190, 226, 250)]


# ================================================================ Circuit Assembler (top half)
def act_assembler(t, f, lit):
    """A press head over the belt: Electronic Circuit (blue chip) in, a plasma-fed stamp, Plasma Circuit (magenta) out."""
    ff = f if lit else 0
    for x in range(2, 14):  # belt
        t.px(x, 6, (50, 52, 58) if (x - ff) % 3 else (30, 32, 36))
    t.rect(5, 2, 10, 2, STEEL[1])  # gantry + plasma feed line
    t.hline(5, 10, 2, STEEL[2])
    t.px(7, 2, P['on'] if lit else P['off']); t.px(8, 2, P['on'] if lit else P['off'])
    stroke = (0, 0, 1, 1, 1, 0, 0, 0)[ff % FRAMES] if lit else 0  # press comes down, holds, lifts
    for y in range(3, 4 + stroke):  # piston
        t.px(7, y, STEEL[3]); t.px(8, y, STEEL[2])
    hy = 4 + stroke
    t.hline(6, 9, hy, STEEL[3]); t.px(6, hy, STEEL[4]); t.px(9, hy, STEEL[1])  # stamp
    if lit and stroke:
        t.px(7, hy + 1 if hy < 5 else hy, P['hi'])  # plasma flash under the stamp
        t.px(5, 5, shade(P['on'], 0.6)); t.px(10, 5, shade(P['on'], 0.6))
    t.vline(4, 3, 5, K['d']); t.vline(11, 3, 5, K['d'])  # gantry legs
    # boards on the belt: blue chips in from the left, magenta chips out on the right
    bx = 2 + (ff // 2) % 2
    t.hline(bx, bx + 1, 5, K['hi']); t.px(bx, 5, E['on'])
    ox = 12 + (ff // 2) % 2
    if ox < 14:
        t.hline(ox, min(13, ox + 1), 5, K['hi']); t.px(ox, 5, P['on'] if lit else P['off'])


# ================================================================ Ore Washer (top half)
def act_washer(t, f, lit):
    """A slotted drum turning under spray nozzles; water falls through, clean grit drops out the bottom right."""
    ff = f if lit else 0
    for x in range(3, 13):  # spray bar
        t.px(x, 2, STEEL[1] if x % 3 else STEEL[3])
    for y in range(3, 7):  # drum
        for x in range(4, 12):
            c = cylinder_row(y, 3, 6, STEEL[3], STEEL[0])
            if (x + ff) % 3 == 0:
                c = shade(c, 0.55)  # slats turning
            t.px(x, y, c)
    for (x, y) in ((5, 4), (8, 5), (10, 4)):  # ore inside the drum
        xx = 4 + (x - 4 + ff) % 8
        t.px(xx, y, ORE[(x + y) % 3])
    if lit:  # water drops from every nozzle, splashing on the drum
        for k, x in enumerate((3, 6, 9, 12)):
            y = 3 + (ff + k * 2) % 4
            if t.img.getpixel((x, y))[:3] == GLASS or x in (3, 12):
                t.px(x, y, WATER[2])
            t.px(x, 3, WATER[3])
        t.px(4 + ff % 8, 6, WATER[3])  # spray off the bottom of the drum
    else:
        for x in (3, 6, 9, 12):
            t.px(x, 3, WATER[0])
    t.vline(2, 3, 6, WATER[1] if lit else WATER[0])  # water in (left), level glows
    t.px(13, 6, ORE[(ff // 2) % 3])  # clean grit out


def washer_side():
    """Water inlet port above the seam (blue-ringed), grille below: shows where pipes and tanks connect."""
    t = casing()
    well(t, 4, 2, 11, 6, K['d'])
    for x in range(5, 11):
        t.px(x, 3, WATER[0]); t.px(x, 5, WATER[0])
    t.px(5, 4, WATER[0]); t.px(10, 4, WATER[0])
    t.rect(6, 4, 9, 4, (14, 16, 20))
    t.px(7, 4, WATER[2]); t.px(8, 4, WATER[1])
    seam(t, [(x, 8) for x in range(1, 15)], 'plasma', False, 0)
    t.rect(2, 10, 13, 13, K['m'])
    t.hline(2, 13, 10, K['hi']); t.hline(2, 13, 13, K['o'])
    for x in range(4, 12, 2):
        t.vline(x, 11, 12, K['d'])
    return t


# ================================================================ Plasma Cable (same UVs as the energy cable)
def plasma_cable_sheet():
    """Like the energy cable but magenta, with steel bands on the arm and a double ring on the plate (heavier duty)."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))

    def px(x, y, c):
        img.putpixel((x, y), tuple(c[:3]) + (255,))

    for y in range(6):
        for x in range(6):
            edge = x in (0, 5) or y in (0, 5)
            px(x, y, K['o'] if edge else mix(K['l'], K['m'], y / 5))
    for (x, y) in ((2, 2), (3, 2), (2, 3), (3, 3)):
        px(x, y, P['hi'] if (x, y) == (2, 2) else P['on'])
    for (x, y) in ((1, 1), (4, 1), (1, 4), (4, 4)):
        px(x, y, STEEL[2])
    for y in range(8):
        for x in range(8):
            c = mix(K['l'], K['d'], y / 7)
            if y == 0 or x == 0:
                c = K['hi']
            if y == 7 or x == 7:
                c = K['o']
            px(8 + x, y, c)
    for y in range(1, 7):
        for x in range(1, 7):
            outer = x in (1, 6) or y in (1, 6)
            inner = x in (2, 5) or y in (2, 5)
            if outer:
                px(8 + x, y, STEEL[1])
            elif inner:
                px(8 + x, y, shade(P['on'], 0.8))
            else:
                px(8 + x, y, (14, 10, 16))
    px(8 + 3, 3, P['hi'])
    rows = [K['l'], P['on'], shade(P['on'], 0.62), K['o']]
    for x in range(16):
        for y, c in enumerate(rows):
            if x % 8 in (0, 1):
                c = STEEL[3] if y == 0 else (STEEL[2] if y < 3 else STEEL[0])  # steel bands
            elif y == 1 and x % 8 == 4:
                c = P['hi']
            px(x, 8 + y, c)
    return img


def cable_run(sheet, length=3):
    W = 16 * length
    img = Image.new('RGBA', (W, 8), (0, 0, 0, 0))
    arm = sheet.crop((0, 8, 16, 12))
    for k in range(length):
        img.paste(arm, (16 * k, 2))
    plate = sheet.crop((8, 0, 9, 8))
    img.paste(plate, (0, 0)); img.paste(plate, (W - 1, 0))
    return img


def energy_cable_sheet():
    return Image.open(os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft',
                                   'textures', 'block', 'energy_cable.png')).convert('RGBA')


# ================================================================ sheet
MACHINES = [
    ('Plasma Granulator', act_conveyor, 'faster, still 2x'),
    ('Plasma Smelter', act_conveyor_smelt, 'faster'),
    ('Circuit Assembler', act_assembler, 'Electronic -> Plasma Circuit'),
    ('Ore Washer', act_washer, 'ore + water + plasma -> 3x'),
]


def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    W, H = 1320, 1260
    sheet = Image.new('RGBA', (W, H), BG)
    dr = ImageDraw.Draw(sheet)
    label = (170, 170, 180)
    head = (235, 235, 240)

    ptop, pside = top('plasma').img, side('plasma').img
    dr.text((20, 14), 'Tier-3 machines: top half = the job, bottom half = the plasma tube (off / running)', font=font, fill=head)
    for i, (name, act, note) in enumerate(MACHINES):
        x = 20 + i * 325
        s = washer_side().img if name == 'Ore Washer' else pside
        off = front(act, tier_plasma, 'plasma').img
        on = front(act, tier_plasma, 'plasma', 3).img
        sheet.alpha_composite(iso_block(ptop, off, s, 150), (x - 5, 50))
        sheet.alpha_composite(iso_block(ptop, on, s, 150), (x + 150, 50))
        dr.text((x, 205), name, font=small, fill=head)
        dr.text((x, 224), note, font=small, fill=label)
        sheet.alpha_composite(up(off, 4), (x, 252))
        sheet.alpha_composite(up(on, 4), (x + 72, 252))
        sheet.alpha_composite(up(s, 4), (x + 144, 252))
        dr.text((x, 320), 'front      on       side', font=small, fill=label)
        for k in range(FRAMES):
            sheet.alpha_composite(up(front(act, tier_plasma, 'plasma', k).img, 2), (x + (k % 4) * 36, 346 + (k // 4) * 36))
    dr.text((20, 424), 'animation frames under each machine; top face is the shared tier-3 top (magenta ring)', font=small, fill=label)

    # tier ladder: same granulator, electric vs plasma
    y0 = 470
    dr.text((20, y0), 'Granulator line-up', font=font, fill=head)
    e_front = front(act_conveyor, tier_elec_meter, 'electric', 3).img
    sheet.alpha_composite(iso_block(top('electric').img, e_front, side('electric').img, 150), (20, y0 + 34))
    sheet.alpha_composite(iso_block(ptop, front(act_conveyor, tier_plasma, 'plasma', 3).img, pside, 150), (180, y0 + 34))
    dr.text((45, y0 + 190), 'Electric', font=small, fill=label)
    dr.text((210, y0 + 190), 'Plasma', font=small, fill=label)

    # circuits
    dr.text((380, y0), 'Plasma Circuit (gold traces, magenta chip)', font=font, fill=head)
    sheet.alpha_composite(up(circuit('electric', COPPER).img, 6), (380, y0 + 40))
    sheet.alpha_composite(up(circuit('plasma', GOLD).img, 8), (500, y0 + 24))
    dr.text((380, y0 + 150), 'Electronic', font=small, fill=label)
    dr.text((530, y0 + 160), 'Plasma', font=small, fill=label)
    for k, item in enumerate((circuit('electric', COPPER).img, circuit('plasma', GOLD).img)):
        sx = 660 + k * 60
        dr.rectangle((sx, y0 + 60, sx + 53, y0 + 113), fill=(139, 139, 139), outline=(55, 55, 55))
        sheet.alpha_composite(up(item, 3), (sx + 3, y0 + 63))
    dr.text((660, y0 + 120), 'in slots', font=small, fill=label)

    # cables
    y1 = y0 + 230
    dr.text((20, y1), 'Plasma Cable (same shape as the Energy Cable; tiers never join)', font=font, fill=head)
    pc = plasma_cable_sheet()
    ec = energy_cable_sheet()
    sheet.alpha_composite(up(pc, 8), (20, y1 + 40))
    dr.text((20, y1 + 172), 'texture sheet', font=small, fill=label)
    sheet.alpha_composite(up(cable_run(ec, 3), 6), (180, y1 + 50))
    sheet.alpha_composite(up(cable_run(pc, 3), 6), (180, y1 + 110))
    dr.text((480, y1 + 60), 'Energy Cable', font=small, fill=label)
    dr.text((480, y1 + 120), 'Plasma Cable', font=small, fill=label)

    # battery
    dr.text((700, y1), 'Plasma Energy Battery', font=font, fill=head)
    btop, bside = battery_top('plasma').img, battery_side('plasma').img
    sheet.alpha_composite(iso_block(battery_top('electric').img, battery_front('electric', 6).img,
                                    battery_side('electric').img, 150), (690, y1 + 34))
    sheet.alpha_composite(iso_block(btop, battery_front('plasma', 4).img, bside, 150), (850, y1 + 34))
    dr.text((710, y1 + 190), 'Energy Battery', font=small, fill=label)
    dr.text((870, y1 + 190), 'Plasma (4/6 charged)', font=small, fill=label)
    for lvl in range(BATTERY_CELLS + 1):
        sheet.alpha_composite(up(battery_front('plasma', lvl).img, 2), (1020 + (lvl % 4) * 36, y1 + 50 + (lvl // 4) * 40))
    dr.text((1020, y1 + 140), 'charge 0..6', font=small, fill=label)

    # everything together
    y2 = y1 + 240
    dr.text((20, y2), 'A tier-3 row: generator -> battery -> washer, assembler', font=font, fill=head)
    row = [iso_block(t3.generator_top().img, t3.generator_front(3).img, t3.generator_side().img, 140),
           iso_block(btop, battery_front('plasma', 5).img, bside, 140),
           iso_block(ptop, front(act_washer, tier_plasma, 'plasma', 3).img, washer_side().img, 140),
           iso_block(ptop, front(act_assembler, tier_plasma, 'plasma', 3).img, pside, 140),
           iso_block(ptop, front(act_conveyor_smelt, tier_plasma, 'plasma', 3).img, pside, 140)]
    for k, im in enumerate(row):
        sheet.alpha_composite(im, (20 + k * 150, y2 + 36))
    dr.text((800, y2 + 60), 'GUIs: same layout as the electric machines,', font=small, fill=label)
    dr.text((800, y2 + 80), 'plus the plasma gauge from the generator', font=small, fill=label)
    dr.text((800, y2 + 100), '(washer also gets a water gauge).', font=small, fill=label)
    dr.text((800, y2 + 120), 'Mock-ups come with the code in C1/C2.', font=small, fill=label)
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (1000, 230), BG)
        for i, (name, act, _) in enumerate(MACHINES):
            s = washer_side().img if name == 'Ore Washer' else pside
            g.alpha_composite(iso_block(ptop, front(act, tier_plasma, 'plasma', k).img, s, 230), (i * 245, 0))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
