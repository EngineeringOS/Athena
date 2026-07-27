---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 1.1: Establish The Canonical Representation Contract

Status: done

## Story

As an Athena platform developer,
I want one canonical compiled representation model and explicit migration ownership,
so that later Symbol and Element features cannot create competing truths.

## Acceptance Criteria

1. **Given** the existing M30-M33 representation contracts, **when** the M34 model is introduced,
   **then** `RepresentationDefinition` owns Symbol/Element kind, intrinsic composition,
   `GraphicPrimitive` body, anchor compatibility, slots, lifecycle, version, and provenance, while
   `RepresentationDescriptor` remains generated and cannot be independently authored.
2. **Given** a representation definition attempts to own a project device, port, connection, or
   classification, **when** validation runs, **then** compilation fails with stable diagnostics that
   preserve representation source provenance.
3. **Given** `DrawingSymbolAnatomy`, `M33IecSymbolDefinition`, `PresentationPrimitive`, descriptor-to-
   definition fallback conversion, and legacy box/direct-SVG paths, **when** migration tests and the
   ledger run, **then** every path has one Reuse/Extend/Replace/Delete disposition, named active
   callers, owner, target story, and executable deletion gate.
4. **Given** all previous criteria are green, **when** the mandatory final polish/purge runs, **then**
   source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed; stale or
   duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-1, FR-3..FR-4, FR-13..FR-15, FR-22..FR-23, FR-41; NFR-1, NFR-5..NFR-6.

## Tasks / Subtasks

- [x] Add canonical M34 definition contract tests before production edits (AC: 1, 2)
  - [x] Prove Symbol and Element kinds share `RepresentationDefinition`.
  - [x] Prove the definition carries one typed `GraphicPrimitiveDocument`, anchors, slots,
        lifecycle/version/provenance, and optional intrinsic composition.
  - [x] Prove duplicate ids and forbidden project-authority claims fail deterministically.
- [x] Extend the existing representation model without adding a parallel IR (AC: 1, 2)
  - [x] Split definition-focused models out of the oversized `RepresentationContracts.kt` only where
        it improves role-based organization; keep package names and public call sites stable.
  - [x] Add minimal Symbol/Element, anchor-compatibility, source-provenance, composition, and forbidden-
        authority contracts required by M34.
  - [x] Extend `RepresentationContractValidator`; do not introduce frontend, package-runtime, or
        renderer inference.
- [x] Preserve current M30-M33 callers while making migration direction explicit (AC: 1, 3)
  - [x] Keep compatibility defaults/adapters only where existing callers require them.
  - [x] Record the exact transition from `DrawingSymbolAnatomy` and `M33IecSymbolDefinition` to the
        canonical definition.
  - [x] Record descriptor reverse-construction, `PresentationPrimitive`, direct SVG, and box-renderer
        deletion gates; do not delete a path with active product callers in this story.
- [x] Run focused and module regression tests sequentially (AC: 1..3)
  - [x] Capture a genuine RED failure before implementation and GREEN output afterward.
  - [x] Run `:kernel:representation-model:test` and any directly affected package-runtime tests.
- [x] Perform mandatory polish/purge and evidence review (AC: 4)
  - [x] Check source/test responsibility, stale fixtures/docs, generated output, encoding, and workspace.
  - [x] Record AC-to-evidence mapping, three-layer review, and all touched files.

### Review Findings

- [x] [Review][Patch] Prevent canonical Graphic Primitive definitions from independently authoring
  legacy visual anatomy; add an explicit compatibility-shell authority.
- [x] [Review][Patch] Validate canonical Graphic Primitive bodies and retain definition provenance in
  stable admission diagnostics.
- [x] [Review][Patch] Use canonical terminal anchors for occurrence anchor-existence validation while
  preserving the named legacy-anatomy compatibility path.
- [x] [Review][Patch] Add explicit Symbol and Element contract coverage, exact authority diagnostics,
  order-independence coverage, and canonical-body rejection coverage.
- [x] [Review][Patch] Add provenance to library/lifecycle diagnostics and the final deterministic sort key.
- [x] [Review][Patch] Replace prose migration gates with named owners and exact Gradle/search commands.
- [x] [Review][Defer] Resolve child definitions, reject Element nesting/cycles, and validate exported
  anchors in Story 1.3, which owns intrinsic composition compilation.
- [x] [Review][Defer] Reject ambiguous Profile/Binding policy matches in Story 3.1, which owns the
  deterministic selection chain.
- [x] [Review][Defer] Evaluate direction/signal compatibility predicates in Story 3.2, which owns
  project-port-to-element-anchor binding.
- [x] [Review][Defer] Remove independently authored descriptors and reverse descriptor construction in
  Story 3.3; Story 1.1 preserves those named M32/M33 callers only as ledgered compatibility debt.
- [x] [Review][Defer] Remove the final legacy anatomy and Presentation Primitive product paths in Story
  3.4 after the canonical Cabinet path is active.

## Dev Notes

### Non-Negotiable Boundaries

- Extend `RepresentationDefinition`; do not create `ElementDefinition`, SVG IR, or another reusable-
  definition authority.
- `Element` is visual composition only. It cannot own vendor engineering parameters, actual project
  ports, device instances, connections, classification, or authored project layout.
