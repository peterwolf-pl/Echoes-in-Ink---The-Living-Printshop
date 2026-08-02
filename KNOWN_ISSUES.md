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

## Tracking guidelines

- Critical: crashes, dupe exploits, server freezes — block release.
- Major: broken progression, desync, missing assets that break loop.
- Minor: visual polish, tooltip copy, balance.
- Document workarounds for players when possible.
