# `ide/lsp`

[English](README.md) | 简体中文

`ide/lsp` 是 Athena 在 IDE 路径上的语义服务宿主。

## 职责

- IDE 路径上的 repository-session authority
- 嵌入现有 JVM runtime stack 的 stdio Athena LSP server
- 在 `initialize` 阶段于 LSP 边界内完成仓库激活
- 面向 `.athena` authored source 的 `textDocument/didOpen` 语义路径
- 来自 Athena 自有解析、语义分析与校验的 `textDocument/publishDiagnostics`
- 通过同一条面向 Problems 的 `textDocument/publishDiagnostics` 路径发布增量 M9 knowledge diagnostics
- 由 Athena 自有文档状态驱动的 `textDocument/completion`、`textDocument/documentSymbol`、`textDocument/definition` 与 `textDocument/references`
- 具备版本感知的 tracked document state，在重复编辑时拒绝陈旧回滚
- 在 semantic inspection 中增量暴露 derived context、capability facts、constraint evaluations 与 engineering sufficiency diagnostics 的当前计数
- 通过现有 runtime-backed source-mutation request 增量传输 typed engineering impact consequences
- 通过现有 semantic review 与 semantic SCM transport 增量传输 typed engineering-impact consequence 列表，以及显式的 `engineering-impact` review / commit entry
- 面向 baseline-driven review、commit-preparation 与 package-history state 的增量语义 SCM 请求表面
- 面向 AI reasoning 的增量请求表面：前转 typed reasoning-session request，并返回 DTO-only 的 proposal / session payload
- 面向 runtime-owned graphical state inspection 的增量 projection-session 请求表面
- 面向 inspect-first graphical interaction 的显式 governed projection-command allowlist，目前仅包含 active-view switching
- 增量传输 M11 的 projection family id、sheet state、notation pack 与 cross-reference payload
- 后续故事中的 hover、rename 与更丰富的 workspace navigation

## 边界

Story `2.4` 把这个包从 authoring transport 扩展为第一条 semantic SCM projection bridge。Story `3.3` 再沿用同一条增量桥接路径，把 package evolution 与 release relevance 也投影到现有产品边界。M7 Story `1.4` 在此基础上增加了面向 runtime-owned projection session 的 typed query，以及一个受治理的 projection-command seam。

Theia 可以负责进程生命周期与传输，但语义或 projection 访问必须继续经由这里的 LSP 方法流动，而不是直接调用 `kernel/*`。

M9 保持交付规则狭窄：

- knowledge diagnostics 继续走现有 diagnostics path，而不是新开一条 renderer-only 或 workbench-only warning channel
- semantic inspection 仍然是 JVM-owned 的只读快照，只是增量暴露当前 knowledge-runtime 计数
- before/after engineering impact 继续走现有 source-mutation request 表面，而不是引入第二套 knowledge transport
- semantic SCM 与 accepted-mutation review 现在也会投影同一套 typed engineering-impact consequence，让 direct edit 与 downstream affected subject 在 LSP 边界保持可区分
- AI reasoning request 在这里仍然只是 transport：`athena/aiReasoning` 可以引用 semantic SCM 的 baseline 选择，但 deterministic evidence assembly、review-summary resolution、provider-neutral session orchestration 与 typed proposal recording 仍然保持 JVM-owned

当前 M7 projection boundary 故意保持狭窄：

- `athena/projectionSession` 返回 runtime-owned supported views、active view state、可检查的 ready 或 unavailable projection payload，以及已发布的 command allowlist
- ready projection payload 现在也可以携带 projection family id、governed sheet metadata、notation-pack mapping 与 repeated-reference cross-reference data，这些都来自 JVM-owned runtime state
- unavailable projection payload 会保留底层 runtime diagnostic；当上游失败暴露 stable code 与 provenance 时，这些细节也会一并透出
- `athena/projectionCommand` 只接受 Athena allowlist 中的 projection action，而不是暴露通用 runtime tunnel
- hosted plugin commands、graph-framework commands 与任意 frontend-local actions 都不是这里的公开传输契约

