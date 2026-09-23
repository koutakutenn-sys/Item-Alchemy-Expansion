# Item Alchemy Expansion — Minecraft 26.2 Fabric Port

> Minecraft 26.2 Fabric port of Item Alchemy Expansion, maintained by **koutakutenn-sys**. Based on the original 1.2.0 release by **luckfun233**.

[![Releases](https://img.shields.io/badge/download-releases-blue.svg)](https://github.com/koutakutenn-sys/Item-Alchemy-Expansion/releases)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

English | [中文](README.md)

An addon for [Item Alchemy](https://github.com/Pitan76/item-alchemy) that adds NBT-aware item distinction, recipe-based auto-pricing, and shulker box support to the Alchemy Table.

## Download

| Version | Where |
| --- | --- |
| Minecraft 26.2 | [Releases](https://github.com/koutakutenn-sys/Item-Alchemy-Expansion/releases) |
| 1.20.1 | [upstream project](https://github.com/luckfun233/Item-Alchemy-Expansion) |

Drop `itemalchemy-expansion-*.jar` into your instance `mods` folder. Do not install `*-sources.jar` or `*-native-dev.jar`.

## Environment

| Item | Requirement |
| --- | --- |
| Minecraft | **26.2** |
| Loader | **Fabric** (Fabric Loader ≥ 0.19.5) |
| Java | **25** (required by 26.2 itself) |

## AI-Assisted Development Notice

This project used AI during development for code writing, refactoring and documentation. All AI-generated content has been manually tested to ensure it works properly. If you find any issues, feel free to report them through an issue.

## Requirements

Required:

| Mod | Version | Notes |
| --- | --- | --- |
| [Item Alchemy](https://modrinth.com/mod/item-alchemy) | **1.3.9** | the mod this addon extends |
| [MCPitanLib](https://modrinth.com/mod/mcpitanlib) | **≥ 4.0.7-fix.1** (`mcpitanlib-4.0.7-fix.1-26.2-fabric.jar`) | this specific fix build is required, see below |
| Fabric API | matching 26.2 build (e.g. `0.161.0+26.2`) | |
| littleintermediaryfallback | 1.0.2.262 | **do not install separately** — it is bundled inside MCPitanLib 4.0.7-fix.1 |

### Why MCPitanLib `4.0.7-fix.1` specifically

Fabric treats `4.0.7-fix.1` as **lower** than `4.0.7`, so the plain 4.0.7 release fails the dependency check:

```
mod 'Item Alchemy Expansion' requires 'mcpitanlib' of version 4.0.7-fix.1 or above,
but only mcpitanlib 4.0.7 is present
```

If you see that, install the `4.0.7-fix.1` build again. Do **not** edit this mod's dependency metadata.

### About littleintermediaryfallback

No separate download is needed: it ships as a jar-in-jar inside MCPitanLib `4.0.7-fix.1` and is loaded automatically.

### Optional

- [Cloth Config](https://modrinth.com/mod/cloth-config) (26.2.155) — in-game config screen
- [Mod Menu](https://modrinth.com/mod/modmenu) (20.0.2) — config screen entry point

## Building from source

> **Note: a clean clone cannot be built directly right now.** `build.gradle` consumes dependency jars from a local
> `libs/` directory which is **not committed** (to avoid redistributing third-party binaries). Prepare those jars first,
> as described in [PORT_26.2.md](PORT_26.2.md).

```sh
git clone -b 26.2 https://github.com/koutakutenn-sys/Item-Alchemy-Expansion.git
cd Item-Alchemy-Expansion
# prepare libs/ dependencies as described in PORT_26.2.md

./gradlew clean build     # Linux / macOS
gradlew.bat clean build   # Windows
```

Java **25** is required; the Gradle Wrapper is pinned to 9.5.1. The compiled jar lands in `build/libs/`.

CI is currently disabled (the build is not reproducible without the uncommitted dependency jars); see
[`.github/workflows/build.yml.disabled`](.github/workflows/build.yml.disabled) for the reason and how to restore it.

## Reporting issues

- 26.2 port issues → [this repository](https://github.com/koutakutenn-sys/Item-Alchemy-Expansion/issues)
- 1.20.1 upstream issues → [upstream repository](https://github.com/luckfun233/Item-Alchemy-Expansion/issues)

## About this port

Port of the original, complete 1.2.0 sources. See [PORT_26.2.md](PORT_26.2.md) for environment, build steps, 26.2 API notes and the regression test scope.

## Credits

- **luckfun233** — original author
- **Pitan** — author of [item-alchemy](https://github.com/Pitan76/item-alchemy), the upstream mod this expands on
- **MisterPeModder** — author of [ShulkerBoxTooltip](https://github.com/MisterPeModder/ShulkerBoxTooltip); its preview interaction design inspired the built-in shulker preview
- **mezz** — author of [JustEnoughItems](https://github.com/mezz/JustEnoughItems); its recipe scanning and ingredient normalization approach informed the auto-pricing implementation
- **koutakutenn-sys** — 26.2 port maintainer

## License

MIT — see [LICENSE](LICENSE).
26.2 port additions © 2026 koutakutenn-sys, licensed under MIT.
