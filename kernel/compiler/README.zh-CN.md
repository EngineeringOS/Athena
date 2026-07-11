# `:kernel:compiler`

[English](README.md) | 简体中文

`:kernel:compiler` 模块是 Athena 的编译编排核心。它公开编译器门面，拥有编译流水线报告，协调领域语义，解析受治理的知识包，校验外部边界描述符，加载并验证受治理的 Athena 仓库根契约，推导显式的 `Layout IR`、`Geometry IR` 与渲染器中立的 `projection-model` 文档，发布第一批 M9 `DerivedEngineeringContext`，提升第一批 M9 `EngineeringCapabilityFacts`，评估第一条固定 M9 constraint slice，计算第一批跨编译规范状态的 M9 impact consequences，并驱动第一条基于几何结果的下游后端路径。

## 职责

- 公开 `AthenaCompiler` 的 `parse`、`lower`、`compile` 入口。
- 保持声明式 pass 顺序稳定：`PARSE -> LOWER -> SEMANTIC_ENRICHMENT -> VALIDATE -> BACKEND_PREPARATION -> BACKEND_EMISSION`。
- 将语法层源码 lowering 为规范的 `Engineering IR`。
- 运行通用语义校验与领域插件校验。
- 从规范 `Engineering IR` 与类型化 `ViewDefinition` 贡献推导受支持的 `Layout IR`。
- 从显式 `Layout IR` 推导受支持的 `Geometry IR`。
- 从 `Geometry IR` 与 layout-owned `ViewDefinition` 推导受支持的 `ProjectionDocument`。
- 从规范 `Engineering IR` 派生第一批受治理的 `DerivedEngineeringContext` 快照。
- 通过固定评审过的 knowledge pack 提升第一批受治理的 `EngineeringCapabilityFacts`。
- 评估第一条受治理的工程充分性 rule slice，并输出 typed、renderer-independent diagnostics。
- 从 before/after 编译后的规范状态计算确定性的 engineering impact consequences。
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
- `DerivedEngineeringContextDeriver`：从规范 `Engineering IR` 到第一批 M9 派生上下文的窄范围编译器推导器。
- `EngineeringCapabilityFactPromoter`：通过固定 knowledge pack 将派生上下文提升为第一批 M9 capability facts 的编译器提升器。
- `EngineeringConstraintEvaluator`：通过固定 knowledge pack 将 capability facts 评估为第一批 M9 工程充分性结果与 diagnostics 的编译器评估器。
- `EngineeringImpactConsequenceCalculator`：在受治理 inputs、derived context、capability facts 与 constraint results 之上执行确定性的 before/after impact 评估。
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

## 派生上下文边界

Story `1.2` 引入了第一条窄范围 M9 派生证明：

- 编译器通过 `CompilerCompilationSuccess.derivedContext` 发布 Athena 自有的 kernel/compiler 输出。
- 编译器通过 `CompilerCompilationSuccess.capabilityFacts` 发布 Athena 自有的 kernel/compiler 输出。
- 编译器还会通过 `CompilerCompilationSuccess.constraintEvaluations` 发布 accepted/warning/error 约束结果，并在 validation breakdown 里单独发布 `engineeringSufficiencyDiagnostics`。
- 编译器通过 `AthenaCompiler.calculateImpactConsequences(before, after)` 暴露类型化 impact 计算，供后续 runtime 与 review story 复用，而不是重新拼接字符串。
- 第一条 proof slice 故意保持很窄，而且只覆盖 electrical：可以从现有 authored `Symbol` 或 `Text` 值中规范化 motor 的 `power`、`voltage`、`powerFactor`/`pf`、`efficiency`。
- 当前 proof 会先推导确定性的中间值，例如 motor full-load current 与 thermal load，再提升固定 current-demand facts，最后对 authored breaker/cable/relay current 输入执行固定充分性检查。
- 第一条 impact proof 会比较受治理的 before/after 状态，并发布 affected semantic identities，以及 changed input、derived context、capability fact 与 constraint evaluation 的短分类原因标签。
- 固定 knowledge pack 随后会把这些结果提升为 required protection current、cable current demand 与 relay sizing demand 等 capability facts。
- 工程充分性与结构校验保持分离：accepted 结果保存在 `constraintEvaluations`，warning/error 结果另外映射为 `KNOWLEDGE` diagnostics，供后续语义交付使用。
- 这一层仍然位于 SCM review、LSP delivery 与 renderer metadata 之下。
- 本 story 不扩大 parser grammar；数量值解析仍然是 compiler 自己拥有的窄规范化步骤。

## 依赖

- `:kernel:language`
- `:kernel:plugins:plugin-api`
- `:kernel:plugins:plugin-host`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`
- `:kernel:projection-model`
- `:kernel:repository-model`
- `:kernel:svg-renderer`

仅测试依赖：

- `:extensions:domain-electrical`

## 边界

该模块不拥有 DSL 语法本身，不拥有规范 IR 结构，不拥有公共插件 SPI，不拥有宿主插件 source 或 approval 治理，也不拥有具体的 Electrical/Runtime 领域规则。它负责编排这些部分，同时保持架构规则不变：DSL 是作者输入源，`Engineering IR` 是规范模型，`Layout IR` 是第一层显式下游投影，`Geometry IR` 是几何层下游结果，`:kernel:projection-model` 是位于 runtime、LSP 与图适配器之上的渲染器中立图形投影边界，而 renderer 仍然只是下游后端，不是语义捷径。

## 验证

```bash
./gradlew :kernel:compiler:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:compiler:test
```
