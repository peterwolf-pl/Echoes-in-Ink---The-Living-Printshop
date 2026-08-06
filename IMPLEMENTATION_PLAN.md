# Implementation Plan — Echoes in Ink: The Living Printshop

**ModJam 2026 theme:** Echoes of the Past  
**Target:** Minecraft Java 26.2 · Fabric Loader · Fabric API · Java 25  
**Mod ID:** `echoes_in_ink`  
**Package:** `pl.peterwolf.echoesinink`  
**Author:** Peter Wolf  
**Repository:** https://github.com/peterwolf-pl/Echoes-in-Ink---The-Living-Printshop

## Design priorities

1. Few polished mechanics over many unfinished ones.
2. Printing press is the visual and mechanical centerpiece.
3. Physical machine interaction (not furnace-only GUIs).
4. Structures tell stories through environmental detail.
5. Vanilla-compatible visuals; fictional historical content only.
6. Fabric API only; multiplayer-safe; server-authoritative.
7. No new currency, dimension, or large combat systems.

## Central gameplay loop

1. Discover an abandoned print workshop.
2. Investigate historical remains.
3. Recover tools, type, matrices, and machine parts.
4. Restore a functional printing press.
5. Reconstruct and print a damaged historical document.
6. Trigger an echo of the past.
7. Use the document to discover another location / unlock tech.

## Package layout

```text
pl.peterwolf.echoesinink
├── EchoesInInk                 # common entry
├── block / block.entity
├── item
├── screen / screen.handler
├── recipe
├── structure / world
├── echo / archive
├── networking / config
├── advancement / datagen
├── client / client.render / client.animation
├── command / util
```

Client rendering never owns gameplay authority.

---

## Phase 0 — Project foundation

- [x] Gradle + Fabric Loom for MC 26.2
- [x] Mod metadata, client + common entrypoints
- [x] Logging, config, lang (EN + PL)
- [x] Data generation entrypoint
- [x] Dev commands scaffold
- [x] Git ignore, licence (MPL-2.0), tracking docs
- [x] Clean compile + client/server run verification

**Acceptance:** Compiles; client and dedicated server start; mod listed; no client classes on dedicated server.

## Phase 1 — Basic items and materials

Items: Printer's Brush, Magnifying Lens, Charcoal Rubbing Paper, Blank/Damaged Archive Page, Ink Ball/Pad, Wooden Printing Matrix, Metal Type Piece, Press Screw/Handle/Platen/Carriage, Restored Chronicle Page.

- Brush: timed clean, durability, sound, particles, config.
- Lens: server-side inspection + cooldown.
- Rubbing paper: data-component pattern id (no arbitrary code).

**Acceptance:** Command-obtainable; translations; brush/lens/rubbing work in multiplayer.

## Phase 2 — Historical debris and investigation

Blocks: Dusty Printing Table, Collapsed Type Cabinet, Ink-Stained Floorboards, Damaged Archive Shelf, Printing Debris, Broken Press Frame, Carved Wooden Matrix Block, Faded Workshop Plaque.

- Investigation states: untouched → partially cleaned → fully investigated.
- Weighted loot; once-only; persists across unload/restart.
- Server-authoritative; no client loot.

## Phase 3 — Abandoned Printshop structure

- [x] Custom structure type + procedural piece (workshop + cellar)
- [x] Investigation props, hidden chest, storage loot
- [x] Biome tag, structure set, terrain adaptation
- [x] Workshop id on piece NBT
- [x] `/locate` + `/echoesinink locate_printshop`
- [x] Config enable flag

## Phase 4 — Historical Screw Printing Press

Multiblock / controller with slots: matrix, ink, paper, output tray.

Sequence: insert matrix → ink → paper → carriage in → pull handle → press → return handle → carriage out → collect.

- States: incomplete, ready, missing parts, carriage, pressing, reset, output, jammed.
- Client animation follows server state; multiplayer-safe; drop contents on break.

