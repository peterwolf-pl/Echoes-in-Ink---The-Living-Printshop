# Codex Implementation Prompt: Printshop Progression Rebalance

You are working in the existing repository `peterwolf-pl/Echoes-in-Ink---The-Living-Printshop`.

Implement a gameplay progression rebalance for the Fabric mod **Echoes in Ink: The Living Printshop**.

## Core design decision

The first abandoned printshop discovered by the player must contain every component required to assemble the basic historical screw printing press.

The player must not need to search several ruins just to unlock the mod's central mechanic.

Later printshops should expand what the player can print. They should reward new matrices, matrix fragments, metal type formes, ink, paper, documents, archive entries, and historical echo content.

## Required press components

The first printshop must guarantee all four components:

- Press Screw
- Press Handle
- Press Platen
- Press Carriage

Do not place all four components in one chest.

Distribute them across meaningful investigation points inside the structure. The player should need to explore and understand the whole workshop.

Recommended distribution:

- one component recovered from the main Broken Press Frame
- one component recovered from a second damaged machine element or collapsed work area
- one component found in the cellar or hidden compartment
- one component found beneath suspicious floorboards, inside the annex, or in a clearly signposted storage location

The exact positions may vary by structure variant, but every valid starter printshop must contain one complete set.

## Starter printshop contents

The first printshop must also guarantee access to:

- one basic Wooden Printing Matrix
- one Damaged Archive Page
- enough basic ink for approximately five impressions
- enough paper or blank archive pages to test the press
- a readable Printer's Instruction Sheet, workshop note, plaque clue, or equivalent guidance
- a clue that starts the search for another printshop or archive location

The player should be able to complete the following loop using one printshop:

1. Discover the ruin.
2. Obtain or craft the Printer's Brush.
3. Investigate the workshop.
4. Recover all four press components.
5. Assemble the press.
6. Load the first matrix, ink, and page.
7. Produce the first meaningful print.
8. Unlock the next exploration objective.

## Deterministic progression loot

Separate mandatory progression loot from optional random loot.

Mandatory items must not rely on independent weighted rolls.

Create a deterministic structure-bound progression allocation system. It may use the existing `workshopId`, a stored structure variant, and a stable loot seed.

The system must guarantee:

- exactly one complete basic press set in the starter printshop
- no missing component caused by random rolls
- no duplicate press component replacing a required component
- persistence across chunk unloads and server restarts
- protection against break-and-place reroll exploits
- server-authoritative behavior

Keep normal weighted loot for optional rewards such as extra type, paper, books, ink, decorative prints, and lore items.

## Later printshops

After the player has access to the basic press, later printshops should focus on content expansion rather than another required press assembly.

Prioritize rewards such as:

- Wooden Matrix Fragments
- complete specialist wooden matrices
- Composed Metal Type Formes
- missing type inserts or locked chases
- Ink Balls and Ink Pads
- larger quantities of basic ink
- rare or historical inks
- blank archive pages and specialist paper
- damaged documents requiring reconstruction
- decorative woodcuts and posters
- workshop maps and guild notes
- new Printer's Archive entries
- new historical Echo Events

Basic replacement press parts may remain as rare optional loot, but they must not be the primary reward of later structures.

## Matrix fragment progression

Add a reusable system for matrices assembled from fragments.

Example concepts:

### Village Chronicle Matrix

Requires:

- Upper Matrix Fragment
- Lower Matrix Fragment
- Missing Letter Insert

### Forbidden Notice Metal Forme

Requires:

- Lead Type Set
- Iron Chase
- Missing Headline Type
- Printer's Notes

The implementation should support data-driven recipes or definitions where practical. Avoid hard-coding every future matrix into one large conditional method.

A completed matrix should unlock at least one new printing recipe, archive entry, or clue.

## Ink balance

Treat ink as a useful production resource, not an early-game blocker.

Target balance:

- the first printshop provides enough ink for about five impressions
- later printshops commonly provide enough materials for another four to ten impressions
- basic ink should eventually be craftable or renewable
- rare historical or colored inks may remain exploration rewards
- required story progression must never fail because all early ink was consumed experimentally

If the current Ink Ball or Ink Pad items do not track uses, introduce a clear and maintainable durability, charge, or consumption model. Preserve existing recipes where possible.

## Remove progression skips

The `RESTORED_CHRONICLE_PAGE` must not appear as random investigation loot from archive shelves or other generic loot sources.

The intended path is:

1. Find a Damaged Archive Page.
2. Recover and assemble the press.
3. Load the correct matrix, ink, and page.
4. Print the Restored Chronicle Page.

Audit every loot source and remove any other reward that bypasses this sequence.

## Reduce investigation grind

Do not make every floorboard in the structure an equally valuable investigation target.

Split decorative floorboards from suspicious floorboards.

Recommended block roles:

- `ink_stained_floorboards`: decorative, not investigatable
- `loose_ink_stained_floorboards`: investigatable, uncommon
- optional `hidden_floor_compartment`: rare, high-value investigation target

Per workshop, target:

- three to five suspicious floor locations
- no more than one high-value hidden floor compartment
- the remaining floor should be decorative

The Magnifying Lens should help identify suspicious targets through subtle feedback such as a particle, sound, outline, or localized message.

## Printshop visual variants

