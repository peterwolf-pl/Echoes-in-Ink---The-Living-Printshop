# Echoes in Ink: The Living Printshop

Fabric mod for **Minecraft Java Edition 26.2** - ModJam 2026 theme **Echoes of the Past**.

Discover abandoned historical print workshops, restore lost printing technology, reconstruct damaged texts, and experience temporary visual and audio echoes of the people who once worked there.

**Author:** Peter Wolf  
**Mod ID:** `echoes_in_ink`  
**Version:** 1.1.0

**Licence:** MPL-2.0

## Requirements

- Minecraft **26.2**
- Fabric Loader 0.19.3 or newer
- Fabric API matching Minecraft 26.2
- Java 25 or newer

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Install Fabric API for Minecraft 26.2.
3. Place the mod JAR in the `mods` directory.
4. Launch Minecraft or the dedicated Fabric server.

## Current gameplay loop

1. Find an abandoned print workshop.
2. Investigate debris, machine remains, and archives.
3. Recover tools, type, matrices, and press parts.
4. Restore the historical screw printing press.
5. Reconstruct and print a damaged chronicle.
6. Witness an echo of the past.
7. Follow the restored document to another location.

## Progression rebalance

The first structure-bound investigation completed in a world now claims that
printshop as the shared multiplayer starter workshop. Its stable workshop ID,
semantic investigation roles, and world saved data guarantee the complete
starter inventory across chunk unloads and server restarts.

The starter printshop guarantees:

- Press Screw
- Press Handle
- Press Platen
- Press Carriage
- one basic Wooden Printing Matrix
- one Damaged Archive Page
- paper and enough basic ink for the first printing attempts
- instructions and a clue leading to another location

The screw, platen, carriage, and handle are bound respectively to the main press
frame, second machine remains, cellar cache, and marked floor cache. Mandatory
rewards do not use weighted rolls and cannot be rerolled by breaking a node.

Later printshops use stable specialist reward profiles rather than a second
mandatory press assembly.

Their primary rewards include:

- matrix fragments and specialist matrices
- composed metal type formes
- additional ink and paper
- damaged documents and archive clues
- decorative prints and posters
- workshop-specific stories and echo events

Generic archive shelves and loot chests no longer contain Restored Chronicle
Pages. That page must be printed from a Damaged Archive Page.

Ordinary `ink_stained_floorboards` are decorative. Each generated workshop now
contains only the configured three-to-five suspicious targets: uncommon loose
boards and at most one high-value hidden floor compartment. The Magnifying Lens
distinguishes them with localized feedback, particles, and a quiet chime.

Generated printshops select and persist one of four functional types, each with
two stable layout subvariants:

- Rural Woodcut Workshop — timber annex, carving props, woodblock fragments
- Town Type Foundry — brick/stone foundry, cabinet rows, metal type and Ink Pads
- Scholarly Archive Printer — catalog wing, correction desk, documents and maps
- Burned Clandestine Printshop — charred shell, blocked front, crawlspace and secret notices

Their footprints, entrance positions, roof collapse, cellar/crawlspace, machine
remains, props, deterministic investigation rewards, and optional chest tables
differ. `enablePrintshopVariants=false` preserves the rural layout selection for
compatibility.

### Matrix, ink, and archive progression

Later investigation can reconstruct two reusable specialist forms through
data-pack crafting recipes:

- Village Chronicle Matrix = Upper Matrix Fragment + Lower Matrix Fragment + Missing Letter Insert
- Forbidden Notice Metal Forme = Lead Type Set + Iron Chase + Missing Headline Type + Printer's Notes

The Village matrix prints a Village Chronicle; the forbidden metal forme prints
a prohibited notice. Both outputs are readable, unlock their own archive entry,
and provide workshop-specific story leads.

Each Ink Ball is one impression. The starter station guarantees five by default,
later ink stations provide four to seven, and four new Ink Balls can be crafted
from charcoal plus a clay ball. Required story progression therefore has a
renewable recovery path if the starter supply is used experimentally.

