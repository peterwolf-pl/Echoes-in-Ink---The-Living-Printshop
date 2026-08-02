# Changelog

All notable changes to **Echoes in Ink** are documented here.

## [1.1.0] — 2026-08-02

### Progression rebalance — Stage 1

- Added a world-persistent starter printshop claim shared by multiplayer groups.
- Bound the screw, platen, carriage, and handle to four distinct investigation locations.
- Guaranteed a basic matrix, damaged page, at least five blank pages, configurable ink for five impressions, a readable instruction sheet, and a follow-up clue.
- Added stable later-workshop specialist reward allocation keyed by workshop ID, variant, and investigation role.
- Removed Restored Chronicle Pages and mandatory press parts from generic chest loot.
- Reduced generic press-part rewards to rare optional spares controlled by configuration.
- Added migration-safe workshop identity/role fields to investigation block entities and carried block data.
- Connected the existing Fabric datagen entrypoint to a strict `runDatagen` Loom run configuration.
- Added JUnit coverage for complete unique starter sets, starter operating supplies,
  later reward priorities, deterministic allocation, floor-count contracts, and generic loot skips.

### Progression rebalance — Stage 2

- Converted ordinary ink-stained floorboards to non-investigatable decoration while retaining their legacy state property for existing-world loading.
- Added uncommon Loose Ink-Stained Floorboards and one high-value Hidden Floor Compartment role.
- Connected `suspiciousFloorboardsPerWorkshop` to generation with a sanitized range of three through five total targets.
- Added distinct localized Magnifying Lens messages, enchant particles, and a subtle chime for suspicious floors.
- Added block models, item definitions, loot tables, EN/PL names, and automated configured-count coverage.

### Progression rebalance — Stage 3

- Added stable Rural Woodcut, Town Type Foundry, Scholarly Archive, and Burned Clandestine structure selection.
- Added two deterministic layout subvariants per major type and persisted variant/layout fields in structure NBT with rural migration defaults.
- Differentiated footprints, entrances, room divisions, roof-collapse shapes, cellar/crawlspace arrangements, machine remains, storytelling props, and construction palettes.
- Added four specialist optional chest tables; required starter items remain on deterministic semantic nodes.
- Connected `enablePrintshopVariants` to generation, with a rural compatibility fallback.
- Added tests for stable selection/distribution, stored-identity round trips, migration defaults, and exact required-role placement in every procedural builder.

### Progression rebalance — Stage 4

- Added data-pack assembly recipes for the Village Chronicle Matrix and Forbidden Notice Metal Forme.
- Added two reusable-form press recipes with readable Village Chronicle and Forbidden Notice outputs, Archive entries, and story leads.
- Added a renewable four-Ink-Ball recipe from charcoal and clay while preserving one-item-per-impression consumption.
- Added Press Screw, Handle, Platen, and Carriage replacement recipes revealed after the first handle pull for multiplayer recovery.
- Expanded the migration-safe, owner-only Printer's Archive attachment with a press checklist, workshop IDs/variants, recovered material, available recipe, printed-work, and unresolved-clue tracking.
- Fixed collected prints losing their Archive identity after inventory insertion reduced the transfer stack to zero.
- Added JVM contracts for matrix definitions/recipes, Archive copy/tracking, and carried investigation state.
- Added integrated ClientGameTest coverage for the complete starter-only assembly and Restored Chronicle print loop, specialist recipes, replacement tracking, and break/place reroll protection.

## [1.0.5] — 2026-08-02

### Changed

- Extended the press screw and added a visible upper handle mounting collar.
- Raised the handle assembly above the top timber so it remains clear throughout the full pressure stroke.
- Allowed composed metal type to print the warning poster with either an Ink Ball or an Ink Pad.

### Fixed

- Prevented an accepted Ink Pad with metal type from producing a false recipe jam.
- Kept incompatible material combinations in the recoverable idle state and added clear drawer guidance instead of trapping the player in a jam-clearing loop.

### Tests

- Updated ClientGameTest to exercise the formerly failing metal-type and Ink Pad sequence.
- Added an under-pressure handle-clearance screenshot and a minimum one-block screw-height assertion.

## [1.0.4] — 2026-07-30

### Changed

- Rebuilt metal type as a complete 3D printing forme: a locked iron chase with twelve separate, level type areas instead of one flat cast letter.
- Renamed the metal-type item and archive entry to describe a composed, reusable printing matrix.
- Reduced composed metal formes to a stack size of eight.

### Added

- Added a server-authoritative 32-tick inking phase before the carriage can enter the press.
- Animated the ink ball or pad through four sweeps across the exposed form.
- Progressively blackened only the raised printing surfaces, preserving the clean metal chase around them.
- Persisted and synchronized the inked state, clearing it after an impression or when the form/ink is removed.
- Added EN/PL action-bar guidance and accessibility subtitles for inking.
- Added ClientGameTest coverage and screenshots for mid-inking and fully inked metal type.

## [1.0.3] — 2026-07-29

### Changed

- Extended the press pull bar from one block to two blocks so both wooden grips project beyond the press frame.
- Centered the iron handle socket directly above the spindle.

### Fixed

- Removed the intermittent stone-texture clipping behind the carriage by lowering the fixed bed, rails, and cross tie below the moving carriage.
- Replaced the exposed fixed-bed stone face with timber so it remains visually distinct from the moving metal type chase.
- Replaced the platen's exposed metal underside with solid timber so camera-angle occlusion no longer resembles a flickering transparent panel.
- Lowered the finished sheet onto the moving type chase, removing the parallax gap that exposed a partial grey panel from shallow camera angles.
- Moved the carriage's iron pull completely in front of its timber body, eliminating the coplanar metal/wood faces that flickered after output collection.
- Separated the fixed iron rails from the coplanar timber sill tops and kept the empty carriage fully beyond them, so the side strips no longer shimmer while the camera pans.

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
