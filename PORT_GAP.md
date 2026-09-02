# Port gaps — 1.21.11 V0.5

## Part 1 — keypad chest rendering regressions (build green, block fully functional)

MC 1.21.9/1.21.10 ("Copper Age") replaced the block-entity render pipeline with
extracted render states, and `ChestRenderState` only carries a fixed enum of chest
materials — there is no hook for an arbitrary custom texture the way earlier
versions (and upstream's Forge AT on `ChestRenderer#getMaterial`) allowed.

As a result, on this branch:

- **The keypad chest renders with the vanilla chest texture** in the world and in
  the hand/inventory, instead of its bespoke artwork. Its "active" (lid-open) and
  Christmas texture swaps are gone.
- **The disguise module no longer changes the keypad chest's in-world model.** The
  disguise still works for every other purpose (Jade overlay, block behaviour);
  only the visual swap is missing.
- The bespoke `KeypadChestRenderer` (from-scratch model rendering),
  `KeypadChestItemModel`, `KeypadChestSpecialModelRenderer` and the
  `ItemModelResolver` mixin were removed. `KeypadChestRenderer` now just extends
  vanilla `ChestRenderer`; the item uses a `minecraft:chest` special item model
  (`assets/securitycraft/items/keypad_chest.json`) pointing at
  `securitycraft:entity/chest/inactive`.

To restore the artwork, `KeypadChestRenderer` needs a proper port to the
submit/`SubmitNodeCollector` pipeline with its own `ChestRenderState` subclass and
material selection, matching how upstream `1.21.11` does it (which relies on a
`state.customMaterial` field that does not exist yet in 1.21.10 — so 1.21.10 needs
a bespoke submit path).

## Part 2 — newer reinforced blocks: NOT DONE

Vanilla 1.21.10 has these blocks that the 540-block V0.5 set omits and upstream
`1.21.11` reinforces. All confirmed present in the 1.21.10 client jar
(`data/minecraft/loot_table/blocks/<b>.json`):

- `reinforced_copper_bars`, `reinforced_exposed_copper_bars`,
  `reinforced_weathered_copper_bars`, `reinforced_oxidized_copper_bars` (4)
- `reinforced_exposed_lightning_rod`, `reinforced_weathered_lightning_rod`,
  `reinforced_oxidized_lightning_rod` (3) — plain `reinforced_lightning_rod`
  already exists
- `reinforced_{oak,spruce,birch,jungle,acacia,dark_oak,mangrove,cherry,bamboo,crimson,warped,pale_oak}_shelf` (12)

`reinforced_chiseled_resin_bricks` is **already in the set** — nothing to do there.

Upstream generated assets live at
`repos/Geforce132/SecurityCraft/contents/src/generated/resources/...?ref=1.21.11`
(blockstates named `reinforced_copper_bars.json` etc. already exist there). The
copper bars can model on the existing `reinforced_iron_bars`; the lightning rods
on the existing `reinforced_lightning_rod`; the shelves are a block type this port
does not have yet (horizontal-facing, `SHELF`/powered states) and need their
block class, blockstate and models built fresh. Registration follows the existing
reinforced-block pattern in `SCContent`.
