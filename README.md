# Item Alchemy Expansion — Minecraft 26.2 Fabric（非官方移植）

> **这是非官方（unofficial）移植版。**
> 本项目是原作者 **luckfun233** 的 [Item-Alchemy-Expansion](https://github.com/luckfun233/Item-Alchemy-Expansion) 的
> **Minecraft 26.2 + Fabric 移植**，基于原作者 1.2.0 源码修改。
> 它**不是原作者发布的产品，也未获得原作者背书**；26.2 移植引入的问题请不要反馈给原作者。
>
> 26.2 移植维护者：**koutakutenn-sys**
>
> 原项目（1.20.1）：https://github.com/luckfun233/Item-Alchemy-Expansion
> CurseForge（原版）：https://www.curseforge.com/minecraft/mc-mods/item-alchemy-expansion

[![Releases](https://img.shields.io/badge/download-releases-blue.svg)](https://github.com/koutakutenn-sys/Item-Alchemy-Expansion/releases)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

[English](README-en.md) | 中文

Item Alchemy 的扩展模组，为炼金桌添加 NBT 物品区分、基于配方的自动定价和潜影盒支持。

## 下载

| 版本 | 位置 |
| --- | --- |
| Minecraft **26.2**（本移植） | [Releases](https://github.com/koutakutenn-sys/Item-Alchemy-Expansion/releases) |
| Minecraft 1.20.1（原作者原版） | [原项目](https://github.com/luckfun233/Item-Alchemy-Expansion) |

下载 `itemalchemy-expansion-*.jar` 放进实例的 `mods` 目录即可；不要放 `*-sources.jar` 或 `*-native-dev.jar`。

## 运行环境

| 项目 | 要求 |
| --- | --- |
| Minecraft | **26.2** |
| Loader | **Fabric**（Fabric Loader ≥ 0.19.5） |
| Java | **25**（26.2 本身的要求） |

## AI 开发声明

本项目在开发过程中使用了 AI 参与代码编写、重构与文档生成。AI 产出的内容均经过人工测试验证以确保功能正常。若发现问题，欢迎通过 issue 反馈。

## 依赖

必需：

| 依赖 | 版本要求 | 说明 |
| --- | --- | --- |
| [Item Alchemy](https://modrinth.com/mod/item-alchemy) | **1.3.9** | 本移植就是它的附属模组 |
| [MCPitanLib](https://modrinth.com/mod/mcpitanlib) | **≥ 4.0.7-fix.1**（`mcpitanlib-4.0.7-fix.1-26.2-fabric.jar`） | 必须使用这个修复构建，见下方说明 |
| Fabric API | 26.2 对应版本（如 `0.161.0+26.2`） | |
| littleintermediaryfallback | 1.0.2.262 | **不需要单独安装**，已内嵌在 MCPitanLib 4.0.7-fix.1 中 |

### 为什么必须是 MCPitanLib `4.0.7-fix.1`

Fabric 的版本比较规则认为 `4.0.7-fix.1` **低于** `4.0.7`，因此如果装的是正式版 4.0.7，加载器会报依赖不满足：

```
mod 'Item Alchemy Expansion' requires 'mcpitanlib' of version 4.0.7-fix.1 or above,
but only mcpitanlib 4.0.7 is present
```

看到这条报错说明 MCPitanLib 装错了版本，换回 `4.0.7-fix.1` 即可，**不要**修改本模组的依赖声明。

### 关于 littleintermediaryfallback

无需单独下载：它已作为 jar-in-jar 打包在 MCPitanLib `4.0.7-fix.1` 内部，装上该 MCPitanLib 后 Fabric 会自动加载。

### 可选

- [Cloth Config](https://modrinth.com/mod/cloth-config)（26.2.155）— 游戏内配置界面
- [Mod Menu](https://modrinth.com/mod/modmenu)（20.0.2）— 配置界面入口

## 从源码构建

> **注意：目前无法 clone 后直接构建。** `build.gradle` 依赖本地 `libs/` 目录下的依赖 jar，
> 这些 jar **没有随仓库提交**（避免公开分发第三方二进制）。请先按
> [PORT_26.2.md](PORT_26.2.md) 准备依赖，再执行下面的命令。

```sh
git clone -b 26.2 https://github.com/koutakutenn-sys/Item-Alchemy-Expansion.git
cd Item-Alchemy-Expansion
# 依赖准备：按 PORT_26.2.md 放入 libs/ 下的依赖 jar

./gradlew clean build     # Linux / macOS
gradlew.bat clean build   # Windows
```

需要 **Java 25**，Gradle Wrapper 固定为 9.5.1；编译产物在 `build/libs/`。

CI 当前未启用（依赖未提交、无法在 CI 中复现构建），原因与恢复步骤见
[`.github/workflows/build.yml.disabled`](.github/workflows/build.yml.disabled)。

## 问题反馈

- **26.2 移植版的问题请在本仓库反馈**：[koutakutenn-sys/Item-Alchemy-Expansion/issues](https://github.com/koutakutenn-sys/Item-Alchemy-Expansion/issues)
- 崩溃请附完整日志（`logs/latest.log` 或 `crash-reports/` 内的文件），并写明 Minecraft、Fabric Loader、Item Alchemy、MCPitanLib 的版本
- **请不要把 26.2 移植相关的问题提交到原作者仓库**；1.20.1 原版的问题请去[原项目 issue 区](https://github.com/luckfun233/Item-Alchemy-Expansion/issues)

## 移植说明

这是基于原作者完整 1.2.0 源码的兼容移植，不是精简重写版。运行环境、构建步骤、26.2 适配要点与回归测试范围见
[PORT_26.2.md](PORT_26.2.md)。

## Credits

- **luckfun233** — author of [Item-Alchemy-Expansion](https://github.com/luckfun233/Item-Alchemy-Expansion); this repository is an unofficial Minecraft 26.2 port of that project
- **Pitan** — author of [item-alchemy](https://github.com/Pitan76/item-alchemy), the upstream mod this expands on
- **MisterPeModder** — author of [ShulkerBoxTooltip](https://github.com/MisterPeModder/ShulkerBoxTooltip); its preview interaction design inspired the built-in shulker preview
- **mezz** — author of [JustEnoughItems](https://github.com/mezz/JustEnoughItems); its recipe scanning and ingredient normalization approach informed the auto-pricing implementation
- **koutakutenn-sys** — Minecraft 26.2 port maintainer（非官方移植维护者）

## License

MIT，详见 [LICENSE](LICENSE)。
原始代码版权归原作者 **luckfun233** 所有；26.2 移植部分同样以 MIT 授权。
本仓库是**非官方移植**，与原作者无隶属或背书关系。
