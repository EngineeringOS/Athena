# Story 4.3: Preserve Compiler Output Continuity On Supported Source

Status: done

## Story

As a compiler engineer,
I want compiler outputs on supported source to remain semantically continuous across parser migration,
so that M17 proves architecture hardening rather than accidental semantic drift.

## FR Traceability

- FR-4: Athena can preserve lowering through authored AST instead of parse trees.
- FR-7: Athena can keep compiler and LSP semantics on the compiler parser path.
- NFR-1: `Engineering IR` remains canonical engineering truth.
- NFR-2: Generated parser-tree types do not become public semantic contracts.

## Acceptance Criteria

1. Given one supported Athena source input, when it is compiled through the M17 parser path (the `ANTLR4`-backed compiler path once Epic 2 lands), then `EngineeringIrLowerer.lower(CompilerSourceDocument)` continues to produce the same canonical `EngineeringDocument` shape (same `EngineeringSystem`, `EngineeringComponent`, `EngineeringPort`, `EngineeringConnection` identities and provenance) as it does today for the current supported `system`/`device`/`port`/`connect`/qualified-name/string/property subset.
2. Given parser parity is reviewed, when the current valid proof inputs (`examples/m0/demo-cabinet.athena`, `examples/m0/dual-drive-cabinet.athena`, and other checked-in valid `examples/m0`/`m2`/`m3`/`m9` fixtures) are compiled before and after the parser implementation changes, then `AthenaCompilerTest`'s existing conformance assertions (`matches the published engineering ir conformance artifact`, `matches the published svg conformance artifact`, `lowering is deterministic for identical input`) continue to pass byte-for-byte unchanged against `examples/m0/demo-cabinet.engineering-ir.txt` and the sibling `.expectation.txt`/`.svg` fixtures.
3. Given the compiler pipeline is inspected, when `AthenaCompiler.compile(path, sourceText)` runs for supported source, then `CompilerPipelineReport.passes` still reports the same six named passes (`PARSE_PASS`, `LOWER_PASS`, `SEMANTIC_ENRICHMENT_PASS`, `VALIDATE_PASS`, `BACKEND_PREPARATION_PASS`, `BACKEND_EMISSION_PASS`) in the same order with the same `CompilerPassExecutionStatus`, so downstream consumers of `CompilerCompilationSuccess.pipeline` observe no structural change.
4. Given `EngineeringIrLowerer` is reviewed for its dependency surface, when its imports are checked, then it continues to depend only on `CompilerSourceDocument.ast` (a `SourceFileAst`) and never on any `ANTLR4`-generated parse-tree/visitor type or Tree-sitter CST type, satisfying AD-106 across the parser migration.

## Tasks / Subtasks

