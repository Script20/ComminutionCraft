"""Tier 6 phase B previews: the Singularity Processor (raw ore -> 5 ingots in one step, needs a running Event Horizon
Reactor nearby), the Singularity Shard (rare by-product), the Singularity Circuit, the Singularity Cable and the
Singularity Energy Battery. Builds on phase_6a_preview (singularity colours, the tier-6 bottom).

Run: tools/.venv/Scripts/python design/previews/phase_6b_preview.py design/previews/phase_6b.png design/previews/phase_6b_running.gif
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.join(HERE, '..', '..', 'tools')
sys.path.insert(0, TOOLS)
sys.path.insert(0, HERE)
from mekstyle_lib import Tex, mix, shade, iso_block, up, FRAMES, TAU, ORE  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import K, STEEL  # noqa: E402
import phase_6a_preview as p6  # noqa: E402  (singularity tier colours + bottom)
import phase_5b_preview as p5b  # noqa: E402
import fluid_pipe_preview as fp  # noqa: E402
import gen_tier3_textures as t3  # noqa: E402
import gen_tier4_textures as t4  # noqa: E402

S = p6.S
VOID = (6, 6, 10)
GOLD = [(120, 84, 20), (190, 140, 40), (255, 196, 70), (255, 236, 168)]
INGOT = [(150, 150, 160), (196, 198, 206), (230, 232, 238)]


# ================================================================ Singularity Processor
def act_processor(t, f, lit):
    """Top half: raw ore spiralling into a tiny gold-ringed singularity on the left, finished ingots stacking up on the
    right (5 for each ore)."""
    ff = f if lit else 0
    for y in range(2, 7):
        for x in range(2, 14):
            t.px(x, y, (10, 10, 14))
    cx, cy = 5.5, 4.0
    for a in range(16):  # the photon ring
        x = int(round(cx + 1.8 * math.cos(a * TAU / 16) - 0.5))
        y = int(round(cy + 1.4 * math.sin(a * TAU / 16)))
        if 2 <= y <= 6:
            hot = 0.5 + 0.5 * math.cos(a * TAU / 16 - ff * TAU / FRAMES * 2)
            t.px(x, y, mix(S['on'], S['hi'], hot) if lit else S['off'])
    t.px(5, 4, (0, 0, 0)); t.px(6, 4, (0, 0, 0)); t.px(5, 3, (0, 0, 0)); t.px(6, 3, (0, 0, 0))
    if lit:  # ore chunks falling in along a spiral
        for k in range(3):
            a = (ff + k * 3) * TAU / FRAMES * 1.5
            r = 3.4 - ((ff + k * 3) % FRAMES) / FRAMES * 2.2
            x = int(round(cx + r * math.cos(a) - 0.5))
            y = int(round(cy + r * 0.6 * math.sin(a)))
            if 2 <= x <= 13 and 2 <= y <= 6:
                t.px(x, y, ORE[k % 3])
    # ingots stacking up on the right (5 per ore)
    n = (ff // 2) % 6 if lit else 3
    for i in range(min(n, 5)):
        y = 6 - (i % 3) * 1
        x = 10 + (i // 3) * 2
        t.px(x, y, INGOT[2] if i == n - 1 and lit else INGOT[1]); t.px(x + 1, y, INGOT[0])
    t.vline(8, 2, 6, K['d'])  # the divider


def processor_front(f=None):
    return dcs.front(act_processor, p6.tier_singularity, 'singularity', f)


def processor_side():
    """A gravity sensor above the seam (it needs the black hole nearby), grille below."""
    t = dcs.side('singularity')
    for (x, y) in ((7, 3), (8, 3), (6, 4), (9, 4), (7, 5), (8, 5)):
        t.px(x, y, S['off'])
    t.px(7, 4, (0, 0, 0)); t.px(8, 4, (0, 0, 0))
    return t


# ================================================================ items
def singularity_shard():
    """A sharp shard of collapsed matter: a black crystal with gold edges and a tiny white-hot core."""
    t = Tex()
    pts = [(8, 1), (11, 5), (10, 11), (7, 14), (4, 9), (5, 4)]
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    ImageDraw.Draw(img).polygon(pts, fill=(14, 12, 18, 255))
    t.img = img
    for (x, y) in ((8, 1), (9, 2), (10, 3), (11, 5), (11, 6), (10, 8), (10, 10), (9, 12), (8, 13), (7, 14)):
        t.px(x, y, GOLD[2])
    for (x, y) in ((5, 4), (5, 5), (4, 7), (4, 9), (5, 11), (6, 12)):
        t.px(x, y, GOLD[1])
    for (x, y) in ((7, 3), (7, 4), (6, 6)):
        t.px(x, y, (60, 52, 70))
    t.px(8, 8, (255, 250, 230)); t.px(7, 8, GOLD[3]); t.px(8, 7, GOLD[3])
    return t


def singularity_circuit():
    """The circuit on a black board with gold traces and a gold-ringed black chip."""
    t = dcs.circuit('singularity', GOLD)
    board = {K['l'], K['m'], K['hi'], K['o']}
    for y in range(16):
        for x in range(16):
            c = t.img.getpixel((x, y))
            if c[3] and (c[:3] in board or (abs(c[0] - c[1]) < 6 and abs(c[1] - c[2]) < 10 and 30 < c[0] < 90)):
                k = (c[0] - 16) / 84
                t.img.putpixel((x, y), tuple(int(v) for v in mix((4, 4, 6), (40, 34, 26), max(0.0, min(1.0, k)))[:3]) + (255,))
    t.px(7, 7, (0, 0, 0)); t.px(8, 8, (0, 0, 0)); t.px(8, 7, S['hi']); t.px(7, 8, S['on'])
    return t


def singularity_cable_sheet():
    """The antimatter cable recoloured: gold core on a void-black tube."""
    img = p5b.antimatter_cable_sheet().copy()
    px = img.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            if b > 150 and r > 120:  # icy -> gold
                k = (r + g + b) / 765
                px[x, y] = tuple(int(v) for v in mix(S['off'], S['hi'], k)[:3]) + (a,)
            elif b > r + 15:  # void blue -> black-gold
                px[x, y] = tuple(int(v) for v in mix((10, 8, 6), S['off'], b / 255)[:3]) + (a,)
    return img


def battery(level):
    return dcs.battery_front('singularity', level)


# ================================================================ sheet + gif
def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1320, 900), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)
    top = dcs.top('singularity').img

    dr.text((20, 14), 'Singularity Processor (raw ore -> 5 ingots; needs a running Event Horizon Reactor nearby)', font=font, fill=head)
    sheet.alpha_composite(iso_block(top, processor_front().img, processor_side().img, 190), (20, 50))
    sheet.alpha_composite(iso_block(top, processor_front(3).img, processor_side().img, 190), (230, 50))
    dr.text((70, 262), 'idle', font=small, fill=label)
    dr.text((280, 262), 'running', font=small, fill=label)
    for k, (name, im) in enumerate([('Front', processor_front().img), ('Front (running)', processor_front(3).img),
                                    ('Side (gravity sensor)', processor_side().img), ('Top', top)]):
        x = 460 + k * 200
        sheet.alpha_composite(up(im, 8), (x, 50))
        dr.text((x, 186), name, font=small, fill=label)

    y1 = 310
    dr.text((20, y1), 'Singularity Shard and Circuit', font=font, fill=head)
    for k, (name, t) in enumerate([('Singularity Shard', singularity_shard()), ('Antimatter Circuit', p5b.antimatter_circuit()),
                                   ('Singularity Circuit', singularity_circuit())]):
        x = 20 + k * 160
        sheet.alpha_composite(up(t.img, 8), (x, y1 + 36))
        dr.text((x, y1 + 172), name, font=small, fill=label)

    dr.text((540, y1), 'Singularity Cable', font=font, fill=head)
    cable = singularity_cable_sheet()
    scene = fp.scene(cable)
    sheet.alpha_composite(scene.crop((60, 0, 480, 330)).resize((300, 236)), (540, y1 + 34))
    sheet.alpha_composite(fp.item(cable), (850, y1 + 30))
    sheet.alpha_composite(up(cable, 8), (1160, y1 + 40))

    y2 = 610
    dr.text((20, y2), 'Singularity Energy Battery', font=font, fill=head)
    sheet.alpha_composite(iso_block(dcs.battery_top('singularity').img, battery(dcs.BATTERY_CELLS).img,
                                    dcs.battery_side('singularity').img, 190), (20, y2 + 36))
    for lvl in range(dcs.BATTERY_CELLS + 1):
        x = 250 + lvl * 110
        sheet.alpha_composite(up(battery(lvl).img, 6), (x, y2 + 50))
        dr.text((x + 30, y2 + 152), f'{lvl}/6', font=small, fill=label)
    sheet.alpha_composite(up(dcs.battery_side('singularity').img, 6), (1040, y2 + 50))
    sheet.alpha_composite(up(dcs.battery_top('singularity').img, 6), (1150, y2 + 50))
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (560, 230), BG)
        g.alpha_composite(iso_block(top, processor_front(k).img, processor_side().img, 190), (10, 20))
        g.alpha_composite(up(processor_front(k).img, 12), (360, 20))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
