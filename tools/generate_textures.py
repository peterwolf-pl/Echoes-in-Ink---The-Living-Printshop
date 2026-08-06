#!/usr/bin/env python3
"""Generate cohesive Minecraft-style 16x16 textures for Echoes in Ink."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ITEM = ROOT / "src/main/resources/assets/echoes_in_ink/textures/item"
BLOCK = ROOT / "src/main/resources/assets/echoes_in_ink/textures/block"

# ── Palette (historical printshop) ──────────────────────────────────────────
T = (0, 0, 0, 0)
PAPER = (236, 226, 204, 255)
PAPER_S = (206, 192, 166, 255)
PAPER_E = (168, 152, 126, 255)
PAPER_STAIN = (186, 168, 140, 255)
WOOD_L = (170, 120, 68, 255)
WOOD = (124, 80, 44, 255)
WOOD_D = (78, 50, 28, 255)
WOOD_VD = (48, 32, 18, 255)
DUST = (196, 184, 158, 255)
DUST_D = (160, 148, 122, 255)
INK = (24, 24, 34, 255)
INK_M = (48, 50, 64, 255)
INK_L = (72, 74, 90, 255)
METAL_L = (208, 210, 218, 255)
METAL = (158, 160, 170, 255)
METAL_D = (96, 98, 108, 255)
METAL_VD = (58, 60, 68, 255)
BRASS = (200, 162, 72, 255)
BRASS_D = (148, 112, 42, 255)
BRASS_L = (228, 196, 110, 255)
HANDLE = (92, 58, 34, 255)
BRISTLE = (220, 210, 190, 255)
BRISTLE_D = (170, 160, 140, 255)
LENS = (140, 200, 190, 255)
LENS_H = (200, 240, 230, 255)
CHAR = (36, 36, 40, 255)
CHAR_L = (70, 70, 76, 255)
RED = (140, 48, 48, 255)
RED_D = (96, 28, 28, 255)
BOOK = (90, 52, 36, 255)
BOOK_L = (130, 80, 52, 255)
GOLD = (212, 176, 80, 255)


def new_img() -> Image.Image:
    return Image.new("RGBA", (16, 16), T)


def px(img: Image.Image, x: int, y: int, c) -> None:
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), c)


def fill_rect(img: Image.Image, x0: int, y0: int, x1: int, y1: int, c) -> None:
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px(img, x, y, c)


def hline(img: Image.Image, x0: int, x1: int, y: int, c) -> None:
    for x in range(x0, x1 + 1):
        px(img, x, y, c)


def vline(img: Image.Image, x: int, y0: int, y1: int, c) -> None:
    for y in range(y0, y1 + 1):
        px(img, x, y, c)


def save(img: Image.Image, folder: Path, name: str) -> None:
    folder.mkdir(parents=True, exist_ok=True)
    path = folder / f"{name}.png"
    img.save(path, "PNG")
    print(f"  {path.relative_to(ROOT)}")


# ── Items ───────────────────────────────────────────────────────────────────

def item_printers_brush() -> Image.Image:
    img = new_img()
    # handle
    fill_rect(img, 7, 8, 8, 14, HANDLE)
    px(img, 7, 14, WOOD_D)
    px(img, 8, 14, WOOD_D)
    # ferrule
    fill_rect(img, 6, 6, 9, 7, METAL)
    hline(img, 6, 9, 6, METAL_L)
    # bristles
    fill_rect(img, 5, 1, 10, 5, BRISTLE)
    for x in (5, 7, 9):
        vline(img, x, 1, 5, BRISTLE_D)
    hline(img, 5, 10, 1, BRISTLE_D)
    # dust tip
    px(img, 6, 1, DUST)
    px(img, 8, 2, DUST)
    return img


def item_workshop_broom() -> Image.Image:
    img = new_img()
    # long diagonal ash handle
    for x, y in [(11, 1), (10, 2), (10, 3), (9, 4), (9, 5), (8, 6), (8, 7), (7, 8), (7, 9)]:
        px(img, x, y, WOOD_L)
        px(img, x - 1, y, WOOD_D)
    # binding and broad straw head
    fill_rect(img, 5, 9, 8, 10, BRASS_D)
    hline(img, 4, 8, 11, BRISTLE_D)
    hline(img, 3, 8, 12, BRISTLE)
    hline(img, 2, 8, 13, BRISTLE)
    hline(img, 1, 8, 14, BRISTLE_D)
    for x in (2, 4, 6, 8):
        px(img, x, 14, WOOD_D)
    return img


def item_magnifying_lens() -> Image.Image:
    img = new_img()
    # ring
    for x, y in [
        (5, 2), (6, 2), (7, 2), (8, 2), (9, 2),
        (4, 3), (10, 3), (3, 4), (11, 4), (3, 5), (11, 5),
        (3, 6), (11, 6), (3, 7), (11, 7), (4, 8), (10, 8),
        (5, 9), (6, 9), (7, 9), (8, 9), (9, 9),
    ]:
        px(img, x, y, BRASS)
    for x, y in [(5, 3), (9, 3), (4, 4), (10, 4), (4, 7), (10, 7), (5, 8), (9, 8)]:
        px(img, x, y, BRASS_D)
    # glass
    fill_rect(img, 5, 4, 9, 7, LENS)
    px(img, 6, 4, LENS_H)
    px(img, 5, 5, LENS_H)
    # handle
    fill_rect(img, 10, 9, 11, 10, BRASS_D)
    fill_rect(img, 11, 11, 12, 14, WOOD)
    px(img, 12, 14, WOOD_D)
    return img


def item_charcoal_rubbing_paper() -> Image.Image:
    img = new_img()
    fill_rect(img, 3, 2, 12, 13, PAPER)
    hline(img, 3, 12, 2, PAPER_E)
    hline(img, 3, 12, 13, PAPER_S)
    vline(img, 3, 2, 13, PAPER_E)
    vline(img, 12, 2, 13, PAPER_S)
    # faint grid
    for y in (5, 8, 11):
        hline(img, 5, 10, y, PAPER_STAIN)
    return img


def item_blank_archive_page() -> Image.Image:
    img = new_img()
    fill_rect(img, 3, 1, 12, 14, PAPER)
    hline(img, 3, 12, 1, PAPER_E)
    vline(img, 3, 1, 14, PAPER_E)
    vline(img, 12, 1, 14, PAPER_S)
    hline(img, 3, 12, 14, PAPER_S)
    # Truly blank paper: just a few fibres, never faux printed lines.
    for x, y in ((5, 4), (10, 7), (6, 12)):
        px(img, x, y, PAPER_STAIN)
    return img


def item_damaged_archive_page() -> Image.Image:
    img = item_blank_archive_page()
    # tears / stains
    for x, y in [(3, 3), (4, 2), (11, 4), (12, 5), (5, 13), (10, 12), (8, 3)]:
        px(img, x, y, T)
    for x, y in [(6, 5), (7, 6), (8, 7), (6, 9), (9, 10), (7, 11)]:
        px(img, x, y, INK_M)
    px(img, 7, 7, INK)
    px(img, 8, 8, INK)
    return img


def item_ink_ball() -> Image.Image:
    img = new_img()
    # handle
    fill_rect(img, 7, 1, 8, 6, WOOD)
    px(img, 7, 1, WOOD_L)
    # ball
    fill_rect(img, 4, 7, 11, 13, INK)
    fill_rect(img, 5, 6, 10, 6, INK_M)
    fill_rect(img, 5, 14, 10, 14, INK_M)
    px(img, 4, 8, INK_M)
    px(img, 11, 8, INK_M)
    px(img, 6, 8, INK_L)
    px(img, 7, 9, INK_L)
    return img


def item_ink_pad() -> Image.Image:
    img = new_img()
    fill_rect(img, 2, 5, 13, 12, WOOD_D)
    fill_rect(img, 3, 4, 12, 4, WOOD)
    fill_rect(img, 3, 6, 12, 11, INK)
    fill_rect(img, 4, 7, 7, 9, INK_L)
    hline(img, 3, 12, 6, INK_M)
    return img


def item_wooden_printing_matrix() -> Image.Image:
    img = new_img()
    fill_rect(img, 2, 2, 13, 13, WOOD)
    hline(img, 2, 13, 2, WOOD_L)
    vline(img, 2, 2, 13, WOOD_D)
    vline(img, 13, 2, 13, WOOD_VD)
    hline(img, 2, 13, 13, WOOD_VD)
    # carved motif (ornament, no letters)
    fill_rect(img, 5, 5, 10, 10, WOOD_D)
    fill_rect(img, 6, 6, 9, 9, WOOD_VD)
    px(img, 7, 7, WOOD_L)
    px(img, 8, 8, WOOD_L)
    # border carvings
    for x in range(4, 12):
        px(img, x, 4, WOOD_D)
        px(img, x, 11, WOOD_D)
    return img


def item_metal_type_piece() -> Image.Image:
    img = new_img()
    # Locked chase: this item represents a complete reusable forme, not one loose sort.
    fill_rect(img, 1, 1, 14, 14, METAL_D)
    fill_rect(img, 2, 2, 13, 13, METAL)
    fill_rect(img, 3, 3, 12, 12, INK_M)
    # Twelve composed type areas with narrow furniture/gutters between them.
    for y0, y1 in ((3, 4), (6, 7), (9, 10), (12, 12)):
        for x0, x1 in ((3, 5), (7, 9), (11, 12)):
            fill_rect(img, x0, y0, x1, y1, METAL_L)
            px(img, x0, y1, METAL_D)
    hline(img, 1, 14, 1, METAL_L)
    vline(img, 1, 1, 14, METAL_L)
    hline(img, 1, 14, 14, METAL_VD)
    vline(img, 14, 1, 14, METAL_VD)
    return img


def item_press_screw() -> Image.Image:
    img = new_img()
    # vertical screw shaft with thread
    fill_rect(img, 7, 1, 8, 14, METAL)
    for y in range(2, 14, 2):
        hline(img, 6, 9, y, METAL_D)
        hline(img, 7, 8, y, METAL_L)
    # top nut
    fill_rect(img, 5, 1, 10, 3, METAL_L)
    hline(img, 5, 10, 1, METAL)
    # bottom tip
    fill_rect(img, 6, 14, 9, 15, METAL_D)
    return img


def item_press_handle() -> Image.Image:
    img = new_img()
    # long bar
    fill_rect(img, 1, 7, 14, 8, METAL)
    hline(img, 1, 14, 7, METAL_L)
    hline(img, 1, 14, 8, METAL_D)
    # wooden grips
    fill_rect(img, 1, 6, 3, 9, WOOD)
    fill_rect(img, 12, 6, 14, 9, WOOD)
    # center boss
    fill_rect(img, 6, 5, 9, 10, METAL_D)
    fill_rect(img, 7, 6, 8, 9, METAL_L)
    return img


def item_press_platen() -> Image.Image:
    img = new_img()
    fill_rect(img, 2, 4, 13, 12, METAL)
    hline(img, 2, 13, 4, METAL_L)
    hline(img, 2, 13, 12, METAL_D)
    vline(img, 2, 4, 12, METAL_D)
    vline(img, 13, 4, 12, METAL_VD)
    # plate face
    fill_rect(img, 4, 6, 11, 10, METAL_L)
    fill_rect(img, 5, 7, 10, 9, METAL)
    return img


def item_press_carriage() -> Image.Image:
    img = new_img()
    # bed
    fill_rect(img, 1, 6, 14, 11, WOOD)
    hline(img, 1, 14, 6, WOOD_L)
    hline(img, 1, 14, 11, WOOD_D)
    # rails
    hline(img, 1, 14, 12, METAL_D)
    hline(img, 1, 14, 13, METAL)
    # matrix seat
    fill_rect(img, 4, 7, 11, 10, WOOD_D)
    fill_rect(img, 5, 8, 10, 9, WOOD_VD)
    # wheels
    fill_rect(img, 2, 13, 4, 14, METAL_VD)
    fill_rect(img, 11, 13, 13, 14, METAL_VD)
    return img


def item_restored_chronicle_page() -> Image.Image:
    img = new_img()
    fill_rect(img, 3, 1, 12, 14, PAPER)
    hline(img, 3, 12, 1, PAPER_E)
    vline(img, 3, 1, 14, PAPER_E)
    vline(img, 12, 1, 14, PAPER_S)
    hline(img, 3, 12, 14, PAPER_S)
    # Bold drop cap and dense type make this unmistakably printed at distance.
    vline(img, 4, 4, 8, INK)
    hline(img, 4, 6, 4, INK)
    hline(img, 4, 6, 8, INK)
    px(img, 6, 5, INK_M)
    px(img, 6, 7, INK_M)
    for y, x1 in ((4, 10), (6, 11), (8, 10), (10, 11), (12, 9)):
        hline(img, 8 if y < 9 else 5, x1, y, INK_M)
    # Gold archive corner mark.
    fill_rect(img, 4, 2, 5, 3, GOLD)
    return img


def item_charcoal_rubbing() -> Image.Image:
    img = new_img()
    fill_rect(img, 3, 2, 12, 13, PAPER_S)
    hline(img, 3, 12, 2, PAPER_E)
    # charcoal transfer pattern
    fill_rect(img, 5, 4, 10, 11, CHAR)
    fill_rect(img, 6, 5, 9, 10, CHAR_L)
    px(img, 7, 7, PAPER)
    px(img, 8, 8, PAPER)
    for x, y in [(4, 5), (11, 6), (5, 12), (10, 3)]:
        px(img, x, y, CHAR)
    return img


def item_printers_instruction_sheet() -> Image.Image:
    img = new_img()
    fill_rect(img, 3, 1, 12, 14, PAPER)
    hline(img, 3, 12, 1, PAPER_E)
    vline(img, 3, 1, 14, PAPER_E)
    # A heavy title survives mip filtering while the clean body remains
    # available for the localized text rendered on the press.
    fill_rect(img, 4, 3, 11, 4, INK)
    return img


def item_workshop_map_fragment() -> Image.Image:
    img = new_img()
    fill_rect(img, 2, 2, 13, 13, PAPER_STAIN)
    # torn edges
    for x, y in [(2, 2), (3, 2), (2, 3), (13, 12), (12, 13), (13, 13)]:
        px(img, x, y, T)
    # Strong route, north arrow and a large red X.
    hline(img, 4, 10, 5, INK_M)
    vline(img, 7, 4, 11, INK_M)
    hline(img, 5, 10, 10, INK)
    vline(img, 4, 4, 7, INK)
    px(img, 3, 5, INK)
    px(img, 5, 5, INK)
    for x, y in ((9, 7), (10, 8), (11, 9), (11, 7), (10, 8), (9, 9)):
        px(img, x, y, RED if (x + y) % 2 == 0 else RED_D)
    return img


def item_decorative_woodcut() -> Image.Image:
    img = new_img()
    fill_rect(img, 3, 2, 12, 13, PAPER)
    # Dense mirrored woodcut with a double printed border.
    hline(img, 4, 11, 3, INK)
    hline(img, 4, 11, 12, INK)
    vline(img, 4, 3, 12, INK)
    vline(img, 11, 3, 12, INK)
    for x, y in (
        (6, 5), (9, 5), (5, 6), (7, 6), (8, 6), (10, 6),
        (6, 7), (9, 7), (7, 8), (8, 8),
        (6, 9), (9, 9), (5, 10), (7, 10), (8, 10), (10, 10),
    ):
        px(img, x, y, INK if (x + y) % 2 else INK_M)
    return img


def item_printed_warning_poster() -> Image.Image:
    img = new_img()
    fill_rect(img, 3, 1, 12, 14, PAPER)
    # A bold, readable exclamation and heavy type bars.
    fill_rect(img, 4, 3, 11, 4, RED_D)
    fill_rect(img, 7, 5, 8, 9, INK)
    fill_rect(img, 7, 11, 8, 12, INK)
    hline(img, 4, 11, 13, RED)
    px(img, 4, 6, RED)
    px(img, 11, 6, RED)
    return img


def item_printers_archive() -> Image.Image:
    img = new_img()
    fill_rect(img, 3, 2, 12, 14, BOOK)
    fill_rect(img, 4, 3, 11, 13, BOOK_L)
    # spine
    vline(img, 3, 2, 14, WOOD_VD)
    vline(img, 4, 2, 14, WOOD_D)
    # gold band
    hline(img, 5, 11, 5, GOLD)
    hline(img, 5, 11, 10, GOLD)
    # pages edge
    vline(img, 12, 3, 13, PAPER)
    return img


# ── Blocks ──────────────────────────────────────────────────────────────────

def wood_base(light: bool = True) -> Image.Image:
    img = new_img()
    a, b, c = (WOOD_L, WOOD, WOOD_D) if light else (WOOD, WOOD_D, WOOD_VD)
    for y in range(16):
        for x in range(16):
            # plank bands
            band = (y // 4) % 2
            col = a if (x + band) % 3 == 0 else (b if (x + y) % 2 == 0 else c)
            px(img, x, y, col)
    # plank seams
    for y in (3, 7, 11, 15):
        hline(img, 0, 15, y, WOOD_VD if not light else WOOD_D)
    return img


def add_dust(img: Image.Image, amount: int) -> Image.Image:
    """amount 0..3 dust coverage."""
    out = img.copy()
    spots = [
        (1, 1), (4, 2), (8, 1), (12, 3), (2, 5), (6, 6), (10, 5), (14, 7),
        (3, 9), (7, 10), (11, 9), (15, 11), (0, 13), (5, 14), (9, 12), (13, 15),
        (2, 2), (9, 4), (5, 8), (12, 12), (1, 10), (14, 2),
    ]
    for i, (x, y) in enumerate(spots):
        if i < amount * 6:
            px(out, x, y, DUST if i % 2 == 0 else DUST_D)
    return out


def add_ink_stains(img: Image.Image, heavy: bool) -> Image.Image:
    out = img.copy()
    stains = [(3, 4), (4, 5), (5, 4), (8, 9), (9, 10), (10, 9), (12, 2), (2, 12), (7, 7)]
    if heavy:
        stains += [(6, 6), (7, 8), (8, 7), (11, 11), (4, 10), (13, 6)]
    for x, y in stains:
        px(out, x, y, INK if heavy else INK_M)
    return out


def block_printing_debris(stage: str) -> Image.Image:
    """Wood scrap pile used as the main debris face texture."""
    img = wood_base(True)
    if stage == "untouched":
        img = add_dust(img, 3)
        img = add_ink_stains(img, True)
        # metal type bits + paper scraps
        fill_rect(img, 5, 5, 6, 6, METAL_D)
        px(img, 6, 5, METAL)
        px(img, 5, 6, METAL_L)
        fill_rect(img, 10, 9, 11, 10, METAL)
        px(img, 11, 10, BRASS_D)
        fill_rect(img, 9, 3, 12, 5, PAPER_S)
        px(img, 9, 3, PAPER)
        px(img, 12, 5, PAPER_E)
        fill_rect(img, 3, 10, 6, 12, PAPER_E)
        px(img, 4, 11, PAPER_STAIN)
        fill_rect(img, 0, 14, 4, 15, WOOD_VD)
        fill_rect(img, 12, 0, 15, 2, WOOD_D)
    elif stage == "partial":
        img = add_dust(img, 1)
        img = add_ink_stains(img, False)
        fill_rect(img, 6, 6, 8, 8, METAL)
        px(img, 6, 6, METAL_L)
        fill_rect(img, 10, 4, 12, 5, PAPER_S)
    else:  # done
        img = add_dust(img, 0)
        fill_rect(img, 5, 5, 10, 10, WOOD_L)
        fill_rect(img, 6, 6, 9, 9, WOOD)
        px(img, 7, 7, WOOD_L)
    return img


def block_printing_debris_paper() -> Image.Image:
    """Block-atlas paper scrap (item textures cannot be used on block models)."""
    img = new_img()
    fill_rect(img, 0, 0, 15, 15, PAPER)
    for y in range(16):
        for x in range(16):
            if (x + y * 3) % 7 == 0:
                px(img, x, y, PAPER_S)
            if (x * 2 + y) % 11 == 0:
                px(img, x, y, PAPER_E)
    for y in (3, 6, 9, 12):
        for x in range(2, 14):
            if (x + y) % 3 != 0:
                px(img, x, y, PAPER_STAIN if y % 2 else PAPER_E)
    px(img, 4, 5, INK_M)
    px(img, 5, 5, INK_L)
    px(img, 11, 10, INK_M)
    for i in range(16):
        px(img, i, 0, PAPER_E)
        px(img, i, 15, PAPER_S)
        px(img, 0, i, PAPER_E)
        px(img, 15, i, PAPER_S)
    return img


def block_printing_debris_metal() -> Image.Image:
    """Block-atlas lead/type metal for debris pile bits."""
    img = new_img()
    for y in range(16):
        for x in range(16):
            v = (x * 3 + y * 5) % 5
            if v == 0:
                px(img, x, y, METAL_L)
            elif v == 1:
                px(img, x, y, METAL)
            elif v == 2:
                px(img, x, y, METAL_D)
            else:
                px(img, x, y, METAL if (x + y) % 2 == 0 else METAL_D)
    for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11), (8, 8)):
        px(img, cx, cy, METAL_VD)
        px(img, cx + 1, cy, METAL_L)
    fill_rect(img, 6, 2, 9, 3, BRASS_D)
    fill_rect(img, 6, 12, 9, 13, METAL_VD)
    return img


def block_printing_debris_dark() -> Image.Image:
    """Dark underside wood for debris pile."""
    img = new_img()
    for y in range(16):
        for x in range(16):
            band = (x // 4) % 2
            px(img, x, y, WOOD_VD if band == 0 else WOOD_D)
            if (x + y * 2) % 9 == 0:
                px(img, x, y, (32, 20, 12, 255))
    return img


def block_carved_wooden_matrix() -> Image.Image:
    img = wood_base(True)
    fill_rect(img, 3, 3, 12, 12, WOOD_D)
    fill_rect(img, 4, 4, 11, 11, WOOD_VD)
    # relief motif
    fill_rect(img, 6, 6, 9, 9, WOOD_L)
    px(img, 7, 7, WOOD)
    px(img, 8, 8, WOOD)
    hline(img, 5, 10, 5, WOOD)
    hline(img, 5, 10, 10, WOOD)
    vline(img, 5, 5, 10, WOOD)
    vline(img, 10, 5, 10, WOOD)
    return img


def block_dusty_printing_table(stage: str) -> Image.Image:
    img = wood_base(True)
    # tabletop frame
    hline(img, 0, 15, 0, WOOD_VD)
    hline(img, 0, 15, 15, WOOD_VD)
    # work surface mark
    fill_rect(img, 2, 2, 13, 13, WOOD)
    if stage == "untouched":
        img = add_dust(img, 3)
        img = add_ink_stains(img, True)
        # tools silhouette
        fill_rect(img, 4, 4, 6, 10, METAL_D)
        fill_rect(img, 9, 6, 12, 8, PAPER_S)
    elif stage == "partial":
        img = add_dust(img, 1)
        img = add_ink_stains(img, False)
    else:
        fill_rect(img, 3, 3, 12, 12, WOOD_L)
        hline(img, 3, 12, 7, WOOD)
    return img


def block_damaged_archive_shelf(stage: str) -> Image.Image:
    img = new_img()
    fill_rect(img, 0, 0, 15, 15, WOOD_D)
    # shelves
    for y in (2, 7, 12):
        hline(img, 1, 14, y, WOOD_L)
    vline(img, 0, 0, 15, WOOD_VD)
    vline(img, 15, 0, 15, WOOD_VD)
    # books
    books = [(2, 3, BOOK), (5, 3, BOOK_L), (8, 3, INK_M), (11, 3, RED_D),
             (2, 8, BOOK_L), (6, 8, BOOK), (10, 8, GOLD),
             (3, 13, BOOK), (7, 13, INK_M), (11, 13, BOOK_L)]
    for x, y, c in books:
        fill_rect(img, x, y, x + 1, y + 3, c)
    if stage == "untouched":
        img = add_dust(img, 3)
    elif stage == "partial":
        img = add_dust(img, 1)
        # remove some books
        fill_rect(img, 8, 3, 9, 6, WOOD_D)
    else:
        # emptier shelf
        fill_rect(img, 5, 3, 9, 6, WOOD_D)
        fill_rect(img, 6, 8, 10, 11, WOOD_D)
    return img


def block_broken_press_frame(stage: str) -> Image.Image:
    img = wood_base(False)
    # frame posts
    fill_rect(img, 1, 1, 3, 14, WOOD_VD)
    fill_rect(img, 12, 1, 14, 14, WOOD_VD)
    fill_rect(img, 1, 1, 14, 3, WOOD_D)
    if stage == "untouched":
        img = add_dust(img, 3)
        # broken metal
        fill_rect(img, 6, 6, 9, 12, METAL_D)
        px(img, 7, 5, METAL_VD)
        px(img, 8, 13, METAL_VD)
    elif stage == "partial":
        img = add_dust(img, 1)
        fill_rect(img, 6, 6, 9, 12, METAL)
    else:
        fill_rect(img, 6, 5, 9, 13, METAL_L)
        fill_rect(img, 5, 4, 10, 5, METAL)
    return img


def block_collapsed_type_cabinet(stage: str) -> Image.Image:
    img = wood_base(False)
    # drawers
    for y0 in (1, 6, 11):
        fill_rect(img, 2, y0, 13, y0 + 3, WOOD)
        hline(img, 2, 13, y0, WOOD_L)
        # handle
        fill_rect(img, 7, y0 + 1, 8, y0 + 2, METAL)
    if stage == "untouched":
        img = add_dust(img, 3)
        # spilled type
        for x, y in [(3, 5), (5, 5), (10, 10), (12, 15), (4, 14)]:
            px(img, x, y, METAL_L)
    elif stage == "partial":
        img = add_dust(img, 1)
        # open drawer
        fill_rect(img, 2, 6, 13, 9, WOOD_L)
        for x in range(3, 13, 2):
            px(img, x, 8, METAL)
    else:
        fill_rect(img, 3, 2, 12, 4, WOOD_L)
        for x in range(4, 12, 2):
            px(img, x, 3, METAL_L)
    return img


def block_ink_stained_floorboards(stage: str) -> Image.Image:
    img = wood_base(False)
    if stage == "untouched":
        img = add_ink_stains(img, True)
        img = add_dust(img, 2)
        fill_rect(img, 5, 5, 10, 10, INK)
        fill_rect(img, 6, 6, 9, 9, INK_M)
    elif stage == "partial":
        img = add_ink_stains(img, False)
        fill_rect(img, 6, 6, 9, 9, INK_M)
    else:
        # cleaned but faint stain remains
        for x, y in [(7, 7), (8, 8), (6, 8)]:
            px(img, x, y, INK_L)
    return img


def block_faded_workshop_plaque(stage: str) -> Image.Image:
    img = new_img()
    fill_rect(img, 0, 0, 15, 15, WOOD_VD)
    fill_rect(img, 1, 1, 14, 14, WOOD_D)
    # worn brass rim and four fasteners, based on the generated press-plaque concept
    hline(img, 1, 14, 1, BRASS_L if stage == "done" else BRASS)
    hline(img, 1, 14, 14, BRASS_D)
    vline(img, 1, 1, 14, BRASS)
    vline(img, 14, 1, 14, BRASS_D)
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        px(img, x, y, BRASS_L if stage == "done" else BRASS_D)
    # screw press maker's mark: cap, screw, platen, posts, bed and base
    metal = BRASS_L if stage == "done" else BRASS
    shadow = BRASS if stage == "done" else BRASS_D
    hline(img, 4, 11, 4, metal)
    px(img, 4, 5, shadow)
    px(img, 11, 5, shadow)
    for y in range(5, 10):
        px(img, 7, y, metal if y % 2 else shadow)
        px(img, 8, y, shadow if y % 2 else metal)
    hline(img, 5, 10, 9, metal)
    vline(img, 4, 6, 12, shadow)
    vline(img, 11, 6, 12, shadow)
    hline(img, 5, 10, 11, metal)
    hline(img, 4, 11, 12, shadow)
    hline(img, 3, 12, 13, metal)
    if stage == "untouched":
        for x, y in [(3, 3), (6, 4), (9, 5), (5, 8), (10, 9), (6, 12), (12, 11)]:
            px(img, x, y, DUST_D)
    elif stage == "partial":
        for x, y in [(3, 3), (10, 9), (12, 11)]:
            px(img, x, y, DUST)
    return img


def block_press_wood() -> Image.Image:
    return wood_base(False)


def block_press_metal() -> Image.Image:
    img = new_img()
    for y in range(16):
        for x in range(16):
            v = METAL if (x + y) % 3 else METAL_D
            if x < 2 or y < 2:
                v = METAL_L
            if x > 13 or y > 13:
                v = METAL_VD
            px(img, x, y, v)
    # rivets
    for x, y in [(3, 3), (12, 3), (3, 12), (12, 12), (8, 8)]:
        px(img, x, y, METAL_L)
        px(img, x + 1, y, METAL_D)
    return img


def block_press_stone() -> Image.Image:
    img = new_img()
    base = (168, 168, 160, 255)
    dark = (120, 120, 112, 255)
    light = (200, 200, 192, 255)
    for y in range(16):
        for x in range(16):
            px(img, x, y, base if (x // 2 + y // 2) % 2 == 0 else dark)
    hline(img, 0, 15, 0, light)
    vline(img, 0, 0, 15, light)
    return img


def main() -> None:
    print("Generating Echoes in Ink textures…")
    items = {
        "workshop_broom": item_workshop_broom,
        "printers_brush": item_printers_brush,
        "magnifying_lens": item_magnifying_lens,
        "charcoal_rubbing_paper": item_charcoal_rubbing_paper,
        "blank_archive_page": item_blank_archive_page,
        "damaged_archive_page": item_damaged_archive_page,
        "ink_ball": item_ink_ball,
        "ink_pad": item_ink_pad,
        "wooden_printing_matrix": item_wooden_printing_matrix,
        "metal_type_piece": item_metal_type_piece,
        "press_screw": item_press_screw,
        "press_handle": item_press_handle,
        "press_platen": item_press_platen,
        "press_carriage": item_press_carriage,
        "restored_chronicle_page": item_restored_chronicle_page,
        "charcoal_rubbing": item_charcoal_rubbing,
        "printers_instruction_sheet": item_printers_instruction_sheet,
        "workshop_map_fragment": item_workshop_map_fragment,
        "decorative_woodcut": item_decorative_woodcut,
        "printed_warning_poster": item_printed_warning_poster,
        "printers_archive": item_printers_archive,
    }
    for name, fn in items.items():
        save(fn(), ITEM, name)

    blocks = {
        "printing_debris_untouched": lambda: block_printing_debris("untouched"),
        "printing_debris_partial": lambda: block_printing_debris("partial"),
        "printing_debris_done": lambda: block_printing_debris("done"),
        "printing_debris_paper": block_printing_debris_paper,
        "printing_debris_metal": block_printing_debris_metal,
        "printing_debris_dark": block_printing_debris_dark,
        "carved_wooden_matrix": block_carved_wooden_matrix,
        "dusty_printing_table_untouched": lambda: block_dusty_printing_table("untouched"),
        "dusty_printing_table_partial": lambda: block_dusty_printing_table("partial"),
        "dusty_printing_table_done": lambda: block_dusty_printing_table("done"),
        "damaged_archive_shelf_untouched": lambda: block_damaged_archive_shelf("untouched"),
        "damaged_archive_shelf_partial": lambda: block_damaged_archive_shelf("partial"),
        "damaged_archive_shelf_done": lambda: block_damaged_archive_shelf("done"),
        "press_frame_untouched": lambda: block_broken_press_frame("untouched"),
        "press_frame_partial": lambda: block_broken_press_frame("partial"),
        "press_frame_done": lambda: block_broken_press_frame("done"),
        "collapsed_type_cabinet_untouched": lambda: block_collapsed_type_cabinet("untouched"),
        "collapsed_type_cabinet_partial": lambda: block_collapsed_type_cabinet("partial"),
        "collapsed_type_cabinet_done": lambda: block_collapsed_type_cabinet("done"),
        "ink_stained_floorboards_untouched": lambda: block_ink_stained_floorboards("untouched"),
        "ink_stained_floorboards_partial": lambda: block_ink_stained_floorboards("partial"),
        "ink_stained_floorboards_done": lambda: block_ink_stained_floorboards("done"),
        "faded_workshop_plaque_untouched": lambda: block_faded_workshop_plaque("untouched"),
        "faded_workshop_plaque_partial": lambda: block_faded_workshop_plaque("partial"),
        "faded_workshop_plaque_done": lambda: block_faded_workshop_plaque("done"),
        "press_wood": block_press_wood,
        "press_metal": block_press_metal,
        "press_stone": block_press_stone,
    }
    for name, fn in blocks.items():
        save(fn(), BLOCK, name)

    print(f"Done: {len(items)} items + {len(blocks)} blocks")


if __name__ == "__main__":
    main()
