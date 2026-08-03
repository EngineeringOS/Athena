---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.3: Add Electrical Package Construct Implementations

Status: review

## Story

As an engineer,
I want rail, rung, branch, wire bundle, terminal strip, contact group, and coil group constructs,
so that I can author projection structure with electrical vocabulary.

## Acceptance Criteria

1. The electrical package contributes `RailProjection`, `RungProjection`, `BranchProjection`,
   `WireBundleProjection`, `TerminalStripProjection`, `ContactGroupProjection`, and
   `CoilGroupProjection` under the `ProjectionConstruct` contract.
2. Construct words resolve through the electrical package exactly like M39 domain relation verbs
   (`power`/`control`/`earth`); the kernel compiles without dependency on them.
3. The M40 example authors all seven construct forms from the committed Syntax Target.
4. Grouped endpoints remain one traceable engineering relationship through Projection.

## Tasks / Subtasks

- [x] Add seven construct declaration forms to the grammar (view members).
- [x] Add AST/IR lowering with sheet binding.
- [x] Add electrical package kind registry (seven kinds).
- [x] Wire constructs into projection sheets; validate membership.
- [x] Add language + compiler + registry tests; update story + sprint.

## References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 2.3]
- [Source: PRD FR-8, FR-11; Syntax Target; ARCHITECTURE-SPINE.md AD-11]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added construct grammar, AST/IR, electrical registry, sheet wiring.
- 2026-08-02: Language 75/75, electrical + compiler suites green.

### Completion Notes List

- Grammar: seven construct kinds (`power-rail`, `rung`, `branch`, `wire-bundle`,
  `terminal-strip`, `contact-group`, `coil-group`) as view members with qualified member lists;
  tokens added to `ident`.
- Language model: `ProjectionConstructDeclaration`; adapter wired.
- IR: `EngineeringProjectionConstruct` bound to the most recent sheet; lowered in
  `EngineeringIrLowerer`.
- Electrical package: `ElectricalProjectionConstructKinds` registry + contract implementations
  under the kernel-owned `ProjectionConstruct` interface (new projection-model dependency).
- Compiler: constructs attach to sheets with stable ids; empty/duplicate/unresolved-member
  validation fails closed.
- LSP: formatter + document symbols handle constructs; facade allow-list extended.

### File List

- kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt
- kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/ViewDeclarationParserTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AstExtensibilityLandingZoneTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt
- kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionConstructCompilationTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt
- extensions/domain-electrical/build.gradle.kts
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalProjectionConstructKinds.kt
- extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalProjectionConstructKindsTest.kt
- extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeLowering.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectSourceFormatter.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 2.3 and PRD FR-8/FR-11.
- 2026-08-02: Implemented seven construct forms + electrical package contributions; suites
  green; marked review.
