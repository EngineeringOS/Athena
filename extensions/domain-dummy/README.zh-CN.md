# `:extensions:domain-dummy`

[English](README.md) | 简体中文

`:extensions:domain-dummy` 模块是 M3 阶段的合成托管证明领域插件。它存在的唯一目的，是证明 Athena 的扩展 SPI 可以承载第二个、非电气的领域，而不需要让 kernel 学会任何 dummy 专属词汇。

## 职责

- 以 `AthenaDomainPlugin` 形式实现 `DummyRuntimeDomainPlugin`。
- 通过稳定的 `:kernel:plugins:plugin-api` 契约发布合成领域 schema。
- 通过稳定 SPI 发布可检查的 validation、compiler-stage、runtime-view 与 renderer contribution 元数据。
- 将 dummy 参与范围限制在显式标记为 dummy 的作者声明上，使其可以与 `:extensions:domain-electrical` 共存。
- 在不添加 kernel 拥有的 dummy 名词、编译器特判或默认全局视图定义扩张的前提下，证明第二个托管领域。
- 在 `META-INF/services/com.engineeringood.athena.plugin.AthenaPlugin` 下发布 JVM `ServiceLoader` 注册。

## 证明词汇

- 组件类型：`Glyph`、`Pulse`、`Totem`
- 连接类型：`DummyLink`
- 归属标记：`domain "dummy-runtime"`

## 主要类型

- `DummyRuntimeDomainPlugin`
- `DummyRuntimeDomainMarker`

## 依赖

- `:kernel:plugins:plugin-api`
- `:kernel:language`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:runtime`

## 边界

该模块不拥有规范 Engineering IR schema、编译 pass 顺序、通用语义校验、公共插件 SPI 所有权，或托管发现机制。它被刻意设计为合成领域，只能作为可扩展性证明，而不是第二个产品领域。

## 验证

```bash
./gradlew :extensions:domain-dummy:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :extensions:domain-dummy:test
```
