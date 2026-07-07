# `:kernel:compiler`

[English](README.md) | 简体中文

`:kernel:compiler` 模块是 Athena 的编排核心。它公开编译器门面，拥有编译流水线报告，承载插件发现与审批，协调领域语义，解析受治理知识包，校验外部边界描述符，推导显式 `Layout IR`、显式 `Geometry IR`，并驱动首条基于 geometry 的下游后端路径。

## 职责

- 公开 `AthenaCompiler` 的 `parse`、`lower` 与 `compile` 入口。
- 保持声明的 pass 顺序稳定：`PARSE -> LOWER -> VALIDATE -> DOWNSTREAM_DERIVATION`。
- 将语法层源码 lowering 为规范 `Engineering IR`。
- 运行通用语义校验与领域插件校验。
- 从规范 `Engineering IR` 与类型化 `ViewDefinition` 贡献推导支持的 `Layout IR`。
- 从显式 `Layout IR` 推导支持的 `Geometry IR`。
- 消费 `:kernel:plugin-api` 提供的稳定公共 SPI。
- 承载当前 JVM-first 路径下的插件发现、审批、校验与已批准清单模型。
- 解析受治理知识包。
- 校验外部边界描述符。
- 从选定的 `Geometry IR` 派生 runtime viewer 模型。
- 将选定的 `Geometry IR` 直接送入 SVG 后端。

## 主要区域

- `AthenaCompiler`: 门面与流水线编排。
- `LayoutIrDeriver`: 面向受支持视图的确定性 `Engineering IR -> Layout IR` 推导。
- `GeometryIrDeriver`: 面向受支持视图的确定性 `Layout IR -> Geometry IR` 推导。
- `CompilerModels.kt`: 编译器公开结果模型。
- `EngineeringIrLowerer`: 语法到 IR 的 lowering。
- `plugin/*`: 宿主侧插件发现、激活、审批清单与领域协调。
- `knowledge/*`: 受治理知识包模型、加载与解析。
- `boundary/*`: 外部边界描述符模型、加载与解析。

## 依赖

- `:kernel:language`
- `:kernel:plugin-api`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`
- `:kernel:svg-renderer`

测试期额外依赖：

- `:extensions:domain-electrical`

## 边界

该模块不拥有 DSL 语法本身、规范 IR schema、公共插件 SPI，或具体 Electrical/Runtime 领域规则。它负责编排这些部分，并保持架构规则不变：DSL 是作者源码，`Engineering IR` 是规范模型，`Layout IR` 是第一层显式下游投影，`Geometry IR` 是面向渲染的下游层，renderers 是消费 geometry 的后端而不是语义捷径。

## 验证

```bash
./gradlew :kernel:compiler:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:compiler:test
```
