---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 1.2: Implement PackageItem Authored/Admitted Contracts

Status: done

## Story

As a package author, I want one native `PackageItem` contract, so all Symbol, Element, Part, Macro,
Variant, and Placeholder items share identity without duplicate authorities.

## Acceptance Criteria

1. Authored metadata cannot set digest, admission state, diagnostics, or resolved references.
2. Compiler/runtime admission emits `AdmittedPackageItem` with closed item kinds and normalized payload.
3. Canonical `athena-package-item-c14n-v1` digest vectors are byte deterministic and exclude machine-local paths.
4. Old duplicate package descriptors and `symbol.yaml` admission are removed, not adapted.
5. Invalid authored contracts fail closed with exact subject, problem, and correction diagnostics; no invalid item reaches lock, binding, snapshot, or scene.

## Tasks / Subtasks

- [x] Task 1: Define shared authored/admitted PackageItem contracts (AC: 1, 2)
  - [x] Add closed `PackageItemKind` and `PackageItemMetadata` using `PackageIdentifier`.
  - [x] Add `AdmittedPackageItem` and admission state/diagnostic contracts in package-model/runtime boundary.
  - [x] Reject authored derived fields and unknown item kinds.
- [x] Task 2: Normalize item payload and identity (AC: 2, 5)
  - [x] Validate `(packageId, itemId, itemVersion)` identity and package ownership.
  - [x] Normalize NFC text, forward-slash package-relative resource paths, canonical EngineeringValue text, and declared-set ordering.
  - [x] Keep Symbol/Element/Part/Macro/Variant/Placeholder payloads representation/package facts; never create Engineering Entity, Function, Port, Relationship, or Part binding truth.
- [x] Task 3: Implement canonical digest (AC: 3)
  - [x] Implement `athena-package-item-c14n-v1` canonical JSON/SHA-256 preimage rules from AD-3.
  - [x] Add fixed vector for key sorting, list order, omitted optional fields, NFC text, resource paths, and payload changes.
  - [x] Exclude admission state, diagnostics, resolved references, absolute paths, and machine-local data.
- [x] Task 4: Replace duplicate legacy admission (AC: 4)
  - [x] Delete `EngineeringPackageDescriptor`, `RepresentationPackageDescriptor`, `symbol.yaml`, BindingManifest, and duplicate package ids from active production paths.
  - [x] Remove compiler dependency on retired symbol descriptor authority; future representation binding consumes admitted PackageItem.
- [x] Task 5: Verify staged fail-closed behavior (AC: 1-5)
  - [x] Run focused package-model/package-runtime/compiler tests sequentially.
  - [x] Prove invalid/incomplete, duplicate, and foreign-owner items never publish a `ReadyPackageItemIndex`.
  - [x] Run encoding and source-set hygiene audits; scan for retired duplicate symbols.

## Dev Notes

### Architecture Guardrails

- Follow M45 AD-3, AD-4, AD-7, AD-9, AD-19 in `ARCHITECTURE-SPINE.md`.
- `PackageItemMetadata` is authored once. `AdmittedPackageItem` alone owns normalized payload, resolved references, digest, state, and diagnostics.
- Package metadata describes reusable library facts. Athena source remains authority for Entity, Function, Engineering Port, Relationship, FunctionPartBinding, and FunctionRepresentationBinding.
- SVG is package-local geometry only. `.elmt`, HTML, `reference/elements`, and `reference/elements_contrib` are reference-only and must not gain parser/import/runtime paths.
- No compatibility shims, fallback parsers, duplicate graphs, or legacy adapters.

### Previous Story Intelligence

- Story 1.1 introduced direct `packages/<package-name>/package.yaml` discovery through SnakeYAML Engine and canonical `ResolvedPackageGraph` selection.
- Story 1.1 deleted `representationPackageRoots`, `LocalPackageRegistry`, `LocalPackageResolver`, registry/resolution/cache models, and snapshot-only stager paths.
- Typed `ADD_PACKAGE_DEPENDENCY` now stages `athena.yaml` through Source Revision transaction and materializes lock only after publication.
- Verification pattern: Gradle commands sequential; use focused tests before broad module tests, then encoding/source-set audits.

### Testing Requirements

- Use Kotlin tests with deterministic fixtures. No tests for retired behavior.
- Test all six closed kinds, derived-field rejection, malformed payload, duplicate identity, canonical vectors, provenance, and fail-closed publication.

### References

- [M45 PRD](../../planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md)
- [M45 Epics](../../planning-artifacts/m45/epics.md#story-12-implement-packageitem-authoredadmitted-contracts)
- [M45 Architecture AD-3/AD-4/AD-7/AD-9/AD-19](../../planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md)
- [Repository rules](../../../AGENTS.md)

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

### Completion Notes List

- BMad context created from M45 PRD, architecture spine, epics, sprint status, AGENTS.md, and Story 1.1 implementation intelligence.
- Replaced duplicate package and symbol descriptor contracts with native `PackageItem` authored/admitted phases.
- Added staged `PackageItemPublicationGate`; only an all-ready package can publish `ReadyPackageItemIndex`.
- Removed retired compiler symbol-descriptor input; compiler no longer lets library metadata validate or own Engineering Ports.
- Added deterministic canonical digest vector and fail-closed incomplete/duplicate/foreign-owner tests.
- Sequential verification passed: `:kernel:package-model:test`, `:kernel:package-runtime:test`, `:kernel:compiler:test`, `:ide:lsp:test`, encoding audit, source-set hygiene audit.

### File List

- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/PackageItemModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/PackageItemCanonicalizer.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/PresentationProfileModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageItemAdmission.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageItemPublicationGate.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PackageItemAdmissionTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompilerTest.kt`
- Deleted retired package descriptor, binding resolver, descriptor evidence, and symbol YAML admission files/tests.

### Change Log

- 2026-08-08: Created Story 1.2 context; status `ready-for-dev`.
- 2026-08-08: Implemented and verified Story 1.2; status `review`.