## M17 Parser 迁移边界

M17 加固了 `ide/lsp` 之下的语言架构，使得 Epic 2 的 ANTLR4 编译器路径与 Epic 3 的 Tree-sitter 语法 UX 都无法悄悄改变 IDE 语义。以下两条不变量由代码 KDoc 与回归测试（`AthenaSemanticAuthorityBoundaryTest`、`AthenaSourceNavigationParityTest`）锁定。

### 语义诊断路径（Story 4.1，AD-108 / AD-107）

诊断必须始终只经由编译器 parser 路径产生，绝不来自 Tree-sitter 语法树或查询结果。不可回归的链路为：

- `AthenaTextDocumentService.didOpen`/`didChange` → `AthenaLanguageServer.publishDiagnostics`
- `publishDiagnostics` → `AthenaLanguageFeatures.trackDocument(uri, path, version, text)`（唯一产生文档已编译状态的位置）
- `trackDocument` → `AthenaCompiler.compile(path, text)` → `CompilerCompilationResult`，保存在 `AthenaTrackedDocument.compilation`
- `CompilerCompilationResult.toLspDiagnostics()` 将 `CompilerSyntaxDiagnostic`（`CompilerCompilationParseFailure.diagnostics`）与 `SemanticDiagnostic`（`CompilerCompilationSuccess.semanticResult.diagnostics` 以及 `validationBreakdown.engineeringSufficiencyDiagnostics`）转换为 LSP `Diagnostic`
- `languageClient.publishDiagnostics(...)`

`AthenaLanguageFeatures.semanticInspection(uri)` 同样只从 `CompilerCompilationParseFailure`/`CompilerCompilationSuccess` 字段以及 `AthenaNavigationIndex` 的 source range 构建。绝不允许出现 `TreeSitterDiagnostic`/`TreeSitterSemanticResult` 之类的类型；诊断始终只来源于 `com.engineeringood.athena.compiler` 与 `com.engineeringood.athena.semantics.core`。

### 依赖 AST 的工具清单（Story 4.2，AD-109 / AD-106）

以下工具仅读取 authored `SourceFileAst`（`DeviceDeclaration`、`PortDeclaration`、`ConnectionDeclaration`、`QualifiedName`）及其 `SourceSpan`/`SourcePosition`。当底层 parser 实现更换时，它们必须保持不变；Tree-sitter 也绝不能成为其中任何一项的替代实现：

| 工具 | 文件 | AST 依赖 |
| --- | --- | --- |
| `documentSymbols`、`definition`、`references` | `AthenaLanguageFeatures.kt` | `SourceFileAst.declarations`、`SourceSpan` |
| `AthenaNavigationIndex`（`deviceDeclarations`、`portDeclarations`、`ownerReferences`、`portReferences`、`targetAt`） | `AthenaLanguageFeatures.kt` | `SourceFileAst.declarations`、`SourceSpan` |
| `componentSourceRange`、`portSourceRange`、`connectionSourceRange` | `AthenaLanguageFeatures.kt` | `Declaration.span` / `QualifiedName.span`（经 `SourceSpan.toLspRange()` 1:1 转换） |
| `BackendAuthoringSourceEditPlanner`、`acceptedUpdateSemanticEntityPropertiesSourceEdit` | 编译器规划器与 `Athena*SourceEditProtocol.kt` 传输层 | 基于精确修订文本的 `SourceFileAst.system.span`、`DeviceDeclaration.span`、`PortDeclaration.qualifiedName.span`、`ConnectionDeclaration.from`/`to` span |
| `revealSemanticId`（前端消费方） | `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts` | 仅使用 server 端的 `definition`/`references` 结果；不在本地重新解析 Athena 源码 |

Source-edit 锚定由 `AthenaAuthoringRequestTest` 覆盖；navigation/symbol/source-range parity 由 `AthenaSourceNavigationParityTest` 覆盖。