The Printer's Archive now shows the four-part press checklist and tracks hashed
workshop IDs, variants, recovered matrices/fragments, available recipes, printed
works, and unresolved clues. Its Fabric attachment remains server-persisted and
is selectively synchronized only to the owning player.

The full Codex implementation specification is available in `docs/CODEX_GAMEPLAY_REBALANCE_PROMPT.md`.

## Printing press

The historical screw press is assembled from four recovered components:

- screw
- handle
- platen
- carriage

The press uses physical world interaction instead of a furnace-style production interface.

Typical sequence:

1. Install the required press components.
2. Load a matrix or metal type forme.
3. Apply ink.
4. Insert paper or a damaged archive page.
5. Push the carriage into the press.
6. pull and reset the handle.
7. Pull the carriage out and collect the print.

## Exploration and investigation

Printshops contain investigatable machine remains, archive shelves, type cabinets, printing tables, plaques, debris, and hidden storage.

Investigation state and generated loot are server-authoritative and persist across chunk unloads and restarts. Breaking and replacing investigated blocks does not reroll their contents.

Mandatory progression rewards are separate from optional weighted loot, so
random duplicates cannot block access to the press.

## Development

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)

./gradlew build
./gradlew runClient
./gradlew runServer
./gradlew runDatagen
```

### Development commands

| Command | Purpose |
|---------|---------|
| `/echoesinink give_test_items` | Give Phase 1 and later test items |
| `/echoesinink locate_printshop` | Locate a generated printshop |
| `/echoesinink locate_cache` | Locate an ink archive cache |
| `/echoesinink trigger_echo` | Force-trigger an echo event |
| `/echoesinink reset_archive` | Reset player archive progression |
| `/echoesinink debug [on\|off\|reload_config]` | Debug and configuration tools |

### Configuration

The human-readable configuration file is:

`config/echoes_in_ink.json`

It contains investigation timing, printing duration, echo accessibility, generation flags, and debug settings.

Progression settings currently connected to gameplay include
`starterPrintshopGuaranteesFullPress`, `starterInkImpressions`, and
`allowSparePressPartsInLaterRuins`, plus the clamped
`suspiciousFloorboardsPerWorkshop` target and `enablePrintshopVariants`.

Structure spacing is currently controlled by the structure-set data file. The numeric spacing fields in the config document the intended values but do not replace the world-generation JSON.

## Project documentation

| File | Purpose |
|------|---------|
| `docs/CODEX_GAMEPLAY_REBALANCE_PROMPT.md` | Codex specification for the next gameplay progression update |
| `MODJAM_CHECKLIST.md` | ModJam submission checklist |
| `IMPLEMENTATION_PLAN.md` | Phased implementation plan |
| `IMPLEMENTATION_STATUS.md` | Current implementation status |
| `TESTING.md` | Test checklist |
| `KNOWN_ISSUES.md` | Known bugs and workarounds |
| `FUTURE_ROADMAP.md` | Post-MVP roadmap |
| `CHANGELOG.md` | Release history |

## Production JAR

```bash
./gradlew clean build
```

The production artifact is generated in `build/libs`.

## Multiplayer

The mod uses a server-authoritative design. Dedicated servers must not load client-only rendering classes. Clients handle rendering, animation interpolation, particles, subtitles, and visual presentation only.

The first investigated workshop is claimed once per world, so a group shares
one physical deterministic starter set and cannot repeat interactions to
duplicate it. After the first handle pull, the Press Screw, Handle, Platen, and
Carriage replacement recipes are revealed in the player's recipe book. Later
ruins may additionally contain rare spare parts when
`allowSparePressPartsInLaterRuins` is enabled.

## Attribution

- Built with Fabric and Fabric API.
- Minecraft is property of Mojang Studios and Microsoft.
- Historical content in this mod is fictional and does not reproduce copyrighted books.
