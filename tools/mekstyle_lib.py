"""Three Mekanism-inspired (original) granulator concepts sharing one casing language."""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, os.path.dirname(__file__))
FRAMES = 8
TAU = 2 * math.pi


def clamp(v):
    return max(0, min(255, int(round(v))))


def rgba(c):
    return tuple(c[:3]) + (255,)


def shade(c, f):
    return tuple(clamp(x * f) for x in c[:3]) + (255,)


def mix(a, b, t):
    return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


# cool, smooth greys
OUTL = (38, 40, 46)
D2 = (52, 55, 62)
D1 = (66, 69, 77)
M = (86, 90, 99)
L1 = (104, 108, 117)
L2 = (124, 128, 137)
HI = (148, 152, 161)
SCREEN = (24, 26, 31)
SCREEN2 = (32, 35, 41)
# tier-1 accent: warm orange
ACC_OFF = (92, 62, 40)
ACC = (255, 150, 40)
ACC_HI = (255, 214, 120)
ORE = [(216, 175, 147), (196, 128, 92), (232, 198, 92), (150, 146, 140)]
FIRE = [(255, 240, 170), (255, 196, 80), (246, 132, 40), (196, 70, 26), (100, 34, 18)]


def fire(t):
    t = max(0.0, min(0.999, t)) * (len(FIRE) - 1)
    i = int(t)
    return mix(FIRE[i], FIRE[i + 1], t - i)


