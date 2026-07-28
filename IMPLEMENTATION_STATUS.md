# Implementation Status

**Last updated:** 2026-07-28  
**Version:** 0.1.0  
**Current phase:** Phase 2 complete → Phase 3 next

## Summary

| Phase | Name | Status |
|------:|------|--------|
| 0 | Project foundation | **Complete** |
| 1 | Basic items and materials | **Complete** |
| 2 | Historical debris and investigation | **Complete** |
| 3 | Abandoned printshop structure | Not started |
| 4 | Functional printing press | Not started |
| 5 | Archive book and progression | Not started |
| 6 | Echoes of the past | Not started |
| 7 | Chronicle and location discovery | Not started |
| 8 | Polish and release | Not started |

## Phase 2 checklist (acceptance)

| Task | Status |
|------|--------|
| All required workshop blocks | Done |
| Brush cleaning with progress | Done |
| Three visual states (untouched / partial / full) | Done |
| InvestigationBlockEntity NBT persistence | Done |
| Once-only loot (`LootGenerated`) | Done |
| Weighted server-side loot (no client roll) | Done |
| Results: nothing, type, matrix, press, page, clue, hidden | Done |
| Break/place does not re-roll loot (item data component) | Done |
| Survives chunk unload / restart | Done |
| `/echoesinink debug inspect` | Done |
| EN + PL investigation messages | Done |
| Clean build + dedicated server | Done |

## How to test Phase 2

1. `/echoesinink give_test_items`
2. Place `printing_debris` (or table/shelf/press/cabinet).
3. Hold **Printer's Brush**, use on block until state advances twice.
4. On full investigation: message + possible item loot.
5. Brush again: no second loot.
6. Break the block → pick up → place again → `/echoesinink debug inspect` → `lootGenerated=true`.
7. Leave area / reload world → state and loot flag remain.

## Remaining (next)

### Phase 3
- Abandoned Printshop structure + `/locate`
- Structure processors, biome placement, workshop id

## Tech notes

- Do not `ofFullCopy` axis-dependent log blocks onto plain `Block`.
- Investigation anti-dupe uses `echoes_in_ink:investigation` data component on dropped block items.
