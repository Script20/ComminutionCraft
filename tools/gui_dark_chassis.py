"""Dark Chassis machine GUIs (all tiers). Writes straight into the mod's GUI texture folder.

Usage: python tools/gui_dark_chassis.py

Sheet layout matches MachineScreen (256x256):
  background (0,0) 176x166, filled flame (176,0) 14x14, filled progress arrow (176,14) 24x17,
  energy bar fill (176,31) 10x52 (tier 2; drawn at (11,17) on the background).
Slot positions match FuelMachineMenu (vanilla furnace layout), ElectricMachineMenu, ElectricGeneratorMenu and
EnergyBatteryMenu (player inventory only).
The seam lines beside the title and the groove after "Inventory" are drawn in MachineScreen,
so they fit whatever width the (translated) labels have.
"""
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
from mekstyle_lib import mix, shade, fire  # noqa: E402

OUT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources',
                   'assets', 'comminutioncraft', 'textures', 'gui', 'container')

# Same casing palette and tier colours as dark_chassis_system.py
K = dict(o=(16, 17, 20), d=(28, 30, 35), m=(42, 45, 52), l=(60, 64, 73), hi=(84, 89, 100))
GLASS = (13, 14, 17)
SLOT = (22, 24, 28)
COAL = dict(off=(86, 56, 34), on=(255, 150, 40), hi=(255, 214, 120))
ELEC = dict(off=(30, 66, 86), on=(64, 196, 255), hi=(190, 240, 255))
MOLTEN = [(150, 50, 16), (226, 104, 28), (255, 168, 56), (255, 226, 140)]

W, H = 176, 166

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


def rgba(c, a=255):
    return tuple(c[:3]) + (a,)


class Sheet:
    def __init__(self):
        self.img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))

    def px(self, x, y, c):
        self.img.putpixel((x, y), rgba(c))

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.px(x, y, c)

    def hline(self, x0, x1, y, c):
        for x in range(x0, x1 + 1):
            self.px(x, y, c)

    def vline(self, x, y0, y1, c):
        for y in range(y0, y1 + 1):
            self.px(x, y, c)


def body(s):
    """Charcoal panel, smooth top-to-bottom gradient, rounded 1px-outlined frame with a bevel."""
    for y in range(H):
        c = mix(K['l'], K['m'], y / (H - 1))
        s.hline(0, W - 1, y, c)
    # outline
    s.hline(2, W - 3, 0, K['o'])
    s.hline(2, W - 3, H - 1, K['o'])
    s.vline(0, 2, H - 3, K['o'])
    s.vline(W - 1, 2, H - 3, K['o'])
    for (x, y) in ((1, 1), (W - 2, 1), (1, H - 2), (W - 2, H - 2)):
        s.px(x, y, K['o'])
    for (x, y) in ((0, 0), (1, 0), (0, 1), (W - 1, 0), (W - 2, 0), (W - 1, 1),
                   (0, H - 1), (1, H - 1), (0, H - 2), (W - 1, H - 1), (W - 2, H - 1), (W - 1, H - 2)):
        s.img.putpixel((x, y), (0, 0, 0, 0))
    # bevel: light top/left, dark bottom/right
    s.hline(2, W - 3, 1, K['hi'])
    s.vline(1, 2, H - 3, shade(K['hi'], 0.85))
    s.hline(2, W - 3, H - 2, K['d'])
    s.vline(W - 2, 2, H - 3, K['d'])
    # corner bolts
    for (x, y) in ((4, 4), (W - 6, 4), (4, H - 6), (W - 6, H - 6)):
        s.px(x, y, K['hi'])
        s.px(x + 1, y, K['l'])
        s.px(x, y + 1, K['l'])
        s.px(x + 1, y + 1, K['o'])


def slot(s, x, y, size=18, rim=None):
    """Recessed slot: dark well, shadow top/left, highlight bottom/right (vanilla slot geometry)."""
    s.rect(x, y, x + size - 1, y + size - 1, SLOT)
    s.hline(x, x + size - 2, y, K['o'])
    s.vline(x, y, y + size - 2, K['o'])
    s.hline(x + 1, x + size - 1, y + size - 1, K['hi'])
    s.vline(x + size - 1, y + 1, y + size - 1, K['hi'])
    s.px(x + size - 1, y, K['d'])
    s.px(x, y + size - 1, K['d'])
    if rim:
        # tier-coloured seam around the slot, one pixel outside it
        s.hline(x - 1, x + size, y - 1, rim)
        s.hline(x - 1, x + size, y + size, rim)
        s.vline(x - 1, y - 1, y + size, rim)
        s.vline(x + size, y - 1, y + size, rim)


# Unlit flame and arrow, stamped into the panel so the coloured sprites fill over them.
def flame_silhouette(s, x0, y0):
    for y, row in enumerate(FLAME):
        for x, ch in enumerate(row):
            if ch == '#':
                s.px(x0 + x, y0 + y, K['d'])


def arrow_silhouette(s, x0, y0):
    for y in range(17):
        for x in range(24):
            if arrow_mask(x, y):
                edge = not (arrow_mask(x, y - 1) and arrow_mask(x, y + 1))
                s.px(x0 + x, y0 + y, K['o'] if edge else K['d'])


ENERGY_X, ENERGY_Y, ENERGY_W, ENERGY_H = 11, 17, 10, 52


