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
- [ ] Clean compile + client/server run verification

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

Small ruined workshop + storage/basement, environmental storytelling, hidden compartment, damaged chronicle, machine parts.

- Biome-appropriate, `/locate`, configurable frequency, structure processors.
- Workshop id in structure data.

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
