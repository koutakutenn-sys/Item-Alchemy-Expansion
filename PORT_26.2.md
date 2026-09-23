# Minecraft 26.2 Fabric 移植版

基于原作者 luckfun233 的 1.2.0 完整源码移植，非精简重写。
原始仓库：https://github.com/luckfun233/Item-Alchemy-Expansion
源码基线：`10f429f52435c7f95590b467a597ec22aa8e0512`
26.2 移植维护者：koutakutenn-sys

可直接安装的成品 jar 见 [Releases](https://github.com/koutakutenn-sys/Item-Alchemy-Expansion/releases)。

## 运行环境

- Minecraft Java Edition 26.2、Java 25
- Fabric Loader ≥ 0.19.5（验证于 0.19.5）
- Fabric API 0.161.0+26.2
- Item Alchemy **1.3.9**
- MCPitanLib **4.0.7-fix.1-26.2-fabric**，含 littleintermediaryfallback **1.0.2.262**
- 可选配置界面：Cloth Config 26.2.155、Mod Menu 20.0.2

MCPitanLib 必须是 `4.0.7-fix.1` 这个构建：Fabric 的版本比较规则认为它低于正式版 `4.0.7`，装错会报依赖不满足。
本移植只针对 Item Alchemy 1.3.9 验证。

## 使用

关闭游戏，备份存档，把 `itemalchemy-expansion-1.2.0+26.2.port.1.jar` 放入目标实例的 `mods`。
不要同时保留另一个 Item Alchemy Expansion JAR，也不要安装 `*-sources.jar` 或 `*-native-dev.jar`。
`libs/` 下的依赖 jar 与 `*-native-dev.jar` 都是**构建用**的，不要放进 `mods/`。
首次使用建议先在测试世界验证，不要直接拿唯一存档试。

## 构建和测试

使用 Java 25，Gradle Wrapper 固定为 9.5.1：

```sh
./gradlew clean build
./gradlew runClientGameTest -PportTestBuiltJar
```

`build` 包含实际 Minecraft 注册表上的数据断言测试。客户端测试会打开测试游戏窗口、创建独立世界、完成后退出。
测试模组位于 `src/gametest`，不打包进发布 JAR。生产包在 `build/libs`。

### 构建前需要手动准备的依赖（重要）

`build.gradle` 目前通过 `libs/` 读取本地依赖 jar，而 `libs/` **未提交到仓库**（避免公开分发第三方二进制）。
因此**干净 clone 无法直接构建**，需要自行准备以下 jar 放进 `libs/`：

| 文件 | 来源 | 必需 |
| --- | --- | --- |
| `itemalchemy-1.3.9.jar` | Item Alchemy 1.3.9 原版 jar | 是 |
| `mcpitanlib-4.0.7-fix.1-26.2-fabric.jar` | 上述 MCPitanLib 修复构建 | 是 |
| `cloth-config-26.2.155.jar` | Cloth Config（可选配置界面） | 否 |
| `modmenu-20.0.2.jar` | Mod Menu（可选配置入口） | 否 |

`prepareItemAlchemyCompileView` 会用上面的 Item Alchemy jar 生成仅编译使用的 native-dev 视图，不需要手动准备。
CI 当前未启用（依赖未提交，无法在 CI 中复现构建），原因与恢复步骤见 [`.github/workflows/build.yml.disabled`](.github/workflows/build.yml.disabled)。

### 构建机制

- `build-tools/LegacyMixinBridge.java`：Item Alchemy 1.3.9 的类仍是旧命名空间，且它的运行时转换发生在 Mixin 注入之后；该工具只对注入该依赖的 Mixin 做前置处理，不改写游戏或依赖 JAR。
- 其余代码使用 26.2 官方类名，MCPitanLib 负责运行时转换，Minecraft 与 MCPitanLib 自身的 Mixin 不经过上述处理。

## 已覆盖的回归项目

- 空槽、药水组件、潜影盒内容和槽位、自定义数据往返保存
- 嵌套 NBT 指纹确定性；按配置忽略耐久和修复费用
- 新增 4 个配方加载；EMC 卡数据和存取网络交易
- EMC 卡主界面、存入、取出、配置、记录、EMC 编辑器
- 制卡台界面；转换桌学习和重建带组件的变体
- 渲染钩子实际触发；鼠标悬停槽位命中；Shift 潜影盒预览激活并绘制内容物
- 转能器收取物品和计入 EMC；原版方块实体保存/加载
- 输出器扣除 EMC 和生成物品；自动装置界面
- 配方自动定价扫描；共享账户保存；精确 EMC 网络设置

## 26.2 界面 API 注意点

- `AbstractContainerScreen.hoveredSlot` 不再由鼠标事件写入，改为每帧在 `extractRenderState` 内用私有
  `getHoveredSlot(mouseX, mouseY)` 重新赋值，且方法不可调用；本移植改为读取 `leftPos`/`topPos`
  后自行做几何命中判定。
- 界面渲染/输入使用 GUI 缩放坐标，`MouseHandler.xpos()`/`ypos()` 使用窗口像素坐标；
  两者混用会导致悬停与预览静默失效。
- `Screen.render` 被 26.2 的 `extractRenderState` 取代，屏幕背景由渲染管线统一绘制，
  自定义界面不要再次调用背景绘制（会因重复模糊而崩溃）。

以上为回归测试覆盖范围；大型整合包、多人服务器长期运行与旧存档升级未在此验证。

## 问题反馈

- 26.2 port issues → https://github.com/koutakutenn-sys/Item-Alchemy-Expansion/issues
- 1.20.1 upstream issues → https://github.com/luckfun233/Item-Alchemy-Expansion/issues
- 崩溃请附完整日志（`logs/latest.log` 或 `crash-reports/` 内的文件），并写明 Minecraft / Fabric Loader / Item Alchemy / MCPitanLib 版本

## 许可

原始项目与 26.2 移植均以 MIT 授权，版权声明见 [LICENSE](LICENSE)（保持原作者原样）。
26.2 port additions © 2026 koutakutenn-sys, licensed under MIT.
