# Story 1.3: Preserve AST Extensibility For Future Syntax

Status: done

## Story

As a language architect,
I want the authored AST boundary to leave room for future constructs such as imports,
so that M17 becomes a foundation for language growth instead of a one-off migration.

## FR Traceability

- FR-1: Athena can preserve one explicit authored AST boundary.
- FR-9: Athena can define a durable landing zone for future syntax.
- NFR-5: The M17 architecture must make future syntax additions cheaper, not harder.

## Acceptance Criteria

1. Given future authored constructs are considered, when the AST and parser adaptation boundary are reviewed, then the design leaves a deliberate landing zone for future syntax (such as `import`) without forcing direct parse-tree-driven lowering; the `Declaration` sealed interface and its adaptation path must be extensible by adding new sealed variants rather than by widening existing declaration types or by lowering directly from parser output.
2. Given M17 scope is inspected, when milestone commitments are reviewed, then the architecture prepares for future syntax growth (documented landing zone, sealed-hierarchy extension point, adaptation-isolation note) and it does not claim to finalize that future syntax in M17; no `ImportDeclaration` type, import resolution logic, or package-aware authored semantics may be implemented as part of this story.
3. Given the sealed `Declaration` and `ScalarValue` hierarchies are reviewed, when a future contributor reads the code and docs, then they find an explicit, concrete example (in KDoc, README, or a landing-zone note) of how a hypothetical future `import` declaration would be added as a new `Declaration` variant without breaking existing `DeviceDeclaration`, `PortDeclaration`, or `ConnectionDeclaration` consumers, and without requiring `Engineering IR` lowering to depend on parser-tree types.
4. Given the extensibility landing zone is exercised, when a test proves the extension point works structurally, then a test-only or documentation-only sealed-hierarchy exhaustiveness check demonstrates that adding a new `Declaration` variant requires (and is caught by) an exhaustive `when` at the lowering boundary, so future syntax additions fail loudly at compile time in `:kernel:compiler` rather than being silently ignored.

## Tasks / Subtasks

- [x] Confirm the current `Declaration` and `ScalarValue` sealed hierarchies are structurally ready for extension. (AC: 1, 3)
  - [x] Re-confirm `Declaration` in `AthenaLanguageModel.kt` is a `sealed interface` (not a closed `enum` or a flat data class) with `DeviceDeclaration`, `PortDeclaration`, `ConnectionDeclaration` as its only current implementers, each carrying its own `span: SourceSpan`.
  - [x] Re-confirm `ScalarValue` is a `sealed interface` with `Identifier` and `StringLiteral` as its only current implementers.
  - [x] Confirm `SourceFileAst.declarations: List<Declaration>` already models declarations as an open, ordered list rather than fixed named fields, which is the correct shape for adding new declaration kinds later without breaking the `SourceFileAst` constructor signature.
- [x] Document the deliberate landing zone for future syntax such as `import`. (AC: 1, 2, 3)
  - [x] Add a KDoc block on the `Declaration` sealed interface in `AthenaLanguageModel.kt` explicitly naming `import` as an example of a future declaration kind that would be added as a new sealed variant (e.g. a hypothetical `ImportDeclaration`), and stating that M17 does not implement it.
  - [x] Add a short "Future Syntax Landing Zone" section to `kernel/language/README.md` (and the Chinese counterpart) explaining: (a) new authored constructs land as new `Declaration` (or `ScalarValue`) sealed variants, (b) parser adaptation for a new construct stays isolated inside the parser-internal package established in Story `1.2`, (c) lowering in `:kernel:compiler` must handle new variants through an exhaustive `when`, and (d) this landing zone intentionally does not finalize `import` semantics in M17.
  - [x] Cross-reference Epic 5 Story `5.3` (future syntax landing-zone note) in the README so the note is discoverable from both the language module and the milestone closeout artifact.
