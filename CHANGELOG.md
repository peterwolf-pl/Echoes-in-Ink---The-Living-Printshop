# Changelog

All notable changes to **Echoes in Ink** are documented here.

## [1.0.2] — 2026-07-29

### Changed

- Raised the sliding press table to approximately one block above the floor for a more believable working height.
- Added four substantial timber legs and four lower stretchers beneath the press bed.
- Raised every animated press component, loaded material, finished sheet, and collision section with the new standing frame.

## [1.0.1] — 2026-07-29

### Changed

- Rebuilt the printing press as a compact, historically grounded wooden screw press with massive cheeks, a timber crosshead, bed rails, iron straps, and an open-frame collision shape.
- Replaced the four flat animated part sprites with full 3D models: threaded wooden spindle, socketed pull bar, heavy platen, and sliding carriage/type chase.
- The pull bar now turns around the vertical spindle, the screw and platen descend together, and the carriage travels farther out for loading and collection.
- Loaded matrix, ink, paper, and finished output are now presented separately on the press bed.

### Fixed

- Finished prints no longer resemble the blank input sheet: the blank page is visibly clean while every output has a stronger, high-contrast printed texture.
- A completed impression now renders three localized lines of actual content directly on the sheet for all five printing recipes.
- Removed the duplicate unconditional screw layer that made an incomplete press look assembled.
- Removed the red damage overlay that incorrectly tinted the press's animated wooden and metal parts.

## [1.0.0] — 2026-07-28

### Added

#### Art pass
- Full custom 16×16 pixel-art set for all items and workshop blocks.
- Investigation stage variants (untouched / partial / done) with distinct dirt/ink/clean looks.
- Press wood / metal / stone textures used by the press block model.
- Regenerator script: `tools/generate_textures.py`.

#### Phase 8 — Polish, security, release
- Custom sound events with EN/PL accessibility subtitles (vanilla audio aliases).
- Printing press hopper/dropper lock (`WorldlyContainer` with no accessible faces).
- Config sanitization (clamped durations, volume, spacing).
- ModJam checklist and release notes.
- Version **1.0.0** production JAR target.

### Notes

- Temporary textures remain intentional for ModJam; art pass is post-event.
- Full gameplay loop Phases 0–7 included below.

## [0.1.0] — 2026-07-28 (development)

### Added

#### Phase 0 — Foundation
- Initial Fabric project for Minecraft 26.2 (Java 25).
- Mod metadata (`echoes_in_ink`), common and client entrypoints.
- Configuration file `config/echoes_in_ink.json`.
- Data generation entrypoint scaffold.
- English and Polish base translations.
- Development commands under `/echoesinink` (permission level 2):
  - `give_test_items`, `locate_printshop`, `locate_cache`, `trigger_echo`, `reset_archive`, `debug`
- Documentation: README, implementation plan/status, testing, known issues, roadmap.
- MPL-2.0 licence (repository default).

#### Phase 1 — Items and materials
- Investigation tools: Printer's Brush, Magnifying Lens, Charcoal Rubbing Paper.
- Archive and press materials: blank/damaged archive pages, ink ball/pad, wooden matrix, metal type, press screw/handle/platen/carriage, restored chronicle page.
- Charcoal rubbing item with `rubbing_pattern` data component.
- Workshop blocks: printing debris (3 investigation states), carved wooden matrix, dusty table, archive shelf, broken press frame, type cabinet, ink-stained floorboards, faded plaque.
- Creative tab **Echoes in Ink**.
- Temporary vanilla-style item/block models.

#### Phase 2 — Investigation
- `InvestigationBlockEntity` with persistent once-only loot flag.
- Weighted server-side loot profiles for workshop blocks.
- Results: nothing, type, matrix, press parts, pages, clues, hidden compartments.
- Three visual investigation states on all investigatable blocks.
- Anti-dupe: `investigation` data component on dropped blocks restores state on place.
- Distinct models per investigation stage; item tooltips; `/echoesinink debug inspect`.

#### Phase 3 — Abandoned Printshop
- World-generated surface structure with main workshop + storage cellar.
- Broken press remains, investigation blocks, debris, storytelling props.
- Hidden chest with damaged chronicle page and press components.
- Structure set placement (spacing 32 / separation 12), biome tag, beard_thin adaptation.
- `/echoesinink locate_printshop` and vanilla `/locate structure echoes_in_ink:abandoned_printshop`.

#### Phase 4 — Historical Screw Printing Press
- Assemblable press block with matrix / ink / paper / output slots.
- Physical use sequence: install parts → load → carriage → handle → wait → collect.
- Five printable outputs and server-side recipe matching.
- Client block-entity animation driven by server phase.
- Safe drops on break; multiplayer-safe state machine.

#### Phase 5 — Printer's Archive
- Per-player persistent archive (Fabric data attachment).
- Categories for workshops, parts, matrices, works, echoes, clues.
- Unlocks from investigation and printing only.
- Printer's Archive item lists unlocked knowledge.
- Advancements: Dust and Ink, Letters from the Rubble, The Forgotten Machine, Pull the Handle, A Page Restored, Echoes in Ink.
- `/echoesinink reset_archive` for testing.

#### Phase 6 — Echoes of the Past
- Server-driven **The Last Print Run** (~30s, configurable).
- No permanent entities/blocks; client particles and subtitles only.
- Mid-join sync; skip after first viewing (sneak); accessibility config flags.
- Completing the echo unlocks archive entries and the Echoes in Ink advancement.
- `/echoesinink trigger_echo` for development.

#### Phase 7 — Restored Chronicle and location discovery
- Restored Chronicle Page progressive reading (server-only): biome → bearing → map → precise site.
- Prerequisites gate stages (investigation / echo / map accessibility).
- Explorer map with red X pointing to the nearest ink archive cache.
- New structure **Ink Archive Cache**: half-buried chamber, ladder hatch, moss marker, loot chest.
- Structure set, biome tag, loot table `chests/ink_archive_cache`.
- Archive entries for chronicle clues and site discovery.
- Advancement **The Buried Cache**.
- `/echoesinink locate_cache` and vanilla `/locate structure echoes_in_ink:ink_archive_cache`.

### Notes

- Superseded by 1.0.0 Phase 8 release packaging.
