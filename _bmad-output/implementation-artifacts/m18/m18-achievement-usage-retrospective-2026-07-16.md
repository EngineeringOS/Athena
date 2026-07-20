# M18 Achievement, Usage, And Retrospective

Date: 2026-07-16
Milestone: Athena M18 - Project Semantic Graph And Package Resolution

## Closeout Status

M18 is complete from a story perspective. The local sprint status shows all 21 stories done across
four epics:

- Epic 1: Package-Aware Authored Syntax
- Epic 2: Governed Project Semantic Workspace
- Epic 3: Cross-Package Linking And Capability Semantics
- Epic 4: Package-Aware IDE Experience And Closeout Evidence

The milestone result is that Athena now has a compiler-owned package-aware semantic workspace path:
authored `package` and `import` syntax flows through ANTLR-authored AST contracts, governed
repository state, a deterministic project semantic graph, cross-source/package linking, canonical
Engineering IR lowering, and compiler/LSP-backed IDE projections.

## Achievements

### Package-Aware Syntax

- Added optional file-level `package` declarations with dotted, digit-bearing, and internally
  hyphenated name segments.
- Added ordered file-level `import` declarations before the authored `system` block.
- Preserved Athena-owned authored AST contracts without exposing generated ANTLR types or repository
  model types through the language boundary.
- Kept the syntax slice narrow: alias, wildcard, visibility, export, and unrelated broad language
  forms remain rejected or deferred.
- Mirrored package/import syntax in Tree-sitter for syntax UX only, including Theia semantic-token
  highlighting and recovery behavior.

### Project Semantic Graph

- Added canonical package, source unit, namespace, declaration, binding, content, and graph identity
  builders.
- Added an immutable compiler-owned project semantic graph snapshot with ordered packages, source
  units, namespaces, declarations, bindings, and diagnostics.
- Built semantic graph snapshots from governed repository publication state instead of raw
  filesystem, JVM classpath, or frontend heuristics.
- Resolved imports against the semantic graph with deterministic package ownership, namespace
  availability, and explanation payloads.
- Added stable typed semantic diagnostics for unavailable packages, unavailable namespaces,
  ambiguous namespaces, invalid graph state, duplicate declarations, unresolved references, and
  ambiguous references.

### Linking, Capability, And Lowering

- Indexed authored device and port declarations into semantic namespaces.
- Linked references across source units in the same governed package.
- Linked references across governed package boundaries only when an authored resolved import exposes
  the dependency namespace.
- Preserved governed package capability markers on semantic namespaces and exposed capability
  provenance through compiler diagnostics/explanation.
- Lowered linked project semantic meaning through the canonical Engineering IR path without AST
  paste, source include, or hidden expansion.

### IDE And Canvas Boundary

- Published compiler-owned package-aware diagnostics through normal LSP diagnostic surfaces.
- Added snapshot-first package-aware go-to-definition and references with fallback to existing
  document-local navigation only when no package-aware snapshot is available.
- Added package-aware document-symbol behavior through a package root while keeping workspace
  symbols deferred.
- Kept the frontend logic Theia/LSP-centered. No frontend-owned package, import, or symbol
  resolution was added.
- Connected package-aware navigation to the existing Theia workbench reveal model by preserving
  canonical engineering subject ids. This stayed inside the IDE frontend model and tests; it did not
  involve `apps/desktop-viewer`, Kotlin Compose, GLSP, or a new renderer path.

### Proof Corpus And Boundary Documentation

- Added `examples/m18/syntax-proof/` for valid and invalid package/import syntax.
- Added `examples/m18/linking-lowering-proof/` for single-package, cross-source, cross-package,
  unresolved symbol, and invalid availability behavior.
- Added `examples/m18/repository-proof/` for repository-backed valid workspace, local governed
  vendor package availability, invalid import, unresolved symbol, and graph-invalid publication
  behavior.
- Added closeout boundary documentation in `m18-closeout-boundaries.md`.
- Added deferred work tracking in `deferred-work.md`.
- Added a repeatable scope-boundary audit for absolute path discipline and excluded-scope language.

## Usage Summary

Use M18 by working through the governed repository and compiler/LSP path:

1. Put authored source under a governed repository with `athena.yaml` and `athena.lock`.
2. Declare the source unit package with `package <qualified-name>`.
3. Import governed package or symbol meaning with `import <qualified-name>`.
4. Let the compiler build the project semantic graph from governed package state and authored source
   units.
5. Use the existing Theia/LSP IDE surfaces for Problems, go-to-definition, references, and outline.
6. Use Tree-sitter only for syntax UX. It can highlight and recover package/import syntax, but it is
   not evidence that imports resolved semantically.
7. Use the proof corpus under `examples/m18/` as the reference fixture set.

