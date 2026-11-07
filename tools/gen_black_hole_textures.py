"""Textures for the Event Horizon Reactor's black hole renderer (tier 6, phase 6A part 2), made from the approved
mock-up (design/previews/singularity_mockup.py: same colour ramp and streak noise). Smooth, not pixel art - owner OK'd
that for this one effect. Needs numpy (tools/.venv).

Writes assets/comminutioncraft/textures/entity/black_hole/:
  halo.png   - camera-facing lensed arch round the hole + the photon ring (colour + alpha from the brightness)
  glow.png   - soft warm glow behind everything (fakes bloom)
  disc.png   - the accretion disc seen from above (colour + alpha); the renderer spins rings of it at Kepler speeds and
               brightens the side moving towards the camera
  shadow.png - the black hole itself: a black disc (cutout, writes depth so the far half of the disc hides behind it)
  field.png  - faint gold shimmer for the deadly 5x5x5 field (scrolled by the renderer)

Run: tools/.venv/Scripts/python tools/gen_black_hole_textures.py
"""
import json
import math
import os
import sys

import numpy as np
from PIL import Image

TOOLS = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(TOOLS, '..', 'design', 'previews'))
import singularity_mockup as mock  # noqa: E402

OUT = os.path.join(TOOLS, '..', 'src', 'main', 'resources', 'assets', 'comminutioncraft', 'textures', 'entity', 'black_hole')

# Shared with BlackHoleRenderer: in shadow radii.
HALO_EXTENT = 2.4      # halo quad half-size
DISC_INNER = 1.45      # disc inner edge
DISC_OUTER = 5.4       # disc outer edge (= the disc quad's half-size)


def tone(rgb, exposure):
    return np.clip(1 - np.exp(-rgb * exposure), 0, 1)


def save(arr, name, alpha=None, blur=True):
    arr = np.clip(arr, 0, 1)
    if alpha is None:
        # Light-only layers: drawn with normal (translucent) blending, so the brightness becomes the alpha and the
        # colour is kept at full strength - they keep their gold over a bright sky instead of adding up to white.
        lum = arr.max(axis=2)
        alpha = (np.clip(lum * 1.15, 0, 1) * 255).astype(np.uint8)
        arr = arr / np.maximum(lum, 1e-4)[..., None]
    a = (np.clip(arr, 0, 1) * 255).astype(np.uint8)
    Image.fromarray(np.dstack([a, alpha]), 'RGBA').save(os.path.join(OUT, name))
    if blur:  # smooth textures: linear filtering, so they don't go blocky up close
        with open(os.path.join(OUT, name + '.mcmeta'), 'w') as fh:
            json.dump({"texture": {"blur": True}}, fh, indent=2)
            fh.write('\n')


def grid(n, extent):
    y, x = np.mgrid[0:n, 0:n].astype(np.float64)
    x = (x + 0.5) / n * 2 - 1
    y = (y + 0.5) / n * 2 - 1
    return x * extent, -y * extent  # +y up


def halo(n=512):
    x, y = grid(n, HALO_EXTENT)
    r = np.hypot(x, y)
    theta = np.arctan2(y, x)
    up = np.sin(theta)
    rs = 1.0
    inner = rs * 1.035
    thick = rs * (0.22 + 0.85 * np.clip(up, 0, 1) ** 0.7 + 0.16 * np.clip(-up, 0, 1))
    u = (r - inner) / thick
    uc = np.clip(u, 0, 1)
    band = np.where((u > 0) & (u < 1), (1 - uc) ** 1.3 * np.clip(uc * 14, 0, 1), 0)
    px = r * 118  # the mock-up's streaks were tuned in pixels at a shadow radius of 118
    streak = (0.1 + 0.9 * mock.streaks(px * 2.1 + 3 * np.cos(theta * 3), mock.RADIAL) ** 2.6) \
        * (0.5 + 0.5 * mock.streaks(theta * 8 + px * 0.05, mock.BREAK))
    side = 0.55 + 0.45 * np.clip(up, 0, 1) + 0.18 * np.clip(-up, 0, 1)
    light = band * streak * side * 4.2
    temp = (0.45 + 0.35 * (1 - uc) ** 2) * light
    ring = np.exp(-((r - rs * 1.015) * 118) ** 2 / (2 * 2.2 ** 2)) * (1.0 + 0.6 * np.clip(up, 0, 1)) * 0.45
    light = light + ring
    temp = temp + ring * 0.72
    t = np.where(light > 1e-6, temp / np.maximum(light, 1e-6), 0)
    rgb = mock.ramp(t) * light[..., None]
    fade = np.clip((HALO_EXTENT - r) / 0.25, 0, 1)[..., None]  # nothing at the quad's edge
    save(tone(rgb, 2.3) * fade, 'halo.png')


def glow(n=256):
    x, y = grid(n, 1.0)
    r = np.hypot(x, y)
    k = np.clip(1 - r, 0, 1) ** 2.2
    rgb = np.array([1.0, 0.62, 0.30])[None, None, :] * k[..., None] * 0.55
    save(rgb, 'glow.png')


def disc(n=1024):
    x, y = grid(n, DISC_OUTER)
    rho = np.hypot(x, y)
    phi = np.arctan2(y, x)
    v = (rho - DISC_INNER) / (DISC_OUTER - DISC_INNER)
    edge = np.clip((rho - DISC_INNER) / 0.10, 0, 1) * np.clip((DISC_OUTER - rho) / 1.8, 0, 1)
    px = rho * 118
    swirl = mock.streaks(phi * 6 + px * 0.035, mock.SWIRL)
    brk = mock.streaks(phi * 2.0 + px * 0.11, mock.BREAK)
    fine = mock.streaks(px * 1.3 + 9 * np.sin(phi) * (1 - np.clip(v, 0, 1)), mock.RADIAL)
    s = (0.06 + 0.94 * fine ** 2.8) * (0.35 + 0.65 * swirl ** 1.4) * (0.4 + 0.6 * brk)
    falloff = (DISC_INNER / np.maximum(rho, DISC_INNER)) ** 1.5
    light = np.where(rho < DISC_OUTER, edge * s * falloff * 3.2, 0)
    t = np.clip(0.8 - v * 0.6, 0, 1)
    rgb = mock.ramp(t) * light[..., None]
    save(tone(rgb, 2.0), 'disc.png')


def shadow(n=128):
    x, y = grid(n, 1.0)
    r = np.hypot(x, y)
    alpha = np.where(r <= 1.0, 255, 0).astype(np.uint8)
    save(np.zeros((n, n, 3)), 'shadow.png', alpha, blur=False)


def field(n=64):
    x, y = np.mgrid[0:n, 0:n].astype(np.float64) / n
    k = 0.5 + 0.5 * np.sin((x * 3 + y * 2) * math.tau) * np.sin((x * 2 - y * 3) * math.tau)
    lines = np.exp(-((np.sin(y * math.tau * 4 + np.sin(x * math.tau) * 0.8)) ** 2) / 0.02)
    light = 0.10 + 0.25 * k ** 3 + 0.35 * lines * (0.4 + 0.6 * k)
    rgb = np.array([1.0, 0.73, 0.22])[None, None, :] * light[..., None]
    save(rgb, 'field.png')


if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    halo()
    glow()
    disc()
    shadow()
    field()
    print('ok')
