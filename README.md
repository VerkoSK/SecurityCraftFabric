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

## Versions (one branch per Minecraft version)

The port is carried across the last ~10 Minecraft releases the upstream mod supports. Each
version is its own branch (matching upstream's own branch-per-version layout). Check out the
branch for your Minecraft version and run `./gradlew build`.

| Minecraft | Branch | Build |
|---|---|---|
| 1.20.6 | [`1.20.6`](../../tree/1.20.6) | ✅ |
| 1.21.1 | [`1.21.1`](../../tree/1.21.1) | ✅ |
| 1.21.3 | [`1.21.3`](../../tree/1.21.3) | ✅ |
| 1.21.4 | [`1.21.4`](../../tree/1.21.4) | ✅ |
| 1.21.5 | [`1.21.5`](../../tree/1.21.5) | ✅ |
| 1.21.6 | [`1.21.6`](../../tree/1.21.6) | ✅ |
| 1.21.7 | [`1.21.7`](../../tree/1.21.7) | ✅ |
| 1.21.8 | [`1.21.8`](../../tree/1.21.8) | ✅ |
| 1.21.10 | [`1.21.10`](../../tree/1.21.10) | ✅ |
| 1.21.11 | [`1.21.11`](../../tree/1.21.11) | ⛔ blocked by a Loom↔Fabric-API toolchain bug — [details](PORTING.md#known-toolchain-issue-1219) |

9 of the 10 targets build green. The newest, 1.21.11, is fully written and configured; only a
Fabric Loom / Fabric API javadoc-namespace bug (under Mojang mappings, 1.21.9+) blocks its build —
not the mod code, which is identical to the green 1.21.10 branch. See [PORTING.md](PORTING.md).

Each newer version required real API adaptation — mandatory registration IDs (1.21.2),
`EnumProperty<Direction>` (1.21.2), the `items/` model system (1.21.4), `Optional` NBT getters
(1.21.5), `ValueInput`/`ValueOutput` serialization (1.21.6), and the `isClientSide()` / `KeyEvent`
input changes (1.21.9). PORTING.md lists each delta per version.

## Credits

- **Original mod & all assets:** the SecurityCraft team — Geforce, bl4ckscor3, Redstone_Dubstep,
  ChainmailPickaxe. <https://github.com/Geforce132/SecurityCraft>
- **License:** [MIT](LICENSE) (unchanged from upstream).