- [x] Prove the lowering boundary already fails loudly on unhandled `Declaration` variants, and keep it that way. (AC: 1, 4)
  - [x] Locate the lowering code in `:kernel:compiler` that consumes `SourceFileAst.declarations` (the `EngineeringIrLowerer` and related lowering code referenced from `AthenaCompiler.kt`) and confirm it dispatches over `Declaration` with an exhaustive `when` (no `else -> {}` catch-all that would silently swallow a future declaration kind).
  - [x] If an `else` branch or non-exhaustive dispatch exists anywhere in the current lowering path over `Declaration`, tighten it to an exhaustive `when` as part of this story so that adding a future `Declaration` variant produces a compile error at the lowering site, forcing explicit handling rather than silent ignoring.
  - [x] Add a focused test (e.g. in `:kernel:language` or as a documentation-oriented compile-time check) that documents this exhaustiveness expectation, such as a KDoc-referenced note plus a regression test asserting today's three declaration kinds are exactly the ones lowering handles.
- [x] Write a concrete "how a future import would land" illustration without implementing it. (AC: 2, 3)
  - [x] Add a code comment or small design note (in `AthenaLanguageModel.kt` KDoc or a dedicated `kernel/language/docs/future-syntax-landing-zone.md`-style note, consistent with existing repo documentation conventions) sketching the shape of a hypothetical future `ImportDeclaration` (e.g. carrying a qualified module/package reference and a span) purely as illustration.
  - [x] Explicitly state in that note that: no `ImportDeclaration` type is added to the compiled model in this story, no parser support for an `import` keyword is added, and no lowering or `Engineering IR` change is made for imports.
  - [x] Cross-check the note against AD-111 and AD-110 language so the illustration matches the architecture spine's own future-syntax guidance rather than inventing new semantics.
- [x] Keep Story `1.3` narrow. (AC: 1, 2, 3, 4)
  - [x] Do not add `ImportDeclaration`, `import` keyword parsing, or any new `Declaration`/`ScalarValue` variant to compiled code.
  - [x] Do not implement package-aware authored semantics, macro-use syntax, or any expression language feature.
  - [x] Do not implement the ANTLR4 grammar (Epic 2) or Tree-sitter grammar (Epic 3); this story only ensures the AST/lowering boundary is shaped correctly for whatever parser eventually feeds it.
  - [x] Do not restructure the parser-internal packaging introduced in Story `1.2` beyond referencing it in documentation.
- [x] Run focused and regression verification sequentially on Windows with Java 25. (AC: 1, 2, 3, 4)

## Dev Notes

### Story Intent

- Story `1.3` is the forward-looking closeout of Epic 1: it does not add any new authored syntax, but it proves and documents that the AST boundary Story `1.1` froze and Story `1.2` isolated can absorb future syntax growth (starting with `import`) as an additive sealed-hierarchy extension rather than a breaking redesign.
- The success condition is not "Athena parses imports." The success condition is "a future contributor implementing import syntax after M17 has an unambiguous, documented, compiler-enforced pattern to follow: add a `Declaration` variant, adapt it in the isolated parser-internal package, and handle it through an exhaustive `when` at lowering — with zero ambiguity about where semantic finalization happens."
- This story deliberately produces documentation, KDoc, and a compile-time exhaustiveness guarantee, not new production AST types. Any temptation to add a real `ImportDeclaration` "since we're already here" must be resisted; that is explicitly out of scope per FR-9's own "does not claim to finalize" language and per Epic 5 Story `5.3`, which is the actual milestone-closeout landing-zone note.

### Architecture Guardrails

