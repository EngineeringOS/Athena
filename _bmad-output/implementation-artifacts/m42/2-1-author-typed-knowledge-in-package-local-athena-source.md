---
baseline_commit: c00b416c463d3e18876dced3be6b750d2f0652ff
---

# Story 2.1: Author Typed Knowledge In Package-Local Athena Source

Status: done

## Story

As a domain knowledge maintainer,
I want to author typed cross-domain definitions in package-local Athena Source,
so that engineering meaning lives in inspectable packages instead of Kotlin templates or metadata payloads.

## Acceptance Criteria

1. A Knowledge Package `.athena` source file with one default `domain` is parsed by the same ANTLR
   frontend and source-span model as project source. Concepts, Parts, Capabilities, Relationships,
   Flows, units/dimensions, formulas, and Constraints produce sealed typed AST nodes. Filename parser
   forks, second semantic parsers, string formula evaluation, and manifest engineering facts are rejected.
2. Formula syntax admits only `+`, `-`, `*`, `/`, `min`, `max`, `round-up`, parentheses, typed
   references, comparisons `>`, `>=`, `<`, `<=`, `=`, `!=`, and intervals `[10, 20)`, `[10, 20]`,
   `(10, 20)`. Validation checks operand dimensions, interval bounds, positive rounding quantum, and
   affine-unit multiplication/division restrictions exactly. Scripts, loops, arbitrary functions,
   reflection, filesystem/network access, and hidden Kotlin calculations are impossible.
3. Concept, Part, Capability, and Relationship definition contracts keep concerns separate:
   Capability means participation/provision/consumption only; classification triggers no inference;
   Part facts never generate authored anatomy; Relationship definitions own roles, subject levels,
   cardinality, direction, required Capabilities, admitted Flows, logical pairing, and connectivity
   admission. `AuthoringActionAvailability`, Capability evidence, and Relationship participation stay
   separate contracts.
4. Accepted/rejected M42 syntax fixtures are shared by ANTLR and Tree-sitter. Tree-sitter remains
   syntax/highlighting-only; regenerated parser/WASM and highlight/query tests agree; frontend bundle
   rebuilds. Active Kotlin/default templates, ad hoc `.properties` definition authority, and
   superseded grammar aliases touched by this story are deleted. No compatibility aliases/adapters.

## Tasks / Subtasks

- [x] Task 1: Establish package knowledge AST contracts (AC: 1, 3)
  - [x] Add cohesive typed declaration/model nodes for domain, Concept, Part, Capability, Relationship,
        Flow, Unit/Dimension, Formula, Constraint, typed literals/references, and source Provenance.
  - [x] Keep contracts domain-neutral and sealed; package/version identity belongs to package context,
        not kernel constants.
- [x] Add exact value/formula/comparison/interval contracts with no executable escape hatch.
- [x] Task 2: Extend shared ANTLR source frontend (AC: 1, 2)
  - [x] Add package knowledge grammar admission using existing lexer/parser and source spans.
  - [x] Lower parser contexts to typed knowledge AST deterministically; reject malformed, duplicate,
        ambiguous, and unsupported constructs with plain diagnostics.
  - [x] Add red tests first for accepted definitions, exact spans, formula operators, dimensions,
        intervals, and rejected script/legacy forms; implement minimal parser/lowering then refactor.
- [x] Task 3: Install closed formula and constraint syntax validation (AC: 2, 3)
  - [x] Implement bounded formula AST validation for operator arity/types, dimensions, affine rules,
        interval compatibility, and positive `round-up` quantum.
- [x] Keep evaluation out of this story; no string parsing or hidden Kotlin formulas.
  - [x] Ensure Capability/classification and Part/anatomy boundaries are explicit in tests.
- [x] Task 4: Migrate editor grammar and delete superseded authority (AC: 4)
  - [x] Update Tree-sitter grammar/highlight/query fixtures from shared accepted/rejected syntax and
        regenerate parser/WASM artifacts.
  - [x] Remove active Kotlin/default knowledge templates, `.properties` definition payload authority,
        duplicate legacy grammar aliases, and stale tests/docs/examples touched by replacement.
  - [x] Preserve only package manifest/lock/location mechanics; no compatibility shell.
- [x] Task 5: Verify and complete records (AC: 1-4)
  - [x] Run affected Gradle tests sequentially, full `test`, frontend tests/build, source-set hygiene,
        encoding audit, and `git diff --check`.
  - [x] Record Debug Log, Completion Notes, complete File List, Change Log, and mark story `review` only
        after every acceptance check passes.

## Dev Notes

### Architecture Guardrails

- Follow AD-20 through AD-28, AD-31, AD-32, AD-36, AD-37, AD-39. Chain: source meaning -> typed
  Knowledge AST -> compiler-owned Knowledge Document (Story 2.2) -> resolution/evaluation.
