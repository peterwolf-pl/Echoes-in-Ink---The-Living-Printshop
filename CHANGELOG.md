# Changelog

All notable changes to **Echoes in Ink** are documented here.

## [0.1.0] — 2026-07-28

### Added

#### Phase 0 — Foundation
- Initial Fabric project for Minecraft 26.2 (Java 25).
- Mod metadata (`echoes_in_ink`), common and client entrypoints.
- Configuration file `config/echoes_in_ink.json`.
- Data generation entrypoint scaffold.
- English and Polish base translations.
- Development commands under `/echoesinink` (permission level 2):
  - `give_test_items`, `locate_printshop`, `trigger_echo`, `reset_archive`, `debug`
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

### Notes

- Structure generation, printing press, archive UI, and echoes are planned for later phases.
