# 26.2 branch — V0.5 port status

## TL;DR
**DONE.** `./gradlew build` is green, `./gradlew runClient` reaches the title screen
(sits on vanilla's first-run AccessibilityOnboardingScreen — the game is fully up),
`mod_version` unchanged at 0.5. All work committed on top of `a654631` (see
`git log --oneline a654631..HEAD`).

## What landed
- **Task 1 — build against MC 26.2.** Full API-generation port (588 -> 0 errors).
  Three commits by theme: block/registration/BE API, rendering/screen API,
  networking/recipe/compat API.
- **Task 2 — MRAT hotfix.** `components/BoundMines.java` committed alone, byte-identical
  to `git show 21c852d:...BoundMines.java`, message matching `21c852d`.
- **Task 3 — the runtime-crash fixes.**
  - `1a851ce` item half: `.setId(...)` added to the 22 secret (hanging) sign items.
    Blocks + the 5 named items already carried `setId` from the prior sessions;
    `registerReinforcedCopy` already builds inside the `register(name, key -> …)` factory.
  - `cc51db6`: creative-tab decoration loop now skips any reinforced block whose
    `asItem() == Items.AIR` and logs a warning.
  - `3352392`: `models/item/reinforced_chain.json` layer0 -> `minecraft:item/iron_chain`
    (26.2 renamed the vanilla chain; SCContent + block model were already updated).
  - `900bb0d`: AW opens + makes mutable `BlockEntityType#validBlocks`; new
    `SCContent.allowVanillaBlockEntityOn(vanillaType, scType)` called for HOPPER,
    LECTERN, CHISELED_BOOKSHELF, HANGING_SIGN, BRUSHABLE_BLOCK, CREAKING_HEART.
    `SecretSignBlockEntity` uses the 3-arg `super(SECRET_SIGN_BLOCK_ENTITY, pos, state)`.
- **Task 4 — reinforced blocks (partial, per brief).** Restored the 21 blocks the
  V0.5-propagation regression dropped: reinforced set is back to **540**, matching
  1.21.11 (pale oak family incl. stripped/wood, resin brick family, pale moss block,
  tuff slab/stairs/wall, chiseled resin bricks). Assets/loot/tags/lang carried from
  1.21.11.

## Deferred (Task 4 26.2-only additions — NOT done)
26.2 is the newest MC, so upstream `C:/u262` reinforces blocks that older branches
can't. These were **not** added and should be a follow-up:
- `copper_bars` and its oxidation + waxed variants
- oxidised / waxed `lightning_rod` variants
- the wood `*_shelf` blocks (1.21.7+)
- anything else in `C:/u262/src/generated/resources/assets/securitycraft/blockstates/reinforced_*`
  that vanilla 26.2 has and this branch still lacks
Method: same as the restore above — `C:/u262` generated resources or 1.21.11 for
assets, one `REINFORCED`/`REINFORCED_COPIES` row + loot table + tags + lang each.

## Known cosmetic gaps (compile/run clean, noted not chased)
- **Keypad chest** renders with the **vanilla chest texture**, not its own
  active/inactive artwork. 1.21.6+ moved chest texture selection into
  `ChestRenderState` with no extension point and NeoForge's `customSprite` patch has
  no Fabric equivalent; a mixin into `ChestRenderer` would be needed to restore it.
  The keypad chest item uses a `minecraft:special` / `minecraft:chest` model.
- **SC Manual**: the in-book crafting-recipe grid is dropped. 1.21.6+ replaced the
  flat `Recipe`/`Ingredient` lookup with the opaque `RecipeDisplay`/`SlotDisplay`
  system. The manual still shows every page, its text, icons and the patron list.
- **Secret signs**: the disallowed side's text is hidden by blanking its `SignText`
  in the render state (Fabric has no per-side `submitSignText` hook like NeoForge).
- `securitycraft:block/reinforced_chiseled_bookshelf_*_slot_*` models warn "Missing
  texture references: particle" at load — cosmetic, pre-existing, 12 models.
