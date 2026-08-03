---
story_key: 2-3-prove-projection-neutral-semantic-truth
epic: m37-e2
requirements: [FR-4, FR-23, FR-24]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.3: Prove Projection-Neutral Semantic Truth

Status: review

## Story

As an engineering platform evaluator,
I want the same Athena source compiled through different Projection Policies,
so that Athena proves one semantic truth instead of separate Cabinet and drawing worlds.

## Acceptance Criteria

1. A dedicated M37 source fixture declares at least two Projection Policies: one professional connection drawing policy and one Cabinet policy.
2. Structural compilation lowers the same source through both policies and preserves identical contract, Interface, Port, Connection, Connection Intent, External Evidence Mapping, and source provenance identities.
3. Only projection-owned derived facts may differ: placement, routing, sheet/presentation structure, view metadata, and policy-selected output evidence.
4. Projection, renderer payload, SVG, planner graph, or any projection-local block cannot redefine Ports, Connections, Connection Intent, compatibility, Anchors, or External Evidence Mapping.
5. A structured comparison payload records the shared semantic snapshot plus each policy-specific derived snapshot without raw XML, SVG markup, planner-native objects, or renderer-owned facts.
6. The professional connection drawing remains the only visible product-quality M37 surface; Cabinet lowering is structural evidence only in this story.
7. Focused projection/compiler/LSP tests, source-set hygiene audit, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED projection-neutrality tests (AC: 1, 2, 3, 4, 5)
  - [x] Add a compiler test fixture with one source and two policies: `professional-connection-drawing` / `orthogonal-grid` and `cabinet` / `cabinet-layout`.
  - [x] Assert semantic identity sets are identical across policy lowering for contracts, Interfaces, Ports, Connections, Connection Intent, Evidence, and provenance.
  - [x] Assert policy-specific snapshots may differ only in placement/routing/presentation fields.
  - [x] Add a negative fixture where projection-owned engineering truth fails before planning/rendering.
- [x] Implement projection-neutral comparison model (AC: 2, 3, 5)
  - [x] Add only responsibility-named production types if needed, such as `ProjectionSemanticComparison`; do not use `Proof`, `Demo`, `Sample`, milestone names, or `V0`/`V1` in `src/main`.
  - [x] Reuse `EngineeringDocument.projectionPolicies`, `AthenaProjectionPolicyCompiler`, and existing compiler/presentation/projection models instead of creating a parallel projection IR.
  - [x] Compute comparison evidence from actual semantic/derived facts, not constants.
- [x] Wire professional and Cabinet policy consumers cleanly (AC: 2, 3, 6)
  - [x] Keep professional drawing as the visible path and selected-policy consumer.
  - [x] Use Cabinet policy only for structural lower/compare evidence; do not create a separate Cabinet source truth.
  - [x] Ensure target surface is compiler behavior selection, not emitted semantic identity.
- [x] Extend diagnostics and LSP/protocol coverage (AC: 4, 5)
  - [x] Reuse `projection.policy.engineering-truth.forbidden` where projection blocks attempt to author truth.
  - [x] Add deterministic diagnostics for mismatched projection comparison facts if implementation introduces a comparison validator.
  - [x] Ensure payloads carry Athena facts only: no raw XML, SVG markup, planner graph, or renderer inference.
