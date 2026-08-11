---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 2.5: Persist FunctionRepresentationBinding

Status: done

## Story

As an engineer,
I want explicit source-owned Function representation bindings,
so one Function can use multiple projections without losing semantic identity.

## Acceptance Criteria

1. Native `FunctionRepresentationBinding` has stable persisted identity and explicit key
   `(functionId, projectionId, bindingRole)`; one active binding is allowed per key.
2. Binding references only admitted Element item identity, optional Variant identity, typed
   Placeholder values, compatibility constraints, package trace, and provenance. It owns no Part,
   Relationship, EngineeringPort, or raw geometry facts.
3. Representation binding changes preserve Entity, Function, EngineeringPort, Relationship, occurrence,
   and Source Trace identity. Replacing Element, Variant, or Placeholder values does not allocate a new
   binding identity; delete/recreate does.
4. Binding authoring uses the existing `.binding.athena` companion, typed `ChangeSymbol`/representation
   operation, full Source Revision CAS, staged compile, durable transaction, journal, and rollback path.
5. Missing, ambiguous, non-READY, incompatible, duplicate, stale, or compile-invalid binding leaves
   source bytes, lock, revision, scene, and journal unchanged.
6. Canonical binding payload and Source Revision digest are deterministic independent of map/declaration
   order and exclude machine-local absolute paths.

## Tasks / Subtasks

- [x] Task 1: Define source-owned FunctionRepresentationBinding identity, payload, and canonical digest.
- [x] Task 2: Resolve Element/Variant/Placeholder compatibility against admitted package snapshot.
- [x] Task 3: Persist/update bindings through existing companion and typed transaction paths.
- [x] Task 4: Add model, runtime/compiler, LSP, and frontend contract regression tests; run sequential audits.

## Dev Notes

### Mandatory architecture

- Follow M45 AD-1, AD-3, AD-4, AD-7, AD-8, AD-10, AD-12, AD-13, AD-14, and AD-18.
- Athena source and `.binding.athena` companion own binding meaning. Package items provide reusable
  Element/Variant/Placeholder facts. No second registry, ledger, compatibility adapter, or fallback path.
- Existing `RepresentationBindingCompanionEditor`, `ChangeSymbol`, `SourceRevisionService`,
  `SourceTransactionEngine`, `EditOperationService`, and `RepresentationAndEngineeringOperationHandler`
  are extension seams. Refactor them; do not duplicate or preserve stale entity-level behavior.
- `.elmt`, HTML, XML, and `reference/` trees are reference-only and must not enter compiler/runtime.
- Canonical Scene remains renderer-neutral; Konva/Theia remain typed clients.
- Placement remains `SemanticPlacementIntent -> LogicalLayoutCoordinate -> compiler-derived
  PhysicalGeometryCoordinate`; binding never stores raw canvas X/Y.

### Binding contract

```text
FunctionRepresentationBinding
  bindingId (stable source identity)
  functionId
  projectionId
  bindingRole
  elementItemRef (packageId/itemId/itemVersion)
  variantItemRef?
  placeholderValues (typed, non-interface fields only)
  compatibilityConstraints
  packageTrace
  provenance
```

Binding identity must survive recompilation and representation updates. Scene occurrence identity is
separate. Element/Variant changes must preserve Function, Entity, Port, Relationship, trace, and
occurrence identities. Interface fingerprint changes require a new admitted Element item/version.

### Testing requirements

- Red-green-refactor: author failing tests before implementation.
- Model tests: stable identity/cardinality, canonical ordering, no Part/Relationship/geometry fields.
- Runtime/compiler tests: READY-only resolution, interface fingerprint preservation, typed placeholder
  validation, missing/ambiguous/duplicate diagnostics, deterministic digest.
- LSP/transaction tests: valid update, stale Source Revision, compile failure, rollback, unchanged
  source/lock/scene/journal on rejection, identity preservation.
- Frontend schema/types must encode typed intent only; no source or scene patching.
- Run sequentially: `:kernel:package-model:test`, `:kernel:package-runtime:test`,
  `:kernel:interaction-model:test`, `:ide:lsp:test`.
- Run `tools/encoding-audit.ps1` and `tools/source-set-hygiene-audit.ps1`.

### References

- `_bmad-output/planning-artifacts/m45/epics.md` Epic 2 / Story 2.5.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md` Sections 2-5 and 9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md` AD-7, AD-8, AD-12, AD-13, AD-18.
- `AGENTS.md` pre-1.0, source-set hygiene, E2E, and visual golden rules.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story 2.4 established source-owned Part binding and companion transaction path.
- Replaced old subject-to-element companion mapping with explicit binding id, Function, projection, and role.
- Sequential model, runtime, language, interaction, and LSP tests passed. Encoding and source-set hygiene audits passed.

### Completion Notes List

- Added FunctionRepresentationBinding identity/payload and READY-only admission contract.
- Binding companion now requires stable `id`, `projection`, `role`, and Function selector facts.
- ChangeSymbol remains source transaction with full revision CAS; UI occurrence resolves unique Function binding.
- Binding payload ordering is canonical; Source Revision already includes companion bytes.

### File List

- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/FunctionRepresentationBindingContracts.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/FunctionRepresentationBindingContractsTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/FunctionRepresentationBindingAdmission.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/FunctionRepresentationBindingAdmissionTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationBindingCompanion.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt`
- `examples/m44/rolling-shutter/src/com/engineeringood/m44/rollingshutter/rolling-shutter.binding.athena`
- `_bmad-output/implementation-artifacts/m45/2-5-persist-functionrepresentationbinding.md`

### Change Log

- 2026-08-09: Created Story 2.5 context; status `ready-for-dev`.
- 2026-08-09: Implemented FunctionRepresentationBinding authority, companion persistence, admission, and regression proof; status `review`.
