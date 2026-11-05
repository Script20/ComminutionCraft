"""Creative blocks preview: Creative Energy Battery (endless FE out of every face) and Creative Fluid Tank (endless
fluid of the kind picked in its GUI). Creative blocks get a rainbow seam (every tier's colour, cycling) and an
infinity mark on top.

Usage: tools/.venv/Scripts/python.exe design/previews/creative_preview.py <out.png> <out.gif>
"""
import colorsys
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

TOOLS = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'tools')
sys.path.insert(0, TOOLS)
from mekstyle_lib import Tex, mix, shade, iso_block, up, FRAMES, TAU  # noqa: E402
import dark_chassis_system as dcs  # noqa: E402
from dark_chassis_system import K, GLASS, STEEL, casing, well  # noqa: E402
import gen_tier3_textures as t3  # noqa: E402
import gen_tier4_textures as t4  # noqa: E402


def rainbow(k, f=0, light=0.62):
    """A hue along the rainbow (k in 0..1), shifted by animation frame f."""
    h = (k + f / FRAMES) % 1.0
    r, g, b = colorsys.hls_to_rgb(h, light, 0.9)
    return (int(r * 255), int(g * 255), int(b * 255))


def rainbow_seam(t, pts, f):
    for i, (x, y) in enumerate(pts):
        t.px(x, y, rainbow(i / max(1, len(pts)), f))


def infinity(t, cx, cy, f, colour=None):
    """A small lying-eight (7x3) centred on (cx, cy)."""
    pts = [(-3, 0), (-2, -1), (-2, 1), (-1, 0), (0, 0), (1, 0), (2, -1), (2, 1), (3, 0), (-1, -1), (1, 1)]
    pts = [(-3, 0), (-2, -1), (-1, -1), (0, 0), (1, 1), (2, 1), (3, 0), (2, -1), (1, -1), (0, 0), (-1, 1), (-2, 1)]
    for i, (dx, dy) in enumerate(pts):
        t.px(cx + dx, cy + dy, colour or rainbow(i / len(pts), f, 0.7))


# ================================================================ Creative Energy Battery
def battery_front(f=0):
    """Six cells, every one lit, each a different colour of the rainbow (it never runs down)."""
    t = casing()
    well(t, 3, 1, 12, 14)
    for x in range(4, 12):
        t.px(x, 2, STEEL[1])
    t.vline(4, 3, 13, STEEL[0]); t.vline(11, 3, 13, STEEL[0])
    for i in range(6):
        y = 13 - i * 2
        c = rainbow(i / 6, f * 0.25)
        for x in range(5, 11):
            t.px(x, y, mix(c, (255, 255, 255), 0.35) if x == 5 else shade(c, 0.7) if x == 10 else c)
    rainbow_seam(t, [(1, y) for y in range(2, 14)], f)
    rainbow_seam(t, [(14, y) for y in range(13, 1, -1)], f)
    return t


def battery_side(f=0):
    t = casing()
    for x in range(2, 14):
        for y in range(2, 14):
            if y != 8:
                t.px(x, y, mix(K['l'], K['m'], (y - 2) / 11) if x % 2 == 0 else K['d'])
    t.hline(2, 13, 1, K['m']); t.hline(2, 13, 14, K['o'])
    rainbow_seam(t, [(x, 8) for x in range(1, 15)], f)
    return t


def battery_top(f=0):
    t = casing()
    ring = [(x, 2) for x in range(2, 14)] + [(13, y) for y in range(3, 13)] + \
           [(x, 13) for x in range(13, 1, -1)] + [(2, y) for y in range(12, 2, -1)]
    rainbow_seam(t, ring, f)
    t.rect(3, 3, 12, 12, K['d'])
    infinity(t, 7, 7, f)
    return t


# ================================================================ Creative Fluid Tank
def tank_side(f=0, fluid=None):
    """The Fluid Tank's frame with a rainbow trim; the window shows whatever fluid is picked (always full)."""
    t = t3.tank_side_cutout()
    if fluid is not None:
        fl = fluid(2).img
        for y in range(1, 15):
            for x in range(3, 13):
                t.px(x, y, fl.getpixel((x, y))[:3])
    rainbow_seam(t, [(2, y) for y in range(1, 15)], f)
    rainbow_seam(t, [(13, y) for y in range(14, 0, -1)], f)
    return t


def tank_top(f=0):
    t = casing()
    ring = [(x, 3) for x in range(3, 13)] + [(12, y) for y in range(4, 12)] + \
           [(x, 12) for x in range(12, 2, -1)] + [(3, y) for y in range(11, 3, -1)]
    rainbow_seam(t, ring, f)
    t.rect(4, 4, 11, 11, K['d'])
    infinity(t, 7, 8, f)
    return t


# ================================================================ GUI mock (creative tank)
FLUIDS = [('Water', (40, 92, 186)), ('Lava', (226, 104, 28)), ('Plasma', (255, 80, 210)), ('Hydrogen', (228, 230, 248)),
          ('Oxygen', (110, 170, 230)), ('Deuterium', (120, 186, 214)), ('Tritium', (70, 190, 100)),
          ("(other mods')", (120, 120, 130))]


