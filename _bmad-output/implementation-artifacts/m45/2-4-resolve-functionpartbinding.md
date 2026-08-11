---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 2.4: Resolve FunctionPartBinding

Status: done

## Story

As an engineer,
I want a stable source-owned Part binding for each Function implementation role,
so every projection observes the same validated implementation fact.

## Acceptance Criteria

1. Native `FunctionPartBinding` identity is `(functionId, implementationRole)` and remains stable
   across recompilation and representation changes.
2. One active admitted Part is allowed per `(functionId, implementationRole)`; multiple roles on one
   Function are explicit and independently addressable.
3. Binding resolves only `PACKAGE_READY` Part items and proves function-template/capability/interface
   compatibility before publication.
4. Part selection is an `EngineeringEditOperation` using full Source Revision CAS and the existing
   recoverable transaction path.
5. Invalid, missing, ambiguous, incompatible, or stale Part selection leaves Athena source, lock,
   Source Revision, Canonical Scene, and operation journal unchanged.
6. Binding payload and diagnostics are canonical/deterministic independent of map or declaration order.
7. Part binding never owns Symbol/Element/Variant/Placeholder data, creates Relationships, or changes
   semantic Entity, Function, Engineering Port, Relationship, occurrence, or source-trace identity.

## Tasks / Subtasks

- [x] Task 1: Add source-owned `FunctionPartBinding` identity and canonical payload contracts.
- [x] Task 2: Resolve admitted Part compatibility for Function implementation roles, fail closed.
- [x] Task 3: Route `BindPart` through typed source mutation and preserve transaction/CAS semantics.
- [x] Task 4: Add deterministic model/runtime/LSP regression tests and run sequential validation.

## Dev Notes

### Mandatory architecture

- Follow M45 AD-3, AD-4, AD-5, AD-7, AD-8, AD-12, AD-13, AD-17, AD-18, AD-19.
- Engineering source remains sole authority for Entity, Function, Engineering Port, Relationship,
  and `FunctionPartBinding`.
- Package model/runtime owns reusable Part facts and admission only. Do not copy package facts into
  geometry or create a second registry/ledger.
- `FunctionPartBinding` is projection-independent. It references admitted Part identity and
  compatibility evidence, never Symbol/Element geometry.
- Do not parse `.elmt`, HTML, XML, or anything under `reference/` in compiler/runtime. No compatibility
  adapters or fallback paths.
- Frontend remains typed intent client; it must not patch source or scene directly.
- Persist logical Sheet coordinates only through existing spatial contracts; raw canvas X/Y is never
  Part-binding truth.

### Existing code to extend, not duplicate

- `kernel/package-model/.../PartContracts.kt` and `kernel/package-runtime/.../PartFactsAdmission.kt`
  are the native Part fact/admission seams from Story 2.3.
- `kernel/engineering-model/.../EngineeringAnatomyModels.kt` contains canonical
  `EngineeringFunction`, `EngineeringPort`, and identity/reference contracts.
- `kernel/interaction-model/.../EditOperationContracts.kt` already defines `BindPart`, but its
  current wire/body shape and semantics are entity-level. Replace with Function + implementation role
  authority; do not retain entity-level compatibility behavior.
- `ide/lsp/.../RepresentationAndEngineeringOperationHandler.kt` currently writes an inline
  `part "<package/item@version>"` field. Refactor this path to author the canonical
  `FunctionPartBinding` source form, stage all declared inputs, recompile, and publish only on success.
- Existing `SourceRevisionService`, `SourceTransactionEngine`, `EditOperationService`,
  `EditOperationWireMapper`, and operation-journal contracts must remain the transaction/CAS path.

### Compatibility rules

- Required capability set must be a subset of admitted Part capability contracts.
- Required function-template/interface declarations must match the target Function/Port contract.
- Missing Part, package/item version drift, duplicate active role, unknown Function, ambiguous Part,
  and incompatible capability/interface produce plain diagnostics naming subject, problem, correction.
- Stable binding identity survives Element/Symbol representation changes and scene recompilation.
- Invalid operation must leave before/after bytes, lock, revision, scene, and journal unchanged.

### Testing requirements

- Red-green-refactor: add failing tests before implementation.
- Model tests: identity stability, canonical ordering, role cardinality, no geometry/relationship fields.
- Runtime tests: READY-only resolution, missing/ambiguous/incompatible diagnostics, deterministic payload.
- LSP/transaction tests: valid `BindPart`, stale `SourceRevision`, compile failure, rollback proof,
  unchanged source/lock/scene/journal on rejection, identity preservation.
- Run Gradle verification sequentially only. At minimum:
  `:kernel:package-model:test`, `:kernel:package-runtime:test`,
  `:kernel:interaction-model:test`, `:ide:lsp:test`.
- Run `tools/encoding-audit.ps1` and `tools/source-set-hygiene-audit.ps1`.

### Definition of done

- Every task checked.
- Acceptance criteria backed by passing tests.
- Story Dev Agent Record contains debug log, completion notes, full file list, and change log.
- Status moves to `review` only after sequential regression and audits pass.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M45 Epic 2 after Stories 2.1-2.3.
- Existing `BindPart` path was entity-level and was replaced by Function/role binding.
- Sequential verification passed: package-model, package-runtime, interaction-model, and LSP tests.
- Encoding and source-set hygiene audits passed.

### Completion Notes List

- Added canonical `PartItemReference`, `FunctionPartBinding`, requirements, and admission contracts.
- Added native `role ...` / `use part ... version ...` binding syntax and companion editor integration.
- Routed `BindPart` through full Source Revision CAS and recoverable source transaction path.
- Added deterministic model/runtime/LSP regression coverage, including rejection invariants.

### File List

- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/PartContracts.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/FunctionPartBindingTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/FunctionPartBindingAdmission.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/FunctionPartBindingAdmissionTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/EditOperationContracts.kt`
- `kernel/interaction-model/src/main/resources/schema/athena-edit-operation.schema.json`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/FunctionPartBindingCompanion.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceRevisionService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngine.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandler.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationWireMapper.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt`
- `ide/theia-frontend/src/browser/diagram/generated/types.ts`
- `ide/theia-frontend/scripts/generate-diagram-contracts.mjs`

### Change Log

- 2026-08-09: Implemented FunctionPartBinding contracts, admission, language syntax, source transaction, wire schema, and regression tests; status `review`.