- [x] Verify and record gates (AC: 7)
  - [x] Run focused compiler projection-neutrality tests.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` if LSP/protocol touched.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Athena source remains SSOT. Projection Policy can select projection behavior but cannot own engineering truth.
- Story 2.3 is structural proof of the M37 principle: one semantic source, many typed projections.
- Cabinet is not a product-quality surface here. Use it only to prove projection-neutral semantic identity preservation.
- No compatibility shim. Athena is pre-public: if an old projection path conflicts, directly refactor it to current M37 authority.
- No XML/AML/ECLASS runtime authority. External standards remain Evidence Mapping only.
- No production class with `Proof`, `Demo`, `Sample`, milestone names, or `V0`/`V1`.

### Current Code Intelligence

- Story 2.2 introduced `EngineeringDocument.projectionPolicies` and `AthenaProjectionPolicyCompiler`.
- `AthenaProjectionPolicySelection.materialProjectionContext` maps `professional-connection-drawing` to `schematic` and `cabinet` to `cabinet`.
- `AthenaProfessionalDrawingCompiler` now receives `selectedProjectionPolicy` and uses it as compiler input while the emitted view remains stable (`schematic` / `Control Drawing`).
- `ProjectionModelDeriver` already derives renderer-neutral `ProjectionDocument` from `ViewDefinition`, `EngineeringDocument`, `GeometryDocument`, and knowledge context. Reuse it for structural derived evidence if needed.
- Existing professional drawing path emits `PresentationDocument.routeFactSnapshot`, `graphicOccurrences`, and `drawingComposition`; do not make renderer output authoritative.

### Suggested Fixture Shape

Use compact Athena source, either inline in compiler tests or under an M37-owned test fixture path:

```athena
projection ControlDrawingProjection {
  target professional-connection-drawing
  layout orthogonal-grid
  drawingProfile ControlDrawingIEC
  routeQuality ControlDrawingRouteQuality
  proof semantic-neutrality
  proof source-trace
}

projection CabinetProjection {
  target cabinet
  layout cabinet-layout
  drawingProfile ControlDrawingIEC
  routeQuality ControlDrawingRouteQuality
  proof semantic-neutrality
}
```

Forbidden:

```athena
projection CabinetProjection {
  target cabinet
  layout cabinet-layout
  port Drive.L1 input
  connect hidden Drive.L1 -> Motor.U
}
```

### Comparison Requirements

The shared semantic comparison must include stable sorted IDs for:

- Engineering Connectivity Contracts;
- Interfaces;
- Ports;
- Connections;
- Connection Intent;
- External Evidence Mappings;
- provenance/source spans.

Policy-specific sections may include:

- selected policy identity;
- target surface;
- material projection context;
- placement facts;
- route facts;
- presentation/sheet facts;
- diagnostics.

### TDD And Verification

- RED first: projection-neutral compiler test fails before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.EngineeringConnectivityCompilationTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` if LSP/protocol touched
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 2.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-4, FR-23, FR-24, NFR-1, NFR-2, NFR-5, NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Non-Negotiable Source Principle, Projection Policy, Direct Refactor Targets]
- [Source: `_bmad-output/implementation-artifacts/m37/2-2-select-a-typed-projection-policy.md` - Previous story policy selection and verification notes]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-30: Added RED compiler coverage for one source with professional and Cabinet Projection Policies.
- 2026-07-30: Added negative compiler coverage proving projection-owned engineering truth fails through `projection.policy.engineering-truth.forbidden`.
- 2026-07-30: Implemented `ProjectionSemanticComparisonCompiler` with shared semantic snapshot, per-policy derived snapshots, source provenance IDs, and computed digest.
- 2026-07-30: Verification pass: focused `EngineeringConnectivityCompilationTest`, full `:kernel:compiler:test`, `:ide:lsp:test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Projection-neutral comparison now records one shared semantic snapshot for contracts, Interfaces, Ports, Connections, Connection Intent, External Evidence, and provenance.
- Professional drawing and Cabinet Projection Policies receive separate derived snapshots while sharing the same semantic digest.
- Comparison failure reuses projection policy diagnostics and blocks projection-owned engineering truth before planner or renderer work.

### File List

- _bmad-output/implementation-artifacts/m37/2-3-prove-projection-neutral-semantic-truth.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSemanticComparisonCompiler.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringConnectivityCompilationTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 2.3 from finalized M37 PRD, addendum, epics, and Story 2.2 learnings.
- 2026-07-30: Implemented projection-neutral semantic comparison and compiler tests for shared semantic identity across professional drawing and Cabinet policies.
