# ModJam 2026 — Release checklist

**Mod:** Echoes in Ink: The Living Printshop  
**Theme:** Echoes of the Past  
**Target:** Minecraft Java 26.2 · Fabric · Java 25+  
**Version:** 1.0.0

## MVP gate

| Requirement | Status |
|-------------|--------|
| One generated printshop | Yes — `abandoned_printshop` |
| Full investigation loop | Yes — brush / lens / rubbing / once-only loot |
| Working historical press | Yes — assemble → load → carriage → handle → collect |
| Recipe system | Yes — 5 built-in recipes |
| Five print outputs | Instruction, chronicle, woodcut, poster, map fragment |
| Chronicle reconstruction | Yes — progressive page clues |
| Archive UI | Yes — Printer's Archive item |
| Advancements | Yes — root + dust, letters, machine, handle, page, echo, cache |
| One echo | Yes — The Last Print Run |
| One follow-up location | Yes — `ink_archive_cache` |
| Multiplayer + dedicated server | Server-authoritative design |
| EN + PL | Yes |
| Config | `config/echoes_in_ink.json` |
| Sounds | Custom events + subtitles (vanilla audio aliases) |
| Basic animations | Press BER |
| No critical dupe exploits | Hopper-locked press; investigation BE + item data |
| No known crashes | CME in echo tick fixed |

## Build & package

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
./gradlew clean build
# JAR: build/libs/echoes-in-ink-1.0.0.jar
# Do not submit the -sources JAR.
```

## Smoke tests (pre-submit)

1. Fresh world: structure gen or `/echoesinink locate_printshop`
2. Investigate debris → parts → assemble press (`/echoesinink assemble_press` for speed)
3. Print instruction + chronicle; echo fires once
4. Use chronicle 4× → map / coords → `/echoesinink locate_cache`
5. Hopper against press does **not** insert/extract
6. Break investigated block, re-place: no second loot
7. Dedicated server: join mid-echo, disconnect mid-press
8. Language: EN default; PL loads when selected

## Accessibility

- Echo subtitles on by default (`echoSubtitles`)
- Reduced particles / flashes / screen tint flags
- Echo volume slider
- Skip after first full echo (sneak while active)
- Chronicle stage 4 gives precise coords for players who need them

## Known deferrals (OK for ModJam)

- Placeholder / temporary item and block textures (polish later)
- No custom recorded audio (uses vanilla event aliases)
- Structure spacing JSON not hot-reloaded from config integers
- Archive is chat-based, not a full GUI book

## Submit package contents

- [ ] `echoes-in-ink-1.0.0.jar`
- [ ] Short description (see `fabric.mod.json` / README)
- [ ] Banner / icon (`banner.png`, `icon.png` in repo)
- [ ] Licence MPL-2.0
- [ ] Repo link: https://github.com/peterwolf-pl/Echoes-in-Ink---The-Living-Printshop
