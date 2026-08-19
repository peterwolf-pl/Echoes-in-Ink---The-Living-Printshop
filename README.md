# Echoes in Ink: The Living Printshop

Fabric mod for **Minecraft Java Edition 26.2** - ModJam 2026 theme **Echoes of the Past**.

Discover abandoned historical print workshops, restore lost printing technology, reconstruct damaged texts, and experience temporary visual and audio echoes of the people who once worked there.

**Author:** Peter Wolf  
**Mod ID:** `echoes_in_ink`  
**Version:** 1.1.2

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

Semantic investigation roles and world saved data guarantee the complete
starter inventory across chunk unloads and server restarts. Starter mode stays
active in every visited printshop until a complete press actually pulls its
handle, so leaving an earlier ruin cannot permanently remove required parts.
After that real press run, newly investigated printshops switch to their stable
specialist profiles.

Before that first press operation, opening a chest inside a confirmed printshop
adds one deterministic physical kit for that workshop: Workshop Broom,
Printer's Brush, Magnifying Lens, Printer's Archive, Press Screw, Press Handle,
Press Platen, and Press Carriage. This does not replace the variant's specialist
loot. Reopen an already-looted printshop chest in an existing world to receive
the kit; no new world is required. If the chest is full, overflow goes to the
opening player instead of disappearing.

Printshops generated before 1.1 are upgraded when an unbound investigation
node is next cleaned. Their old furniture is assigned deterministic semantic
roles; rewards belonging to already searched mandatory roles are compensated
through that migration interaction. If every old node was already fully
searched, use the Printer's Brush on any investigated printshop furniture once
more to trigger the same one-time recovery bundle.

The starter printshop guarantees:

- Workshop Broom, Printer's Brush, Magnifying Lens, and Printer's Archive
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
differ. Every variant also contains several cobwebs, laid papers/prints on work
surfaces, and wall posters. `enablePrintshopVariants=false` preserves the rural
layout selection for compatibility.

Abandoned printshops generate in two ways. Beside vanilla villages they share
the village chunk grid and sit on the outskirts rather than on the well; every
village gets one workshop, and larger plains, meadow, savanna, and taiga
villages also get a second shop on the opposite side. A second, independent
grid still scatters extra ruins through forests, swamps, and other workshop
biomes. `/echoesinink locate_printshop` finds the nearest of either kind.

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

Printshops contain investigatable machine remains, archive shelves, type cabinets, printing tables, plaques, debris, and hidden storage. Cleaning Printing Debris with the Printer's Brush takes the pile apart: scraps come free on the first stroke, then the remainder becomes paper, ink, type, and spare press parts and the block is removed.

Every generated printshop contains three almost-full chests. Beside the variant's specialist loot, the shared supply kit always includes clean paper, blank archive pages, ink balls and pads, metal type, and spare press components.

Workshop walls now carry several different hanging posters (warning, woodcut, chronicle, forbidden notice, map, and type specimen). Extra dusty and crafting tables sit beside the investigation bench, each with laid paper, notes, or a finished print.

The Faded Workshop Plaque is not filler decoration. Inspect it with the
Magnifying Lens, then hold use with the Printer's Brush twice to clean both
stages. During starter mode it contains the Printer's Instruction Sheet and
Workshop Map Fragment; later it provides a stable historical clue. The optional
Workshop Broom removes decorative cobwebs and sweeps stained decorative
floorboards through their visual cleanup stages; it does not replace the
Printer's Brush during investigations or create floor loot.

The Magnifying Lens can also inspect laid pages, finished prints, matrices,
type formes, press parts, and hanging posters. It identifies the exact displayed
item, and readable finished prints show their complete text in chat.

Every custom item and block item has two tooltip lines: a description and an
explicit gameplay purpose identifying required components, crafting parts,
investigation locations, optional tools, finished prints, or decoration.

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

Village printshops use the vanilla village salt with spacing 34 and
separation 8. Wilderness printshops use a separate set (salt 20260728,
spacing 32, separation 12). The numeric spacing fields in the config document
the village values but do not rewrite the world-generation JSON.

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

Starter mode and physical chest-kit claims are world-shared and remain active
until the first real handle pull. This lets a multiplayer group recover from an
abandoned or legacy ruin; each investigation node and each workshop chest kit
still pays only once and cannot be rerolled.
After that handle pull, the Press Screw, Handle, Platen, and Carriage replacement
recipes are revealed in the player's recipe book. Later ruins may additionally
contain rare spare parts when
`allowSparePressPartsInLaterRuins` is enabled.

## Attribution

- Built with Fabric and Fabric API.
- Minecraft is property of Mojang Studios and Microsoft.
- Historical content in this mod is fictional and does not reproduce copyrighted books.
