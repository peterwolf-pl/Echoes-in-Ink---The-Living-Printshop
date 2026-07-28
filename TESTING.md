# Testing Checklist

## Environment

- macOS ARM, Windows, Linux
- Java 25+
- Minecraft 26.2 + Fabric Loader + Fabric API
- Single-player integrated server and dedicated Fabric server

## Build tests

- [ ] `./gradlew clean build`
- [ ] `./gradlew runDatagen` (when providers exist)
- [ ] `./gradlew runClient` — mod appears in mod list
- [ ] `./gradlew runServer` — no client-class load errors

## Phase 0

- [ ] Config file created at `config/echoes_in_ink.json`
- [ ] `/echoesinink debug` works (op level 2+)
- [ ] `/echoesinink give_test_items` responds (pending Phase 1 content)
- [ ] EN and PL language keys resolve

## Gameplay (later phases)

- [ ] Natural structure generation + `/locate`
- [ ] Investigation, brush durability, once-only loot
- [ ] Press assembly and full print sequence
- [ ] Recipe processing, no item duplication
- [ ] Archive unlocks, advancements once
- [ ] Echo event cleanup, join mid-event
- [ ] Chronicle reconstruction: use page four times (biome → bearing → map → coords)
- [ ] Follow-up location: ink archive cache hatch + chest loot
- [ ] `/echoesinink locate_cache` and vanilla `/locate structure echoes_in_ink:ink_archive_cache`

## Persistence

- [ ] World save/reload
- [ ] Server restart
- [ ] Chunk unload
- [ ] Player logout / death
- [ ] Press broken while idle / while processing

## Multiplayer

- [ ] Two players investigating same block (no double loot)
- [ ] Two players at one press
- [ ] Join during echo
- [ ] Disconnect mid-press cycle
- [ ] Dedicated server without client classes

## Exploit checks

- [ ] Break/replace investigated blocks (no second loot)
- [ ] Hopper / dropper against press: no insert or extract
- [ ] Break press mid-cycle: drops parts + inputs once
- [ ] Chunk-unload duplication
- [ ] No arbitrary code execution from item NBT / components
- [ ] Invalid config values clamped on load
