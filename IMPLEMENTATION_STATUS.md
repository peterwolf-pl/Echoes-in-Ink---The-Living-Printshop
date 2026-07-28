# Implementation Status

**Last updated:** 2026-07-28  
**Version:** 0.1.0  
**Current phase:** Phase 7 complete → Phase 8 next

## Summary

| Phase | Name | Status |
|------:|------|--------|
| 0 | Project foundation | **Complete** |
| 1 | Basic items and materials | **Complete** |
| 2 | Historical debris and investigation | **Complete** |
| 3 | Abandoned printshop structure | **Complete** |
| 4 | Functional printing press | **Complete** |
| 5 | Archive book and progression | **Complete** |
| 6 | Echoes of the past | **Complete** |
| 7 | Chronicle and location discovery | **Complete** |
| 8 | Polish and release | Not started |

## Phase 7 checklist

| Task | Status |
|------|--------|
| Restored Chronicle Page progressive use (server-side) | Done |
| Clue stages: biome → bearing → map → precise site | Done |
| Archive unlocks for chronicle clues + ink cache site | Done |
| Ink Archive Cache structure (piece + type + datapack) | Done |
| Cache loot table (matrices, pages, tools) | Done |
| Explorer map with RED_X decoration | Done |
| `/echoesinink locate_cache` | Done |
| Advancement **The Buried Cache** | Done |
| EN + PL language keys | Done |
| Build | Pending this commit |

## How to test Phase 7

1. Investigate a printshop (unlocks dust/hidden clues) and print a restored chronicle page.
2. Optionally witness The Last Print Run (bearing stage prefers echo or hidden clue).
3. Use the Restored Chronicle Page repeatedly:
   - Stage 1: biome flavour text + archive entry
   - Stage 2: compass bearing (N/NE/…) toward nearest cache
   - Stage 3: filled map item (red X)
   - Stage 4: precise coordinates + site archive entry
4. Or skip exploration with `/echoesinink locate_cache` (op).
5. Dig at moss carpet / ladder hatch; loot the cache chest.

## Remaining

### Phase 8 — Polish and release
Sounds, better textures, balance, exploit hardening, screenshots, ModJam checklist, production JAR.
