# Future Roadmap

The ModJam MVP is complete. Future work should deepen the central printing loop without turning press assembly into repetitive exploration grind.

## Current gameplay milestone: progression rebalance

Version 1.1.1 implements verified starter recovery, legacy workshop migration,
and a denser printshop atmosphere on top of the 1.1.0 progression stages.

### Starter printshop

Implemented in Stage 1: guarantee the following rewards in the first progression workshop:

- Press Screw
- Press Handle
- Press Platen
- Press Carriage
- one basic Wooden Printing Matrix
- one Damaged Archive Page
- enough paper and basic ink for the first printing attempts
- instructions and a clue to another location

The required items are distributed across stable investigation roles and do not
depend on independent weighted random rolls.

### Later printshops

Shift later-structure rewards toward content for the completed press:

- matrix fragments
- specialist wooden matrices
- composed metal type formes
- Ink Balls and Ink Pads
- larger ink supplies
- specialist paper
- damaged documents
- decorative woodcuts and posters
- maps, guild notes, and archive clues
- workshop-specific historical Echo Events

Spare press parts may appear as rare replacement loot, but they should not remain the main progression reward.

### Investigation improvements

- [x] Separate decorative ink-stained floorboards from suspicious investigatable floorboards.
- [x] Limit each workshop to three-to-five meaningful floor investigation targets.
- [x] Use the Magnifying Lens to help identify suspicious locations.
- [x] Remove Restored Chronicle Page from generic random loot.
- [x] Preserve the required sequence of damaged page, press reconstruction, and printing.

## Printshop variants

Implemented in version 1.1.0 Stage 3 with two stable layout subvariants per type.

Add visually and functionally distinct workshop types.

### Rural Woodcut Workshop — implemented

- timber construction
- carving benches and wooden matrix work areas
- woodcut and matrix rewards

### Town Type Foundry — implemented

- brick and stone construction
- rows of type cabinets
- metal type and Ink Pad rewards

### Scholarly Archive Printer — implemented

- archive shelves and correction desks
- damaged documents, chronicle fragments, and map clues

### Burned Clandestine Printshop — implemented

- charred structure and hidden storage
- secret notices, rubbing materials, and concealed matrices

Each type should vary room layout, entrance position, damage pattern, hidden areas, storytelling props, and specialist loot. Palette swaps alone are not sufficient.

## Matrix progression — implemented

The reusable matrix-fragment system currently includes:

- Village Chronicle Matrix assembled from upper and lower fragments plus a missing letter insert
- Forbidden Notice Metal Forme assembled from a lead type set, iron chase, headline type, and printer's notes

Each completed form unlocks a dedicated press recipe, readable output, archive
entry, and variant-specific story lead. More forms remain suitable post-1.1.0
content rather than required progression.

## Ink progression — implemented baseline

- Keep early basic ink generous enough for experimentation.
- [x] Make basic ink renewable with the charcoal + clay recipe.
- Use rare historical and colored inks as optional exploration rewards.
- Do not let story progression fail because the player consumed all starter ink.

## Additional post-MVP features

- Additional echo sequences per workshop
- More matrices and decorative woodcuts
- Multiple ink colors
- User-created custom books
- Server newspapers
- Industrial printing press
- Large printing factories
- Villager printer profession
- Complex type arrangement editor
- Full text typesetting simulator
- Additional historical eras
- Accessibility refinements
- Localization beyond English and Polish
- Performance profiling for multiplayer
- Other-mod integrations
- High-resolution resource pack

## Out of scope for the near-term progression update

- new dimension
- boss fights
- combat-focused enemies
- large quest framework
- industrial-scale automation

## Implementation specification

The detailed Codex implementation prompt is stored in:

`docs/CODEX_GAMEPLAY_REBALANCE_PROMPT.md`
