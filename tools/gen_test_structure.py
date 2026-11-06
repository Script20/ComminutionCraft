"""Writes the GameTest structures: boxes with a stone floor and air above - floor_5x3x5 (most tests) and
floor_9x10x9 (room for the 7x7x7 Event Horizon Reactor).

Usage: python tools/gen_test_structure.py   (no third-party packages needed)

Bump DATA_VERSION when Minecraft updates (world_version in the client jar's version.json).
"""
import gzip
import os
import struct

DATA_VERSION = 4790  # Minecraft 26.1.2
DIR = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources',
                   'data', 'comminutioncraft', 'structure', 'gametest')

TAG_END, TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 3, 8, 9, 10


def name(n):
    b = n.encode('utf-8')
    return struct.pack('>H', len(b)) + b


def int_tag(value):
    return struct.pack('>i', value)


def string_tag(value):
    return name(value)


def list_tag(element_type, payloads):
    return struct.pack('>bi', element_type, len(payloads)) + b''.join(payloads)


def compound(entries):
    """entries: list of (tag_type, name, payload_bytes)."""
    out = b''
    for tag_type, key, payload in entries:
        out += struct.pack('>b', tag_type) + name(key) + payload
    return out + struct.pack('>b', TAG_END)


def write(size):
    palette = [
        compound([(TAG_STRING, 'Name', string_tag('minecraft:stone'))]),
        compound([(TAG_STRING, 'Name', string_tag('minecraft:air'))]),
    ]
    blocks = []
    for y in range(size[1]):
        for z in range(size[2]):
            for x in range(size[0]):
                blocks.append(compound([
                    (TAG_LIST, 'pos', list_tag(TAG_INT, [int_tag(x), int_tag(y), int_tag(z)])),
                    (TAG_INT, 'state', int_tag(0 if y == 0 else 1)),
                ]))

    root = struct.pack('>b', TAG_COMPOUND) + name('') + compound([
        (TAG_INT, 'DataVersion', int_tag(DATA_VERSION)),
        (TAG_LIST, 'size', list_tag(TAG_INT, [int_tag(v) for v in size])),
        (TAG_LIST, 'palette', list_tag(TAG_COMPOUND, palette)),
        (TAG_LIST, 'blocks', list_tag(TAG_COMPOUND, blocks)),
        (TAG_LIST, 'entities', list_tag(TAG_END, [])),
    ])
    out = os.path.join(DIR, 'floor_%dx%dx%d.nbt' % size)
    os.makedirs(DIR, exist_ok=True)
    with gzip.GzipFile(out, "wb", mtime=0) as fh:  # mtime=0: same bytes on every run
        fh.write(root)


write((5, 3, 5))
write((9, 10, 9))
print('ok')
