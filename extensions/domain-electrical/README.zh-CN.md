# `:extensions:domain-electrical`

[English](README.md) | 简体中文

`:extensions:domain-electrical` 模块是 Athena 的首个真实领域插件。它把 M0 的 Electrical/Runtime lowering 与 validation 规则封装在 kernel 所有的插件契约之后，证明领域语义可以驻留在编译器核心之外。

## 职责

- 以 `AthenaDomainPlugin` 形式实现 `ElectricalRuntimeDomainPlugin`。
- 声明包含身份、类型、兼容性与扩展点的核心插件清单。
- 为 `device`、`port` 与 `connect` 声明贡献 lowering。
- 通过类型化核心契约贡献首批受支持的 `cabinet` 与 `wiring` 视图定义。
- 贡献 Electrical/Runtime 专属语义诊断，例如设备类型、端口方向与信号兼容性检查。
- 在 `META-INF/services/com.engineeringood.athena.plugin.AthenaPlugin` 下发布 JVM `ServiceLoader` 注册。

## 主要类型

- `ElectricalRuntimeDomainPlugin`
- `ElectricalRuntimeDomainMarker`

## 依赖

- `:kernel:compiler`
- `:kernel:plugin-api`
- `:kernel:language`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:runtime`

## 边界

该模块不拥有规范 IR schema、编译 pass 顺序、通用语义校验、公共插件 SPI 所有权，或插件发现机制。这些仍然属于 `:kernel:engineering-model`、`:kernel:plugin-api`、`:kernel:compiler` 与 `:kernel:validation`。

## 验证

```bash
./gradlew :extensions:domain-electrical:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :extensions:domain-electrical:test
```
