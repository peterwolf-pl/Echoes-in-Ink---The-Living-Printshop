# Echoes in Ink: The Living Printshop

Fabric mod for **Minecraft Java Edition 26.2** — ModJam 2026 theme **Echoes of the Past**.

Discover abandoned historical print workshops, restore lost printing technology, reconstruct damaged texts, and experience temporary visual and audio echoes of the people who once worked there.

**Author:** Peter Wolf  
**Mod ID:** `echoes_in_ink`  
**Version:** 1.0.3

**Licence:** MPL-2.0  
**Repository:** https://github.com/peterwolf-pl/Echoes-in-Ink---The-Living-Printshop

## Requirements

- Minecraft **26.2**
- **Fabric Loader** ≥ 0.19.3
- **Fabric API** (version matching 26.2)
- **Java 25+**

## Installation (players)

1. Install Fabric Loader for Minecraft 26.2.
2. Install Fabric API for 26.2.
3. Drop the mod JAR into the `mods` folder.
4. Launch the game (or dedicated Fabric server).

## Gameplay loop (MVP target)

1. Find an abandoned print workshop.
2. Investigate debris and archives.
3. Recover tools, type, matrices, and press parts.
4. Restore the historical screw printing press.
5. Reconstruct and print a damaged chronicle.
6. Witness an echo of the past.
7. Follow the restored document to a new location.

## Development

```bash
# Requires Java 25
export JAVA_HOME=$(/usr/libexec/java_home -v 25)   # macOS example

./gradlew build          # produce JAR in build/libs
./gradlew runClient      # development client
./gradlew runServer      # dedicated server
./gradlew runDatagen     # data generation (when providers exist)
```

### Dev commands (permission level 2+)

| Command | Purpose |
|---------|---------|
| `/echoesinink give_test_items` | Give Phase 1+ test items |
| `/echoesinink locate_printshop` | Help locate generated printshop |
| `/echoesinink locate_cache` | Help locate ink archive cache |
| `/echoesinink trigger_echo` | Force-trigger echo event |
| `/echoesinink reset_archive` | Reset player archive progression |
| `/echoesinink debug [on\|off\|reload_config]` | Debug / config tools |

### Configuration

`config/echoes_in_ink.json` — brush timing, structure spacing, echo duration, accessibility flags, debug logging.

### Package rename

Identifiers are centralized in:

- `gradle.properties` (`mod_id`, `maven_group`, `archives_base_name`)
- `EchoesInInk.MOD_ID`
- `fabric.mod.json`
- asset namespace `assets/echoes_in_ink/`

## Project docs

| File | Purpose |
|------|---------|
| [MODJAM_CHECKLIST.md](MODJAM_CHECKLIST.md) | ModJam 2026 submit checklist |
| [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) | Phased plan |
| [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) | Progress tracker |
| [TESTING.md](TESTING.md) | Test checklist |
| [KNOWN_ISSUES.md](KNOWN_ISSUES.md) | Bugs and workarounds |
| [FUTURE_ROADMAP.md](FUTURE_ROADMAP.md) | Post-MVP features |
| [CHANGELOG.md](CHANGELOG.md) | Release notes |

## Production JAR

```bash
./gradlew clean build
# → build/libs/echoes-in-ink-1.0.3.jar
```

## Multiplayer

Server-authoritative design. Dedicated servers must not load client-only classes. Client handles rendering and animation interpolation only.

## Attribution

- Built with [Fabric](https://fabricmc.net/) and Fabric API.
- Minecraft is property of Mojang Studios / Microsoft.
- Historical content in this mod is **fictional** and does not reproduce copyrighted books.