def energy_well(s):
    """Recessed glass tube on the left with tick marks; the fill sprite is drawn over its inside."""
    x0, y0, x1, y1 = ENERGY_X - 1, ENERGY_Y - 1, ENERGY_X + ENERGY_W, ENERGY_Y + ENERGY_H
    s.rect(x0, y0, x1, y1, GLASS)
    s.hline(x0, x1, y0, K['o'])
    s.vline(x0, y0, y1, K['o'])
    s.hline(x0, x1, y1, K['hi'])
    s.vline(x1, y0, y1, K['hi'])
    for y in range(ENERGY_Y + 3, ENERGY_Y + ENERGY_H, 4):
        s.px(x1 + 1, y, K['hi'])  # scale ticks beside the tube
        s.hline(ENERGY_X, ENERGY_X + ENERGY_W - 1, y, (18, 20, 24))


def energy_sprite(s):
    """Blue charge, brighter at the top, with the same segment lines as the empty tube."""
    for y in range(ENERGY_H):
        for x in range(ENERGY_W):
            c = mix(ELEC['hi'], ELEC['on'], min(1.0, y / 12)) if y < 12 else mix(ELEC['on'], shade(ELEC['on'], 0.55), (y - 12) / (ENERGY_H - 12))
            if x == 0 or x == ENERGY_W - 1:
                c = shade(c, 0.75)
            if (y - 3) % 4 == 0:
                c = shade(c, 0.7)
            s.px(176 + x, 31 + y, c)


def flame_sprite(s):
    for y, row in enumerate(FLAME):
        for x, ch in enumerate(row):
            if ch == '#':
                centre = 1 - min(1, abs(x - 6.5) / 6)
                # hot (pale) at the base and core, darker orange-red at the tips
                s.px(176 + x, y, fire((1 - y / 13) * 0.85 - centre * 0.3 + 0.1))


def arrow_sprite(s, colour_at):
    for y in range(17):
        for x in range(24):
            if arrow_mask(x, y):
                edge = not (arrow_mask(x, y - 1) and arrow_mask(x, y + 1))
                c = colour_at(x, y)
                s.px(176 + x, 14 + y, shade(c, 0.7) if edge else c)


def granulator_arrow(x, y):
    """Orange tier glow broken into chevron segments, like grit moving along a belt."""
    base = mix(COAL['on'], COAL['hi'], 0.15 + 0.35 * (1 - abs(y - 8) / 8))
    return shade(base, 0.72) if (x + abs(y - 8)) % 6 == 0 else base


def smelter_arrow(x, y):
    """Molten gradient, hotter towards the tip."""
    t = x / 23 * (len(MOLTEN) - 1)
    i = min(int(t), len(MOLTEN) - 2)
    return mix(MOLTEN[i], MOLTEN[i + 1], t - i)


def electric_granulator_arrow(x, y):
    """Blue tier glow broken into chevron segments."""
    base = mix(ELEC['on'], ELEC['hi'], 0.15 + 0.35 * (1 - abs(y - 8) / 8))
    return shade(base, 0.72) if (x + abs(y - 8)) % 6 == 0 else base


def player_inventory(s):
    for row in range(3):
        for col in range(9):
            slot(s, 7 + col * 18, 83 + row * 18)
    for col in range(9):
        slot(s, 7 + col * 18, 141)


# Slot positions below are the menu's slot positions minus 1 (the slot border).
def coal_sheet(arrow_colour):
    s = Sheet()
    body(s)
    slot(s, 55, 16)
    slot(s, 55, 52)
    slot(s, 111, 30, 26, rim=COAL['off'])
    flame_silhouette(s, 56, 36)
    arrow_silhouette(s, 79, 34)
    player_inventory(s)
    flame_sprite(s)
    arrow_sprite(s, arrow_colour)
    return s.img


def electric_sheet(arrow_colour):
    s = Sheet()
    body(s)
    energy_well(s)
    slot(s, 55, 34)
    slot(s, 111, 30, 26, rim=ELEC['off'])
    arrow_silhouette(s, 79, 34)
    player_inventory(s)
    arrow_sprite(s, arrow_colour)
    energy_sprite(s)
    return s.img


def generator_sheet():
    s = Sheet()
    body(s)
    energy_well(s)
    slot(s, 79, 52, rim=ELEC['off'])
    flame_silhouette(s, 81, 34)
    player_inventory(s)
    flame_sprite(s)
    energy_sprite(s)
    return s.img


def battery_sheet():
    """Energy Battery: only the energy tube; the screen writes stored FE and the in/out rates beside it."""
    s = Sheet()
    body(s)
    energy_well(s)
    player_inventory(s)
    energy_sprite(s)
    return s.img


os.makedirs(OUT, exist_ok=True)
coal_sheet(granulator_arrow).save(os.path.join(OUT, 'coal_granulator.png'))
coal_sheet(smelter_arrow).save(os.path.join(OUT, 'coal_smelter.png'))
electric_sheet(electric_granulator_arrow).save(os.path.join(OUT, 'electric_granulator.png'))
electric_sheet(smelter_arrow).save(os.path.join(OUT, 'electric_smelter.png'))
generator_sheet().save(os.path.join(OUT, 'electric_generator.png'))
battery_sheet().save(os.path.join(OUT, 'energy_battery.png'))
print('ok')