- [x] Establish the explicit pre-migration parity baseline. (AC: 1, 2, 3)
  - [x] Confirm and, if needed, extend `AthenaCompilerTest`'s existing conformance tests (`matches the published engineering ir conformance artifact`, `matches the published svg conformance artifact`, `lowers the demo cabinet example into canonical engineering ir`, `lowering is deterministic for identical input`) so they cover every currently checked-in valid `examples/m0` fixture, not only `demo-cabinet.athena`.
  - [x] Record the current `CompilerPipelineReport.passes` shape (six named passes, in order, from `CompilerModels.kt`'s `PARSE_PASS`/`LOWER_PASS`/`SEMANTIC_ENRICHMENT_PASS`/`VALIDATE_PASS`/`BACKEND_PREPARATION_PASS`/`BACKEND_EMISSION_PASS` descriptors) as the explicit continuity baseline in Dev Notes and in `kernel/compiler/README.md`.
  - [x] Add, if missing, an explicit test that re-parses and re-lowers each valid `examples/m0` fixture twice and asserts the resulting `EngineeringDocument` values are structurally equal (extending the existing `lowering is deterministic for identical input` pattern to the full valid corpus).
- [x] Confirm and document `EngineeringIrLowerer`'s authored-AST-only dependency. (AC: 4)
  - [x] Re-read `EngineeringIrLowerer.lower(source: CompilerSourceDocument)` (`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`) and confirm it only reads `source.ast.system.name`, `source.ast.system.span`, and (through `AthenaDomainSemanticsCoordinator.lower(source)`) `SourceFileAst.declarations`, never a generated parse-tree or CST type.
  - [x] Add KDoc to `EngineeringIrLowerer.lower` stating explicitly that it must never be changed to accept or read an `ANTLR4` parse-tree/visitor result or a Tree-sitter CST node directly; its only legal input is the authored `SourceFileAst` via `CompilerSourceDocument`.
  - [x] Confirm `AthenaDomainSemanticsCoordinator.lower(source)` (consumed by `EngineeringIrLowerer`) and the domain plugin lowering contribution path (`AthenaDomainLoweringContribution`) also only read `SourceFileAst`-derived data, not parser-generator internals.
- [x] Add a parser-parity smoke check anchored to this story rather than only to Story `5.1`'s corpus. (AC: 1, 2)
  - [x] Add a focused `kernel/compiler` test that compiles every valid `examples/m0` fixture and asserts `CompilerCompilationSuccess.document` (the `EngineeringDocument`) has the expected component/port/connection counts and identity naming scheme (`system:<name>`, `component:<name>`, `port:<owner>.<port>`, `connection:<from>-><to>`) as already implemented in `EngineeringIrLowerer`.
  - [x] Cross-reference this new test's expectations with the Story `5.1` proof corpus once it exists, so both stories reinforce the same parity guarantee instead of diverging.
- [x] Keep Story `4.3` narrow. (AC: 1, 2, 3, 4)
  - [x] Do not implement the `ANTLR4` grammar or parse-tree-to-AST adaptation itself (Epic 2, Story `2.1`/`2.2`). This story defines and pins the continuity contract that Epic 2's implementation must satisfy.
  - [x] Do not change `EngineeringIrLowerer`'s current lowering behavior, identity scheme, or provenance mapping. This story hardens and tests the existing behavior; it does not redesign lowering.
  - [x] Do not widen scope into unsupported syntax (no `import`, no macro-use forms); continuity is scoped to the current supported subset only, per AD-110.
- [x] Run focused and regression verification sequentially on Windows with Java 25. (AC: 1, 2, 3, 4)

## Dev Notes

### Story Intent

- Story `4.3` is the "compiler output must not drift" story: it pins today's `EngineeringIrLowerer` output shape, pipeline pass structure, and conformance-artifact match as the explicit continuity contract that Epic 2's `ANTLR4` migration must satisfy on the currently supported syntax subset.
- The success condition is not "the compiler produces new output." The success condition is "Athena has an explicit, test-enforced baseline proving that lowering, pipeline structure, and canonical `Engineering IR` shape for supported source are identical before and after the parser implementation changes underneath."
- This story is the semantic-continuity counterpart to Story `4.1` (diagnostics authority) and Story `4.2` (navigation/editing utility): together they cover the three externally observable compiler/LSP surfaces that AD-110's parity-first proof depends on.
- Story `5.1` later publishes the checked-in parser-parity corpus that exercises this exact continuity guarantee end to end on real repository-backed sources; this story establishes the contract and the first in-module regression coverage that `5.1` builds on.

### Architecture Guardrails

- Align to AD-106: authored AST remains the only lowering input before `Engineering IR`; `Engineering IR` may not depend on generated parser nodes, parser visitors, or editor CST nodes. Story `4.3` confirms `EngineeringIrLowerer` already satisfies this and adds an explicit KDoc and test guardrail so Epic 2 cannot regress it while wiring in `ANTLR4`. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-106---Authored-AST-Remains-The-Only-Lowering-Input-Before-Engineering-IR]
- Align to AD-110: the first M17 proof stays parity-first on the current supported syntax subset (`system`, `device`, `port`, `connect`, qualified names, string literals, property assignments). Story `4.3` scopes its continuity guarantee to exactly this subset, matching the checked-in `examples/m0` fixtures that already exercise it. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-110---The-First-M17-Proof-Stays-Parity-First-On-The-Current-Supported-Syntax-Subset]
- Preserve inherited AD-39: cross-surface anchoring continues to use canonical semantic identity; Story `4.3`'s parity checks explicitly assert on `StableSemanticIdentity` naming (`system:`, `component:`, `port:`, `connection:` prefixes) produced by `EngineeringIrLowerer`. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt` already lowers `CompilerSourceDocument` (wrapping `SourceFileAst`) into `EngineeringDocument` deterministically, computing `StableSemanticIdentity` values as `system:<name>`, `component:<name>` (with `#<n>` duplicate-ordinal suffixes), `port:<owner>.<port>`, and `connection:<from>-><to>`, and converting `SourceSpan` into `SourceProvenance` via the private `SourceSpan.toProvenance(file)` extension.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`'s `compileParsedSource`/`buildCompilationSuccess` already assemble `CompilerPipelineReport` from six named `CompilerPassDescriptor` constants (`PARSE_PASS`, `LOWER_PASS`, `SEMANTIC_ENRICHMENT_PASS`, `VALIDATE_PASS`, `BACKEND_PREPARATION_PASS`, `BACKEND_EMISSION_PASS`) defined at the bottom of `CompilerModels.kt`'s sibling file (actually declared as private `val`s in `AthenaCompiler.kt` itself); this ordered pass list is the pipeline-continuity baseline.
- `examples/m0/demo-cabinet.engineering-ir.txt` already publishes the exact canonical `Engineering IR` textual expectation for `demo-cabinet.athena` (system/component/port/connection lines with `id=`, `name=`, `provenance=` fields), and `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`'s `matches the published engineering ir conformance artifact` test already asserts against it.
- `examples/m0/README.md` (via `examples/README.md`) already documents `demo-cabinet.athena`, `dual-drive-cabinet.athena`, `invalid-semantic-cabinet.athena`, `invalid-direction-cabinet.athena`, `duplicate-identity-cabinet.athena`, and `quoted-properties-cabinet.athena` as the current valid/invalid M0 fixture set; every `m0/*.athena` file already has a matching `.expectation.txt` sidecar per the existing conformance-suite convention.
- There is no `ANTLR4` grammar or generated parser anywhere in the repository yet. `EngineeringIrLowerer` and `AthenaCompiler` already only import from `com.engineeringood.athena.language` (the syntax contract package), never anything parser-generator-specific.

