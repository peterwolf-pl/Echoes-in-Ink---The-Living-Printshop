#!/usr/bin/env python3
"""16x16 hanging-poster designs matching the existing printshop palette."""
from pathlib import Path
from generate_textures import (
    BLOCK,
    INK,
    INK_L,
    INK_M,
    PAPER,
    PAPER_E,
    PAPER_S,
    PAPER_STAIN,
    RED,
    RED_D,
    fill_rect,
    hline,
    new_img,
    px,
    save,
    vline,
)


def poster_woodcut():
    img = new_img()
    fill_rect(img, 1, 1, 14, 14, PAPER)
    fill_rect(img, 2, 2, 13, 13, PAPER_S)
    hline(img, 3, 12, 3, INK)
    hline(img, 3, 12, 12, INK)
    vline(img, 3, 3, 12, INK)
    vline(img, 12, 3, 12, INK)
    # leaf-and-screw woodcut
    for x, y in (
        (6, 5), (9, 5), (5, 6), (7, 6), (8, 6), (10, 6),
        (6, 7), (9, 7), (7, 8), (8, 8),
        (6, 9), (9, 9), (5, 10), (7, 10), (8, 10), (10, 10),
    ):
        px(img, x, y, INK if (x + y) % 2 == 0 else INK_M)
    return img


def poster_chronicle():
    img = new_img()
    fill_rect(img, 1, 1, 14, 14, PAPER)
    fill_rect(img, 2, 2, 13, 4, INK)
    for y in (6, 8, 10, 12):
        hline(img, 3, 6, y, INK_M)
        hline(img, 8, 12, y, INK if y % 4 == 2 else INK_L)
    vline(img, 7, 6, 12, PAPER_E)
    return img


def poster_notice():
    img = new_img()
    fill_rect(img, 1, 1, 14, 14, PAPER_STAIN)
    fill_rect(img, 2, 2, 13, 4, RED_D)
    fill_rect(img, 5, 6, 10, 10, RED)
    hline(img, 3, 12, 12, INK)
    px(img, 4, 7, INK)
    px(img, 11, 7, INK)
    return img


def poster_map():
    img = new_img()
    fill_rect(img, 1, 1, 14, 14, PAPER)
    fill_rect(img, 2, 2, 13, 13, PAPER_S)
    hline(img, 3, 10, 5, INK_M)
    vline(img, 10, 5, 11, INK_M)
    hline(img, 6, 10, 11, INK)
    px(img, 6, 11, RED_D)
    px(img, 5, 10, RED)
    px(img, 7, 10, RED)
    px(img, 6, 12, RED)
    px(img, 4, 7, INK_L)
    px(img, 12, 8, INK_L)
    return img


def poster_specimen():
    img = new_img()
    fill_rect(img, 1, 1, 14, 14, PAPER)
    hline(img, 2, 13, 2, INK)
    # type-case grid of different letter weights
    for i, x in enumerate((3, 6, 9, 12)):
        fill_rect(img, x, 4, x + 1, 6, INK if i % 2 == 0 else INK_M)
        fill_rect(img, x, 8, x + 1, 10 if i != 2 else 12, INK_M if i % 2 == 0 else INK)
    hline(img, 2, 13, 13, PAPER_E)
    return img


def main() -> None:
    designs = {
        "hanging_poster_woodcut": poster_woodcut,
        "hanging_poster_chronicle": poster_chronicle,
        "hanging_poster_notice": poster_notice,
        "hanging_poster_map": poster_map,
        "hanging_poster_specimen": poster_specimen,
    }
    for name, fn in designs.items():
        save(fn(), BLOCK, name)


if __name__ == "__main__":
    main()
