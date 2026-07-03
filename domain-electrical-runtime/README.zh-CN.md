# `:domain-electrical-runtime`

[English](README.md) | 简体中文

`:domain-electrical-runtime` 模块是 Athena 的第一个真实领域插件。它通过核心拥有的插件契约封装 M0 Electrical/Runtime 的 lowering 与校验规则，用来证明领域含义可以存在于编译器核心之外。

## 职责

- 以 `AthenaDomainPlugin` 的形式实现 `ElectricalRuntimeDomainPlugin`。
- 声明由核心拥有的插件 manifest，包括标识、类型、兼容性与扩展点。
- 为 `device`、`port` 与 `connect` 声明提供 lowering 贡献。
- 提供 Electrical/Runtime 专属语义诊断，例如设备类型、端口方向与信号兼容性检查。
- 在 `META-INF/services/com.engineeringood.athena.compiler.plugin.AthenaPlugin` 下发布 JVM `ServiceLoader` 注册。

## 主要类型

- `ElectricalRuntimeDomainPlugin`
- `ElectricalRuntimeDomainMarker`

## 依赖

- `:compiler`
- `:language`
- `:semantics-core`
- `:ir`

## 边界

该模块不拥有规范 IR 结构、编译器 pass 顺序、通用语义校验或插件发现机制。这些能力仍然由 `:compiler`、`:ir` 与 `:semantics-core` 核心拥有。

## 验证

```bash
./gradlew :domain-electrical-runtime:test
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat :domain-electrical-runtime:test
```
