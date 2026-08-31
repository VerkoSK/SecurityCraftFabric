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

---

## V0.6 — Access control

The largest single gap: everything that decides *who may open what*. The passcode blocks already
exist, so this release is about the other two keys the original offers — keycards and eye scans —
and about the doors they open.

| Content | Notes |
| --- | --- |
| Keycard Reader, Keycard Lock | the block half of the keycard system |
| Keycard levels 1–5, Limited Use Keycard, Keycard Holder | the items, including the per-owner link |
| Universal Key Changer | resets a block's passcode or keycard link |
| Codebreaker | the attacker's side of the same system, with its cooldown and failure chance |
| Keypad Door, Keypad Trapdoor | passcode-locked doors, on top of V0.5's reinforced door work |
| Retinal Scanner, Scanner Door, Scanner Trapdoor | opens for whoever the owner allows, by name |
| Version checker + update notification | asked for since V0.5; small, and useful from here on |

**Depends on:** V0.5's passcode and ownership work. Nothing else blocks it.

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

## Known gaps that are not new content

These are places where the port already has the content but not all of the original's behaviour.
They get folded into whichever release touches the same area.

- **Lens colouring on 1.20.6** — the recipe does not apply the dye there.
- **Module automation** — the original lets hoppers insert modules through a Forge capability. Fabric's
  equivalent is the Transfer API; nothing in the port exposes one yet.
- **Door activators** — the original has an `IDoorActivator` registry so any SecurityCraft block can
  open a door or fence gate. The port hardcodes the keypad instead; the registry arrives with V0.6,
  when there is more than one thing that can open a door.
- **Disguise module** — works on the keypad and laser block. Every further disguisable block added
  from V0.6 onwards has to be wired into the same baked-model wrapper.
