# Implementation Status

**Last updated:** 2026-07-28  
**Version:** 0.1.0  
**Current phase:** Phase 3 complete → Phase 4 next

## Summary

| Phase | Name | Status |
|------:|------|--------|
| 0 | Project foundation | **Complete** |
| 1 | Basic items and materials | **Complete** |
| 2 | Historical debris and investigation | **Complete** |
| 3 | Abandoned printshop structure | **Complete** |
| 4 | Functional printing press | Not started |
| 5 | Archive book and progression | Not started |
| 6 | Echoes of the past | Not started |
| 7 | Chronicle and location discovery | Not started |
| 8 | Polish and release | Not started |

## Phase 3 checklist

| Task | Status |
|------|--------|
| Custom structure type + piece registration | Done |
| Procedural ruined workshop layout | Done |
| Workshop room + storage cellar | Done |
| Broken press, tables, cabinets, shelves, debris | Done |
| Hidden compartment chest (damaged chronicle + parts) | Done |
| Storage chest loot table | Done |
| Environmental storytelling props | Done |
| Workshop id stored on structure piece NBT | Done |
| Biome tag + structure set (spacing 32 / sep 12) | Done |
| `terrain_adaptation: beard_thin` | Done |
| Config flag `enablePrintshopGeneration` | Done |
| `/echoesinink locate_printshop` | Done |
| `/locate structure echoes_in_ink:abandoned_printshop` | Supported via datapack |
| Clean build + dedicated server | Done |

## How to test Phase 3

1. Create a **new world** (or explore far enough for new chunks).
2. `/echoesinink locate_printshop` (op) or  
   `/locate structure echoes_in_ink:abandoned_printshop`
3. Visit the ruin: main room, annex, ladder to cellar, chests, investigation blocks.
4. Disable generation via `config/echoes_in_ink.json` → `"enablePrintshopGeneration": false` (requires restart for new chunks).

**Note:** Structure set spacing lives in  
`data/echoes_in_ink/worldgen/structure_set/abandoned_printshops.json`  
(config spacing fields document the intended rarity; edit the JSON to change placement density).

## Remaining (next)

### Phase 4
- Historical Screw Printing Press (assembly + physical sequence + recipes + animation)

## Tech notes

- Structure is code-built (`ScatteredFeaturePiece`), not NBT template — reliable CI and custom blocks.
- Workshop id format: `printshop_<hex>` from chunk coordinates.