class Tex:
    def __init__(self):
        self.img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))

    def px(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
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

    def panel(self, x0, y0, x1, y1, fill, hi, lo, round_=True):
        """Raised rounded panel."""
        self.rect(x0, y0, x1, y1, fill)
        self.hline(x0, x1, y0, hi)
        self.vline(x0, y0, y1, hi)
        self.hline(x0, x1, y1, lo)
        self.vline(x1, y0, y1, lo)
        if round_:
            for (x, y) in ((x0, y0), (x1, y0), (x0, y1), (x1, y1)):
                self.px(x, y, OUTL)

    def well(self, x0, y0, x1, y1, fill, round_=True):
        """Recessed area: dark top/left lip, light bottom/right lip."""
        self.rect(x0, y0, x1, y1, fill)
        self.hline(x0, x1, y0, D2)
        self.vline(x0, y0, y1, D2)
        self.hline(x0, x1, y1, L2)
        self.vline(x1, y0, y1, L2)
        if round_:
            for (x, y) in ((x0, y0), (x1, y0), (x0, y1), (x1, y1)):
                self.px(x, y, M)


def casing():
    """Shared casing: rounded frame, soft gradient body, dark waist band at y=9..10."""
    t = Tex()
    for y in range(16):
        for x in range(16):
            # soft vertical gradient, no noise (the Mekanism look)
            base = mix(L1, M, y / 15)
            t.px(x, y, base)
    # outer frame
    t.hline(1, 14, 0, HI)
    t.vline(0, 1, 14, L2)
    t.hline(1, 14, 15, OUTL)
    t.vline(15, 1, 14, D2)
    for (x, y) in ((0, 0), (15, 0), (0, 15), (15, 15)):
        t.px(x, y, OUTL)
    # top tab notch (centre), a Mek-ish detail
    t.hline(6, 9, 0, L2)
    t.hline(6, 9, 1, HI)
    # waist band
    t.hline(0, 15, 9, D2)
    t.hline(0, 15, 10, D1)
    t.hline(1, 14, 11, L2)
    return t


def accent_strip(t, x0, x1, y, lit, frame=0):
    for x in range(x0, x1 + 1):
        if lit:
            pulse = 0.85 + 0.15 * math.sin((frame + x) * TAU / FRAMES)
            t.px(x, y, mix(ACC, ACC_HI, 0.3 * pulse))
        else:
            t.px(x, y, ACC_OFF)


def rollers(t, box, frame, lit):
    """Two small crushing rollers inside a screen box."""
    x0, y0, x1, y1 = box
    cy = (y0 + y1) / 2
    w = x1 - x0
    centres = (x0 + w * 0.28, x0 + w * 0.72)
    r = min(w * 0.22, (y1 - y0) / 2 - 0.3)
    phase = frame * (TAU / 4) / FRAMES
    for i, cx in enumerate(centres):
        spin = 1 if i == 0 else -1
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                dx, dy = x - cx, y - cy
                d = math.hypot(dx, dy)
                if d > r + 0.2:
                    continue
                a = (math.atan2(dy, dx) * spin + phase) % TAU
                tooth = int(a / (TAU / 8)) % 2 == 0
                if d > r - 0.8:
                    c = (HI if dy < 0 else L2) if tooth else D2
                elif d < 0.8:
                    c = D2
                else:
                    c = L1 if dx + dy < 0 else M
                t.px(x, y, c)
    if lit:
        mid = int(round((centres[0] + centres[1]) / 2 - 0.5))
        t.px(mid + frame % 2, y0, ORE[frame % 4])
        for k in range(2):
            t.px(mid + (k + frame // 2) % 2, int(cy) + 1 + (frame + 2 * k) % (y1 - int(cy)), ORE[(frame + k + 1) % 4])


def vents(t, x0, x1, y0, y1, lit, frame, step=2):
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            if (x - x0) % step == 0:
                if lit:
                    t.px(x, y, fire(0.1 + 0.6 * (y - y0) / max(1, y1 - y0) + 0.15 * ((x + frame) % 3 == 0)))
                else:
                    t.px(x, y, SCREEN)
            else:
                t.px(x, y, D1)


def iso_block(top, front, side, size=256):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    c30 = math.cos(math.radians(30))
    scale = size / 2.05
    ox, oy = size / 2, size * 0.27

    def proj(p):
        x, y, z = p
        return (ox + (x - z) * c30 * scale, oy + ((x + z) * 0.5 - y) * scale + scale)

    for tex, light, fn in ((top, 1.0, lambda u, v: (u, 1, v)), (front, 0.84, lambda u, v: (u, 1 - v, 1)),
                           (side, 0.68, lambda u, v: (1, 1 - v, 1 - u))):
        for v in range(16):
            for u in range(16):
                c = tex.getpixel((u, v))
                if c[3] == 0:
                    continue
                col = shade(c, light)
                pts = [proj(fn(a / 16, b / 16)) for a, b in ((u, v), (u + 1, v), (u + 1, v + 1), (u, v + 1))]
                d.polygon(pts, fill=col, outline=col)
    return img


def up(img, s):
    return img.resize((img.width * s, img.height * s), Image.NEAREST)


def render_all(DESIGNS, OUT, stem):
    os.makedirs(OUT, exist_ok=True)
    font = ImageFont.load_default(size=22)
    small = ImageFont.load_default(size=15)
    BG = (38, 40, 48, 255)
    COL_W = 300
    sheet = Image.new('RGBA', (COL_W * 3 + 20, 620), BG)
    dr = ImageDraw.Draw(sheet)
    gif_frames = [Image.new('RGBA', (COL_W * 3 + 20, 330), BG) for _ in range(FRAMES)]

    for i, (key, name, ftop, fside, ffront) in enumerate(DESIGNS):
        top, side, front = ftop().img, fside().img, ffront().img
        frames = [ffront(k).img for k in range(FRAMES)]
        d = os.path.join(OUT, f'{key}_{name.lower().replace(" & ", "_").replace(" ", "_")}')
        os.makedirs(d, exist_ok=True)
        top.save(os.path.join(d, 'coal_granulator_top.png'))
        side.save(os.path.join(d, 'coal_granulator_side.png'))
        front.save(os.path.join(d, 'coal_granulator_front.png'))
        strip = Image.new('RGBA', (16, 16 * FRAMES))
        for k, fr in enumerate(frames):
            strip.paste(fr, (0, 16 * k))
        strip.save(os.path.join(d, 'coal_granulator_front_on.png'))

        x0 = 10 + i * COL_W
        dr.text((x0 + 10, 12), f'{key}. {name}', font=font, fill=(235, 235, 240))
        sheet.alpha_composite(iso_block(top, front, side, 140), (x0 + 5, 45))
        sheet.alpha_composite(iso_block(top, frames[2], side, 140), (x0 + 150, 45))
        dr.text((x0 + 60, 185), 'off', font=small, fill=(170, 170, 180))
        dr.text((x0 + 190, 185), 'running', font=small, fill=(170, 170, 180))
        for k, (lab, tx) in enumerate(zip(['top', 'side', 'front', 'on'], [top, side, front, frames[2]])):
            sheet.alpha_composite(up(tx, 4), (x0 + 8 + k * 70, 215))
            dr.text((x0 + 10 + k * 70, 283), lab, font=small, fill=(170, 170, 180))
        for k, fr in enumerate(frames):
            sheet.alpha_composite(up(fr, 2), (x0 + 8 + (k % 4) * 70, 310 + (k // 4) * 40))
        dr.text((x0 + 10, 393), 'animation frames', font=small, fill=(170, 170, 180))
        for gx in range(3):
            for gy in range(2):
                tile = front if (gx, gy) == (1, 1) else side
                sheet.alpha_composite(up(tile, 5), (x0 + 30 + gx * 80, 425 + gy * 80))
        dr.text((x0 + 10, 590), 'next to its own casing', font=small, fill=(170, 170, 180))
        for k in range(FRAMES):
            g = gif_frames[k]
            ImageDraw.Draw(g).text((x0 + 10, 10), f'{key}. {name}', font=font, fill=(235, 235, 240))
            g.alpha_composite(iso_block(top, frames[k], side, 260), (x0 + 15, 50))

    sheet.save(os.path.join(OUT, stem + '.png'))
    pal = [g.convert('RGB').convert('P', palette=Image.ADAPTIVE, colors=255) for g in gif_frames]
    pal[0].save(os.path.join(OUT, stem + '_running.gif'), save_all=True, append_images=pal[1:], duration=100, loop=0)