Recipes: Restored Chronicle Page, Workshop Map Fragment, Decorative Woodcut, Printed Warning Poster, Printer's Instruction Sheet.

## Phase 5 — Printer's Archive and progression

Categories: Workshops, Machine Parts, Matrices, Printed Works, Echoes, Unresolved Clues.

- Per-player server storage; selective client sync.
- Advancements: Dust and Ink, Letters from the Rubble, The Forgotten Machine, Pull the Handle, A Page Restored, Echoes in Ink.

## Phase 6 — Historical Echo Events

Signature sequence **The Last Print Run** (~20–40s): ghost workers, temporary furniture outlines, press sounds, subtitles, final clue; no permanent entities/blocks.

Accessibility: subtitles, reduced particles/flashes/tint, volume, skip after first view.

## Phase 7 — Restored Chronicle and location discovery

Clean → inspect → recover type → print restored copy → clue progression (biome → direction → map fragment → precise location).

Follow-up: second printshop / cache / library basement / buried crate.

## Phase 8 — Polish, security, release

Sounds, tooltips, balance, exploit hardening, screenshots, trailer shots, production JAR, ModJam checklist.

## Progression rebalance — version 1.1.0

- [x] Stage 1: deterministic world-bound starter allocation, complete press set,
  starter ink/paper/matrix/documents, generic Chronicle skip removal, JVM tests.
- [x] Stage 2: decorative versus suspicious floorboards and Magnifying Lens cues.
- [x] Stage 3: four persistent structure variants with two layouts each and
  specialist later-workshop profiles.
- [x] Stage 4: reusable matrix-fragment assembly, expanded archive tracking,
  renewable ink, multiplayer replacement recipes, and final acceptance audit.

The first completed structure-bound investigation records the initial workshop,
but starter rewards remain available until a complete press actually begins an
impression. This prevents an abandoned ruin from stranding the shared world.

## Recovery and atmosphere follow-up — version 1.1.1

- [x] Keep starter rewards active until the first real press handle pull.
- [x] Migrate semantic roles into pre-1.1 generated printshops and compensate
  mandatory roles already exhausted under weighted loot, including a revisit
  path for completely searched ruins.
- [x] Restore cobwebs and add laid papers/prints plus wall posters to every variant.
- [x] Give every mod item and block item EN/PL description and purpose tooltips.
- [x] Extend JVM and ClientGameTest coverage through actual cleaning/inventory paths.

## Physical starter supplies and plaque clarity — version 1.1.2

- [x] Put all four basic press components and all four investigation tools in a
  deterministic physical printshop chest kit before the first press operation.
- [x] Persist one chest-kit claim per workshop and retain specialist chest loot.
- [x] Support already-opened chests in existing worlds by injecting the kit on
  their next valid interaction.
- [x] Add a functional Workshop Broom for optional cobweb cleanup.
- [x] Rebuild the Faded Workshop Plaque as a thin directional model with a
  readable historical press emblem and explicit lens/brush instructions.
- [x] Extend automated progression, item-description, orientation, and visual
  screenshot coverage.

---

## Workflow rules

After every phase:

1. Compile  
2. Run tests / datagen when available  
3. Launch dev client  
4. Check dedicated server when relevant  
5. Fix warnings/crashes  
6. Summarize + update `IMPLEMENTATION_STATUS.md`

## Explicitly postponed (post-ModJam)

See `FUTURE_ROADMAP.md`: industrial press, colour inks, custom books, newspapers, villager profession, type editor, extra eras, large quests, dimensions, bosses, other-mod integrations.

## MVP gate (ModJam)

One generated printshop · full investigation · working press · recipe system · five outputs · chronicle reconstruction · archive UI · six advancements · one echo · one follow-up location · multiplayer + dedicated server · EN/PL · config · sounds · basic animations · no critical dupe exploits · no known crashes.