### Technical Requirements

- Do not change `EngineeringIrLowerer`'s identity-naming scheme, provenance mapping, or duplicate-ordinal tagging logic. This story adds tests and documentation around existing behavior; it does not redesign lowering.
- Do not change the six-pass `CompilerPipelineReport` structure, pass ordering, or `CompilerPassExecutionStatus` semantics.
- Keep new tests inside `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/`, following the existing `kotlin.test` style already used throughout `AthenaCompilerTest.kt`.
- Do not add a third-party dependency to `kernel/compiler/build.gradle.kts` for this story.

### Architecture Compliance

- The story is only successful if Epic 2's `ANTLR4` migration can be verified against this story's exact assertions (same `EngineeringDocument` shape, same six-pass pipeline, same conformance-artifact match) without those assertions needing to change, proving parser-implementation-neutral compiler output.
- Prevent these failure modes:
  - `EngineeringIrLowerer` growing a second lowering path that reads a generated parse tree "temporarily" during the Epic 2 migration.
  - Conformance tests being weakened (e.g. loosened equality, skipped fixtures) to make a parser migration "pass" instead of catching a real regression.
  - Silently changing pipeline pass names, order, or count as an unrelated cleanup bundled into this story.

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the workspace:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Do not add `ANTLR4` or any parser-generator dependency in this story.
- Reuse the existing `kotlin.test` style already present in `kernel/compiler/src/test/kotlin`.

### File Structure Requirements

- Expected update files likely include:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt` (KDoc hardening only)
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt` (extended parity/continuity assertions across the full valid `examples/m0` corpus)
  - `kernel/compiler/README.md`
  - `kernel/compiler/README.zh-CN.md`
- Do not add new `examples/` fixtures in this story; publishing the dedicated parser-parity corpus is Story `5.1`'s responsibility. This story reuses the already-checked-in `examples/m0` valid fixtures.

### Testing Requirements