Primary usage guide: `../../../docs/usages/m18-proof-usage.md`

Important boundaries:

- Compiler/LSP remains the package, import, diagnostic, symbol, and navigation authority.
- Engineering IR remains canonical after linked lowering.
- Theia frontend may render and reveal compiler/LSP results, but it must not resolve package-aware
  meaning itself.
- Desktop viewer, Kotlin Compose, remote registry, marketplace, publish, multi-root, broad
  visibility/export, and package ecosystem behavior are excluded from M18.

## Verification Evidence

Story records show focused red/green checks first, then broader sequential verification. The
repeated verification pattern was:

- `.\gradlew.bat --no-daemon --console=plain :kernel:language:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
- `yarn --cwd ide/tree-sitter-athena test` where syntax UX changed
- `yarn --cwd ide/theia-frontend test` where Theia frontend behavior changed
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- `powershell -ExecutionPolicy Bypass -File .\tools\m18-scope-boundary-audit.ps1` for closeout
  boundary verification

The final closeout stories recorded passing compiler, LSP, Theia frontend, scope-boundary, and
encoding checks. Tree-sitter verification was intentionally not rerun in Story 4.5 because that
story did not change Tree-sitter files or syntax-proof fixtures.

## What Worked

- The implementation order was right: syntax first, semantic graph second, linking/lowering third,
  IDE/proof closeout last. Each stage consumed the previous stage instead of inventing parallel
  state.
- The compiler-owned snapshot became the central integration contract. Diagnostics, navigation,
  symbols, lowering, and tests could all refer to the same project semantic graph shape.
- The red/green story logs were useful. Several real defects were caught before broad verification:
  missing import targets consuming later tokens, drive-relative source paths, dangling graph edges,
  repeated diagnostic projection, cross-source ambiguity, current-source-unit identity in LSP
  snapshots, inactive-sheet reveal false positives, and unsaved sibling-buffer navigation gaps.
- The IDE scope correction held. Package-aware behavior stayed in Theia/LSP frontend logic and did
  not drift into desktop-viewer or Kotlin Compose work.
- Executable proof fixtures made closeout stronger than prose. Syntax, linking/lowering, LSP, and
  repository-backed behavior all have local fixture evidence.

## What Needed Tightening

- Package-aware LSP state was the highest-risk area. It needed multiple follow-up fixes for dirty
  buffer overlay, package/path identity, sibling source-unit URI mapping, and unsaved sibling buffer
  participation.
- Several early implementation details had to be hardened by review: exact syntax diagnostics,
  incomplete header recovery, deterministic ordering, duplicate handling, and idempotent diagnostic
  projection.
- The boundary between syntax UX and semantic authority required constant enforcement. Tree-sitter
  and frontend code can make package/import authoring feel native, but cannot become package
  resolution authority.
- The milestone needed explicit boundary docs because several tempting directions were out of
  scope: registry, marketplace, publish, broad visibility/export, multi-root, desktop-viewer, and
  Kotlin Compose.

## Lessons Learned

- Package-aware language growth should start from the semantic graph contract, not from IDE behavior
  or parser convenience.
- Snapshot identity matters as much as diagnostics. LSP navigation is only reliable when current
  source unit, source roots, graph id, declarations, bindings, and URI mappings stay together.
- Dirty buffers are part of IDE reality. Package-aware navigation cannot rely only on saved files
  once sibling source units can affect the current semantic snapshot.
- Proof fixtures should accumulate throughout the milestone. Waiting until closeout would have made
  the final corpus less trustworthy and harder to debug.
- Scope audits are worthwhile for milestones with many attractive non-goals. They convert boundary
  discipline into a repeatable check instead of a memory task.

## Deferred Work

Deferred work is tracked in `deferred-work.md`. The key deferred items are:

- Remote registry, cloud package resolution, marketplace behavior, and package publish flows.
- Full export/visibility semantics and broad authored-language redesign.
- Multi-root sessions and package-local manifest redesign.
- Frontend-owned semantic resolution.
- Desktop-viewer, Kotlin Compose UI work, and alternate canvas/rendering authority.
- Tree-sitter semantic diagnostics, package resolution, and symbol linking.
- A pre-existing M15 component-knowledge test completeness improvement: strengthen active
  implementation inventory assertions by identity rather than count alone.

## Next Milestone Preparation

- Build future package-aware language work on the project semantic graph foundation instead of
  adding frontend-local or filesystem-local resolution.
- Treat workspace symbols as a later explicit promotion unless the next milestone accepts that UX
  and performance surface.
- If richer package behavior is added, extend diagnostics, proof fixtures, and scope-boundary docs
  at the same time.
- Keep Windows Gradle verification sequential.
- Keep M18 docs and proof references relative.
