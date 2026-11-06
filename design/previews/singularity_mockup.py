"""Mock-up of the Event Horizon Reactor's black hole (tier 6), before the renderer is written.

It's built from the same layers the in-game block entity renderer would draw, so what's approved here is what can be
built:
  1. lensed halo   - the far side of the accretion disc, bent up over the top and under the bottom of the hole
                     (always faces the camera, which is also what real lensing does)
  2. far disc      - the back half of the flat disc, behind the hole
  3. shadow        - the black disc of the event horizon
  4. photon ring   - a thin, white-hot ring hugging the shadow
  5. near disc     - the front half of the disc, crossing in front of the hole; one side brighter (Doppler beaming)
  6. bloom         - a soft glow over everything bright
The disc turns (inner parts faster), so the streaks crawl round.

Run: tools/.venv/Scripts/python design/previews/singularity_mockup.py design/previews/singularity.png design/previews/singularity.gif
"""
import math
import sys

import numpy as np
from PIL import Image, ImageFilter

TAU = math.tau
RNG = np.random.default_rng(7)

# white-hot -> peach -> orange -> deep ember, by "temperature" 0..1
RAMP = np.array([
    [0.05, 0.015, 0.01],
    [0.42, 0.12, 0.04],
    [0.95, 0.42, 0.16],
    [1.00, 0.72, 0.48],
    [1.00, 0.92, 0.82],
    [1.00, 1.00, 0.98],
])


def ramp(t):
    t = np.clip(t, 0, 1) * (len(RAMP) - 1)
    i = np.minimum(t.astype(int), len(RAMP) - 2)
    f = (t - i)[..., None]
    return RAMP[i] * (1 - f) + RAMP[i + 1] * f


def noise_bank(n, fmin, fmax):
    return [(RNG.uniform(fmin, fmax), RNG.uniform(0, TAU), RNG.uniform(0.3, 1.0)) for _ in range(n)]


RADIAL = noise_bank(60, 0.015, 0.42)       # concentric streaks (per px of radius), many sizes so they don't repeat
SWIRL = noise_bank(10, 0.6, 3.0)          # clumps along the orbit
BREAK = noise_bank(14, 0.8, 5.0)          # breaks the streaks up along the orbit


def streaks(r, bank):
    out = np.zeros_like(r)
    total = 0
    for f, p, a in bank:
        out += a * np.sin(r * f + p)
        total += a
    return 0.5 + 0.5 * out / total


