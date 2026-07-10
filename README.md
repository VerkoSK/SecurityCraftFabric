# SecurityCraft (Fabric)

A **Fabric** port of [SecurityCraft](https://github.com/Geforce132/SecurityCraft), the popular
security/defence mod originally written for Forge/NeoForge by the SecurityCraft team
(Geforce, bl4ckscor3, Redstone_Dubstep, ChainmailPickaxe).

> The original mod is licensed MIT. This port keeps that license and preserves the original
> copyright and attribution. It is **not** affiliated with or endorsed by the original authors.

---

## Status

This is an **incremental, honest** port. SecurityCraft is one of the largest Minecraft mods
(~565 Java classes + ~5,000 resource files **per version**), so a full 1:1 port of every feature
across many Minecraft versions is a long road. Rather than dump a non-compiling code drop, this
repo grows a **working core** feature-by-feature and version-by-version.

### Ported so far (base branch, MC 1.21.1)

| Feature | State |
|---|---|
| Reinforced blocks (stone, cobblestone, stone bricks, smooth stone, oak planks, dirt, iron block, glass) | ✅ blast-resistant, own creative tab, textures, loot |
| Keypad | ✅ ownable + salted-hash passcode + redstone pulse + GUI + networking |
| Owner API (`Owner`, `IOwnable`, `OwnableBlockEntity`) | ✅ foundation for all ownable blocks |
| `/securitycraft` command | ✅ `help`, `version` |
| Creative tab, lang, models, blockstates, loot tables | ✅ |

See [PORTING.md](PORTING.md) for the full roadmap (remaining features + the version matrix).

---

## Building

Requires JDK 21.

```bash
./gradlew build
```

The built jar lands in `build/libs/`. Run the dev client with `./gradlew runClient`.

## Target versions

The port is being carried across the last ~10 Minecraft releases the upstream mod supports.
The base branch targets **1.21.1**; each additional version is added as its own branch/target
(see [PORTING.md](PORTING.md)). Newer versions (1.21.2+) require extra work because Mojang made
block/item registration IDs mandatory — that is tracked per-version.

## Credits

- **Original mod & all assets:** the SecurityCraft team — Geforce, bl4ckscor3, Redstone_Dubstep,
  ChainmailPickaxe. <https://github.com/Geforce132/SecurityCraft>
- **License:** [MIT](LICENSE) (unchanged from upstream).
