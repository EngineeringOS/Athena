# `:kernel:compiler`

[English](README.md) | 简体中文

`:kernel:compiler` 模块是 Athena 的编排核心。它暴露公共编译器门面，维护编译管线报告，发布插件契约与发现逻辑，协调领域语义，解析治理知识包，校验外部边界描述符，派生显式 `Layout IR`、显式 `Geometry IR`，并驱动首条基于几何的下游后端链路。

## 职责

- 通过 `AthenaCompiler` 暴露 `parse`、`lower`、`compile` 入口。
- 保持声明的 pass 顺序稳定：`PARSE -> LOWER -> VALIDATE -> DOWNSTREAM_DERIVATION`。
- 将语法层源文档 lowering 为规范 `Engineering IR`。
- 运行通用语义校验与领域插件校验。
- 从规范 `Engineering IR` 与类型化 `ViewDefinition` 贡献派生受支持的 `Layout IR` 文档。
- 从显式 `Layout IR` 派生受支持的 `Geometry IR` 文档。
- 发布核心拥有的插件契约、manifest、校验、发现与已批准清单模型。
- 加载并解析治理知识包。
- 加载并校验外部边界描述符。
- 从选定的 `Geometry IR` 派生运行时查看器模型。
- 将选定的 `Geometry IR` 直接送入 SVG 后端。

## 主要区域

- `AthenaCompiler`：门面与管线编排。
- `LayoutIrDeriver`：受支持视图的确定性 `Engineering IR -> Layout IR` 派生。
- `GeometryIrDeriver`：受支持视图的确定性 `Layout IR -> Geometry IR` 派生。
- `CompilerModels.kt`：公共编译结果模型。
- `EngineeringIrLowerer`：语法到 IR 的 lowering。
- `plugin/*`：插件契约、manifest、校验、发现与领域协调。
- `knowledge/*`：治理知识包模型、加载与解析。
- `boundary/*`：外部边界描述符模型、加载与解析。

## 增量刷新边界

Story `2.3` 为 M2 增加了第一个收敛的增量重算证明：

- 范围只限于运行时拥有的 `connect ports` 语义命令路径。
- 验证、`Layout IR`、`Geometry IR` 与下游渲染都会报告本次刷新是保持 scoped reuse，还是诚实地回退到 `FULL_FALLBACK`。
- 当刷新后的文档结构仍然稳定时，`LayoutIrDeriver` 与 `GeometryIrDeriver` 会复用未变化的投影对象。
- 如果安全的 scoped merge 不成立，编译器必须报告 `FULL_FALLBACK`，而不是假装仍然是增量刷新。
- 规范语义真源不会移动。运行时修改 `Engineering IR`，编译器再从该规范状态重新推导下游工件。

## 依赖

- `:kernel:language`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`
- `:kernel:svg-renderer`

仅测试依赖：

- `:extensions:domain-electrical`

## 边界

该模块不拥有 DSL 语法本身，不拥有规范 IR 结构，也不拥有具体的 Electrical/Runtime 领域规则。它负责编排这些部分，并保持架构规则不变：DSL 是作者输入真源，`Engineering IR` 是规范模型，`Layout IR` 是首个显式下游投影层，`Geometry IR` 是面向渲染的下游层，而渲染器必须由几何输入驱动，而不是再走语义快捷路径。

## 验证

```bash
./gradlew :kernel:compiler:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:compiler:test
```
