"""Creative blocks' textures (approved preview: design/previews/creative.png). Writes into the mod's texture folders.

Usage: python tools/gen_creative_textures.py

Creative blocks get a rainbow seam (every tier's colour, cycling) and an infinity mark on top; every face is an
8-frame strip so the rainbow moves. Creative Energy Battery: six always-lit cells in rainbow colours. Creative Fluid
Tank: the Fluid Tank frame (fully transparent window, cutout) with a rainbow trim; FluidTankRenderer draws the fluid.
"""
import colorsys
import os
import sys

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TOOLS)
from mekstyle_lib import mix, shade, FRAMES  # noqa: E402
from dark_chassis_system import K, STEEL, casing, well  # noqa: E402
import gen_tier3_textures as t3  # noqa: E402

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



BLOCK = os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures', 'block')

if __name__ == '__main__':
    os.makedirs(BLOCK, exist_ok=True)
    for name, fn in (('creative_energy_battery_front', battery_front), ('creative_energy_battery_front_on', battery_front),
                     ('creative_energy_battery_side', battery_side), ('creative_energy_battery_top', battery_top),
                     ('creative_fluid_tank_side', tank_side), ('creative_fluid_tank_top', tank_top)):
        t3.save_strip(os.path.join(BLOCK, f'{name}.png'), lambda k, fn=fn: fn(k))
    print('ok')
