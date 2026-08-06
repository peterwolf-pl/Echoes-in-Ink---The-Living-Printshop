# Implementation Status

**Last updated:** 2026-08-04

**Version:** 1.1.2

**Current phase:** Physical starter supplies and plaque clarity complete

## Summary

| Phase | Name | Status |
|------:|------|--------|
| 0 | Project foundation | **Complete** |
| 1 | Basic items and materials | **Complete** |
| 2 | Historical debris and investigation | **Complete** |
| 3 | Abandoned printshop structure | **Complete** |
| 4 | Functional printing press | **Complete** |
| 5 | Archive book and progression | **Complete** |
| 6 | Echoes of the past | **Complete** |
| 7 | Chronicle and location discovery | **Complete** |
| 8 | Polish and release | **Complete** |

## Phase 8 checklist

| Task | Status |
|------|--------|
| Sound events + accessibility subtitles | Done |
| Press hopper / automation lock | Done |
| Config value sanitization | Done |
| Version bump 1.0.0 | Done |
| ModJam checklist document | Done |
| EN/PL subtitle keys | Done |
| Docs / changelog / known issues | Done |
| Production JAR (`./gradlew build`) | This commit |

## Release artifact

The verified 1.1.2 release artifact is
`build/libs/echoes-in-ink-1.1.2.jar`. Final ZIP integrity validation is
recorded in `TESTING.md`.

## Rebalance status

| Stage | Scope | Status |
|------:|-------|--------|
| 1 | Deterministic starter set and progression-skip removal | **Complete** |
| 2 | Focused floor investigation and lens feedback | **Complete** |
| 3 | Four visual/functional printshop variants | **Complete** |
| 4 | Matrix assembly, archive expansion, renewability, final audit | **Complete** |

Stage 1 validation: `build` passed with 6 JVM tests; strict `runDatagen`
completed with zero providers; `runClient` initialized 1.1.0 and loaded resource
atlases; `runServer` reached `Done` without client-class loading and stopped
cleanly. Detailed commands are recorded in `TESTING.md`.

Stage 2 implementation: decorative floorboards retain the legacy blockstate for
world loading but no longer expose an investigation block entity; generated
workshops place a clamped three-to-five loose/hidden targets, including at most
one high-value compartment. Lens identification remains server-authoritative.
Stage 2 `build`, seven JVM tests, strict `runDatagen`, development-client
resource loading, and dedicated-server `Done`/clean stop all passed.

Stage 3 implementation: stable selection and NBT persistence cover Rural
Woodcut, Town Type Foundry, Scholarly Archive, and Burned Clandestine types,
each with two mirrored/rearranged layouts. Builders differ in footprint,
entrance, roof loss, hidden-space form, machinery, props, material palette,
deterministic rewards, and optional chest loot.

Stage 3 validation: 11 JVM tests passed; the integrated client generated and
asserted all four variants, captured four 1920×1080 screenshots, and verified
their exact semantic nodes and configured floor count. The dedicated server
located a natural printshop, generated its surrounding chunks, and stopped
cleanly.

Stage 4 implementation: two data-pack matrix assembly recipes and two new press
outputs, renewable basic ink, four post-milestone replacement-part recipes,
expanded migration-safe Archive tracking, and exact output registration after
inventory collection. The integrated starter test assembles and operates the
press using only deterministic starter rewards, produces the Restored Chronicle
Page, unlocks its Archive entry, checks specialist print recipes, and proves a
replaced investigation node remains ineligible for reroll.

### 1.1.1 recovery and atmosphere follow-up

The starter gate now closes only after a complete press actually begins an
impression. Visiting or abandoning several ruins beforehand therefore cannot
strand the world without mandatory components. Pre-1.1 investigation furniture
is recognized, assigned deterministic semantic roles, and compensated for any
mandatory role whose legacy node was already searched. Even a completely
exhausted old ruin can trigger its one-time compensation when any investigated
furnishing is brushed again.

All four generated variants now require at least four cobwebs, two populated
laid papers/prints, and two wall posters. A client-only tooltip callback adds
localized descriptions and explicit gameplay-purpose lines to all 31 items and
11 block items without loading client classes on a dedicated server. The Faded
Workshop Plaque is documented as the starter instruction/map clue location.

The 1.1.1 JVM suite contains 18 tests in ten classes. The integrated test now
uses the real investigation cleaning path, verifies player inventory delivery,
fully searched legacy recovery, the first handle pull, and specialist
switching. The variant test asserts decoration counts and captures exterior
plus interior screenshots.
Final `build` and JAR integrity passed; all project JSON parsed; `git diff
--check` passed; and the generic-loot audit found no Restored Chronicle Page.

Final 1.1.0 validation: `build` passed with 15 tests in seven test classes and no
failures/errors; strict `runDatagen` passed; all three integrated ClientGameTests
passed; normal `runClient` initialized 1.1.0 and built every atlas; `runServer`
loaded 1592 recipes/1699 advancements, reached `Done`, located the printshop at
`[-208, 224]`, and stopped cleanly with no client-class loading. Exact commands
are recorded in `TESTING.md`.

### 1.1.2 physical starter supply follow-up

Opening a chest in a confirmed printshop before the first real press operation
now inserts one deterministic workshop kit: Workshop Broom, Printer's Brush,
Magnifying Lens, Printer's Archive, and all four basic press components. The
variant's specialist loot table is unpacked first, and any capacity overflow is
delivered to the opening player. Claims are persisted once per workshop and are
shared by multiplayer players; reopening an old already-looted chest triggers
the same migration-safe path.

The Workshop Broom is a new optional cobweb-cleaning tool. The Faded Workshop
Plaque is now a thin directional wall object with a brass screw-press emblem,
three cleaning textures, correct structure orientation, and explicit localized
lens plus two-brush-use instructions. Every one of the 32 custom items and 11
block items retains EN/PL description and gameplay-purpose coverage.

Validation passed on version 1.1.2: 18 JVM tests in ten classes; strict datagen;
all integrated ClientGameTests including exact eight-item starter-chest
contents, first-handle transition, full Restored Chronicle print, all four
variant atmosphere/orientation contracts, and 12 new/refreshed printshop
screenshots; normal client atlas loading; and dedicated server startup to
`Done`, natural structure locate at `[-208, 224]`, and clean shutdown. Final
build/JAR integrity results are recorded in `TESTING.md`.

See [MODJAM_CHECKLIST.md](MODJAM_CHECKLIST.md).
