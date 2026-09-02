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

## V0.5 client-render fixes back-ported from 1.21.4 (2026-09)
The 1.21.4 line found six client-render bugs after 26.2 was first ported to V0.5.
Applied to 26.2 in place:
- `93e585a` — keypad barrel `getMenuProvider` override (barrel would never open);
  `FakeWater/LavaBlock#getName()` borrow water/lava's name; restore
  `reinforced_fence_gate.png` from `C:/u262` (26.2's copy had regressed, md5
  `329985dd…` vs upstream `95498cdd…`); `SCManualScreen.ChangePageButton` now
  `blitSprite`s the `widget/page_forward|backward[_highlighted]` sprites instead of
  the long-gone `book.png` arrow uv. Also added the `fake_*` block/fluid lang keys
  (folds in `9acb8ea` — the keypad-barrel `useItemOn` half was already correct on
  26.2, and the JEI liquid names were the same lang keys).
- `f198c95` — 43 `items/<name>.json` model definitions generated for the crystal
  quartz set, keypad containers, secret signs, manual, fake buckets, universal
  tools, etc. (skips: `block_mine_overlay`, `remote_access_mine_{idle,linked,not_linked}`).
- `1f2cc53` — tint alpha: 516 `items/*.json` had `"value": 10066329` /
  `"default": 16777215` (alpha-0 → invisible reinforced-item icons + lens); now
  `-6710887` / `-1`. `models/block/keypad_chest.json` was `builtin/entity`
  (checkerboard placed chest) → particle-only model. Restored the missing
  `textures/gui/info_book_icons.png` from 1.21.3.
- `7230faf` — `SCManualScreen` body text: `extractor.textWithWordWrap(…)` now
  passes `dropShadow=false` (the 6-arg overload defaults to true → smeared text).
- `f6ebcde` — **not one of the six.** 80 recipe JSONs (crystal quartz set,
  reinforced smelting/stonecutting, mines, manual, universal tools) were still in
  the pre-1.21.2 `{"item": …}` / `result.item` shape — they would have thrown
  `Couldn't parse data file` on world load. Replaced with the byte-identical
  converted files from `1.21.4`.
- `eff9bae` (block-item naming) was **not** applied: 26.2 already solves it with
  ~512 `item.securitycraft.*` lang aliases + `overrideDescription` on the secret
  sign blocks. `useBlockDescriptionPrefix()` was not introduced.
- `831492d` part (e) (`awardRecipes` on JOIN) and part (f) (`Item.BY_BLOCK` link
  before block register) were **not** applied: the manual's recipe grid is already
  dropped on 26.2 (see below), and `Item#registerBlocks` does not exist on 26.2;
  the decoration-tab `asItem()==AIR` guard logged nothing in `runClient`.

`./gradlew clean build` green; `runClient` brings the game fully up (title /
first-run onboarding). Only securitycraft log noise is the 12 known
`reinforced_chiseled_bookshelf_*_slot_*` `: particle` warnings. No
`builtin/entity` missing-model warning any more.

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
