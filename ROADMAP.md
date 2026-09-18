# Road to V1.0

V1.0 means "everything the original SecurityCraft has, working the same way, on every supported
Minecraft version". This file lists what is still missing and which release it is planned for.

Everything is measured against the original mod's own branch for the same Minecraft version
(`Geforce132/SecurityCraft`), so "done" always means *behaves like the original*, not *behaves
plausibly*.

Development happens on **1.20.1** first. A release is only propagated to the other 14 branches once
it has been tested there.

---

## Done

**V0.1 – V0.3** — reinforced block set (444 blocks) and the Universal Block Reinforcer/Remover,
keypad, key panel, keypad frame, laser block + laser fields + lenses, portable radar, all eight
modules, the allow/deny list editor, the disguise module screen.

**V0.4** — the whole explosives set (42 entries: mine, bouncing betty, claymore, IMS, track mine and
every block mine), the Mine Remote Access Tool, wire cutters, JEI and Jade integration, creative tab
ordering.

**V0.5** [RELEASING 25.8.2026] — the ownership system (every reinforced block, door and trapdoor remembers its owner and
can only be broken by them; team ownership; variable break time), the Universal Owner Changer, the
Universal Block Modifier and the whole Customize screen including the module enable/disable toggle,
the electrified iron fence and fence gate, 47 further reinforced blocks (carpets, glazed terracotta,
ladder, lanterns, chain, end rod, cobweb, scaffolding, lever, redstone lamp, grass block, podzol,
mycelium, sea lantern, bookshelf), fake water and fake lava, the SecurityCraft Manual, and the
passcode-protected chest, barrel, furnace, smoker and blast furnace, and the reinforced
hopper, dispenser, dropper, observer, pistons, cauldrons, lectern and chiseled bookshelf, the
crystal quartz set and the secret signs.

**V0.6** [RELEASING 18.9.2026] — access control: the keycard system (Keycard Reader, Keycard Lock,
keycard levels 1–5, the Limited Use Keycard and Keycard Holder, all with per-owner linking), the
Universal Key Changer, the Codebreaker, Keypad Door and Keypad Trapdoor on top of V0.5's reinforced
door work, the Retinal Scanner with the Scanner Door and Scanner Trapdoor, and the version checker
with its update notification.

---

## V0.7 — Surveillance

Everything that watches and reports. The Portable Radar already covers the "who walked past" case;
this release adds the rest of the original's detection blocks and the camera system.

| Content | Notes |
| --- | --- |
| Security Camera + Camera Monitor | includes the camera entity the player's view is mounted on |
| Username Logger | |
| Motion Activated Light | |
| Block Change Detector | |
| Projector | |
| Alarm, Panic Button | |
| Secure Trading Station | |

**Depends on:** the camera system needs its own view-mounting entity and a packet to enter and leave
it; that is the bulk of the release.

---

## V0.8 — Defence

The blocks that shoot back.

| Content | Notes |
| --- | --- |
| Sentry + Sentry Remote Access Tool | its own entity, targeting modes, disguise, three sentry modes |
| Taser (and the powered variant) | plus the bullet entity |
| Trophy System | shoots projectiles down |
| Protecto | |
| Floor Trap, Cage Trap | |
| Rift Stabilizer | |
| Inventory Scanner + scanner field | its prohibited-item list and the storage mode |

**Depends on:** V0.7's module and targeting groundwork.

---

## V0.9 — Storage and the block pocket

What is left of the original's own blocks once the reinforced set is complete.

| Content | Notes |
| --- | --- |
| Display Case, Glow Display Case | |
| Secure Redstone Interface | |
| Block Pocket Manager + Block Pocket Wall | the original's largest single block by code size |

---

## V1.0 — The remainder, then every Minecraft version

| Content | Notes |
| --- | --- |
| Sonic Security System + Portable Tune Player | note-block listening and the linked-block system |
| Security Sea Boats (9 types) | |
| Briefcase | its own passcode and inventory |
| Incognito Mask, Admin Tool | |
| Horizontal reinforced iron bars | |

Then: bring V1.0 to all 15 supported Minecraft versions, and a full pass comparing this port's
behaviour against the original's branch for each of them.

---

## Beyond the original

Features this port has that upstream SecurityCraft does not.

- **Locking any modded container** — the Key Panel converts a vanilla-compatible chest (see the
  `convertible_chests` tag above) into a real keypad chest, same as upstream. For a container it
  can't convert (a modded chest/barrel/etc that doesn't extend `ChestBlock`), right-clicking it
  with a Key Panel instead locks that position, tracked in `ContainerLockData` rather than by
  replacing the block. The locked position then behaves exactly like a real keypad chest - same
  set/check passcode screens, opening it requires the code same as everyone else including the
  owner, and only the owner (and their team) can break it - enforced globally regardless of the
  block's mod of origin. Right-click your own lock again with a Key Panel to remove it.

---

## Known gaps that are not new content

These are places where the port already has the content but not all of the original's behaviour.
They get folded into whichever release touches the same area.

- **Lens colouring on 1.20.6** — the recipe does not apply the dye there.
- **Module automation** — the original lets hoppers insert modules through a Forge capability. Fabric's
  equivalent is the Transfer API; nothing in the port exposes one yet.
- **Disguise module** — works on the keypad and laser block. Every further disguisable block added
  from V0.6 onwards has to be wired into the same baked-model wrapper.
