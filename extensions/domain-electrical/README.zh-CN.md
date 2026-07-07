# `:extensions:domain-electrical`

[English](README.md) | 简体中文

`:extensions:domain-electrical` 模块是 Athena 在 M3 阶段的参考真实领域插件。它把刻意收窄的 Electrical/Runtime 证明词汇表封装在核心拥有的插件契约之后，用来证明托管领域语义可以留在 kernel 之外，同时 kernel 仍然保持通用。

## 职责

- 以 `AthenaDomainPlugin` 形式实现 `ElectricalRuntimeDomainPlugin`。
- 声明包含身份、类型、兼容性与扩展点的核心拥有插件清单。
- 通过稳定的 `:kernel:plugins:plugin-api` 契约发布 M3 电气证明领域 schema。
- 通过稳定 SPI 发布可检查的 validation、compiler-stage 与 renderer contribution 元数据。
- 通过稳定 SPI 声明并参与受治理的 `LOWER` 与 `VALIDATE` 编译阶段。
- 为 `device`、`port` 与 `connect` 声明贡献 lowering。
- 通过类型化核心契约贡献首批受支持的 `cabinet` 与 `wiring` 视图定义。
- 贡献 Electrical/Runtime 专属语义诊断，例如证明设备类型、端口方向与信号兼容性检查。
- 在 `META-INF/services/com.engineeringood.athena.plugin.AthenaPlugin` 下发布 JVM `ServiceLoader` 注册。

## 证明词汇

- 组件类型：`Lamp`、`Motor`、`Switch`
- 连接类型：`Wire`

## 主要类型

- `ElectricalRuntimeDomainPlugin`
- `ElectricalRuntimeDomainMarker`

## 依赖

- `:kernel:plugins:plugin-api`
- `:kernel:language`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:runtime`

## 边界

该模块不拥有规范 Engineering IR schema、编译 pass 顺序、通用语义校验、公共插件 SPI 所有权，或插件发现机制。这些仍然由 `:kernel:engineering-model`、`:kernel:plugins:plugin-api`、`:kernel:compiler` 与 `:kernel:validation` 持有。

## 验证

```bash
./gradlew :extensions:domain-electrical:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :extensions:domain-electrical:test
```