- Align to AD-104: M17 freezes one language architecture before language breadth expands, and every future authored construct must land on the same architecture (compiler parser -> authored AST -> lowering -> `Engineering IR`). Story `1.3` proves the authored-AST link in that chain is extensible without breaking the chain. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-104---M17-Freezes-One-Language-Architecture-Before-Language-Breadth-Expands]
- Align to AD-106: authored AST remains the only lowering input; parse-tree-to-AST adaptation stays isolated inside the syntax layer, and `Engineering IR` may not depend on generated parser nodes. Story `1.3`'s exhaustiveness check exists precisely to guarantee lowering keeps consuming `Declaration` variants (authored AST), never parser output directly, even as new variants are added later. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-106---Authored-AST-Remains-The-Only-Lowering-Input-Before-Engineering-IR]
- Align to AD-110: the first M17 proof stays parity-first on the current supported syntax subset (`system`, `device`, `port`, `connect`, qualified names, string literals, property assignments); M17 may prepare future constructs such as `import` but does not need to ship their final semantics. This story is exactly that preparation, with the explicit non-shipping boundary this AD requires. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-110---The-First-M17-Proof-Stays-Parity-First-On-The-Current-Supported-Syntax-Subset]
- Align to AD-111: future syntax growth lands through AST extensibility, not ad hoc grammar patches; AST contracts remain extensible, parser adaptation remains isolated, and lowering remains organized around authored semantic categories rather than parser token sequences. This is the primary AD this story exists to satisfy directly. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-111---Future-Syntax-Growth-Lands-Through-AST-Extensibility-Not-Ad-Hoc-Grammar-Patches]
- Preserve inherited AD-82: DSL remains canonical serialization, not the default human interface. The future-syntax note must not imply M17 is shifting Athena toward a DSL-first authoring strategy; it only documents extensibility of the existing syntax-owned AST. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` already models `Declaration` as `sealed interface Declaration { val span: SourceSpan }` with exactly three implementers today: `DeviceDeclaration`, `PortDeclaration`, `ConnectionDeclaration`. This is already the correct extensible shape; this story adds documentation and a compile-time guarantee around it, not a redesign.
- `ScalarValue` is already `sealed interface ScalarValue { val span: SourceSpan }` with exactly two implementers: `ScalarValue.Identifier` and `ScalarValue.StringLiteral`, nested inside the sealed interface using the existing repo convention.
- `SourceFileAst.declarations` is already `List<Declaration>`, an open ordered collection, not fixed named fields — this is already friendly to future declaration kinds being appended without breaking the `SourceFileAst` constructor.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt` calls `lowerer.lower(parseResult.source)` where `lowerer: EngineeringIrLowerer`. The actual `when` dispatch over `Declaration` variants lives inside `EngineeringIrLowerer` (a separate file in `:kernel:compiler`, not reproduced in the `AthenaCompiler.kt` excerpt already reviewed); this story's task to audit exhaustiveness must locate and inspect that lowering code directly rather than assuming its current shape.
- There is no `ImportDeclaration`, no `import` keyword handling in `AthenaLanguageParser.kt`'s tokenizer/parser, and no package-aware authored semantics anywhere in the current grammar (`parseDeclaration()` only recognizes `device`, `port`, `connect` keywords). This story must not add any of these.
- `kernel/language/README.md` and `README.zh-CN.md` currently describe the syntax boundary and its "Main Types" but do not yet mention future extensibility or a landing zone for constructs like `import`.

### Technical Requirements

- Keep all changes additive at the documentation and test level; do not modify the runtime shape of `Declaration`, `ScalarValue`, or `SourceFileAst` in this story.
- If tightening an existing non-exhaustive `when` in `:kernel:compiler`'s lowering code to be exhaustive, make the minimal change needed (remove a catch-all `else` branch only if every current variant is already explicitly handled) and do not otherwise restructure the lowerer.
- Any illustrative "future `ImportDeclaration`" sketch must live in documentation or KDoc comments only — never as compiled Kotlin code, never registered in the sealed hierarchy, and never referenced by the parser or lowerer.
- Follow the existing bilingual documentation pattern (`README.md` + `README.zh-CN.md`, the latter saved as UTF-8 with BOM) for any new landing-zone documentation.

### Architecture Compliance

- The story is only successful if a future engineer implementing Epic-5-style or post-M17 `import` support can read this story's documentation and the `Declaration` KDoc and immediately understand the required pattern: add a sealed variant, adapt it inside the Story `1.2` parser-internal package, and extend the lowering `when` — without needing to reverse-engineer the intended extension point from scratch.
- Prevent these failure modes:
  - A non-exhaustive `when` (silent `else`) at the lowering boundary that would let a future declaration kind compile successfully while being silently dropped from `Engineering IR`.
  - Documentation drift where the landing-zone note contradicts or duplicates Epic 5 Story `5.3`'s milestone-closeout note instead of cross-referencing it.
  - Scope creep where "documenting the landing zone" turns into actually implementing `import` parsing or lowering.
  - Treating `ScalarValue` and `Declaration` identically when they serve different extension purposes (declarations are top-level authored constructs; scalar values are field-level literal kinds) — keep the landing-zone note specific about which hierarchy future constructs like `import` would extend (most likely `Declaration`, not `ScalarValue`).

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the workspace:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Do not add any new dependency to `kernel/language/build.gradle.kts` or `kernel/compiler/build.gradle.kts` for this story.
- Reuse current Kotlin sealed-interface and KDoc conventions already present in `kernel/language`.

