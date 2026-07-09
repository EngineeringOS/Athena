# `:kernel:compiler`

[English](README.md) | 简体中文

`:kernel:compiler` 模块是 Athena 的编译编排核心。它公开编译器门面，拥有编译流水线报告，协调领域语义，解析受治理的知识包，校验外部边界描述符，加载并验证受治理的 Athena 仓库根契约，推导显式的 `Layout IR` 与 `Geometry IR`，并驱动第一条基于几何结果的下游后端路径。

## 职责

- 公开 `AthenaCompiler` 的 `parse`、`lower`、`compile` 入口。
- 保持声明式 pass 顺序稳定：`PARSE -> LOWER -> SEMANTIC_ENRICHMENT -> VALIDATE -> BACKEND_PREPARATION -> BACKEND_EMISSION`。
- 将语法层源码 lowering 为规范的 `Engineering IR`。
- 运行通用语义校验与领域插件校验。
- 从规范 `Engineering IR` 与类型化 `ViewDefinition` 贡献推导受支持的 `Layout IR`。
- 从显式 `Layout IR` 推导受支持的 `Geometry IR`。
- 消费 `:kernel:plugins:plugin-api` 提供的稳定公共 SPI。
- 消费由 `:kernel:plugins:plugin-host` 治理的已批准插件清单。
- 加载并解析受治理知识包。
- 加载并校验外部边界描述符。
- 加载并校验受治理的 Athena 仓库根契约。
- 从选定的 `Geometry IR` 派生运行时 viewer 模型。
- 直接把选定的 `Geometry IR` 送入 SVG 后端。

## 主要区域

- `AthenaCompiler`：门面与流水线编排。
- `LayoutIrDeriver`：确定性的 `Engineering IR -> Layout IR` 推导。
- `GeometryIrDeriver`：确定性的 `Layout IR -> Geometry IR` 推导。
- `CompilerModels.kt`：公开编译结果模型。
- `EngineeringIrLowerer`：语法到 IR 的 lowering。
- `plugin/*`：仅保留编译器拥有的领域协调逻辑。
- `knowledge/*`：受治理知识包模型、加载与解析。
- `boundary/*`：外部边界描述符模型、加载与解析。
- `repository/*`：受治理仓库根契约加载与布局校验。

## 增量刷新边界

Story `2.3` 为 M2 引入了第一条窄范围增量重算证明：

- 范围仅限于 runtime 拥有的 `connect ports` 变更路径。
- validation、layout、geometry、downstream rendering 都会报告是否保持 scoped reuse，还是回退为 full fallback。
- `LayoutIrDeriver` 与 `GeometryIrDeriver` 可以在刷新后的文档结构仍稳定时复用未变化的投影对象。
- 编译器必须保持诚实：如果没有安全的 scoped merge，pass 会报告 `FULL_FALLBACK`，而不是假装增量成功。
- 规范语义真相来源不会改变。runtime 变更的是 `Engineering IR`；编译器从该规范状态重新计算下游工件。

## 依赖

- `:kernel:language`
- `:kernel:plugins:plugin-api`
- `:kernel:plugins:plugin-host`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`
- `:kernel:repository-model`
- `:kernel:svg-renderer`

仅测试依赖：

- `:extensions:domain-electrical`

## 边界

该模块不拥有 DSL 语法本身，不拥有规范 IR 结构，不拥有公共插件 SPI，不拥有宿主插件 source 或 approval 治理，也不拥有具体的 Electrical/Runtime 领域规则。它负责编排这些部分，同时保持架构规则不变：DSL 是作者输入源，`Engineering IR` 是规范模型，`Layout IR` 是第一层显式下游投影，`Geometry IR` 是面向 renderer 的下游层，而 renderer 是消费 geometry 的后端，而不是语义捷径。

## 验证

```bash
./gradlew :kernel:compiler:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:compiler:test
```