- `knowledge-model` depends on `engineering-model` and `package-model`; downstream Reality modules do
  not depend on it. This story installs syntax/contracts only; no evaluator or Validation Document.
- ANTLR is sole semantic parser for project and package `.athena`; Tree-sitter is editor-only.
- Exact arithmetic uses reduced `BigInteger` rationals and package-defined dimensions/units. Do not add
  electrical constants to shared modules.
- Capability is provision/requirement/participation, never classification. Part assignment does not
  create Entity, Function, Port, Relationship, or Flow anatomy.
- No backward compatibility. Delete superseded active surfaces directly. Closed M0-M41 artifacts stay
  immutable.

### Existing Code Intelligence

- Story 1.2 migrated active project relations to typed `EngineeringRelationship`/`EngineeringFlow` and
  removed active Connection, graph-glsp, reuse, Semantic Macro, and stale milestone surfaces.
- Existing `Declaration` sealed model and ANTLR adapter are shared project syntax entry points. Extend
  them or introduce cohesive knowledge declaration siblings; do not add a second parser.
- CodeGraph blast radius identified deleted legacy knowledge classes (`AthenaKnowledgePackageLoader`,
  `AthenaKnowledgeResolver`, governed source builder) and package-runtime resolver survivors. Rebuild
  only current M42 authority; do not restore legacy metadata semantics.
- Keep Kotlin files grouped by role; split mixed files near 200-300 lines.

### Testing Requirements

- Literal assertions: declaration kinds, package/domain identity, typed values, operator trees,
  dimensions, interval inclusivity, source spans, deterministic ordering, and exact diagnostics.
- Negative tests: duplicate definitions, unsupported script/loop/function syntax, malformed interval,
  incompatible dimensions, affine multiply/divide, non-positive round-up quantum, legacy Component/
  Connection/Semantic Macro/property authority.
- Required sequential commands after implementation:
  ` .\gradlew.bat --no-daemon --console=plain :kernel:language:test`
  ` .\gradlew.bat --no-daemon --console=plain :kernel:knowledge-model:test`
  ` .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  ` .\gradlew.bat --no-daemon --console=plain :ide:tree-sitter-athena:test`
  ` .\gradlew.bat --no-daemon --console=plain test`
  ` powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  ` powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  ` git diff --check`

### References

- `_bmad-output/planning-artifacts/m42/epics.md` Story 2.1
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/prd.md` FR-7, FR-9-FR-12, NFR-2,
  NFR-5, NFR-6
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-04-m42/ARCHITECTURE-SPINE.md`
  AD-20 through AD-28, AD-31, AD-32, AD-36, AD-37, AD-39
- `_bmad-output/implementation-artifacts/m42/1-2-express-typed-relationships-and-independent-flows.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story created through BMad create-story workflow after full M42 artifact/context and CodeGraph review.
- Red/green/refactor completed per task; syntax and contract tests added before final gate.

### Completion Notes List

- Added package-local `domain` source AST with Concepts, Parts, Capabilities, Relationships, Flows,
  dimensions, units, formulas, predicates, intervals, and exact source spans.
- Added `knowledge-model` module with domain-neutral definition contracts and closed formula validator.
- Extended shared ANTLR grammar/adapter; Tree-sitter grammar and WASM now mirror knowledge syntax.
- Deleted stale Tree-sitter scripts tied to removed M0-M37 examples; no metadata authority restored.
- Passing: `:kernel:language:test`, `:kernel:knowledge-model:test`, `:kernel:compiler:test`, Tree-sitter
  `yarn test`, source-set hygiene, encoding audit, `git diff --check`.

### File List

- `settings.gradle.kts`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/KnowledgeSourceSyntaxTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/knowledge-model/build.gradle.kts`
- `kernel/knowledge-model/src/main/kotlin/com/engineeringood/athena/knowledge/KnowledgeDefinitionModels.kt`
- `kernel/knowledge-model/src/test/kotlin/com/engineeringood/athena/knowledge/KnowledgeDefinitionModelsTest.kt`
- `kernel/compiler/build.gradle.kts`
- `ide/tree-sitter-athena/grammar.js`, generated `src/*`, `tree-sitter-athena.wasm`, knowledge corpus,
  and deletion of stale M0-M37 script tests.

### Change Log

- 2026-08-05: Created Story 2.1 from final M42 PRD, architecture, epics, sprint status, prior-story
  intelligence, and current repository authority inventory.
- 2026-08-05: Implemented typed knowledge source contracts, shared ANTLR admission, closed formula
  syntax surface, Tree-sitter projection, and legacy test authority deletion; marked `review`.