### File Structure Requirements

- Expected update files likely include:
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` (KDoc on `Declaration` and `ScalarValue` only)
  - `kernel/language/README.md` (new "Future Syntax Landing Zone" section)
  - `kernel/language/README.zh-CN.md` (equivalent Chinese section, UTF-8 with BOM)
  - Possibly `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt` or the equivalent lowering file, only if a non-exhaustive `when` over `Declaration` is found and must be tightened
  - `kernel/language/src/test/kotlin/com/engineeringood/athena/language/` or `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/` — a new focused test documenting the exhaustiveness expectation
- Do not create a new Gradle module. This story stays inside the existing `:kernel:language` and (only if needed for the exhaustiveness fix) `:kernel:compiler` modules.

### Testing Requirements

- Minimum verification should target the language module directly first:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:language:test"`
- If any lowering code in `:kernel:compiler` is touched to tighten exhaustiveness, also run:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Keep Gradle verification strictly sequential on Windows. Do not run these commands concurrently; wait for each to finish before starting the next.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after updating the bilingual README files.

### Explicit Non-Goals

- No `ImportDeclaration` type or any other new `Declaration`/`ScalarValue` variant added to compiled code.
- No `import` keyword parsing or tokenizer support.
- No package-aware authored semantics, module resolution, or import resolution logic.
- No macro-use syntax or expression language feature.
- No ANTLR4 grammar (Epic 2) or Tree-sitter grammar (Epic 3) work.
- No restructuring of the Story `1.2` parser-internal package boundary beyond referencing it in documentation.
- No finalization of any future syntax semantics; this story only proves and documents the landing zone.

### Previous Milestone Intelligence

- M16 Story `1.1`'s Dev Notes explicitly separated "what this story defines" from "what a later story should define" (e.g. deferring template payloads to Story `1.2` and runtime seams to Story `1.3`). M17 Story `1.3` follows the same discipline in reverse direction: it defines the extension point for future work (post-M17 `import`) without pulling that future work forward into this milestone.
- The architecture spine's own "Deferred" section explicitly lists "final `import` semantics and package-aware authored behavior" as out of scope for all of M17, not just this story — reinforcing that Story `1.3`'s job is structural readiness, not delivery.
- Epic 5 Story `5.3` ("Publish The Future Syntax Landing-Zone Note And Verification Path") is the milestone-level closeout note that records how future syntax should land; Story `1.3` should produce the code-level and module-level extensibility groundwork that Story `5.3` can later cite as evidence, rather than duplicating Story `5.3`'s milestone-closeout scope.

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/language/README.md]

## Dev Agent Record

### Agent Model Used

Composer (Sonnet)

### Debug Log References

- Domain lowering previously used `filterIsInstance`, which would silently drop future `Declaration` variants; tightened to exhaustive `when` partitions in electrical/dummy plugins and the compiler test fixture.
- LSP already had an exhaustive `Declaration` `when`; left unchanged.
- No `ImportDeclaration` type or import parsing added.

### Completion Notes List

- Documented future-syntax landing zone in `Declaration`/`ScalarValue` KDoc, bilingual READMEs, and `docs/future-syntax-landing-zone.md` (cross-ref Epic 5 Story `5.3`, AD-110/AD-111).
- Added `AstExtensibilityLandingZoneTest` with compile-time-exhaustive classifiers for today's three declaration kinds and two scalar value kinds.
- Preserved Story `1.2` parser-internal package boundary; no ANTLR/Tree-sitter work.

### File List

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/docs/future-syntax-landing-zone.md`
- `kernel/language/README.md`
- `kernel/language/README.zh-CN.md`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt`
- `extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `_bmad-output/implementation-artifacts/m17/1-3-preserve-ast-extensibility-for-future-syntax.md`
- `_bmad-output/implementation-artifacts/m17/sprint-status.yaml`

### Change Log

- 2026-07-14: Documented AST extensibility landing zone and enforced exhaustive Declaration dispatch; status → done after verification.

## Story Completion Status

- Status: done
- Completion note: Future-syntax landing zone documented (no ImportDeclaration implemented). Exhaustive Declaration dispatch tightened at domain lowering sites; `AstExtensibilityLandingZoneTest` locks today's sealed surface.
