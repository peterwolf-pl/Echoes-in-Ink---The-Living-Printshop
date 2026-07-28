# Implementation Status

**Last updated:** 2026-07-28  
**Version:** 0.1.0  
**Current phase:** Phase 4 complete → Phase 5 next

## Summary

| Phase | Name | Status |
|------:|------|--------|
| 0 | Project foundation | **Complete** |
| 1 | Basic items and materials | **Complete** |
| 2 | Historical debris and investigation | **Complete** |
| 3 | Abandoned printshop structure | **Complete** |
| 4 | Functional printing press | **Complete** |
| 5 | Archive book and progression | Not started |
| 6 | Echoes of the past | Not started |
| 7 | Chronicle and location discovery | Not started |
| 8 | Polish and release | Not started |

## Phase 4 checklist

| Task | Status |
|------|--------|
| `printing_press` block + BE | Done |
| Assembly from screw / platen / handle / carriage | Done |
| Physical sequence (world interaction, no furnace GUI) | Done |
| Server phase machine + tick progress | Done |
| 5 printing recipes / outputs | Done |
| Drop contents + parts on break (no double-drop) | Done |
| Client BER animation (carriage / platen / handle) | Done |
| EN + PL messages | Done |
| Build + dedicated server | Done |

## How to test Phase 4

1. `/echoesinink give_test_items`
2. Place **Historical Screw Printing Press**.
3. Right-click with **Press Screw, Platen, Handle, Carriage** (install).
4. Insert **Wooden Printing Matrix**, **Ink Ball**, **Blank Archive Page**.
5. Empty hand: push carriage → pull handle → wait → pull carriage → collect.
6. Sneak + use: status line.
7. Break press: parts and inputs drop.

### Recipes

| Matrix | Paper | Ink | Output |
|--------|-------|-----|--------|
| Wooden matrix | Blank page | Ink ball | Printer's Instruction Sheet |
| Wooden matrix | Damaged page | Ink ball | Restored Chronicle Page |
| Wooden matrix | Blank page | Ink pad | Decorative Woodcut |
| Metal type piece | Blank page | Ink ball | Printed Warning Poster |
| Charcoal rubbing | Blank page | Ink pad | Workshop Map Fragment |

## Remaining (next)

### Phase 5
- Printer's Archive UI + per-player progression + advancements