- Minimum verification should target the compiler module directly:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Recommended regression given `:kernel:compiler` depends on `:kernel:language` and `:kernel:engineering-model`:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:language:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test"`
- Keep Gradle verification strictly sequential on Windows. Do not run these commands concurrently; wait for each to finish before starting the next.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` if any documentation files change.

### Explicit Non-Goals

- No `ANTLR4` grammar, dependency, or generated parser code in this story (Epic 2).
- No new `examples/` fixtures (Story `5.1`).
- No change to `EngineeringIrLowerer`'s lowering behavior, identity scheme, or provenance mapping.
- No change to the compiler pipeline's pass structure, ordering, or naming.
- No widening into unsupported syntax such as `import` or macro-use forms.

### Previous Milestone Intelligence

- M0 established the `Engineering IR` conformance-artifact pattern (`.engineering-ir.txt` sidecars compared byte-for-byte in `AthenaCompilerTest`); M17 reuses this exact pattern as its parser-migration continuity proof instead of inventing a new comparison mechanism.
- M3, M9, and M16 each proved that additive, plugin- or knowledge-driven growth can happen above canonical `Engineering IR` without changing lowering's fundamental identity/provenance scheme; M17's parser migration must hold the same "lowering shape stays stable" property even though the change this time is underneath the AST (parser implementation) rather than above it (new semantics).

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt]
- [Source: kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt]
- [Source: examples/m0/demo-cabinet.engineering-ir.txt]
- [Source: examples/README.md]

## Dev Agent Record

### Agent Model Used

Sonnet 5 (Cursor subagent)

### Debug Log References

- None. `EngineeringIrLowerer` already read only `CompilerSourceDocument.ast`/`SourceFileAst` on inspection; no defects found.

### Completion Notes List

- Added `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaParserContinuityTest.kt`, extending pre-migration parity coverage across the full valid `examples/m0` corpus (`demo-cabinet`, `dual-drive-cabinet`) plus the `examples/m17` parity corpus: identity-scheme assertions (`system:`/`component:`/`port:`/`connection:`), deterministic re-lowering, the six-pass `CompilerPipelineReport` shape in order, and a direct structural cross-check between `examples/m0/demo-cabinet.athena` and `examples/m17/parser-parity-proof/parity-cabinet.athena`.
- Confirmed `EngineeringIrLowerer.lower(CompilerSourceDocument)` reads only `source.ast.system.name`/`.span` and, via `AthenaDomainSemanticsCoordinator.lower(source)`, `SourceFileAst.declarations` -- never a generated parse-tree or CST type -- and added KDoc stating this must never change.
- Added `CompilerParserBoundaryTest.kt` as a source-scan guard (not reflection) proving no `kernel/compiler` main source references `org.antlr` or the internal `com.engineeringood.athena.language.antlr` package, catching violations even inside method bodies before they could reach a public signature.
- Recorded the six-named-pass continuity baseline (`PARSE -> LOWER -> SEMANTIC_ENRICHMENT -> VALIDATE -> BACKEND_PREPARATION -> BACKEND_EMISSION`) and the identity/provenance scheme in `kernel/compiler/README.md`/`README.zh-CN.md` under "M17 Parser Migration Continuity Baseline", cross-referenced by Story 5.1's corpus.
- No change to `EngineeringIrLowerer`'s lowering behavior, identity scheme, provenance mapping, or the compiler pipeline's pass structure/ordering.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt` (KDoc hardening)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaParserContinuityTest.kt` (new continuity test)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/CompilerParserBoundaryTest.kt` (new AD-106 boundary guard)
- `kernel/compiler/README.md`, `kernel/compiler/README.zh-CN.md`

## Story Completion Status

- Status: done
- Completion note: Verified by reading `AthenaParserContinuityTest.kt`, `CompilerParserBoundaryTest.kt`, the authored-AST-only KDoc on `EngineeringIrLowerer.lower`, and the "M17 Parser Migration Continuity Baseline" section of `kernel/compiler/README.md`/`README.zh-CN.md`. All four acceptance criteria are satisfied by existing, checked-in code and tests; lowering behavior, identity scheme, and pipeline structure are unchanged.
