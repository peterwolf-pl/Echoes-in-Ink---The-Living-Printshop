# Echoes in Ink: The Living Printshop

Fabric mod for **Minecraft Java Edition 26.2** - ModJam 2026 theme **Echoes of the Past**.

Discover abandoned historical print workshops, restore lost printing technology, reconstruct damaged texts, and experience temporary visual and audio echoes of the people who once worked there.

**Author:** Peter Wolf  
**Mod ID:** `echoes_in_ink`  
**Version:** 1.0.5

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

## Planned gameplay rebalance

The next progression update will make the printing press available from the first fully investigated printshop.

The starter printshop is planned to guarantee:

- Press Screw
- Press Handle
- Press Platen
- Press Carriage
- one basic Wooden Printing Matrix
- one Damaged Archive Page
- paper and enough basic ink for the first printing attempts
- instructions and a clue leading to another location

The required components will be distributed across meaningful investigation points instead of being placed together in one chest.

Later printshops will focus on expanding the printing system rather than repeating the initial press assembly. Their primary rewards are planned to include:

- matrix fragments and specialist matrices
- composed metal type formes
- additional ink and paper
- damaged documents and archive clues
- decorative prints and posters
- workshop-specific stories and echo events

The rebalance will also introduce visually distinct workshop variants and reduce repetitive floorboard investigation.

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

The planned rebalance separates mandatory progression rewards from optional weighted loot so that random duplicates cannot block access to the press.

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

## Attribution

- Built with Fabric and Fabric API.
- Minecraft is property of Mojang Studios and Microsoft.
- Historical content in this mod is fictional and does not reproduce copyrighted books.
