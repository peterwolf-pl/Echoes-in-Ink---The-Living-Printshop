# Known Issues

## Active

| ID | Severity | Description | Status |
|----|----------|-------------|--------|
| TEX-01 | Minor | Pixel art is handmade 16×16 procedural style (not hand-painted HD). Can be upgraded later. | Softened by art pass |
| CFG-01 | Minor | Structure spacing JSON is not rewritten from `printshopSpacingChunks` config fields (datapack values apply). | Acceptable for MVP |
| UI-01 | Minor | Printer's Archive uses system chat listing, not a full GUI book. | Deferred |
| QA-MP | Minor | Two-human concurrent investigation and disconnect-mid-press scenarios remain manual QA; deterministic allocation and all mutations are server-thread authoritative. | Manual follow-up |
| QA-SAVE | Minor | Automated tests cover codecs, NBT identity contracts, carried block state, and repeated dedicated-server startup, but do not yet automate a full starter claim across a process restart. | Manual follow-up |

## Resolved

| ID | Description | Fixed in |
|----|-------------|----------|
| ECHO-CME | `ConcurrentModificationException` while iterating active echoes during finish. | e90603d |
| PRESS-HOPPER | Hoppers/droppers could touch press slots and skip the physical sequence. | Phase 8 (WorldlyContainer no faces) |
| CONFIG-NA | Invalid / extreme config values could stall loops (zero duration). | Phase 8 sanitize |
| REB-FLOOR | Every generated floorboard was an equal investigation target. | 1.1.0 Stage 2 |
| REB-VARIANTS | Generated printshops shared one palette and layout. | 1.1.0 Stage 3 |
| REB-ARCHIVE-OUTPUT | Collected prints were consumed into inventory before their Archive result was identified. | 1.1.0 Stage 4 |
| REB-STARTER-STRAND | Leaving the first claimed workshop switched all later ruins away from mandatory components before a press was operated. | 1.1.1 |
| REB-LEGACY-ROLES | Pre-1.1 generated investigation furniture had no semantic role and therefore used weighted fallback loot. | 1.1.1 |
| REB-ASCETIC | Rebalanced variants lost much of the former cobweb, paper, and poster atmosphere. | 1.1.1 |
| UI-ITEM-PURPOSE | Several block items lacked tooltips and item descriptions did not clearly state whether they were required. | 1.1.1 |
| REB-CHEST-PARTS | Printshop chests contained only weighted specialist loot, so players could visit several ruins without seeing physical starter press parts or tools in a chest. | 1.1.2 |
| REB-TOOLS | The Workshop Broom did not exist and the brush, lens, and Archive were not physically supplied by printshop chests. | 1.1.2 |
| REB-PLAQUE-UX | The Faded Workshop Plaque looked like a wooden cube and did not explain its two-step brush interaction. | 1.1.2 |

## Tracking guidelines

- Critical: crashes, dupe exploits, server freezes — block release.
- Major: broken progression, desync, missing assets that break loop.
- Minor: visual polish, tooltip copy, balance.
- Document workarounds for players when possible.
