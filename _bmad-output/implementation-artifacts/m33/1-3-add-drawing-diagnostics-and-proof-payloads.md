---
story_id: 1.3
story_key: 1-3-add-drawing-diagnostics-and-proof-payloads
epic: 1
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 1.3: Add Drawing Diagnostics And Proof Payloads

## Status

Done

## Story

As a maintainer, I want drawing diagnostics and proof payloads so visual correctness is machine
checkable.

## Acceptance Criteria

- Diagnostics identify failed authority: symbol anatomy, Graphic Primitive IR, sheet composition,
  route anchor, renderer adapter, Workbench chrome, or package binding.
- Proof payloads serialize without Theia/browser runtime.
- Proof payloads can be transported through existing product/LSP payload patterns.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED tests for all seven M33 diagnostic authority classifications. (AC: 1)
- [x] Add RED tests for deterministic transport-safe proof payload mapping and fail-closed
  acceptance. (AC: 2,3)
- [x] Implement generic drawing diagnostic, proof fact, proof payload, and plain transport DTO
  contracts. (AC: 1..3)
- [x] Implement diagnostic mapping from symbol-anatomy and Graphic Primitive IR validators. (AC:
  1,3)
- [x] Audit payload naming and retained M32 proof conventions for generic M33 language. (AC: 4)
- [x] Run focused, module, and repository regression tests sequentially on Windows. (AC: 1..4)
- [x] Complete mandatory AC-to-evidence and polish/purge review. (AC: 4)

## Dev Notes

- Bind to M33 architecture AD-10.
- Reuse M32 package evidence payload conventions where possible.
- Avoid screenshot-only proof.

## Testing

- Unit tests for diagnostic authority classification.
- Payload mapping tests for transport-safe serialization.

## Evidence Plan

- Tests prove proof payloads and diagnostics.
- Cleanup ledger records any retained legacy proof path.

## Polish And Purge

Audit proof payload names for M33 generic drawing language, not IEC-only vocabulary.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- CodeGraph inspected M32 `BindingEvidencePayload`, `M32ProductSmokeProof`, and LSP presentation
  payload conventions before implementation.
- RED: focused test failed during test compilation because drawing proof authorities, facts,
  diagnostics, transport DTOs, and mappers did not exist.
- GREEN: focused `DrawingProofPayloadContractTest` passed after adding the minimal contracts.
- REGRESSION: `:kernel:representation-model:test` passed.
- FULL REGRESSION: `gradlew test` passed with 147 actionable tasks (25 executed, 122 up-to-date).
- TEXT: `tools/encoding-audit.ps1` passed.
- REVIEW RED: acceptance audit failed because in-memory DTO mapping did not prove an actual LSP
  adapter and proof acceptance allowed partial/empty/unknown-schema evidence.
- REVIEW GREEN: focused proof/LSP tests, full `:ide:lsp:test`, full `gradlew test`, and encoding audit
  passed after the review fixes.

### Completion Notes List

- Added seven generic drawing proof authorities: symbol anatomy, Graphic Primitive IR, sheet
  composition, route anchor, renderer adapter, Workbench chrome, and package binding.
- Added structured-proof-only acceptance with fail-closed fact and error-diagnostic checks.
- Added deterministic transport DTOs composed only of strings, booleans, lists, and maps, matching
  existing product/LSP payload conventions without importing LSP or browser code.
- Added bridge mappers for Story 1.1 and Story 1.2 diagnostics.
- AC-to-evidence: AC1 is covered by authority and mapper tests; AC2 and AC3 are covered by
  deterministic transport and fail-closed tests; AC4 is covered by full regression, encoding audit,
  generic-language scan, and this record.
- Polish/purge: no IEC-only proof names were introduced; M32 milestone proof remains valid historical
  evidence and does not require a cleanup-ledger entry.
- Review follow-up: added required-authority coverage, supported-schema checks, non-empty evidence,
  unique fact ids, canonical ordering, and a production LSP-owned mapper with mapping test.
- Final acceptance review: PASS for AC1-AC4; the final auditor confirmed the fixed seven-authority
  proof gate at `DrawingProofPayloads.kt:106-117` and the LSP adapter evidence.

### File List

- `_bmad-output/implementation-artifacts/m33/1-3-add-drawing-diagnostics-and-proof-payloads.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingProofPayloads.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/DrawingProofPayloadContractTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingProofPayloads.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingProofPayloadMapperTest.kt`

## Change Log

- 2026-07-23: Implemented and verified generic drawing diagnostics and structured proof payloads.
- 2026-07-23: Addressed Epic 1 code-review findings for complete proof acceptance and LSP transport.
- 2026-07-23: Final acceptance review passed; story closed.