- Direction/signal/role values on representation anchors are compatibility predicates only.
- `GraphicPrimitive` is the canonical active Cabinet visual vocabulary. `PresentationPrimitive` is
  migration-only and must receive no new M34 producer.
- `RepresentationDescriptor` belongs to package indexing/resolution and is derived from the canonical
  definition. The existing descriptor-to-definition rectangle fallback is reverse authority and must
  be ledgered for removal in Story 3.3/3.4.
- Do not touch ANTLR4/tree-sitter syntax in this story; Story 1.2 owns native Symbol source.

### Existing Code To Reuse

- `RepresentationDefinition` currently lives in `RepresentationContracts.kt` and already owns
  symbol/library/version/lifecycle/kind, `PresentationAnatomy`, labels, variants, and style tokens.
- `GraphicPrimitiveDocument` and the sealed `GraphicPrimitive` vocabulary already live in
  `GraphicPrimitiveModels.kt`; reuse them directly.
- `DrawingSymbolAnatomy` already carries M33 primitives, anchors, slots, hotspots, bounds, and
  provenance. Treat it as compatibility input, not the new canonical model.
- `M33IecSymbolDefinition` wraps descriptor/resource ids plus `DrawingSymbolAnatomy`; its active
  producer is `M33IecSymbolSupport`.
- `PackageBackedRepresentationOccurrenceFactory.toRepresentationDefinition` currently reconstructs
  a definition from a descriptor using `PresentationPrimitive.Rectangle`. Do not expand it.
- Existing validation and serialization patterns use stable `RepresentationDiagnosticCode.wireValue`
  values and deterministic sorted payloads.

### File Structure

- Main model: `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/`
- Main tests: `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/`
- Migration evidence: `_bmad-output/implementation-artifacts/m34/`
- Keep small related value types grouped. Do not create one file per tiny DTO or a new 400-line dump.

### Testing Requirements

- Kotlin tests use `kotlin.test` and Gradle module tasks.
- Run Gradle tasks strictly sequentially on Windows; never overlap Gradle invocations.
- Tests must assert real model/validator behavior, not source-text presence alone.
- Stable diagnostics must test code, provenance, ordering, and rejected package admission.
- Compatibility tests must show existing M30/M33 definition callers still compile until their named
  migration story removes them.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Addendum](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationContracts.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingSymbolAnatomy.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecSymbolSupport.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactory.kt`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `RepresentationDefinitionContractTest` initially failed to compile because the new canonical
  definition types did not exist. Follow-up RED runs exposed invalid test assumptions about
  `RepresentationContext.CABINET` and the legacy anatomy fixture requirement.
- GREEN: `:kernel:representation-model:test --tests RepresentationDefinitionContractTest`,
  `:kernel:representation-model:test`, `:kernel:package-runtime:test`, and the full `test` task all
  completed successfully in sequential Gradle runs. The post-review full suite completed in 3m32s
  with 151 actionable tasks and no failures.

### Completion Notes List

- Story context created from current PRD, architecture spine, epics, and CodeGraph caller analysis.
- Extended the existing canonical `RepresentationDefinition` with Symbol/Element kind, typed
  `GraphicPrimitiveDocument`, typed anchor compatibility, intrinsic composition, and explicit
  forbidden-authority claims while preserving M30-M33 callers through migration defaults.
- Added stable validation diagnostics for duplicate anchors, duplicate composition children, and
  representation attempts to claim project semantic authority. Diagnostics retain definition
  provenance and deterministic ordering.
- Split cohesive definition models out of `RepresentationContracts.kt`; no parallel definition IR,
  frontend inference, renderer inference, XML authority, or new legacy primitive producer was added.
- Added `representation-migration-ledger.md` with Reuse/Extend/Replace/Delete dispositions, active
  callers, target stories, owners, and executable deletion gates.
- AC-to-evidence: AC1 and AC2 are covered by `RepresentationDefinitionContractTest` plus module tests;
  AC3 is covered by package-runtime regression and the migration ledger; AC4 is covered by the full
  sequential test suite, encoding audit, diff check, file responsibility review, and this record.
- Three-layer review: source responsibility remained in representation-model; tests exercise model
  behavior and diagnostics rather than source text; workspace review found unrelated M32/M33 work
  and preserved it without reverting or absorbing it into this story.
- Adversarial review returned Story 1.1 to `in-progress`, fixed six current-story gaps, and assigned
  composition, policy, binding, descriptor migration, and final renderer migration only to their
  existing M34 owner stories. No acceptance criterion was weakened to hide compatibility debt.
- Canonical definitions now explicitly select `GRAPHIC_PRIMITIVE` authority and can carry only a
  non-authoritative `PresentationAnatomy` compatibility shell; legacy definitions retain their
  default authority until their named deletion stories execute.

### File List

- `_bmad-output/implementation-artifacts/m34/1-1-establish-the-canonical-representation-contract.md`
- `_bmad-output/implementation-artifacts/m34/representation-migration-ledger.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/deferred-work.md`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/PresentationAnatomy.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationContracts.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationValidation.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionContractTest.kt`

## Change Log

- 2026-07-24: Established the canonical M34 representation definition contract, validation,
  compatibility migration ledger, and regression evidence; moved story to review.
- 2026-07-24: Addressed adversarial review findings with explicit body authority, canonical body and
  anchor validation, deterministic provenance evidence, stronger tests, and executable migration gates;
  all sequential regressions passed and the story moved to done.