def gui_mock():
    s = Image.new('RGBA', (176, 166), (0, 0, 0, 0))
    d = ImageDraw.Draw(s)
    d.rectangle((0, 0, 175, 165), fill=(46, 49, 56), outline=(16, 17, 20))
    d.rectangle((1, 1, 174, 1), fill=(84, 89, 100))
    for i, (_, c) in enumerate(FLUIDS):  # a button per fluid, 4 per row
        x, y = 12 + (i % 4) * 22, 20 + (i // 4) * 22
        d.rectangle((x, y, x + 17, y + 17), fill=(22, 24, 28), outline=(84, 89, 100) if i != 2 else (255, 186, 56))
        d.rectangle((x + 2, y + 2, x + 15, y + 15), fill=c)
    d.rectangle((150, 16, 162, 69), fill=(13, 14, 17), outline=(84, 89, 100))  # the full gauge of the picked fluid
    d.rectangle((151, 17, 161, 68), fill=(255, 80, 210))
    for row in range(3):
        for col in range(9):
            x, y = 7 + col * 18, 83 + row * 18
            d.rectangle((x, y, x + 17, y + 17), fill=(22, 24, 28), outline=(16, 17, 20))
    for col in range(9):
        d.rectangle((7 + col * 18, 141, 24 + col * 18, 158), fill=(22, 24, 28), outline=(16, 17, 20))
    return s


def main(out_png, out_gif):
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    sheet = Image.new('RGBA', (1320, 820), BG)
    dr = ImageDraw.Draw(sheet)
    label, head = (170, 170, 180), (235, 235, 240)

    dr.text((20, 14), 'Creative blocks: rainbow seams (every tier at once), an infinity mark on top', font=font, fill=head)
    sheet.alpha_composite(iso_block(battery_top().img, battery_front().img, battery_side().img, 200), (10, 50))
    dr.text((40, 260), 'Creative Energy Battery', font=small, fill=head)
    dr.text((40, 280), 'endless FE out of every face', font=small, fill=label)
    for k, im in enumerate((battery_front(), battery_side(), battery_top())):
        sheet.alpha_composite(up(im.img, 5), (240 + k * 95, 80))
    dr.text((240, 170), 'front         side          top', font=small, fill=label)

    ttop = tank_top().img
    plasma_side = tank_side(0, t3.plasma_still).img
    water = lambda f: t3.plasma_still(f)  # noqa: E731 (stand-in: the renderer draws the real fluid)
    sheet.alpha_composite(iso_block(ttop, plasma_side, plasma_side, 200), (580, 50))
    trit = tank_side(0, t4.tritium_still).img
    sheet.alpha_composite(iso_block(ttop, trit, trit, 200), (790, 50))
    dr.text((600, 260), 'Creative Fluid Tank (plasma picked / tritium picked)', font=small, fill=head)
    dr.text((600, 280), 'endless fluid out of every face; the window is always full', font=small, fill=label)
    for k, im in enumerate((tank_side(0).img, ttop)):
        sheet.alpha_composite(up(im, 5), (1020 + k * 95, 80))
    dr.text((1020, 170), 'side          top', font=small, fill=label)

    y0 = 330
    dr.text((20, y0), 'Creative Fluid Tank GUI (mock): click a fluid to fill the tank with it', font=font, fill=head)
    sheet.alpha_composite(up(gui_mock(), 2), (20, y0 + 40))
    gd = ImageDraw.Draw(sheet)
    gd.text((20 + 8 * 2, y0 + 40 + 5 * 2), 'Creative Fluid Tank', font=small, fill=(228, 230, 236))
    gd.text((400, y0 + 60), 'Every fluid in the game gets a button (ours, vanilla, other mods\'),', font=small, fill=label)
    gd.text((400, y0 + 80), 'drawn with its own texture; the picked one has a gold frame.', font=small, fill=label)
    gd.text((400, y0 + 100), 'More than fit on one page: arrows page through them.', font=small, fill=label)
    gd.text((400, y0 + 130), 'Empty button / shift-click: no fluid (the tank does nothing).', font=small, fill=label)
    gd.text((400, y0 + 160), 'Creative only: no recipes; both sit at the end of the creative tab.', font=small, fill=label)
    for i, (name, c) in enumerate(FLUIDS):
        gd.rectangle((400, y0 + 200 + i * 22, 414, y0 + 214 + i * 22), fill=c)
        gd.text((422, y0 + 199 + i * 22), name, font=small, fill=label)
    sheet.save(out_png)

    gifs = []
    for k in range(FRAMES):
        g = Image.new('RGBA', (460, 240), BG)
        g.alpha_composite(iso_block(battery_top(k).img, battery_front(k).img, battery_side(k).img, 220), (0, 10))
        ts = tank_side(k, t3.plasma_still).img
        g.alpha_composite(iso_block(tank_top(k).img, ts, ts, 220), (230, 10))
        gifs.append(g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255))
    gifs[0].save(out_gif, save_all=True, append_images=gifs[1:], duration=100, loop=0)
    print('wrote', out_png, out_gif)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