Add a structure variant system. Store the selected variant in structure NBT so it remains stable.

Implement at least four visually and functionally distinct variants:

### Rural Woodcut Workshop

- mostly timber construction
- carving table and wooden matrix work area
- open shed or small annex
- stronger wooden matrix and woodcut rewards

### Town Type Foundry

- brick, stone, and heavier framing
- rows of type cabinets
- metal type and Ink Pad rewards
- public notices, newspapers, and official printing theme

### Scholarly Archive Printer

- archive shelves, correction desk, catalog area
- more damaged pages and chronicle fragments
- map and research clues
- stronger archive and document rewards

### Burned Clandestine Printshop

- charred structure, blocked entrance, hidden storage
- secret notices and concealed matrices
- charcoal rubbing materials
- stronger hidden compartment and Echo Event theme

Each variant must differ in more than block palette.

Vary at least:

- footprint or room layout
- entrance position
- roof damage pattern
- cellar, annex, or hidden compartment arrangement
- position of machine remains
- storytelling props
- specialist loot profile

Use two or more layout subvariants per major type where practical.

## Starter selection and progression safety

A player must not need a specific specialist variant to obtain the first press.

Choose one of these approaches:

1. Generate a dedicated Starter Abandoned Workshop as the first progression target, then use specialist variants later.
2. Allow every variant to act as a starter while guaranteeing all required starter items.

Prefer the approach that produces the most reliable server-authoritative implementation with the least fragile world-state tracking.

Document the chosen approach in code comments and project documentation.

## Finding the next printshop

The first completed print should provide a useful lead to another location.

Use a staged clue where possible:

1. biome or workshop type
2. cardinal direction
3. approximate distance
4. map fragment or marked search area

Avoid requiring blind exploration across thousands of blocks.

Reuse the existing chronicle, map fragment, archive, and structure location systems where appropriate.

## Printer's Archive updates

Add a clear press recovery checklist:

```text
Historical Printing Press

[✓] Press Screw
[✓] Press Handle
[ ] Press Platen
[✓] Press Carriage

Parts recovered: 3/4
```

Also track:

- discovered workshop IDs
- discovered workshop variants
- recovered matrices and fragments
- available printing recipes
- printed works
- unresolved clues

Do not expose sensitive or excessive server state to the client. Continue selective synchronization.

## Multiplayer behavior

Preserve the server-authoritative design.

The generated starter workshop must physically contain one complete press set. Do not create repeatable interaction duplication.

After the first press is assembled or the relevant archive milestone is unlocked, provide a reasonable way for multiplayer groups to obtain replacement parts. Suitable options include:

- unlocked crafting recipes
- repair recipes
- rare later-ruin spare parts
- a configurable additional starter set for shared servers

Document the chosen multiplayer behavior.

## Configuration

Add sanitized configuration values where useful, for example:

- `starterPrintshopGuaranteesFullPress`
- `starterInkImpressions`
- `suspiciousFloorboardsPerWorkshop`
- `enablePrintshopVariants`
- `allowSparePressPartsInLaterRuins`

Do not add configuration fields that are not actually connected to gameplay.

If structure spacing remains data-pack controlled, clearly document that fact instead of presenting inactive config values as functional controls.

## Testing requirements

Add or update automated tests for:

- every starter structure variant contains all four unique press parts
- no starter structure contains duplicate required parts instead of the complete set
- the first structure contains a basic matrix, damaged page, paper, and sufficient ink
- later structures prioritize matrices, fragments, ink, and documents
- Restored Chronicle Page is not available from generic random loot
- suspicious floorboard count stays within the configured range
- structure variant and workshop ID persist after save and reload
- investigation cannot reroll after block break and replacement
- press can be fully assembled and used using only starter printshop rewards
- dedicated server starts without client-only class loading

Where full world-generation tests are difficult, extract deterministic allocation logic into testable pure or low-dependency classes.

## Documentation updates

Update:

- `README.md`
- `IMPLEMENTATION_PLAN.md`
- `IMPLEMENTATION_STATUS.md`
- `FUTURE_ROADMAP.md`
- `CHANGELOG.md`
- `TESTING.md`
- `KNOWN_ISSUES.md` if relevant

Clearly distinguish implemented behavior from planned behavior until the code and tests are complete.

## Delivery requirements

1. Inspect the current repository before editing.
2. Preserve the existing Fabric 26.2, Java 25+, and server-authoritative architecture.
3. Avoid unrelated refactors.
4. Keep backward compatibility with existing worlds where practical.
5. Add migration-safe defaults for newly stored data.
6. Run formatting, compilation, tests, datagen if applicable, client checks, and dedicated server checks.
7. Fix all failures caused by the change.
8. Summarize modified files, gameplay behavior, tests, and remaining limitations.

## Acceptance criteria

The task is complete when:

- one starter printshop always provides everything needed to build and operate the basic press
- the player can complete the first meaningful print without finding a second printshop
- later printshops primarily unlock new matrices, ink, documents, and stories
- required progression no longer depends on weighted random press-part rolls
- archive shelf loot cannot skip chronicle reconstruction
- floor investigation is focused rather than repetitive
- multiple printshop variants are visually and mechanically distinct
- tests prove the starter loop is reliable
- project documentation accurately describes the implemented system
