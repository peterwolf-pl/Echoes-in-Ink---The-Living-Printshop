# Known Issues

## Active

| ID | Severity | Description | Status |
|----|----------|-------------|--------|
| TEX-01 | Minor | Item/block textures are temporary placeholders; gameplay is complete. | Deferred post-ModJam |
| CFG-01 | Minor | Structure spacing JSON is not rewritten from `printshopSpacingChunks` config fields (datapack values apply). | Acceptable for MVP |
| UI-01 | Minor | Printer's Archive uses system chat listing, not a full GUI book. | Deferred |

## Resolved

| ID | Description | Fixed in |
|----|-------------|----------|
| ECHO-CME | `ConcurrentModificationException` while iterating active echoes during finish. | e90603d |
| PRESS-HOPPER | Hoppers/droppers could touch press slots and skip the physical sequence. | Phase 8 (WorldlyContainer no faces) |
| CONFIG-NA | Invalid / extreme config values could stall loops (zero duration). | Phase 8 sanitize |

## Tracking guidelines

- Critical: crashes, dupe exploits, server freezes — block release.
- Major: broken progression, desync, missing assets that break loop.
- Minor: visual polish, tooltip copy, balance.
- Document workarounds for players when possible.
