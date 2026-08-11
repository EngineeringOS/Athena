---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 2.1: Admit Symbol Geometry and Port Compatibility

Status: done

## Story

As a package author,
I want SVG-backed Symbol metadata,
so geometry and anchor compatibility are complete without moving engineering meaning into the library.

## Acceptance Criteria

1. Symbol metadata admits package-local SVG resource identity, bounds, center/origin, transforms, labels,
   and stable named anchors only when all required fields are complete.
2. Each anchor declares a `PortCompatibilityContract` with direction, domain, and flow kind; missing or
   contradictory compatibility fails closed with exact diagnostics.
3. EngineeringPort remains source authority. Symbol anchors provide reusable compatibility facts only and
   cannot create Entity, Function, Relationship, or semantic Port identity.
4. Unsafe/absolute/traversal resource references and malformed geometry reject admission.
5. Equivalent metadata in different map/order enumeration produces identical normalized payload and digest.

## Tasks / Subtasks

- [x] Task 1: Add native Symbol geometry and compatibility value contracts.
- [x] Task 2: Validate complete metadata, anchors, and package-local resource references through PackageItem admission.
- [x] Task 3: Add deterministic canonical vectors and fail-closed diagnostics.
- [x] Task 4: Run package-model/package-runtime/compiler tests plus encoding and hygiene audits.

## Dev Notes

- Follow M45 AD-3, AD-4, AD-7, AD-9, AD-16, AD-19, and AD-20.
- No `.elmt`, HTML, XML, or `reference/` parser/import/runtime path.
- SVG owns geometry only. Source owns EngineeringPort semantics and identity.
- Keep contracts small and renderer-neutral; Konva/SVG adapters consume admitted facts later.
- Package item kind remains closed `SYMBOL`; no duplicate descriptor or `symbol.yaml` authority.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M45 PRD, architecture spine, Epic 2, and Story 1.1-1.4 intelligence.
- Geometry contract tests caught bounds and duplicate-anchor invariants before runtime admission.

### Completion Notes List

- Added renderer-neutral Symbol geometry, center, bounds, rotation, stable anchors, and compatibility contracts.
- Added fail-closed Symbol geometry admission for undeclared resources and source-owned port authority leakage.
- Sequential package-model and package-runtime tests passed.

### File List

- `_bmad-output/implementation-artifacts/m45/2-1-admit-symbol-geometry-and-port-compatibility.md`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/SymbolGeometryContracts.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/SymbolGeometryContractsTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/SymbolGeometryAdmission.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/SymbolGeometryAdmissionTest.kt`

### Change Log

- 2026-08-08: Created Story 2.1 context; status `ready-for-dev`.
- 2026-08-08: Implemented Symbol geometry and PortCompatibilityContract admission; status `review`.