def render(W=1400, H=780, rs=118, tilt_deg=12.0, t=0.0, exposure=2.3):
    y, x = np.mgrid[0:H, 0:W].astype(np.float64)
    cx, cy = W * 0.52, H * 0.5
    dx, dy = x - cx, y - cy
    r = np.hypot(dx, dy)
    theta = np.arctan2(-dy, dx)  # 0 = right, +pi/2 = up

    light = np.zeros((H, W))      # emission strength
    temp = np.zeros((H, W))       # colour temperature (emission-weighted)

    def add(intensity, temperature):
        nonlocal light, temp
        temp = temp + intensity * temperature
        light = light + intensity

    # ---- 1. lensed halo: a ring round the shadow, thick and bright over the top, thinner underneath
    up = np.sin(theta)
    inner = rs * 1.035
    thick = rs * (0.22 + 0.85 * np.clip(up, 0, 1) ** 0.7 + 0.16 * np.clip(-up, 0, 1))
    u = (r - inner) / thick
    uc = np.clip(u, 0, 1)
    band = np.where((u > 0) & (u < 1), (1 - uc) ** 1.3 * np.clip(uc * 14, 0, 1), 0)
    halo_streak = (0.1 + 0.9 * streaks(r * 2.1 + 3 * np.cos(theta * 3), RADIAL) ** 2.6) * (0.5 + 0.5 * streaks(theta * 8 + r * 0.05, BREAK))
    side = 0.55 + 0.45 * np.clip(up, 0, 1) + 0.18 * np.clip(-up, 0, 1)
    doppler_h = 1 + 0.5 * np.cos(theta - math.pi)  # the left side brighter
    halo = band * halo_streak * side * doppler_h * 4.2
    add(halo, 0.45 + 0.35 * (1 - uc) ** 2)

    # ---- disc maths: the flat disc seen nearly edge-on (tilt above its plane), given real thickness by stacking
    # a few copies of it a little above and below its plane (fluffier towards the edge)
    s = math.sin(math.radians(tilt_deg))
    r_in, r_out = rs * 1.45, rs * 5.4

    def disc_layer(shift):
        ddy = dy - shift
        rho = np.hypot(dx, ddy / s)
        phi = np.arctan2(ddy / s, dx)  # disc angle; ddy > 0 = near side (below the centre on screen)
        v = (rho - r_in) / (r_out - r_in)
        edge = np.clip((rho - r_in) / (rs * 0.10), 0, 1) * np.clip((r_out - rho) / (rs * 1.8), 0, 1)
        spin = t * TAU * (r_in / np.maximum(rho, r_in)) ** 1.5  # Kepler: inner parts orbit faster
        swirl = streaks((phi - spin) * 6 + rho * 0.035, SWIRL)
        brk = streaks((phi - spin) * 2.0 + rho * 0.11, BREAK)
        fine = streaks(rho * 1.3 + 9 * np.sin(phi - spin) * (1 - v), RADIAL)
        disc_streak = (0.06 + 0.94 * fine ** 2.8) * (0.35 + 0.65 * swirl ** 1.4) * (0.4 + 0.6 * brk)
        falloff = (r_in / np.maximum(rho, r_in)) ** 1.5
        doppler = np.clip(1 + 1.05 * -np.cos(phi), 0.12, None)  # approaching (left) side far brighter
        disc = np.where(rho < r_out * 1.05, edge * disc_streak * falloff * doppler * 6.0, 0)
        return disc, np.clip(0.8 - v * 0.6, 0, 1), ddy < 0

    layers = [disc_layer(k * rs * 0.035) + (math.exp(-k * k / 4.5),) for k in range(-3, 4)]
    wsum = sum(w for *_, w in layers)
    disc_far = sum(np.where(far, d, 0) * w for d, _, far, w in layers) / wsum
    disc_near = sum(np.where(~far, d, 0) * w for d, _, far, w in layers) / wsum
    disc_temp = layers[3][1]

    # ---- 2. far half of the disc (behind the hole)
    add(disc_far * 0.85, disc_temp)

    # ---- 3. shadow: everything behind it goes dark
    shadow = np.clip((rs - r) / 2.0 + 0.5, 0, 1)
    light = light * (1 - shadow)
    temp = temp * (1 - shadow)

    # ---- 4. photon ring
    ring = np.exp(-((r - rs * 1.015) ** 2) / (2 * 2.2 ** 2)) * (1.0 + 0.6 * np.clip(up, 0, 1)) * doppler_h
    add(ring * 0.45, np.ones_like(r) * 0.72)

    # ---- 5. near half of the disc (in front of the hole)
    add(disc_near * 2.1, disc_temp)

    # ---- colour, bloom, tone map
    t_avg = np.where(light > 1e-6, temp / np.maximum(light, 1e-6), 0)
    rgb = ramp(t_avg) * light[..., None]
    glow_src = Image.fromarray((np.clip(rgb * 0.35, 0, 1) * 255).astype(np.uint8))
    bloom = np.asarray(glow_src.filter(ImageFilter.GaussianBlur(28)), dtype=np.float64) / 255
    bloom2 = np.asarray(glow_src.filter(ImageFilter.GaussianBlur(7)), dtype=np.float64) / 255
    rgb = rgb + bloom * 1.0 + bloom2 * 0.45
    out = 1 - np.exp(-rgb * exposure)

    # faint stars, darkened near the hole (they'd be lensed away)
    stars = RNG.random((H, W)) > 0.9993
    starlight = np.where(stars, RNG.uniform(0.25, 0.8, (H, W)), 0) * np.clip((r - rs * 1.6) / (rs * 2), 0, 1) * (light < 0.05)
    out = np.clip(out + starlight[..., None] * np.array([0.85, 0.88, 1.0]), 0, 1)
    return Image.fromarray((out * 255).astype(np.uint8))


if __name__ == '__main__':
    render().save(sys.argv[1])
    frames = []
    for k in range(16):
        im = render(W=700, H=390, rs=59, t=k / 16)
        frames.append(im.convert('P', palette=Image.ADAPTIVE, colors=255))
    frames[0].save(sys.argv[2], save_all=True, append_images=frames[1:], duration=90, loop=0)
    print('wrote', sys.argv[1], sys.argv[2])
