"""Dark Chassis machine GUIs (all tiers). Writes straight into the mod's GUI texture folder.

Usage: python tools/gui_dark_chassis.py

Sheet layout matches MachineScreen (256x256):
  background (0,0) 176x166, filled flame (176,0) 14x14, filled progress arrow (176,14) 24x17,
  energy bar fill (176,31) 10x52 (tier 2; drawn at (11,17) on the background),
  plasma gauge fill (186,31) 10x52 (tier 3; drawn at (155,17)).
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
PLASMA = dict(off=(92, 34, 84), on=(255, 80, 210), hi=(255, 196, 244))
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


PLASMA_X = 155


def plasma_well(s):
    """Recessed tube on the right, mirroring the energy tube; the plasma fill sprite is drawn over its inside."""
    x0, y0, x1, y1 = PLASMA_X - 1, ENERGY_Y - 1, PLASMA_X + ENERGY_W, ENERGY_Y + ENERGY_H
    s.rect(x0, y0, x1, y1, GLASS)
    s.hline(x0, x1, y0, K['o'])
    s.vline(x0, y0, y1, K['o'])
    s.hline(x0, x1, y1, K['hi'])
    s.vline(x1, y0, y1, K['hi'])
    for y in range(ENERGY_Y + 3, ENERGY_Y + ENERGY_H, 4):
        s.px(x0 - 1, y, K['hi'])
        s.hline(PLASMA_X, PLASMA_X + ENERGY_W - 1, y, (18, 20, 24))


def plasma_sprite(s):
    """Magenta plasma, bright at the surface, with soft swirl bands."""
    import math
    for y in range(ENERGY_H):
        for x in range(ENERGY_W):
            base = mix(PLASMA['hi'], PLASMA['on'], min(1.0, y / 8)) if y < 8 else                 mix(PLASMA['on'], (120, 20, 110), (y - 8) / (ENERGY_H - 8))
            swirl = 0.5 + 0.5 * math.sin(x * 0.9 + y * 0.45)
            c = mix(base, PLASMA['hi'], 0.18 * swirl) if y >= 8 else base
            if x == 0 or x == ENERGY_W - 1:
                c = shade(c, 0.75)
            if (y - 3) % 4 == 0:
                c = shade(c, 0.78)
            s.px(186 + x, 31 + y, c)


def plasma_generator_sheet():
    """Plasma Generator: energy left, plasma right, Air Filter slot, and air -> plasma intake dots
    (3 dots at x 66/74/82, the plasma dot at x 102, y 30-33; PlasmaGeneratorScreen lights them)."""
    s = Sheet()
    body(s)
    energy_well(s)
    plasma_well(s)
    slot(s, 79, 52, rim=PLASMA['off'])
    for i in range(3):
        s.rect(66 + i * 8, 30, 69 + i * 8, 33, K['d'])
    s.rect(102, 30, 105, 33, K['d'])
    for x in range(91, 99):
        s.px(x, 31, K['hi']); s.px(x, 32, K['hi'])
    s.px(99, 30, K['hi']); s.px(99, 33, K['hi'])
    player_inventory(s)
    energy_sprite(s)
    plasma_sprite(s)
    return s.img


def plasma_granulator_arrow(x, y):
    """Magenta tier glow broken into chevron segments."""
    base = mix(PLASMA['on'], PLASMA['hi'], 0.15 + 0.35 * (1 - abs(y - 8) / 8))
    return shade(base, 0.72) if (x + abs(y - 8)) % 6 == 0 else base


def tier3_sheet(arrow_colour):
    """Tier-3 FE machines (Plasma Granulator / Smelter): the electric layout with magenta slot rims, no fluid tube."""
    s = Sheet()
    body(s)
    energy_well(s)
    slot(s, 55, 34)
    slot(s, 111, 30, 26, rim=PLASMA['off'])
    arrow_silhouette(s, 79, 34)
    player_inventory(s)
    arrow_sprite(s, arrow_colour)
    energy_sprite(s)
    return s.img


def plasma_machine_sheet(arrow_colour):
    """The Circuit Assembler: the electric layout (energy left, input, arrow, output) plus the plasma tube right and,
    left of it (x 141), the antimatter tube for the Antimatter Circuit (fill sprite at (246, 31))."""
    s = Sheet()
    body(s)
    energy_well(s)
    plasma_well(s)
    gauge_well(s, 141)
    fluid_sprite(s, 246, ANTIMATTER_FLUID)
    slot(s, 55, 34)
    slot(s, 111, 30, 26, rim=PLASMA['off'])
    arrow_silhouette(s, 79, 34)
    player_inventory(s)
    arrow_sprite(s, arrow_colour)
    energy_sprite(s)
    plasma_sprite(s)
    return s.img


WATER_X = 141
WATER = [(24, 52, 120), (40, 92, 186), (86, 150, 228), (190, 226, 250)]


def water_well(s):
    """A second tube left of the plasma one (Ore Washer); the water fill sprite is drawn over its inside."""
    x0, y0, x1, y1 = WATER_X - 1, ENERGY_Y - 1, WATER_X + ENERGY_W, ENERGY_Y + ENERGY_H
    s.rect(x0, y0, x1, y1, GLASS)
    s.hline(x0, x1, y0, K['o'])
    s.vline(x0, y0, y1, K['o'])
    s.hline(x0, x1, y1, K['hi'])
    s.vline(x1, y0, y1, K['hi'])
    for y in range(ENERGY_Y + 3, ENERGY_Y + ENERGY_H, 4):
        s.hline(WATER_X, WATER_X + ENERGY_W - 1, y, (18, 20, 24))


def water_sprite(s):
    """Blue water, light at the surface, at (196, 31) on the sheet."""
    import math
    for y in range(ENERGY_H):
        for x in range(ENERGY_W):
            base = mix(WATER[3], WATER[2], min(1.0, y / 6)) if y < 6 else mix(WATER[2], WATER[0], (y - 6) / (ENERGY_H - 6))
            c = mix(base, WATER[3], 0.12 * (0.5 + 0.5 * math.sin(x * 1.1 + y * 0.5))) if y >= 6 else base
            if x == 0 or x == ENERGY_W - 1:
                c = shade(c, 0.75)
            if (y - 3) % 4 == 0:
                c = shade(c, 0.78)
            s.px(196 + x, 31 + y, c)


def washer_arrow(x, y):
    """Water blue fading into the magenta tier colour towards the tip."""
    base = mix(WATER[2], PLASMA['on'], x / 23)
    return shade(base, 0.72) if (x + abs(y - 8)) % 6 == 0 else base


def ore_washer_sheet():
    """The Ore Washer runs on FE and water: the tier-3 layout with one water tube on the right."""
    s = Sheet()
    s.img = tier3_sheet(washer_arrow)
    gauge_well(s, 155)
    all_fluid_sprites(s)
    return s.img


HYDROGEN = [(150, 150, 176), (196, 198, 224), (228, 230, 248), (250, 250, 255)]
OXYGEN = [(60, 120, 190), (110, 170, 230), (160, 206, 248), (214, 236, 255)]


def gauge_well(s, x):
    """A recessed fluid tube like the plasma one, inner area at x .. x + ENERGY_W - 1."""
    x0, y0, x1, y1 = x - 1, ENERGY_Y - 1, x + ENERGY_W, ENERGY_Y + ENERGY_H
    s.rect(x0, y0, x1, y1, GLASS)
    s.hline(x0, x1, y0, K['o'])
    s.vline(x0, y0, y1, K['o'])
    s.hline(x0, x1, y1, K['hi'])
    s.vline(x1, y0, y1, K['hi'])
    for y in range(ENERGY_Y + 3, ENERGY_Y + ENERGY_H, 4):
        s.px(x0 - 1, y, K['hi'])
        s.hline(x, x + ENERGY_W - 1, y, (18, 20, 24))


def fluid_sprite(s, u, palette):
    """A fluid fill sprite at (u, 31): light at the surface, soft bands below (palette: dark .. light)."""
    import math
    for y in range(ENERGY_H):
        for x in range(ENERGY_W):
            base = mix(palette[3], palette[2], min(1.0, y / 6)) if y < 6 else mix(palette[2], palette[0], (y - 6) / (ENERGY_H - 6))
            c = mix(base, palette[3], 0.12 * (0.5 + 0.5 * math.sin(x * 1.1 + y * 0.5))) if y >= 6 else base
            if x == 0 or x == ENERGY_W - 1:
                c = shade(c, 0.75)
            if (y - 3) % 4 == 0:
                c = shade(c, 0.78)
            s.px(u + x, 31 + y, c)


def all_fluid_sprites(s):
    """Fill sprites for every fluid gauge: plasma 186, water 196, hydrogen 206, oxygen 216 (all at v 31)."""
    plasma_sprite(s)
    fluid_sprite(s, 196, WATER)
    fluid_sprite(s, 206, HYDROGEN)
    fluid_sprite(s, 216, OXYGEN)


def fade_arrow(a, b):
    def colour(x, y):
        base = mix(a, b, x / 23)
        return shade(base, 0.72) if (x + abs(y - 8)) % 6 == 0 else base
    return colour


def fluid_machine_sheet(gauges, arrow_colour):
    """Electrolyser / Plasma Generator / MHD Generator: energy left, a fluid tube at each x in gauges, an arrow."""
    s = Sheet()
    body(s)
    energy_well(s)
    for x in gauges:
        gauge_well(s, x)
    arrow_silhouette(s, 79, 34)
    player_inventory(s)
    arrow_sprite(s, arrow_colour)
    energy_sprite(s)
    all_fluid_sprites(s)
    return s.img


def water_generator_sheet():
    """Atmospheric Water Generator: water tube right, Air Filter slot, air -> water intake dots (no FE)."""
    s = Sheet()
    body(s)
    gauge_well(s, 155)
    slot(s, 79, 52, rim=PLASMA['off'])
    for i in range(3):
        s.rect(66 + i * 8, 30, 69 + i * 8, 33, K['d'])
    s.rect(102, 30, 105, 33, K['d'])
    for x in range(91, 99):
        s.px(x, 31, K['hi']); s.px(x, 32, K['hi'])
    s.px(99, 30, K['hi']); s.px(99, 33, K['hi'])
    player_inventory(s)
    all_fluid_sprites(s)
    return s.img


FUSION = dict(off=(32, 84, 44), on=(96, 255, 128), hi=(214, 255, 214))
DEUTERIUM = [(70, 120, 150), (120, 186, 214), (180, 228, 244), (232, 250, 255)]
TRITIUM = [(30, 90, 50), (70, 190, 100), (140, 245, 160), (220, 255, 226)]


def fusion_sprites(s):
    """Tier-4 fluid fill sprites: deuterium 226, tritium 236 (at v 31), next to the others."""
    all_fluid_sprites(s)
    fluid_sprite(s, 226, DEUTERIUM)
    fluid_sprite(s, 236, TRITIUM)


def fusion_fluid_sheet(gauges, arrow_colour):
    """Isotope Separator / Tritium Breeder: the fluid-machine layout with the tier-4 sprites."""
    s = Sheet()
    s.img = fluid_machine_sheet(gauges, arrow_colour)
    fusion_sprites(s)
    return s.img


def pellet_press_sheet():
    """Pellet Press: casing in, pellet out (green rims), deuterium and tritium tubes on the right."""
    s = Sheet()
    body(s)
    energy_well(s)
    slot(s, 55, 34)
    slot(s, 111, 30, 26, rim=FUSION['off'])
    arrow_silhouette(s, 79, 34)
    gauge_well(s, 141)
    gauge_well(s, 155)
    player_inventory(s)
    arrow_sprite(s, fade_arrow(DEUTERIUM[2], TRITIUM[2]))
    energy_sprite(s)
    fusion_sprites(s)
    return s.img


def fusion_reactor_sheet():
    """Reactor Controller: energy left, plasma (ignition) tube, pellet slot, core-heat bar, tritium tube right.
    Heat bar well at (64, 19)-(112, 25), its fill sprite at (176, 90), 47x5."""
    s = Sheet()
    body(s)
    energy_well(s)
    gauge_well(s, 50)
    gauge_well(s, 155)
    slot(s, 79, 52, rim=FUSION['off'])
    x0, y0, x1, y1 = 64, 19, 112, 25
    s.rect(x0, y0, x1, y1, GLASS)
    s.hline(x0, x1, y0, K['o']); s.vline(x0, y0, y1, K['o'])
    s.hline(x0, x1, y1, K['hi']); s.vline(x1, y0, y1, K['hi'])
    player_inventory(s)
    energy_sprite(s)
    fusion_sprites(s)
    for x in range(47):
        for y in range(5):
            c = mix(FUSION['off'], FUSION['on'], x / 46) if x < 40 else mix(FUSION['on'], FUSION['hi'], (x - 40) / 6)
            s.px(176 + x, 90 + y, shade(c, 0.8) if y in (0, 4) else c)
    return s.img


def ore_purifier_sheet():
    """Ore Purifier: washed ore in, purified ore out (top right slot) and the by-product below it, oxygen tube right.
    Slots (menu positions): input (56, 35), output (116, 24), by-product (116, 46); oxygen tube at x 155."""
    s = Sheet()
    body(s)
    energy_well(s)
    slot(s, 55, 34)
    slot(s, 115, 23, rim=FUSION['off'])
    slot(s, 115, 45, rim=FUSION['off'])
    arrow_silhouette(s, 79, 34)
    gauge_well(s, 155)
    player_inventory(s)
    arrow_sprite(s, fade_arrow(OXYGEN[2], FUSION['on']))
    energy_sprite(s)
    fusion_sprites(s)
    return s.img


ANTIMATTER = dict(off=(86, 90, 104), on=(214, 228, 255), hi=(255, 255, 255))
ANTIMATTER_FLUID = [(40, 50, 92), (110, 130, 200), (190, 206, 245), (246, 250, 255)]


def accelerator_sheet():
    """Particle Accelerator controller: energy left, hydrogen (x 141) and antimatter (x 155) tubes, status text from
    x 30, a beam-progress bar well at (29, 55)-(108, 61) with its fill sprite at (176, 90), 78x5. Antimatter fill
    sprite at (246, 31)."""
    s = Sheet()
    body(s)
    energy_well(s)
    gauge_well(s, 141)
    gauge_well(s, 155)
    x0, y0, x1, y1 = 29, 55, 108, 61
    s.rect(x0, y0, x1, y1, GLASS)
    s.hline(x0, x1, y0, K['o']); s.vline(x0, y0, y1, K['o'])
    s.hline(x0, x1, y1, K['hi']); s.vline(x1, y0, y1, K['hi'])
    player_inventory(s)
    energy_sprite(s)
    all_fluid_sprites(s)
    fluid_sprite(s, 246, ANTIMATTER_FLUID)
    for x in range(78):
        for y in range(5):
            c = mix(ANTIMATTER['off'], ANTIMATTER['on'], x / 70) if x < 70 else mix(ANTIMATTER['on'], ANTIMATTER['hi'], (x - 70) / 7)
            s.px(176 + x, 90 + y, shade(c, 0.8) if y in (0, 4) else c)
    return s.img


def antimatter_arrow(x, y):
    """Icy tier glow broken into chevron segments."""
    base = mix(ANTIMATTER['off'], ANTIMATTER['on'], 0.35 + 0.65 * x / 23)
    return shade(base, 0.72) if (x + abs(y - 8)) % 6 == 0 else base


def antimatter_sheet(arrow_colour, gauges=(), byproduct=False):
    """Tier-5 machines: the tier-3 layout with icy slot rims, fluid tubes at each x in gauges, and (purifier) an
    output + by-product slot pair at (116, 24) / (116, 46) like the Ore Purifier."""
    s = Sheet()
    body(s)
    energy_well(s)
    slot(s, 55, 34)
    if byproduct:
        slot(s, 115, 23, rim=ANTIMATTER['off'])
        slot(s, 115, 45, rim=ANTIMATTER['off'])
    else:
        slot(s, 111, 30, 26, rim=ANTIMATTER['off'])
    arrow_silhouette(s, 79, 34)
    for x in gauges:
        gauge_well(s, x)
    player_inventory(s)
    arrow_sprite(s, arrow_colour)
    energy_sprite(s)
    all_fluid_sprites(s)
    return s.img


def creative_tank_sheet():
    """Creative Fluid Tank: 8 fluid buttons (4 x 2 at (12 + 22c, 20 + 22r), 18x18), a fluid tube at x 155, inventory.
    The screen draws each fluid's own sprite in the buttons and the tube."""
    s = Sheet()
    body(s)
    for i in range(8):
        slot(s, 12 + (i % 4) * 22, 20 + (i // 4) * 22)
    gauge_well(s, 155)
    player_inventory(s)
    return s.img


os.makedirs(OUT, exist_ok=True)
coal_sheet(granulator_arrow).save(os.path.join(OUT, 'coal_granulator.png'))
coal_sheet(smelter_arrow).save(os.path.join(OUT, 'coal_smelter.png'))
electric_sheet(electric_granulator_arrow).save(os.path.join(OUT, 'electric_granulator.png'))
electric_sheet(smelter_arrow).save(os.path.join(OUT, 'electric_smelter.png'))
generator_sheet().save(os.path.join(OUT, 'electric_generator.png'))
battery_sheet().save(os.path.join(OUT, 'energy_battery.png'))
# phase D: the Plasma Generator runs on hydrogen (its old sheet's layout lives on in the water generator's)
fluid_machine_sheet((50, 155), fade_arrow(HYDROGEN[2], PLASMA['on'])).save(os.path.join(OUT, 'plasma_generator.png'))
water_generator_sheet().save(os.path.join(OUT, 'atmospheric_water_generator.png'))
fluid_machine_sheet((50, 131, 155), fade_arrow(WATER[2], HYDROGEN[2])).save(os.path.join(OUT, 'electrolyser.png'))
fluid_machine_sheet((50,), fade_arrow(PLASMA['on'], ELEC['on'])).save(os.path.join(OUT, 'mhd_generator.png'))
# tier 4 (fusion), phase 4A
fusion_fluid_sheet((50, 155), fade_arrow(WATER[2], DEUTERIUM[2])).save(os.path.join(OUT, 'isotope_separator.png'))
fusion_fluid_sheet((50, 155), fade_arrow(DEUTERIUM[2], TRITIUM[2])).save(os.path.join(OUT, 'tritium_breeder.png'))
pellet_press_sheet().save(os.path.join(OUT, 'pellet_press.png'))
fusion_reactor_sheet().save(os.path.join(OUT, 'reactor_controller.png'))
ore_purifier_sheet().save(os.path.join(OUT, 'ore_purifier.png'))
creative_tank_sheet().save(os.path.join(OUT, 'creative_fluid_tank.png'))
# tier 5 (antimatter), phase 5A
accelerator_sheet().save(os.path.join(OUT, 'accelerator_controller.png'))
# phase 5B: antimatter in (x 50) -> FE
_gen = Sheet()
_gen.img = fluid_machine_sheet((50,), fade_arrow(ANTIMATTER_FLUID[2], ELEC['on']))
fluid_sprite(_gen, 246, ANTIMATTER_FLUID)
_gen.img.save(os.path.join(OUT, 'annihilation_generator.png'))
# phase 5C: the antimatter suite and the Froth Flotation Cell (water x141, oxygen x155)
antimatter_sheet(antimatter_arrow).save(os.path.join(OUT, 'antimatter_granulator.png'))
antimatter_sheet(smelter_arrow).save(os.path.join(OUT, 'antimatter_smelter.png'))
antimatter_sheet(washer_arrow, (155,)).save(os.path.join(OUT, 'antimatter_washer.png'))
antimatter_sheet(fade_arrow(OXYGEN[2], ANTIMATTER['on']), (155,), byproduct=True).save(os.path.join(OUT, 'antimatter_purifier.png'))
antimatter_sheet(fade_arrow(WATER[2], OXYGEN[2]), (141, 155)).save(os.path.join(OUT, 'froth_flotation_cell.png'))
tier3_sheet(plasma_granulator_arrow).save(os.path.join(OUT, 'plasma_granulator.png'))
tier3_sheet(smelter_arrow).save(os.path.join(OUT, 'plasma_smelter.png'))
plasma_machine_sheet(plasma_granulator_arrow).save(os.path.join(OUT, 'circuit_assembler.png'))
ore_washer_sheet().save(os.path.join(OUT, 'ore_washer.png'))
print('ok')
