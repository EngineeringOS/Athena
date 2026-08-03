---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.3: Declare Reality Authority And Validation

Status: review

## Story

As a maintainer,
I want each reality to state authority, identity, and required facts,
so that broken pipelines fail at the right boundary.

## Acceptance Criteria

1. Engineering, Projection, Spatial, and Presentation realities each declare purpose, authority, owned facts, identity rules, and validation failures in plain product terms.
2. Validation reports missing required facts in plain language and names the reality that failed.
3. Fact identity remains traceable to source identity or the prior reality without renderer repair.
4. New M39 names avoid milestone names, `V0`/`V1`, vague `Evidence` terms, and long unclear names.
5. No generic graph framework, universal `Fact` base class, compatibility shim, or empty wrapper model is introduced.

## Tasks / Subtasks

- [x] Establish failing authority and validation tests first (AC: 1-5)
  - [x] Add tests proving each reality declaration exposes purpose, authority, owned facts, identity rules, and required facts.
  - [x] Add tests proving validation failures include the reality name and a plain missing-fact message.
  - [x] Add naming guard tests for new M39 reality files: no milestone names, `V0`/`V1`, vague `Evidence`, `ProfessionalControlDrawing`, or compatibility naming.
- [x] Add a small shared reality declaration contract where it removes duplication (AC: 1, 5)
  - [x] Keep the contract concrete and product-named.
  - [x] Do not create a universal `Fact` hierarchy, graph framework, or transformation metadata.
  - [x] Keep existing reality roots stable unless cleanup is required by the new contract.
- [x] Declare identity and required-fact rules per reality (AC: 1, 3)
  - [x] Engineering identity rules trace to source/system/device/port/connection identities.
  - [x] Projection identity rules trace to engineering subjects plus view/sheet/occurrence identity.
  - [x] Spatial identity rules trace to projection occurrence/connection identity plus placement, anchor, lane, and route ids.
  - [x] Presentation identity rules trace to spatial shape/connector/label targets plus paint item ids.
- [x] Implement validation boundaries (AC: 2, 3)
  - [x] Engineering validation rejects missing system or missing engineering source identity.
  - [x] Projection validation rejects missing view, sheet, occurrence source, or reading-order facts.
  - [x] Spatial validation rejects missing placement, bounds, anchor position, lane, or route identity where present.
  - [x] Presentation validation rejects paintable facts without target, visibility, or paint order where present.
- [x] Clean stale naming discovered during implementation (AC: 4, 5)
  - [x] Delete or rename incompatible stale production code directly; do not add aliases.
  - [x] Update tests/docs only where they preserve current M39 architecture.
  - [x] Keep `to` and `->` alias behavior untouched.
- [x] Verify and update tracking (AC: 1-5)
  - [x] Run affected module tests sequentially.
  - [x] Run full `gradlew test` if shared reality contracts affect compiler/runtime/LSP.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story finishes Epic 2 foundation. It does not implement Engineering to Projection, Projection to Spatial, or Spatial to Presentation transformations. It does not build placement, routing, labels, Theia rendering, SVG export, or screenshots.

The goal is a small, testable authority contract for the four existing M39 realities:

- what each reality owns;
- who may create/modify it;
- how facts are identified;
- what validation failures stop the pipeline.

Do not expand Athena source syntax. Do not bring back `intent`. Do not add compatibility shims.

### Current Code Intelligence

Story 2.1 added the four root declarations:

- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringReality.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReality.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationReality.kt`

Story 2.2 removed Projection-owned spatial facts:

- `ProjectionDocument` no longer owns canvas size, labels, electrical anchors, endpoints, or routing corridors.
- `ProjectionNode` no longer owns final bounds.
- `ProjectionConnection` no longer owns route start/end geometry.
- Runtime and LSP projection-session payloads no longer publish empty compatibility geometry fields.

Use CodeGraph before editing symbols. Expect dependencies in model modules, compiler, runtime, LSP, CLI, and domain tests if a shared contract is introduced.

### Implementation Hints

Prefer direct product language:

- `RealityDeclaration`
- `RealityValidation`
- `RealityValidationResult`
- `RealityValidationIssue`
- `RealityIdentityRule`

Avoid:

- `FactBase`
- `RealityGraphNode`
- `Evidence`
- `ProfessionalControlDrawing`
- milestone names such as `M39Reality...`
- `V0` / `V1`
- deprecated aliases

A small shared contract may live in an existing model module only if dependencies stay clean. If shared placement would create bad dependencies, keep small duplicated declarations per module and document the decision in the Dev Agent Record.

### Testing Requirements

Use TDD. Write failing tests before production code.

Likely focused tests:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
```

Run broader checks if shared contracts affect compiler/runtime/LSP:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-9 through FR-19]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-1 through AD-6]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E2 Story 2.3]
- [Source: `_bmad-output/implementation-artifacts/m39/2-1-define-four-reality-roots.md` - root descriptors]
- [Source: `_bmad-output/implementation-artifacts/m39/2-2-remove-spatial-facts-from-projection-models.md` - projection spatial cleanup]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 2.2 entered review.
- Started development from sprint status after story creation.
- Used CodeGraph before editing the four reality roots.
- Focused tests passed sequentially:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
- Full regression and audits passed:
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added `RealityDeclaration`, `RealityIdentityRule`, `RealityValidationIssue`, and `RealityValidationResult` as concrete product contracts in engineering-model.
- Engineering, Projection, Spatial, and Presentation realities now declare identity rules, required facts, and direct validation functions.
- Validation issues name the failing reality and use plain missing-fact messages.
- Removed milestone wording from touched reality declarations and renamed projection resolved-subject source file away from vague evidence naming.

### File List

- _bmad-output/implementation-artifacts/m39/2-3-declare-reality-authority-and-validation.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/RealityDeclaration.kt
- kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringReality.kt
- kernel/engineering-model/src/test/kotlin/com/engineeringood/athena/ir/EngineeringRealityTest.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReality.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionResolvedSubject.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSemanticEvidence.kt (deleted)
- kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionRealityTest.kt
- kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt
- kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationReality.kt
- kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationRealityTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Stories 2.1-2.2 output.
- 2026-08-01: Started development.
- 2026-08-01: Implemented reality authority, identity rules, required facts, validation boundaries, and current-product naming cleanup.
