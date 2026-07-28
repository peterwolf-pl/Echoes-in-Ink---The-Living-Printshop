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

## Phase 0 checklist

| Task | Status |
|------|--------|
| Gradle + Loom (MC 26.2, Java 25) | Done |
| `fabric.mod.json` + package layout | Done |
| Common + client entrypoints | Done |
| Logging | Done |
| Config system (`config/echoes_in_ink.json`) | Done |
| Data generation entrypoint | Done |
| EN + PL translations | Done |
| Dev commands (`/echoesinink …`) | Done |
| Placeholder icon | Done |
| `.gitignore` + MPL-2.0 licence | Done |
| Tracking docs | Done |
| Clean Gradle build | Done |
| Dedicated server run | Done (mod loads, Done!) |
| Client run | Pending manual check |

## Phase 1 checklist

| Task | Status |
|------|--------|
| All required items registered | Done |
| Printer's Brush (timed clean, durability, sound, particles, config) | Done |
| Magnifying Lens (server inspect + cooldown, ≥3 findings) | Done |
| Charcoal Rubbing Paper (data component pattern id) | Done |
| Test debris block with investigation states | Done |
| Carved matrix for rubbings | Done |
| EN + PL names/tooltips | Done |
| Creative tab | Done |
| `/echoesinink give_test_items` gives all items | Done |
| Temporary vanilla-style textures | Done |
| Multiplayer-safe item behaviour (server authority) | Done |
| Clean build after Phase 1 | Done |
| Dedicated server after Phase 1 | Done |

## Completed this session

- Fabric project for Minecraft **26.2** / Fabric Loader **0.19.3** / Fabric API **0.155.2+26.2** / Java **25**.
- Phase 0 foundation + Phase 1 items/blocks/tools.
- Custom data component `echoes_in_ink:rubbing_pattern`.
- Config-driven brush duration/durability and lens cooldown.

## Phase 2 checklist

| Task | Status |
|------|--------|
| Investigation block entity with NBT persistence | Done |
| Once-only loot flag (`LootGenerated`) | Done |
| Weighted server-side loot profiles | Done |
| Results: nothing / type / matrix / press / page / clue / hidden | Done |
| Three visual states on investigatable blocks | Done |
| Clean → loot only on fully investigated | Done |
| Build + dedicated server | Done |

## Remaining (next)

### Phase 3
- Abandoned Printshop structure + `/locate`
- Structure processors, biome placement, workshop id

### Later phases
- Press multiblock, archive, echoes, chronicle chain

## Notes / pitfalls fixed

- Do **not** `ofFullCopy` axis-dependent log blocks onto plain `Block` (crashes on AXIS property in light/occlusion lambdas). Use planks-like copies instead.
- `GameProfile` no longer exposes `getName()` in this toolchain — use `ServerPlayer#getScoreboardName()`.

## Tech stack

- Fabric API: `0.155.2+26.2`
- Loader: `0.19.3`
- Loom: `1.16-SNAPSHOT` (resolved 1.16.3)
- Repo: https://github.com/peterwolf-pl/Echoes-in-Ink---The-Living-Printshop
