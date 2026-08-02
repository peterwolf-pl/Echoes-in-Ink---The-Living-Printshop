# Implementation Status

**Last updated:** 2026-08-02

**Version:** 1.1.0

**Current phase:** Progression rebalance complete; final verification passed

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

The verified 1.1.0 release artifact is `build/libs/echoes-in-ink-1.1.0.jar`
(about 1.0 MiB). ZIP integrity validation reported no compressed-data errors.

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

Final validation: `build` passed with 15 tests in seven test classes and no
failures/errors; strict `runDatagen` passed; all three integrated ClientGameTests
passed; normal `runClient` initialized 1.1.0 and built every atlas; `runServer`
loaded 1592 recipes/1699 advancements, reached `Done`, located the printshop at
`[-208, 224]`, and stopped cleanly with no client-class loading. Exact commands
are recorded in `TESTING.md`.

See [MODJAM_CHECKLIST.md](MODJAM_CHECKLIST.md).
