---
baseline_commit: c007cd140cd0fcaffdc5ccb66bf2f917d69294cf
---

# Story 2.1: Give Every Engineering Connection Stable Authored Identity

Status: in-progress

## Story

As an engineer or AI author,
I want every connection to have a stable source alias,
so that physical installation intent and future editing can reference one connection without endpoint or group guesses.

## Acceptance Criteria

1. Given grouped and ungrouped connection source, when M35 grammar and AST compile it, then every connection requires a source-unit-unique alias and `EngineeringConnectionId` is `(SourceUnitId, connectionAlias)`, and group names remain organizational and never identify or merge connections.
2. Given alias-free current grammar, AST, endpoint-plus-ordinal identity, examples, and fixtures, when the migration story completes, then all repository sources/tests are migrated to required aliases and the old forms/identity are deleted, and there is no compatibility parser or adapter.
3. Given an alias reference in governed source, when semantic linking runs, then it resolves exactly one connection alias in the same `SourceUnitId`, and missing, duplicate, group-name, or cross-source references fail with stable source-spanned diagnostics.
4. Given aliased syntax in normal, invalid, and incomplete files, when ANTLR4, semantic lowering, formatter, LSP, Tree-sitter, highlighting, and parser parity run, then every frontend and generated artifact reflects the same required alias contract.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and alias-free source, parser branches, generated grammar artifacts, fixtures, ids, and compatibility names are purged.

## Tasks / Subtasks

- [ ] Add RED tests for required connection aliases and stable identity (AC: 1, 2, 3, 4)
  - [ ] Cover grouped and ungrouped valid connections with explicit aliases.
  - [ ] Cover alias-free syntax rejection, duplicate aliases, group-name misuse, missing references, and cross-source references.
  - [ ] Cover `EngineeringConnectionId` construction from `SourceUnitId + alias`, not endpoints or ordinals.
  - [ ] Cover ANTLR4, formatter, LSP, Tree-sitter/highlighting, and parser parity surfaces.
- [ ] Implement required alias syntax and source model updates (AC: 1, 2, 4)
  - [ ] Update ANTLR4 grammar and language models so every connection carries a required alias token and source span.
  - [ ] Remove alias-free parser branches and compatibility constructors.
  - [ ] Update formatter and language feature surfaces to keep aliased connection syntax canonical.
- [ ] Update semantic lowering and diagnostics (AC: 1, 3)
  - [ ] Lower each connection to a source-unit-scoped `EngineeringConnectionId`.
  - [ ] Make group declarations organizational only; never use group names as connection ids.
  - [ ] Emit stable source-spanned diagnostics for duplicate/missing/cross-source aliases.
- [ ] Migrate repository sources, examples, fixtures, and generated artifacts (AC: 2, 4)
  - [ ] Update all active `.athena` source and tests to required aliases.
  - [ ] Update Tree-sitter grammar/corpus/highlighting and parser parity corpus together.
  - [ ] Delete or rewrite alias-free fixtures instead of preserving compatibility.
- [ ] Polish/purge and evidence gate (AC: 5)
  - [ ] Audit touched and adjacent paths for alias-free grammar, endpoint-plus-ordinal ids, stale examples, generated artifacts, and compatibility names.
  - [ ] Run sequential verification and record RED/GREEN evidence.
  - [ ] Record AC-to-evidence mapping and three-layer review.

## Dev Notes

### Scope Boundary

Story 2.1 owns stable authored identity for engineering connections. It does not introduce Cabinet installation syntax, physical routing geometry, route channels, selection trace, or graphical editing. Later Epic 2/3 stories may reference aliases, but this story only makes connection identity stable, source-scoped, and universally parsed/lowered.

### Architecture Requirements

- Athena source remains the only authority for connection identity.
- Every connection must have a source-unit-unique alias. Alias-free connection syntax is removed, not preserved.
- `EngineeringConnectionId` must be derived from `SourceUnitId` and the connection alias, never from endpoints, source order, group names, or generated ordinals.
- Connection group names are organizational only. They may not merge, identify, or substitute for individual connection aliases.
- Alias references resolve only inside the same `SourceUnitId` unless a later story explicitly adds a typed cross-source import/reference model.
- ANTLR4 remains parser authority. Tree-sitter, formatter, LSP, highlighting, examples, and parser parity must move together.
- Because Athena is unreleased, stale grammar branches, examples, and compatibility adapters should be deleted instead of wrapped.

### Previous Story Intelligence

- Epic 1 established package/resource/lock/SVG authority. Do not disturb package-local resource semantics while migrating `.athena` sources.
- Story 1.5 created `examples/m35/package-platform-proof` as a package proof only; it has no connections and should not be forced into physical routing work.
- Existing examples were already migrated toward package-path hierarchy in earlier M35 work; preserve that discipline when editing source fixtures.

### Likely Code Areas

- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`
- `kernel/connection-model/src/main/kotlin/...` if connection value objects are separated there.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/test/corpus/*`
- active `examples/**/src/**/*.athena` connection fixtures and parser parity corpus.

### Testing Requirements

- Follow RED/GREEN. Write failing tests before production code.
- Run Gradle sequentially on Windows. Never run Gradle tasks in parallel.
- Minimum expected verification for this story:
  - targeted language/parser tests;
  - targeted compiler semantic lowering tests;
  - targeted LSP diagnostics/completion/token tests;
  - targeted Tree-sitter/parser parity tests if JS tests are present;
  - `encoding-audit.ps1` after text edits;
  - `git diff --check`.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 2, Story 2.1.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-28, FR-35, FR-41..FR-42.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-12, AD-13, AD-14, AD-17.
- `_bmad-output/implementation-artifacts/m35/1-5-prove-portable-standard-and-vendor-packages-end-to-end.md` - previous story.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

### Completion Notes List

### File List
