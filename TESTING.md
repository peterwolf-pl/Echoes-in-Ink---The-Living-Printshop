# Testing Checklist

## Environment

- macOS ARM, Windows, Linux
- Java 25+
- Minecraft 26.2 + Fabric Loader + Fabric API
- Single-player integrated server and dedicated Fabric server

## Build tests

- [x] `./gradlew build` — Stage 1 compile/package/test gate (2026-08-02)
- [x] `./gradlew test classes` — Stage 1 deterministic allocation/audit tests (2026-08-02)
- [x] `./gradlew runDatagen` — strict run completed; zero providers/zero generated files (Stage 1)
- [x] `./gradlew runClient` — 1.1.0 common/client initialization and resource atlases completed (Stage 1)
- [x] `./gradlew runServer` — reached `Done`, no client-class loading, clean `stop` (Stage 1)
- [x] `./gradlew runClientGameTest` — press renderer/state machine, four generated variants, starter-only loop (Stages 3–4)

## 1.1.1 recovery and atmosphere verification — 2026-08-04

- [x] `./gradlew test` — 18 tests in ten JVM test classes, zero failures/errors.
- [x] Abandoned-first-workshop regression: first, second, and third workshops
  remain starter-eligible until an actual handle pull; later rewards activate
  immediately after that press operation.
- [x] Legacy rural role planner assigns every mandatory role exactly once.
- [x] Runtime ClientGameTest migrates a completely searched, unbound
  pre-1.1-style furniture cluster and compensates all eight mandatory roles on
  revisit; it then separately obtains all four components through real block
  cleaning and verifies the delivered player inventory.
- [x] Item description coverage requires name, description, and gameplay
  purpose in EN/PL for 31 items and 11 block items.
- [x] Variant ClientGameTest requires at least four cobwebs, two wall posters,
  two laid papers/prints, and non-empty laid-paper block entities in every type.
- [x] `./gradlew runDatagen` — strict success, zero providers/files, version 1.1.1.
- [x] `./gradlew runClient` — 1.1.1 common/client initialization and all atlases loaded; stopped intentionally.
- [x] `./gradlew runServer` — loaded 1592 recipes/1699 advancements, reached `Done`, located the printshop at `[-208, 224]`, force-loaded nine surrounding chunks, and stopped cleanly.
- [x] Final `./gradlew build` — success; 18 tests in ten JVM test classes,
  zero failures/errors.
- [x] `unzip -t build/libs/echoes-in-ink-1.1.1.jar` — no compressed-data
  errors; all project JSON parsed; `git diff --check` passed; generic loot
  contains no Restored Chronicle Page.
- [x] Refreshed exterior and interior screenshots captured for all four
  printshop variants (eight 1920×1080 images total).

## Final 1.1.0 verification — 2026-08-02

- [x] `./gradlew build` — success; 15 tests in seven JVM test classes, zero failures/errors.
- [x] `./gradlew runDatagen` — strict validation success; hand-authored data loaded, zero providers and zero generated files.
- [x] `./gradlew runClientGameTest` — three entrypoints passed; four variant screenshots plus press-state screenshots created.
- [x] `./gradlew runClient` — common/client entrypoints initialized and all block/item/resource atlases loaded; stopped intentionally after the smoke check.
- [x] `./gradlew runServer` — 1592 recipes and 1699 advancements loaded, reached `Done`, `/locate` returned `[-208, 224]`, clean stop, no client-only class loading.
- [x] `unzip -t build/libs/echoes-in-ink-1.1.0.jar` — no compressed-data errors.
- [x] Every project JSON parsed; `git diff --check` passed; generic loot grep found no Restored Chronicle Page.

## Phase 0

- [x] Config file created/loaded at `config/echoes_in_ink.json`
- [ ] `/echoesinink debug` works (op level 2+)
- [ ] `/echoesinink give_test_items` responds (pending Phase 1 content)
- [x] EN and PL language JSON parses and new resource models load without missing-model errors

## Gameplay (later phases)

- [x] Natural structure locate and forced surrounding-chunk generation on dedicated server (Stage 3)
- [x] Investigation once-only loot and carried break/place state (automated contract + integrated replacement test)
- [x] Press assembly and full print sequence (integrated client/server GameTest)
- [x] Recipe processing and one-time ink/paper consumption (integrated client/server GameTest)
- [x] Archive unlock for collected chronicle and advancement completion (integrated client/server GameTest)
- [ ] Echo event cleanup, join mid-event
- [ ] Chronicle reconstruction: use page four times (biome → bearing → map → coords)
- [ ] Follow-up location: ink archive cache hatch + chest loot
- [ ] `/echoesinink locate_cache` and vanilla `/locate structure echoes_in_ink:ink_archive_cache`

## Progression rebalance 1.1.0

- [x] Every declared starter variant/layout allocates all four unique press parts exactly once.
- [x] Starter allocation contains matrix, damaged page, five blank pages, five ink uses, instructions, and clue.
- [x] Later allocation is stable and specialist rewards outnumber optional spares.
- [x] Generic loot audit rejects Restored Chronicle Page references.
- [x] Focused suspicious-floor allocation/configuration tests (Stage 2; 7 JVM tests total).
- [x] Stage 2 full gate: `build`, `runDatagen`, `runClient` resource reload, and `runServer` to `Done`/clean stop.
- [x] Structure identity NBT migration contract and four-variant integrated generation/screenshots (Stage 3).
- [x] Stable variant/layout selection, identity round-trip/defaults, and required-node builder contract (Stage 3; 11 JVM tests total).
- [x] Matrix definition/JSON contract, Archive tracking/copy, and investigation payload tests (Stage 4).
- [x] Full starter press-operation ClientGameTest: four parts, matrix, ink, damaged page, Restored Chronicle output, Archive unlock (Stage 4).
- [x] Specialist Village/Forbidden press recipes resolve to their new outputs (Stage 4 ClientGameTest).
- [x] Seven data-pack recipes and three recipe advancements loaded: runtime total increased from 1585/1696 to 1592/1699.

## Persistence

- [x] World save/reload (development worlds and saved-data codec smoke-tested)
- [x] Server restart (dedicated startup/clean stop repeated for 1.1.1)
- [ ] Chunk unload
- [ ] Player logout / death
- [ ] Press broken while idle / while processing

## Multiplayer

- [ ] Two players investigating same block (no double loot)
- [ ] Two players at one press
- [ ] Join during echo
- [ ] Disconnect mid-press cycle
- [x] Dedicated server without client classes

## Exploit checks

- [x] Break/replace investigated blocks (carried full state remains ineligible for a second loot allocation)
- [ ] Hopper / dropper against press: no insert or extract
- [ ] Break press mid-cycle: drops parts + inputs once
- [ ] Chunk-unload duplication
- [ ] No arbitrary code execution from item NBT / components
- [ ] Invalid config values clamped on load
