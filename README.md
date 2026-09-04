# SecurityCraft (Fabric)

A **Fabric** port of [SecurityCraft](https://github.com/Geforce132/SecurityCraft), the security and
defence mod originally written for Forge/NeoForge by the SecurityCraft team (Geforce, bl4ckscor3,
Redstone_Dubstep, ChainmailPickaxe).

> The original mod is licensed MIT. This port keeps that license and preserves the original
> copyright and attribution. It is **not** affiliated with or endorsed by the original authors.

[![image](https://media.forgecdn.net/attachments/description/1615498/description_366d7ee0-3830-4ded-b411-0bf089a21dca.png)](https://billing.kinetichosting.com/aff.php?aff=1031)

Want a Minecraft server for this mod? [Kinetic Hosting](https://billing.kinetichosting.com/aff.php?aff=1031) has you covered.

---

## Status — V0.5

SecurityCraft is one of the largest Minecraft mods there is, so this port grows a **working core**
feature by feature rather than dropping a half-compiling copy of everything at once. Every feature
is measured against the original mod's own branch for the same Minecraft version, so "done" means
*behaves like the original*, not *behaves plausibly*.

### In the mod so far

| | |
| --- | --- |
| **Reinforced blocks** | the complete set — every block the original reinforces on this Minecraft version, including the carpets, glazed terracotta, ladder, lanterns, chain, end rod, cobweb, scaffolding, lever, and the functional ones: hopper, dispenser, dropper, observer, pistons, cauldrons, lectern, chiseled bookshelf |
| **Ownership** | every owned block remembers who placed it and can only be broken by them, with team ownership, variable break time and the Universal Owner Changer |
| **Universal Block Reinforcer / Remover** | all three reinforcer levels, with their colour chooser |
| **Universal Block Modifier** | the full Customize screen: module slots, per-block options, and enabling or disabling a module without taking it out |
| **Modules** | all eight, with the allow/deny list editor and the disguise module screen |
| **Passcode-protected blocks** | keypad, key panel, keypad frame, and the passcode chest, barrel, furnace, smoker and blast furnace |
| **Laser block** | laser fields, per-side configuration and dyed lenses |
| **Explosives** | the whole set of 42 — mine, bouncing betty, claymore, IMS, track mine and every block mine — plus the Mine Remote Access Tool and the wire cutters |
| **Portable Radar** | |
| **Electrified iron fence and fence gate** | |
| **Secret signs** | standing, wall and hanging, for every wood type |
| **Crystal quartz set** | plain and reinforced |
| **Fake water and fake lava** | |
| **SecurityCraft Manual** | the in-game manual, with its recipe pages |
| **Mod compatibility** | JEI and Jade |

[**ROADMAP.md**](ROADMAP.md) lists what is still missing and which release each piece is planned
for, all the way to V1.0.

---

## Supported Minecraft versions

One branch per Minecraft version, each a complete source tree rather than a preprocessor target:

`1.20.1` · `1.20.6` · `1.21.1` · `1.21.3` · `1.21.4` · `1.21.5` · `1.21.6` · `1.21.7` · `1.21.8` ·
`1.21.10` · `1.21.11` · `26.1` · `26.1.1` · `26.1.2` · `26.2`

New features land on **1.20.1** first and are only carried to the other branches once they have been
tested there, so the newest release may reach some branches a little later than others.

## Building

Check out the branch for the Minecraft version you want, then:

```bash
./gradlew build
```

The jar lands in `build/libs/`. `./gradlew runClient` and `./gradlew runServer` start a development
game. The JDK each branch needs is declared in its own `build.gradle` and downloaded automatically
by the toolchain resolver — 17 for 1.20.1, newer for the later branches.

## Credits

- **Original mod and all assets:** the SecurityCraft team — Geforce, bl4ckscor3, Redstone_Dubstep,
  ChainmailPickaxe. <https://github.com/Geforce132/SecurityCraft>
- **Fabric port:** Verkos.
- **License:** [MIT](LICENSE), unchanged from the original.
