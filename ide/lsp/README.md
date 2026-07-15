# `ide/lsp`

English | [Chinese (Simplified)](README.zh-CN.md)

`ide/lsp` is Athena's semantic-service host for the IDE path.

## Responsibility

- repository-session authority for the IDE path
- the stdio Athena LSP server that embeds the existing JVM runtime stack
- `initialize`-time repository activation inside the LSP boundary
- the Athena-authored `textDocument/didOpen` semantic path for `.athena` source
- `textDocument/publishDiagnostics` sourced from Athena-owned parsing, semantic analysis, and validation
- additive M9 knowledge diagnostics published through the same Problems-facing `textDocument/publishDiagnostics` path
- `textDocument/completion`, `textDocument/documentSymbol`, `textDocument/definition`, and `textDocument/references` backed by Athena-owned document state
- version-aware tracked document state that rejects stale rollbacks during repeated editing
- additive semantic-inspection knowledge counts for derived context, capability facts, constraint evaluations, and engineering sufficiency diagnostics
- additive source-mutation transport for typed engineering impact consequences over the existing runtime-backed mutation request
- additive semantic review and semantic SCM transport for typed engineering-impact consequence lists plus explicit `engineering-impact` review and commit entries
- additive semantic SCM request surfaces for baseline-driven review, commit-preparation, and package-history state
- additive AI reasoning request surface that forwards typed reasoning-session requests and returns DTO-only proposal/session payloads
- additive projection-session request surfaces for runtime-owned graphical state inspection
- one explicit governed projection-command allowlist for inspect-first graphical interaction, currently limited to active-view switching
- additive M11 transport for projection-family ids, sheet state, notation packs, and cross-reference payloads
- future hover, rename, and richer workspace navigation in later stories

## Boundary

Story `2.4` extended this package from authoring transport into the first semantic SCM projection bridge. Story `3.3` widens that same additive bridge to include package evolution and release relevance. M7 Story `1.4` adds typed projection-session queries plus one governed projection-command seam over the runtime-owned projection session.

Theia may manage process lifecycle and transport, but semantic or projection access must continue to flow through LSP methods here instead of direct calls into `kernel/*`.

M9 keeps the delivery rule narrow:

- knowledge diagnostics flow through the existing diagnostics path instead of creating a renderer-only or workbench-only warning channel
- semantic inspection remains a read-only JVM-owned snapshot and now exposes current knowledge-runtime counts additively
- before/after engineering impact flows through the existing source-mutation request surface instead of introducing a second knowledge transport
- semantic SCM and accepted-mutation review now project the same typed engineering-impact consequence set so direct edits and downstream affected subjects stay distinguishable at the LSP boundary
- AI reasoning requests stay transport-only here: `athena/aiReasoning` can reference semantic SCM baseline selection, but deterministic evidence assembly, review-summary resolution, provider-neutral session orchestration, and typed proposal recording all remain JVM-owned downstream

The current M7 projection boundary is intentionally narrow:

- `athena/projectionSession` returns runtime-owned supported views, active view state, inspectable ready or unavailable projection payloads, and the published command allowlist
- ready projection payloads may now include projection-family ids, governed sheet metadata, notation-pack mappings, and repeated-reference cross-reference data sourced from JVM-owned runtime state
- unavailable projection payloads preserve underlying runtime diagnostics, including stable codes and provenance when the upstream failure exposes them
- `athena/projectionCommand` accepts only Athena-allowlisted projection actions instead of exposing a generic runtime tunnel
- hosted plugin commands, graph-framework commands, and arbitrary frontend-local actions are not public transport contracts here

## M17 Parser Migration Boundary

M17 hardens the language architecture beneath `ide/lsp` so Epic 2's ANTLR4 compiler path and Epic 3's Tree-sitter syntax UX cannot silently change IDE semantics. Two invariants are locked in by code KDoc and regression tests (`AthenaSemanticAuthorityBoundaryTest`, `AthenaSourceNavigationParityTest`).

### Semantic Diagnostics Path (Story 4.1, AD-108 / AD-107)

Diagnostics must always flow only through the compiler parser path, never from a Tree-sitter tree or query. The must-not-regress chain is:

- `AthenaTextDocumentService.didOpen`/`didChange` → `AthenaLanguageServer.publishDiagnostics`
- `publishDiagnostics` → `AthenaLanguageFeatures.trackDocument(uri, path, version, text)` (the only place a document's compiled state is produced)
- `trackDocument` → `AthenaCompiler.compile(path, text)` → `CompilerCompilationResult` stored on `AthenaTrackedDocument.compilation`
- `CompilerCompilationResult.toLspDiagnostics()` converts `CompilerSyntaxDiagnostic` (`CompilerCompilationParseFailure.diagnostics`) and `SemanticDiagnostic` (`CompilerCompilationSuccess.semanticResult.diagnostics` plus `validationBreakdown.engineeringSufficiencyDiagnostics`) into LSP `Diagnostic`
- `languageClient.publishDiagnostics(...)`

`AthenaLanguageFeatures.semanticInspection(uri)` is likewise built only from `CompilerCompilationParseFailure`/`CompilerCompilationSuccess` fields plus `AthenaNavigationIndex` source ranges. No `TreeSitterDiagnostic`/`TreeSitterSemanticResult`-style type may ever appear; diagnostics stay derived exclusively from `com.engineeringood.athena.compiler` and `com.engineeringood.athena.semantics.core`.

### AST-Dependent Utility Inventory (Story 4.2, AD-109 / AD-106)

The following utilities read only the authored `SourceFileAst` (`DeviceDeclaration`, `PortDeclaration`, `ConnectionDeclaration`, `QualifiedName`) and its `SourceSpan`/`SourcePosition` values. They must keep working unchanged once the parser implementation changes underneath, and Tree-sitter must never become an alternative implementation of any of them:

| Utility | File | AST dependency |
| --- | --- | --- |
| `documentSymbols`, `definition`, `references` | `AthenaLanguageFeatures.kt` | `SourceFileAst.declarations`, `SourceSpan` |
| `AthenaNavigationIndex` (`deviceDeclarations`, `portDeclarations`, `ownerReferences`, `portReferences`, `targetAt`) | `AthenaLanguageFeatures.kt` | `SourceFileAst.declarations`, `SourceSpan` |
| `componentSourceRange`, `portSourceRange`, `connectionSourceRange` | `AthenaLanguageFeatures.kt` | `Declaration.span` / `QualifiedName.span` (1:1 via `SourceSpan.toLspRange()`) |
| `acceptedUpdateComponentPropertiesSourceEdit`, `acceptedConnectPortsSourceEdit`, `acceptedCreateComponentSourceEdit` | `Athena*SourceEditProtocol.kt` | `DeviceDeclaration.span`, `PortDeclaration.qualifiedName.span`, `ConnectionDeclaration.from`/`to` spans over raw tracked text |
| `revealSemanticId` (frontend consumer) | `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts` | server-owned `definition`/`references` results only; no local Athena re-parse |

Source-edit anchoring is covered by `AthenaAuthoringRequestTest`; navigation/symbol/source-range parity is covered by `AthenaSourceNavigationParityTest`.
